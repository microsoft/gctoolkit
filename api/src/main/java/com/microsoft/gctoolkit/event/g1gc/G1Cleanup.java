// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * Pause phase
 */
public class G1Cleanup extends G1RealPause {

    /**
     * @param timeStamp time of the event
     * @param duration duration of the event
     */
    public G1Cleanup(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.G1GCCleanup, GCCause.GCCAUSE_NOT_SET, duration);
    }

}
