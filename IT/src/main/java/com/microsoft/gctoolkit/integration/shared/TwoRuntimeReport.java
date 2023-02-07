package com.microsoft.gctoolkit.integration.shared;

import com.microsoft.gctoolkit.aggregator.Collates;

@Collates(TwoRuntimeAggregator.class)

public class TwoRuntimeReport extends SharedAggregation {


    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
