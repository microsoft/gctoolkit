// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class G1ConcurrentMarkResetForOverflow extends G1GCConcurrentEvent {

    public G1ConcurrentMarkResetForOverflow(DateTimeStamp timeStamp) {
        super(timeStamp, GarbageCollectionTypes.G1ConcurrentMarkResetForOverflow, GCCause.CONCURRENT_MARK_STACK_OVERFLOW, 0.0d);
    }

}
