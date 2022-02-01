package com.microsoft.gctoolkit.message;

import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;

import java.util.List;

public interface JVMEventBus {

    void registerAggregators(List<Aggregator> aggregators);
    void registerAggregator(Aggregator aggregator);
    void start();
    void stop();
    void publish(JVMEvent event);

}
