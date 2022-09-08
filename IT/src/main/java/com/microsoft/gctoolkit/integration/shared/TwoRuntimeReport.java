package com.microsoft.gctoolkit.integration.shared;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.integration.shared.OneRuntimeAggregator;
import com.microsoft.gctoolkit.integration.shared.SharedAggregation;

@Collates(TwoRuntimeAggregator.class)

public class TwoRuntimeReport extends SharedAggregation implements Aggregation {


    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
