package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.event.GarbageCollectionTypes;

import java.io.PrintStream;
import java.util.HashMap;

public class CollectionCycleCountsSummary implements CollectionCycleCountsAggregation {

    private HashMap<GarbageCollectionTypes,Integer> collectionCycleCounts = new HashMap<>();
    @Override
    public void count(GarbageCollectionTypes gcType) {
        if ( !collectionCycleCounts.containsKey(gcType))
            collectionCycleCounts.put(gcType,0);
        collectionCycleCounts.put(gcType, collectionCycleCounts.get(gcType) + 1);
    }

    private String format = "%s : %s";
    public void printOn(PrintStream printStream) {
        collectionCycleCounts.keySet().forEach(k -> printStream.printf(format,k, collectionCycleCounts.get(k)));
    }

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return collectionCycleCounts.isEmpty();
    }
}

/*
@Collates(HeapOccupancyAfterCollection.class)
public class HeapOccupancyAfterCollectionSummary implements HeapOccupancyAfterCollectionAggregation {

    private final Map<GarbageCollectionTypes, XYDataSet> aggregations = new ConcurrentHashMap<>();

    public void addDataPoint(GarbageCollectionTypes gcType, DateTimeStamp timeStamp, long heapOccupancy) {
        XYDataSet dataSet = aggregations.get(gcType);
        if ( dataSet == null) {
            dataSet = new XYDataSet();
            aggregations.put(gcType,dataSet);
        }
        dataSet.add(timeStamp.getTimeStamp(),heapOccupancy);
    }

    public Map<GarbageCollectionTypes, XYDataSet> get() {
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

    @Override
    public String toString() {
        return "Collected " + aggregations.size() + " different collection types";
    }
}
 */
