// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class ConcurrentMark extends CMSConcurrentEvent {

    public ConcurrentMark(DateTimeStamp timeStamp, double duration, double cpuTime, double wallClockTime) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentMark, GCCause.UNKNOWN_GCCAUSE, duration, cpuTime, wallClockTime);
    }

}
