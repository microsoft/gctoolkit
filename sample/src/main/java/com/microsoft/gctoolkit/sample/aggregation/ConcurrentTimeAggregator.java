package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.g1gc.G1GCConcurrentEvent;

/**
 * An Aggregator that extracts pause time.
 */
@Aggregates({EventSource.G1GC})
public class ConcurrentTimeAggregator extends Aggregator<ConcurrentTimeAggregation> {

    public ConcurrentTimeAggregator(ConcurrentTimeAggregation aggregation) {
        super(aggregation);
        register(G1GCConcurrentEvent.class, this::process);
    }

    private void process(G1GCConcurrentEvent event) {
        aggregation().recordDuration(event.getDuration());
    }
}
