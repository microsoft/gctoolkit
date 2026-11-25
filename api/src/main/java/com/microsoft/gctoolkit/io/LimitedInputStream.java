// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.LongSupplier;

final class LimitedInputStream extends FilterInputStream {

    private final LogFileReadBudget budget;
    private final LogFileReadLimits limits;
    private final Path path;
    private final String archiveEntry;
    private final LongSupplier compressedBytes;
    private long expandedBytes;
    private long recordedCompressedBytes;

    LimitedInputStream(
            InputStream inputStream,
            LogFileReadBudget budget,
            LogFileReadLimits limits,
            Path path,
            String archiveEntry,
            LongSupplier compressedBytes) {
        super(inputStream);
        this.budget = budget;
        this.limits = limits;
        this.path = path;
        this.archiveEntry = archiveEntry;
        this.compressedBytes = compressedBytes;
        if (compressedBytes != null) {
            budget.registerCompressedBytes(compressedBytes.getAsLong());
        }
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
        int permittedLength = budget.nextReadLength(length);
        int count = super.read(bytes, offset, permittedLength);
        if (count > 0) {
            record(count);
        }
        return count;
    }

    @Override
    public long skip(long count) throws IOException {
        long skipped = super.skip(budget.nextSkipLength(count));
        if (skipped > 0) {
            record(skipped);
        }
        return skipped;
    }

    private void record(long count) {
        budget.record(count, path, archiveEntry);
        expandedBytes += count;
        if (compressedBytes == null) {
            return;
        }
        long currentCompressedBytes = compressedBytes.getAsLong();
        if (currentCompressedBytes > recordedCompressedBytes) {
            budget.registerCompressedBytes(currentCompressedBytes - recordedCompressedBytes);
            recordedCompressedBytes = currentCompressedBytes;
        }
        budget.recordCompressedExpansion(count, -1L, limits, path, archiveEntry);
        if (expandedBytes <= limits.getCompressionRatioGraceBytes()) {
            return;
        }

        double ratio = currentCompressedBytes == 0L
                ? Double.POSITIVE_INFINITY
                : (double) expandedBytes / (double) currentCompressedBytes;
        if (ratio > limits.getMaxCompressionRatio()) {
            throw new LogFileReadLimitExceededException(
                    LogFileReadLimitExceededException.LimitType.COMPRESSION_RATIO,
                    path,
                    archiveEntry,
                    Double.toString(limits.getMaxCompressionRatio()),
                    Double.toString(ratio));
        }
    }
}
