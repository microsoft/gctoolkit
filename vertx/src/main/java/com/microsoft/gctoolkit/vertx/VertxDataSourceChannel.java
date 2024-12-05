// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.DataSourceChannel;
import com.microsoft.gctoolkit.message.DataSourceParser;

import java.util.concurrent.CountDownLatch;

/**
 * A class that represents a Vert.x data source channel.
 * It extends VertxChannel and implements DataSourceChannel.
 */
public class VertxDataSourceChannel extends VertxChannel implements DataSourceChannel {

    /**
     * Default constructor.
     */
    public VertxDataSourceChannel() {
        super();
    }

    /**
     * Registers a listener for the data source channel.
     * @param listener the DataSourceParser listener to register.
     */
    @Override
    public void registerListener(DataSourceParser listener) {
        final DataSourceVerticle processor = new DataSourceVerticle(vertx(), listener.channel().getName(), listener);
        CountDownLatch latch = new CountDownLatch(1);
        vertx().deployVerticle(processor, state -> {
            processor.setID((state.succeeded()) ? state.result() : "");
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    /**
     * Publishes a message to a specified channel.
     * @param channel the channel to publish to.
     * @param message the message to publish.
     */
    @Override
    public void publish(ChannelName channel, String message) {
        vertx().eventBus().publish(channel.getName(), message);
    }

    /**
     * Closes the data source channel.
     */
    @Override
    public void close() {
        super.close();
    }
}