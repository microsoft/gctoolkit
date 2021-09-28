// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 *
 */

public class ConcurrentCompleteCleanup extends G1GCConcurrentEvent {

    public ConcurrentCompleteCleanup(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCompleteCleanup, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    public ConcurrentCompleteCleanup(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCompleteCleanup, cause, duration);
    }

}
