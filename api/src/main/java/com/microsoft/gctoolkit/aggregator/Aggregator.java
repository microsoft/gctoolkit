// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.aggregator;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;

import java.util.function.Consumer;

/**
 * An Aggregator consumes a JVMEvent, extracts data from the event, and calls on an
 * Aggregation which collates the data.
 * An Aggregators uses the <code>{@literal @}Aggregates</code> annotation to declare the
 * EventSource(s) of the JVMEvents it handles.
 * The constructor of an Aggregator must call {@link #register(Class, Consumer)}
 * to register the consumer methods of JVMEvents the Aggregator consumes.
 * <p>
 * This example Aggregator aggregates events from the G1GC event source. It consumes
 * and processes four different events. The {@code Consumer} method for each event extracts
 * the cause of the collection and calls the GCCauseAggregation {@code record} method.
 *
 * <pre><code>
 *
 * {@literal @}Collates(GCCauseAggregator)
 * public interface GCCauseAggregation extends Aggregation {
 *     publish(GarbageCollectionType type, GCCause cause);
 * }
 *
 * {@literal @}Aggregates(EventSource.G1GC)
 * public class GCCauseAggregator extends Aggregator{@literal <}GCCauseAggregation{@literal >} {
 *
 *     public GCCauseAggregator(GCCauseAggregation aggregation) {
 *         super(aggregation);
 *         register(G1Young.class, this::process);
 *         register(G1Mixed.class, this::process);
 *         register(G1YoungInitialMark.class, this::process);
 *         register(G1FullGC.class, this::process);
 *     }
 *
 *     private void process(G1Young collection) {
 *         aggregation().publish(GarbageCollectionTypes.Young, collection.getGCCause());
 *     }
 *
 *     private void process(G1Mixed collection) {
 *         aggregation().publish(GarbageCollectionTypes.Mixed, collection.getGCCause());
 *     }
 *
 *     private void process(G1YoungInitialMark collection) {
 *         aggregation().publish(GarbageCollectionTypes.G1GCYoungInitialMark, collection.getGCCause());
 *     }
 *
 *     private void process(G1FullGC collection) {
 *         aggregation().publish(GarbageCollectionTypes.FullGC, collection.getGCCause());
 *     }
 * }
 * </code></pre>
 * @param <A> The type of Aggregation
 */
public abstract class Aggregator<A extends Aggregation> {

    private final A aggregation;

    /// JVMEventDispatcher manages all of the registered events and event consumers
    private final JVMEventDispatcher jvmEventDispatcher = new JVMEventDispatcher();
    private volatile boolean done = false;

    /**
     * Subclass only.
     * @param aggregation The Aggregation that {@literal @}Collates this Aggregator
     * @see Collates
     * @see Aggregation
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
     * <pre>{@code
     *     register(G1Young.class, this::process);
     * }</pre>
     * The {@code Consumer} for this example would be coded as:
     * <pre>{@code
     *     private void process(G1Young collection) {...}
     * }</pre>
     * Where the body of the method would pull the relevant data from the event
     * and pass the data on to the Aggregation.
     * @param eventClass the Class of the JVMEvent type to register.
     * @param process the handler which processes the event
     * @param <E> the type of JVMEvent
     */
    protected <E extends JVMEvent> void register(Class<E> eventClass, Consumer<? super E> process) {
        jvmEventDispatcher.register(eventClass, process);
    }

    /**
     * This method consumes a JVMEvent and dispatches it to the
     * {@link #register(Class, Consumer) registered consumer}.
     * @param event an event to be processed
     * @param < E> the type of JVMEvent
     */
    //@Override
    public void receive(JVMEvent event) {
        jvmEventDispatcher.dispatch(event);
    }

    /**
     * The JVMTermination event signals the end of processing of a log file.
     * @param event JVMTermination is a sentinel for the end of log processing.
     */
    private void terminationHandler(JVMTermination event) {
        done = true;
    }

    /**
     * Whether all events have been processed by the Aggregator.
     * @return {@code true} if this Aggregator is done processing events.
     */
    public boolean isDone() {
        return done;
    }
}
