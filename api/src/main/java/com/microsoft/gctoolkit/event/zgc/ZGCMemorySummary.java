package com.microsoft.gctoolkit.event.zgc;

public class ZGCMemorySummary {
    private final long start;
    private final long end;

    public ZGCMemorySummary(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

}
