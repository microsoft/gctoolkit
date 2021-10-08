// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

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
