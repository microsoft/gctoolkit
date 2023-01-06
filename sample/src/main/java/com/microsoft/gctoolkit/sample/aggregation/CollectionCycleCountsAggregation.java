package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;

@Collates(CollectionCycleCountsAggregator.class)
public abstract class CollectionCycleCountsAggregation extends Aggregation {

    abstract public void count(GarbageCollectionTypes gcType);

}
