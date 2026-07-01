// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.jvm;


import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.message.DataSourceChannel;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.List;
import java.util.Optional;

/// JavaVirtualMachine is a representation of the JVM state obtained by analyzing a GC log file.
/// An instance of JavaVirtualMachine is created by calling [GCToolKit#analyze(DataSource)]
public interface JavaVirtualMachine {

    /// @param dataSource the log to be considered.
    /// Return `true` if the JavaVirtualMachine implementation can work with the GC log.
    /// @return `true` if the JavaVirtualMachine implementation can work with the GC Log.

    boolean accepts(DataSource dataSource);

    /// True if the log is unified or false for preunified
    /// @return true is the log is from JDK 9+

    boolean isUnifiedLogging();

    /// Return `true` if the JVM was using G1GC.
    /// @return `true` if the GC is G1GC.
    boolean isG1GC();

    /// Return `true` if the JVM was using ZGC.
    /// @return `true` if the GC is ZGC.
    boolean isZGC();

    /// Return `true` if the JVM was using Shenandoah.
    /// @return `true` if the GC is Shenandoah.
    boolean isShenandoah();

    /// Return `true` if the JVM was using Parallel GC.
    /// @return `true` if the GC is Parallel GC.
    boolean isParallel() ;

    /// Return `true` if the JVM was using Serial GC.
    /// @return `true` if the GC is Serial GC.
    boolean isSerial();

    /// Return `true` if the JVM was using CMS GC.
    /// @return `true` if the GC is CMS GC.
    boolean isCMS();

    /// Return the command line used to run the JVM, if available.
    /// @return The command line used to run the JVM, or `null`
    String getCommandLine();

    /// Return the time of the first event in the GC log file.
    /// @return The time of the last event.
    DateTimeStamp getTimeOfFirstEvent();

    /// Estimates the initial start time of the log in the case that the log
    /// is determined to be a fragment. Otherwise, return a start time of 0.000 seconds
    /// @return The time of the first event.
    DateTimeStamp getEstimatedJVMStartTime();

    /// Return the time of the last event in the GC log file.
    /// @return The time of the last event.
    DateTimeStamp getJVMTerminationTime();

    /// Return the runtime duration. This is not necessarily the difference
    /// between the first and last event. Rather, this calculation considers
    /// the duration of the events.
    /// @return The runtime duration that the GC log represents.
    double getRuntimeDuration();

    /// Return the `Aggregation` that was used in the analysis of the GC log file
    /// that is the same class as `aggregationClass`. In other words, `aggregationClass`
    /// is a key used to look up an instance of the `Aggregation`. The return value
    /// may be `null` if the `Aggregation` was not used in the analysis. Which
    /// `Aggregation`s are used depends on the GC.
    /// @param aggregationClass The class of the Aggregation to be returned.
    /// @param T type cast for the Aggregation class type.
    /// @return an `Aggregation` whose `getClass() == aggregationClass`, or `null`
    /// if given aggregationClass is not available.
    <T extends Aggregation> Optional<T> getAggregation(Class<T> aggregationClass);

    /// Interface to trigger the analysis of a gc log.
    /// @param registeredAggregations all aggregations supplied by the module SPI
    /// @param eventChannel JVMEvent message channel
    /// @param dataSourceChannel GC logging data channel
    void analyze(List<Aggregator<? extends Aggregation>> registeredAggregations, JVMEventChannel eventChannel, DataSourceChannel dataSourceChannel);
}