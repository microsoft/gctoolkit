package com.microsoft.gctoolkit.integration.shared;

import com.microsoft.gctoolkit.aggregator.Collates;

@Collates(OneRuntimeAggregator.class)
public class OneRuntimeReport extends SharedAggregation {


    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
