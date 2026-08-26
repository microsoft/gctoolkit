// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

final class LimitedGZIPInputStream extends InputStream {

    private static final int BUFFER_SIZE = 8192;
    private static final int FLAG_HEADER_CRC = 2;
    private static final int FLAG_EXTRA = 4;
    private static final int FLAG_NAME = 8;
    private static final int FLAG_COMMENT = 16;
    private static final int FLAG_RESERVED = 224;

    private final CountingInputStream compressedSource;
    private final PushbackInputStream source;
    private final Inflater inflater = new Inflater(true);
    private final CRC32 crc = new CRC32();
    private final CRC32 headerCrc = new CRC32();
    private final byte[] compressedBuffer = new byte[BUFFER_SIZE];
    private final byte[] singleByte = new byte[1];
    private final LogFileReadBudget budget;
    private final LogFileReadLimits limits;
    private final Path path;

    private boolean memberOpen;
    private boolean endOfStream;
    private boolean closed;
    private int lastInputLength;
    private long headerBytes;
    private long memberExpandedBytes;
    private int memberCount;

    LimitedGZIPInputStream(
            InputStream inputStream,
            LogFileReadBudget budget,
            LogFileReadLimits limits,
            Path path) {
        compressedSource = new CountingInputStream(
                inputStream,
                limits.getMaxCompressedBytes(),
                path);
        source = new PushbackInputStream(compressedSource, BUFFER_SIZE);
        this.budget = budget;
        this.limits = limits;
        this.path = path;
    }

    @Override
    public int read() throws IOException {
        int count = read(singleByte, 0, 1);
        return count == -1 ? -1 : Byte.toUnsignedInt(singleByte[0]);
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        ensureOpen();
        if (length == 0) {
            return 0;
        }

        while (!endOfStream) {
            if (!memberOpen && !openMember()) {
                endOfStream = true;
                return -1;
            }

            try {
                int permittedLength = budget.nextReadLength(length);
                int count = inflater.inflate(bytes, offset, permittedLength);
                if (count > 0) {
                    crc.update(bytes, offset, count);
                    budget.record(count, path, null);
                    memberExpandedBytes += count;
                    budget.recordCompressedExpansion(
                            count,
                            compressedSource.getBytesRead(),
                            limits,
                            path,
                            null);
                    checkCompressionRatio();
                    return count;
                }
                if (inflater.finished()) {
                    finishMember();
                } else if (inflater.needsDictionary()) {
                    throw new ZipException("GZIP member requires a preset dictionary: " + path);
                } else if (inflater.needsInput()) {
                    fillInflater();
                } else {
                    throw new ZipException("Unable to make progress while inflating " + path);
                }
            } catch (DataFormatException exception) {
                ZipException failure = new ZipException("Invalid GZIP data: " + path);
                failure.initCause(exception);
                throw failure;
            }
        }
        return -1;
    }

    private boolean openMember() throws IOException {
        int magic1 = source.read();
        if (magic1 == -1) {
            return false;
        }
        if (++memberCount > limits.getMaxArchiveEntries()) {
            throw new LogFileReadLimitExceededException(
                    LogFileReadLimitExceededException.LimitType.COMPRESSED_MEMBERS,
                    path,
                    null,
                    Integer.toString(limits.getMaxArchiveEntries()),
                    Integer.toString(memberCount));
        }
        headerCrc.reset();
        headerCrc.update(magic1);
        headerBytes = 1L;
        checkHeaderBytes();
        int magic2 = readHeaderByte();
        if (magic1 != 0x1f || magic2 != 0x8b) {
            throw new ZipException("Invalid GZIP header: " + path);
        }
        if (readHeaderByte() != 8) {
            throw new ZipException("Unsupported GZIP compression method: " + path);
        }
        int flags = readHeaderByte();
        if ((flags & FLAG_RESERVED) != 0) {
            throw new ZipException("Invalid GZIP flags: " + path);
        }
        skipHeaderBytes(6);
        if ((flags & FLAG_EXTRA) != 0) {
            int extraLength = readHeaderByte() | (readHeaderByte() << 8);
            skipHeaderBytes(extraLength);
        }
        if ((flags & FLAG_NAME) != 0) {
            skipZeroTerminatedHeaderField();
        }
        if ((flags & FLAG_COMMENT) != 0) {
            skipZeroTerminatedHeaderField();
        }
        if ((flags & FLAG_HEADER_CRC) != 0) {
            int expectedHeaderCrc = readStoredHeaderByte() | (readStoredHeaderByte() << 8);
            if (expectedHeaderCrc != ((int) headerCrc.getValue() & 0xffff)) {
                throw new ZipException("Corrupt GZIP header: " + path);
            }
        }

        inflater.reset();
        crc.reset();
        memberExpandedBytes = 0L;
        lastInputLength = 0;
        memberOpen = true;
        return true;
    }

    private void fillInflater() throws IOException {
        int count = source.read(compressedBuffer);
        if (count == -1) {
            throw new EOFException("Unexpected end of GZIP member: " + path);
        }
        lastInputLength = count;
        inflater.setInput(compressedBuffer, 0, count);
    }

    private void finishMember() throws IOException {
        int remaining = inflater.getRemaining();
        if (remaining > 0) {
            source.unread(compressedBuffer, lastInputLength - remaining, remaining);
        }

        long expectedCrc = readLittleEndianUnsignedInt();
        long expectedSize = readLittleEndianUnsignedInt();
        if (expectedCrc != crc.getValue()) {
            throw new ZipException("Corrupt GZIP CRC: " + path);
        }
        if (expectedSize != (memberExpandedBytes & 0xffffffffL)) {
            throw new ZipException("Corrupt GZIP size: " + path);
        }
        memberOpen = false;
    }

    private void checkCompressionRatio() {
        if (memberExpandedBytes <= limits.getCompressionRatioGraceBytes()) {
            return;
        }
        long compressedBytes = headerBytes + inflater.getBytesRead();
        double ratio = compressedBytes == 0L
                ? Double.POSITIVE_INFINITY
                : (double) memberExpandedBytes / (double) compressedBytes;
        if (ratio > limits.getMaxCompressionRatio()) {
            throw new LogFileReadLimitExceededException(
                    LogFileReadLimitExceededException.LimitType.COMPRESSION_RATIO,
                    path,
                    null,
                    Double.toString(limits.getMaxCompressionRatio()),
                    Double.toString(ratio));
        }
    }

    private int readHeaderByte() throws IOException {
        int value = source.read();
        if (value == -1) {
            throw new EOFException("Unexpected end of GZIP header: " + path);
        }
        headerBytes++;
        checkHeaderBytes();
        headerCrc.update(value);
        return value;
    }

    private int readStoredHeaderByte() throws IOException {
        int value = source.read();
        if (value == -1) {
            throw new EOFException("Unexpected end of GZIP header: " + path);
        }
        headerBytes++;
        checkHeaderBytes();
        return value;
    }

    private void checkHeaderBytes() {
        if (headerBytes > limits.getMaxGzipHeaderBytes()) {
            throw new LogFileReadLimitExceededException(
                    LogFileReadLimitExceededException.LimitType.HEADER_BYTES,
                    path,
                    null,
                    Integer.toString(limits.getMaxGzipHeaderBytes()),
                    Long.toString(headerBytes));
        }
    }

    private void skipHeaderBytes(int count) throws IOException {
        for (int index = 0; index < count; index++) {
            readHeaderByte();
        }
    }

    private void skipZeroTerminatedHeaderField() throws IOException {
        while (readHeaderByte() != 0) {
            // Continue to the field terminator.
        }
    }

    private long readLittleEndianUnsignedInt() throws IOException {
        long value = 0L;
        for (int index = 0; index < 4; index++) {
            int next = source.read();
            if (next == -1) {
                throw new EOFException("Unexpected end of GZIP trailer: " + path);
            }
            value |= (long) next << (8 * index);
        }
        return value;
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            inflater.end();
            source.close();
        }
    }
}
