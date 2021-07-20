// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

/**
 * full GC not elsewhere specified on the G1 garbage collector
 */
public class G1FullGCNES extends G1FullGC {
    public G1FullGCNES(DateTimeStamp timeStamp, GCCause cause, double pauseTime) {
        this(timeStamp, GarbageCollectionTypes.Full, cause, pauseTime);
    }

    public G1FullGCNES(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double pauseTime) {
        super(timeStamp, type, cause, pauseTime);
    }

}
