// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;


import com.microsoft.gctoolkit.event.jvm.Safepoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SafepointPatternsTest implements SafepointPatterns {


    @Test
    public void testSafepointPatternsRules() {
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
            Safepoint safepoint = TRACE.parse(line).toSafepoint();
            assertNotNull(safepoint);
            assertEquals(safepoint.getVmOperation(), reasons[index++]);
        }
    }
}
