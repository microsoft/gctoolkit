package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.g1gc.G1GCConcurrentEvent;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.generational.GenerationalGCPauseEvent;
import com.microsoft.gctoolkit.event.shenandoah.ShenandoahCycle;
import com.microsoft.gctoolkit.event.zgc.ZGCCycle;

// Aggregates({EventSource.G1GC,EventSource.GENERATIONAL,EventSource.ZGC,EventSource.SHENANDOAH})
@Aggregates({EventSource.G1GC})
public class CollectionCycleCounts extends Aggregator<CollectionCycleCountsAggregation> {

    public CollectionCycleCounts(CollectionCycleCountsAggregation results) {
        super(results);
        //register(GenerationalGCPauseEvent.class, this::count);
        register(G1GCPauseEvent.class, this::count);
        register(G1GCConcurrentEvent.class, this::count);
        //register(ZGCCycle.class,this::count);
        //register(ShenandoahCycle.class,this::count);
    }

    public void count(G1GCPauseEvent pauseEvent) {
        aggregation().count(pauseEvent.getGarbageCollectionType());
    }

    public void count(G1GCConcurrentEvent concurrentEvent) {
        aggregation().count(concurrentEvent.getGarbageCollectionType());
    }
}

