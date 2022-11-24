// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.DataSourceChannel;
import com.microsoft.gctoolkit.message.DataSourceChannelListener;
import io.vertx.core.Vertx;

public class VertxDataSourceChannel implements DataSourceChannel {

    private Channels channel;
    final private Vertx vertx;

    public VertxDataSourceChannel() {
        vertx = Vertx.vertx();
    }

    @Override
    public void setChannel(Channels channel) {
        this.channel = channel;
    }

    @Override
    public void publish(String message) {
        vertx.eventBus().publish(channel.getName(),message);
    }

    @Override
    public void registerListener(DataSourceChannelListener listener) {
        final DataSourceVerticle processor = new DataSourceVerticle(vertx, channel.getName(), listener);
        vertx.deployVerticle(processor, state -> processor.setID((state.succeeded()) ? state.result() : ""));
    }
}
