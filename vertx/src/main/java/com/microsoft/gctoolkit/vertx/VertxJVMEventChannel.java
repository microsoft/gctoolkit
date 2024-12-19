// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import com.microsoft.gctoolkit.vertx.io.JVMEventCodec;
import io.vertx.core.eventbus.DeliveryOptions;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that represents a Vert.x JVM event channel.
 * It extends VertxChannel and implements JVMEventChannel.
 */
public class VertxJVMEventChannel extends VertxChannel implements JVMEventChannel {

    // Delivery options for the event bus, using the JVMEventCodec.
    final private DeliveryOptions options = new DeliveryOptions().setCodecName(JVMEventCodec.NAME);

    /**
     * Default constructor.
     */
    public VertxJVMEventChannel() {}

    /**
     * Registers a listener for the JVM event channel.
     * @param listener the JVMEventChannelListener to register.
     */
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
            Thread.interrupted();
            LOGGER.log(Level.SEVERE, "Vert.x: Latch.await interrupted: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Publishes a JVM event message to a specified channel.
     * @param channel the channel to publish to.
     * @param message the JVMEvent message to publish.
     */
    @Override
    public void publish(ChannelName channel, JVMEvent message) {
        try {
            vertx().eventBus().publish(channel.getName(), message, options);
        } catch(Exception ex) {
            LOGGER.log(Level.SEVERE, "Vert.x: Unable to publish message: " + message, ex);
        }
    }

    /**
     * Closes the VertxChannel.
     */
    @Override
    public void close() {
        super.close();
    }
}