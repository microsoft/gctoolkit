// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

public class ZGCReclaimedSummary {

    private final long relocateStart;
    private final long relocateEnd;

    public ZGCReclaimedSummary(long reclaimStart, long relocateEnd) {
        this.relocateStart = reclaimStart;
        this.relocateEnd = relocateEnd;
    }

    public long getRelocateStart() {
        return relocateStart;
    }

    public long getRelocateEnd() {
        return relocateEnd;
    }

    public ZGCReclaimedSummary sum(ZGCReclaimedSummary other) {
        if (other == null) {
            return this;
        }
        return new ZGCReclaimedSummary(relocateStart + other.relocateStart, relocateEnd + other.relocateEnd);
    }

}
