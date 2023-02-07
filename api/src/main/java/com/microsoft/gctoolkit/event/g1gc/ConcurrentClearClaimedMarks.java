// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * Concurrent phase, Clear claimed marks
 */

public class ConcurrentClearClaimedMarks extends G1GCConcurrentEvent {

    /**
     *
     * @param timeStamp time of event
     * @param duration duration of event
     */
    public ConcurrentClearClaimedMarks(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentClearClaimedMarks, GCCause.UNKNOWN_GCCAUSE, duration);
    }


}
