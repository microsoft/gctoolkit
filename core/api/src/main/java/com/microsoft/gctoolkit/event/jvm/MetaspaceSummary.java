// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.event.MemoryPoolSummary;

public class MetaspaceSummary extends MemoryPoolSummary {

    private long reserved = -1L;
    private long available = -1L;

    public MetaspaceSummary(long before, long after, long committed, long reserved) {
        super(before, after, committed);
        this.reserved = reserved;
    }

    public MetaspaceSummary(long before, long after, long available, long committed, long reserved) {
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
