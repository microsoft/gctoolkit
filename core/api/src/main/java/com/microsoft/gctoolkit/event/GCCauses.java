// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

import java.util.HashMap;

import static com.microsoft.gctoolkit.event.GCCause.*;

public class GCCauses {

    private final static HashMap<String, GCCause> GC_CAUSES;

    static {
        GC_CAUSES = new HashMap<String, GCCause>(50);

        GC_CAUSES.put(GCCause.JAVA_LANG_SYSTEM.getLabel(), GCCause.JAVA_LANG_SYSTEM);
        GC_CAUSES.put("System", GCCause.JAVA_LANG_SYSTEM);
        GC_CAUSES.put(GCCause.DIAGNOSTIC_COMMAND.getLabel(), GCCause.JAVA_LANG_SYSTEM);
        GC_CAUSES.put(GCCause.FULL_GC_ALOT.getLabel(), GCCause.FULL_GC_ALOT);
        GC_CAUSES.put(GCCause.SCAVENGE_ALOT.getLabel(), GCCause.SCAVENGE_ALOT);
        GC_CAUSES.put(GCCause.ALLOCATION_PROFILER.getLabel(), GCCause.ALLOCATION_PROFILER);
        GC_CAUSES.put(GCCause.JVMTI_FORCE_GC.getLabel(), GCCause.JVMTI_FORCE_GC);
        GC_CAUSES.put(GCCause.GC_LOCKER.getLabel(), GCCause.GC_LOCKER);
        GC_CAUSES.put(GCCause.HEAP_INSPECTION.getLabel(), GCCause.HEAP_INSPECTION);
        GC_CAUSES.put(GCCause.HEAP_DUMP.getLabel(), GCCause.HEAP_DUMP);
        GC_CAUSES.put(GCCause.NO_GC.getLabel(), GCCause.NO_GC);
        GC_CAUSES.put(GCCause.ALLOCATION_FAILURE.getLabel(), GCCause.ALLOCATION_FAILURE);
        GC_CAUSES.put(GCCause.TENURED_GENERATION_FULL.getLabel(), GCCause.TENURED_GENERATION_FULL);
        GC_CAUSES.put(GCCause.METADATA_GENERATION_THRESHOLD.getLabel(), GCCause.METADATA_GENERATION_THRESHOLD);
        GC_CAUSES.put(GCCause.PERMANENT_GENERATION_FULL.getLabel(), GCCause.PERMANENT_GENERATION_FULL);
        GC_CAUSES.put(GCCause.CMS_GENERATION_FULL.getLabel(), GCCause.CMS_GENERATION_FULL);
        GC_CAUSES.put(GCCause.CMS_INITIAL_MARK.getLabel(), GCCause.CMS_INITIAL_MARK);
        GC_CAUSES.put(GCCause.CMS_FINAL_REMARK.getLabel(), GCCause.CMS_FINAL_REMARK);
        GC_CAUSES.put(GCCause.CMS_CONCURRENT_MARK.getLabel(), GCCause.CMS_CONCURRENT_MARK);
        GC_CAUSES.put(GCCause.OLD_GENERATION_EXPANDED_ON_LAST_SCAVENGE.getLabel(), GCCause.OLD_GENERATION_EXPANDED_ON_LAST_SCAVENGE);
        GC_CAUSES.put(GCCause.OLD_GENERATION_TOO_FULL_TO_SCAVENGE.getLabel(), GCCause.OLD_GENERATION_TOO_FULL_TO_SCAVENGE);
        GC_CAUSES.put(GCCause.ADAPTIVE_SIZE_POLICY.getLabel(), GCCause.ADAPTIVE_SIZE_POLICY);
        GC_CAUSES.put(GCCause.G1_EVACUATION_PAUSE.getLabel(), GCCause.G1_EVACUATION_PAUSE);
        GC_CAUSES.put(GCCause.G1_HUMONGOUS_ALLOCATION.getLabel(), GCCause.G1_HUMONGOUS_ALLOCATION);
        GC_CAUSES.put(GCCause.LAST_DITCH_COLLECTION.getLabel(), GCCause.LAST_DITCH_COLLECTION);
        GC_CAUSES.put(GCCause.LAST_GC_CAUSE.getLabel(), GCCause.LAST_GC_CAUSE);
        GC_CAUSES.put(GCCause.UNKNOWN_GCCAUSE.getLabel(), GCCause.UNKNOWN_GCCAUSE);
        GC_CAUSES.put(GCCause.GCCAUSE_NOT_SET.getLabel(), GCCause.GCCAUSE_NOT_SET);
        GC_CAUSES.put(GCCause.CMS_FAILURE.getLabel(), GCCause.CMS_FAILURE);
        GC_CAUSES.put(GCCause.LAST_GC_CAUSE.getLabel(), GCCause.LAST_GC_CAUSE);
        GC_CAUSES.put(GCCause.PROMOTION_FAILED.getLabel(), GCCause.PROMOTION_FAILED);
        GC_CAUSES.put(GCCause.UPDATE_ALLOCATION_CONTEXT_STATS.getLabel(), GCCause.UPDATE_ALLOCATION_CONTEXT_STATS);
        // Additional GCCauses not found in gcause.cpp
        GC_CAUSES.put(GCCause.CONCURRENT_MARK_STACK_OVERFLOW.getLabel(), GCCause.CONCURRENT_MARK_STACK_OVERFLOW);
        GC_CAUSES.put(GCCause.G1GC_YOUNG.getLabel(), GCCause.G1GC_YOUNG);
        //JDK 11+
        GC_CAUSES.put(WHITEBOX_YOUNG.getLabel(), WHITEBOX_YOUNG);
        GC_CAUSES.put(WHITEBOX_CONCURRENT_MARK.getLabel(), WHITEBOX_CONCURRENT_MARK);
        GC_CAUSES.put(WHITEBOX_FULL.getLabel(), WHITEBOX_FULL);
        GC_CAUSES.put(META_CLEAR_SOFT_REF.getLabel(), META_CLEAR_SOFT_REF);
        GC_CAUSES.put(TIMER.getLabel(), TIMER);
        GC_CAUSES.put(WARMUP.getLabel(), WARMUP);
        GC_CAUSES.put(ALLOC_RATE.getLabel(), ALLOC_RATE);
        GC_CAUSES.put(ALLOC_STALL.getLabel(), ALLOC_STALL);
        GC_CAUSES.put(PROACTIVE.getLabel(), PROACTIVE);
    }

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
 */
