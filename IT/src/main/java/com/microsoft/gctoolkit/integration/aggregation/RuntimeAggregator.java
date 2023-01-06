package com.microsoft.gctoolkit.integration.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;

/**
 * An Aggregator that collects only the DateTimeStamp and duration of a JVMEvent.
 */
public class RuntimeAggregator<T extends RuntimeAggregation> extends Aggregator<T> {

    protected RuntimeAggregator(T aggregation) {
        super(aggregation);
    }

}
