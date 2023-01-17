// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.DataSourceChannel;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelAggregator;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Phaser;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of JavaVirtualMachine that uses io.vertx verticles to feed
 * lines to the parser(s) and post events to the aggregators. This implementation
 * is here in the vertx module so that the api and parser modules can exist without
 * having to import io.vertx. In the api module, the class GCToolKit uses the classloader
 * to load the appropriate JavaVirtualMachine.
 */
public abstract class AbstractJavaVirtualMachine implements JavaVirtualMachine {

    private static final Logger LOGGER = Logger.getLogger(AbstractJavaVirtualMachine.class.getName());
    private static final double LOG_FRAGMENT_THRESHOLD_SECONDS = 60.0d; //todo: replace magic threshold with a heuristic

    private GCLogFile dataSource;
    private Diary diary;
    private DateTimeStamp estimatedStartTime;
    private DateTimeStamp timeOfLastEvent;
    private double logDuration = -1.0d;
    private final Map<Class<? extends Aggregation>, Aggregation> aggregatedData = new ConcurrentHashMap<>();

    public void setDataSource(DataSource logFile) throws IOException {
        this.dataSource = (GCLogFile) logFile;
        this.diary = logFile.diary();
    }

    @Override
    public boolean isG1GC() {
        return diary.isG1GC();
    }

    @Override
    public boolean isZGC() {
        return diary.isZGC();
    }

    @Override
    public boolean isShenandoah() {
        return diary.isShenandoah();
    }

    @Override
    public boolean isParallel() {
        return diary.isPSYoung();
    }

    @Override
    public boolean isSerial() {
        return diary.isSerialFull();
    }

    @Override
    public boolean isCMS() {
        return diary.isCMS();
    }

    @Override
    public String getCommandLine() {
        return ""; //todo: extract from diary... jvmConfigurationFromParser.getCommandLine();
    }

    public DateTimeStamp getTimeOfFirstEvent() {
        return diary.getTimeOfFirstEvent();
    }

    /**
     * todo: fix this to be a globally available value. Also, the JVM start time is not zero if the time
     * If the first event is significantly distant from zero in relation to the time intervals between the
     * of the next N events, where N maybe 1, then this is likely a log fragment and not the start of the run.
     * <p>
     * Try to estimate the time at which the JVM started. For log fragments, this will be the time
     * of the first event in the log. Otherwise it will be 0.000 seconds.
     *
     * @return DateTimeStamp
     */
    @Override
    public DateTimeStamp getEstimatedJVMStartTime() {
        DateTimeStamp startTime = diary.getTimeOfFirstEvent();
        // Initial entries in GC log happen within seconds. Lets allow for 60 before considering the log
        // to be a fragment.
        if (startTime.getTimeStamp() < LOG_FRAGMENT_THRESHOLD_SECONDS) {
            return startTime.minus(startTime.getTimeStamp());
        } else {
            return startTime;
        }
    }

    public void setEstimatedJVMStartTime(DateTimeStamp estimatedStartTime) {
        this.estimatedStartTime = estimatedStartTime;
    }

    /**
     * JVM termination time will be one of either, the time stamp in the termination event if present or, the
     * time of the last event + that events duration.
     *
     * @return DateTimeStamp
     */
    @Override
    public DateTimeStamp getJVMTerminationTime() {
        return timeOfLastEvent;
    }

    private void setJVMTerminationTime(DateTimeStamp terminationTime) {
        timeOfLastEvent = terminationTime;
    }

    @Override
    public double getRuntimeDuration() {
        return logDuration;
    }

    private void setRuntimeDuration(double duration) {
        this.logDuration = duration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Aggregation> Optional<T> getAggregation(Class<T> aggregationClass) {
        return Optional.ofNullable((T) aggregatedData.get(aggregationClass));
    }

    @SuppressWarnings("unchecked")
    private Constructor<? extends Aggregator<?>> constructor(Aggregation aggregation) {
        Class<? extends Aggregator<?>> targetClazz = aggregation.collates();
        Constructor<?>[] constructors = targetClazz.getConstructors();
        for ( Constructor<?> constructor : constructors) {
            Parameter[] parameters = constructor.getParameters();
            if ( parameters.length == 1 && Aggregation.class.isAssignableFrom(parameters[0].getType()))
                return (Constructor<? extends Aggregator<?>>)constructor;
        }
        return null;
    }

    @Override
    public void analyze(List<Aggregation> registeredAggregations, JVMEventChannel eventBus, DataSourceChannel dataSourceBus) {
        Phaser finishLine = new Phaser();
        try {
            Set<EventSource> generatedEvents = diary.generatesEvents();
            for (Aggregation aggregation : registeredAggregations) {
                Constructor<? extends Aggregator<?>> constructor = constructor(aggregation);
                if ( constructor == null) continue;
                Aggregator<? extends Aggregation> aggregator = constructor.newInstance(aggregation);
                aggregatedData.put(aggregation.getClass(), aggregation);
                Optional<EventSource> source = generatedEvents.stream().filter(aggregator::aggregates).findFirst();
                if (source.isPresent()) {
                    finishLine.register();
                    aggregator.onCompletion(finishLine::arriveAndDeregister);
                    JVMEventChannelAggregator eventChannelAggregator = new JVMEventChannelAggregator(source.get().toChannel(), aggregator);
                    eventBus.registerListener(eventChannelAggregator);
                }
            }
            dataSource.stream().forEach(message -> dataSourceBus.publish(Channels.DATA_SOURCE, message));
            finishLine.awaitAdvance(0);
            dataSourceBus.close();
            eventBus.close();

            // Fill in termination info.
            Optional<Aggregation> aggregation = aggregatedData.values().stream().findFirst();
            aggregation.ifPresent(terminationRecord -> {
                setJVMTerminationTime(terminationRecord.timeOfTerminationEvent());
                setRuntimeDuration(terminationRecord.estimatedRuntime());
                setEstimatedJVMStartTime(terminationRecord.estimatedStartTime());
            });
        } catch (IOException | ClassCastException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
