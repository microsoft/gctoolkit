// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.vertx.jvm;

import com.microsoft.censum.aggregator.Aggregation;
import com.microsoft.censum.io.DataSource;
import com.microsoft.censum.io.GCLogFile;
import com.microsoft.censum.jvm.JavaVirtualMachine;
import com.microsoft.censum.jvm.JvmConfiguration;
import com.microsoft.censum.time.DateTimeStamp;
import com.microsoft.censum.vertx.CensumVertx;
import com.microsoft.censum.parser.jvm.JVMConfiguration;
import com.microsoft.censum.parser.jvm.PreUnifiedJVMConfiguration;
import com.microsoft.censum.parser.jvm.UnifiedJVMConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of JavaVirtualMachine that uses io.vertx verticles to feed
 * lines to the parser(s) and post events to the aggregators. This implementation
 * is here in the vertx module so that the api and parser modules can exist without
 * having to import io.vertx. In the api module, the class Censum uses the classloader
 * to load DefaultJavaVirtualMachine.
 */
public class DefaultJavaVirtualMachine implements JavaVirtualMachine {

    private static final Logger LOGGER = Logger.getLogger(DefaultJavaVirtualMachine.class.getName());

    private static final double LOG_FRAGMENT_THRESHOLD = 18000;

    private JVMConfiguration jvmConfigurationFromParser;
    private JvmConfiguration jvmConfigurationForCoreApi;
    private DateTimeStamp timeOfLastEvent;
    private Map<Class<? extends Aggregation>, Aggregation> aggregatedData;

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
    public boolean isConcurrent() {
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
    public <T extends Aggregation> T getAggregation(Class<T> aggregationClass) {
        return aggregatedData != null ? (T)aggregatedData.get(aggregationClass) : null;
    }

    @Override
    public JvmConfiguration getJvmConfiguration() {
        return jvmConfigurationForCoreApi;
    }

    public DefaultJavaVirtualMachine() { this.aggregatedData = new HashMap<>(); }

    // Invoked reflectively from Censum
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

            CensumVertxParameters censumVertxParameters = gcLogFile.isUnifiedFormat()
                    ? new CensumVertxParametersForUnifiedLogs(registeredAggregations, this.jvmConfigurationFromParser)
                    : new CensumVertxParametersForPreUnifiedLogs(registeredAggregations, this.jvmConfigurationFromParser);

            this.timeOfLastEvent = CensumVertx.aggregateDataSource(
                    dataSource,
                    censumVertxParameters.logFileParsers(),
                    censumVertxParameters.aggregatorVerticles(),
                    censumVertxParameters.mailBox()
            );

            censumVertxParameters.aggregatorVerticles().stream()
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
