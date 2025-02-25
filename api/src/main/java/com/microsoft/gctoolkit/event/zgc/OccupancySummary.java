// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

public class OccupancySummary {
    private final long markStart;
    private final long markEnd;
    private final long reclaimStart;
    private final long reclaimEnd;

    public OccupancySummary(long markStart, long markEnd, long reclaimStart, long reclaimEnd) {
        this.markStart = markStart;
        this.markEnd = markEnd;
        this.reclaimStart = reclaimStart;
        this.reclaimEnd = reclaimEnd;
    }

    public long getMarkEnd() {
        return markEnd;
    }

    public long getReclaimStart() {
        return reclaimStart;
    }

    public long getReclaimEnd() {
        return reclaimEnd;
    }

    public long getMarkStart() {
        return markStart;
    }

    public OccupancySummary sum(OccupancySummary other) {
        if (other == null) {
            return this;
        }
        return new OccupancySummary(markStart + other.markStart, markEnd + other.markEnd, reclaimStart + other.reclaimStart, reclaimEnd + other.reclaimEnd);
    }
}
