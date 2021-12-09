package com.microsoft.gctoolkit.integration.aggregation;

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

    private String format = "%s : %s\n";
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
