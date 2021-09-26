// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class AbortablePreClean extends CMSConcurrentEvent {

    private final boolean abortedDueToTime;

    public AbortablePreClean(DateTimeStamp timeStamp, double duration, double cpuTime, double wallClockTime, boolean abortDueToTime) {
        super(timeStamp, GarbageCollectionTypes.Abortable_Preclean, GCCause.UNKNOWN_GCCAUSE, duration, cpuTime, wallClockTime);
        this.abortedDueToTime = abortDueToTime;
    }

    public boolean isAbortedDueToTime() {
        return this.abortedDueToTime;
    }

}
