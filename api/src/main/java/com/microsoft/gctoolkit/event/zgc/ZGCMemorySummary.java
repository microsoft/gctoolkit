package com.microsoft.gctoolkit.event.zgc;

public class ZGCMemorySummary {
    private final long occupancyBefore;
    private final long occupancyAfter;

    public ZGCMemorySummary(long occupancyBefore, long occupancyAfter) {
        this.occupancyBefore = occupancyBefore;
        this.occupancyAfter = occupancyAfter;
    }

    public long getOccupancyBefore() {
        return occupancyBefore;
    }

    public long getOccupancyAfter() {
        return occupancyAfter;
    }

}
