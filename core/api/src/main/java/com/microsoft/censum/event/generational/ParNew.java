// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.event.TLABSummary;
import com.microsoft.censum.time.DateTimeStamp;

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

    @Override
    public boolean isYoung() {
        return true;
    }

}
