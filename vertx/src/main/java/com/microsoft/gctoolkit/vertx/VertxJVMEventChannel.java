// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import com.microsoft.gctoolkit.vertx.io.JVMEventCodec;

public class VertxJVMEventChannel extends AbstractVertxChannel implements JVMEventChannel {

    public VertxJVMEventChannel() {
        super();
        vertx().eventBus().registerDefaultCodec(JVMEvent.class, new JVMEventCodec());
    }

    @Override
    public void publish(Channels channel, JVMEvent message) {
        vertx().eventBus().publish(channel.getName(),message);
    }

    @Override
    public void registerListener(JVMEventChannelListener listener) {
        JVMEventVerticle processor = new JVMEventVerticle(vertx(), listener.channel().getName(), listener);
        vertx().deployVerticle(processor, state -> processor.setID((state.succeeded()) ? state.result() : ""));
    }
}
