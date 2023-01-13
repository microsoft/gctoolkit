package com.microsoft.gctoolkit.integration.shared;

import com.microsoft.gctoolkit.aggregator.Aggregation;

public abstract class SharedAggregation extends Aggregation {

    public double getRuntimeDuration() { return super.estimatedRuntime(); }

}
