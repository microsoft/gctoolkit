package com.microsoft.gctoolkit.event.zgc;

public class ZGCCompactedSummary {
    private final long relocateEnd;

    public ZGCCompactedSummary(long relocateEnd) {
        this.relocateEnd = relocateEnd;
    }

    public long getRelocateEnd() {
        return relocateEnd;
    }
}
