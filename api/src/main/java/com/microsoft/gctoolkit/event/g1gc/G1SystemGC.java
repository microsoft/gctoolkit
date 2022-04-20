// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class G1SystemGC extends G1FullGC {

    public G1SystemGC(DateTimeStamp timeStamp, double pauseTime) {
        super(timeStamp, GarbageCollectionTypes.SystemGC, GCCause.JAVA_LANG_SYSTEM, pauseTime);
    }
}
