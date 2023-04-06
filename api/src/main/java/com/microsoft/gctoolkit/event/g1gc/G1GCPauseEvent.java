// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.ReferenceGCSummary;
import com.microsoft.gctoolkit.event.RegionSummary;
import com.microsoft.gctoolkit.event.SurvivorMemoryPoolSummary;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public abstract class G1GCPauseEvent extends G1GCEvent {

    private static final MemoryPoolSummary NULL_POOL = new MemoryPoolSummary(-1L, -1L, -1L, -1L);
    private static final RegionSummary NULL_REGION = new RegionSummary(-1, -1, -1);

    private MemoryPoolSummary eden;
    private SurvivorMemoryPoolSummary survivor;
    private MemoryPoolSummary heap;
    private MemoryPoolSummary permOrMetaspace;
    private ReferenceGCSummary referenceGCSummary = null;

    private RegionSummary edenRegion;
    private RegionSummary survivorRegion;
    private RegionSummary oldRegion;
    private RegionSummary humongousRegion;
    private RegionSummary archiveRegion;

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

    public void addRegionSummary(RegionSummary eden, RegionSummary survivor, RegionSummary old, RegionSummary humongous, RegionSummary archive) {
        this.edenRegion = eden;
        this.survivorRegion = survivor;
        this.oldRegion = old;
        this.humongousRegion = humongous;
        this.archiveRegion = archive;
    }

    public RegionSummary getEdenRegionSummary() {
        return this.edenRegion == null ? NULL_REGION : this.edenRegion;
    }

    public RegionSummary getSurvivorRegionSummary() {
        return this.survivorRegion == null ? NULL_REGION : this.survivorRegion;
    }

    public RegionSummary getOldRegionSummary() {
        return this.oldRegion == null ? NULL_REGION : this.oldRegion;
    }

    public RegionSummary getHumongousRegionSummary() {
        return this.humongousRegion == null ? NULL_REGION : this.humongousRegion;
    }

    public RegionSummary getArchiveRegionSummary() {
        return this.archiveRegion == null ? NULL_REGION : this.archiveRegion;
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
            return getHeap().minus(getEden());
        } else {
            return new MemoryPoolSummary(getHeap().getOccupancyBeforeCollection() - this.getEden().getOccupancyBeforeCollection() - getSurvivor().getOccupancyBeforeCollection(),
                    getHeap().getSizeBeforeCollection() - getEden().getSizeBeforeCollection() - getSurvivor().getOccupancyBeforeCollection(),
                    getHeap().getOccupancyAfterCollection() - getEden().getOccupancyAfterCollection() - getSurvivor().getOccupancyAfterCollection(),
                    getHeap().getSizeAfterCollection() - getEden().getSizeAfterCollection() - getSurvivor().getOccupancyAfterCollection());
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
