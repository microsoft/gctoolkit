// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.RotatingGCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.message.DataSourceChannel;
import com.microsoft.gctoolkit.message.DataSourceParser;
import com.microsoft.gctoolkit.message.JVMEventChannel;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.lang.Class.forName;

/**
 * The primary API for analyzing Java Garbage Collection (GC) logs.
 */
public class GCToolKit {

    private static final Logger LOGGER = Logger.getLogger(GCToolKit.class.getName());

    private static final String GCTOOLKIT_DEBUG = System.getProperty("gctoolkit.debug");
    private static final boolean DEBUGGING = GCTOOLKIT_DEBUG != null;

    // returns true if gctoolkit.debug is set to "all" or contains "className", but does not contain "-className"
    private static boolean isDebugging(String className) {
        return DEBUGGING
                && (GCTOOLKIT_DEBUG.isEmpty()
                || ((GCTOOLKIT_DEBUG.contains("all") || GCTOOLKIT_DEBUG.contains(className))
                && !GCTOOLKIT_DEBUG.contains("-" + className)));
    }

    /**
     * Print a debug message to System.out if gctoolkit.debug is empty, is set to "all",
     * or contains "className" but does not contain "-className".
     * For example, to enable debug logging for all classes except UnifiedG1GCParser:
     * <code>-Dgctoolkit.debug=all,-com.microsoft.gctoolkit.parser.UnifiedG1GCParser</code>
     *
     * @param message Supplies the message to log. If null, nothing will be logged.
     */
    public static void LOG_DEBUG_MESSAGE(Supplier<String> message) {
        if (DEBUGGING && message != null) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String methodName = stackTrace[2].getMethodName();
            String className = stackTrace[2].getClassName();
            String fileName = stackTrace[2].getFileName();
            int lineNumber = stackTrace[2].getLineNumber();
            if (isDebugging(className)) {
                System.out.println(String.format("DEBUG: %s.%s(%s:%d): %s", className, methodName, fileName, lineNumber, message.get()));
            }
        }
    }

    private final HashSet<DataSourceParser> registeredDataSourceParsers = new HashSet<>();
    private List<Aggregation> registeredAggregations;
    private JVMEventChannel jvmEventChannel = null;
    private DataSourceChannel dataSourceChannel = null;

    /**
     * Instantiate a GCToolKit object. The same GCToolKit object can be used to analyze
     * more than one GC log. It is not necessary to create a GCToolKit object for
     * each GC log to be analyzed. Please note, however, that GCToolKit API is not
     * thread safe.
     */
    public GCToolKit() {
        // Allow for adding aggregations from source code,
        // but don't corrupt the ones loaded by the service loader
        this.registeredAggregations = new ArrayList<>();
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
                .forEach(aggregation -> {
                    registeredAggregations.add(aggregation);
                    LOG_DEBUG_MESSAGE(() -> "ServiceLoader provided: " + aggregation.getClass().getName());
                });
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
     * @param aggregation the Aggregation class to register.
     * @see Aggregation
     * @see JavaVirtualMachine
     */
    public void loadAggregation(Aggregation aggregation) {
        registeredAggregations.add(aggregation);
    }

    /**
     * Load the first implementation of JavaVirtualMachine that can process
     * the supplied DataSource, GCLog in this instance.
     * @param logFile GCLogFile DataSource
     * @return JavaVirtualMachine implementation.
     */
    private JavaVirtualMachine loadJavaVirtualMachine(GCLogFile logFile) {
        return logFile.getJavaVirtualMachine();
    }

    public void loadDataSourceChannel(DataSourceChannel channel) {
        if (dataSourceChannel == null)
            this.dataSourceChannel = channel;
    }

    private void loadDataSourceChannel() {
        if ( dataSourceChannel == null) {
            ServiceLoader<DataSourceChannel> serviceLoader = ServiceLoader.load(DataSourceChannel.class);
            if (serviceLoader.findFirst().isPresent()) {
                loadDataSourceChannel(serviceLoader
                        .stream()
                        .map(ServiceLoader.Provider::get)
                        .findFirst()
                        .orElseThrow(() -> new ServiceConfigurationError("Internal Error - No suitable DataSourceBus implementation found")));
            } else {
                try {
                    Class clazz = forName("com.microsoft.gctoolkit.vertx.VertxDataSourceChannel", true, Thread.currentThread().getContextClassLoader());
                    loadDataSourceChannel((DataSourceChannel) clazz.getConstructors()[0].newInstance());
                } catch (Exception e) {
                    throw new ServiceConfigurationError("Unable to find a suitable DataSourceChannel provider");
                }
            }
        }
    }

    public void loadJVMEventChannel(JVMEventChannel channel) {
        if (jvmEventChannel == null)
            this.jvmEventChannel = channel;
    }

    private void loadJVMEventChannel() {
        if ( jvmEventChannel == null) {
            ServiceLoader<JVMEventChannel> serviceLoader = ServiceLoader.load(JVMEventChannel.class);
            if (serviceLoader.findFirst().isPresent()) {
                loadJVMEventChannel(ServiceLoader.load(JVMEventChannel.class)
                        .stream()
                        .map(ServiceLoader.Provider::get)
                        .findFirst()
                        .orElseThrow(() -> new ServiceConfigurationError("Internal Error - No suitable JVMEventBus implementation found")));
            } else {
                try {
                    Class clazz = forName("com.microsoft.gctoolkit.vertx.VertxJVMEventChannel", true, Thread.currentThread().getContextClassLoader());
                    loadJVMEventChannel((JVMEventChannel) clazz.getConstructors()[0].newInstance());
                } catch (Exception e) {
                    throw new ServiceConfigurationError("Unable to find a suitable provider to create a JVMEventChannel");
                }
            }
        }
    }

    /**
     * This method allows full control over which DataSourceParsers are used to parse the DataSource.
     * This method should be called before the {@link #analyze(DataSource)} method.
     * DataSourceParsers loaded by this method are used in place of those that are ordinarily loaded via
     * the service provider interface.
     * Use the {@link #addDataSourceParser(DataSourceParser)} method to load a DataSourceParser in addition
     * to those loaded by the service provider interface.
     * @param dataSourceParser An implementation of DataSourceParser that will be used to parse the DataSource.
     */
    public void loadDataSourceParser(DataSourceParser dataSourceParser) {
        registeredDataSourceParsers.add(dataSourceParser);
    }

    private List<DataSourceParser> additiveParsers = new ArrayList<>();

    /**
     * Add a DataSourceParser to be used to parse a DataSource. The DataSourceParser will be used in addition
     * to those loaded by the service provider interface. This method should be called before the
     * {@link #analyze(DataSource)} method.
     * @param dataSourceParser An implementation of DataSourceParser that will be used to parse the DataSource.
     */
    public void addDataSourceParser(DataSourceParser dataSourceParser) {
        additiveParsers.add(dataSourceParser);
    }

    private Set<EventSource> loadDataSourceParsers(Diary diary) {

        loadDataSourceChannel();
        loadJVMEventChannel();
        List<DataSourceParser> dataSourceParsers;
        if (registeredDataSourceParsers.isEmpty()) {
            dataSourceParsers = ServiceLoader.load(DataSourceParser.class)
                    .stream()
                    .map(ServiceLoader.Provider::get)
                    .filter(dataSourceParser -> dataSourceParser.accepts(diary))
                    .collect(Collectors.toList());
        } else{
            dataSourceParsers = new ArrayList<>();
            dataSourceParsers.addAll(registeredDataSourceParsers);
        }


        if (dataSourceParsers.isEmpty()) {
            String[] parsers = {
                    "com.microsoft.gctoolkit.parser.CMSTenuredPoolParser",
                    "com.microsoft.gctoolkit.parser.GenerationalHeapParser",
                    "com.microsoft.gctoolkit.parser.JVMEventParser",
                    "com.microsoft.gctoolkit.parser.PreUnifiedG1GCParser",
                    "com.microsoft.gctoolkit.parser.ShenandoahParser",
                    "com.microsoft.gctoolkit.parser.SurvivorMemoryPoolParser",
                    "com.microsoft.gctoolkit.parser.UnifiedG1GCParser",
                    "com.microsoft.gctoolkit.parser.UnifiedGenerationalParser",
                    "com.microsoft.gctoolkit.parser.UnifiedJVMEventParser",
                    "com.microsoft.gctoolkit.parser.UnifiedSurvivorMemoryPoolParser",
                    "com.microsoft.gctoolkit.parser.ZGCParser"
            };
            dataSourceParsers = Arrays.stream(parsers)
                    .map(parserName -> {
                        try {
                            Class<?> clazz = forName(parserName, true, Thread.currentThread().getContextClassLoader());
                            return Optional.of(clazz.getConstructors()[0].newInstance());
                        } catch (ClassNotFoundException
                                | InstantiationException
                                | IllegalAccessException
                                | InvocationTargetException e) {
                            return Optional.empty();
                        }
                    })
                    .filter(Optional::isPresent)
                    .map(optional -> (DataSourceParser) optional.get())
                    .filter(dataSourceParser -> dataSourceParser.accepts(diary))
                    .collect(Collectors.toList());

        }

        //  add in any additional parsers not provided by the module SPI.
        dataSourceParsers.addAll(additiveParsers);

        if (dataSourceParsers.isEmpty()) {
            throw new ServiceConfigurationError("Unable to find a suitable provider to create a DataSourceParser");
        }

        for (DataSourceParser dataSourceParser : dataSourceParsers) {
            LOG_DEBUG_MESSAGE(() -> "Registering " + dataSourceParser.getClass().getName() + " with " + dataSourceChannel.getClass().getName());
            dataSourceParser.diary(diary);
            dataSourceChannel.registerListener(dataSourceParser);
            dataSourceParser.publishTo(jvmEventChannel);
        }

        return dataSourceParsers.stream()
                .map(DataSourceParser::eventsProduced)
                .collect(HashSet::new, Set::addAll, Set::addAll);
    }

    /**
     * Perform an analysis on a GC log file. The analysis will use the Aggregations
     * that were {@link #loadAggregation(Aggregation) registered}, if appropriate for
     * the GC log file.
     *
     * @param dataSource The log to analyze, typically a
     *                   {@link SingleGCLogFile} or
     *                   {@link RotatingGCLogFile}.
     * @return a representation of the state of the Java Virtual Machine resulting
     * from the analysis of the GC log file.
     * @throws IOException when something goes wrong reading the data source
     */
    public JavaVirtualMachine analyze(DataSource<?> dataSource) throws IOException  {
        GCLogFile logFile = (GCLogFile)dataSource;
        Set<EventSource> events = loadDataSourceParsers(logFile.diary());
        JavaVirtualMachine javaVirtualMachine = loadJavaVirtualMachine(logFile);
        try {
            List<Aggregator<? extends Aggregation>> filteredAggregators = filterAggregations(events);
            javaVirtualMachine.analyze(filteredAggregators, jvmEventChannel, dataSourceChannel);
        } catch(Throwable t) {
            LOGGER.log(Level.SEVERE, "Internal Error: Cannot invoke analyze method", t);
        }
        return javaVirtualMachine;
    }

    private List<Aggregator<? extends Aggregation>> filterAggregations(Set<EventSource> events) {
        List<Aggregator<? extends Aggregation>> aggregators = new ArrayList<>();
        for (Aggregation aggregation : registeredAggregations) {
            LOG_DEBUG_MESSAGE(() -> "Evaluating: " + aggregation.getClass().getName());
            Constructor<? extends Aggregator<?>> constructor = constructor(aggregation);
            if (constructor == null) {
                LOGGER.log(Level.WARNING, "Cannot find one of: default constructor or @Collates annotation for " + aggregation.getClass().getName());
                continue;
            }

            Aggregator<? extends Aggregation> aggregator = null;
            try {
                aggregator = constructor.newInstance(aggregation);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                continue;
            }
            if (events.stream().anyMatch(aggregator::aggregates)) {
                LOG_DEBUG_MESSAGE(() -> "Including : " + aggregation.getClass().getName());
                aggregators.add(aggregator);
            } else {
                LOG_DEBUG_MESSAGE(() -> "Excluding : " + aggregation.getClass().getName());
            }
        }
        return aggregators;

    }

    @SuppressWarnings("unchecked")
    private Constructor<? extends Aggregator<?>> constructor(Aggregation aggregation) {
        Class<? extends Aggregator<?>> targetClazz = aggregation.collates();
        if ( targetClazz != null) {
            Constructor<?>[] constructors = targetClazz.getConstructors();
            for (Constructor<?> constructor : constructors) {
                Parameter[] parameters = constructor.getParameters();
                if (parameters.length == 1 && Aggregation.class.isAssignableFrom(parameters[0].getType()))
                    return (Constructor<? extends Aggregator<?>>) constructor;
            }
        }
        return null;
    }

}
