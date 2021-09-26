// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.event.MemoryPoolSummary;

public class ClassspaceSummary extends MemoryPoolSummary {

    private final long reserved;
    private final long available;

    public ClassspaceSummary(long before, long after, long committed, long reserved) {
        this(before, after, 0L, committed, reserved);
    }

    public ClassspaceSummary(long before, long after, long available, long committed, long reserved) {
        super(before, after, committed);
        this.reserved = reserved;
        this.available = available;
    }

    public long getReserved() {
        return reserved;
    }

    public long getAvailable() {
        return available;
    }
}
