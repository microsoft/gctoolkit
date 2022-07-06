// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public abstract class CMSConcurrentEvent extends GenerationalGCEvent implements CMSPhase {

    private double cpuTime;
    private double wallClockTime;

    protected CMSConcurrentEvent(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration, double cpuTime, double wallClockTime) {
        super(timeStamp, type, cause, duration);
        this.cpuTime = cpuTime;
        this.wallClockTime = wallClockTime;
    }

    public double getCpuTime() {
        return cpuTime;
    }

    public double getWallClockTime() {
        return wallClockTime;
    }

}
