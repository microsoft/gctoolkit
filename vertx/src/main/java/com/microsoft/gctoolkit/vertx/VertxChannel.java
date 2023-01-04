package com.microsoft.gctoolkit.vertx;

import io.vertx.core.Vertx;

public class VertxChannel {

    private static Vertx vertx = Vertx.vertx();

    protected VertxChannel() {
    }

    protected Vertx vertx() {
        return vertx;
    }

}
