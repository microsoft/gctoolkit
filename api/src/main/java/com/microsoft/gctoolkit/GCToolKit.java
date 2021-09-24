// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.io.RotatingGCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The primary API for analyzing Java Garbage Collection (GC) logs.
 */
public class GCToolKit {

    private static final Logger LOGGER = Logger.getLogger(GCToolKit.class.getName());

    private static JavaVirtualMachine loadJavaVirtualMachine() {
        try {
            // TODO: property for to allow override of default implementation.
            Class<?> clazz =
                    Class.forName("com.microsoft.gctoolkit.vertx.jvm.DefaultJavaVirtualMachine", true, Thread.currentThread()
                            .getContextClassLoader());
            Constructor<?> constructor = clazz.getConstructor();
            JavaVirtualMachine javaVirtualMachine = (JavaVirtualMachine) constructor.newInstance();
            return javaVirtualMachine;
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Cannot load \"com.microsoft.gctoolkit.vertx.jvm.DefaultJavaVirtualMachine\"", e);
        }
        return null;
    }

    private final Set<Class<? extends Aggregation>> registeredAggregations;

    /**
     * Instantiate a GCToolKit object. The same GCToolKit object can be used to analyze
     * more than one GC log. It is not necessary to create a GCToolKit object for
     * each GC log to be analyzed. Please note, however, that GCToolKit API is not
     * thread safe.
     */
    public GCToolKit() {
        // Allow for adding aggregations from source code,
        // but don't corrupt the ones loaded by the service loader
        this.registeredAggregations = new HashSet<>();
    }

    /**
     * Loads all Aggregations defined in the application module through
     * the java.util.ServiceLoader model. To register a class that
     * provides the {@link Aggregation} API, define the following
     * in {@code module-info.java}:
     * <pre>
     * import com.microsoft.gctoolkit.aggregator.Aggregation;
     * import com.microsoft.gctoolkit.sample.aggregation.HeapOccupancyAfterCollectionSummary;
     * 
     * module com.microsoft.gctoolkit.sample {
     *     ...
     *     provides Aggregation with HeapOccupancyAfterCollectionSummary;
     * }
     * </pre>
     */
    public void loadAggregationsFromServiceLoader() {
        ServiceLoader.load(Aggregation.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .map(Aggregation::getClass)
                .forEach(registeredAggregations::add);
    }

    /**
     * Registers an {@code Aggregation} class which can be used to perform analysis
     * on {@code JVMEvent}s. GCToolKit will instantiate the Aggregation when needed.
     * <p>
     * The {@link JavaVirtualMachine#getAggregation(Class)}
     * API will return an Aggregation that was used in the log analysis. Even though
     * an Aggregation was registered, the {@code getAggregation} method will return
     * null if the Aggregation was not used in the analysis.
     *
     * @param aggregationClass the Aggregation class to register.
     * @see Aggregation
     * @see JavaVirtualMachine
     */
    public void registerAggregation(Class<? extends Aggregation> aggregationClass) {
        registeredAggregations.add(aggregationClass);
    }

    /**
     * Perform an analysis on a GC log file. The analysis will use the Aggregations
     * that were {@link #registerAggregation(Class) registered}, if appropriate for
     * the GC log file.
     *
     * @param dataSource The log to analyze, typically a
     *                   {@link SingleGCLogFile} or
     *                   {@link RotatingGCLogFile}.
     * @return a representation of the state of the Java Virtual Machine resulting
     * from the analysis of the GC log file.
     */
    public JavaVirtualMachine analyze(DataSource<?> dataSource) {
        // Potential NPE, but would have logged if there was trouble creating the instance.
        JavaVirtualMachine javaVirtualMachine = loadJavaVirtualMachine();
        try {
            Method analyze = javaVirtualMachine.getClass()
                    .getMethod("analyze", Set.class, DataSource.class);
            analyze.invoke(javaVirtualMachine, this.registeredAggregations, dataSource);
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Cannot invoke analyze method", e);
        }
        return javaVirtualMachine;
    }
}
