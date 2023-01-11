package com.microsoft.gctoolkit.vertx;

import io.vertx.core.Vertx;

public class VertxChannel {

    private static Vertx vertx;

    static {
        System.setProperty("vertx.disableFileCPResolving", "true");
        System.setProperty("vertx.disableFileCaching", "true");
        vertx = Vertx.vertx();
    }

    protected VertxChannel() {
    }

    protected Vertx vertx() {
        return vertx;
    }

}
