// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unified;


import com.microsoft.gctoolkit.parser.GCParseRule;

public interface UnifiedG1GCPatterns extends UnifiedPatterns {

    GCParseRule G1_TAG = new GCParseRule("G1_TAG", "Using G1$");

    //[90.450s][debug][gc,heap      ] GC(1459) Heap before GC invocations=1459 (full 0):
    GCParseRule HEAP_BEFORE_AFTER_GC_INVOCATION_COUNT = new GCParseRule("HEAP_BEFORE_AFTER_GC_INVOCATION_COUNT", "Heap (before|after) GC invocations=" + COUNTER + " \\(full " + COUNTER + "\\):");

    //[90.450s][debug][gc,heap      ] GC(1459)  garbage-first heap   total 975872K, used 587987K [0x00000006c0000000, 0x00000006c0101dc8, 0x00000007c0000000)
    GCParseRule HEAP_SUMMARY = new GCParseRule("HEAP_SUMMARY", "garbage-first heap   total " + COUNTER + "K, used " + COUNTER + "K \\[" + HEX + ", " + HEX + ", " + HEX);

    // [90.450s][debug][gc,heap      ] GC(1459)   region size 1024K, 571 young (584704K), 1 survivors (1024K)
    GCParseRule REGION_DISBURSEMENT = new GCParseRule("REGION_DISBURSEMENT", "region size " + COUNTER + "K, " + COUNTER + " young \\(" + COUNTER + "K\\), " + COUNTER + " survivors \\(" + COUNTER + "K\\)");

    //[90.452s][debug][gc,heap      ] GC(1459)  Metaspace       used 16279K, capacity 17210K, committed 17408K, reserved 1064960K\
    //[90.452s][debug][gc,heap      ] GC(1459)   class space    used 1773K, capacity 1988K, committed 2048K, reserved 1048576K
    GCParseRule META_CLASS_SPACE = new GCParseRule("META_CLASS_SPACE", "(Metaspace|class space)\\s+" + POOL_SUMMARY);

    String YOUNG_COLLECTION_SUB_TYPE = "(Normal|Prepare Mixed|Mixed|Concurrent Start|Concurrent End)";
    String YOUNG_COLLECTION_TYPES = "(Young|Mixed|Initial Mark|Full)";
    GCParseRule G1_COLLECTION = new GCParseRule("G1_COLLECTION", "Pause " + YOUNG_COLLECTION_TYPES + " (\\(" + YOUNG_COLLECTION_SUB_TYPE + "\\) )?" + GC_CAUSE + "$");

    //Pre Evacuate Collection Set: 0.0ms
    GCParseRule PRE_EVACUATE_COLLECTION_SET = new GCParseRule("PRE_EVACUATE_COLLECTION_SET", "(Pre|Post)? Evacuate Collection Set: " + PAUSE_TIME);
    String PRE_EVACUATE_SUBPHASE_NAME = "(Prepare TLABs|Choose Collection Set|Humongous Register)";
    GCParseRule PRE_EVACUATION_SUBPHASE = new GCParseRule("Pre Evacuation Phase", PRE_EVACUATE_SUBPHASE_NAME + ": " + PAUSE_TIME);

    String EVACUATION_PHASES = "(Ext Root Scanning|Update RS|Scan RS|Code Root Scanning|Object Copy|Termination|GC Worker Other|GC Worker Total)";
    GCParseRule EVACUATION_PHASE = new GCParseRule("EVACUATION_PHASE", EVACUATION_PHASES + " \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);

    String PARALLEL_PHASES_COUNTS = "(Processed Buffers|Termination Attempts)";
    GCParseRule PARALLEL_COUNT = new GCParseRule("PARALLEL_COUNT", PARALLEL_PHASES_COUNTS + ":\\s+" + WORKER_SUMMARY_INT);

    String POST_EVACUATE_PHASES = "(Code Roots Fixup|Preserve CM Refs|Reference Processing|Clear Card Table|Evacuation Failure|Reference Enqueuing|Merge Per-Thread State|Code Roots Purge|Redirty Cards|Clear Claimed Marks|Free Collection Set|Humongous Reclaim|Expand Heap After Collection)";
    GCParseRule POST_EVACUATE_PHASE = new GCParseRule("POST_EVACUATE_PHASE", POST_EVACUATE_PHASES + ": " + PAUSE_TIME);
    GCParseRule REFERENCE_PROCESSING = new GCParseRule("REFERENCE_PROCESSING", "(Reference Processing) " + PAUSE_TIME);
    GCParseRule TO_SPACE_EXHAUSTED = new GCParseRule("TO_SPACE_EXHAUSTED", "To-space exhausted");

    GCParseRule OTHER = new GCParseRule("OTHER", "Other: " + PAUSE_TIME);

    GCParseRule REGION_SUMMARY = new GCParseRule("REGION_SUMMARY", "(Eden|Survivor|Old|Humongous|Archive) regions: " + REGION_MEMORY_BLOCK);
    GCParseRule METASPACE = new GCParseRule("METASPACE", "Metaspace: " + BEFORE_AFTER_CONFIGURED);

    //Metaspace: 10107K(10240K)->10107K(10240K) NonClass: 9227K(9280K)->9227K(9280K) Class: 880K(960K)->880K(960K)
    GCParseRule META_SPACE_BREAKOUT = new GCParseRule("META_SPACE_BREAKOUT", "Metaspace: " + BEFORE_CONFIGURED_AFTER_CONFIGURED + " NonClass: " + BEFORE_CONFIGURED_AFTER_CONFIGURED + " Class: " + BEFORE_CONFIGURED_AFTER_CONFIGURED);

    //[00.170s][info ][gc           ] GC(1222) Pause Young (Allocation Failure) 19M->2M(61M) 5.221ms
    //[90.452s][info ][gc           ] GC(1459) Pause Young (G1 Evacuation Pause) 574M->4M(953M) 2.065ms
    GCParseRule YOUNG_DETAILS = new GCParseRule("YOUNG_DETAILS", "Pause " + YOUNG_COLLECTION_TYPES + " (\\(" + YOUNG_COLLECTION_SUB_TYPE + "\\) )?" + GC_CAUSE + BEFORE_AFTER_CONFIGURED_PAUSE);

    GCParseRule HEAP_REGION_SIZE = new GCParseRule("HEAP_REGION_SIZE", "Heap region size: " + MEMORY_SIZE);

    GCParseRule HEAP_SIZE = new GCParseRule("HEAP_SIZE", "Minimum heap " + COUNTER + "  Initial heap " + COUNTER + "  Maximum heap " + COUNTER);

    //[73.082s][info ][gc           ] GC(263) Concurrent Cycle
    GCParseRule CONCURRENT_CYCLE_START = new GCParseRule("CONCURRENT_CYCLE_START", "Concurrent (?:Mark )?Cycle$");
    //[73.171s][info ][gc            ] GC(263) Concurrent Cycle 89.437ms
    GCParseRule CONCURRENT_CYCLE_END = new GCParseRule("CONCURRENT_CYCLE_END", "Concurrent (?:Mark )?Cycle " + CONCURRENT_TIME);

    //[73.082s][info ][gc           ] GC(263) Concurrent Cycle
    GCParseRule CONCURRENT_UNDO_CYCLE_START = new GCParseRule("CONCURRENT_CYCLE_START", "Concurrent Undo Cycle$");
    //[73.171s][info ][gc            ] GC(263) Concurrent Cycle 89.437ms
    GCParseRule CONCURRENT_UNDO_CYCLE_END = new GCParseRule("CONCURRENT_CYCLE_END", "Concurrent Undo Cycle " + CONCURRENT_TIME);

    //    [73.082s][info ][gc,marking   ] GC(263) Concurrent Clear Claimed Marks
    //    [73.082s][info ][gc,marking   ] GC(263) Concurrent Clear Claimed Marks 0.018ms
    //
    //    [155.787s][info ][gc          ] GC(2457) Concurrent Undo Cycle
    //    [155.836s][info ][gc          ] GC(2457) Concurrent Undo Cycle 49.351ms
    //
    //    [73.082s][info ][gc,marking   ] GC(263) Concurrent Scan Root Regions
    //    [73.084s][info ][gc,marking   ] GC(263) Concurrent Scan Root Regions 2.325ms
    String CONCURRENT_PHASES = "(Mark|Clear Claimed Marks|Scan Root Regions|Rebuild Remembered Sets|Create Live Data|Complete Cleanup|Cleanup for Next Mark)";
    GCParseRule CONCURRENT_PHASE = new GCParseRule("CONCURRENT_PHASE", "Concurrent " + CONCURRENT_PHASES + "$");
    GCParseRule CONCURRENT_PHASE_DURATION = new GCParseRule("CONCURRENT_PHASE_DURATION", "Concurrent " + CONCURRENT_PHASES + " " + CONCURRENT_TIME);

    //    [73.084s][info ][gc,marking   ] GC(263) Concurrent Mark (73.084s)
    //    [73.084s][info ][gc,marking   ] GC(263) Concurrent Mark From Roots
    //    [73.084s][info ][gc,task      ] GC(263) Using 2 workers of 2 for marking
    //    [73.138s][info ][gc,marking   ] GC(263) Concurrent Mark From Roots 53.902ms
    //    [73.138s][info ][gc,marking   ] GC(263) Concurrent Mark (73.084s, 73.138s) 53.954ms
    String CONCURRENT_MARK_PHASES = "(Mark From Roots|Preclean)";
    GCParseRule CONCURRENT_MARK_START = new GCParseRule("CONCURRENT_MARK_START", "Concurrent (Mark) \\(.+\\)$");
    GCParseRule CONCURRENT_MARK_PHASE = new GCParseRule("CONCURRENT MARK PHASE", "Concurrent " + CONCURRENT_MARK_PHASES + "$");
    GCParseRule CONCURRENT_MARK_PHASE_DURATION = new GCParseRule("CONCURRENT MARK PHASE", "Concurrent " + CONCURRENT_MARK_PHASES + " " + CONCURRENT_TIME);
    GCParseRule CONCURRENT_MARK_WORKERS = new GCParseRule("CONCURRENT_MARK_WORKERS", "Using " + COUNTER + " workers of " + COUNTER + " for marking");
    GCParseRule CONCURRENT_MARK_ABORTED = new GCParseRule("Concurrent Mark Abort", "Concurrent Mark Abort");
    GCParseRule CONCURRENT_MARK_END = new GCParseRule("CONCURRENT_MARK_END", "Concurrent (Mark) \\(.+\\) " + CONCURRENT_TIME);

    //    [73.139s][info ][gc,start     ] GC(263) Pause Remark
    //    [73.139s][debug][gc,phases    ] GC(263) Finalize Marking 0.251ms
    //    [73.139s][debug][gc,ref       ] GC(263) SoftReference 0.026ms
    //    [73.158s][debug][gc,ref       ] GC(263) WeakReference 18.890ms
    //    [73.158s][debug][gc,ref       ] GC(263) FinalReference 0.025ms
    //    [73.158s][debug][gc,ref       ] GC(263) PhantomReference 0.160ms
    //    [73.158s][debug][gc,ref       ] GC(263) JNI Weak Reference 0.240ms
    //    [73.158s][debug][gc,ref       ] GC(263) Ref Counts: Soft: 0 Weak: 258086 Final: 11 Phantom: 816
    //    [73.158s][debug][gc,phases    ] GC(263) Reference Processing 19.485ms
    //    [73.159s][debug][gc,phases    ] GC(263) System Dictionary Unloading 0.138ms
    //    [73.160s][info ][gc,stringtable] GC(263) Cleaned string and symbol table, strings: 7924 processed, 21 removed, symbols: 55530 processed, 17 removed
    //    [73.160s][debug][gc,phases     ] GC(263) Parallel Unloading 1.714ms
    //    [73.160s][info ][gc            ] GC(263) Pause Remark 211M->211M(256M) 21.685ms
    //    [73.160s][info ][gc,cpu        ] GC(263) User=0.03s Sys=0.00s Real=0.02s
    GCParseRule PAUSE_REMARK_START = new GCParseRule("PAUSE_REMARK_START", "Pause Remark$");
    GCParseRule FINALIZE_MARKING = new GCParseRule("FINIALIZE_MARKING", "Finalize Marking " + PAUSE_TIME);
    GCParseRule SYSTEM_DICTIONARY_UNLOADING = new GCParseRule("SYSTEM_DICTIONARY_UNLOADING", "System Dictionary Unloading " + PAUSE_TIME);
    GCParseRule STRING_SYMBOL_TABLE = new GCParseRule("STRING_SYMBOL_TABLE", "Cleaned string and symbol table, strings: " + COUNTER + " processed, " + COUNTER + " removed, symbols: " + COUNTER + " processed, " + COUNTER + " removed");
    GCParseRule PARALLEL_UNLOADING = new GCParseRule("PARALLEL_UNLOADING", "Parallel Unloading " + PAUSE_TIME);
    GCParseRule PAUSE_REMARK_END = new GCParseRule("PAUSE_REMARK_END", "Pause Remark " + BEFORE_AFTER_CONFIGURED_PAUSE);

    //
    //    [73.160s][info ][gc,marking    ] GC(263) Concurrent Create Live Data
    //    [73.168s][info ][gc,marking    ] GC(263) Concurrent Create Live Data 8.089ms
    //
    //    [73.169s][info ][gc,start      ] GC(263) Pause Cleanup
    //    [11.049s][debug][gc,phases     ] GC(63) Update Remembered Set Tracking After Rebuild 0.007ms
    //    [11.049s][debug][gc,phases     ] GC(63) Finalize Concurrent Mark Cleanup 0.049ms
    //    [73.169s][info ][gc            ] GC(263) Pause Cleanup 223M->213M(256M) 0.271ms
    //    [73.169s][info ][gc,cpu        ] GC(263) User=0.00s Sys=0.00s Real=0.00s
    GCParseRule CLEANUP_START = new GCParseRule("CLEANUP_START", "Pause Cleanup$");
    GCParseRule CLEANUP__FINALIZE_CONC_MARK = new GCParseRule("", "Finalize Concurrent Mark Cleanup " + PAUSE_TIME );
    GCParseRule CLEANUP_END = new GCParseRule("CLEANUP_END", "Pause Cleanup " + BEFORE_AFTER_CONFIGURED_PAUSE);

    //    [73.169s][info ][gc,marking    ] GC(263) Concurrent Complete Cleanup
    //    [73.169s][info ][gc,marking    ] GC(263) Concurrent Complete Cleanup 0.013ms
    //
    //    [73.169s][info ][gc,marking    ] GC(263) Concurrent Cleanup for Next Mark
    //    [73.171s][info ][gc,marking    ] GC(263) Concurrent Cleanup for Next Mark 1.646ms
    //    these records are covered by CONCURRENT_PHASES

    String FULL_PHASES = "(Mark live objects|Compute new object addresses|Adjust pointers|Move objects|Prepare for compaction|Compact heap)";
    GCParseRule FULL_PHASE = new GCParseRule("FULL_PHASE", "Phase " + COUNTER + ": " + FULL_PHASES + "( " + PAUSE_TIME + ")?");
    GCParseRule FULL_CLASS_UNLOADING = new GCParseRule("FULL_CLASS_UNLOADING", "Class Unloading " + PAUSE_TIME);
    GCParseRule FULL_STRING_SYMBOL_TABLE = new GCParseRule("FULL_STRING_SYMBOL_TABLE", "Scrub String and Symbol Tables " + PAUSE_TIME);

    //"[156.473s][debug][gc,phases   ] GC(2467)     Concatenate Dirty Card Logs: 0.0ms"
    GCParseRule CONCATENATE_DIRTY_CARD_LOGS = new GCParseRule("CONCATENATE_DIRTY_CARD_LOGS","Concatenate Dirty Card Logs: " + PAUSE_TIME);
    //"[156.473s][debug][gc,phases   ] GC(2467)     Region Register: 0.1ms"
    GCParseRule REGION_REGISTER = new GCParseRule("REGION_REGISTER","Region Register: " + PAUSE_TIME);
    //"[156.473s][debug][gc,phases   ] GC(2467)     Prepare Heap Roots: 0.0ms"
    //"[156.473s][info ][gc,phases   ] GC(2467)   Merge Heap Roots: 0.3ms",
    //"[156.473s][debug][gc,phases   ] GC(2467)     Prepare Merge Heap Roots: 0.0ms"
    GCParseRule HEAP_ROOTS = new GCParseRule("HEAP_ROOTS","(Prepare|Merge|Prepare Merge) Heap Roots: " + PAUSE_TIME);
    //"[156.473s][debug][gc,phases   ] GC(2467)     Eager Reclaim (ms):            Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1"
    GCParseRule EAGER_RECLAIM  = new GCParseRule("EAGER_RECLAIM","Eager Reclaim \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.473s][debug][gc,phases   ] GC(2467)     Remembered Sets (ms):          Min:  0.0, Avg:  0.1, Max:  0.2, Diff:  0.2, Sum:  3.6, Workers: 53"
    GCParseRule REMEMBERED_SETS = new  GCParseRule("REMEMBERED_SETS","Remembered Sets \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.473s][debug][gc,phases   ] GC(2467)       Merged Sparse:                 Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53",
    //"[156.473s][debug][gc,phases   ] GC(2467)       Merged Fine:                   Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53",
    //"[156.473s][debug][gc,phases   ] GC(2467)       Merged Coarse:                 Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53"
    GCParseRule EAGER_RECLAIM_STEP = new GCParseRule("EAGER_RECLAIM_STEP","Merged (Sparse|Fine|Coarse):\\s+" + WORKER_SUMMARY_INT);
    //"[156.473s][debug][gc,phases   ] GC(2467)       Dirty Cards:                   Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53",
    //"[156.473s][debug][gc,phases   ] GC(2467)       Skipped Cards:                 Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53"
    GCParseRule CARDS = new GCParseRule("CARDS","(Dirty|Skipped) Cards:\\s+" + WORKER_SUMMARY_INT);
    //"[156.473s][debug][gc,phases   ] GC(2467)     Hot Card Cache (ms):           Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.1, Workers: 53"
    //"[156.473s][debug][gc,phases   ] GC(2467)       Reset Hot Card Cache (ms):     Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1"
    GCParseRule HOT_CARD_CACHE = new GCParseRule("HOT_CARD_CACHE","(Reset )?Hot Card Cache \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.473s][debug][gc,phases   ] GC(2467)     Log Buffers (ms):              Min:  0.0, Avg:  0.1, Max:  0.2, Diff:  0.2, Sum:  6.9, Workers: 53"
    GCParseRule LOG_BUFFERS = new GCParseRule("LOG_BUFFERS","Log Buffers \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.473s][debug][gc,phases   ] GC(2467)     Scan Heap Roots (ms):          Min:  0.0, Avg:  0.0, Max:  0.1, Diff:  0.1, Sum:  1.8, Workers: 53"
    GCParseRule SCAN_HEAP_ROOTS = new GCParseRule("SCAN_HEAP_ROOTS","Scan Heap Roots \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);

    //"[156.473s][debug][gc,phases   ] GC(2467)       Scanned Cards:                 Min: 0, Avg: 10.7, Max: 67, Diff: 67, Sum: 569, Workers: 53",
    //"[156.473s][debug][gc,phases   ] GC(2467)       Scanned Blocks:                Min: 0, Avg:  8.3, Max: 39, Diff: 39, Sum: 440, Workers: 53",
    GCParseRule SCANS = new GCParseRule("SCANS","Scanned (Cards|Blocks):\\s+" + WORKER_SUMMARY_INT);
    //"[156.473s][debug][gc,phases   ] GC(2467)       Claimed Chunks:                Min: 0, Avg:  3.4, Max: 10, Diff: 10, Sum: 179, Workers: 53"
    GCParseRule CLAIMED_CHUNKS = new GCParseRule("CLAIMED_CHUNKS","Claimed Chunks:\\s+" + WORKER_SUMMARY_INT);
    //"[156.473s][debug][gc,phases   ] GC(2467)     Code Root Scan (ms):           Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 53"
    GCParseRule CODE_ROOT_SCAN = new GCParseRule("CODE_ROOT_SCAN","Code Root Scan \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.473s][debug][gc,phases   ] GC(2467)       StringDedup Requests0 Weak     Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 7",
    //"[156.473s][debug][gc,phases   ] GC(2467)       StringDedup Requests1 Weak     Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 7"
    GCParseRule STRING_DEDUP = new GCParseRule("STRING_DEDUP","StringDedup (?:Requests(0|1)|Table) Weak\\s+" + WORKER_SUMMARY_REAL);
    //GCParseRule STRING_DEDUP = new GCParseRule("STRING_DEDUP","StringDedup Requests(0|1) Weak\\s+" + WORKER_SUMMARY_REAL);
    //"[156.473s][debug][gc,phases   ] GC(2467)       Weak JFR Old Object Samples    Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 7"
    GCParseRule WEAK_JFR_SAMPLES = new GCParseRule("WEAK_JFR_SAMPLES","Weak JFR Old Object Samples\\s+" + WORKER_SUMMARY_REAL);

    //"[156.473s][debug][gc,phases   ] GC(2467)     Post Evacuate Cleanup 2: 1.1ms"
    //"[156.473s][debug][gc,phases   ] GC(2467)     Post Evacuate Cleanup 1: 0.1ms"
    GCParseRule POST_EVAC_CLEANUP = new GCParseRule("POST_EVAC_CLEANUP","Post Evacuate Cleanup (1|2): " + PAUSE_TIME);
    //"[156.473s][debug][gc,phases   ] GC(2467)       Merge Per-Thread State (ms):   Min:  0.1, Avg:  0.1, Max:  0.1, Diff:  0.0, Sum:  0.1, Workers: 1"
    GCParseRule MERGE_THREAD_STATE = new GCParseRule("MERGE_THREAD_STATE","Merge Per-Thread State \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.473s][debug][gc,phases   ] GC(2467)         Copied Bytes                   Min: 0, Avg:  4.1, Max: 32, Diff: 32, Sum: 216, Workers: 53"
    GCParseRule COPIED_BYTES = new GCParseRule("COPIED_BYTES","Copied Bytes\\s+" + WORKER_SUMMARY_INT);
    //"[156.473s][debug][gc,phases   ] GC(2467)         LAB Waste                      Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53",
    //"[156.473s][debug][gc,phases   ] GC(2467)         LAB Undo Waste                 Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53"
    GCParseRule LAB = new GCParseRule("LAB","LAB( Undo)? Waste\\s+" + WORKER_SUMMARY_INT);
    //"[156.473s][debug][gc,phases   ] GC(2467)       Clear Logged Cards (ms):       Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.3, Workers: 10"
    GCParseRule CLEAR_LOGGED_CARDS = new GCParseRule("CLEAR_LOGGED_CARDS","Clear Logged Cards \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);



    //"[156.473s][debug][gc,phases   ] GC(2467)       Recalculate Used Memory (ms):  Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1"
    GCParseRule RECALC_USED_MEM = new GCParseRule("RECALC_USED_MEM","Recalculate Used Memory \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.473s][debug][gc,phases   ] GC(2467)       Purge Code Roots (ms):         Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1"
    GCParseRule PURGE_CODE_ROOTS = new GCParseRule("PURGE_CODE_ROOTS","Purge Code Roots \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.473s][debug][gc,phases   ] GC(2467)       Update Derived Pointers (ms):  Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1"
    GCParseRule UPDATE_DERIVED_POINTERS = new GCParseRule("UPDATE_DERIVED_POINTERS","Update Derived Pointers \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.473s][debug][gc,phases   ] GC(2467)       Eagerly Reclaim Humongous Objects (ms): Min:  1.1, Avg:  1.1, Max:  1.1, Diff:  0.0, Sum:  1.1, Workers: 1"
    GCParseRule EAGER_HUMONGOUS_RECLAIM = new GCParseRule("EAGER_HUMONGOUS_RECLAIM","Eagerly Reclaim Humongous Objects \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.473s][debug][gc,phases   ] GC(2467)         Humongous Total                Min: 1685, Avg: 1685.0, Max: 1685, Diff: 0, Sum: 1685, Workers: 1",
    //"[156.473s][debug][gc,phases   ] GC(2467)         Humongous Candidates           Min: 1685, Avg: 1685.0, Max: 1685, Diff: 0, Sum: 1685, Workers: 1",
    //"[156.474s][debug][gc,phases   ] GC(2467)         Humongous Reclaimed            Min: 390, Avg: 390.0, Max: 390, Diff: 0, Sum: 390, Workers: 1"
    GCParseRule HUMONGOUS = new GCParseRule("HUMONGOUS","Humongous (Total|Candidates|Reclaimed)\\s+" + WORKER_SUMMARY_INT);


    //"[156.474s][debug][gc,phases   ] GC(2467)       Redirty Logged Cards (ms):     Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  1.1, Workers: 53",
    GCParseRule REDIRTY_CARDS = new GCParseRule("REDIRTY_CARDS","Redirty Logged Cards \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.474s][debug][gc,phases   ] GC(2467)         Redirtied Cards:               Min: 0, Avg:  9.1, Max: 130, Diff: 130, Sum: 482, Workers: 53"
    GCParseRule REDIRTIED_CARDS = new GCParseRule("REDIRTIED_CARDS","Redirtied Cards:\\s+" + WORKER_SUMMARY_INT);
    //"[156.474s][debug][gc,phases   ] GC(2467)       Free Collection Set (ms):      Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.9, Workers: 53"
    GCParseRule FREE_CSET = new GCParseRule("FREE_CSET","Free Collection Set \\(ms\\):\\s+" + WORKER_SUMMARY_REAL);
    //"[156.474s][debug][gc,phases   ] GC(2467)     Rebuild Free List: 0.1ms"
    GCParseRule REBUILD_FREELIST = new GCParseRule("REBUILD_FREELIST","Rebuild Free List: " + PAUSE_TIME);
    //"[156.474s][debug][gc,phases   ] GC(2467)     Start New Collection Set: 0.0ms"
    GCParseRule NEW_CSET = new GCParseRule("NEW_CSET","Start New Collection Set: " + PAUSE_TIME);
    //"[156.474s][debug][gc,phases   ] GC(2467)     Resize TLABs: 0.0ms"
    GCParseRule RESIZE_TLAB = new GCParseRule("RESIZE_TLAB","Resize TLABs: " + PAUSE_TIME);
    GCParseRule WEAK_PROCESSING = new GCParseRule( "WEAK_PROCESSING","Weak Processing(?::)? " + PAUSE_TIME);

}
