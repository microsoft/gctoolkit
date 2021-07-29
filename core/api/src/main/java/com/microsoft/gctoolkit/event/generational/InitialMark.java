// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.time.DateTimeStamp;

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
