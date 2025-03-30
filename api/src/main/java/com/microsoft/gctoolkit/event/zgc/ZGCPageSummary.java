package com.microsoft.gctoolkit.event.zgc;

public class ZGCPageSummary {
    private final long candidates;
    private final long selected;
    private final long inPlace;
    private final long size;
    private final long empty;
    private final long relocated;

    public ZGCPageSummary(long candidates, long selected, long inPlace, long size, long empty, long relocated) {
        this.candidates = candidates;
        this.selected = selected;
        this.inPlace = inPlace;
        this.size = size;
        this.empty = empty;
        this.relocated = relocated;
    }

    public long getCandidates() {
        return candidates;
    }

    public long getEmpty() {
        return empty;
    }

    public long getInPlace() {
        return inPlace;
    }

    public long getRelocated() {
        return relocated;
    }

    public long getSelected() {
        return selected;
    }

    public long getSize() {
        return size;
    }
}
