// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.vertx.GCToolkitVertx;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    private Diary diary;
    private DateTimeStamp timeOfLastEvent;
    private final Map<Class<? extends Aggregation>, Aggregation> aggregatedData = new ConcurrentHashMap<>();

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
        return diary.isParNew();
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

    /**
     * If the first event is significantly distant from zero in relation to the time intervals between the
     * of the next N events, where N maybe 1, then this is likely a log fragment and not the start of the run.
     *
     * Try to estimate the time at which the JVM started. For log fragments, this will be the time
     * of the first event in the log. Otherwise it will be 0.000 seconds.
     * @return DateTimeStamp
     */
    @Override
    public DateTimeStamp getEstimatedJVMStartTime() {
        DateTimeStamp startTime = getTimeOfFirstEvent();
        // Initial entries in GC log happen within seconds. Lets allow for 60 before considering the log
        // to be a fragment.
        if (startTime.getTimeStamp() < LOG_FRAGMENT_THRESHOLD_SECONDS) {
            return startTime.minus(startTime.getTimeStamp());
        } else {
            return startTime;
        }
    }

    /**
     * todo: fix this to be a globally available value. Also, the JVM start time is not zero if the time
     * of the first event is significantly away from zero in relation to the time intervals between the
     * of the next N events, where N maybe 1.
     *
     * try to estimate the time at which the JVM started. For log fragments, this will be the time
     * of the first event in the log. Otherwise it will be 0.000 seconds.
     * @return DateTimeStamp
     */
    @Override
    public DateTimeStamp getTimeOfFirstEvent() {
        return diary.getTimeOfFirstEvent();
    }

    /**
     * JVM termination time will be one of either, the time stamp in the termination event if present or, the
     * time of the last event + that events duration.
     * @return DateTimeStamp
     */
    @Override
    public DateTimeStamp getJVMTerminationTime() {
        return timeOfLastEvent;
    }

    @Override
    public double getRuntimeDuration() {
        return getJVMTerminationTime().minus(getEstimatedJVMStartTime());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Aggregation> Optional<T> getAggregation(Class<T> aggregationClass) {
        return Optional.ofNullable((T) aggregatedData.get(aggregationClass));
    }

    abstract GCToolkitVertxParameters getParameters(Set<Class<? extends Aggregation>> registeredAggregations, Diary diary);

    // Invoked reflectively from GCToolKit
    public void analyze(Set<Class<? extends Aggregation>> registeredAggregations, DataSource<?> dataSource) {

        try {
            final GCLogFile gcLogFile = (GCLogFile) dataSource;
            this.diary = gcLogFile.diary();

            GCToolkitVertxParameters GCToolkitVertxParameters = getParameters(registeredAggregations, gcLogFile.diary());

            this.timeOfLastEvent = GCToolkitVertx.aggregateDataSource(
                    dataSource,
                    GCToolkitVertxParameters.logFileParsers(),
                    GCToolkitVertxParameters.aggregatorVerticles(),
                    GCToolkitVertxParameters.mailBox()
            );

            GCToolkitVertxParameters.aggregatorVerticles().stream()
                    .flatMap(aggregatorVerticle -> aggregatorVerticle.aggregators().stream())
                    .forEach(aggregator -> {
                        Aggregation aggregation = aggregator.aggregation();
                        this.aggregatedData.put(aggregation.getClass(), aggregation);
                    });

        } catch (IOException | ClassCastException e ) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
