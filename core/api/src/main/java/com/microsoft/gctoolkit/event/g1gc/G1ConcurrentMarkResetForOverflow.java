// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class G1ConcurrentMarkResetForOverflow extends G1GCConcurrentEvent {

    public G1ConcurrentMarkResetForOverflow(DateTimeStamp timeStamp) {
        super(timeStamp, GarbageCollectionTypes.G1ConcurrentMarkResetForOverflow, GCCause.CONCURRENT_MARK_STACK_OVERFLOW, 0.0d);
    }

}
