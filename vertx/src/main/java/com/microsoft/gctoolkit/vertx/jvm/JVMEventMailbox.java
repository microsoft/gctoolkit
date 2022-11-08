// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.message.JVMEventBus;
import com.microsoft.gctoolkit.message.JVMEventListener;
import io.vertx.core.AbstractVerticle;

public class JVMEventMailbox extends AbstractVerticle implements JVMEventBus {

    private final JVMEventSink sinkPoint;
    private final String lineSource;
    private final String eventSource;

    public JVMEventMailbox(String lineSource, String eventSource, JVMEventSink sinkPoint) {
        this.lineSource = lineSource;
        this.eventSource = eventSource;
        this.sinkPoint = sinkPoint;
    }

    @Override
    public void start() {
        sinkPoint.start(vertx, lineSource, eventSource);
    }

    @Override
    public void registerJVMEventListener(JVMEventListener listener) {

    }

    @Override
    public void close() {

    }

    @Override
    public void publish(JVMEvent event) {

    }
}
