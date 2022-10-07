// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;


import com.microsoft.gctoolkit.event.jvm.Safepoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SafepointPatternsTest implements SafepointPatterns {


//    @Test
    public void testPreUnifiedSafepointPatternsRules() {
        String[] lines = {
                "0.099: Deoptimize                       [       9          0              0    ]      [     0     0     0     0     0    ]  0",
                "1.474: no vm operation                  [      13          0              0    ]      [     0     0     0     0     0    ]  0",
                "4.088: EnableBiasedLocking              [      13          0              0    ]      [     0     0     0     0     0    ]  0",
                "19.135: GenCollectForAllocation          [      14          0              0    ]      [     0     0     0     0     5    ]  0",
                "19.142: RevokeBias                       [      14          0              1    ]      [     0     0     0     0     0    ]  0",
                "36.464: BulkRevokeBias                   [      20          1              1    ]      [     0     0     0     0     0    ]  1",
                "37.557: ThreadDump                       [      22          0              1    ]      [     0     0     0     0     0    ]  0",
                "37.566: FindDeadlocks                    [      22          0              0    ]      [     0     0     0     0     0    ]  0"
        };

        String[] reasons = {
                "Deoptimize",
                "no vm operation",
                "EnableBiasedLocking",
                "GenCollectForAllocation",
                "RevokeBias",
                "BulkRevokeBias",
                "ThreadDump",
                "FindDeadlocks"
        };

        int index = 0;
        for (String line : lines) {
            Safepoint safepoint = PreUnified.TRACE.parse(line, SafepointParser.PreUnified.class).toSafepoint();
            assertNotNull(safepoint);
            assertEquals(safepoint.getVmOperation(), reasons[index++]);
        }
    }
    @Test
    public void testUnifiedSafepointPatternsRules() {
        String[] lines = {
                "[48.623s][info ][safepoint   ] Safepoint \"G1TryInitiateConcMark\", Time since last: 57800 ns, Reaching safepoint: 695245 ns, At safepoint: 11720 ns, Total: 706965 ns",
        };
        String[] vmops = {
                "G1TryInitiateConcMark",
                "G1Concurrent",
                "G1CollectForAllocation",
                "G1TryInitiateConcMark",
                "ZMarkStart",
                "ZMarkEnd",
                "ZRelocateStart"
        };

        int index = 0;
        for (String line : lines) {
            Safepoint safepoint = Unified.TRACE.parse(line, SafepointParser.Unified.class).toSafepoint();
            assertNotNull(safepoint);
            assertEquals(safepoint.getVmOperation(), vmops[index++]);
        }
    }

}
