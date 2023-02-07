package com.microsoft.gctoolkit.integration.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public abstract class HeapOccupancyAfterCollectionAggregation extends Aggregation {

    public abstract void addDataPoint(GarbageCollectionTypes gcType, DateTimeStamp timeStamp, long heapOccupancy);

}
