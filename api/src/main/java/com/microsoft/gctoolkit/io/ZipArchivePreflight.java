// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

final class ZipArchivePreflight {

    private static final long END_OF_CENTRAL_DIRECTORY = 0x06054b50L;
    private static final long ZIP64_END_OF_CENTRAL_DIRECTORY = 0x06064b50L;
    private static final long ZIP64_END_OF_CENTRAL_DIRECTORY_LOCATOR = 0x07064b50L;
    private static final long CENTRAL_DIRECTORY_ENTRY = 0x02014b50L;
    private static final long CENTRAL_DIRECTORY_ARCHIVE_EXTRA_DATA = 0x08064b50L;
    private static final long CENTRAL_DIRECTORY_DIGITAL_SIGNATURE = 0x05054b50L;
    private static final int ZIP64_EXTRA_FIELD = 1;
    private static final int MAXIMUM_COMMENT_LENGTH = 0xffff;
    private static final int END_OF_CENTRAL_DIRECTORY_SIZE = 22;

    private ZipArchivePreflight() {
    }

    static Index validate(Path path, LogFileReadLimits limits) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
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
                throw entryLimit(path, limits.getMaxArchiveEntries(), directory.entryCount);
            }

            long directoryEnd = directory.offset + directory.size;
            file.seek(directory.offset);
            List<Entry> entries = new ArrayList<>((int) directory.entryCount);
            long recordCount = 0L;
            long maximumRecords = (long) limits.getMaxArchiveEntries() + 2L;
            while (file.getFilePointer() < directoryEnd) {
                if (++recordCount > maximumRecords) {
                    throw entryLimit(path, (int) maximumRecords, recordCount);
                }
                long signature = readUnsignedInt(file);
                if (signature == CENTRAL_DIRECTORY_DIGITAL_SIGNATURE) {
                    int signatureLength = readUnsignedShort(file);
                    skip(file, signatureLength, directoryEnd, path);
                    break;
                }
                if (signature == CENTRAL_DIRECTORY_ARCHIVE_EXTRA_DATA) {
                    long extraDataLength = readUnsignedInt(file);
                    skip(file, extraDataLength, directoryEnd, path);
                    continue;
                }
                if (signature != CENTRAL_DIRECTORY_ENTRY) {
                    throw new ZipException("Invalid ZIP central directory entry: " + path);
                }
                if (entries.size() == limits.getMaxArchiveEntries()) {
                    throw entryLimit(path, limits.getMaxArchiveEntries(), entries.size() + 1L);
                }
                entries.add(readEntry(file, path, directory.offset, directoryEnd));
            }
            if (file.getFilePointer() != directoryEnd) {
                throw new ZipException("Invalid ZIP central directory size: " + path);
            }
            if (entries.size() != directory.entryCount) {
                throw new ZipException("ZIP central directory entry count mismatch: " + path);
            }
            return new Index(directory.offset, entries);
        }
    }

    private static Directory readDirectory(RandomAccessFile file, Path path) throws IOException {
        long endOffset = findEndOfCentralDirectory(file);
        file.seek(endOffset + 4);
        int diskNumber = readUnsignedShort(file);
        int centralDirectoryDisk = readUnsignedShort(file);
        long entriesOnDisk = readUnsignedShort(file);
        long totalEntries = readUnsignedShort(file);
        long centralDirectorySize = readUnsignedInt(file);
        long centralDirectoryOffset = readUnsignedInt(file);

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

    private static Entry readEntry(
            RandomAccessFile file,
            Path path,
            long centralDirectoryOffset,
            long directoryEnd) throws IOException {
        skip(file, 4, directoryEnd, path);
        int flags = readUnsignedShort(file);
        int method = readUnsignedShort(file);
        skip(file, 4, directoryEnd, path);
        long crc = readUnsignedInt(file);
        long compressedSize = readUnsignedInt(file);
        long expandedSize = readUnsignedInt(file);
        boolean zip64Size = compressedSize == 0xffffffffL || expandedSize == 0xffffffffL;
        int nameLength = readUnsignedShort(file);
        int extraLength = readUnsignedShort(file);
        int commentLength = readUnsignedShort(file);
        int diskStart = readUnsignedShort(file);
        skip(file, 6, directoryEnd, path);
        long localHeaderOffset = readUnsignedInt(file);

        byte[] nameBytes = new byte[nameLength];
        file.readFully(nameBytes);
        byte[] extra = new byte[extraLength];
        file.readFully(extra);
        skip(file, commentLength, directoryEnd, path);

        long[] zip64 = parseZip64Extra(
                extra,
                expandedSize == 0xffffffffL,
                compressedSize == 0xffffffffL,
                localHeaderOffset == 0xffffffffL,
                diskStart == 0xffff,
                path);
        int zip64Index = 0;
        if (expandedSize == 0xffffffffL) {
            expandedSize = zip64[zip64Index++];
        }
        if (compressedSize == 0xffffffffL) {
            compressedSize = zip64[zip64Index++];
        }
        if (localHeaderOffset == 0xffffffffL) {
            localHeaderOffset = zip64[zip64Index++];
        }
        if (diskStart == 0xffff) {
            diskStart = Math.toIntExact(zip64[zip64Index]);
        }
        if (diskStart != 0) {
            throw new ZipException("Split ZIP entries are not supported: " + path);
        }
        if (localHeaderOffset < 0L || localHeaderOffset >= centralDirectoryOffset) {
            throw new ZipException("Invalid ZIP local header offset: " + path);
        }
        if ((flags & 1) != 0) {
            throw new ZipException("Encrypted ZIP entries are not supported: " + path);
        }
        if (method != ZipEntry.STORED && method != ZipEntry.DEFLATED) {
            throw new ZipException("Unsupported ZIP compression method: " + method);
        }

        String name = new String(nameBytes, StandardCharsets.UTF_8);
        return new Entry(
                name,
                name.endsWith("/"),
                flags,
                method,
                crc,
                compressedSize,
                expandedSize,
                localHeaderOffset,
                zip64Size);
    }

    private static long[] parseZip64Extra(
            byte[] extra,
            boolean expandedSizeRequired,
            boolean compressedSizeRequired,
            boolean offsetRequired,
            boolean diskRequired,
            Path path) throws ZipException {
        int requiredValues = (expandedSizeRequired ? 1 : 0)
                + (compressedSizeRequired ? 1 : 0)
                + (offsetRequired ? 1 : 0)
                + (diskRequired ? 1 : 0);
        if (requiredValues == 0) {
            return new long[0];
        }

        int offset = 0;
        while (offset <= extra.length - 4) {
            int id = unsignedShort(extra, offset);
            int size = unsignedShort(extra, offset + 2);
            offset += 4;
            if (size > extra.length - offset) {
                throw new ZipException("Invalid ZIP extra field: " + path);
            }
            if (id == ZIP64_EXTRA_FIELD) {
                List<Long> values = new ArrayList<>(requiredValues);
                int valueOffset = offset;
                int fieldEnd = offset + size;
                if (expandedSizeRequired) {
                    values.add(littleEndianLong(extra, valueOffset, fieldEnd, path));
                    valueOffset += 8;
                }
                if (compressedSizeRequired) {
                    values.add(littleEndianLong(extra, valueOffset, fieldEnd, path));
                    valueOffset += 8;
                }
                if (offsetRequired) {
                    values.add(littleEndianLong(extra, valueOffset, fieldEnd, path));
                    valueOffset += 8;
                }
                if (diskRequired) {
                    if (valueOffset > fieldEnd - 4) {
                        throw new ZipException("Invalid ZIP64 extra field: " + path);
                    }
                    values.add(unsignedInt(extra, valueOffset));
                }
                long[] result = new long[values.size()];
                for (int index = 0; index < result.length; index++) {
                    result[index] = values.get(index);
                }
                return result;
            }
            offset += size;
        }
        throw new ZipException("Required ZIP64 extra field not found: " + path);
    }

    private static long littleEndianLong(
            byte[] bytes,
            int offset,
            int fieldEnd,
            Path path) throws ZipException {
        if (offset > fieldEnd - 8 || offset > bytes.length - 8) {
            throw new ZipException("Invalid ZIP64 extra field: " + path);
        }
        long low = unsignedInt(bytes, offset);
        long high = unsignedInt(bytes, offset + 4);
        long value = low | (high << 32);
        if (value < 0L) {
            throw new ZipException("ZIP64 value is too large: " + path);
        }
        return value;
    }

    private static int unsignedShort(byte[] bytes, int offset) {
        return Byte.toUnsignedInt(bytes[offset]) | (Byte.toUnsignedInt(bytes[offset + 1]) << 8);
    }

    private static long unsignedInt(byte[] bytes, int offset) {
        return (long) Byte.toUnsignedInt(bytes[offset])
                | ((long) Byte.toUnsignedInt(bytes[offset + 1]) << 8)
                | ((long) Byte.toUnsignedInt(bytes[offset + 2]) << 16)
                | ((long) Byte.toUnsignedInt(bytes[offset + 3]) << 24);
    }

    private static long findEndOfCentralDirectory(RandomAccessFile file) throws IOException {
        long minimumOffset = Math.max(0L,
                file.length() - END_OF_CENTRAL_DIRECTORY_SIZE - MAXIMUM_COMMENT_LENGTH);
        for (long offset = file.length() - END_OF_CENTRAL_DIRECTORY_SIZE;
             offset >= minimumOffset;
             offset--) {
            file.seek(offset);
            if (readUnsignedInt(file) == END_OF_CENTRAL_DIRECTORY) {
                file.seek(offset + 20);
                int commentLength = readUnsignedShort(file);
                if (offset + END_OF_CENTRAL_DIRECTORY_SIZE + commentLength == file.length()) {
                    return offset;
                }
            }
        }
        throw new ZipException("ZIP end of central directory not found");
    }

    private static Directory readZip64Directory(
            RandomAccessFile file,
            Path path,
            long endOffset) throws IOException {
        long locatorOffset = endOffset - 20L;
        if (locatorOffset < 0L) {
            throw new ZipException("ZIP64 locator not found: " + path);
        }
        file.seek(locatorOffset);
        if (readUnsignedInt(file) != ZIP64_END_OF_CENTRAL_DIRECTORY_LOCATOR) {
            throw new ZipException("ZIP64 locator not found: " + path);
        }
        if (readUnsignedInt(file) != 0L) {
            throw new ZipException("Split ZIP64 archives are not supported: " + path);
        }
        long zip64Offset = readLong(file);
        if (readUnsignedInt(file) != 1L || zip64Offset < 0L || zip64Offset > file.length() - 56L) {
            throw new ZipException("Invalid ZIP64 locator: " + path);
        }
        file.seek(zip64Offset);
        if (readUnsignedInt(file) != ZIP64_END_OF_CENTRAL_DIRECTORY) {
            throw new ZipException("ZIP64 end of central directory not found: " + path);
        }
        long recordSize = readLong(file);
        if (recordSize < 44L) {
            throw new ZipException("Invalid ZIP64 end of central directory: " + path);
        }
        skip(file, 4, file.length(), path);
        long diskNumber = readUnsignedInt(file);
        long centralDirectoryDisk = readUnsignedInt(file);
        long entriesOnDisk = readLong(file);
        long totalEntries = readLong(file);
        long centralDirectorySize = readLong(file);
        long centralDirectoryOffset = readLong(file);
        if (diskNumber != 0L || centralDirectoryDisk != 0L || entriesOnDisk != totalEntries) {
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

    private static int readUnsignedShort(RandomAccessFile file) throws IOException {
        return file.readUnsignedByte() | (file.readUnsignedByte() << 8);
    }

    private static long readUnsignedInt(RandomAccessFile file) throws IOException {
        return (long) file.readUnsignedByte()
                | ((long) file.readUnsignedByte() << 8)
                | ((long) file.readUnsignedByte() << 16)
                | ((long) file.readUnsignedByte() << 24);
    }

    private static long readLong(RandomAccessFile file) throws IOException {
        long value = readUnsignedInt(file) | (readUnsignedInt(file) << 32);
        if (value < 0L) {
            throw new ZipException("ZIP64 value is too large");
        }
        return value;
    }

    private static void skip(
            RandomAccessFile file,
            long count,
            long maximumOffset,
            Path path) throws IOException {
        long current = file.getFilePointer();
        long target = current + count;
        if (count < 0L || target < current || target > maximumOffset) {
            throw new ZipException("Invalid ZIP central directory field length: " + path);
        }
        file.seek(target);
    }

    private static LogFileReadLimitExceededException entryLimit(
            Path path,
            int maximumEntries,
            long observedEntries) {
        return new LogFileReadLimitExceededException(
                LogFileReadLimitExceededException.LimitType.ARCHIVE_ENTRIES,
                path,
                null,
                Integer.toString(maximumEntries),
                Long.toString(observedEntries));
    }

    static final class Index {
        private final long centralDirectoryOffset;
        private final List<Entry> entries;

        private Index(long centralDirectoryOffset, List<Entry> entries) {
            this.centralDirectoryOffset = centralDirectoryOffset;
            this.entries = Collections.unmodifiableList(entries);
        }

        long getCentralDirectoryOffset() {
            return centralDirectoryOffset;
        }

        List<Entry> getEntries() {
            return entries;
        }

        Entry firstFile() {
            return entries.stream().filter(entry -> !entry.isDirectory()).findFirst().orElse(null);
        }

        Entry find(String name) {
            return entries.stream().filter(entry -> entry.getName().equals(name)).findFirst().orElse(null);
        }
    }

    static final class Entry {
        private final String name;
        private final boolean directory;
        private final int flags;
        private final int method;
        private final long crc;
        private final long compressedSize;
        private final long expandedSize;
        private final long localHeaderOffset;
        private final boolean zip64Size;

        private Entry(
                String name,
                boolean directory,
                int flags,
                int method,
                long crc,
                long compressedSize,
                long expandedSize,
                long localHeaderOffset,
                boolean zip64Size) {
            this.name = name;
            this.directory = directory;
            this.flags = flags;
            this.method = method;
            this.crc = crc;
            this.compressedSize = compressedSize;
            this.expandedSize = expandedSize;
            this.localHeaderOffset = localHeaderOffset;
            this.zip64Size = zip64Size;
        }

        String getName() {
            return name;
        }

        boolean isDirectory() {
            return directory;
        }

        int getMethod() {
            return method;
        }

        int getFlags() {
            return flags;
        }

        long getCrc() {
            return crc;
        }

        long getCompressedSize() {
            return compressedSize;
        }

        long getExpandedSize() {
            return expandedSize;
        }

        long getLocalHeaderOffset() {
            return localHeaderOffset;
        }

        boolean isZip64Size() {
            return zip64Size;
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
