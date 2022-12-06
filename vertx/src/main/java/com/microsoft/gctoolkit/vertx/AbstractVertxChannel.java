package com.microsoft.gctoolkit.vertx;

import io.vertx.core.Vertx;

public class AbstractVertxChannel {

    private static Vertx vertx = Vertx.vertx();

    protected AbstractVertxChannel() {
    }

    protected Vertx vertx() {
        return vertx;
    }

}
