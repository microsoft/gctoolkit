// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class ConcurrentModeFailure extends FullGC {

    public ConcurrentModeFailure(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentModeFailure, cause, duration);
    }

    @Override
    public boolean isConcurrentModeFailure() {
        return true;
    }
}
