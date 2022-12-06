// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import io.vertx.core.AbstractVerticle;

public class JVMEventMailbox extends AbstractVerticle implements JVMEventChannel {

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
    public void registerListener(JVMEventChannelListener listener) {

    }

    @Override
    public void publish(Channels channel, JVMEvent message) {
    }
}
