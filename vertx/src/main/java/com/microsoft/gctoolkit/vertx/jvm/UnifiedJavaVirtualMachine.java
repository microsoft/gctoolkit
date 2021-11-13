// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.jvm.JvmConfiguration;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.vertx.GCToolkitVertx;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
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
 * to load UnifiedJavaVirtualMachine.
 */
public class UnifiedJavaVirtualMachine extends AbstractJavaVirtualMachine implements JavaVirtualMachine {

    private static final Logger LOGGER = Logger.getLogger(UnifiedJavaVirtualMachine.class.getName());

    private static final double LOG_FRAGMENT_THRESHOLD = 18000;
    private Diary diary;
    private JvmConfiguration jvmConfigurationForCoreApi;
    private DateTimeStamp timeOfLastEvent;
    private final Map<Class<? extends Aggregation>, Aggregation> aggregatedData =
            new ConcurrentHashMap<>();

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
        return null; //diary.getCommandLine();
    }

    @Override
    public DateTimeStamp getTimeOfFirstEvent() {
        return new DateTimeStamp(0.0d); //diary.getTimeOfFirstEvent();
    }

    @Override
    public DateTimeStamp getTimeOfLastEvent() {
        if (getTimeOfFirstEvent().before(timeOfLastEvent))
            return timeOfLastEvent;
        else
            return getTimeOfFirstEvent();
    }

    @Override
    public double getRuntimeDuration() {
        boolean isLogFragment = getTimeOfFirstEvent().getTimeStamp() > LOG_FRAGMENT_THRESHOLD;
        if (isLogFragment)
            return getTimeOfLastEvent().minus(getTimeOfFirstEvent());
        return getTimeOfLastEvent().getTimeStamp();        }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Aggregation> Optional<T> getAggregation(Class<T> aggregationClass) {
        return Optional.ofNullable((T) aggregatedData.get(aggregationClass));
    }

    @Override
    public JvmConfiguration getJvmConfiguration() {
        return jvmConfigurationForCoreApi;
    }

    // Invoked reflectively from GCToolKit
    public void analyze(Set<Class<? extends Aggregation>> registeredAggregations, DataSource<?> dataSource) {

        try {
            final GCLogFile gcLogFile = (GCLogFile) dataSource;
            this.diary = gcLogFile.diary();

            GCToolkitVertxParameters GCToolkitVertxParameters = new GCToolkitVertxParametersForUnifiedLogs(registeredAggregations, this.diary);

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


            this.jvmConfigurationForCoreApi = new JvmConfigurationImpl(diary);


        } catch (IOException | ClassCastException e ) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
