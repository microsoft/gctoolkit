// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

/**
 * There are causes for Garbage Collection, this is a representation of the
 * causes that a GC log file can hold.
 */
public enum GCCause {
    JAVA_LANG_SYSTEM("System.gc()"),
    DIAGNOSTIC_COMMAND("Diagnostic Command"),
    FULL_GC_ALOT("FullGCALot"),
    SCAVENGE_ALOT("ScavengeAlot"),
    ALLOCATION_PROFILER("Allocation Profiler"),
    JVMTI_FORCE_GC("JvmtiEnv ForceGarbageCollection"),
    GC_LOCKER("GCLocker Initiated GC"),
    HEAP_INSPECTION("Heap Inspection Initiated GC"),
    HEAP_DUMP("Heap Dump Initiated GC"),
    NO_GC("No GC"),
    ALLOCATION_FAILURE("Allocation Failure"),
    TENURED_GENERATION_FULL("Tenured Generation Full"),
    METADATA_GENERATION_THRESHOLD("Metadata GC Threshold"),
    PERMANENT_GENERATION_FULL("Permanent Generation Full"),
    CMS_GENERATION_FULL("CMS Generation Full"),
    CMS_INITIAL_MARK("CMS Initial Mark"),
    CMS_FINAL_REMARK("CMS Final Remark"),
    CMS_CONCURRENT_MARK("CMS Concurrent Mark"),
    CMS_FAILURE("CMS Failure"),
    OLD_GENERATION_EXPANDED_ON_LAST_SCAVENGE("Old Generation Expanded On Last Scavenge"),
    OLD_GENERATION_TOO_FULL_TO_SCAVENGE("Old Generation Too Full To Scavenge"),
    ADAPTIVE_SIZE_POLICY("Ergonomics"),
    G1_EVACUATION_PAUSE("G1 Evacuation Pause"),
    G1_COMPACTION_PAUSE("G1 Compaction Pause"),
    G1_HUMONGOUS_ALLOCATION("G1 Humongous Allocation"),
    G1_PERIODIC_COLLECTION("G1 Periodic Collection"),
    LAST_DITCH_COLLECTION("Last ditch collection"),
    LAST_GC_CAUSE("ILLEGAL VALUE - last gc cause - ILLEGAL VALUE"),
    UNKNOWN_GCCAUSE("unknown GCCause"),
    PROMOTION_FAILED("promotion failed"),
    UPDATE_ALLOCATION_CONTEXT_STATS("Update Allocation Context Stats"),
    GCCAUSE_NOT_SET("Missing GC Cause"),
    // Additional GCCauses not found in gcause.cpp
    CONCURRENT_MARK_STACK_OVERFLOW("Concurrent Mark Stack Overflow"),
    G1GC_YOUNG("young"),

    //JDK 11
    WHITEBOX_YOUNG("WhiteBox Initiated Young GC"),
    WHITEBOX_CONCURRENT_MARK("WhiteBox Initiated Concurrent Mark"),
    WHITEBOX_FULL("WhiteBox Initiated Full GC"),
    WHITEBOX_RUN_TO_BREAKPOINT("WhiteBox Initiated Run to Breakpoint"),
    META_CLEAR_SOFT_REF("Metadata GC Clear Soft References"),
    PREVENTIVE("G1 Preventive Collection"),
    CODE_CACHE_THRESHOLD("CodeCache GC Threshold"),
    CODE_CACHE_AGGRESSIVE("CodeCache GC Aggressive"),

    // Shenandoah
    ALLOCATION_FAILURE_EVAC("Allocation Failure During Evacuation"),
    STOP_VM("Stopping VM"),
    CONCURRENT_GC("Concurrent GC"),
    UPGRADE_TO_FULL_GC("Upgrade To Full GC"),

    // ZGC Specific
    TIMER("Timer"),
    WARMUP("Warmup"),
    ALLOC_RATE("Allocation Rate"),
    ALLOC_STALL("Allocation Stall"),
    PROACTIVE("Proactive"),
    HIGH_USAGE("High Usage");

    private final String label;

    GCCause(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
