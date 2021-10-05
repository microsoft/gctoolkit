package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.aggregators.RuntimeAggregator;
import com.microsoft.gctoolkit.event.g1gc.G1RealPause;
import com.microsoft.gctoolkit.event.generational.GenerationalGCPauseEvent;

/**
 * An Aggregator that extracts pause time.
 */
@Aggregates({EventSource.G1GC, EventSource.GENERATIONAL})
public class PauseTimeAggregator extends RuntimeAggregator<PauseTimeAggregation> {

    public PauseTimeAggregator(PauseTimeAggregation aggregation) {
        super(aggregation);
        register(G1RealPause.class, this::process);
        register(GenerationalGCPauseEvent.class, this::record);
    }

    private void record(GenerationalGCPauseEvent event) {
        aggregation().recordPauseDuration(event.getDuration());
    }

    private void process(G1RealPause event) {
        aggregation().recordPauseDuration(event.getDuration());
    }
}
