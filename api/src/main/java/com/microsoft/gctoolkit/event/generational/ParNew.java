// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.TLABSummary;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ParNew extends GenerationalGCPauseEvent {

    private TLABSummary tlabSummary = null;

    private int dutyCycle = -1;

    public ParNew(DateTimeStamp dateTimeStamp, GCCause gcCause, double pauseTime) {
        this(dateTimeStamp, GarbageCollectionTypes.ParNew, gcCause, pauseTime);
    }

    public ParNew(DateTimeStamp dateTimeStamp, GarbageCollectionTypes gcCollectionType, GCCause gcCause, double pauseTime) {
        super(dateTimeStamp, gcCollectionType, gcCause, pauseTime);
    }

    public void recordDutyCycle(int dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

    public int getDutyCycle() {
        return dutyCycle;
    }

    public void recordTLabSummary() {
        tlabSummary = new TLABSummary();
    }

    public TLABSummary getTlabSummary() {
        return tlabSummary;
    }

}
