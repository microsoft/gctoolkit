// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.zip.ZipException;

final class ZipArchivePreflight {

    private static final long END_OF_CENTRAL_DIRECTORY = 0x06054b50L;
    private static final long ZIP64_END_OF_CENTRAL_DIRECTORY = 0x06064b50L;
    private static final long ZIP64_END_OF_CENTRAL_DIRECTORY_LOCATOR = 0x07064b50L;
    private static final long CENTRAL_DIRECTORY_ENTRY = 0x02014b50L;
    private static final long CENTRAL_DIRECTORY_ARCHIVE_EXTRA_DATA = 0x08064b50L;
    private static final long CENTRAL_DIRECTORY_DIGITAL_SIGNATURE = 0x05054b50L;
    private static final int MAXIMUM_COMMENT_LENGTH = 0xffff;
    private static final int END_OF_CENTRAL_DIRECTORY_SIZE = 22;

    private ZipArchivePreflight() {
    }

    static void validate(
            SeekableByteChannel channel,
            Path path,
            LogFileReadLimits limits) throws IOException {
        Reader file = new Reader(channel);
        if (file.length() > limits.getMaxCompressedBytes()) {
            throw new LogFileReadLimitExceededException(
                    LogFileReadLimitExceededException.LimitType.COMPRESSED_BYTES,
                    path,
                    null,
                    Long.toString(limits.getMaxCompressedBytes()),
                    Long.toString(file.length()));
        }

        Directory directory = readDirectory(file, path);
        if (directory.entryCount > limits.getMaxArchiveEntries()) {
            throw limit(
                    LogFileReadLimitExceededException.LimitType.ARCHIVE_ENTRIES,
                    path,
                    limits.getMaxArchiveEntries(),
                    directory.entryCount);
        }
        if (directory.size > limits.getMaxArchiveMetadataBytes()) {
            throw limit(
                    LogFileReadLimitExceededException.LimitType.ARCHIVE_METADATA_BYTES,
                    path,
                    limits.getMaxArchiveMetadataBytes(),
                    directory.size);
        }

        long directoryEnd = directory.offset + directory.size;
        long entryCount = 0L;
        long recordCount = 0L;
        long maximumRecords = (long) limits.getMaxArchiveEntries() + 2L;
        file.seek(directory.offset);
        while (file.position() < directoryEnd) {
            if (++recordCount > maximumRecords) {
                throw limit(
                        LogFileReadLimitExceededException.LimitType.ARCHIVE_ENTRIES,
                        path,
                        maximumRecords,
                        recordCount);
            }
            long signature = file.readUnsignedInt();
            if (signature == CENTRAL_DIRECTORY_ENTRY) {
                entryCount++;
                file.skip(24L, directoryEnd, path);
                int nameLength = file.readUnsignedShort();
                int extraLength = file.readUnsignedShort();
                int commentLength = file.readUnsignedShort();
                file.skip(
                        12L + nameLength + extraLength + commentLength,
                        directoryEnd,
                        path);
            } else if (signature == CENTRAL_DIRECTORY_ARCHIVE_EXTRA_DATA) {
                file.skip(file.readUnsignedInt(), directoryEnd, path);
            } else if (signature == CENTRAL_DIRECTORY_DIGITAL_SIGNATURE) {
                file.skip(file.readUnsignedShort(), directoryEnd, path);
            } else {
                throw new ZipException("Invalid ZIP central directory record: " + path);
            }
        }
        if (file.position() != directoryEnd || entryCount != directory.entryCount) {
            throw new ZipException("ZIP central directory does not match its declared size: " + path);
        }
    }

    private static Directory readDirectory(Reader file, Path path) throws IOException {
        long endOffset = findEndOfCentralDirectory(file);
        file.seek(endOffset + 4L);
        int diskNumber = file.readUnsignedShort();
        int centralDirectoryDisk = file.readUnsignedShort();
        long entriesOnDisk = file.readUnsignedShort();
        long totalEntries = file.readUnsignedShort();
        long centralDirectorySize = file.readUnsignedInt();
        long centralDirectoryOffset = file.readUnsignedInt();

        if (diskNumber != 0 || centralDirectoryDisk != 0 || entriesOnDisk != totalEntries) {
            throw new ZipException("Split ZIP archives are not supported: " + path);
        }
        if (totalEntries == 0xffffL
                || centralDirectorySize == 0xffffffffL
                || centralDirectoryOffset == 0xffffffffL) {
            return readZip64Directory(file, path, endOffset);
        }
        return checkedDirectory(
                path,
                file.length(),
                totalEntries,
                centralDirectorySize,
                centralDirectoryOffset);
    }

    private static long findEndOfCentralDirectory(Reader file) throws IOException {
        long minimumOffset = Math.max(
                0L,
                file.length() - END_OF_CENTRAL_DIRECTORY_SIZE - MAXIMUM_COMMENT_LENGTH);
        for (long offset = file.length() - END_OF_CENTRAL_DIRECTORY_SIZE;
             offset >= minimumOffset;
             offset--) {
            file.seek(offset);
            if (file.readUnsignedInt() == END_OF_CENTRAL_DIRECTORY) {
                file.seek(offset + 20L);
                int commentLength = file.readUnsignedShort();
                if (offset + END_OF_CENTRAL_DIRECTORY_SIZE + commentLength == file.length()) {
                    return offset;
                }
            }
        }
        throw new ZipException("ZIP end of central directory not found");
    }

    private static Directory readZip64Directory(
            Reader file,
            Path path,
            long endOffset) throws IOException {
        long locatorOffset = endOffset - 20L;
        if (locatorOffset < 0L) {
            throw new ZipException("ZIP64 locator not found: " + path);
        }
        file.seek(locatorOffset);
        if (file.readUnsignedInt() != ZIP64_END_OF_CENTRAL_DIRECTORY_LOCATOR
                || file.readUnsignedInt() != 0L) {
            throw new ZipException("Invalid or split ZIP64 locator: " + path);
        }
        long zip64Offset = file.readLong();
        if (file.readUnsignedInt() != 1L
                || zip64Offset < 0L
                || zip64Offset > file.length() - 56L) {
            throw new ZipException("Invalid ZIP64 locator: " + path);
        }

        file.seek(zip64Offset);
        if (file.readUnsignedInt() != ZIP64_END_OF_CENTRAL_DIRECTORY) {
            throw new ZipException("ZIP64 end of central directory not found: " + path);
        }
        long recordSize = file.readLong();
        if (recordSize < 44L) {
            throw new ZipException("Invalid ZIP64 end of central directory: " + path);
        }
        file.skip(4L, file.length(), path);
        long diskNumber = file.readUnsignedInt();
        long centralDirectoryDisk = file.readUnsignedInt();
        long entriesOnDisk = file.readLong();
        long totalEntries = file.readLong();
        long centralDirectorySize = file.readLong();
        long centralDirectoryOffset = file.readLong();
        if (diskNumber != 0L
                || centralDirectoryDisk != 0L
                || entriesOnDisk != totalEntries) {
            throw new ZipException("Split ZIP64 archives are not supported: " + path);
        }
        return checkedDirectory(
                path,
                file.length(),
                totalEntries,
                centralDirectorySize,
                centralDirectoryOffset);
    }

    private static Directory checkedDirectory(
            Path path,
            long fileLength,
            long entryCount,
            long size,
            long offset) throws ZipException {
        if (entryCount < 0L || size < 0L || offset < 0L || offset > fileLength - size) {
            throw new ZipException("Invalid ZIP central directory bounds: " + path);
        }
        return new Directory(entryCount, size, offset);
    }

    private static LogFileReadLimitExceededException limit(
            LogFileReadLimitExceededException.LimitType limitType,
            Path path,
            long configured,
            long observed) {
        return new LogFileReadLimitExceededException(
                limitType,
                path,
                null,
                Long.toString(configured),
                Long.toString(observed));
    }

    private static final class Reader {
        private final SeekableByteChannel channel;
        private final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);

        private Reader(SeekableByteChannel channel) {
            this.channel = channel;
        }

        private long length() throws IOException {
            return channel.size();
        }

        private long position() throws IOException {
            return channel.position();
        }

        private void seek(long position) throws IOException {
            channel.position(position);
        }

        private int readUnsignedShort() throws IOException {
            read(Short.BYTES);
            return Short.toUnsignedInt(buffer.getShort());
        }

        private long readUnsignedInt() throws IOException {
            read(Integer.BYTES);
            return Integer.toUnsignedLong(buffer.getInt());
        }

        private long readLong() throws IOException {
            read(Long.BYTES);
            long value = buffer.getLong();
            if (value < 0L) {
                throw new ZipException("ZIP64 value is too large");
            }
            return value;
        }

        private void read(int length) throws IOException {
            buffer.clear();
            buffer.limit(length);
            while (buffer.hasRemaining()) {
                if (channel.read(buffer) == -1) {
                    throw new EOFException("Unexpected end of ZIP metadata");
                }
            }
            buffer.flip();
        }

        private void skip(long count, long maximumOffset, Path path) throws IOException {
            long current = position();
            long target = current + count;
            if (count < 0L || target < current || target > maximumOffset) {
                throw new ZipException("Invalid ZIP central directory field length: " + path);
            }
            seek(target);
        }
    }

    private static final class Directory {
        private final long entryCount;
        private final long size;
        private final long offset;

        private Directory(long entryCount, long size, long offset) {
            this.entryCount = entryCount;
            this.size = size;
            this.offset = offset;
        }
    }
}
