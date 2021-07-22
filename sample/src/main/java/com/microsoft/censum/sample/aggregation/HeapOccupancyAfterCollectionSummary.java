package com.microsoft.censum.sample.aggregation;

import com.microsoft.censum.aggregator.Collates;
import com.microsoft.censum.collections.CensumXYDataSet;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

import java.util.HashMap;

@Collates(HeapOccupancyAfterCollection.class)
public class HeapOccupancyAfterCollectionSummary implements HeapOccupancyAfterCollectionAggregation {

    private HashMap<GarbageCollectionTypes,CensumXYDataSet> aggregations = new HashMap();

    public void addDataPoint(GarbageCollectionTypes gcType, DateTimeStamp timeStamp, long heapOccupancy) {
        CensumXYDataSet dataSet = aggregations.get(gcType);
        if ( dataSet == null) {
            dataSet = new CensumXYDataSet();
            aggregations.put(gcType,dataSet);
        }
        dataSet.add(timeStamp.getTimeStamp(),heapOccupancy);
    }

    public HashMap<GarbageCollectionTypes,CensumXYDataSet> get() {
        return aggregations;
    }

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return aggregations.isEmpty();
    }

    public String toString() {
        return "Collected " + aggregations.size() + " elements";
    }
}
