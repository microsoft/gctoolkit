// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

final class CountingInputStream extends FilterInputStream {

    private final long maximumBytes;
    private final Path path;
    private long bytesRead;

    CountingInputStream(InputStream inputStream, long maximumBytes, Path path) {
        super(inputStream);
        this.maximumBytes = maximumBytes;
        this.path = path;
    }

    long getBytesRead() {
        return bytesRead;
    }

    @Override
    public int read() throws IOException {
        int value = super.read();
        if (value != -1) {
            record(1L);
        }
        return value;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        int count = super.read(bytes, offset, nextReadLength(length));
        if (count > 0) {
            record(count);
        }
        return count;
    }

    @Override
    public long skip(long count) throws IOException {
        long skipped = super.skip(nextReadLength(count));
        record(skipped);
        return skipped;
    }

    private int nextReadLength(int requestedLength) {
        return (int) nextReadLength((long) requestedLength);
    }

    private long nextReadLength(long requestedLength) {
        long remaining = maximumBytes - bytesRead;
        long detectableLength = remaining == Long.MAX_VALUE ? Long.MAX_VALUE : remaining + 1L;
        return Math.min(requestedLength, detectableLength);
    }

    private void record(long count) {
        if (count <= 0L) {
            return;
        }
        if (count > maximumBytes - bytesRead) {
            long observed = bytesRead == Long.MAX_VALUE
                    ? Long.MAX_VALUE
                    : bytesRead + count;
            throw new LogFileReadLimitExceededException(
                    LogFileReadLimitExceededException.LimitType.COMPRESSED_BYTES,
                    path,
                    null,
                    Long.toString(maximumBytes),
                    Long.toString(observed));
        }
        bytesRead += count;
    }
}
