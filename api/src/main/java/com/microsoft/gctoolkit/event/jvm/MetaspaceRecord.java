// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.event.MemoryPoolSummary;

public class MetaspaceRecord extends MemoryPoolSummary {

    public MetaspaceRecord(long before, long after, long size) {
        super(before, after, size);
    }
}
