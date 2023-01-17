// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import com.microsoft.gctoolkit.vertx.io.JVMEventCodec;
import io.vertx.core.eventbus.DeliveryOptions;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VertxJVMEventChannel extends VertxChannel implements JVMEventChannel {

    private static final Logger LOGGER = Logger.getLogger(VertxJVMEventChannel.class.getName());
    final private DeliveryOptions options = new DeliveryOptions().setCodecName(JVMEventCodec.NAME);

    public VertxJVMEventChannel() {}

    @Override
    public void registerListener(JVMEventChannelListener listener) {
        final JVMEventVerticle processor = new JVMEventVerticle(vertx(), listener.channel().getName(), listener);
        CountDownLatch latch = new CountDownLatch(1);
        vertx().deployVerticle(processor, state -> {
            processor.setID((state.succeeded()) ? state.result() : "");
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(Channels channel, JVMEvent message) {
        try {
            vertx().eventBus().publish(channel.getName(), message, options);
        } catch(Exception ex) {
            LOGGER.log(Level.SEVERE, "Vert.x: Unable to publish message: " + message, ex);
        }
    }

    @Override
    public void close() {}
}
