// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ConcurrentReset extends CMSConcurrentEvent {

    public ConcurrentReset(DateTimeStamp timeStamp, double duration, double cpuTime, double wallClockTime) {
        super(timeStamp, GarbageCollectionTypes.Concurrent_Reset, GCCause.UNKNOWN_GCCAUSE, duration, cpuTime, wallClockTime);
    }

}
