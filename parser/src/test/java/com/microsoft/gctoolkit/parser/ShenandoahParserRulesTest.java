// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.parser.unified.ShenandoahPatterns;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShenandoahParserRulesTest implements ShenandoahPatterns {

    private static final Logger LOGGER = Logger.getLogger(ShenandoahParserRulesTest.class.getName());

   @Test
    public void testParseRules() {
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

    private int captureTest(GCParseRule rule, String[] lines) {
        int captureCount = 0;
        for (String line : lines) {
            GCLogTrace trace = rule.parse(line);
            if (rule.parse(line) != null) {
                captureCount++;
            }
        }
        return captureCount;
    }

    // Convenience test for debugging single rules
    //@Test
    public void testSingeRule() {
        int index = 9;
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
            CONCURRENT,                 //   0
            CLEANUP,
            WORKERS,
            PAUSE,
            PACER,
            CSET_SELECTION,             //   5
            COLLECTABLE,
            FREE,
            METASPACE,
            TRIGGER,
            HEADROOM,                   //  10
            INFO,
            ADVICE1,
            REGION,
            HUMONGOUS_OBJECT_THRESHOLD,
            TLAB,                       //  15
            THREADS,
            MODE,
            HEURISTICS,
            INIT,
            SAFEPOINTING,
            SHENANDOAH_TAG,
            HEAP,
            REFERENCE
    };

    private static final String[][] lines = {
            {  //   0
                    "[31.818s][info][gc,start      ] GC(7) Concurrent reset",
                    "[31.953s][info][gc            ] GC(7) Concurrent reset 134.638ms",
                    "[31.960s][info][gc,start      ] GC(7) Concurrent marking",
                    "[35.488s][info][gc            ] GC(7) Concurrent marking 3526.876ms",
                    "[35.497s][info][gc,start      ] GC(7) Concurrent cleanup",
                    "[35.497s][info][gc,start      ] GC(7) Concurrent evacuation",
                    "[35.970s][info][gc            ] GC(7) Concurrent evacuation 464.639ms",
                    "[35.999s][info][gc,start      ] GC(7) Concurrent update references",
                    "[36.589s][info][gc            ] GC(7) Concurrent update references 589.570ms",
                    "[36.591s][info][gc,start      ] GC(7) Concurrent cleanup"
            },
            {  //   1
                    "[35.497s][info][gc            ] GC(7) Concurrent cleanup 6652M->6612M(8192M) 0.056ms"
            },
            {  //   2
                    "[31.818s][info][gc,task       ] GC(7) Using 2 of 4 workers for concurrent reset",
                    "[31.955s][info][gc,task       ] GC(7) Using 4 of 4 workers for init marking",
                    "[31.960s][info][gc,task       ] GC(7) Using 2 of 4 workers for concurrent marking",
                    "[35.492s][info][gc,task       ] GC(7) Using 4 of 4 workers for final marking",
                    "[35.498s][info][gc,task       ] GC(7) Using 2 of 4 workers for concurrent evacuation",
                    "[35.999s][info][gc,task       ] GC(7) Using 2 of 4 workers for concurrent reference update",
                    "[36.589s][info][gc,task       ] GC(7) Using 4 of 4 workers for final reference update"
            },
            {  //   3
                    "[31.954s][info][gc,start      ] GC(7) Pause Init Mark",
                    "[31.960s][info][gc            ] GC(7) Pause Init Mark 5.454ms",
                    "[35.492s][info][gc,start      ] GC(7) Pause Final Mark",
                    "[35.496s][info][gc            ] GC(7) Pause Final Mark 4.257ms",
                    "[35.996s][info][gc,start      ] GC(7) Pause Init Update Refs",
                    "[35.999s][info][gc            ] GC(7) Pause Init Update Refs 2.569ms",
                    "[36.589s][info][gc,start      ] GC(7) Pause Final Update Refs",
                    "[36.591s][info][gc            ] GC(7) Pause Final Update Refs 1.685ms"
            },
            {  //   4
                    "[31.818s][info][gc,ergo       ] GC(7) Pacer for Reset. Non-Taxable: 8192M",
                    "[31.960s][info][gc,ergo       ] GC(7) Pacer for Mark. Expected Live: 1014M, Free: 1244M, Non-Taxable: 124M, Alloc Tax Rate: 3.0x",
                    "[35.496s][info][gc,ergo       ] GC(7) Pacer for Evacuation. Used CSet: 788M, Free: 1132M, Non-Taxable: 113M, Alloc Tax Rate: 1.7x",
                    "[35.999s][info][gc,ergo       ] GC(7) Pacer for Update Refs. Used: 6956M, Free: 1127M, Non-Taxable: 112M, Alloc Tax Rate: 7.5x",
                    "[36.592s][info][gc,ergo       ] Pacer for Idle. Initial: 163M, Alloc Tax Rate: 1.0x"
            },
            {  //   5
                    "[35.494s][info][gc,ergo       ] GC(7) Adaptive CSet Selection. Target Free: 1160M, Actual Free: 1544M, Max CSet: 341M, Min Garbage: 0B"
            },
            {  //   6
                    "[35.494s][info][gc,ergo       ] GC(7) Collectable Garbage: 488M (17%), Immediate: 40960K (1%), CSet: 448M (16%)"
            },
            {  //   7
                    "[35.497s][info][gc,ergo       ] GC(7) Free: 1127M, Max: 4096K regular, 260M humongous, Frag: 77% external, 0% internal; Reserve: 411M, Max: 4096K",
                    "[36.592s][info][gc,ergo       ] Free: 1536M, Max: 4096K regular, 260M humongous, Frag: 84% external, 1% internal; Reserve: 412M, Max: 4096K",
                    "[37.857s][info][gc,ergo       ] Free: 1252M, Max: 4096K regular, 260M humongous, Frag: 80% external, 0% internal; Reserve: 412M, Max: 4096K"
            },
            {  //   8
                    "[36.592s][info][gc,metaspace  ] Metaspace: 51709K->51724K(1095680K)"
            },
            {  //   9
                    "[37.857s][info][gc            ] Trigger: Average GC time (1377.92 ms) is above the time for allocation rate (80715 KB/s) to deplete free headroom (105M)",
                    "[0.876s][info][gc           ] Trigger: Metadata GC Threshold"
            },
            {  //  10
                    "[37.857s][info][gc,ergo       ] Free headroom: 1252M (free) - 409M (spike) - 737M (penalties) = 105M",
            },
            {  //  11
                    "[0.002s][info][gc] Min heap equals to max heap, disabling ShenandoahUncommit"
            },
            {  //  12
                    "[0.002s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are observed on class-unloading sensitive workloads"
            },
            {  //  13
                    "[0.004s][info][gc,init] Regions: 2048 x 4096K"
            },
            {  // 14
                    "[0.004s][info][gc,init] Humongous object threshold: 4096K\n"
            },
            {  //  15
                    "[0.004s][info][gc,init] Max TLAB size: 4096K"
            },
            {  //  16
                    "[0.004s][info][gc,init] GC threads: 4 parallel, 2 concurrent"
            },
            {  //  17
                    "[0.004s][info][gc,init] Shenandoah GC mode: Normal",
                    "[0.004s][info][gc,init] Shenandoah heuristics: adaptive"
            },
            {  //  18
                    "[0.004s][info][gc     ] Heuristics ergonomically sets -XX:+ExplicitGCInvokesConcurrent",
                    "[0.004s][info][gc     ] Heuristics ergonomically sets -XX:+ShenandoahImplicitGCInvokesConcurrent"
            },
            {  //  19
                    "[0.004s][info][gc,init] Initialize Shenandoah heap: 8192M initial, 8192M min, 8192M max"
            },
            {  //  20
                    "[0.004s][info][gc,init] Safepointing mechanism: global-page poll"
            },
            {  //  21
                    "[0.004s][info][gc     ] Using Shenandoah"
            },
            {  //  22
                    "[0.004s][info][gc,heap,coops] Heap address: 0x0000000600000000, size: 8192 MB, Compressed Oops mode: Zero based, Oop shift amount: 3"
            },
            {  //  23
                    "[0.013s][info][gc,init      ] Reference processing: parallel discovery, parallel processing"
            }
    };
}
