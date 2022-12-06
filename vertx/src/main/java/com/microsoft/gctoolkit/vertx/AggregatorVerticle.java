// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.parser.JVMEventConsumer;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.vertx.internal.util.concurrent.StartingGun;
import io.vertx.core.AbstractVerticle;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AggregatorVerticle extends AbstractVerticle implements JVMEventConsumer {

    private static class AggregatorWrapper extends Aggregator<Aggregation> {

        private final Aggregator<?> proxy;
        private Runnable completionHandler;

        private AggregatorWrapper(Aggregator<?> proxy) {
            super(null);
            this.proxy = proxy;
        }
        @Override
        public Aggregation aggregation() {
            return proxy.aggregation();
        }

        @Override
        public void receive(JVMEvent event) {
            proxy.receive(event);
            if (isDone()) {
                if (completionHandler != null) completionHandler.run();
            }
        }

        @Override
        public boolean isDone() {
            return proxy.isDone();
        }

        /**
         * Allows other code to run once this Aggregator is done processing events.
         * @param runnable The code to run once this Aggregator is done processing events.
         */
        public void onCompletion(Runnable runnable) {
            this.completionHandler = runnable;
            if (isDone()) runnable.run();
        }

    }
    private static final Logger LOGGER = Logger.getLogger(AggregatorVerticle.class.getName());

    private final Set<Aggregator<?>> aggregators = new HashSet<>();
    private final StartingGun deployed = new StartingGun();
    private final StartingGun completion = new StartingGun();

    private volatile DateTimeStamp timeOfTerminationEvent;

    private final String inbox;

    public AggregatorVerticle(String inbox) {
        this.inbox = inbox;
    }
    
    public String getInbox() {
    	return this.inbox;
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

    public void registerAggregator(Aggregator<?> aggregator) {
        aggregators.add(new AggregatorWrapper(aggregator));
    }

    public Set<Aggregator<?>> aggregators() { return aggregators; }

    public void publish(JVMEvent event) {
        aggregators.forEach(aggregator -> {
                    try {
                        aggregator.receive(event);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error in aggregator", e);
                    }
                }
        );
    }

    public void awaitDeployment() {
        deployed.awaitUninterruptibly();
    }

    /*
    we need some check on the state of the consumer...
     */
    public DateTimeStamp awaitCompletion() {
        completion.awaitUninterruptibly();
        return timeOfTerminationEvent;
    }

    private void monitorForTermination() {
        Executors.newSingleThreadExecutor().execute(() -> completion.awaitUninterruptibly());
    }

    @Override
    public void start() {
        try {
            monitorForTermination();
            vertx.eventBus().
                    <JVMEvent>consumer(inbox, message -> {
                        try {
                            JVMEvent event = message.body();
                            this.publish(event);
                            if ( event instanceof JVMTermination) {
                                timeOfTerminationEvent = event.getDateTimeStamp();
                                completion.ready();
                            }
                        } catch (Throwable t) {
                            LOGGER.throwing(this.getClass().getName(), "start", t);
                        }
                    });
            deployed.ready();
        } catch (Throwable t) {
            LOGGER.throwing(this.getClass().getName(), "start", t);
        }
    }


}
