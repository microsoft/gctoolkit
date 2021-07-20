// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

abstract public class CMSConcurrentEvent extends GenerationalGCEvent implements CMSPhase {

    double cpuTime;
    double wallClockTime;

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

    @Override
    public boolean isConcurrent() {
        return true;
    }

}
