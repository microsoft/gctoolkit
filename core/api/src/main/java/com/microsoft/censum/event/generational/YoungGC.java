// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class YoungGC extends GenerationalGCPauseEvent {

    public YoungGC(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, gcType, cause, duration);
    }

    public YoungGC(DateTimeStamp timeStamp, GCCause cause, double duration) {
        this(timeStamp, GarbageCollectionTypes.GC, cause, duration);
    }

    @Override
    public boolean isYoung() {
        return true;
    }

}
