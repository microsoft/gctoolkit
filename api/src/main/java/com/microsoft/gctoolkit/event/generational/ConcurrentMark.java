// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * CMS concurrent mark phase
 */
public class ConcurrentMark extends CMSConcurrentEvent {

    /**
     *
     * @param timeStamp start of event
     * @param duration duration of event
     * @param cpuTime amount of CPU time consumes
     * @param wallClockTime duration real time (hints at level of parallelize)
     */
    public ConcurrentMark(DateTimeStamp timeStamp, double duration, double cpuTime, double wallClockTime) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentMark, GCCause.UNKNOWN_GCCAUSE, duration, cpuTime, wallClockTime);
    }

}
