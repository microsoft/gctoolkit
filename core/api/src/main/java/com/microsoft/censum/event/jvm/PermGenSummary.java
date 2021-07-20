// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.jvm;

import com.microsoft.censum.event.MemoryPoolSummary;

public class PermGenSummary extends MemoryPoolSummary {

    public PermGenSummary(long before, long after, long size) {
        super(before, after, size);
    }
}
