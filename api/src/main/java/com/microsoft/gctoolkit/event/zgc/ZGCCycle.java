// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GCEvent;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ZGCCycle extends GCEvent {
    public ZGCCycle(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, gcType, cause, duration);
    }

    public ZGCCycle(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

    public ZGCCycle(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, cause, duration);
    }

    public ZGCCycle(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, double duration) {
        super(timeStamp, gcType, duration);
    }

    private long gcId;

    private DateTimeStamp pauseMarkStartTimeStamp;
    private double pauseMarkStartDuration;

    private DateTimeStamp concurrentMarkTimeStamp;
    private double concurrentMarkDuration;

    private DateTimeStamp concurrentMarkFreeTimeStamp;
    private double concurrentMarkFreeDuration;

    private DateTimeStamp pauseMarkEndTimeStamp;
    private double pauseMarkEndDuration;

    private DateTimeStamp concurrentProcessNonStrongReferencesTimeStamp;
    private double concurrentProcessNonStrongReferencesDuration;

    private DateTimeStamp concurrentResetRelocationSetTimeStamp;
    private double concurrentResetRelocationSetDuration;

    private DateTimeStamp concurrentSelectRelocationSetTimeStamp;
    private double concurrentSelectRelocationSetDuration;

    private DateTimeStamp pauseRelocateStartTimeStamp;
    private double pauseMarkRelocateDuration;

    private DateTimeStamp concurrentRelocateTimeStamp;
    private double concurrentRelocateDuration;

    private double[] load = new double[3];
    private double[] mmu = new double[6];

    public DateTimeStamp getPauseMarkStartTimeStamp() {
        return pauseMarkStartTimeStamp;
    }

    public double getPauseMarkStartDuration() {
        return pauseMarkStartDuration;
    }

    public void setPauseMarkStart(DateTimeStamp pauseMarkStartTimeStamp, double duration) {
        this.pauseMarkStartTimeStamp = pauseMarkStartTimeStamp;
        this.pauseMarkStartDuration = duration;
    }

    public long getGcId() {
        return gcId;
    }

    public void setGcId(long gcId) {
        this.gcId = gcId;
    }

    public DateTimeStamp getConcurrentMarkTimeStamp() {
        return concurrentMarkTimeStamp;
    }

    public double getConcurrentMarkDuration() {
        return concurrentMarkDuration;
    }

    public void setConcurrentMark(DateTimeStamp concurrentMarkTimeStamp, double duration) {
        this.concurrentMarkTimeStamp = concurrentMarkTimeStamp;
        this.concurrentMarkDuration = duration;
    }

    public DateTimeStamp getConcurrentMarkFreeTimeStamp() {
        return concurrentMarkFreeTimeStamp;
    }

    public double getConcurrentMarkFreeDuration() {
        return concurrentMarkFreeDuration;
    }

    public void setConcurrentMarkFree(DateTimeStamp concurrentMarkFreeStart, double duration) {
        this.concurrentMarkFreeTimeStamp = concurrentMarkFreeStart;
        this.concurrentMarkFreeDuration = duration;
    }

    public DateTimeStamp getPauseMarkEndTimeStamp() {
        return pauseMarkEndTimeStamp;
    }

    public double getPauseMarkEndDuration() {
        return pauseMarkEndDuration;
    }

    public void setPauseMarkEnd(DateTimeStamp pauseMarkEndTimeStamp, double duration) {
        this.pauseMarkEndTimeStamp = pauseMarkEndTimeStamp;
        this.pauseMarkEndDuration = duration;
    }

    public DateTimeStamp getConcurrentProcessNonStrongReferencesTimeStamp() {
        return concurrentProcessNonStrongReferencesTimeStamp;
    }

    public double getConcurrentProcessNonStrongReferencesDuration() {
        return concurrentProcessNonStrongReferencesDuration;
    }

    public void setConcurrentProcessNonStrongReferences(DateTimeStamp concurrentProcessNonStrongReferencesTimeStamp, double duration) {
        this.concurrentProcessNonStrongReferencesTimeStamp = concurrentProcessNonStrongReferencesTimeStamp;
        this.concurrentProcessNonStrongReferencesDuration = duration;
    }

    public DateTimeStamp getConcurrentResetRelocationSetTimeStamp() {
        return concurrentResetRelocationSetTimeStamp;
    }

    public double getConcurrentResetRelocationSetDuration() {
        return concurrentResetRelocationSetDuration;
    }

    public void setConcurrentResetRelocationSet(DateTimeStamp concurrentResetRelocationSetTimeStamp, double duration) {
        this.concurrentResetRelocationSetTimeStamp = concurrentResetRelocationSetTimeStamp;
        this.concurrentResetRelocationSetDuration = duration;
    }

    public DateTimeStamp getConcurrentSelectRelocationSetTimeStamp() {
        return concurrentSelectRelocationSetTimeStamp;
    }

    public double getConcurrentSelectRelocationSetDuration() {
        return concurrentSelectRelocationSetDuration;
    }

    public void setConcurrentSelectRelocationSet(DateTimeStamp concurrentSelectRelocationSetTimeStamp, double duration) {
        this.concurrentSelectRelocationSetTimeStamp = concurrentSelectRelocationSetTimeStamp;
        this.concurrentSelectRelocationSetDuration = duration;
    }

    public DateTimeStamp getPauseRelocateStartTimeStamp() {
        return pauseRelocateStartTimeStamp;
    }

    public double getPauseRelocateStartDuration() {
        return pauseMarkRelocateDuration;
    }

    public void setPauseRelocateStart(DateTimeStamp pauseRelocateStartTimeStamp, double duration) {
        this.pauseRelocateStartTimeStamp = pauseRelocateStartTimeStamp;
        this.pauseMarkRelocateDuration = duration;
    }

    public DateTimeStamp getConcurrentRelocateTimeStamp() {
        return concurrentRelocateTimeStamp;
    }

    public double getConcurrentRelocateDuration() {
        return concurrentRelocateDuration;
    }

    public void setConcurrentRelocate(DateTimeStamp concurrentRelocateTimeStamp, double duration) {
        this.concurrentRelocateTimeStamp = concurrentRelocateTimeStamp;
        this.concurrentRelocateDuration = duration;
    }

    //Memory
    private ZGCMemoryPoolSummary markStart;
    private ZGCMemoryPoolSummary markEnd;
    private ZGCMemoryPoolSummary relocateStart;
    private ZGCMemoryPoolSummary relocateEnd;
    private OccupancySummary live;
    private OccupancySummary allocated;
    private OccupancySummary garbage;
    private ReclaimSummary reclaimed;
    private ReclaimSummary memorySummary;
    private ZGCMetaspaceSummary metaspace;

    public void setMarkStart(ZGCMemoryPoolSummary summary) {
        this.markStart = summary;
    }

    public void setMarkEnd(ZGCMemoryPoolSummary summary) {
        this.markEnd = summary;
    }

    public void setRelocateStart(ZGCMemoryPoolSummary summary) {
        this.relocateStart = summary;
    }

    public void setRelocateEnd(ZGCMemoryPoolSummary summary) {
        this.relocateEnd = summary;
    }

    public void setLive(OccupancySummary summary) {
        this.live = summary;
    }

    public void setAllocated(OccupancySummary summary) {
        this.allocated = summary;
    }

    public void setGarbage(OccupancySummary summary) {
        this.garbage = summary;
    }

    public void setReclaimed(ReclaimSummary summary) {
        this.reclaimed = summary;
    }

    public void setMemorySummary(ReclaimSummary summary) {
        this.memorySummary = summary;
    }

    public void setMetaspace(ZGCMetaspaceSummary summary) {
        this.metaspace = summary;
    }

    public ZGCMemoryPoolSummary getMarkStart() {
        return markStart;
    }

    public ZGCMemoryPoolSummary getMarkEnd() {
        return markEnd;
    }

    public ZGCMemoryPoolSummary getRelocateStart() {
        return relocateStart;
    }

    public ZGCMemoryPoolSummary getRelocateEnd() {
        return relocateEnd;
    }

    public OccupancySummary getLive() {
        return live;
    }

    public OccupancySummary getAllocated() {
        return allocated;
    }

    public OccupancySummary getGarbage() {
        return garbage;
    }

    public ReclaimSummary getReclaimed() {
        return reclaimed;
    }

    public ReclaimSummary getMemorySummary() {
        return memorySummary;
    }

    public ZGCMetaspaceSummary getMetaspace() {
        return metaspace;
    }

    public void setLoadAverages(double[] load) {
        this.load = load;
    }
    public double getLoadAverageAt(int time) {
        switch (time) {
            case 1:
                return load[0];
            case 5:
                return load[1];
            case 15:
                return load[2];
            default:
                return 0.0d;
        }
    }

    public void setMMU(double[] mmu) {
        this.mmu = mmu;
    }

    public double getMMU(int percentage) {
        switch (percentage) {
            case 2:
                return mmu[0];
            case 5:
                return mmu[1];
            case 10:
                return mmu[2];
            case 20:
                return mmu[3];
            case 50:
                return mmu[4];
            case 100:
                return mmu[5];
            default:
                return 0.0d;
        }
    }
}

// Concurrent Mark duration
// Pause mark end duration
// Concurrent reference processing duration
// Concurrent reset relocation set duration
// Concurrent select relocation set duration
// Pause relocate start
// Load
// MMU
// Concurrent relocate
// Relocation volume
// NMethods, registered, unregistered
// Metaspace used, capacity committed, reserved
// Soft, weak, final, phantom.. encountered, discovered, enqueued
// Memory stats

/*
[32.121s][info][gc,start    ] GC(2) Garbage Collection (Metadata GC Threshold)
[32.121s][info][gc,phases   ] GC(2) Pause Mark Start 0.023ms
[32.166s][info][gc,phases   ] GC(2) Concurrent Mark 44.623ms
[32.166s][info][gc,phases   ] GC(2) Pause Mark End 0.029ms
[32.166s][info][gc,phases   ] GC(2) Concurrent Mark Free 0.001ms
[32.172s][info][gc,phases   ] GC(2) Concurrent Process Non-Strong References 5.797ms
[32.172s][info][gc,phases   ] GC(2) Concurrent Reset Relocation Set 0.012ms
[32.178s][info][gc,phases   ] GC(2) Concurrent Select Relocation Set 6.446ms
[32.179s][info][gc,phases   ] GC(2) Pause Relocate Start 0.024ms
[32.193s][info][gc,phases   ] GC(2) Concurrent Relocate 14.013ms
[32.193s][info][gc,load     ] GC(2) Load: 7.28/6.63/5.01
[32.193s][info][gc,mmu      ] GC(2) MMU: 2ms/98.2%, 5ms/99.3%, 10ms/99.5%, 20ms/99.7%, 50ms/99.9%, 100ms/99.9%
[32.193s][info][gc,marking  ] GC(2) Mark: 4 stripe(s), 3 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s)
[32.193s][info][gc,marking  ] GC(2) Mark Stack Usage: 32M
[32.193s][info][gc,metaspace] GC(2) Metaspace: 60M used, 60M committed, 1080M reserved
[32.193s][info][gc,ref      ] GC(2) Soft: 5447 encountered, 0 discovered, 0 enqueued
[32.193s][info][gc,ref      ] GC(2) Weak: 5347 encountered, 2016 discovered, 810 enqueued
[32.193s][info][gc,ref      ] GC(2) Final: 1041 encountered, 113 discovered, 105 enqueued
[32.193s][info][gc,ref      ] GC(2) Phantom: 558 encountered, 501 discovered, 364 enqueued
[32.193s][info][gc,reloc    ] GC(2) Small Pages: 235 / 470M, Empty: 32M, Relocated: 40M, In-Place: 0
[32.193s][info][gc,reloc    ] GC(2) Medium Pages: 2 / 64M, Empty: 0M, Relocated: 3M, In-Place: 0
[32.193s][info][gc,reloc    ] GC(2) Large Pages: 3 / 24M, Empty: 8M, Relocated: 0M, In-Place: 0
[32.193s][info][gc,reloc    ] GC(2) Forwarding Usage: 13M
[32.193s][info][gc,heap     ] GC(2) Min Capacity: 8M(0%)
[32.193s][info][gc,heap     ] GC(2) Max Capacity: 28686M(100%)
[32.193s][info][gc,heap     ] GC(2) Soft Max Capacity: 28686M(100%)
[32.193s][info][gc,heap     ] GC(2)                Mark Start          Mark End        Relocate Start      Relocate End           High               Low
[32.193s][info][gc,heap     ] GC(2)  Capacity:     1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)
[32.193s][info][gc,heap     ] GC(2)      Free:    28128M (98%)       28110M (98%)       28148M (98%)       28560M (100%)      28560M (100%)      28108M (98%)
[32.193s][info][gc,heap     ] GC(2)      Used:      558M (2%)          576M (2%)          538M (2%)          126M (0%)          578M (2%)          126M (0%)
[32.193s][info][gc,heap     ] GC(2)      Live:         -                71M (0%)           71M (0%)           71M (0%)             -                  -
[32.193s][info][gc,heap     ] GC(2) Allocated:         -                18M (0%)           20M (0%)           18M (0%)             -                  -
[32.193s][info][gc,heap     ] GC(2)   Garbage:         -               486M (2%)          446M (2%)           35M (0%)             -                  -
[32.193s][info][gc,heap     ] GC(2) Reclaimed:         -                  -                40M (0%)          450M (2%)             -                  -
[32.193s][info][gc          ] GC(2) Garbage Collection (Metadata GC Threshold) 558M(2%)->126M(0%)
*/
