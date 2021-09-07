// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.ReferenceGCSummary;
import com.microsoft.gctoolkit.event.SurvivorMemoryPoolSummary;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public abstract class G1GCPauseEvent extends G1GCEvent {

    private final MemoryPoolSummary NULL_POOL = new MemoryPoolSummary(-1L, -1L, -1L, -1L);

    private MemoryPoolSummary eden;
    private SurvivorMemoryPoolSummary survivor;
    private MemoryPoolSummary heap;
    private MemoryPoolSummary permOrMetaspace;
    private ReferenceGCSummary referenceGCSummary = null;

    private CPUSummary cpuSummary;

    public G1GCPauseEvent(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }

    public void addMemorySummary(MemoryPoolSummary eden, SurvivorMemoryPoolSummary survivor, MemoryPoolSummary heap) {
        this.eden = eden;
        this.survivor = survivor;
        this.heap = heap;
    }

    public void addMemorySummary(MemoryPoolSummary heap) {
        this.addMemorySummary(null, null, heap);
    }

    public void addPermOrMetaSpaceRecord(MemoryPoolSummary permOrMetaspaceRecord) {
        permOrMetaspace = permOrMetaspaceRecord;
    }

    public void addCPUSummary(CPUSummary summary) {
        this.cpuSummary = summary;
    }

    public MemoryPoolSummary getEden() {
        return this.eden;
    }

    public SurvivorMemoryPoolSummary getSurvivor() {
        return this.survivor;
    }

    public MemoryPoolSummary getHeap() {
        return this.heap;
    }

    public MemoryPoolSummary getPermOrMetaspace() {
        return this.permOrMetaspace;
    }

    public MemoryPoolSummary getTenured() {
        if ((getEden() == null) || (getHeap() == null)) {
            return NULL_POOL;
        } else if (getSurvivor() == null) {
            return new MemoryPoolSummary(getHeap().getOccupancyBeforeCollection() - this.getEden().getOccupancyBeforeCollection(),
                    getHeap().getSizeBeforeCollection() - getEden().getSizeBeforeCollection(),
                    getHeap().getOccupancyAfterCollection() - getEden().getOccupancyAfterCollection(),
                    getHeap().getSizeAfterCollection() - getEden().getSizeAfterCollection());
        } else {
            return new MemoryPoolSummary(getHeap().getOccupancyBeforeCollection() - this.getEden().getOccupancyBeforeCollection(),
                    getHeap().getSizeBeforeCollection() - getEden().getSizeBeforeCollection() - getSurvivor().getOccupancyBeforeCollection(),
                    getHeap().getOccupancyAfterCollection() - getEden().getOccupancyAfterCollection() - getSurvivor().getOccupancyAfterCollection(),
                    getHeap().getSizeAfterCollection() - getEden().getSizeAfterCollection());
        }
    }

    public void add(ReferenceGCSummary summary) {
        this.referenceGCSummary = summary;
    }

    public ReferenceGCSummary getReferenceGCSummary() {
        return this.referenceGCSummary;
    }

    public CPUSummary getCpuSummary() {
        return this.cpuSummary;
    }

}
