// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.aggregator;

import com.microsoft.censum.event.jvm.JVMEvent;
import com.microsoft.censum.event.jvm.JVMTermination;

import java.util.function.Consumer;

/**
 * An Aggregator consumes a JVMEvent and accumulates counts and stats about that JVMEvent.
 * JVMEvents are {@link #consume(JVMEvent) consumed} and dispatched to methods written to
 * handle a particular kind of event. Aggregators should {@link #register(Class, Consumer)}
 * the event handlers in their constructor.
 * <code>
 *     <pre>
 *public class G1GCCauseAggregator extends GCCauseAggregator {
 *
 *     public G1GCCauseAggregator() {
 *         register(G1Young.class, this::process);
 *         register(G1Mixed.class, this::process);
 *         register(G1YoungInitialMark.class, this::process);
 *         register(G1FullGC.class, this::process);
 *     }
 *
 *     private void process(G1Young collection) {
 *         record(GarbageCollectionTypes.Young, collection.getGCCause());
 *     }
 *
 *     private void process(G1Mixed collection) {
 *         record(GarbageCollectionTypes.Mixed, collection.getGCCause());
 *     }
 *
 *     private void process(G1YoungInitialMark collection) {
 *         record(GarbageCollectionTypes.G1GCYoungInitialMark, collection.getGCCause());
 *     }
 *
 *     private void process(G1FullGC collection) {
 *         record(GarbageCollectionTypes.FullGC, collection.getGCCause());
 *     }
 * }
 *     </pre>
 * </code>
 */
public abstract class Aggregator<A extends Aggregation> {

    private final A aggregation;

    /// JVMEventDispatcher manages all of the registered events and event consumers
    private final JVMEventDispatcher jvmEventDispatcher = new JVMEventDispatcher();
    volatile private boolean done = false;

    /**
     * Subclass only.
     */
    protected Aggregator(A aggregation) {
        this.aggregation = aggregation;
        register(JVMTermination.class,this::terminationHandler);
    }

    /**
     * This method returns the {@link Aggregation} that collates the data
     * which is collected by this {@code Aggregator}.
     * @return The Aggregator's corresponding Aggregation
     */
    public A aggregation() {
        return aggregation;
    }

    /**
     * Register a JVMEvent class and the method in the Aggregator sub-class that handles it.
     * If the JVMEvent class is a super-class of other event types, then the Consumer is called
     * for all sub-classes of that JVMEvent class, unless a Consumer for a more specific event class
     * is registered.
     * <p>
     * The typical pattern is to call this method from the constructor of the Aggregator sub-class.
     * <code><pre>
     *     register(G1Young.class, this::process);
     * </pre></code>
     * The {@code Consumer} for this example would be coded as:
     * <code><pre>
     *     private void process(G1Young collection) {...}
     * </pre></code>
     * Where the body of the method would pull out and aggregate the relevant data from the event.
     * @param eventClass the Class of the JVMEvent type to register.
     * @param process the handler which processes the event
     */
    protected <E extends JVMEvent> void register(Class<E> eventClass, Consumer<? super E> process) {
        jvmEventDispatcher.register(eventClass, process);
    }

    /**
     * This method consumes a JVMEvent and dispatches it to the
     * {@link #register(Class, Consumer) registered consumer}.
     * @param event an event to be processed
     */
    public <R extends JVMEvent> void consume(R event) {
        jvmEventDispatcher.dispatch(event);
    }

    /**
     * The JVMTermination event signals the end of processing of a log file.
     * @param event JVMTermination is a sentinel for the end of log processing.
     */
    private void terminationHandler(JVMTermination event) {
        if (!done) done = true;
    }

    /**
     * Whether all events have been processed by the Aggregator.
     * @return {@code true} if this Aggregator is done processing events.
     */
    public boolean isDone() {
        return done;
    }
}
