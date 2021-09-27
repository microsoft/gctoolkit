// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.parser.unified.ZGCPatterns;
import com.microsoft.gctoolkit.parser.jvm.Decorators;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZGCParserRulesTest implements ZGCPatterns {

    private static final Logger LOGGER = Logger.getLogger(ZGCParserRulesTest.class.getName());

   @Test
    public void testZGCParseRules() {
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
        int index = 14;
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
            CYCLE_START,                   //  0
            PAUSE_PHASE,                   //  1
            CONCURRENT_PHASE,
            LOAD,
            MMU,
            MARK_SUMMARY,                  //  5
            RELOCATION_SUMMARY,
            NMETHODS,
            METASPACE,
            REFERENCE_PROCESSING,
            CAPACITY,                      // 10
            MEMORY_TABLE_HEADER,
            MEMORY_TABLE_ENTRY_SIZE,
            MEMORY_TABLE_ENTRY_OCCUPANCY,
            MEMORY_TABLE_ENTRY_RECLAIMED,
            MEMORY_SUMMARY
    };

    private String[][] lines = {

            {  //   0
                    "[3.558s][info ][gc,start       ] GC(3) Garbage Collection (Warmup)"
            },
            {  //   1
                    "[3.559s][info ][gc,phases      ] GC(3) Pause Mark Start 0.460ms",
                    "[3.574s][info ][gc,phases      ] GC(3) Pause Mark End 0.830ms",
                    "[3.583s][info ][gc,phases      ] GC(3) Pause Relocate Start 0.794ms"
            },
            {  //   2
                    "[3.573s][info ][gc,phases      ] GC(3) Concurrent Mark 14.621ms",
                    "[3.578s][info ][gc,phases      ] GC(3) Concurrent Process Non-Strong References 3.654ms",
                    "[3.578s][info ][gc,phases      ] GC(3) Concurrent Reset Relocation Set 0.194ms",
                    "[3.582s][info ][gc,phases      ] GC(3) Concurrent Select Relocation Set 3.193ms",
                    "[3.596s][info ][gc,phases      ] GC(3) Concurrent Relocate 12.962ms"
            },
            {  //   3
                    "[3.596s][info ][gc,load        ] GC(3) Load: 4.28/3.95/3.22"
            },
            {  //   4
                    "[3.596s][info ][gc,mmu         ] GC(3) MMU: 2ms/32.7%, 5ms/60.8%, 10ms/80.4%, 20ms/85.4%, 50ms/90.8%, 100ms/95.4%"
            },
            {  //   5
                    "[3.596s][info ][gc,marking     ] GC(3) Mark: 1 stripe(s), 2 proactive flush(es), 1 terminate flush(es), 1 completion(s), 0 continuation(s)"
            },
            {  //   6
                    "[3.596s][info ][gc,reloc       ] GC(3) Relocation: Successful, 6M relocated"
            },
            {  //   7
                    "[3.596s][info ][gc,nmethod     ] GC(3) NMethods: 1163 registered, 0 unregistered"
            },
            {  //   8
                    "[3.596s][info ][gc,metaspace   ] GC(3) Metaspace: 14M used, 15M capacity, 15M committed, 16M reserved"
            },
            {  //   9
                    "[3.596s][info ][gc,ref         ] GC(3) Soft: 391 encountered, 0 discovered, 0 enqueued",
                    "[3.596s][info ][gc,ref         ] GC(3) Weak: 587 encountered, 466 discovered, 0 enqueued",
                    "[3.596s][info ][gc,ref         ] GC(3) Final: 799 encountered, 0 discovered, 0 enqueued",
                    "[3.596s][info ][gc,ref         ] GC(3) Phantom: 33 encountered, 1 discovered, 0 enqueued",
            },
            {  //  10
                    "[3.596s][info ][gc,heap        ] GC(3) Min Capacity: 8M(0%)",
                    "[3.596s][info ][gc,heap        ] GC(3) Max Capacity: 4096M(100%)",
                    "[3.596s][info ][gc,heap        ] GC(3) Soft Max Capacity: 4096M(100%)"
            },
            {  //  11
                    "[3.596s][info ][gc,heap        ] GC(3)                Mark Start          Mark End        Relocate Start      Relocate End           High               Low"
            },
            {  //  12
                    "[3.596s][info ][gc,heap        ] GC(3)  Capacity:      936M (23%)        1074M (26%)        1074M (26%)        1074M (26%)        1074M (26%)         936M (23%)",
                    "[3.596s][info ][gc,heap        ] GC(3)   Reserve:       42M (1%)           42M (1%)           42M (1%)           42M (1%)           42M (1%)           42M (1%)",
                    "[3.596s][info ][gc,heap        ] GC(3)      Free:     3160M (77%)        3084M (75%)        3852M (94%)        3868M (94%)        3930M (96%)        3022M (74%)",
                    "[3.596s][info ][gc,heap        ] GC(3)      Used:      894M (22%)         970M (24%)         202M (5%)          186M (5%)         1032M (25%)         124M (3%)"
            },
            {  //  13
                    "[3.596s][info ][gc,heap        ] GC(3)      Live:         -                 8M (0%)            8M (0%)            8M (0%)             -                  -",
                    "[3.596s][info ][gc,heap        ] GC(3) Allocated:         -               172M (4%)          172M (4%)          376M (9%)             -                  -",
                    "[3.596s][info ][gc,heap        ] GC(3)   Garbage:         -               885M (22%)         117M (3%)            5M (0%)             -                  -"
            },
            {  //  14
                    "[3.596s][info ][gc,heap        ] GC(3) Reclaimed:         -                  -               768M (19%)         880M (21%)            -                  -"
            },
            {  //  15
                    "[3.596s][info ][gc             ] GC(3) Garbage Collection (Warmup) 894M(22%)->186M(5%)"
            }
    };

    private String[] decoratorLines = {
            "[2018-04-04T09:10:00.586-0100][0.018s][1522825800586ms][18ms][10026341461044ns][17738937ns][1375][7427][info][gc] Using Concurrent Mark Sweep",
            "[0.018s][1522825800586ms][18ms][10026341461044ns][17738937ns][1375][7427][info][gc] Using Concurrent Mark Sweep",
            "[0.018s][1522825800586ms][7427][info][gc] Using Concurrent Mark Sweep",
            "[0.018s][info][gc] Using Concurrent Mark Sweep"
    };
}
