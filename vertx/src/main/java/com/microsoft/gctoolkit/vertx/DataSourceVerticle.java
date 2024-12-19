// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.message.DataSourceChannelListener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Vert.x verticle for handling data source messages.
 */
public class DataSourceVerticle extends AbstractVerticle {

    // Logger for the DataSourceVerticle class.
    private static final Logger LOGGER = Logger.getLogger(DataSourceVerticle.class.getName());

    // Vert.x instance.
    final private Vertx vertx;
    // Channel name for the inbox.
    final private String inbox;
    // ID of the verticle.
    private String id;
    // Listener for processing data source messages.
    final private DataSourceChannelListener processor;

    /**
     * Constructor for DataSourceVerticle.
     * @param vertx the Vert.x instance.
     * @param channelName the name of the channel.
     * @param listener the listener for processing data source messages.
     */
    public DataSourceVerticle(Vertx vertx, String channelName, DataSourceChannelListener listener) {
        this.vertx = vertx;
        this.inbox = channelName;
        this.processor = listener;
    }

    /**
     * Sets the ID of the verticle.
     * @param id the ID to set.
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Starts the verticle and sets up the event bus consumer for data source messages.
     * @param promise the promise to complete when the verticle is started.
     */
    @Override
    public void start(Promise<Void> promise) {
        try {
            vertx.eventBus().<String>consumer(inbox, message -> {
                processor.receive(message.body());
                if (GCLogFile.END_OF_DATA_SENTINEL.equals(message.body())) {
                    vertx.undeploy(id);
                }
            }).completionHandler(result -> {promise.complete();});
        } catch(Throwable t) {
            LOGGER.log(Level.WARNING,"Vertx: processing DataSource failed",t);
        }
    }

    /**
     * Checks if this verticle is equal to another object.
     * @param other the other object to compare.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        // we want Object.equals(other) because it's ok to have more than 1 AggregatorEngine on the bus
        // just not the same AggregatorEngine multiple times over and over redundantly
        return this == other;
    }

    /**
     * Returns the hash code of this verticle.
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        // see comment for equals
        // Kind of scary
        //  - Objects.hashCode(this) calls this method so that won't end well
        //  - super.hashCode() will make it up to Object.hashCode() as long as
        //    AbstractVerticle and Verticle *never* override hashCode(). If either
        //    of them does, all bets are off as to how this behaves.
        // - David's solution is implemented as it yields the desired result.
        return System.identityHashCode(this);
    }
}