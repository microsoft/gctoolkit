// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.vertx.jvm;

import com.microsoft.censum.aggregator.Aggregates;
import com.microsoft.censum.aggregator.Aggregator;
import com.microsoft.censum.aggregator.EventSource;
import com.microsoft.censum.event.jvm.JVMEvent;
import com.microsoft.censum.event.jvm.JVMTermination;
import com.microsoft.censum.jvm.JavaVirtualMachine;
import com.microsoft.censum.time.DateTimeStamp;

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
