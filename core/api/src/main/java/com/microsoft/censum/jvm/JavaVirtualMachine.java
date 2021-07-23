// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.jvm;


import com.microsoft.censum.aggregator.Aggregation;
import com.microsoft.censum.aggregator.Aggregator;
import com.microsoft.censum.io.DataSource;
import com.microsoft.censum.time.DateTimeStamp;

import java.util.Map;

/**
 * JavaVirtualMachine is a representation of the JVM state obtained by analyzing a GC log file.
 * An instance of JavaVirtualMachine is created by calling {@link com.microsoft.censum.Censum#analyze(DataSource)}
 */
public interface JavaVirtualMachine {

    /**
     * Return {@code true} if the JVM was using G1GC.
     * @return {@code true} if the GC is G1GC.
     */
    boolean isG1GC();

    /**
     * Return {@code true} if the JVM was using ZGC.
     * @return {@code true} if the GC is ZGC.
     */
    boolean isZGC();

    /**
     * Return {@code true} if the JVM was using Shenandoah.
     * @return {@code true} if the GC is Shenandoah.
     */
    boolean isShenandoah();

    /**
     * Return {@code true} if the JVM was using Parallel GC.
     * @return {@code true} if the GC is Parallel GC.
     */
    boolean isParallel() ;

    /**
     * Return {@code true} if the JVM was using Serial GC.
     * @return {@code true} if the GC is Serial GC.
     */
    boolean isSerial();

    /**
     * Return {@code true} if the JVM was using CMS GC.
     * @return {@code true} if the GC is CMS GC.
     */
    boolean isCMS();

    /**
     * Return the command line used to run the JVM, if available.
     * @return The command line used to run the JVM, or {@code null}
     */
    String getCommandLine();

    /**
     * Return the time of the first event in the GC log file.
     * @return The time of the first event.
     */
    DateTimeStamp getTimeOfFirstEvent();

    /**
     * Return the time of the last event in the GC log file.
     * @return The time of the last event.
     */
    DateTimeStamp getTimeOfLastEvent();

    /**
     * Return the runtime duration. This is not necessarily the difference
     * between the first and last event. Rather, this calculation considers
     * the duration of the events.
     * @return The runtime duration that the GC log represents.
     */
    double getRuntimeDuration();

    /**
     * Get the configuration metadata. This configuration data is either
     * known from JVM flags, or inferred from parsing the GC log file.
     * @return The {@code JvmConfiguration}
     */
    JvmConfiguration getJvmConfiguration();

    /**
     * Return the {@code Aggregation} that was used in the analysis of the GC log file
     * that is the same class as {@code aggregationClass}. In other words, {@code aggregationClass}
     * is a key used to look up an instance of the {@code Aggregation}. The return value
     * may be {@code null} if the {@code Aggregation} was not used in the analysis. Which
     * {@code Aggregation}s are used depends on the GC.
     * @param aggregationClass The class of the Aggregation to be returned.
     * @param <T> type cast for the Aggregation class type.
     * @return an {@code Aggregation} whose {@code getClass() == aggregationClass}, or {@code null}
     */
    <T extends Aggregation> T getAggregation(Class<T> aggregationClass);
}