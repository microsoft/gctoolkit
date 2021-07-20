// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class ConcurrentReset extends CMSConcurrentEvent {

    public ConcurrentReset(DateTimeStamp timeStamp, double duration, double cpuTime, double wallClockTime) {
        super(timeStamp, GarbageCollectionTypes.Concurrent_Reset, GCCause.UNKNOWN_GCCAUSE, duration, cpuTime, wallClockTime);
    }

}
