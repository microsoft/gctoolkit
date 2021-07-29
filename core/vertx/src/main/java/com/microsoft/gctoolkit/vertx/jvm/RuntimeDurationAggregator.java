// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;

@Aggregates({EventSource.G1GC, EventSource.GENERATIONAL, EventSource.ZGC, EventSource.SHENANDOAH})
/* package-scope */ class RuntimeDurationAggregator extends Aggregator<RuntimeDuration> {

    /* package-scope */ RuntimeDurationAggregator(RuntimeDuration runtimeDuration) {
        super(runtimeDuration);
        register(JVMEvent.class, this::process);
    }

    private void process(JVMEvent event) {
        if (event instanceof JVMTermination) return;
        aggregation().record(event.getDateTimeStamp(), event.getDuration());
    }

}
