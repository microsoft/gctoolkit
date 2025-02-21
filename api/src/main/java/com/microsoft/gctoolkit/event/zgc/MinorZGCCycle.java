// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GCEvent;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class MinorZGCCycle extends GCEvent {
    private ZGCCycle youngCycle;
    private ZGCMemorySummary memorySummary;
    private long gcId;

    public MinorZGCCycle(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, gcType, cause, duration);
    }

    public MinorZGCCycle(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

    public MinorZGCCycle(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, cause, duration);
    }

    public MinorZGCCycle(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, double duration) {
        super(timeStamp, gcType, duration);
    }

    public ZGCCycle getYoungCycle() {
        return youngCycle;
    }

    public void setYoungCycle(ZGCCycle youngCycle) {
        this.youngCycle = youngCycle;
    }

    public ZGCMemorySummary getMemorySummary() {
        return memorySummary;
    }

    public void setMemorySummary(ZGCMemorySummary memorySummary) {
        this.memorySummary = memorySummary;
    }

    public void setGcId(long gcId) {

        this.gcId = gcId;
    }

    public long getGcId() {
        return gcId;
    }
}
