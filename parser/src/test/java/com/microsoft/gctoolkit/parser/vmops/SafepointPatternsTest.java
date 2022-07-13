// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;


import com.microsoft.gctoolkit.event.jvm.Safepoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SafepointPatternsTest implements SafepointPatterns {


    @Test
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
            Safepoint safepoint = PreUnified.TRACE.parse(line).toSafepoint();
            assertNotNull(safepoint);
            assertEquals(safepoint.getVmOperation(), reasons[index++]);
        }
    }
    @Test
    public void testUnifiedSafepointPatternsRules() {
        String[] lines = {
                "[48.623s][info ][safepoint   ] Safepoint \"G1TryInitiateConcMark\", Time since last: 57800 ns, Reaching safepoint: 695245 ns, At safepoint: 11720 ns, Total: 706965 ns",
                "[48.639s][info ][safepoint   ] Safepoint \"G1Concurrent\", Time since last: 12509959 ns, Reaching safepoint: 2135573 ns, At safepoint: 1361449 ns, Total: 3497022 ns",
                "[48.737s][info ][safepoint   ] Safepoint \"G1CollectForAllocation\", Time since last: 79888304 ns, Reaching safepoint: 2212574 ns, At safepoint: 5864037 ns, Total: 8076611 ns",
                "[48.964s][info ][safepoint   ] Safepoint \"G1TryInitiateConcMark\", Time since last: 112113388 ns, Reaching safepoint: 971886 ns, At safepoint: 4262987 ns, Total: 5234873 ns",
                "[16.469s][info ][safepoint      ] Safepoint \"ZMarkStart\", Time since last: 19984410 ns, Reaching safepoint: 119054 ns, At safepoint: 11169149 ns, Total: 11288203 ns",
                "[16.500s][info ][safepoint      ] Safepoint \"ZMarkEnd\", Time since last: 24471890 ns, Reaching safepoint: 143906 ns, At safepoint: 6381673 ns, Total: 6525579 ns",
                "[16.522s][info ][safepoint      ] Safepoint \"ZRelocateStart\", Time since last: 7651167 ns, Reaching safepoint: 150800 ns, At safepoint: 14046952 ns, Total: 14197752 ns"
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
            Safepoint safepoint = Unified.TRACE.parse(line).toSafepoint();
            assertNotNull(safepoint);
            assertEquals(safepoint.getVmOperation(), vmops[index++]);
        }
    }

}
