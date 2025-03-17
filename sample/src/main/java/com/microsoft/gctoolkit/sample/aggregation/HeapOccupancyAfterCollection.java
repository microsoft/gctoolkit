package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.generational.GenerationalGCPauseEvent;
import com.microsoft.gctoolkit.event.shenandoah.ShenandoahCycle;
import com.microsoft.gctoolkit.event.zgc.FullZGCCycle;
import com.microsoft.gctoolkit.event.zgc.MajorZGCCycle;
import com.microsoft.gctoolkit.event.zgc.MinorZGCCycle;

@Aggregates({EventSource.G1GC,EventSource.GENERATIONAL,EventSource.ZGC,EventSource.SHENANDOAH})
public class HeapOccupancyAfterCollection extends Aggregator<HeapOccupancyAfterCollectionAggregation> {

    public HeapOccupancyAfterCollection(HeapOccupancyAfterCollectionAggregation results) {
        super(results);
        register(GenerationalGCPauseEvent.class, this::extractHeapOccupancy);
        register(G1GCPauseEvent.class, this::extractHeapOccupancy);
        register(FullZGCCycle.class,this::extractHeapOccupancy);
        register(MajorZGCCycle.class,this::extractHeapOccupancy);
        register(MinorZGCCycle.class,this::extractHeapOccupancy);
        register(ShenandoahCycle.class,this::extractHeapOccupancy);
    }

    private void extractHeapOccupancy(MinorZGCCycle event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getMemorySummary().getOccupancyAfter());
    }

    private void extractHeapOccupancy(MajorZGCCycle event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getMemorySummary().getOccupancyAfter());
    }

    private void extractHeapOccupancy(GenerationalGCPauseEvent event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getHeap().getOccupancyAfterCollection());
    }

    private void extractHeapOccupancy(G1GCPauseEvent event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getHeap().getOccupancyAfterCollection());

    }

    private void extractHeapOccupancy(FullZGCCycle event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getMemorySummary().getOccupancyAfter());
    }

    private void extractHeapOccupancy(ShenandoahCycle event) {
        //aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getOccupancy());
    }
}

