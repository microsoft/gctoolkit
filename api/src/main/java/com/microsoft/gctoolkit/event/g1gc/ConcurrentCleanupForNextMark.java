// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * Concurrent cleanup for next mark
 */

public class ConcurrentCleanupForNextMark extends G1GCConcurrentEvent {

    /**
     * @param timeStamp start of event
     * @param duration event duration
     */
    public ConcurrentCleanupForNextMark(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCleanupForNextMark, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    /**
     *
     * @param timeStamp start of event
     * @param cause trigger for the event
     * @param duration event duration
     */
    public ConcurrentCleanupForNextMark(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCleanupForNextMark, cause, duration);
    }

}
