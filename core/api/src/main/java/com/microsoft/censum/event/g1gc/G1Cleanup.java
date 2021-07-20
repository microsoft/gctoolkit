// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;


public class G1Cleanup extends G1RealPause {

    public G1Cleanup(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.G1GCCleanup, GCCause.GCCAUSE_NOT_SET, duration);
    }

}
