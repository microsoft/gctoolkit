// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

/**
 *
 */

public class ConcurrentCleanupForNextMark extends G1GCConcurrentEvent {

    public ConcurrentCleanupForNextMark(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCleanupForNextMark, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    public ConcurrentCleanupForNextMark(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCleanupForNextMark, cause, duration);
    }

}
