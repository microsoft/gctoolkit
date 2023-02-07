package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JVMEventVerticle extends AbstractVerticle {

    private static final Logger LOGGER = Logger.getLogger(JVMEventVerticle.class.getName());
    final private Vertx vertx;
    final private String inbox;
    final private JVMEventChannelListener processor;
    private String id;


    public JVMEventVerticle(Vertx vertx, String channelName, JVMEventChannelListener listener) {
        this.vertx = vertx;
        this.inbox = channelName;
        this.processor = listener;
    }

    public void setID(String id) {
        this.id = id;
    }

    @Override
    public void start(Promise<Void> promise) {
        vertx.eventBus().<JVMEvent>consumer(inbox, message -> {
            JVMEvent event = message.body();
            try {
                processor.receive(event);
            } catch (Throwable t) {
                // Throwable is caught because we don't want the processor to blow up the message bus.
                LOGGER.log(Level.WARNING, "Vertx: processing JVMEvent failed", t);
            }
            if (event instanceof JVMTermination) {
                vertx.undeploy(id);
            }
        }).completionHandler(result -> {promise.complete();});
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
