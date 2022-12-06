package com.microsoft.gctoolkit.integration.shared;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;

import java.util.Arrays;

@Aggregates({EventSource.G1GC,EventSource.GENERATIONAL,EventSource.SHENANDOAH,EventSource.ZGC})
public class TwoRuntimeAggregator extends Aggregator<TwoRuntimeReport> {
    /**
     * Subclass only.
     *
     * @param aggregation The Aggregation that {@literal @}Collates this Aggregator
     */
    public TwoRuntimeAggregator(TwoRuntimeReport aggregation) {
        super(aggregation);
        register(JVMTermination.class,this::publish);
    }

    public void publish(JVMTermination termination) {
        System.out.println("sleeping: " + termination.toString());
        try {
            Thread.sleep(1000);
            StackTraceElement[] threadDump = Thread.currentThread().getStackTrace();
            Arrays.stream(threadDump).forEach(System.out::println);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        aggregation().terminate(termination.getEstimatedRuntimeDuration());
    }
}
