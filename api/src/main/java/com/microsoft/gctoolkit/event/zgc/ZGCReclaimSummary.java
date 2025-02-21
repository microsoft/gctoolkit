// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

public class ZGCReclaimSummary {

    private final long reclaimStart;
    private final long reclaimEnd;

    public ZGCReclaimSummary(long reclaimStart, long reclaimEnd) {
        this.reclaimStart = reclaimStart;
        this.reclaimEnd = reclaimEnd;
    }

    public long getReclaimStart() {
        return reclaimStart;
    }

    public long getReclaimEnd() {
        return reclaimEnd;
    }

    public ZGCReclaimSummary sum(ZGCReclaimSummary other) {
        if (other == null) {
            return this;
        }
        return new ZGCReclaimSummary(reclaimStart + other.reclaimStart, reclaimEnd + other.reclaimEnd);
    }

}
