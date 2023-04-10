// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.jvm;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.DataSourceChannel;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelAggregator;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base implementation of JavaVirtualMachine that uses the message API to feed
 * lines to the parser(s) and post events to the aggregators.
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

    /**
     * Sets the data source
     * @param logFile is the source of GC logging data
     * @throws IOException if there is any issues reading from the data source.
     */
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
     * If the first event is significantly distant from zero in relation to the time intervals between the
     * of the next N events, where N maybe 1, then this is likely a log fragment and not the start of the run.
     * <p>
     * Try to estimate the time at which the JVM started. For log fragments, this will be the time
     * of the first event in the log. Otherwise it will be 0.000 seconds.
     *
     * @return DateTimeStamp as estimated start time.
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

    /**
     * Sets the estimated start time as calculated by the Aggregation class
     * @param estimatedStartTime as calculated from observations of the event timing in the gc log.
     */
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

    /**
     * Orchestrate the analysis of a GC log. Step wise
     * 1. find the aggregators that aggregate events generated by the gc log
     * 2. Register the aggregators with the message bus. Setup a callback so the message framework.
     * 3. Stream the data to a publisher
     * 4. Wait until all the aggregators have completed
     * 5. Set the start and end times
     * 6. Return to the caller
     * @param registeredAggregators all of the aggregations loaded by the module SPI
     * @param eventBus the bus to publish events on
     * @param dataSourceBus the bus that raw log lines are published on
     */
    @Override
    public void analyze(List<Aggregator<? extends Aggregation>> registeredAggregators, JVMEventChannel eventBus, DataSourceChannel dataSourceBus) {
        Phaser finishLine = new Phaser();
        Set<EventSource> generatedEvents = diary.generatesEvents();
        for (Aggregator aggregator : registeredAggregators) {
            Aggregation aggregation = aggregator.aggregation();
            aggregatedData.put(aggregation.getClass(), aggregation);
            generatedEvents.stream().filter(aggregator::aggregates).forEach(eventSource -> {
                GCToolKit.LOG_DEBUG_MESSAGE(() -> "Registering " + aggregator.getClass().getName() + " with " + eventSource.toChannel());
                finishLine.register();
                aggregator.onCompletion(finishLine::arriveAndDeregister);
                JVMEventChannelAggregator eventChannelAggregator = new JVMEventChannelAggregator(eventSource.toChannel(), aggregator);
                eventBus.registerListener(eventChannelAggregator);
            });
        }

        try {
            if (finishLine.getRegisteredParties() > 0) {
                dataSource.stream().forEach(message -> dataSourceBus.publish(ChannelName.DATA_SOURCE, message));
                finishLine.awaitAdvance(0);
            } else {
                LOGGER.log(Level.INFO, "No Aggregations have been registered, DataSource will not be analysed.");
                LOGGER.log(Level.INFO, "Is there a module containing Aggregation classes on the module-path");
                LOGGER.log(Level.INFO, "Is GCToolKit::loadAggregationsFromServiceLoader() or GCToolKit::loadAggregation(Aggregation) being invoked?");
            }

            // Fill in termination info.
            Optional<Aggregation> aggregation = aggregatedData.values().stream().findFirst();
            aggregation.ifPresent(terminationRecord -> {
                setJVMTerminationTime(terminationRecord.timeOfTerminationEvent());
                setRuntimeDuration(terminationRecord.estimatedRuntime());
                setEstimatedJVMStartTime(terminationRecord.estimatedStartTime());
            });
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
        } finally {
            dataSourceBus.close();
            eventBus.close();
        }
    }
}
