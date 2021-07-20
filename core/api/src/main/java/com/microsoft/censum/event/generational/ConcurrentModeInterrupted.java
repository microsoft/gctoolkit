// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class ConcurrentModeInterrupted extends FullGC {

    public ConcurrentModeInterrupted(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }

    public ConcurrentModeInterrupted(DateTimeStamp timeStamp, GCCause cause, double duration) {
        this(timeStamp, GarbageCollectionTypes.ConcurrentModeInterrupted, cause, duration);
    }

    @Override
    public boolean isConcurrentModeInterrupted() {
        return true;
    }

}
