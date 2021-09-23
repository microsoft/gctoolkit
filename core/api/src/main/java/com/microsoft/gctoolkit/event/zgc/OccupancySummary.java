// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

public class OccupancySummary {

    private final long markEnd;
    private final long reclaimStart;
    private final long reclaimEnd;


    public OccupancySummary(long markEnd, long reclaimStart, long reclaimEnd) {
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
}
