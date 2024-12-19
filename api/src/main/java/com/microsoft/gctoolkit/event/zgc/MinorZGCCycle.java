// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class MinorZGCCycle extends MajorZGCCycle {
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
}