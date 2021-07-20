// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class PSFullGC extends FullGC {

    public PSFullGC(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }

    public PSFullGC(DateTimeStamp timeStamp, GCCause cause, double duration) {
        this(timeStamp, GarbageCollectionTypes.PSFull, cause, duration);
    }

}
