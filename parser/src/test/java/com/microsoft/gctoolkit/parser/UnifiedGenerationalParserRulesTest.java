// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.parser.unified.UnifiedGenerationalPatterns;
import com.microsoft.gctoolkit.parser.jvm.Decorators;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static com.microsoft.gctoolkit.parser.CommonTestHelper.captureTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnifiedGenerationalParserRulesTest implements UnifiedGenerationalPatterns {

    private static final Logger LOGGER = Logger.getLogger(UnifiedGenerationalParserRulesTest.class.getName());

    @Test
    public void testGenerationalParseRules() {
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

    // Convenience test for debugging single rules
    //@Test
    public void testSingeRule() {
        int index = 0;
        assertTrue(captureTest(rules[index], lines[index]) == 4);
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

    private static final String PARALLEL_PHASES = "(Marking Phase|Summary Phase|Adjust Roots|Compaction Phase|Post Compact)";

    private GCParseRule[] rules = {
            YOUNG_DETAILS,                  //   0
            CPU_BREAKOUT,
            YOUNG_HEADER,
            GENERATIONAL_MEMORY_SUMMARY,
            GENERATIONAL_MEMORY_SUMMARY_EXTENDED,
            INITIAL_MARK,                   //   5
            INITIAL_MARK_SUMMARY,
            CONCURRENT_PHASE_START,
            CONCURRENT_PHASE_END,
            WORKER_THREADS,
            REMARK,                         //  10
            REMARK_SUMMARY,
            GC_PHASE,
            PROMOTION_FAILED,
            FULL_GC,
            FULL_GC_SUMMARY,                //  15
            FULL_GC_PHASE_START,
            FULL_GC_PHASE_END,
            PRE_COMPACT,
            PARALLEL_PHASE,
            PARALLEL_PHASE_SUMMARY,         //  20
            METASPACE_DETAILED
    };

    private String[][] lines = {
            {   //  0
                    "[0.170s][info ][gc           ] GC(1) Pause Young (Allocation Failure) 19M->2M(61M) 5.221ms",
            },
            {   //  1
                    "[2018-03-09T11:14:05.002-0100][12.277s][info ][gc,cpu       ] GC(0) User=0.04s Sys=0.01s Real=0.01s"
            },
            {   //  2
                    "[0.165s][info ][gc,start     ] GC(1) Pause Young (Allocation Failure)"
            },
            {   //  3
                    "[0.170s][info ][gc,heap      ] GC(1) ParNew: 19356K->1696K(19648K)",
                    "[0.170s][info ][gc,heap      ] GC(1) CMS: 130K->1179K(43712K)",
                    "[10.026s][info ][gc,heap      ] GC(0) PSYoungGen: 16384K->2559K(18944K)",
                    "[10.026s][info ][gc,heap      ] GC(0) ParOldGen: 0K->2121K(44032K)",
                    "[11.910s][info ][gc,heap      ] GC(0) DefNew: 17294K->2176K(19648K)",
                    "[11.910s][info ][gc,heap      ] GC(0) Tenured: 0K->1387K(43712K)"
            },
            {   //  4
                    "PSYoungGen: 53228K(53248K)->7148K(53248K) Eden: 46080K(46080K)->0K(46080K) From: 7148K(7168K)->7148K(7168K)",
                    "[0.694s][info][gc,heap      ] GC(2) ParOldGen: 65568K(121856K)->111512K(121856K)"
            },
            {  //   4
                    "[0.278s][info ][gc,start     ] GC(35) Pause Initial Mark"
            },
            {  //   5
                    "[0.279s][info ][gc           ] GC(35) Pause Initial Mark 29M->29M(61M) 0.184ms"
            },
            {  //   6
                    "[0.279s][info ][gc           ] GC(35) Concurrent Mark",
                    "[0.282s][info ][gc           ] GC(35) Concurrent Preclean",
                    "[0.284s][info ][gc           ] GC(35) Concurrent Sweep",
                    "[0.285s][info ][gc           ] GC(35) Concurrent Reset"
            },
            {  //   7
                    "[0.280s][info ][gc           ] GC(35) Concurrent Mark 1.553ms",
                    "[0.282s][info ][gc           ] GC(35) Concurrent Preclean 0.172ms",
                    "[0.285s][info ][gc           ] GC(35) Concurrent Sweep 0.543ms",
                    "[0.285s][info ][gc           ] GC(35) Concurrent Sweep 0.543ms",
                    "[0.285s][info ][gc           ] GC(35) Concurrent Reset 0.323ms"
            },
            {  //   8
                    "[0.279s][info ][gc,task      ] GC(35) Using 2 workers of 2 for marking"
            },
            {  //   9
                    "[0.283s][info ][gc,start     ] GC(35) Pause Remark"
            },
            {  //  10
                    "[0.284s][info ][gc           ] GC(35) Pause Remark 26M->26M(61M) 1.736ms"
            },
            {  //  11
                    "[10.025s][debug][gc,phases    ] GC(0) Scavenge 4.410ms",
                    "[10.118s][debug][gc,phases      ] GC(25) Par Mark 2.420ms",
                    "[0.284s][debug][gc,phases    ] GC(35) Rescan (parallel) 1.002ms",
                    "[0.284s][debug][gc,phases    ] GC(35) Reference Processing 0.016ms",
                    "[0.284s][debug][gc,phases    ] GC(35) Weak Processing 0.003ms",
                    "[0.284s][debug][gc,phases    ] GC(35) ClassLoaderData 0.020ms",
                    "[0.284s][debug][gc,phases    ] GC(35) ProtectionDomainCacheTable 0.002ms",
                    "[0.284s][debug][gc,phases    ] GC(35) ResolvedMethodTable 0.006ms",
                    "[0.284s][debug][gc,phases    ] GC(35) Class Unloading 0.172ms",
                    "[0.284s][debug][gc,phases    ] GC(35) Scrub Symbol Table 0.430ms",
                    "[0.284s][debug][gc,phases    ] GC(35) Scrub String Table 0.020ms"
            },
            {  //  12
                    "[1.017s][info ][gc,promotion ] Promotion failed"
            },
            {  //  13
                    "[1.017s][info ][gc,start     ] GC(309) Pause Full (Allocation Failure)"
            },
            {  //  14
                    "[1.022s][info ][gc             ] GC(309) Pause Full (Allocation Failure) 60M->3M(61M) 4.853ms"
            },
            {  //  15
                    "[1.017s][info ][gc,phases,start] GC(309) Phase 1: Mark live objects",
                    "[1.019s][info ][gc,phases,start] GC(309) Phase 2: Compute new object addresses",
                    "[1.020s][info ][gc,phases,start] GC(309) Phase 3: Adjust pointers",
                    "[1.021s][info ][gc,phases,start] GC(309) Phase 4: Move objects"
            },
            {  //  16
                    "[1.019s][info ][gc,phases      ] GC(309) Phase 1: Mark live objects 1.788ms",
                    "[1.020s][info ][gc,phases      ] GC(309) Phase 2: Compute new object addresses 0.485ms",
                    "[1.021s][info ][gc,phases      ] GC(309) Phase 3: Adjust pointers 1.138ms",
                    "[1.022s][info ][gc,phases      ] GC(309) Phase 4: Move objects 0.903ms",
            },
            {  //  17
                    "[10.115s][debug][gc,phases    ] GC(25) Pre Compact 0.022ms"
            },
            {  //  18
                    "[10.116s][info ][gc,phases,start] GC(25) Marking Phase",
                    "[10.120s][info ][gc,phases,start] GC(25) Summary Phase",
                    "[10.120s][info ][gc,phases,start] GC(25) Adjust Roots",
                    "[10.122s][info ][gc,phases,start] GC(25) Compaction Phase",
                    "[10.128s][info ][gc,phases,start] GC(25) Post Compact"
            },
            {  //  19
                    "[10.120s][info ][gc,phases      ] GC(25) Marking Phase 4.518ms",
                    "[10.120s][info ][gc,phases      ] GC(25) Summary Phase 0.013ms",
                    "[10.122s][info ][gc,phases      ] GC(25) Adjust Roots 2.423ms",
                    "[10.128s][info ][gc,phases      ] GC(25) Compaction Phase 5.461ms",
                    "[10.129s][info ][gc,phases      ] GC(25) Post Compact 0.974ms"
            },
            {  //  20
                    "[1.208s][info][gc,metaspace   ] GC(3) Metaspace: 3646K(4864K)->3646K(4864K) NonClass: 3271K(4352K)->3271K(4352K) Class: 375K(512K)->375K(512K)"
            }
    };

    private String[] decoratorLines = {
            "[2018-04-04T09:10:00.586-0100][0.018s][1522825800586ms][18ms][10026341461044ns][17738937ns][1375][7427][info][gc] Using Concurrent Mark Sweep",
            "[0.018s][1522825800586ms][18ms][10026341461044ns][17738937ns][1375][7427][info][gc] Using Concurrent Mark Sweep",
            "[0.018s][1522825800586ms][7427][info][gc] Using Concurrent Mark Sweep",
            "[0.018s][info][gc] Using Concurrent Mark Sweep"
    };
}
