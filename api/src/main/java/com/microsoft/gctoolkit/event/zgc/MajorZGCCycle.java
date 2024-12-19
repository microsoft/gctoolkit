// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class MajorZGCCycle extends ZGCCycle {
    public MajorZGCCycle(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, gcType, cause, duration);
    }

    public MajorZGCCycle(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

    public MajorZGCCycle(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, cause, duration);
    }

    public MajorZGCCycle(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, double duration) {
        super(timeStamp, gcType, duration);
    }
}