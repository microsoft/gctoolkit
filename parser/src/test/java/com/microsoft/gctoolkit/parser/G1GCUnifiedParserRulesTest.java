// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.parser.unified.UnifiedG1GCPatterns;
import com.microsoft.gctoolkit.parser.jvm.Decorators;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static com.microsoft.gctoolkit.parser.CommonTestHelper.captureTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class G1GCUnifiedParserRulesTest implements UnifiedG1GCPatterns {

    private static final Logger LOGGER = Logger.getLogger(G1GCUnifiedParserRulesTest.class.getName());

    @Test
    public void testG1GCParseRules() {
        for (int i = 0; i < rules.length; i++)
            for (int j = 0; j < lines.length; j++) {
                int captured = captureTest(rules[i], lines[j]);
                if (i == j) {
                    assertTrue(captured == lines[j].length, i + " failed to captured it's lines");
                } else {
                    assertTrue(captured == 0, i + " captured " + j);
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

    //@Test
    public void testSingeRule() {
        int index = 0;
        assertTrue(captureTest(rules[index], lines[index]) == lines[index].length);
    }


    private void evaluate(GCParseRule rule, String string, boolean dump) {

        GCLogTrace trace = rule.parse(string);
        assertTrue(trace != null);
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
            FINIALIZE_MARKING,
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
            CONCURRENT_MARK_PHASE_DURATION
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
                    "[2018-03-09T11:14:05.001-0100][12.276s][debug][gc,ref       ] GC(0) JNI Weak Reference 0.049ms"
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
                    "[7.984s][info][gc            ] GC(420) Pause Young (Mixed) (G1 Evacuation Pause) 26M->11M(64M) 1.453ms"
            },
            {   //  9
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,cpu       ] GC(0) User=0.04s Sys=0.01s Real=0.01s"
            },
            {   // 10
                    "[73.082s][info ][gc           ] GC(263) Concurrent Cycle"
            },
            {   // 11
                    "[73.082s][info ][gc,marking   ] GC(263) Concurrent Clear Claimed Marks",
                    "[73.082s][info ][gc,marking   ] GC(263) Concurrent Scan Root Regions",
                    "[73.160s][info ][gc,marking    ] GC(263) Concurrent Create Live Data",
                    "[73.169s][info ][gc,marking    ] GC(263) Concurrent Complete Cleanup",
                    "[73.169xs][info ][gc,marking    ] GC(263) Concurrent Cleanup for Next Mark"
            },
            {   // 12
                    "[73.171s][info ][gc            ] GC(263) Concurrent Cycle 89.437ms"
            },
            {   // 13
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
            {
                    "[73.084s][info ][gc,marking   ] GC(263) Concurrent Mark From Roots"
            },
            {
                    "[73.138s][info ][gc,marking   ] GC(263) Concurrent Mark From Roots 53.902ms",
            }
    };

    private String[] decoratorLines = {
            "[2018-04-04T09:10:00.586-0100][0.018s][1522825800586ms][18ms][10026341461044ns][17738937ns][1375][7427][info][gc] Using G1",
            "[0.018s][1522825800586ms][18ms][10026341461044ns][17738937ns][1375][7427][info][gc] Using G1",
            "[0.018s][1522825800586ms][7427][info][gc] Using G1",
            "[0.018s][info][gc] Using G1"
    };
}
