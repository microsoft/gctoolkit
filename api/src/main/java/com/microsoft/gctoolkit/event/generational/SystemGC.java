// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class SystemGC extends FullGC {

    public SystemGC(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }

    public SystemGC(DateTimeStamp timeStamp, GCCause cause, double duration) {
        this(timeStamp, GarbageCollectionTypes.SystemGC, cause, duration);
    }

    public SystemGC(DateTimeStamp timeStamp, double duration) {
        this(timeStamp, GarbageCollectionTypes.SystemGC, GCCause.JAVA_LANG_SYSTEM, duration);
    }
}
