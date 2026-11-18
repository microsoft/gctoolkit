// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;


import com.microsoft.gctoolkit.parser.GCParseRule;
import com.microsoft.gctoolkit.parser.GenericTokens;

public interface UnifiedSafepointPatterns extends GenericTokens {
    //[1.361s][info][safepoint    ] Safepoint "G1CollectForAllocation", Time since last: 295590960 ns, Reaching safepoint: 238882 ns, At safepoint: 23888872 ns, Total: 24127754 ns
    //[0.557s][info][safepoint    ] Safepoint "ICBufferFull", Time since last: 328272185 ns, Reaching safepoint: 4929 ns, Cleanup: 156005 ns, At safepoint: 852 ns, Leaving safepoint: 811 ns, Total: 162597 ns
    //[2.803s][info][safepoint    ] Safepoint "ZMarkStartYoungAndOld", Time since last: 1367178708 ns, Reaching safepoint: 91483 ns, At safepoint: 33824 ns, Leaving safepoint: 38612 ns, Total: 163919 ns, Threads: 3 runnable, 23 total
    GCParseRule SAFEPOINT = new GCParseRule("Unified Safepoint",
            "Safepoint " + SAFE_POINT_CAUSE + ", Time since last: (" + INTEGER + ") ns" + ", Reaching safepoint: (" + INTEGER + ") ns"
                    + ", (?:Cleanup: (" + INTEGER + ") ns, )?" + "At safepoint: (" + INTEGER + ") ns" + ", (?:Leaving safepoint: (" + INTEGER + ") ns, )?"
                    + "Total: (" + INTEGER + ") ns" + "(?:, Threads: (" + INTEGER + ") runnable, (" + INTEGER + ") total)?");
}
