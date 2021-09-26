// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ConcurrentMark extends CMSConcurrentEvent {

    public ConcurrentMark(DateTimeStamp timeStamp, double duration, double cpuTime, double wallClockTime) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentMark, GCCause.UNKNOWN_GCCAUSE, duration, cpuTime, wallClockTime);
    }

}
