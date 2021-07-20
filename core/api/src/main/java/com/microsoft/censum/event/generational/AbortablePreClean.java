// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

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
