// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public abstract class CMSPauseEvent extends GenerationalGCPauseEvent implements CMSPhase {

    public CMSPauseEvent(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, gcType, cause, duration);
    }
}
