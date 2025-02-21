// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.parser.jvm.Decorators;
import com.microsoft.gctoolkit.parser.unified.ZGCPatterns;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class ZGCParserRulesTest implements ZGCPatterns {

    private static final Logger LOGGER = Logger.getLogger(ZGCParserRulesTest.class.getName());

   @Test
    public void testZGCParseRules() {
        for (int i = 0; i < rules.length; i++)
            for (int j = 0; j < lines.length; j++) {
                int captured = captureTest(rules[i], lines[j]);
                if (i == j) {
                    assertEquals(captured, lines[j].length, i + " failed to captured it's lines");
                } else {
                    assertEquals(0, captured, i + " captured " + j);
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
            if (trace != null) {
                captureCount++;
            }
        }
        return captureCount;
    }

    // Convenience test for debugging single rules
    // @Test
    public void testSingeRule() {
        int index = 14;
        assertEquals(captureTest(rules[index], lines[index]), lines[index].length);
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
            CYCLE_START,                   //  0
            PAUSE_PHASE,                   //  1
            CONCURRENT_PHASE,
            LOAD,
            MMU,
            MARK_SUMMARY,                  //  5
            NMETHODS,
            METASPACE,
            REFERENCE_PROCESSING,
            CAPACITY,
            MEMORY_TABLE_HEADER,            // 10
            MEMORY_TABLE_ENTRY_SIZE,
            MEMORY_TABLE_ENTRY_OCCUPANCY,
            MEMORY_TABLE_ENTRY_RECLAIMED_PROMOTED,
            MEMORY_SUMMARY
    };

   private String[][] lines = {
           { // 0
                   "[32.121s][info][gc,start    ] GC(2) Garbage Collection (Metadata GC Threshold)",
           },
           { // 1
                   "[32.121s][info][gc,phases   ] GC(2) Pause Mark Start 0.023ms",
                   "[32.179s][info][gc,phases   ] GC(2) Pause Relocate Start 0.024ms",
                   "[32.166s][info][gc,phases   ] GC(2) Pause Mark End 0.029ms",
           },
           { // 2
                   "[32.166s][info][gc,phases   ] GC(2) Concurrent Mark 44.623ms",
                   "[32.166s][info][gc,phases   ] GC(2) Concurrent Mark Free 0.001ms",
                   "[32.172s][info][gc,phases   ] GC(2) Concurrent Process Non-Strong References 5.797ms",
                   "[32.172s][info][gc,phases   ] GC(2) Concurrent Reset Relocation Set 0.012ms",
                   "[32.178s][info][gc,phases   ] GC(2) Concurrent Select Relocation Set 6.446ms",
                   "[32.193s][info][gc,phases   ] GC(2) Concurrent Relocate 14.013ms",
           },
           { // 3
                   "[32.193s][info][gc,load     ] GC(2) Load: 7.28/6.63/5.01",
           },
           { // 4
                   "[32.193s][info][gc,mmu      ] GC(2) MMU: 2ms/98.2%, 5ms/99.3%, 10ms/99.5%, 20ms/99.7%, 50ms/99.9%, 100ms/99.9%",
           },
           { // 5
                   "[32.193s][info][gc,marking  ] GC(2) Mark: 4 stripe(s), 3 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s) ",
           },
           { // 6
                   "[32.193s][info ][gc,nmethod ] GC(2) NMethods: 1163 registered, 0 unregistered"
           },
           { // 7
                   "[32.193s][info][gc,metaspace] GC(2) Metaspace: 60M used, 60M committed, 1080M reserved",
           },
           { // 8
                   "[32.193s][info][gc,ref      ] GC(2) Soft: 5447 encountered, 0 discovered, 0 enqueued",
                   "[32.193s][info][gc,ref      ] GC(2) Weak: 5347 encountered, 2016 discovered, 810 enqueued",
                   "[32.193s][info][gc,ref      ] GC(2) Final: 1041 encountered, 113 discovered, 105 enqueued",
                   "[32.193s][info][gc,ref      ] GC(2) Phantom: 558 encountered, 501 discovered, 364 enqueued",
           },
           { // 9
                   "[32.193s][info][gc,heap     ] GC(2) Min Capacity: 8M(0%)",
                   "[32.193s][info][gc,heap     ] GC(2) Max Capacity: 28686M(100%)",
                   "[32.193s][info][gc,heap     ] GC(2) Soft Max Capacity: 28686M(100%)",
           },
           { // 10
                    "[32.193s][info][gc,heap     ] GC(2)                Mark Start          Mark End        Relocate Start      Relocate End           High               Low         ",
           },
           { // 11
                   "[32.193s][info][gc,heap     ] GC(2)  Capacity:     1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)     ",
                   "[32.193s][info][gc,heap     ] GC(2)      Free:    28128M (98%)       28110M (98%)       28148M (98%)       28560M (100%)      28560M (100%)      28108M (98%)    ",
                   "[32.193s][info][gc,heap     ] GC(2)      Used:      558M (2%)          576M (2%)          538M (2%)          126M (0%)          578M (2%)          126M (0%)     ",
           },
           { // 12
                   "[32.193s][info][gc,heap     ] GC(2)      Live:         -                71M (0%)           71M (0%)           71M (0%)             -                  -",
                   "[32.193s][info][gc,heap     ] GC(2) Allocated:         -                18M (0%)           20M (0%)           18M (0%)             -                  -",
                   "[32.193s][info][gc,heap     ] GC(2)   Garbage:         -               486M (2%)          446M (2%)           35M (0%)             -                  -",
           },
           { // 13
                   "[32.193s][info][gc,heap     ] GC(2) Reclaimed:         -                  -                40M (0%)          450M (2%)             -                  -",

           },
           { // 14
                   "[32.193s][info][gc          ] GC(2) Garbage Collection (Metadata GC Threshold) 558M(2%)->126M(0%)",
           }
   };

    private String[] decoratorLines = {
            "[2018-04-04T09:10:00.586-0100][0.018s][1522825800586ms][18ms][10026341461044ns][17738937ns][1375][7427][info][gc] Using Concurrent Mark Sweep",
            "[0.018s][1522825800586ms][18ms][10026341461044ns][17738937ns][1375][7427][info][gc] Using Concurrent Mark Sweep",
            "[0.018s][1522825800586ms][7427][info][gc] Using Concurrent Mark Sweep",
            "[0.018s][info][gc] Using Concurrent Mark Sweep"
    };
}
