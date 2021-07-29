// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;


public class G1ConcurrentCleanup extends G1GCConcurrentEvent {

    public G1ConcurrentCleanup(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.G1GCConcurrentCleanup, GCCause.GCCAUSE_NOT_SET, duration);
    }

    public G1ConcurrentCleanup(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.G1GCConcurrentCleanup, cause, duration);
    }

}
