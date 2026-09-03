// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

final class LogFileReadBudget {

    private final long maximumBytes;
    private final AtomicLong expandedBytes = new AtomicLong();
    private final AtomicLong ratioExpandedBytes = new AtomicLong();
    private final AtomicLong ratioCompressedBytes = new AtomicLong();

    LogFileReadBudget(long maximumBytes) {
        this.maximumBytes = maximumBytes;
    }

    int nextReadLength(int requestedLength) {
        long remaining = maximumBytes - expandedBytes.get();
        long detectableLength = remaining == Long.MAX_VALUE ? Long.MAX_VALUE : remaining + 1L;
        return (int) Math.min(requestedLength, detectableLength);
    }

    long nextSkipLength(long requestedLength) {
        long remaining = maximumBytes - expandedBytes.get();
        long detectableLength = remaining == Long.MAX_VALUE ? Long.MAX_VALUE : remaining + 1L;
        return Math.min(requestedLength, detectableLength);
    }

    void record(long bytes, Path path, String archiveEntry) {
        if (bytes <= 0) {
            return;
        }
        while (true) {
            long current = expandedBytes.get();
            if (bytes > maximumBytes - current) {
                long observed = current == maximumBytes ? maximumBytes : current + bytes;
                throw new LogFileReadLimitExceededException(
                        LogFileReadLimitExceededException.LimitType.EXPANDED_BYTES,
                        path,
                        archiveEntry,
                        Long.toString(maximumBytes),
                        Long.toString(observed));
            }
            if (expandedBytes.compareAndSet(current, current + bytes)) {
                return;
            }
        }
    }

    void rejectDeclaredSize(long declaredBytes, Path path, String archiveEntry) {
        long current = expandedBytes.get();
        if (declaredBytes > maximumBytes - current) {
            throw new LogFileReadLimitExceededException(
                    LogFileReadLimitExceededException.LimitType.EXPANDED_BYTES,
                    path,
                    archiveEntry,
                    Long.toString(maximumBytes),
                    Long.toString(current + declaredBytes));
        }
    }

    void registerCompressedBytes(long compressedBytes) {
        if (compressedBytes > 0L) {
            ratioCompressedBytes.addAndGet(compressedBytes);
        }
    }

    void recordCompressedExpansion(
            long expanded,
            long observedCompressedBytes,
            LogFileReadLimits limits,
            Path path,
            String archiveEntry) {
        if (observedCompressedBytes >= 0L) {
            ratioCompressedBytes.accumulateAndGet(observedCompressedBytes, Math::max);
        }
        long totalExpanded = ratioExpandedBytes.addAndGet(expanded);
        if (totalExpanded <= limits.getCompressionRatioGraceBytes()) {
            return;
        }
        long totalCompressed = ratioCompressedBytes.get();
        double ratio = totalCompressed == 0L
                ? Double.POSITIVE_INFINITY
                : (double) totalExpanded / (double) totalCompressed;
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
