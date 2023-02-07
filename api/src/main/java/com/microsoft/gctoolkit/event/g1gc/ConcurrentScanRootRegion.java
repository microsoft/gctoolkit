// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * Concurrent phase
 */

public class ConcurrentScanRootRegion extends G1GCConcurrentEvent {

    /**
     * @param timeStamp time of the event
     * @param duration duration of the event
     */
    public ConcurrentScanRootRegion(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentRootRegionScan, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    /**
     * @param timeStamp time of the event
     * @param cause reason to trigger the event
     * @param duration duration of the event
     */
    public ConcurrentScanRootRegion(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentRootRegionScan, cause, duration);
    }

}
