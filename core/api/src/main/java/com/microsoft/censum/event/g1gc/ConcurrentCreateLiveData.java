// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

/**
 *
 */

public class ConcurrentCreateLiveData extends G1GCConcurrentEvent {

    public ConcurrentCreateLiveData(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCreateLiveData, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    public ConcurrentCreateLiveData(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCreateLiveData, cause, duration);
    }


}
