// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

/**
 * Occupancy of the survivor space before and after a collection. Size is {@code -1} when the log omitted it.
 */
public class SurvivorMemoryPoolSummary {

    private final long occupancyBeforeCollection;
    private final long occupancyAfterCollection;
    private final long size;

    public SurvivorMemoryPoolSummary(long occupancyBeforeCollection, long occupancyAfterCollection) {
        this.occupancyBeforeCollection = occupancyBeforeCollection;
        this.occupancyAfterCollection = occupancyAfterCollection;
        this.size = -1L;
    }

    public SurvivorMemoryPoolSummary(long occupancyBeforeCollection, long occupancyAfterCollection, long sizeAfterCollection) {
        this.occupancyBeforeCollection = occupancyBeforeCollection;
        this.occupancyAfterCollection = occupancyAfterCollection;
        this.size = sizeAfterCollection;
    }

    public long getOccupancyBeforeCollection() {
        return this.occupancyBeforeCollection;
    }

    public long getOccupancyAfterCollection() {
        return this.occupancyAfterCollection;
    }

    public long getSize() {
        return this.size;
    }

}
