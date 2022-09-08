package com.microsoft.gctoolkit.integration.shared;

public class SharedAggregation {

    private double runtimeDuration = -1.0d;

    public void terminate(double duration) {
        this.runtimeDuration = duration;
    }

    public double getRuntimeDuration() { return runtimeDuration; }
}
