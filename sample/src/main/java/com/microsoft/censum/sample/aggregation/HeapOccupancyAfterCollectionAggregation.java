package com.microsoft.censum.sample.aggregation;

import com.microsoft.censum.aggregator.Aggregation;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public interface HeapOccupancyAfterCollectionAggregation extends Aggregation {

    public void addDataPoint(GarbageCollectionTypes gcType, DateTimeStamp timeStamp, long heapOccupancy);

}