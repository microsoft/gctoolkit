// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.vertx.io.JVMEventCodec;
import io.vertx.core.Vertx;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that represents a Vert.x channel for handling JVM events.
 */
public class VertxChannel {

    // Logger for the VertxChannel class.
    protected static final Logger LOGGER = Logger.getLogger(VertxChannel.class.getName());

    // Note well! This cannot be a static final field.
    // UnifiedJavaVirtualMachineConfigurationTest hangs if it is.
    private final Vertx vertx;

    {
        // Disable unused Vert.x functionality
        System.setProperty("vertx.disableFileCPResolving", "true");
        System.setProperty("vertx.disableFileCaching", "true");
        vertx = Vertx.vertx();
        vertx.eventBus().registerDefaultCodec(JVMEvent.class, new JVMEventCodec());
    }

    /**
     * Default constructor.
     */
    protected VertxChannel() {
    }

    /**
     * Gets the Vert.x instance.
     * @return the Vert.x instance.
     */
    protected Vertx vertx() {
        return vertx;
    }

    /**
     * Closes the Vert.x instance.
     */
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