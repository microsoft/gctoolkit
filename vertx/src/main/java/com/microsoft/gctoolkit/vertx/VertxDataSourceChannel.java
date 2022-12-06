// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.DataSourceChannel;
import com.microsoft.gctoolkit.message.DataSourceParser;

public class VertxDataSourceChannel extends AbstractVertxChannel implements DataSourceChannel {

    public VertxDataSourceChannel() {
        super();
    }

    @Override
    public void registerListener(DataSourceParser listener) {
        final DataSourceVerticle processor = new DataSourceVerticle(vertx(), listener.channel().getName(), listener);
        vertx().deployVerticle(processor, state -> processor.setID((state.succeeded()) ? state.result() : ""));
    }

    @Override
    public void publish(Channels channel, String message) {
        vertx().eventBus().publish(channel.getName(),message);
    }
}
