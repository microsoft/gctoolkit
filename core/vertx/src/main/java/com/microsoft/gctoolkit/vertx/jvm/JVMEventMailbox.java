// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import io.vertx.core.AbstractVerticle;

public class JVMEventMailbox extends AbstractVerticle {

    private JVMEventSink sinkPoint;
    private String lineSource, eventSource;

    public JVMEventMailbox(String lineSource, String eventSource, JVMEventSink sinkPoint) {
        this.lineSource = lineSource;
        this.eventSource = eventSource;
        this.sinkPoint = sinkPoint;
    }

    @Override
    public void start() {
        sinkPoint.start(vertx, lineSource, eventSource);
    }

}
