package com.microsoft.gctoolkit.event.zgc;

public class ZGCMarkSummary {
    private final int stripes;
    private final int proactiveFlushes;
    private final int terminatedFlushes;
    private final int completions;
    private final int continuations;
    
    public ZGCMarkSummary(int stripes, int proactiveFlushes, int terminatedFlushes, int completions, int continuations) {
        this.stripes = stripes;
        this.proactiveFlushes = proactiveFlushes;
        this.terminatedFlushes = terminatedFlushes;
        this.completions = completions;
        this.continuations = continuations;
    }
    
    public int getContinuations() {
        return continuations;
    }

    public int getCompletions() {
        return completions;
    }

    public int getTerminatedFlushes() {
        return terminatedFlushes;
    }

    public int getProactiveFlushes() {
        return proactiveFlushes;
    }

    public int getStripes() {
        return stripes;
    }
}
