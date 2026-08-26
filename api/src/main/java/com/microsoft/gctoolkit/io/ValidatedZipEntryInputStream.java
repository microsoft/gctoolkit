// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

final class ValidatedZipEntryInputStream extends InputStream {

    private static final long LOCAL_FILE_HEADER = 0x04034b50L;
    private static final long DATA_DESCRIPTOR = 0x08074b50L;
    private static final int DATA_DESCRIPTOR_FLAG = 8;

    private final InputStream entryInputStream;
    private final TrackingZipInputStream trackingZipInputStream;
    private final ZipArchivePreflight.Entry expectedEntry;
    private final Path path;
    private final CRC32 crc = new CRC32();
    private final byte[] singleByte = new byte[1];
    private long expandedBytes;
    private boolean validated;

    ValidatedZipEntryInputStream(
            InputStream inputStream,
            ZipArchivePreflight.Entry expectedEntry,
            Path path) throws IOException {
        this.expectedEntry = expectedEntry;
        this.path = path;
        TrackingZipInputStream tracking = null;
        try {
            if (expectedEntry.getMethod() == ZipEntry.STORED) {
                entryInputStream = new StoredEntryInputStream(inputStream, expectedEntry, path);
            } else {
                tracking = new TrackingZipInputStream(inputStream);
                ZipEntry actualEntry = tracking.getNextEntry();
                if (actualEntry == null
                        || actualEntry.isDirectory()
                        || actualEntry.getMethod() != expectedEntry.getMethod()
                        || !expectedEntry.getName().equals(actualEntry.getName())) {
                    throw new ZipException(
                            "ZIP local header does not match central directory: " + path);
                }
                entryInputStream = tracking;
            }
            trackingZipInputStream = tracking;
        } catch (IOException | RuntimeException | Error failure) {
            try {
                inputStream.close();
            } catch (IOException closeException) {
                failure.addSuppressed(closeException);
            }
            throw failure;
        }
    }

    long getCompressedBytesRead() {
        if (expectedEntry.getMethod() == ZipEntry.STORED) {
            return expandedBytes;
        }
        return trackingZipInputStream.getCompressedBytesRead();
    }

    @Override
    public int read() throws IOException {
        int count = read(singleByte, 0, 1);
        return count == -1 ? -1 : Byte.toUnsignedInt(singleByte[0]);
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        int count = entryInputStream.read(bytes, offset, length);
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
        if (expandedBytes != expectedEntry.getExpandedSize()) {
            throw new ZipException("ZIP expanded size does not match central directory: " + path);
        }
        if (getCompressedBytesRead() != expectedEntry.getCompressedSize()) {
            throw new ZipException("ZIP compressed size does not match central directory: " + path);
        }
        if (crc.getValue() != expectedEntry.getCrc()) {
            throw new ZipException("ZIP CRC does not match central directory: " + path);
        }
    }

    @Override
    public void close() throws IOException {
        entryInputStream.close();
    }

    private static final class TrackingZipInputStream extends ZipInputStream {

        private TrackingZipInputStream(InputStream inputStream) {
            super(inputStream);
        }

        private long getCompressedBytesRead() {
            return inf.getBytesRead();
        }
    }

    private static final class StoredEntryInputStream extends InputStream {

        private final InputStream inputStream;
        private final ZipArchivePreflight.Entry expectedEntry;
        private final Path path;
        private final boolean hasDescriptor;
        private long remaining;
        private boolean descriptorValidated;

        private StoredEntryInputStream(
                InputStream inputStream,
                ZipArchivePreflight.Entry expectedEntry,
                Path path) throws IOException {
            this.inputStream = inputStream;
            this.expectedEntry = expectedEntry;
            this.path = path;
            if (readUnsignedInt(inputStream, path) != LOCAL_FILE_HEADER) {
                throw new ZipException("ZIP local header not found: " + path);
            }
            ValidatedZipEntryInputStream.skip(inputStream, 2, path);
            int flags = readUnsignedShort(inputStream, path);
            int method = readUnsignedShort(inputStream, path);
            ValidatedZipEntryInputStream.skip(inputStream, 4, path);
            long localCrc = readUnsignedInt(inputStream, path);
            long localCompressedSize = readUnsignedInt(inputStream, path);
            long localExpandedSize = readUnsignedInt(inputStream, path);
            int nameLength = readUnsignedShort(inputStream, path);
            int extraLength = readUnsignedShort(inputStream, path);
            byte[] name = readBytes(inputStream, nameLength, path);
            ValidatedZipEntryInputStream.skip(inputStream, extraLength, path);

            if (method != ZipEntry.STORED
                    || flags != expectedEntry.getFlags()
                    || !new String(name, StandardCharsets.UTF_8).equals(expectedEntry.getName())) {
                throw new ZipException(
                        "ZIP local header does not match central directory: " + path);
            }
            hasDescriptor = (flags & DATA_DESCRIPTOR_FLAG) != 0;
            if (!hasDescriptor) {
                if (localCrc != expectedEntry.getCrc()
                        || (localCompressedSize != 0xffffffffL
                            && localCompressedSize != expectedEntry.getCompressedSize())
                        || (localExpandedSize != 0xffffffffL
                            && localExpandedSize != expectedEntry.getExpandedSize())) {
                    throw new ZipException(
                            "ZIP local header values do not match central directory: " + path);
                }
            }
            remaining = expectedEntry.getCompressedSize();
        }

        @Override
        public int read() throws IOException {
            byte[] oneByte = new byte[1];
            int count = read(oneByte, 0, 1);
            return count == -1 ? -1 : Byte.toUnsignedInt(oneByte[0]);
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            if (remaining == 0L) {
                validateDescriptor();
                return -1;
            }
            int count = inputStream.read(bytes, offset, (int) Math.min(length, remaining));
            if (count == -1) {
                throw new EOFException("Unexpected end of stored ZIP entry: " + path);
            }
            remaining -= count;
            return count;
        }

        private void validateDescriptor() throws IOException {
            if (!hasDescriptor || descriptorValidated) {
                return;
            }
            descriptorValidated = true;
            long crcOrSignature = readUnsignedInt(inputStream, path);
            long descriptorCrc = crcOrSignature == DATA_DESCRIPTOR
                    ? readUnsignedInt(inputStream, path)
                    : crcOrSignature;
            long compressedSize = expectedEntry.isZip64Size()
                    ? readLong(inputStream, path)
                    : readUnsignedInt(inputStream, path);
            long expandedSize = expectedEntry.isZip64Size()
                    ? readLong(inputStream, path)
                    : readUnsignedInt(inputStream, path);
            if (descriptorCrc != expectedEntry.getCrc()
                    || compressedSize != expectedEntry.getCompressedSize()
                    || expandedSize != expectedEntry.getExpandedSize()) {
                throw new ZipException(
                        "ZIP data descriptor does not match central directory: " + path);
            }
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }

    private static int readUnsignedShort(InputStream inputStream, Path path) throws IOException {
        return readByte(inputStream, path) | (readByte(inputStream, path) << 8);
    }

    private static long readUnsignedInt(InputStream inputStream, Path path) throws IOException {
        return (long) readByte(inputStream, path)
                | ((long) readByte(inputStream, path) << 8)
                | ((long) readByte(inputStream, path) << 16)
                | ((long) readByte(inputStream, path) << 24);
    }

    private static long readLong(InputStream inputStream, Path path) throws IOException {
        long value = readUnsignedInt(inputStream, path)
                | (readUnsignedInt(inputStream, path) << 32);
        if (value < 0L) {
            throw new ZipException("ZIP64 value is too large: " + path);
        }
        return value;
    }

    private static int readByte(InputStream inputStream, Path path) throws IOException {
        int value = inputStream.read();
        if (value == -1) {
            throw new EOFException("Unexpected end of ZIP metadata: " + path);
        }
        return value;
    }

    private static byte[] readBytes(
            InputStream inputStream,
            int length,
            Path path) throws IOException {
        byte[] bytes = new byte[length];
        int offset = 0;
        while (offset < length) {
            int count = inputStream.read(bytes, offset, length - offset);
            if (count == -1) {
                throw new EOFException("Unexpected end of ZIP metadata: " + path);
            }
            offset += count;
        }
        return bytes;
    }

    private static void skip(InputStream inputStream, int count, Path path) throws IOException {
        int remaining = count;
        while (remaining > 0) {
            long skipped = inputStream.skip(remaining);
            if (skipped == 0L) {
                readByte(inputStream, path);
                remaining--;
            } else {
                remaining -= (int) skipped;
            }
        }
    }
}
