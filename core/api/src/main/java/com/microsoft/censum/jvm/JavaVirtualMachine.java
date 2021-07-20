// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.jvm;


import com.microsoft.censum.aggregator.Aggregation;
import com.microsoft.censum.aggregator.Aggregator;
import com.microsoft.censum.time.DateTimeStamp;

import java.util.Map;

/**
 * JavaVirtualMachine
 */
public interface JavaVirtualMachine {

    boolean isG1GC();

    boolean isZGC();

    boolean isShenandoah();

    boolean isParallel() ;

    boolean isSerial();

    boolean isConcurrent();

    String getCommandLine();

    DateTimeStamp getTimeOfFirstEvent();

    DateTimeStamp getTimeOfLastEvent();

    double getRuntimeDuration();

    JvmConfiguration getJvmConfiguration();

    <T extends Aggregation> Aggregation getAggregations(Class<T> aggregationClass);
}