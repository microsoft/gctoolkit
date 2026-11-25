// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.InputStreamStatistics;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.CRC32;
import java.util.zip.ZipException;

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
        ZipEntryReference entry = zipEntries(path, limits).stream()
                .filter(candidate -> !candidate.isDirectory())
                .findFirst()
                .orElse(null);
        if (entry == null) {
            throw new IOException("ZIP file contains no readable entries: " + path);
        }
        return zipEntry(path, entry, limits, budget);
    }

    static Stream<String> zipEntry(
            Path path,
            String entryName,
            LogFileReadLimits limits,
            LogFileReadBudget budget) throws IOException {
        ZipEntryReference entry = zipEntries(path, limits).stream()
                .filter(candidate -> !candidate.isDirectory())
                .filter(candidate -> candidate.getName().equals(entryName))
                .findFirst()
                .orElse(null);
        if (entry == null) {
            throw new IOException("ZIP entry not found: " + path + "!" + entryName);
        }
        return zipEntry(path, entry, limits, budget);
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

    static List<ZipEntryReference> zipEntries(
            Path path,
            LogFileReadLimits limits) throws IOException {
        SeekableByteChannel channel = openValidatedZipChannel(path, limits);
        ZipFile zipFile;
        try {
            zipFile = openZipFile(channel);
        } catch (IOException exception) {
            closeAfterFailure(channel, exception);
            throw exception;
        }
        try (ZipFile closeableZipFile = zipFile) {
            List<ZipEntryReference> references = new ArrayList<>();
            Map<String, Integer> occurrences = new HashMap<>();
            Enumeration<ZipArchiveEntry> entries = closeableZipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                if (references.size() == limits.getMaxArchiveEntries()) {
                    throw new LogFileReadLimitExceededException(
                            LogFileReadLimitExceededException.LimitType.ARCHIVE_ENTRIES,
                            path,
                            null,
                            Integer.toString(limits.getMaxArchiveEntries()),
                            Integer.toString(references.size() + 1));
                }
                if (!entry.isDirectory()) {
                    validateEntryMetadata(closeableZipFile, entry, path);
                }
                int occurrence = occurrences.getOrDefault(entry.getName(), 0);
                occurrences.put(entry.getName(), occurrence + 1);
                references.add(new ZipEntryReference(
                        entry.getName(),
                        occurrence,
                        entry.isDirectory(),
                        entry.getMethod(),
                        entry.getCrc(),
                        entry.getCompressedSize(),
                        entry.getSize()));
            }
            return references;
        }
    }

    static Stream<String> zipEntry(
            Path path,
            ZipEntryReference reference,
            LogFileReadLimits limits,
            LogFileReadBudget budget) throws IOException {
        SeekableByteChannel channel = openValidatedZipChannel(path, limits);
        ZipFile zipFile;
        try {
            zipFile = openZipFile(channel);
        } catch (IOException | RuntimeException | Error failure) {
            closeAfterFailure(channel, failure);
            throw failure;
        }
        try {
            ZipArchiveEntry entry = resolveEntry(zipFile, reference, path);
            validateEntryMetadata(zipFile, entry, path);
            budget.rejectDeclaredSize(entry.getSize(), path, entry.getName());
            if (entry.getSize() > limits.getCompressionRatioGraceBytes()) {
                double ratio = entry.getCompressedSize() == 0L
                    ? Double.POSITIVE_INFINITY
                    : (double) entry.getSize() / (double) entry.getCompressedSize();
                if (ratio > limits.getMaxCompressionRatio()) {
                    throw new LogFileReadLimitExceededException(
                            LogFileReadLimitExceededException.LimitType.COMPRESSION_RATIO,
                            path,
                            entry.getName(),
                            Double.toString(limits.getMaxCompressionRatio()),
                            Double.toString(ratio));
                }
            }

            InputStream inputStream = zipFile.getInputStream(entry);
            if (!(inputStream instanceof InputStreamStatistics)) {
                throw new IOException("ZIP entry stream does not expose read statistics: "
                        + path + "!" + entry.getName());
            }
            ValidatedZipInputStream validated = new ValidatedZipInputStream(
                    inputStream,
                    (InputStreamStatistics) inputStream,
                    zipFile,
                    entry,
                    path);
            return lines(
                    validated,
                    Charset.defaultCharset(),
                    limits,
                    budget,
                    path,
                    entry.getName(),
                    validated::getCompressedBytesRead);
        } catch (IOException | RuntimeException | Error failure) {
            closeAfterFailure(zipFile, failure);
            throw failure;
        }
    }

    private static SeekableByteChannel openValidatedZipChannel(
            Path path,
            LogFileReadLimits limits) throws IOException {
        SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ);
        try {
            ZipArchivePreflight.validate(channel, path, limits);
            return channel;
        } catch (IOException | RuntimeException | Error failure) {
            closeAfterFailure(channel, failure);
            throw failure;
        }
    }

    private static ZipFile openZipFile(SeekableByteChannel channel) throws IOException {
        return ZipFile.builder().setSeekableByteChannel(channel).get();
    }

    private static ZipArchiveEntry resolveEntry(
            ZipFile zipFile,
            ZipEntryReference reference,
            Path path) throws IOException {
        int occurrence = 0;
        for (ZipArchiveEntry entry : zipFile.getEntries(reference.getName())) {
            if (occurrence++ == reference.occurrence) {
                if (!reference.matches(entry)) {
                    throw new ZipException(
                            "ZIP entry metadata changed while reading: "
                                    + path + "!" + reference.getName());
                }
                return entry;
            }
        }
        throw new IOException("ZIP entry not found: " + path + "!" + reference.getName());
    }

    private static void validateEntryMetadata(
            ZipFile zipFile,
            ZipArchiveEntry entry,
            Path path) throws ZipException {
        if (!zipFile.canReadEntryData(entry)) {
            throw new ZipException(
                    "Unsupported or encrypted ZIP entry: " + path + "!" + entry.getName());
        }
        if (entry.getSize() < 0L || entry.getCompressedSize() < 0L || entry.getCrc() < 0L) {
            throw new ZipException(
                    "ZIP entry has incomplete metadata: " + path + "!" + entry.getName());
        }
    }

    private static void closeAfterFailure(AutoCloseable closeable, Throwable failure) {
        try {
            closeable.close();
        } catch (Exception closeException) {
            failure.addSuppressed(closeException);
        }
    }

    static final class ZipEntryReference {
        private final String name;
        private final int occurrence;
        private final boolean directory;
        private final int method;
        private final long crc;
        private final long compressedSize;
        private final long expandedSize;

        private ZipEntryReference(
                String name,
                int occurrence,
                boolean directory,
                int method,
                long crc,
                long compressedSize,
                long expandedSize) {
            this.name = name;
            this.occurrence = occurrence;
            this.directory = directory;
            this.method = method;
            this.crc = crc;
            this.compressedSize = compressedSize;
            this.expandedSize = expandedSize;
        }

        String getName() {
            return name;
        }

        boolean isDirectory() {
            return directory;
        }

        private boolean matches(ZipArchiveEntry entry) {
            return directory == entry.isDirectory()
                    && method == entry.getMethod()
                    && crc == entry.getCrc()
                    && compressedSize == entry.getCompressedSize()
                    && expandedSize == entry.getSize();
        }
    }

    private static final class ValidatedZipInputStream extends InputStream {
        private final InputStream inputStream;
        private final InputStreamStatistics statistics;
        private final ZipFile zipFile;
        private final ZipArchiveEntry entry;
        private final Path path;
        private final CRC32 crc = new CRC32();
        private final byte[] singleByte = new byte[1];
        private long expandedBytes;
        private boolean validated;
        private boolean closed;

        private ValidatedZipInputStream(
                InputStream inputStream,
                InputStreamStatistics statistics,
                ZipFile zipFile,
                ZipArchiveEntry entry,
                Path path) {
            this.inputStream = inputStream;
            this.statistics = statistics;
            this.zipFile = zipFile;
            this.entry = entry;
            this.path = path;
        }

        long getCompressedBytesRead() {
            return statistics.getCompressedCount();
        }

        @Override
        public int read() throws IOException {
            int count = read(singleByte, 0, 1);
            return count == -1 ? -1 : Byte.toUnsignedInt(singleByte[0]);
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            int count = inputStream.read(bytes, offset, length);
            if (count > 0) {
                crc.update(bytes, offset, count);
                expandedBytes += count;
            } else if (count == -1) {
                validate();
            }
            return count;
        }

        private void validate() throws ZipException {
            if (validated) {
                return;
            }
            validated = true;
            if (expandedBytes != entry.getSize()) {
                throw new ZipException(
                        "ZIP expanded size does not match central directory: " + path);
            }
            if (statistics.getCompressedCount() != entry.getCompressedSize()) {
                throw new ZipException(
                        "ZIP compressed size does not match central directory: " + path);
            }
            if (crc.getValue() != entry.getCrc()) {
                throw new ZipException("ZIP CRC does not match central directory: " + path);
            }
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            IOException failure = null;
            try {
                inputStream.close();
            } catch (IOException exception) {
                failure = exception;
            }
            try {
                zipFile.close();
            } catch (IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
            if (failure != null) {
                throw failure;
            }
        }
    }
}
