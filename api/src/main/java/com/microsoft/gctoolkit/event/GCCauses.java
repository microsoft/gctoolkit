// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class GCCauses {
    private static final Map<String, GCCause> GC_CAUSES = Arrays.stream(GCCause.values()).collect(toMap(GCCause::getLabel, Function.identity()));

    // Add additional lookup for system.gc()
    static {
        GC_CAUSES.put("System", GCCause.JAVA_LANG_SYSTEM);
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
    //JDK 17
    PREVENTATIVE("G1 Preventive Collection")
    //JDK 21
    CODE_CACHE_THRESHOLD("CodeCache GC Threshold")

 */
