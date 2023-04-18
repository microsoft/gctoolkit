package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.vertx.io.JVMEventCodec;
import io.vertx.core.Vertx;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VertxChannel {

    protected static final Logger LOGGER = Logger.getLogger(VertxChannel.class.getName());

    //
    // Note well! This cannot be a static final field.
    // UnifiedJavaVirtualMachineConfigurationTest hangs if it is.
    //
    private final Vertx vertx;

    {
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

    public void close() {
        vertx().close(result -> {
            if (result.succeeded()) {
                LOGGER.log(Level.FINE, "Vertx: closed");
            } else {
                LOGGER.log(Level.FINE, "Vertx: close failed", result.cause());
            }
        });
    }

}
