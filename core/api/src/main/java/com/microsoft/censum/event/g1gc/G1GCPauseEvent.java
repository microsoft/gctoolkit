// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.CPUSummary;
import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.event.MemoryPoolSummary;
import com.microsoft.censum.event.ReferenceGCSummary;
import com.microsoft.censum.event.SurvivorMemoryPoolSummary;
import com.microsoft.censum.time.DateTimeStamp;

public abstract class G1GCPauseEvent extends G1GCEvent {

    private MemoryPoolSummary NULL_POOL = new MemoryPoolSummary(-1L, -1L, -1L, -1L);

    MemoryPoolSummary eden;
    SurvivorMemoryPoolSummary survivor;
    MemoryPoolSummary heap;
    MemoryPoolSummary permOrMetaspace;
    ReferenceGCSummary referenceGCSummary = null;

    CPUSummary cpuSummary;

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
