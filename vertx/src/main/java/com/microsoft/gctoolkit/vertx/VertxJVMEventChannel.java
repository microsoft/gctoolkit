// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import com.microsoft.gctoolkit.vertx.io.JVMEventCodec;
import io.vertx.core.eventbus.DeliveryOptions;

public class VertxJVMEventChannel extends VertxChannel implements JVMEventChannel {

    final private DeliveryOptions options = new DeliveryOptions().setCodecName(JVMEventCodec.NAME);

    public VertxJVMEventChannel() {
        try {
            vertx().eventBus().registerDefaultCodec(JVMEvent.class, new JVMEventCodec());
        } catch (Throwable t) {} //todo: testing only
    }

    @Override
    public void publish(Channels channel, JVMEvent message) {
        try {
            vertx().eventBus().publish(channel.getName(), message, options);
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println(message);
        }
    }

    @Override
    public void close() {
//        try {
            vertx().eventBus().unregisterDefaultCodec(JVMEvent.class);
//            vertx().close();
//        } catch(IllegalStateException ise) {
//            //Yes, I am intentionally ignoring this exception because I can.
//        }
    }

    @Override
    public void registerListener(JVMEventChannelListener listener) {
        JVMEventVerticle processor = new JVMEventVerticle(vertx(), listener.channel().getName(), listener);
        vertx().deployVerticle(processor, state -> processor.setID((state.succeeded()) ? state.result() : ""));
    }
}
