// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;


import java.util.ArrayList;

public class TLABSummary {

    private ArrayList<String> tlabRecords;

    public TLABSummary() {
    }

    public void add(String record) {
        tlabRecords.add(record);
    }

    public void addTLABRecord() {

    }
}

// Use as template to fill out
//TLAB: gc thread: 0x00007f2f6424f000 [id: 27358] desired_size: 1024KB slow allocs: 0  refill waste: 16384B alloc: 0.00091     3061KB refills: 1 waste 99.6% gc: 1043864B slow: 0B fast: 0B
//TLAB totals: thrds: 60  refills: 3222 max: 1397 slow allocs: 55 max 35 waste:  1.5% gc: 42509088B max: 1048360B slow: 9613800B max: 4478816B fast: 0B max: 0B
