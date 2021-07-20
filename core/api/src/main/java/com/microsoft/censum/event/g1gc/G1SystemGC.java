// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class G1SystemGC extends G1FullGC {

    public G1SystemGC(DateTimeStamp timeStamp, double pauseTime) {
        super(timeStamp, GarbageCollectionTypes.SystemGC, GCCause.JAVA_LANG_SYSTEM, pauseTime);
    }

    public boolean isFull() {
        return true;
    }

    public boolean isSystemGC() {
        return true;
    }


}
