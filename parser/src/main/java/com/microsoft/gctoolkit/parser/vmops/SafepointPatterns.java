// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;


import static com.microsoft.gctoolkit.parser.GenericTokens.COUNTER;
import static com.microsoft.gctoolkit.parser.PreUnifiedTokens.TIMESTAMP;


/**
 * General format is;
 * vmop                    [threads: total initially_running wait_to_block]    [time: spin block sync cleanup vmop] page_trap_count
 */
public interface SafepointPatterns {

    interface PreUnified {
        String VMOP = "(Deoptimize|no vm operation|EnableBiasedLocking|GenCollectForAllocation|RevokeBias|BulkRevokeBias|ThreadDump|FindDeadlocks|Exit)";
        String SAFE_POINT_PREFIX = TIMESTAMP + VMOP;
        String THREAD_COUNTS = "\\[\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+\\]";
        String TIMINGS = "\\[\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+\\]";

        SafepointParseRule TRACE = new SafepointParseRule(SAFE_POINT_PREFIX + "\\s+" + THREAD_COUNTS + "\\s+" + TIMINGS + "\\s+" + COUNTER);
    }

    interface Unified {
        // [108.400s][info ][safepoint   ] Safepoint "G1Concurrent", Time since last: 7404646 ns, Reaching safepoint: 2138853 ns, At safepoint: 384083 ns, Total: 2522936 ns
        String VMOP = "Safepoint\\s+\"(\\w+)\"";
        String SAFEPOINT_TIME = "(\\d+)\\s+ns";

        SafepointParseRule TRACE = new SafepointParseRule(VMOP + ",\\s+Time since last:\\s+" + SAFEPOINT_TIME + ",\\s+Reaching safepoint:\\s+" + SAFEPOINT_TIME + ",\\s+At safepoint:\\s+" + SAFEPOINT_TIME + ",\\s+Total:\\s+" + SAFEPOINT_TIME);
    }

}
