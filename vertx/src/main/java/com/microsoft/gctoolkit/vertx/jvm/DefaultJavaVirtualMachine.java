// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.jvm.JvmConfiguration;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.vertx.GCToolkitVertx;
import com.microsoft.gctoolkit.parser.jvm.JVMConfiguration;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedJVMConfiguration;
import com.microsoft.gctoolkit.parser.jvm.UnifiedJVMConfiguration;

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
 * to load DefaultJavaVirtualMachine.
 */
public class DefaultJavaVirtualMachine implements JavaVirtualMachine {

    private static final Logger LOGGER = Logger.getLogger(DefaultJavaVirtualMachine.class.getName());

    private static final double LOG_FRAGMENT_THRESHOLD = 18000;

    private JVMConfiguration jvmConfigurationFromParser;
    private JvmConfiguration jvmConfigurationForCoreApi;
    private DateTimeStamp timeOfLastEvent;
    private final Map<Class<? extends Aggregation>, Aggregation> aggregatedData =
            new ConcurrentHashMap<>();

    @Override
    public boolean isG1GC() {
        return jvmConfigurationFromParser.getDiary().isG1GC();
    }

    @Override
    public boolean isZGC() {
        return jvmConfigurationFromParser.getDiary().isZGC();
    }

    @Override
    public boolean isShenandoah() {
        return jvmConfigurationFromParser.getDiary().isShenandoah();
    }

    @Override
    public boolean isParallel() {
        return jvmConfigurationFromParser.getDiary().isParNew();
    }

    @Override
    public boolean isSerial() {
        return jvmConfigurationFromParser.getDiary().isSerialFull();
    }

    @Override
    public boolean isCMS() {
        return jvmConfigurationFromParser.getDiary().isCMS();
    }

    @Override
    public String getCommandLine() {
        return jvmConfigurationFromParser.getCommandLine();
    }

    @Override
    public DateTimeStamp getTimeOfFirstEvent() {
        return jvmConfigurationFromParser.getTimeOfFirstEvent();
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
    @SuppressWarnings("unchecked")
    public void analyze(Set<Class<? extends Aggregation>> registeredAggregations, DataSource<?> dataSource) {

        try {
            final GCLogFile gcLogFile = (GCLogFile) dataSource;

            this.jvmConfigurationFromParser = gcLogFile.isUnifiedFormat()
                    ? new UnifiedJVMConfiguration()
                    : new PreUnifiedJVMConfiguration();

            gcLogFile.stream().
                    filter(Objects::nonNull).
                    map(String::trim).
                    filter(s -> s.length() > 0).
                    map(this.jvmConfigurationFromParser::diarize).
                    filter(completed -> completed).
                    findFirst();

            this.jvmConfigurationFromParser.fillInKnowns();

            GCToolkitVertxParameters GCToolkitVertxParameters = gcLogFile.isUnifiedFormat()
                    ? new GCToolkitVertxParametersForUnifiedLogs(registeredAggregations, this.jvmConfigurationFromParser)
                    : new GCToolkitVertxParametersForPreUnifiedLogs(registeredAggregations, this.jvmConfigurationFromParser);

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


            this.jvmConfigurationForCoreApi = new JvmConfigurationImpl(jvmConfigurationFromParser);


        } catch (IOException | ClassCastException e ) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
