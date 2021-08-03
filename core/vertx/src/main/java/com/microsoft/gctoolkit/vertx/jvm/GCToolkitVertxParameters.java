// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.vertx.aggregator.AggregatorVerticle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/* package-scope */ abstract class GCToolkitVertxParameters {

    private static final Logger LOGGER = Logger.getLogger(GCToolkitVertxParameters.class.getName());

    abstract Set<LogFileParser> logFileParsers();
    abstract Set<AggregatorVerticle> aggregatorVerticles();
    abstract String mailBox();

    // routine to find what this Aggregator Aggregates.
    @SuppressWarnings("unchecked")
    private static void aggregatorAggregates(Class<?> clazz, Set<EventSource> eventSources) {

        if (clazz == null || clazz == Aggregator.class) {
            return;
        }

        if (clazz.isAnnotationPresent(Aggregates.class)) {
            Aggregates aggregates = clazz.getAnnotation(Aggregates.class);
            if (aggregates != null) {
                Collections.addAll(eventSources, aggregates.value());
            }
        }

        aggregatorAggregates((Class<?>) clazz.getSuperclass(), eventSources);

        Class<?>[] interfaces = clazz.getInterfaces();
        for(Class<?> iface : interfaces) {
            aggregatorAggregates(iface, eventSources);
        }

    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Aggregator<?>> getAggregatorClassFromCollatesAnnotation(Class<?> clazz) {

        if (clazz == null) {
            return null;
        } else if (clazz.isAnnotationPresent(Collates.class)) {
            Collates collates = clazz.getAnnotation(Collates.class);
            return collates.value();
        } else {
            Class<? extends Aggregator<?>> aggregatorClass = getAggregatorClassFromCollatesAnnotation(clazz.getSuperclass());

            if (aggregatorClass == null) {
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> iface : interfaces) {
                    aggregatorClass = getAggregatorClassFromCollatesAnnotation(iface);
                    if (aggregatorClass != null) break;
                }
            }

            return aggregatorClass;
        }
    }

    private static Aggregator<?> createAggregator(
            Class<? extends Aggregator<?>> aggregatorClass,
            Class<? extends Aggregation> aggregationClass) {
        try {
            Constructor<?>[] constructors = aggregatorClass.getConstructors();
            if (constructors.length == 0) {
                LOGGER.log(Level.WARNING, aggregatorClass + " must have a public constructor which takes a " +  Aggregation.class);
                return null;
            }
            return (Aggregator<?>) aggregatorClass.getConstructors()[0].newInstance(aggregationClass.getConstructors()[0].newInstance());
        } catch (InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            LOGGER.log(Level.WARNING, e + ": Cannot construct instance of " + aggregatorClass);
        }
        return null;
    }

    Set<Aggregator<?>> getAggregators (
            EventSource eventSource,
            Set<Class<? extends Aggregation>> registeredAggregations)
    {
        final Set<Aggregator<?>> aggregators = new HashSet<>();
        final Set<EventSource> eventSources = new HashSet<>();

        registeredAggregations.forEach(aggregationClass -> {
            Class<? extends Aggregator<?>> aggregatorClass = getAggregatorClassFromCollatesAnnotation(aggregationClass);
            eventSources.clear(); // reusing the Set...
            aggregatorAggregates(aggregatorClass, eventSources);
            if (eventSources.contains(eventSource)) {
                Aggregator<?> aggregator = createAggregator(aggregatorClass, aggregationClass);
                if (aggregator != null) aggregators.add(aggregator);
            }
        });
        return aggregators;
    }

}
