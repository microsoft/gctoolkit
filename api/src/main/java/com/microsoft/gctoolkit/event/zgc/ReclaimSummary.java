// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

public class ReclaimSummary {

    final private long reclaimStart;
    final private long reclaimEnd;

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
}
