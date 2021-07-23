// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum;

import com.microsoft.censum.aggregator.Aggregation;
import com.microsoft.censum.io.DataSource;
import com.microsoft.censum.jvm.JavaVirtualMachine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The primary API for analyzing Java Garbage Collection (GC) logs.
 */
public class Censum {

    private static Logger LOGGER = Logger.getLogger(Censum.class.getName());

    private static JavaVirtualMachine loadJavaVirtualMachine() {
        try {
            // TODO: property for to allow override of default implementation.
            Class<?> clazz =
                    Class.forName("com.microsoft.censum.vertx.jvm.DefaultJavaVirtualMachine", true, Thread.currentThread().getContextClassLoader());
            Constructor<?> constructor = clazz.getConstructor();
            JavaVirtualMachine javaVirtualMachine = (JavaVirtualMachine) constructor.newInstance();
            return javaVirtualMachine;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                InstantiationException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Cannot load \"com.microsoft.censum.vertx.jvm.DefaultJavaVirtualMachine\"",e);
        }
        return null;
    }

    private static final Set<Class<? extends Aggregation>> aggregationsFromServiceLoader;

    static {
        aggregationsFromServiceLoader = new HashSet<>();
        ServiceLoader<Aggregation> serviceLoader = ServiceLoader.load(Aggregation.class);
        serviceLoader.stream()
                .map(ServiceLoader.Provider::get)
                .map(Aggregation::getClass)
                .forEach(aggregationsFromServiceLoader::add);
    }

    private final Set<Class<? extends Aggregation>> registeredAggregations;

    /**
     * Instantiate a Censum object. The same Censum object can be used to analyze
     * more than one GC log. It is not necessary to create a Censum object for
     * each GC log to be analyzed. Please note, however, that Censum API is not
     * thread safe.
     */
    public Censum() {
        // Allow for adding aggregations from source code,
        // but don't corrupt the ones loaded by the service loader
        this.registeredAggregations = new HashSet<>(aggregationsFromServiceLoader);
    }

    /**
     * Registers an {@code Aggregation} class which can be used to perform analysis
     * on {@code JVMEvent}s. Censum will instantiate the Aggregation when needed.
     * <p>
     * An alternative, and preferred, method of registering Aggregations is through
     * the java.util.ServiceLoader model. Censum will automatically load classes that
     * provide the {@link com.microsoft.censum.aggregator.Aggregation} API.
     * <p>
     * The {@link com.microsoft.censum.jvm.JavaVirtualMachine#getAggregation(Class)}
     * API will return an Aggregation that was used in the log analysis. Even though
     * an Aggregation was registered, the {@code getAggregation} method will return
     * null if the Aggregation was not used in the analysis.
     *
     * @param aggregationClass the Aggregation class to register.
     * @see com.microsoft.censum.aggregator.Aggregation
     * @see com.microsoft.censum.jvm.JavaVirtualMachine
     */
    public void registerAggregation(Class<? extends Aggregation> aggregationClass) {
        registeredAggregations.add(aggregationClass);
    }

    /**
     * Perform an analysis on a GC log file. The analysis will use the Aggregations
     * that were {@link #registerAggregation(Class) registered}, if appropriate for
     * the GC log file.
     * @param dataSource The log to analyze, typically a
     * {@link com.microsoft.censum.io.SingleGCLogFile} or
     * {@link com.microsoft.censum.io.RotatingGCLogFile}.
     * @return a representation of the state of the Java Virtual Machine resulting
     * from the analysis of the GC log file.
     */
    public JavaVirtualMachine analyze(DataSource<?> dataSource) {
        // Potential NPE, but would have logged if there was trouble creating the instance.
        JavaVirtualMachine javaVirtualMachine = loadJavaVirtualMachine();
        try {
            Method analyze = javaVirtualMachine.getClass().getMethod("analyze", Set.class, DataSource.class);
            analyze.invoke(javaVirtualMachine, this.registeredAggregations, dataSource);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, "Cannot invoke analyze method",e);
        }
        return javaVirtualMachine;
    }
}
