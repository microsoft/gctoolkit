package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;

public class JVMEventVerticle extends AbstractVerticle {

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
        try {
            vertx.eventBus().<JVMEvent>consumer(inbox, message -> {
                JVMEvent event = message.body();
                processor.receive(event);
                if ( event instanceof JVMTermination) {
                    vertx.undeploy(id);
                    //todo: something
                }
            });
            promise.complete();
        } catch(Throwable t) {
            //todo: logging
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
