// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

abstract public class CMSPauseEvent extends GenerationalGCPauseEvent implements CMSPhase {

    public CMSPauseEvent(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, gcType, cause, duration);
    }
}
