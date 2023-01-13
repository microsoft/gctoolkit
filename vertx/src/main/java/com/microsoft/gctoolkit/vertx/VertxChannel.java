package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.vertx.io.JVMEventCodec;
import io.vertx.core.Vertx;

public class VertxChannel {

    private static Vertx vertx;

    static {
        // Properties that will override -D settings. todo: Why we disable should be investigated in the future
        System.setProperty("vertx.disableFileCPResolving", "true");
        System.setProperty("vertx.disableFileCaching", "true");
        vertx = Vertx.vertx();
        vertx.eventBus().registerDefaultCodec(JVMEvent.class, new JVMEventCodec());
    }

    protected VertxChannel() {
    }

    protected Vertx vertx() {
        return vertx;
    }

}
