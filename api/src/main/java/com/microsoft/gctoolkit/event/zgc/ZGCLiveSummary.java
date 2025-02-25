// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

public class ZGCLiveSummary {
    private final long markEnd;
    private final long relocateStart;
    private final long relocateEnd;

    public ZGCLiveSummary(long markEnd, long relocateStart, long relocateEnd) {
        this.markEnd = markEnd;
        this.relocateStart = relocateStart;
        this.relocateEnd = relocateEnd;
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
}
