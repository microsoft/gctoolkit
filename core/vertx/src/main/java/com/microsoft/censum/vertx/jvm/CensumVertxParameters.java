// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.vertx.jvm;

import com.microsoft.censum.aggregator.Aggregates;
import com.microsoft.censum.aggregator.Aggregation;
import com.microsoft.censum.aggregator.Aggregator;
import com.microsoft.censum.aggregator.Collates;
import com.microsoft.censum.aggregator.EventSource;
import com.microsoft.censum.vertx.aggregator.AggregatorVerticle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/* package-scope */ abstract class CensumVertxParameters {

    private static final Logger LOGGER = Logger.getLogger(CensumVertxParameters.class.getName());

    abstract Set<LogFileParser> logFileParsers();
    abstract Set<AggregatorVerticle> aggregatorVerticles();
    abstract String mailBox();

    // routine to find if this Aggregator Aggregates eventSource.
    @SuppressWarnings("unchecked")
    private static boolean aggregatorClassAggregates(Class<? extends Aggregator<?>> clazz, EventSource eventSource) {

        if (clazz == null) return false;

        boolean found = false;
        do {
            if (clazz.isAnnotationPresent(Aggregates.class)) {
                Aggregates aggregates = clazz.getAnnotation(Aggregates.class);
                EventSource[] aggregatesValue = aggregates.value();
                for (EventSource value : aggregatesValue) {
                    if (found = (value == eventSource)) break;
                }
            }
            //clazz = (Class<Aggregator<?>>) clazz.getSuperclass();
        } while (!found && (clazz = (Class<Aggregator<?>>) clazz.getSuperclass()) != null);

        return found;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Aggregator<?>> getAggregatorClassFromCollatesAnnotation(Class<? extends Aggregation> clazz) {

        if (clazz == null) return null;

        Class<? extends Aggregator<?>> aggregatorClass = null;
        do {
            if (clazz.isAnnotationPresent(Collates.class)) {
                Collates collates = clazz.getAnnotation(Collates.class);
                aggregatorClass = collates.value();
            }
        } while (aggregatorClass == null && (clazz = (Class<Aggregation>)clazz.getSuperclass()) != null);

        return aggregatorClass;
    }

    private static Aggregator<?> createAggregator(
            Class<? extends Aggregator<?>> aggregatorClass,
            Class<? extends Aggregation> aggregationClass) {
        try {
            Constructor[] constructors = aggregationClass.getConstructors();
            //Constructor<? extends Aggregator<?>> ctor = aggregatorClass.getConstructor(Aggregation.class);
            constructors[0].newInstance(aggregationClass);
            //return ctor.newInstance(aggregationClass);
//        } catch (NoSuchMethodException | SecurityException e) {
//            LOGGER.log(Level.WARNING, aggregatorClass +
//                    " must have a public constructor that takes a single, Class<? extends Aggregation> parameter");
        } catch (InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            LOGGER.log(Level.WARNING, "Cannot construct instance of " + aggregatorClass + ": " + e);
        }
        return null;
    }

    Set<Aggregator<?>> getAggregators (
            EventSource eventSource,
            Set<Class<? extends Aggregation>> registeredAggregations)
    {
        Set<Aggregator<?>> aggregators = new HashSet<>();

        registeredAggregations.forEach(aggregationClass -> {
            Class<? extends Aggregator<?>> aggregatorClass = getAggregatorClassFromCollatesAnnotation(aggregationClass);
            if (aggregatorClassAggregates(aggregatorClass, eventSource)) {
                Aggregator<?> aggregator = createAggregator(aggregatorClass, aggregationClass);
                if (aggregator != null) aggregators.add(aggregator);
            }
        });
        return aggregators;
    }

}
