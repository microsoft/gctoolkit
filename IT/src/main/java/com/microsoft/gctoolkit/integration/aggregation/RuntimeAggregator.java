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

    // This is atypical of an Aggregator. The typical pattern is to register the method that processes
    // an event in the constructor of the Aggregator. Here we rely on the consume(E event) method to
    // invoke this process method. This ensures that any JVMEvent gets handled by this process method
    // regardless of what JVMEvent classes are registered by subclasses of RuntimeAggregator.
    private void process(JVMEvent event) {
        if (event instanceof JVMTermination) return;
        aggregation().publish(event.getDateTimeStamp(), event.getDuration());
    }

    @Override
    public void receive(JVMEvent event) {
        process(event);
        super.receive(event);
    }
}
