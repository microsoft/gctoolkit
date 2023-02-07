// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.aggregator;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * This is a utility class that supports the {@link Aggregator#register(Class, Consumer)} method.
 */
public class JVMEventDispatcher {

    private final Map<Class<? extends JVMEvent>, Consumer<? super JVMEvent>> eventConsumers = new ConcurrentHashMap<>();

    private final Consumer<? super JVMEvent> nopConsumer = (evt) -> {};

    @SuppressWarnings("unchecked")
    private <R extends JVMEvent> Consumer<? super JVMEvent> getConsumerForClass(Class<R> eventClass) {
        Class<? extends JVMEvent> clazz = eventClass;

        //Fast path that should hit after the event has been seen for the first time
        Consumer<? super JVMEvent> eventConsumer = eventConsumers.get(clazz);
        if (eventConsumer != null) {
            return eventConsumer;
        }

        do {
            eventConsumer = eventConsumers.get(clazz);

            if (eventConsumer != null) {

                if (eventClass != clazz) {
                    //optimisation to avoid walking up the class tree every time, if we get a hit on
                    //R maps to eventConsumer, just put it in the map and it will fetch first time in the future
                    eventConsumers.put(eventClass, eventConsumer);
                }

                //visit the most specific ONLY
                return eventConsumer;
            }

            if (clazz == JVMEvent.class) {
                // Hit the top of the hierarchy
                break;
            } else {
                // Unfortunate cast but assuming register has done its job it is impossible for this cast to fail
                clazz = (Class<? extends JVMEvent>) clazz.getSuperclass(); // unchecked cast
            }
        } while (clazz != null);

        //no handler for eventClass so lets put in a stub so we dont have to figure this out on every event
        eventConsumers.put(eventClass, nopConsumer);
        return nopConsumer;
    }

    /**
     * Called from {@link Aggregator#register(Class, Consumer)}
     * @param eventClass A JVMEvent class that the Aggregator captures
     * @param process A method to call back when an event of type {@code eventClass} is captured.
     * @param <R> A type of JVMEvent
     */
    @SuppressWarnings("unchecked")
    public <R extends JVMEvent> void register(Class<R> eventClass, Consumer<? super R> process) {
        eventConsumers.put(eventClass, (Consumer<JVMEvent>)process);
    }

    /**
     * todo: fix comment for the link below.
     * Called from {@link Aggregator#receive(JVMEvent)}, this invokes the process method that was
     * {@link #register(Class, Consumer) registered}.
     * @param event An event from the parser.
     * @param <R> the type of JVMEvent.
     */
    public <R extends JVMEvent> void dispatch(R event) {
        getConsumerForClass(event.getClass()).accept(event);
    }

}
