// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.LongSupplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class LogFileStreams {

    private LogFileStreams() {
    }

    static Stream<String> plainText(
            Path path,
            LogFileReadLimits limits,
            LogFileReadBudget budget) throws IOException {
        return lines(
                Files.newInputStream(path),
                StandardCharsets.UTF_8,
                limits,
                budget,
                path,
                null,
                null);
    }

    static Stream<String> gzip(
            Path path,
            LogFileReadLimits limits,
            LogFileReadBudget budget) throws IOException {
        LimitedGZIPInputStream gzip = new LimitedGZIPInputStream(
                Files.newInputStream(path),
                budget,
                limits,
                path);
        try {
            return decodedLines(
                    gzip,
                    Charset.defaultCharset(),
                    limits,
                    path,
                    null);
        } catch (RuntimeException | Error failure) {
            closeAfterFailure(gzip, failure);
            throw failure;
        }
    }

    static Stream<String> firstZipEntry(
            Path path,
            LogFileReadLimits limits,
            LogFileReadBudget budget) throws IOException {
        ZipArchivePreflight.Index index =
                ZipArchivePreflight.validate(path, limits);
        ZipArchivePreflight.Entry entry = index.firstFile();
        if (entry == null) {
            throw new IOException("ZIP file contains no readable entries: " + path);
        }
        return zipEntry(path, index, entry, limits, budget);
    }

    static Stream<String> zipEntry(
            Path path,
            String entryName,
            LogFileReadLimits limits,
            LogFileReadBudget budget) throws IOException {
        ZipArchivePreflight.Index index =
                ZipArchivePreflight.validate(path, limits);
        ZipArchivePreflight.Entry entry = index.find(entryName);
        if (entry == null || entry.isDirectory()) {
            throw new IOException("ZIP entry not found: " + path + "!" + entryName);
        }
        return zipEntry(path, index, entry, limits, budget);
    }

    static Stream<String> segment(
            LogFileSegment segment,
            LogFileReadLimits limits,
            LogFileReadBudget budget) {
        try {
            if (segment instanceof GCLogFileZipSegment) {
                return ((GCLogFileZipSegment) segment).stream(budget);
            }
            return plainText(segment.getPath(), limits, budget);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static Stream<String> lines(
            InputStream inputStream,
            Charset charset,
            LogFileReadLimits limits,
            LogFileReadBudget budget,
            Path path,
            String archiveEntry,
            LongSupplier compressedBytes) {
        LimitedInputStream limited = new LimitedInputStream(
                inputStream,
                budget,
                limits,
                path,
                archiveEntry,
                compressedBytes);
        return decodedLines(limited, charset, limits, path, archiveEntry);
    }

    private static Stream<String> decodedLines(
            InputStream inputStream,
            Charset charset,
            LogFileReadLimits limits,
            Path path,
            String archiveEntry) {
        BoundedLineSpliterator lines = new BoundedLineSpliterator(
                new InputStreamReader(inputStream, charset),
                limits.getMaxLineCharacters(),
                path,
                archiveEntry);
        return StreamSupport.stream(lines, false).onClose(lines::closeUnchecked);
    }

    static Stream<String> zipEntry(
            Path path,
            ZipArchivePreflight.Index index,
            ZipArchivePreflight.Entry entry,
            LogFileReadLimits limits,
            LogFileReadBudget budget) throws IOException {
        budget.rejectDeclaredSize(entry.getExpandedSize(), path, entry.getName());
        if (entry.getExpandedSize() > limits.getCompressionRatioGraceBytes()) {
            double ratio = entry.getCompressedSize() == 0L
                    ? Double.POSITIVE_INFINITY
                    : (double) entry.getExpandedSize() / (double) entry.getCompressedSize();
            if (ratio > limits.getMaxCompressionRatio()) {
                throw new LogFileReadLimitExceededException(
                        LogFileReadLimitExceededException.LimitType.COMPRESSION_RATIO,
                        path,
                        entry.getName(),
                        Double.toString(limits.getMaxCompressionRatio()),
                        Double.toString(ratio));
            }
        }
        long archiveSliceLength = index.getCentralDirectoryOffset() - entry.getLocalHeaderOffset();
        ArchiveSliceInputStream archiveSlice = new ArchiveSliceInputStream(
                path,
                entry.getLocalHeaderOffset(),
                archiveSliceLength);
        try {
            ValidatedZipEntryInputStream validated =
                    new ValidatedZipEntryInputStream(archiveSlice, entry, path);
            return lines(
                    validated,
                    Charset.defaultCharset(),
                    limits,
                    budget,
                    path,
                    entry.getName(),
                    validated::getCompressedBytesRead);
        } catch (IOException | RuntimeException | Error failure) {
            closeAfterFailure(archiveSlice, failure);
            throw failure;
        }
    }

    private static void closeAfterFailure(AutoCloseable closeable, Throwable failure) {
        try {
            closeable.close();
        } catch (Exception closeException) {
            failure.addSuppressed(closeException);
        }
    }
}
