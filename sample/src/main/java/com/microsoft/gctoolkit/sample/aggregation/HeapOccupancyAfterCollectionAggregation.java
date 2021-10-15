package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public interface HeapOccupancyAfterCollectionAggregation extends Aggregation {

    void addDataPoint(GarbageCollectionTypes gcType, DateTimeStamp timeStamp, long heapOccupancy);

}
