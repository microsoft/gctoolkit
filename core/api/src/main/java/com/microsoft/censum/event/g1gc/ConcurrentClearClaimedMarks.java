// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

/**
 *
 */

public class ConcurrentClearClaimedMarks extends G1GCConcurrentEvent {

    public ConcurrentClearClaimedMarks(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentClearClaimedMarks, GCCause.UNKNOWN_GCCAUSE, duration);
    }


}
