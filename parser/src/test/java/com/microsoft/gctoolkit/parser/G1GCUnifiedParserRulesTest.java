// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.parser.jvm.Decorators;
import com.microsoft.gctoolkit.parser.unified.UnifiedG1GCPatterns;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static com.microsoft.gctoolkit.parser.CommonTestHelper.captureTest;
import static org.junit.jupiter.api.Assertions.*;

public class G1GCUnifiedParserRulesTest implements UnifiedG1GCPatterns {

    /**
     * The rules are;
     * A pattern should capture the lines for which it is intended to capture
     * A pattern should not capture any other lines.
     *
     * This testing is designed to run all patterns against all lines
     */

    private static final Logger LOGGER = Logger.getLogger(G1GCUnifiedParserRulesTest.class.getName());

    @Test
    public void testG1GCParseRules() {
        assertEquals(lines.length,rules.length, "Rules and data dont't match");
        for (int i = 0; i < rules.length; i++)
            for (int j = 0; j < lines.length; j++) {
                int captured = captureTest(rules[i], lines[j]);
                if (i == j) {
                    assertEquals(lines[j].length, captured,rules[i].getName() + " failed to capture");
                } else {
                    assertEquals(0, captured, rules[i].getName() + " erroneous capture");
                }
            }

        assertTrue(true);
    }

    @Test
    public void testUnifiedLoggingDecorators() {
        for (String decoratorLine : decoratorLines) {
            Decorators decorators = new Decorators(decoratorLine);
            assertTrue(decorators.getNumberOfDecorators() != 0);
        }
    }

    @Test @Disabled("used for debug testing")
    public void testSingeRuleCapture() {
        int index = 6;
        assertEquals(lines[index].length, captureTest(rules[index], lines[index]), "Miss for " + rules[index].getName());
    }

    // for debugging @Test
    public void testSingleRuleMisses() {
        int index = 10;
        for (int i = 0; i < rules.length; i++)
            if ( index != i)
                assertEquals(0, captureTest(rules[index], lines[i]), rules[index].getName() + " hits on lines for " + rules[i].getName());
    }

    // for debugging @Test
    public void testSingleRule() {
        testSingeRuleCapture();
        testSingleRuleMisses();
    }


    private void evaluate(GCParseRule rule, String string, boolean dump) {

        GCLogTrace trace = rule.parse(string);
        assertNotNull(trace);
        if (dump) {
            LOGGER.fine("matches groups " + trace.groupCount());
            for (int i = 0; i <= trace.groupCount(); i++) {
                LOGGER.fine(i + ": " + trace.getGroup(i));
            }
        }
    }

    private GCParseRule[] rules = {
            G1_COLLECTION,
            WORKER_SUMMARY,
            REFERENCES,
            REFERENCE_COUNTS,
            PRE_EVACUATE_COLLECTION_SET,
            OTHER,                         //  5
            REGION_SUMMARY,
            METASPACE,
            YOUNG_DETAILS,
            CPU_BREAKOUT,
            CONCURRENT_CYCLE_START,         // 10
            CONCURRENT_PHASE,
            CONCURRENT_CYCLE_END,
            CONCURRENT_PHASE_DURATION,
            CONCURRENT_MARK_START,
            CONCURRENT_MARK_WORKERS,       // 15
            CONCURRENT_MARK_END,
            PAUSE_REMARK_START,
            FINALIZE_MARKING,
            SYSTEM_DICTIONARY_UNLOADING,
            STRING_SYMBOL_TABLE,           // 20
            PARALLEL_UNLOADING,
            PAUSE_REMARK_END,
            CLEANUP_START,
            CLEANUP_END,
            POST_EVACUATE_PHASE,           // 25
            REFERENCE_PROCESSING,
            TO_SPACE_EXHAUSTED,
            FULL_PHASE,
            FULL_CLASS_UNLOADING,
            FULL_STRING_SYMBOL_TABLE,      // 30
            JVM_EXIT,
            CONCURRENT_MARK_ABORTED,
            CONCURRENT_MARK_PHASE,
            CONCURRENT_MARK_PHASE_DURATION,
            META_SPACE_BREAKOUT,           // 35
            CONCATENATE_DIRTY_CARD_LOGS,
            REGION_REGISTER,
            HEAP_ROOTS,
            EAGER_RECLAIM,
            REMEMBERED_SETS,               // 40
            EAGER_RECLAIM_STEP,
            CARDS,
            HOT_CARD_CACHE,
            LOG_BUFFERS,
            SCAN_HEAP_ROOTS,              // 45
            SCANS,
            CLAIMED_CHUNKS,
            CODE_ROOT_SCAN,
            STRING_DEDUP,
            WEAK_JFR_SAMPLES,             // 50
            POST_EVAC_CLEANUP,
            MERGE_THREAD_STATE,
            COPIED_BYTES,
            LAB,
            CLEAR_LOGGED_CARDS,           // 55
            RECALC_USED_MEM,
            PURGE_CODE_ROOTS,
            UPDATE_DERIVED_POINTERS,
            EAGER_HUMONGOUS_RECLAIM,
            HUMONGOUS,                    // 60
            REDIRTY_CARDS,
            REDIRTIED_CARDS,
            FREE_CSET,
            REBUILD_FREELIST,
            NEW_CSET,                     // 65
            RESIZE_TLAB,
            WEAK_PROCESSING,
            CLEANUP__FINALIZE_CONC_MARK,
            CONCURRENT_UNDO_CYCLE_START,
            CONCURRENT_UNDO_CYCLE_END     // 70
    };

    /*
    [0.015s][info ][gc,heap,coops] Heap address: 0x00000007b0000000, size: 256 MB, Compressed Oops mode: Zero based, Oop shift amount: 3
     */
    /*
       FINE: Missed: [73.158s][debug][gc,phases    ] GC(263) Reference Processing 19.485ms
     */

    private String[][] lines = {
            {   //  0
                    "[2018-03-09T11:14:04.994-0100][12.269s][info][gc,start     ] GC(0) Pause Young (G1 Evacuation Pause)",
                    "[85.690s][info ][gc,start      ] GC(390) Pause Initial Mark (G1 Humongous Allocation)",
                    "[73.198s][info ][gc,start      ] GC(266) Pause Mixed (G1 Evacuation Pause)",
                    "[226.179s][info ][gc,start      ] GC(1198) Pause Full (Diagnostic Command)",
                    "[6.634s][info][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)",
                    "[6.854s][info][gc,start      ] GC(65) Pause Young (Prepare Mixed) (G1 Evacuation Pause)",
                    "[6.856s][info][gc,start      ] GC(66) Pause Young (Mixed) (G1 Evacuation Pause)",
                    "[7.053s][info][gc,start      ] GC(130) Pause Young (Concurrent Start) (G1 Evacuation Pause)"
            },
            {   //  1
                    "[2018-03-09T11:14:04.994-0100][12.269s][info][gc,task      ] GC(0) Using 8 workers of 8 for evacuation",
                    "[226.462s][info ][gc,task        ] GC(1198) Using 8 workers of 8 to rebuild remembered set"
            },
            {   //  2
                    "[2018-03-09T11:14:05.001-0100][12.276s][debug][gc,ref       ] GC(0) SoftReference 0.002ms",
                    "[2018-03-09T11:14:05.001-0100][12.276s][debug][gc,ref       ] GC(0) WeakReference 0.051ms",
                    "[2018-03-09T11:14:05.001-0100][12.276s][debug][gc,ref       ] GC(0) FinalReference 0.183ms",
                    "[2018-03-09T11:14:05.001-0100][12.276s][debug][gc,ref       ] GC(0) PhantomReference 0.016ms",
                    "[2018-03-09T11:14:05.001-0100][12.276s][debug][gc,ref       ] GC(0) JNI Weak Reference 0.049ms",
                    "[156.064s][debug][gc,ref      ] GC(2463) Preclean SoftReferences 0.063ms",
                    "[156.064s][debug][gc,ref      ] GC(2463) Preclean WeakReferences 0.118ms",
                    "[156.064s][debug][gc,ref      ] GC(2463) Preclean FinalReferences 0.055ms",
                    "[156.064s][debug][gc,ref      ] GC(2463) Preclean PhantomReferences 0.052ms"
            },
            {   //  3
                    "[2018-03-09T11:14:05.001-0100][12.276s][debug][gc,ref       ] GC(0) Ref Counts: Soft: 0 Weak: 511 Final: 1079 Phantom: 227"
            },
            {   //  4
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms",
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,phases    ] GC(0)   Evacuate Collection Set: 7.0ms",
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.7ms"
            },
            {   //  5
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,phases    ] GC(0)   Other: 0.2ms"
            },
            {   //  6
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,heap      ] GC(0) Eden regions: 24->0(22)",
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,heap      ] GC(0) Survivor regions: 0->3(3)",
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,heap      ] GC(0) Old regions: 0->9",
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,heap      ] GC(0) Humongous regions: 0->0"
            },
            {   //  7
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,metaspace ] GC(0) Metaspace: 15997K->15997K(1064960K)"
            },
            {   //  8
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc           ] GC(0) Pause Young (G1 Evacuation Pause) 24M->11M(256M) 7.995ms",
                    "[73.081s][info ][gc           ] GC(262) Pause Initial Mark (G1 Humongous Allocation) 206M->118M(256M) 4.382ms",
                    "[226.506s][info ][gc             ] GC(1198) Pause Full (Diagnostic Command) 162M->130M(256M) 327.028ms",
                    "[6.641s][info][gc           ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 23M->6M(64M) 6.302ms",
                    "[7.965s][info][gc            ] GC(415) Pause Young (Concurrent Start) (G1 Evacuation Pause) 49M->30M(64M) 1.965ms",
                    "[7.981s][info][gc            ] GC(419) Pause Young (Prepare Mixed) (G1 Evacuation Pause) 43M->16M(64M) 1.734ms",
                    "[7.984s][info][gc            ] GC(420) Pause Young (Mixed) (G1 Evacuation Pause) 26M->11M(64M) 1.453ms",
                    "[19.869s][info][gc] GC(8) Pause Young (Normal) (G1 Evacuation Pause) 170M->42M(1024M) 8.502ms"
            },
            {   //  9
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,cpu       ] GC(0) User=0.04s Sys=0.01s Real=0.01s"
            },
            {   // 10
                    "[73.082s][info ][gc           ] GC(263) Concurrent Cycle",
                    "[2.179s][info ][gc          ] GC(9) Concurrent Mark Cycle"
            },
            {   // 11
                    "[156.051s][info ][gc,marking  ] GC(2463) Concurrent Mark",
                    "[73.082s][info ][gc,marking   ] GC(263) Concurrent Clear Claimed Marks",
                    "[73.082s][info ][gc,marking   ] GC(263) Concurrent Scan Root Regions",
                    "[73.160s][info ][gc,marking    ] GC(263) Concurrent Create Live Data",
                    "[73.169s][info ][gc,marking    ] GC(263) Concurrent Complete Cleanup",
                    "[73.169xs][info ][gc,marking    ] GC(263) Concurrent Cleanup for Next Mark"
            },
            {   // 12
                    "[73.171s][info ][gc            ] GC(263) Concurrent Cycle 89.437ms",
                    "[2.179s][info ][gc          ] GC(9) Concurrent Mark Cycle 96.518ms"
            },
            {   // 13
                    "[156.068s][info ][gc,marking  ] GC(2463) Concurrent Mark 16.895ms",
                    "[73.082s][info ][gc,marking   ] GC(263) Concurrent Clear Claimed Marks 0.018ms",
                    "[73.084s][info ][gc,marking   ] GC(263) Concurrent Scan Root Regions 2.325ms",
                    "[73.168s][info ][gc,marking    ] GC(263) Concurrent Create Live Data 8.089ms",
                    "[73.169s][info ][gc,marking    ] GC(263) Concurrent Complete Cleanup 0.013ms",
                    "[73.171s][info ][gc,marking    ] GC(263) Concurrent Cleanup for Next Mark 1.646ms"
            },
            {   // 14
                    "[73.084s][info ][gc,marking   ] GC(263) Concurrent Mark (73.084s)"
            },
            {   // 15
                    "[73.084s][info ][gc,task      ] GC(263) Using 2 workers of 2 for marking"
            },
            {   // 16
                    "[73.138s][info ][gc,marking   ] GC(263) Concurrent Mark (73.084s, 73.138s) 53.954ms"
            },
            {   // 17
                    "[73.139s][info ][gc,start     ] GC(263) Pause Remark"
            },
            {   // 18
                    "[73.139s][debug][gc,phases    ] GC(263) Finalize Marking 0.251ms"
            },
            {   // 19
                    "[73.159s][debug][gc,phases    ] GC(263) System Dictionary Unloading 0.138ms"
            },
            {   // 20
                    "[73.160s][info ][gc,stringtable] GC(263) Cleaned string and symbol table, strings: 7924 processed, 21 removed, symbols: 55530 processed, 17 removed"
            },
            {   // 21
                    "[73.160s][debug][gc,phases     ] GC(263) Parallel Unloading 1.714ms"
            },
            {   // 22
                    "[73.160s][info ][gc            ] GC(263) Pause Remark 211M->211M(256M) 21.685ms"
            },
            {   // 23
                    "[73.169s][info ][gc,start      ] GC(263) Pause Cleanup"
            },
            {   // 24
                    "[73.169s][info ][gc            ] GC(263) Pause Cleanup 223M->213M(256M) 0.271ms"
            },
            {   // 25
                    "[73.081s][debug][gc,phases    ] GC(262)     Code Roots Fixup: 0.0ms",
                    "[73.081s][debug][gc,phases    ] GC(262)     Preserve CM Refs: 0.0ms",
                    "[73.081s][debug][gc,phases    ] GC(262)     Reference Processing: 0.2ms",
                    "[73.081s][debug][gc,phases    ] GC(262)     Clear Card Table: 0.0ms",
                    "[263.338s][debug][gc,phases      ] GC(1352)     Evacuation Failure: 15.2ms",
                    "[73.081s][debug][gc,phases    ] GC(262)     Reference Enqueuing: 0.0ms",
                    "[73.081s][debug][gc,phases    ] GC(262)     Merge Per-Thread State: 0.0ms",
                    "[73.081s][debug][gc,phases    ] GC(262)     Code Roots Purge: 0.0ms",
                    "[73.081s][debug][gc,phases    ] GC(262)     Redirty Cards: 0.0ms",
                    "[73.081s][debug][gc,phases    ] GC(262)     Clear Claimed Marks: 0.0ms",
                    "[73.081s][debug][gc,phases    ] GC(262)     Free Collection Set: 0.1ms",
                    "[73.081s][debug][gc,phases    ] GC(262)     Humongous Reclaim: 0.0ms",
                    "[73.081s][debug][gc,phases    ] GC(262)     Expand Heap After Collection: 0.0ms"
            },
            {   // 26
                    "[73.158s][debug][gc,phases    ] GC(263) Reference Processing 19.485ms",
            },
            {   // 27
                    "[151.021s][info ][gc            ] GC(1030) To-space exhausted"
            },
            {   // 28
                    "[226.179s][info ][gc,phases,start] GC(1198) Phase 1: Mark live objects",
                    "[226.310s][info ][gc,phases      ] GC(1198) Phase 1: Mark live objects 131.379ms",
                    "[226.310s][info ][gc,phases,start] GC(1198) Phase 2: Compute new object addresses",
                    "[226.354s][info ][gc,phases      ] GC(1198) Phase 2: Compute new object addresses 44.149ms",
                    "[226.354s][info ][gc,phases,start] GC(1198) Phase 3: Adjust pointers",
                    "[226.432s][info ][gc,phases      ] GC(1198) Phase 3: Adjust pointers 77.897ms",
                    "[226.432s][info ][gc,phases,start] GC(1198) Phase 4: Move objects",
                    "[226.461s][info ][gc,phases      ] GC(1198) Phase 4: Move objects 28.974ms"
            },
            {   // 29
                    "[226.310s][debug][gc,phases      ] GC(1198) Class Unloading 3.481ms"
            },
            {   // 30
                    "[226.310s][debug][gc,phases      ] GC(1198) Scrub String and Symbol Tables 0.451ms"
            },
            {   // 31
                    "[278.769s][info ][gc,heap,exit   ] Heap"
            },
            {   // 32
                    "[9.381s][info][gc,marking     ] GC(877) Concurrent Mark Abort"
            },
            {   // 33
                    "[73.084s][info ][gc,marking   ] GC(263) Concurrent Mark From Roots"
            },
            {   // 34
                    "[73.138s][info ][gc,marking   ] GC(263) Concurrent Mark From Roots 53.902ms",
            },
            {   //  35
                "[1.361s][info ][gc,metaspace] GC(0) Metaspace: 9724K(9856K)->9724K(9856K) NonClass: 8859K(8896K)->8859K(8896K) Class: 864K(960K)->864K(960K)"
            },
            {   // 36
                    "[156.473s][debug][gc,phases   ] GC(2467)     Concatenate Dirty Card Logs: 0.0ms"
            },
            {   // 37
                    "[156.473s][debug][gc,phases   ] GC(2467)     Region Register: 0.1ms"
            },
            {   // 38
                    "[156.473s][debug][gc,phases   ] GC(2467)     Prepare Heap Roots: 0.0ms",
                    "[156.473s][info ][gc,phases   ] GC(2467)   Merge Heap Roots: 0.3ms",
                    "[156.473s][debug][gc,phases   ] GC(2467)     Prepare Merge Heap Roots: 0.0ms"
            },
            {   // 39
                    "[156.473s][debug][gc,phases   ] GC(2467)     Eager Reclaim (ms):            Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1"
            },
            {   // 40
                    "[156.473s][debug][gc,phases   ] GC(2467)     Remembered Sets (ms):          Min:  0.0, Avg:  0.1, Max:  0.2, Diff:  0.2, Sum:  3.6, Workers: 53"
            },
            {   // 41
                    "[156.473s][debug][gc,phases   ] GC(2467)       Merged Sparse:                 Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53",
                    "[156.473s][debug][gc,phases   ] GC(2467)       Merged Fine:                   Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53",
                    "[156.473s][debug][gc,phases   ] GC(2467)       Merged Coarse:                 Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53"
            },
            {   // 42
                    "[156.473s][debug][gc,phases   ] GC(2467)       Dirty Cards:                   Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53",
                    "[156.473s][debug][gc,phases   ] GC(2467)       Skipped Cards:                 Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53"
            },
            {   // 43
                    "[156.473s][debug][gc,phases   ] GC(2467)     Hot Card Cache (ms):           Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.1, Workers: 53",
                    "[156.473s][debug][gc,phases   ] GC(2467)       Reset Hot Card Cache (ms):     Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1"
            },
            {   // 44
                    "[156.473s][debug][gc,phases   ] GC(2467)     Log Buffers (ms):              Min:  0.0, Avg:  0.1, Max:  0.2, Diff:  0.2, Sum:  6.9, Workers: 53"
            },
            {   // 45
                    "[156.473s][debug][gc,phases   ] GC(2467)     Scan Heap Roots (ms):          Min:  0.0, Avg:  0.0, Max:  0.1, Diff:  0.1, Sum:  1.8, Workers: 53"
            },
            {   // 46
                    "[156.473s][debug][gc,phases   ] GC(2467)       Scanned Cards:                 Min: 0, Avg: 10.7, Max: 67, Diff: 67, Sum: 569, Workers: 53",
                    "[156.473s][debug][gc,phases   ] GC(2467)       Scanned Blocks:                Min: 0, Avg:  8.3, Max: 39, Diff: 39, Sum: 440, Workers: 53"
            },
            {   // 47
                    "[156.473s][debug][gc,phases   ] GC(2467)       Claimed Chunks:                Min: 0, Avg:  3.4, Max: 10, Diff: 10, Sum: 179, Workers: 53"
            },
            {   // 48
                    "[156.473s][debug][gc,phases   ] GC(2467)     Code Root Scan (ms):           Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 53"
            },
            {   // 49
                    "[156.473s][debug][gc,phases   ] GC(2467)       StringDedup Requests0 Weak     Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 7",
                    "[156.473s][debug][gc,phases   ] GC(2467)       StringDedup Requests1 Weak     Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 7",
                    "[155.889s][debug][gc,phases   ] GC(2458)       StringDedup Table Weak         Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 7"
            },
            {   // 50
                    "[156.473s][debug][gc,phases   ] GC(2467)       Weak JFR Old Object Samples    Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 7"
            },
            {   // 51
                    "[156.473s][debug][gc,phases   ] GC(2467)     Post Evacuate Cleanup 1: 0.1ms"
            },
            {   // 52
                    "[156.473s][debug][gc,phases   ] GC(2467)       Merge Per-Thread State (ms):   Min:  0.1, Avg:  0.1, Max:  0.1, Diff:  0.0, Sum:  0.1, Workers: 1"
            },
            {   // 53
                    "[156.473s][debug][gc,phases   ] GC(2467)         Copied Bytes                   Min: 0, Avg:  4.1, Max: 32, Diff: 32, Sum: 216, Workers: 53"
            },
            {   // 54
                    "[156.473s][debug][gc,phases   ] GC(2467)         LAB Waste                      Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53",
                    "[156.473s][debug][gc,phases   ] GC(2467)         LAB Undo Waste                 Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 53"
            },
            {   // 55
                    "[156.473s][debug][gc,phases   ] GC(2467)       Clear Logged Cards (ms):       Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.3, Workers: 10"
            },
            {   // 56
                    "[156.473s][debug][gc,phases   ] GC(2467)       Recalculate Used Memory (ms):  Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1"
            },
            {   // 57
                    "[156.473s][debug][gc,phases   ] GC(2467)       Purge Code Roots (ms):         Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1"
            },
            {   // 58
                    "[156.473s][debug][gc,phases   ] GC(2467)       Update Derived Pointers (ms):  Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1"
            },
            {   // 59
                    "[156.473s][debug][gc,phases   ] GC(2467)       Eagerly Reclaim Humongous Objects (ms): Min:  1.1, Avg:  1.1, Max:  1.1, Diff:  0.0, Sum:  1.1, Workers: 1"
            },
            {   // 60
                    "[156.473s][debug][gc,phases   ] GC(2467)         Humongous Total                Min: 1685, Avg: 1685.0, Max: 1685, Diff: 0, Sum: 1685, Workers: 1",
                    "[156.473s][debug][gc,phases   ] GC(2467)         Humongous Candidates           Min: 1685, Avg: 1685.0, Max: 1685, Diff: 0, Sum: 1685, Workers: 1",
                    "[156.474s][debug][gc,phases   ] GC(2467)         Humongous Reclaimed            Min: 390, Avg: 390.0, Max: 390, Diff: 0, Sum: 390, Workers: 1"
            },
            {   // 61
                    "[156.474s][debug][gc,phases   ] GC(2467)       Redirty Logged Cards (ms):     Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  1.1, Workers: 53"
            },
            {   // 62
                    "[156.474s][debug][gc,phases   ] GC(2467)         Redirtied Cards:               Min: 0, Avg:  9.1, Max: 130, Diff: 130, Sum: 482, Workers: 53"
            },
            {   // 63
                    "[156.474s][debug][gc,phases   ] GC(2467)       Free Collection Set (ms):      Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.9, Workers: 53"
            },
            {   // 64
                    "[156.474s][debug][gc,phases   ] GC(2467)     Rebuild Free List: 0.1ms"
            },
            {   // 65
                    "[156.474s][debug][gc,phases   ] GC(2467)     Start New Collection Set: 0.0ms"
            },
            {   // 66
                    "[156.474s][debug][gc,phases   ] GC(2467)     Resize TLABs: 0.0ms"
            },
            {   // 67
                    "[155.889s][debug][gc,phases   ] GC(2458)     Weak Processing: 0.0ms",
                    "[156.067s][debug][gc,phases   ] GC(2463) Weak Processing 0.189ms"
            },
            {   // 68
                    "[156.079s][debug][gc,phases   ] GC(2463) Finalize Concurrent Mark Cleanup 0.154ms"
            },
            {   // 69
                    "[155.787s][info ][gc          ] GC(2457) Concurrent Undo Cycle",
            },
            {   // 70
                    "[155.836s][info ][gc          ] GC(2457) Concurrent Undo Cycle 49.351ms",
            }

            // Remaining lines which may not need to be parsed...
            //[156.067s][debug][gc,phases   ] GC(2463) ClassLoaderData 0.002ms
            //[156.067s][debug][gc,phases   ] GC(2463) Trigger cleanups 0.000ms
            //[156.067s][debug][gc,phases   ] GC(2463) Flush Task Caches 0.206ms
            //[156.068s][debug][gc,phases   ] GC(2463) Update Remembered Set Tracking Before Rebuild 0.118ms
            //[156.068s][debug][gc,phases   ] GC(2463) Reclaim Empty Regions 0.175ms
            //[156.068s][debug][gc,phases   ] GC(2463) Purge Metaspace 0.001ms
            //[156.068s][debug][gc,phases   ] GC(2463) Report Object Count 0.000ms
            //[156.079s][debug][gc,phases   ] GC(2463) Update Remembered Set Tracking After Rebuild 0.107ms
            //[156.079s][debug][gc,phases   ] GC(2463) Finalize Concurrent Mark Cleanup 0.154ms
    };

    private String[] decoratorLines = {
            "[2018-04-04T09:10:00.586-0100][0.018s][1522825800586ms][18ms][10026341461044ns][17738937ns][1375][7427][info][gc] Using G1",
            "[0.018s][1522825800586ms][18ms][10026341461044ns][17738937ns][1375][7427][info][gc] Using G1",
            "[0.018s][1522825800586ms][7427][info][gc] Using G1",
            "[0.018s][info][gc] Using G1"
    };
}
