package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.vertx.io.JVMEventCodec;
import io.vertx.core.Vertx;

public class VertxChannel {

    private static Vertx vertx;

    static {
        //Disable unused Vert.x functionality
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
