// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

/**
 * not a trap
 */
public abstract class G1RealPause extends G1GCPauseEvent {
    public G1RealPause(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }
}
