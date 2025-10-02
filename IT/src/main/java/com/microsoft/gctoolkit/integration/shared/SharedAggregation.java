package com.microsoft.gctoolkit.integration.shared;

import com.microsoft.gctoolkit.aggregator.Aggregation;
// added for testing
import com.microsoft.gctoolkit.parser.UnifiedG1GCParser;

public abstract class SharedAggregation extends Aggregation {

    public double getRuntimeDuration() { return super.estimatedRuntime(); }
    // added for testing
    public UnifiedG1GCParser testonly;

}
