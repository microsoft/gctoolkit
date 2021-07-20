// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.zgc;

public class OccupancySummary {

    final private long markEnd;
    final private long reclaimStart;
    final private long reclaimEnd;


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
