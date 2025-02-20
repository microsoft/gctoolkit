// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GCEvent;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class FullZGCCycle extends GCEvent {
    private ZGCCycle delegate;

    public FullZGCCycle(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, gcType, cause, duration);
    }

    public FullZGCCycle(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

    public FullZGCCycle(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, cause, duration);
    }

    public FullZGCCycle(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, double duration) {
        super(timeStamp, gcType, duration);
    }

    public void setZGCCycle(ZGCCycle zgcCycle) {
        this.delegate = zgcCycle;
    }

    public ZGCMarkSummary getMarkSummary() {
        return delegate.getMarkSummary();
    }

    public DateTimeStamp getPauseMarkStartTimeStamp() {
        return delegate.getPauseMarkStartTimeStamp();
    }

    public double getPauseMarkStartDuration() {
        return delegate.getPauseMarkStartDuration();
    }

    public long getGcId() {
        return delegate.getGcId();
    }


    public DateTimeStamp getConcurrentMarkTimeStamp() {
        return delegate.getConcurrentMarkTimeStamp();
    }

    public double getConcurrentMarkDuration() {
        return delegate.getConcurrentMarkDuration();
    }

    public DateTimeStamp getConcurrentMarkFreeTimeStamp() {
        return delegate.getConcurrentMarkFreeTimeStamp();
    }

    public double getConcurrentMarkFreeDuration() {
        return delegate.getConcurrentMarkFreeDuration();
    }

    public DateTimeStamp getPauseMarkEndTimeStamp() {
        return delegate.getPauseMarkEndTimeStamp();
    }

    public double getPauseMarkEndDuration() {
        return delegate.getPauseMarkEndDuration();
    }


    public DateTimeStamp getConcurrentProcessNonStrongReferencesTimeStamp() {
        return delegate.getConcurrentProcessNonStrongReferencesTimeStamp();
    }

    public double getConcurrentProcessNonStrongReferencesDuration() {
        return delegate.getConcurrentProcessNonStrongReferencesDuration();
    }

    public DateTimeStamp getConcurrentResetRelocationSetTimeStamp() {
        return delegate.getConcurrentResetRelocationSetTimeStamp();
    }

    public double getConcurrentResetRelocationSetDuration() {
        return delegate.getConcurrentResetRelocationSetDuration();
    }

    public DateTimeStamp getConcurrentSelectRelocationSetTimeStamp() {
        return delegate.getConcurrentSelectRelocationSetTimeStamp();
    }

    public double getConcurrentSelectRelocationSetDuration() {
        return delegate.getConcurrentSelectRelocationSetDuration();
    }

    public DateTimeStamp getPauseRelocateStartTimeStamp() {
        return delegate.getPauseRelocateStartTimeStamp();
    }

    public double getPauseRelocateStartDuration() {
        return delegate.getPauseRelocateStartDuration();
    }

    public DateTimeStamp getConcurrentRelocateTimeStamp() {
        return delegate.getConcurrentRelocateTimeStamp();
    }

    public double getConcurrentRelocateDuration() {
        return delegate.getConcurrentRelocateDuration();
    }

    public ZGCMemoryPoolSummary getMarkStart() {
        return delegate.getMarkStart();
    }

    public ZGCMemoryPoolSummary getMarkEnd() {
        return delegate.getMarkEnd();
    }

    public ZGCMemoryPoolSummary getRelocateStart() {
        return delegate.getRelocateStart();
    }

    public ZGCMemoryPoolSummary getRelocateEnd() {
        return delegate.getRelocateEnd();
    }

    public ZGCLiveSummary getLive() {
        return delegate.getLiveSummary();
    }

    public ZGCAllocatedSummary getAllocated() {
        return delegate.getAllocatedSummary();
    }

    public ZGCGarbageSummary getGarbage() {
        return delegate.getGarbageSummary();
    }

    public ZGCReclaimSummary getReclaimed() {
        return delegate.getReclaimSummary();
    }

    public ZGCMemorySummary getMemorySummary() {
        return delegate.getMemorySummary();
    }

    public ZGCMetaspaceSummary getMetaspace() {
        return delegate.getMetaspaceSummary();
    }

    public double getLoadAverageAt(int time) {
        return delegate.getLoadAverageAt(time);
    }

    public double getMMU(int percentage) {
        return delegate.getMMU(percentage);
    }

    public ZGCPromotedSummary getPromotedSummary() {
        return delegate.getPromotedSummary();
    }

    public ZGCCompactedSummary getCompactedSummary() {
        return delegate.getCompactedSummary();
    }

    public OccupancySummary getUsedOccupancySummary() {
        return delegate.getUsedOccupancySummary();
    }

    public ZGCCollectionType getType() {
        return delegate.getType();
    }

    public DateTimeStamp getMarkRootsStart() {
        return delegate.getMarkRootsStart();
    }

    public double getMarkRootsDuration() {
        return delegate.getMarkRootsDuration();
    }

    public DateTimeStamp getMarkFollowStart() {
        return delegate.getMarkFollowStart();
    }

    public double getMarkFollowDuration() {
        return delegate.getMarkFollowDuration();
    }

    public DateTimeStamp getRemapRootColoredStart() {
        return delegate.getRemapRootColoredStart();
    }

    public double getRemapRootsColoredDuration() {
        return delegate.getRemapRootsColoredDuration();
    }

    public DateTimeStamp getRemapRootsUncoloredStart() {
        return delegate.getRemapRootsUncoloredStart();
    }

    public double getRemapRootsUncoloredDuration() {
        return delegate.getRemapRootsUncoloredDuration();
    }

    public DateTimeStamp getRemapRememberedStart() {
        return delegate.getRemapRememberedStart();
    }

    public double getRemapRememberedDuration() {
        return delegate.getRemapRememberedDuration();
    }

    public double getPauseMarkRelocateDuration() {
        return delegate.getPauseMarkRelocateDuration();
    }

    public DateTimeStamp getConcurrentMarkContinueTimeStamp() {
        return delegate.getConcurrentMarkContinueTimeStamp();
    }

    public double getConcurrentMarkContinueDuration() {
        return delegate.getConcurrentMarkContinueDuration();
    }

    public DateTimeStamp getRemapRootsStart() {
        return delegate.getConcurrentRemapRootsStart();
    }

    public double getRemapRootsDuration() {
        return delegate.getConcurrentRemapRootsDuration();
    }

    public double[] getLoad() {
        return delegate.getLoad();
    }

    public double[] getMmu() {
        return delegate.getMmu();
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
