// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

/**
 * Representation of GC Collection Events
 */
public enum GarbageCollectionTypes implements LabelledGCEventType {

    GC("GC"),
    Young("Young"),
    FullGC("Full GC"),
    Full("FUll GC"),
    SystemGC("System.gc()"),
    PSYoungGen("PSYoungGen"),
    ScavengeBeforeFull("PSYoungGen (System)"),
    PSFull("PSFull"),
    DefNew("DefNew"),
    ParNew("ParNew"),
    ParNewPromotionFailed("ParNew (promotion failed)"),
    ConcurrentModeFailure("Concurrent mode failure"),
    ConcurrentModeInterrupted("Concurrent mode interrupted"),
    InitialMark("Initial mark"),
    Initial_Mark("Initial Mark"),
    Remark("Remark"),
    ConcurrentPhase("CMS Concurrent Phase"),
    CMSPausePhase("CMS Pause Phase"),
    ConcurrentMark("Concurrent mark"),
    Concurrent_Mark("Mark"),
    Concurrent_Preclean("Concurrent preclean"),
    //G1ConcurrentPreclean("Preclean"),
    Abortable_Preclean("Concurrent abortable preclean"),
    Concurrent_Sweep("Concurrent sweep"),
    Concurrent_Reset("Concurrent reset"),
    Mixed("Mixed"),
    //G1GCFull("G1GC Full"),
    Concurrent_Cycle("Concurrent Cycle"),
    ConcurrentClearClaimedMarks("Clear Claimed Marks"),
    ConcurrentScanRootRegions("Scan Root Regions"),
    //ConcurrentMarkFromRoots("Mark From Roots"),
    ConcurrentCreateLiveData("Create Live Data"),
    ConcurrentCompleteCleanup("Complete Cleanup"),
    ConcurrentCleanupForNextMark("Cleanup for Next Mark"),
    ConcurrentStringDeduplication("G1GC Concurrent String deduplication"),
    ConcurrentRootRegionScan("G1GC Root Region Scan"),
    G1GCConcurrentUndoCycle("Concurrent Undo Cycle"),
    G1GCRemark("G1GC Remark"),
    G1GCCleanup("G1GC Cleanup"),
    G1GCConcurrentCleanup("G1GC concurrent cleanup"),
    G1ConcurrentMarkResetForOverflow("G1GC Concurrent Mark Reset for Overflow"),
    G1ConcurrentRebuildRememberedSets("Rebuild Remembered Sets"),
    G1GCYoungInitialMark("G1GC Young Initial mark"),
    G1GCMixedInitialMark("G1GC Mixed Initial mark"),
    G1GCConcurrentMark("G1GC Concurrent mark"),
    G1GCFull("Full"),
    G1Trap("G1GC Trap"),
    Unknown("Unknown"),
    ZGCFull("ZGC FULL"),
    ZGCMajor("ZGC Major"),
    ZGCMinor("ZGC Minor"),
    Shenandoah("Shenandoah");

    private final String label;

    GarbageCollectionTypes(String label) {
        this.label = label;
    }

    public static GarbageCollectionTypes fromLabel(String label) {
        return LabelledGCEventType.fromLabel(GarbageCollectionTypes.class, label);
    }

    public String getLabel() {
        return label;
    }
}
