// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.patterns;

import com.microsoft.gctoolkit.parser.G1GCPatterns;
import com.microsoft.gctoolkit.parser.GCParseRule;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static com.microsoft.gctoolkit.parser.CommonTestHelper.captureTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class G1GCPatternsTest implements G1GCPatterns {

    private static final Logger LOGGER = Logger.getLogger(G1GCPatternsTest.class.getName());

    @Test
    public void testParallelParseRules() {
        for (int i = 0; i < rules.length; i++)
            for (int j = 0; j < lines.length; j++) {
                int captured = captureTest(rules[i], lines[j]);
                if (i == j) {
                    assertTrue(captured == lines[j].length, i + " failed to captured it's lines");
                } else {
                    assertTrue(captured == 0, i + " captured " + j);
                }
            }
    }

    /* Code that is useful when testing individual records */

//    private boolean debugging = Boolean.getBoolean("microsoft.debug");
//
//    @Test
//    public void testDebugParallelParseRules() {
//        int index = 10;
//        GCParseRule rule = rules[index];
//        assertEquals(lines[index].length, captureTest( rule, lines[index]));
//    }


    private GCParseRule[] rules = {
            G1_CONCURRENT_START,
            G1_CONCURRENT_END,
            G1_REMARK,
            G1_CLEANUP,
            G1_MEMORY_SUMMARY,
            G1_DETAILS,
            YOUNG,
            FULL_GC,
            SPLIT_CLEANUP,
            FULL_WITH_CONCURRENT_PHASE_START,
            DELAY_MIXED_GC,
            START_MIXED_GC
    };

    private String[][] lines = {
            { // G1_CONCURRENT_START
                    "657.421: [GC concurrent-root-region-scan-start]",
                    "657.426: [GC concurrent-mark-start]",
                    "659.265: [GC concurrent-cleanup-start]"
            },
            { // G1_CONCURRENT_END,
                    "657.426: [GC concurrent-root-region-scan-end, 0.0052220 secs]",
                    "659.250: [GC concurrent-mark-end, 1.8243760 secs]",
                    "659.265: [GC concurrent-cleanup-end, 0.0000200 secs]"
            },
            { // G1_REMARK,
                    "659.250: [GC remark 659.251: [GC ref-proc, 0.0001790 secs], 0.0111120 secs]",
                    "9.251: [GC remark, 0.0012190 secs]",
                    "6.298: [GC remark 6.298: [GC ref-proc, 0.0000570 secs], 0.0010940 secs]",
                    "2014-02-21T16:04:24.321-0100: 7.852: [GC remark 2014-02-21T16:04:24.322-0100: 7.853: [GC ref-proc, 0.0000640 secs], 0.0013310 secs]"
            },
            { // G1_CLEANUP
                    "659.262: [GC cleanup 5791M->5789M(6244M), 0.0027400 secs]"
            },
            { // G1_MEMORY_SUMMARY,
                    "[Eden: 1792.0M(1792.0M)->0.0B(1740.0M) Survivors: 86.0M->80.0M Heap: 3764.1M(6144.0M)->1967.3M(6144.0M)]",
                    "[Eden: 1792,0M(1792,0M)->0,0B(1740,0M) Survivors: 86,0M->80,0M Heap: 3764,1M(6144,0M)->1967,3M(6144,0M)]",
                    "[Eden: 128.0M(512.0M)->0.0B(512.0M) Survivors: 0.0B->0.0B Heap: 147.8M(518.0M)->23.1M(518.0M)], [Metaspace: 35052K->35051K(1079296K)]"
            },
            { // G1_DETAILS
                    "7.495: [GC pause (young), 0.0025420 secs]",
                    "7.490: [GC pause (G1 Evacuation Pause) (young), 0.0025420 secs]",
                    "7.498: [GC pause (G1 Evacuation Pause) (mixed), 0.0026410 secs]",
                    "26.893: [GC pause (G1 Evacuation Pause) (young) (to-space exhausted), 0.1709670 secs]",
                    "7.477: [GC pause (G1 Evacuation Pause) (young) (initial-mark), 0.0042120 secs]"
            },
//            { // unknown source
//                "8.495: [GC pause (young)"
//            },
            { // YOUNG, no details
                    "369310.802: [GC pause (young) 485M->240M(512M), 0.0558340 secs]",
                    "369447.597: [GC pause (young) (initial-mark) 485M->239M(512M), 0.0719290 secs]",
                    "369674.919: [GC pause (mixed) 482M->185M(512M), 0.0679470 secs]",
                    "0.583: [GC pause (G1 Evacuation Pause) (young) 24M->4561K(256M), 0.0047007 secs]"
            },
            { // FULL_GC, no details 1.8.0_102
                    "16.603: [Full GC (System.gc())  14M->3334K(11M), 0.0230975 secs]"
            },
            {
                    "2017-10-17T08:48:58.956+0000: 8.809: [GC cleanup"
            },
            {
                    "2018-01-29T17:34:24.984+0000: 5115.588: [Full GC (Metadata GC Threshold) 2018-01-29T17:34:24.984+0000: 5115.588: [GC concurrent-root-region-scan-start]"
            },
            {
                    "121.597: [G1Ergonomics (Mixed GCs) do not start mixed GCs, reason: concurrent cycle is about to start]"
            },
            {
                    "7.986: [G1Ergonomics (Mixed GCs) do not start mixed GCs, reason: reclaimable percentage not over threshold, candidate old regions: 3 regions, reclaimable: 1380376 bytes (2.53 %), threshold: 10.00 %]"
            }

    };
}
