package com.microsoft.gctoolkit.integration.shared;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.parser;

public abstract class SharedAggregation extends Aggregation {

    public double getRuntimeDuration() { return super.estimatedRuntime(); }
    // added for testing
    public UnifiedG1GCParser testonly;

}
