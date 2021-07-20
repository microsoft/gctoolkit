// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

/**
 *
 */

public class ConcurrentScanRootRegion extends G1GCConcurrentEvent {

    public ConcurrentScanRootRegion(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentRootRegionScan, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    public ConcurrentScanRootRegion(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentRootRegionScan, cause, duration);
    }

}
