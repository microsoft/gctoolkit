// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.event.MemoryPoolSummary;

public class PermGenSummary extends MemoryPoolSummary {

    public PermGenSummary(long before, long after, long size) {
        super(before, after, size);
    }
}
