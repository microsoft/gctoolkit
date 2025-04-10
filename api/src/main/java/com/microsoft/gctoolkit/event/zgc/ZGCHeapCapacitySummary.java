package com.microsoft.gctoolkit.event.zgc;

public class ZGCHeapCapacitySummary {
    private final long minCapacity;
    private final long maxCapacity;
    private final long softMaxCapacity;

    public ZGCHeapCapacitySummary(long minCapacity, long maxCapacity, long softMaxCapacity) {
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.softMaxCapacity = softMaxCapacity;
    }

    public long getMinCapacity() {
        return minCapacity;
    }

    public long getMaxCapacity() {
        return maxCapacity;
    }

    public long getSoftMaxCapacity() {
        return softMaxCapacity;
    }
}
