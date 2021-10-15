// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

import java.util.Map;

import static com.microsoft.gctoolkit.event.GCCause.*;

public class GCCauses {
    private static final Map<String, GCCause> GC_CAUSES = Map.ofEntries(
        Map.entry(GCCause.JAVA_LANG_SYSTEM.getLabel(), GCCause.JAVA_LANG_SYSTEM),
        Map.entry("System", GCCause.JAVA_LANG_SYSTEM),
        Map.entry(GCCause.DIAGNOSTIC_COMMAND.getLabel(), GCCause.JAVA_LANG_SYSTEM),
        Map.entry(GCCause.FULL_GC_ALOT.getLabel(), GCCause.FULL_GC_ALOT),
        Map.entry(GCCause.SCAVENGE_ALOT.getLabel(), GCCause.SCAVENGE_ALOT),
        Map.entry(GCCause.ALLOCATION_PROFILER.getLabel(), GCCause.ALLOCATION_PROFILER),
        Map.entry(GCCause.JVMTI_FORCE_GC.getLabel(), GCCause.JVMTI_FORCE_GC),
        Map.entry(GCCause.GC_LOCKER.getLabel(), GCCause.GC_LOCKER),
        Map.entry(GCCause.HEAP_INSPECTION.getLabel(), GCCause.HEAP_INSPECTION),
        Map.entry(GCCause.HEAP_DUMP.getLabel(), GCCause.HEAP_DUMP),
        Map.entry(GCCause.NO_GC.getLabel(), GCCause.NO_GC),
        Map.entry(GCCause.ALLOCATION_FAILURE.getLabel(), GCCause.ALLOCATION_FAILURE),
        Map.entry(GCCause.TENURED_GENERATION_FULL.getLabel(), GCCause.TENURED_GENERATION_FULL),
        Map.entry(GCCause.METADATA_GENERATION_THRESHOLD.getLabel(), GCCause.METADATA_GENERATION_THRESHOLD),
        Map.entry(GCCause.PERMANENT_GENERATION_FULL.getLabel(), GCCause.PERMANENT_GENERATION_FULL),
        Map.entry(GCCause.CMS_GENERATION_FULL.getLabel(), GCCause.CMS_GENERATION_FULL),
        Map.entry(GCCause.CMS_INITIAL_MARK.getLabel(), GCCause.CMS_INITIAL_MARK),
        Map.entry(GCCause.CMS_FINAL_REMARK.getLabel(), GCCause.CMS_FINAL_REMARK),
        Map.entry(GCCause.CMS_CONCURRENT_MARK.getLabel(), GCCause.CMS_CONCURRENT_MARK),
        Map.entry(GCCause.OLD_GENERATION_EXPANDED_ON_LAST_SCAVENGE.getLabel(), GCCause.OLD_GENERATION_EXPANDED_ON_LAST_SCAVENGE),
        Map.entry(GCCause.OLD_GENERATION_TOO_FULL_TO_SCAVENGE.getLabel(), GCCause.OLD_GENERATION_TOO_FULL_TO_SCAVENGE),
        Map.entry(GCCause.ADAPTIVE_SIZE_POLICY.getLabel(), GCCause.ADAPTIVE_SIZE_POLICY),
        Map.entry(GCCause.G1_EVACUATION_PAUSE.getLabel(), GCCause.G1_EVACUATION_PAUSE),
        Map.entry(GCCause.G1_HUMONGOUS_ALLOCATION.getLabel(), GCCause.G1_HUMONGOUS_ALLOCATION),
        Map.entry(GCCause.LAST_DITCH_COLLECTION.getLabel(), GCCause.LAST_DITCH_COLLECTION),
        Map.entry(GCCause.UNKNOWN_GCCAUSE.getLabel(), GCCause.UNKNOWN_GCCAUSE),
        Map.entry(GCCause.GCCAUSE_NOT_SET.getLabel(), GCCause.GCCAUSE_NOT_SET),
        Map.entry(GCCause.CMS_FAILURE.getLabel(), GCCause.CMS_FAILURE),
        Map.entry(GCCause.LAST_GC_CAUSE.getLabel(), GCCause.LAST_GC_CAUSE),
        Map.entry(GCCause.PROMOTION_FAILED.getLabel(), GCCause.PROMOTION_FAILED),
        Map.entry(GCCause.UPDATE_ALLOCATION_CONTEXT_STATS.getLabel(), GCCause.UPDATE_ALLOCATION_CONTEXT_STATS),
        // Additional GCCauses not found in gcause.cp
        Map.entry(GCCause.CONCURRENT_MARK_STACK_OVERFLOW.getLabel(), GCCause.CONCURRENT_MARK_STACK_OVERFLOW),
        Map.entry(GCCause.G1GC_YOUNG.getLabel(), GCCause.G1GC_YOUNG),
        //JDK 11
        Map.entry(WHITEBOX_YOUNG.getLabel(), WHITEBOX_YOUNG),
        Map.entry(WHITEBOX_CONCURRENT_MARK.getLabel(), WHITEBOX_CONCURRENT_MARK),
        Map.entry(WHITEBOX_FULL.getLabel(), WHITEBOX_FULL),
        Map.entry(META_CLEAR_SOFT_REF.getLabel(), META_CLEAR_SOFT_REF),
        Map.entry(TIMER.getLabel(), TIMER),
        Map.entry(WARMUP.getLabel(), WARMUP),
        Map.entry(ALLOC_RATE.getLabel(), ALLOC_RATE),
        Map.entry(ALLOC_STALL.getLabel(), ALLOC_STALL),
        Map.entry(PROACTIVE.getLabel(), PROACTIVE),
        Map.entry(GCCause.PREVENTIVE.getLabel(),PREVENTIVE));

    public static GCCause get(String gcCauseName) {

        GCCause cause;
        if (gcCauseName == null) {
            cause = GCCause.GCCAUSE_NOT_SET;
        } else {
            String lookup = gcCauseName.trim();
            cause = GC_CAUSES.get(lookup.substring(1, lookup.length() - 1));
            if (cause == null) {
                cause = GCCause.GCCAUSE_NOT_SET;
            }
        }

        return cause;
    }
}

/*
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
    G1_HUMONGOUS_ALLOCATION("G1 Humongous Allocation"),
    LAST_DITCH_COLLECTION("Last ditch collection"),
    LAST_GC_CAUSE("ILLEGAL VALUE - last gc cause - ILLEGAL VALUE"),
    UNKNOWN_GCCAUSE("unknown GCCause"),
    PROMOTION_FAILED("promotion failed"),
    UPDATE_ALLOCATION_CONTEXT_STATS("Update Allocation Context Stats"),
    GCCAUSE_NOT_SET("Missing GC Cause"),

    //JDK 11
    WHITEBOX_YOUNG("WhiteBox Initiated Young GC"),
    WHITEBOX_CONCURRENT_MARK("WhiteBox Initiated Concurrent Mark"),
    WHITEBOX_FULL("WhiteBox Initiated Full GC"),
    META_CLEAR_SOFT_REF("Metadata GC Clear Soft References"),
    TIMER("Timer"),
    WARMUP("Warmup"),
    ALLOC_RATE("Allocation Rate"),
    ALLOC_STALL("Allocation Stall"),
    PROACTIVE("Proactive");
    //JDK 17
    PREVENTATIVE("G1 Preventive Collection")
 */
