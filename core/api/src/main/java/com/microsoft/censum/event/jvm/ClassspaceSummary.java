// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.jvm;

import com.microsoft.censum.event.MemoryPoolSummary;

public class ClassspaceSummary extends MemoryPoolSummary {

    private long reserved;
    private long available;

    public ClassspaceSummary(long before, long after, long committed, long reserved) {
        super(before, after, committed);
        this.reserved = reserved;
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
