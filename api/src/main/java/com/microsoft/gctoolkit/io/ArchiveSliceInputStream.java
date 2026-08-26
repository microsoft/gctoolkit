// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

final class ArchiveSliceInputStream extends InputStream {

    private final FileInputStream inputStream;
    private long remaining;

    ArchiveSliceInputStream(Path path, long offset, long length) throws IOException {
        inputStream = new FileInputStream(path.toFile());
        try {
            inputStream.getChannel().position(offset);
            remaining = length;
        } catch (IOException | RuntimeException | Error failure) {
            try {
                inputStream.close();
            } catch (IOException closeException) {
                failure.addSuppressed(closeException);
            }
            throw failure;
        }
    }

    @Override
    public int read() throws IOException {
        if (remaining == 0L) {
            return -1;
        }
        int value = inputStream.read();
        if (value != -1) {
            remaining--;
        }
        return value;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if (remaining == 0L) {
            return -1;
        }
        int count = inputStream.read(bytes, offset, (int) Math.min(length, remaining));
        if (count > 0) {
            remaining -= count;
        }
        return count;
    }

    @Override
    public long skip(long count) throws IOException {
        long skipped = inputStream.skip(Math.min(count, remaining));
        remaining -= skipped;
        return skipped;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
