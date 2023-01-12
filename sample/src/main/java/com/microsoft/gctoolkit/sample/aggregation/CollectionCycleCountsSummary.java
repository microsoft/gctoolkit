package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.event.GarbageCollectionTypes;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CollectionCycleCountsSummary extends CollectionCycleCountsAggregation {

    private final Map<GarbageCollectionTypes, AtomicInteger> collectionCycleCounts = new HashMap<>();

    @Override
    public void count(GarbageCollectionTypes gcType) {
        collectionCycleCounts.computeIfAbsent(gcType, key -> new AtomicInteger()).incrementAndGet();
    }

    private static final String FORMAT = "%s : %s%n";

    public void printOn(PrintStream printStream) {
        collectionCycleCounts.forEach((k, v) -> printStream.printf(FORMAT, k, v));
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
