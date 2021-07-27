package com.microsoft.censum.sample.aggregation;

import com.microsoft.censum.aggregator.Aggregator;
import com.microsoft.censum.aggregator.Aggregates;
import com.microsoft.censum.aggregator.EventSource;
import com.microsoft.censum.event.g1gc.G1GCPauseEvent;
import com.microsoft.censum.event.generational.GenerationalGCPauseEvent;
import com.microsoft.censum.event.shenandoah.ShenandoahCycle;
import com.microsoft.censum.event.zgc.ZGCCycle;

@Aggregates({EventSource.G1GC,EventSource.GENERATIONAL,EventSource.ZGC,EventSource.SHENANDOAH})
public class HeapOccupancyAfterCollection extends Aggregator<HeapOccupancyAfterCollectionAggregation> {

    public HeapOccupancyAfterCollection(HeapOccupancyAfterCollectionAggregation results) {
        super(results);
        register(GenerationalGCPauseEvent.class, this::extractHeapOccupancy);
        register(G1GCPauseEvent.class, this::extractHeapOccupancy);
        register(ZGCCycle.class,this::extractHeapOccupancy);
        register(ShenandoahCycle.class,this::extractHeapOccupancy);
    }

    private void extractHeapOccupancy(GenerationalGCPauseEvent event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getHeap().getOccupancyAfterCollection());
    }

    private void extractHeapOccupancy(G1GCPauseEvent event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getHeap().getOccupancyAfterCollection());

    }

    private void extractHeapOccupancy(ZGCCycle event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getLive().getReclaimEnd());
    }

    private void extractHeapOccupancy(ShenandoahCycle event) {
        //aggregation.addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getOccupancyAfterMark());
    }
}

