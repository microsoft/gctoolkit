package com.microsoft.gctoolkit.integration.aggregation;

import com.microsoft.gctoolkit.sample.aggregation.RuntimeAggregation;

public class RuntimeAggregationTest extends RuntimeAggregation {

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
