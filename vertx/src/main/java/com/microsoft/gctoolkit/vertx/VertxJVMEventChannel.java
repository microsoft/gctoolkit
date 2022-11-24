// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import com.microsoft.gctoolkit.vertx.io.JVMEventCodec;
import io.vertx.core.Vertx;

public class VertxJVMEventChannel implements JVMEventChannel {

    private Channels channel;
    private Vertx vertx;

    public VertxJVMEventChannel() {
        vertx = Vertx.vertx();
        vertx.eventBus().registerDefaultCodec(JVMEvent.class, new JVMEventCodec());
    }

    @Override
    public void setChannel(Channels channel) {
        this.channel = channel;
    }

    @Override
    public void publish(JVMEvent message) {
        vertx.eventBus().publish(channel.getName(),message);
    }

    @Override
    public void registerListener(JVMEventChannelListener listener) {
        JVMEventVerticle processor = new JVMEventVerticle(vertx, channel.getName(), listener);
        vertx.deployVerticle(processor, state -> processor.setID((state.succeeded()) ? state.result() : ""));
    }
}
