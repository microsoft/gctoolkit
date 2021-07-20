// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class DefNew extends GenerationalGCPauseEvent {

    public DefNew(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.DefNew, cause, duration);
    }

    @Override
    public boolean isYoung() {
        return true;
    }

}
