package com.microsoft.gctoolkit.integration.shared;

import com.microsoft.gctoolkit.aggregator.Aggregation;

public abstract class SharedAggregation extends Aggregation {

    private double runtimeDuration = -1.0d;

    public void terminate(double duration) {
        this.runtimeDuration = duration;
    }

    public double getRuntimeDuration() { return runtimeDuration; }
}
