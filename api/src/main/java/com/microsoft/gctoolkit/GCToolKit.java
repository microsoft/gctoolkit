// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.RotatingGCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.DataSourceChannel;
import com.microsoft.gctoolkit.message.DataSourceParser;
import com.microsoft.gctoolkit.message.JVMEventChannel;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The primary API for analyzing Java Garbage Collection (GC) logs.
 */
public class GCToolKit {

    private static final Logger LOGGER = Logger.getLogger(GCToolKit.class.getName());

    /**
     * Load the first implementation of JavaVirtualMachine that can process
     * the supplied DataSource, GCLog in this instance.
     * @param logFile GCLogFile DataSource
     * @return JavaVirtualMachine implementation.
     */
    private JavaVirtualMachine loadJavaVirtualMachine(GCLogFile logFile) {
        return ServiceLoader.load(JavaVirtualMachine.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(jvm -> jvm.accepts(logFile))
                .findFirst()
                .orElseThrow(() -> new ServiceConfigurationError("Internal Error - No suitable JavaVirtualMachine implementation found"));
    }

    private DataSourceChannel loadDataSourceChannel() {
        return ServiceLoader.load(DataSourceChannel.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .findFirst()
                .orElseThrow(() -> new ServiceConfigurationError("No suitable DataSourceBus implementation found"));

    }

    private JVMEventChannel loadJVMEventChannel() {
        return ServiceLoader.load(JVMEventChannel.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .findFirst()
                .orElseThrow(() -> new ServiceConfigurationError("No suitable JVMEventBus implementation found"));

    }

    private void loadDataSourceParsers(final DataSourceChannel dataSourceChannel, final JVMEventChannel jvmEventChannel, Diary diary) throws IOException {
        List<DataSourceParser> dataSourceParsers = ServiceLoader.load(DataSourceParser.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(consumer->consumer.accepts(diary))
                .collect(Collectors.toList());
        for (DataSourceParser dataSourceParser : dataSourceParsers) {
            dataSourceChannel.registerListener(dataSourceParser);
            dataSourceParser.publishTo(jvmEventChannel);
        }
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
        //Useful for debugging
        if ( Level.FINER.equals(LOGGER.getLevel()))
            registeredAggregations.forEach(a -> LOGGER.log(Level.FINER, "Registered " + a.toString()));
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
    public JavaVirtualMachine analyze(DataSource<?> dataSource) throws IOException  {
        /*
        Assembly....b
         */
        GCLogFile logFile = (GCLogFile)dataSource;
        DataSourceChannel dataSourceChannel = loadDataSourceChannel();
        JVMEventChannel jvmEventChannel = loadJVMEventChannel();
        loadDataSourceParsers(dataSourceChannel, jvmEventChannel, logFile.diary());
        loadAggregationsFromServiceLoader();
        JavaVirtualMachine javaVirtualMachine = null;
        try {
            javaVirtualMachine = loadJavaVirtualMachine(logFile);
            javaVirtualMachine.analyze(this.registeredAggregations, jvmEventChannel, dataSourceChannel, logFile);
        } catch(Throwable t) {
            LOGGER.log(Level.SEVERE, "Internal Error: Cannot invoke analyze method", t);
        }
        return javaVirtualMachine;
    }

}
