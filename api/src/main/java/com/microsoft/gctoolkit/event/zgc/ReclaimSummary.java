// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

public class ReclaimSummary {

    private final long reclaimStart;
    private final long reclaimEnd;

    public ReclaimSummary(long reclaimStart, long reclaimEnd) {
        this.reclaimStart = reclaimStart;
        this.reclaimEnd = reclaimEnd;
    }

    public long getReclaimStart() {
        return reclaimStart;
    }

    public long getReclaimEnd() {
        return reclaimEnd;
    }

    public ReclaimSummary sum(ReclaimSummary other) {
        if (other == null) {
            return this;
        }
        return new ReclaimSummary(reclaimStart + other.reclaimStart, reclaimEnd + other.reclaimEnd);
    }

}
