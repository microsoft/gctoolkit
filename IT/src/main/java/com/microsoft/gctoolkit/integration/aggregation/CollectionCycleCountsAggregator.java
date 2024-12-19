package com.microsoft.gctoolkit.integration.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.g1gc.G1GCConcurrentEvent;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.generational.GenerationalGCPauseEvent;
import com.microsoft.gctoolkit.event.shenandoah.ShenandoahCycle;
import com.microsoft.gctoolkit.event.zgc.MajorZGCCycle;

@Aggregates({EventSource.G1GC,EventSource.GENERATIONAL,EventSource.ZGC,EventSource.SHENANDOAH})
public class CollectionCycleCountsAggregator extends Aggregator<CollectionCycleCountsAggregation> {

    public CollectionCycleCountsAggregator(CollectionCycleCountsAggregation results) {
        super(results);
        register(GenerationalGCPauseEvent.class, this::count);
        register(G1GCPauseEvent.class, this::count);
        register(G1GCConcurrentEvent.class, this::count);
        register(MajorZGCCycle.class,this::count);
        register(ShenandoahCycle.class,this::count);
    }

    private void count(MajorZGCCycle event) {
        aggregation().count(event.getGarbageCollectionType());
    }

    private void count(ShenandoahCycle event) {
        aggregation().count(event.getGarbageCollectionType());
    }

    public void count(G1GCPauseEvent event) {
        aggregation().count(event.getGarbageCollectionType());
    }

    public void count(G1GCConcurrentEvent event) {
        aggregation().count(event.getGarbageCollectionType());
    }

    public void count(GenerationalGCPauseEvent event) {
        aggregation().count(event.getGarbageCollectionType());
    }
}

