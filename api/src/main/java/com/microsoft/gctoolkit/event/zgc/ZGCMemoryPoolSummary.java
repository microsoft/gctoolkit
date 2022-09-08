// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

public class ZGCMemoryPoolSummary {

    // these are effectively final but David likes to see an explicit final. In Java 14 this could be a Record.
    private final long capacity;
    private final long free;
    private final long used;

    public ZGCMemoryPoolSummary(long capacity, long free, long used) {
        this.capacity = capacity;
        this.free = free;
        this.used = used;
    }

    public long getCapacity() {
        return this.capacity;
    }

    public long getFree() {
        return this.free;
    }

    public long getUsed() {
        return this.used;
    }
}
