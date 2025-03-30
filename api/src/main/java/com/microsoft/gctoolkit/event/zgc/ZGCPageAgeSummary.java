package com.microsoft.gctoolkit.event.zgc;

public class ZGCPageAgeSummary {
    private final String name;
    private final long live;
    private final int livePct;
    private final long garbage;
    private final int garbagePct;
    private final long smallPageCandidates;
    private final long smallPageSelected;
    private final long mediumPageCandidates;
    private final long mediumPageSelected;
    private final long largePageCandidates;
    private final long largePageSelected;

    public ZGCPageAgeSummary(
            String name,
            long live,
            int livePct,
            long garbage,
            int garbagePct,
            long smallPageCandidates,
            long smallPageSelected,
            long mediumPageCandidates,
            long mediumPageSelected,
            long largePageCandidates,
            long largePageSelected) {
        this.name = name;
        this.live = live;
        this.livePct = livePct;
        this.garbage = garbage;
        this.garbagePct = garbagePct;
        this.smallPageCandidates = smallPageCandidates;
        this.smallPageSelected = smallPageSelected;
        this.mediumPageCandidates = mediumPageCandidates;
        this.mediumPageSelected = mediumPageSelected;
        this.largePageCandidates = largePageCandidates;
        this.largePageSelected = largePageSelected;
    }

    public long getGarbage() {
        return garbage;
    }

    public int getGarbagePct() {
        return garbagePct;
    }

    public long getLargePageCandidates() {
        return largePageCandidates;
    }

    public long getLargePageSelected() {
        return largePageSelected;
    }

    public long getLive() {
        return live;
    }

    public int getLivePct() {
        return livePct;
    }

    public long getMediumPageCandidates() {
        return mediumPageCandidates;
    }

    public long getMediumPageSelected() {
        return mediumPageSelected;
    }

    public String getName() {
        return name;
    }

    public long getSmallPageCandidates() {
        return smallPageCandidates;
    }

    public long getSmallPageSelected() {
        return smallPageSelected;
    }
}
