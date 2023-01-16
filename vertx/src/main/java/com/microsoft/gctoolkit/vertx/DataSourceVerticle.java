package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.message.DataSourceChannelListener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DataSourceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = Logger.getLogger(DataSourceVerticle.class.getName());

    final private Vertx vertx;
    final private String inbox;
    private String id;
    final private DataSourceChannelListener processor;


    public DataSourceVerticle(Vertx vertx, String channelName, DataSourceChannelListener listener) {
        this.vertx = vertx;
        this.inbox = channelName;
        this.processor = listener;
    }

    public void setID(String id) {
        this.id = id;
    }

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

    @Override
    public void stop(Promise promise) {
        promise.complete();
    }

    @Override
    public boolean equals(Object other) {
        // we want Object.equals(other) because it's ok to have more than 1 AggregatorEngine on the bus
        // just not the same AggregatorEngine multiple times over and over redundantly
        return this == other;
    }

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
