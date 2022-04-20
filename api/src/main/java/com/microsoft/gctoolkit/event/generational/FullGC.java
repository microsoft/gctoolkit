// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class FullGC extends GenerationalGCPauseEvent {

    private int dutyCycle = -1;

    public FullGC(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }

    public FullGC(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.FullGC, cause, duration);
    }

    public void recordDutyCycle(int dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

    public int getDutyCycle() {
        return dutyCycle;
    }

}
