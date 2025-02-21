package com.microsoft.gctoolkit.event.zgc;

public class ZGCReferenceSummary {
    private final long encountered;
    private final long discovered;
    private final long enqueued;

    public ZGCReferenceSummary(long encountered, long discovered, long enqueued) {
        this.encountered = encountered;
        this.discovered = discovered;
        this.enqueued = enqueued;
    }
    public long getEncountered() {
        return encountered;
    }

    public long getDiscovered() {
        return discovered;
    }

    public long getEnqueued() {
        return enqueued;
    }
}
