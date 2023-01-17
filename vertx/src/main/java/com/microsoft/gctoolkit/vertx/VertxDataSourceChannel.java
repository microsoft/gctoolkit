// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.DataSourceChannel;
import com.microsoft.gctoolkit.message.DataSourceParser;

import java.util.concurrent.CountDownLatch;

public class VertxDataSourceChannel extends VertxChannel implements DataSourceChannel {

    public VertxDataSourceChannel() {
        super();
    }

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

    @Override
    public void publish(Channels channel, String message) {
        vertx().eventBus().publish(channel.getName(),message);
    }

    @Override
    public void close() {}
}
