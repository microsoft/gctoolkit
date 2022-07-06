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

    private DateTimeStamp pauseMarkStartTimeStamp;
    private double pauseMarkStartDuration;

    private DateTimeStamp concurrentMarkTimeStamp;
    private double concurrentMarkDuration;

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
    private ZGCMemoryPoolSummary metaspace;

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

    public void setMetaspace(ZGCMemoryPoolSummary summary) {
        this.metaspace = summary;
    }
    public ZGCMemoryPoolSummary getMetaspace() {
        return metaspace;
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
"[3.558s][info ][gc,start       ] GC(3) Garbage Collection (Warmup)",
        "[3.559s][info ][gc,phases      ] GC(3) Pause Mark Start 0.460ms",
        "[3.573s][info ][gc,phases      ] GC(3) Concurrent Mark 14.621ms",
        "[3.574s][info ][gc,phases      ] GC(3) Pause Mark End 0.830ms",
        "[3.578s][info ][gc,phases      ] GC(3) Concurrent Process Non-Strong References 3.654ms",
        "[3.578s][info ][gc,phases      ] GC(3) Concurrent Reset Relocation Set 0.194ms",
        "[3.582s][info ][gc,phases      ] GC(3) Concurrent Select Relocation Set 3.193ms",
        "[3.583s][info ][gc,phases      ] GC(3) Pause Relocate Start 0.794ms",
        "[3.596s][info ][gc,phases      ] GC(3) Concurrent Relocate 12.962ms",
        "[3.596s][info ][gc,load        ] GC(3) Load: 4.28/3.95/3.22",
        "[3.596s][info ][gc,mmu         ] GC(3) MMU: 2ms/32.7%, 5ms/60.8%, 10ms/80.4%, 20ms/85.4%, 50ms/90.8%, 100ms/95.4%",
        "[3.596s][info ][gc,marking     ] GC(3) Mark: 1 stripe(s), 2 proactive flush(es), 1 terminate flush(es), 1 completion(s), 0 continuation(s)",
        "[3.596s][info ][gc,reloc       ] GC(3) Relocation: Successful, 6M relocated",
        "[3.596s][info ][gc,nmethod     ] GC(3) NMethods: 1163 registered, 0 unregistered",
        "[3.596s][info ][gc,metaspace   ] GC(3) Metaspace: 14M used, 15M capacity, 15M committed, 16M reserved",
        "[3.596s][info ][gc,ref         ] GC(3) Soft: 391 encountered, 0 discovered, 0 enqueued",
        "[3.596s][info ][gc,ref         ] GC(3) Weak: 587 encountered, 466 discovered, 0 enqueued",
        "[3.596s][info ][gc,ref         ] GC(3) Final: 799 encountered, 0 discovered, 0 enqueued",
        "[3.596s][info ][gc,ref         ] GC(3) Phantom: 33 encountered, 1 discovered, 0 enqueued",
        "[3.596s][info ][gc,heap        ] GC(3) Min Capacity: 8M(0%)",
        "[3.596s][info ][gc,heap        ] GC(3) Max Capacity: 4096M(100%)",
        "[3.596s][info ][gc,heap        ] GC(3) Soft Max Capacity: 4096M(100%)",
        "[3.596s][info ][gc,heap        ] GC(3)                Mark Start          Mark End        Relocate Start      Relocate End           High               Low",
        "[3.596s][info ][gc,heap        ] GC(3)  Capacity:      936M (23%)        1074M (26%)        1074M (26%)        1074M (26%)        1074M (26%)         936M (23%)",
        "[3.596s][info ][gc,heap        ] GC(3)   Reserve:       42M (1%)           42M (1%)           42M (1%)           42M (1%)           42M (1%)           42M (1%)",
        "[3.596s][info ][gc,heap        ] GC(3)      Free:     3160M (77%)        3084M (75%)        3852M (94%)        3868M (94%)        3930M (96%)        3022M (74%)",
        "[3.596s][info ][gc,heap        ] GC(3)      Used:      894M (22%)         970M (24%)         202M (5%)          186M (5%)         1032M (25%)         124M (3%)",
        "[3.596s][info ][gc,heap        ] GC(3)      Live:         -                 8M (0%)            8M (0%)            8M (0%)             -                  -",
        "[3.596s][info ][gc,heap        ] GC(3) Allocated:         -               172M (4%)          172M (4%)          376M (9%)             -                  -",
        "[3.596s][info ][gc,heap        ] GC(3)   Garbage:         -               885M (22%)         117M (3%)            5M (0%)             -                  -",
        "[3.596s][info ][gc,heap        ] GC(3) Reclaimed:         -                  -               768M (19%)         880M (21%)            -                  -",
        "[3.596s][info ][gc             ] GC(3) Garbage Collection (Warmup) 894M(22%)->186M(5%)"

 */
