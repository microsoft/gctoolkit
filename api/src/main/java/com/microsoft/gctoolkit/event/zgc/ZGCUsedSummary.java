// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

public class ZGCUsedSummary {
    private final long markStart;
    private final long markEnd;
    private final long relocateStart;
    private final long relocateEnd;

    public ZGCUsedSummary(long markStart, long markEnd, long relocateStart, long reclaimEnd) {
        this.markStart = markStart;
        this.markEnd = markEnd;
        this.relocateStart = relocateStart;
        this.relocateEnd = reclaimEnd;
    }

    public long getMarkEnd() {
        return markEnd;
    }

    public long getRelocateStart() {
        return relocateStart;
    }

    public long getRelocateEnd() {
        return relocateEnd;
    }

    public long getMarkStart() {
        return markStart;
    }

    public ZGCUsedSummary sum(ZGCUsedSummary other) {
        if (other == null) {
            return this;
        }
        return new ZGCUsedSummary(markStart + other.markStart, markEnd + other.markEnd, relocateStart + other.relocateStart, relocateEnd + other.relocateEnd);
    }
}
