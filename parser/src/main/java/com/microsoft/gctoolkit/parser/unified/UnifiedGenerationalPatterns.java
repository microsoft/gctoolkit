// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unified;

import com.microsoft.gctoolkit.parser.GCParseRule;

public interface UnifiedGenerationalPatterns extends UnifiedPatterns {

    String PHASE_NAME = "(Mark|Preclean|Abortable Preclean|Sweep|Reset)";
    String GC_PHASES = "(Scavenge|Par Mark|Rescan \\(parallel\\)|Reference Processing|Weak Processing|ClassLoaderData|ProtectionDomainCacheTable|ResolvedMethodTable|Class Unloading|Scrub Symbol Table|Scrub String Table)";
    String FULL_GC_PHASES = "(Mark live objects|Compute new object addresses|Adjust pointers|Move objects)";
    String PARALLEL_PHASES = "(Marking Phase|Summary Phase|Adjust Roots|Compaction Phase|Post Compact)";

    GCParseRule PARNEW_TAG = new GCParseRule("PARNEW_TAG Tag", "ParNew: ");
    GCParseRule CMS_TAG = new GCParseRule("CMS_TAG", "Using Concurrent Mark Sweep$");
    GCParseRule PARALLEL_TAG = new GCParseRule("PARALLEL_TAG", "Using Parallel$");
    GCParseRule SERIAL_TAG = new GCParseRule("SERIAL_TAG", "Using Serial");

    //[0.165s][info ][gc,start     ] GC(1) Pause Young (Allocation Failure)
    GCParseRule YOUNG_HEADER = new GCParseRule("YOUNG_HEADER", "Pause Young " + GC_CAUSE + "$");

    String GENERATIONAL_MEMORY_POOL = "(ParNew|CMS|PSYoungGen|ParOldGen|DefNew|Tenured)";

    //[0.170s][info ][gc,heap      ] GC(1) ParNew: 19356K->1696K(19648K)
    //[0.170s][info ][gc,heap      ] GC(1) CMS: 130K->1179K(43712K)
    GCParseRule GENERATIONAL_MEMORY_SUMMARY = new GCParseRule("GENERATIONAL_MEMORY_SUMMARY", GENERATIONAL_MEMORY_POOL + ": " + BEFORE_AFTER_CONFIGURED);
    GCParseRule GENERATIONAL_MEMORY_SUMMARY_EXTENDED = new GCParseRule("GENERATIONAL_MEMORY_SUMMARY_EXTENDED", GENERATIONAL_MEMORY_POOL + ": " + BEFORE_CONFIGURED_AFTER_CONFIGURED);

    //[00.170s][info ][gc           ] GC(1222) Pause Young (Allocationx Failure) 19M->2M(61M) 5.221ms
    GCParseRule YOUNG_DETAILS = new GCParseRule("YOUNG_DETAILS", "Pause Young " + GC_CAUSE + BEFORE_AFTER_CONFIGURED_PAUSE);

    GCParseRule INITIAL_MARK = new GCParseRule("INITIAL_MARK", "Pause Initial Mark$");
    //[0.279s][info ][gc           ] GC(35) Pause Initial Mark 29M->29M(61M) 0.184ms

    GCParseRule INITIAL_MARK_SUMMARY = new GCParseRule("INITIAL_MARK_SUMMARY", "Pause Initial Mark " + BEFORE_AFTER_CONFIGURED_PAUSE);
    //[0.279s][info ][gc           ] GC(35) Pause Initial Mark 29M->29M(61M) 0.184ms
    GCParseRule CONCURRENT_PHASE_START = new GCParseRule("CONCURRENT_PHASE_START", "Concurrent " + PHASE_NAME + "$");
    GCParseRule CONCURRENT_PHASE_END = new GCParseRule("CONCURRENT_PHASE_END", "Concurrent " + PHASE_NAME + " " + PAUSE_TIME);
    //[0.279s][info ][gc,task      ] GC(35) Using 2 workers of 2 for marking
    GCParseRule WORKER_THREADS = new GCParseRule("WORKER_THREADS", "Using " + COUNTER + " workers of " + COUNTER + " for marking");
    GCParseRule REMARK = new GCParseRule("REMARK", "Pause Remark$");
    GCParseRule REMARK_SUMMARY = new GCParseRule("REMARK_SUMMARY", "Pause Remark " + BEFORE_AFTER_CONFIGURED_PAUSE);
    GCParseRule GC_PHASE = new GCParseRule("GC_PHASE", GC_PHASES + " " + PAUSE_TIME);
    GCParseRule OLD_SUMMARY = new GCParseRule("Old after CMS", "Old: " + BEFORE_AFTER_CONFIGURED);

    GCParseRule PROMOTION_FAILED = new GCParseRule("PROMOTION_FAILED", "Promotion failed$");
    GCParseRule FULL_GC = new GCParseRule("FULL_GC", "Pause Full " + GC_CAUSE + "$");
    GCParseRule FULL_GC_SUMMARY = new GCParseRule("FULL_GC_SUMMARY", "Pause Full " + GC_CAUSE + BEFORE_AFTER_CONFIGURED + " " + PAUSE_TIME);
    GCParseRule FULL_GC_PHASE_START = new GCParseRule("FULL_GC_PHASE_START", "Phase (1|2|3|4): " + FULL_GC_PHASES + "$");    // 15
    GCParseRule FULL_GC_PHASE_END = new GCParseRule("FULL_GC_PHASE_END", "Phase (1|2|3|4): " + FULL_GC_PHASES + " " + PAUSE_TIME);

    // Parallel and Serial patterns
    GCParseRule PRE_COMPACT = new GCParseRule("PRE_COMPACT", "Pre Compact " + PAUSE_TIME);
    GCParseRule PARALLEL_PHASE = new GCParseRule("PARALLEL_PHASE", PARALLEL_PHASES + "$");
    GCParseRule PARALLEL_PHASE_SUMMARY = new GCParseRule("PARALLEL_PHASE_SUMMARY", PARALLEL_PHASES + " " + PAUSE_TIME);
    GCParseRule METASPACE_DETAILED = new GCParseRule("METASPACE", "Metaspace: " + BEFORE_CONFIGURED_AFTER_CONFIGURED + " NonClass: " + BEFORE_CONFIGURED_AFTER_CONFIGURED + " Class: " + BEFORE_CONFIGURED_AFTER_CONFIGURED);

}
