// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GCEvent;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public abstract class GenerationalGCEvent extends GCEvent {

    protected GenerationalGCEvent(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }
}
