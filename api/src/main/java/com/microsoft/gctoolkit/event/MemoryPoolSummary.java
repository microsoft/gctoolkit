// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

public class MemoryPoolSummary {

    private final long occupancyBeforeCollection;
    private final long sizeBeforeCollection;
    private final long occupancyAfterCollection;
    private final long sizeAfterCollection;

    public MemoryPoolSummary(long occupancyBeforeCollection, long sizeBeforeCollection, long occupancyAfterCollection, long sizeAfterCollection) {
        this.occupancyBeforeCollection = occupancyBeforeCollection;
        this.sizeBeforeCollection = sizeBeforeCollection;
        this.occupancyAfterCollection = occupancyAfterCollection;
        this.sizeAfterCollection = sizeAfterCollection;
    }

    public MemoryPoolSummary(long occupancyBeforeCollection, long occupancyAfterCollection, long sizeAfterCollection) {
        this(occupancyBeforeCollection, sizeAfterCollection, occupancyAfterCollection, sizeAfterCollection);
    }

    public long getOccupancyBeforeCollection() {
        return this.occupancyBeforeCollection;
    }

    public long getOccupancyAfterCollection() {
        return this.occupancyAfterCollection;
    }

    public long getSizeBeforeCollection() {
        return this.sizeBeforeCollection;
    }

    public long getSizeAfterCollection() {
        return this.sizeAfterCollection;
    }

    public MemoryPoolSummary minus(MemoryPoolSummary memoryPoolSummary) {
        return new MemoryPoolSummary(getOccupancyBeforeCollection() - memoryPoolSummary.getOccupancyBeforeCollection(), getSizeBeforeCollection() - memoryPoolSummary.getSizeBeforeCollection(),
                getOccupancyAfterCollection() - memoryPoolSummary.getOccupancyAfterCollection(), getSizeAfterCollection() - memoryPoolSummary.getSizeAfterCollection());
    }

    public MemoryPoolSummary add(MemoryPoolSummary memoryPoolSummary) {
        return new MemoryPoolSummary(
                getOccupancyBeforeCollection() + memoryPoolSummary.getOccupancyBeforeCollection(),
                getSizeBeforeCollection() + memoryPoolSummary.getSizeBeforeCollection(),
                getOccupancyAfterCollection() + memoryPoolSummary.getOccupancyAfterCollection(),
                getSizeAfterCollection() + memoryPoolSummary.getSizeAfterCollection());
    }

    public long kBytesRecovered() {
        long kBytesRecovered = kByteDelta();
        return (kBytesRecovered > 0) ? kBytesRecovered : 0L;
    }

    public long kBytesAllocated(MemoryPoolSummary previousHeapState) {
        long kBytesAllocated = occupancyBeforeCollection - previousHeapState.getOccupancyAfterCollection();
        return (kBytesAllocated > 0) ? kBytesAllocated : 0L;
    }

    // Will be positive if data is copied into the pool and negative if the pool has been GC'ed
    public long kByteDelta() {
        return occupancyBeforeCollection - occupancyAfterCollection;
    }

    public boolean isValid() {
        return sizeAfterCollection != -1;
    }

    public String toString() {
        return occupancyBeforeCollection + "K(" + sizeBeforeCollection + "K)->" + occupancyAfterCollection + "K(" + sizeAfterCollection + "K)";
    }
}
