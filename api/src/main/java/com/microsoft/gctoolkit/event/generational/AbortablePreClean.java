// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * CMS phase to put time between the InitialMark and the Remark phase. Abortable when time or space thresholds are met.
 */
public class AbortablePreClean extends CMSConcurrentEvent {

    private final boolean abortedDueToTime;

    /**
     *
     * @param timeStamp time of event start
     * @param duration  how long the event lasted
     * @param cpuTime   CPU consumption time
     * @param wallClockTime  real time
     * @param abortDueToTime was this phase aborted due to time
     */
    public AbortablePreClean(DateTimeStamp timeStamp, double duration, double cpuTime, double wallClockTime, boolean abortDueToTime) {
        super(timeStamp, GarbageCollectionTypes.Abortable_Preclean, GCCause.UNKNOWN_GCCAUSE, duration, cpuTime, wallClockTime);
        this.abortedDueToTime = abortDueToTime;
    }

    /**
     * Was the event aborted due to a timeout
     * @return true is event was aborted due to a timeout (2 minutes by default).
     */
    public boolean isAbortedDueToTime() {
        return this.abortedDueToTime;
    }

}
