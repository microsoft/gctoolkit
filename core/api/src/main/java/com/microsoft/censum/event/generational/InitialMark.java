// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.event.MemoryPoolSummary;
import com.microsoft.censum.time.DateTimeStamp;

public class InitialMark extends CMSPauseEvent {

    public InitialMark(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.InitialMark, cause, duration);
    }

    public InitialMark(DateTimeStamp timeStamp, double duration) {
        this(timeStamp, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    public void add(MemoryPoolSummary tenured, MemoryPoolSummary heap) {
        this.add(heap.minus(tenured), tenured, heap);
    }

}
