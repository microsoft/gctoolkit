// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

public interface JVMPatterns extends PreUnifiedTokens {


    //Total time for which application threads were stopped: 0.0000350 seconds
    GCParseRule SIMPLE_APPLICATION_STOP_TIME = new GCParseRule("SIMPLE_APPLICATION_STOP_TIME", "^Total time for which application threads were stopped: " + TIME + " seconds$");
    GCParseRule APPLICATION_STOP_TIME = new GCParseRule("APPLICATION_STOP_TIME", DATE_TIMESTAMP + "Total time for which application threads were stopped: " + TIME + " seconds$");
    //4227.515: Total time for which application threads were stopped: 0.0737654 seconds, Stopping threads took: 0.0000558 seconds
    GCParseRule APPLICATION_STOP_TIME_WITH_STOPPING_TIME = new GCParseRule("APPLICATION_STOP_TIME_WITH_STOPPING_TIME", DATE_TIMESTAMP + "Total time for which application threads were stopped: " + TIME + " seconds, Stopping threads took: " + TIME + " seconds");
    //Total time for which application threads were stopped: 0.0006115 seconds, Stopping threads took: 0.0003832 seconds
    GCParseRule UNIFIED_LOGGING_APPLICATION_STOP_TIME_WITH_STOPPING_TIME = new GCParseRule("Unified Logging App stop time", "Total time for which application threads were stopped: " + TIME + " seconds, Stopping threads took: " + TIME + " seconds");
    //[1.361s][info ][safepoint   ] Safepoint "G1CollectForAllocation", Time since last: 295590960 ns, Reaching safepoint: 238882 ns, At safepoint: 23888872 ns, Total: 24127754 ns
    GCParseRule UNIFIED_LOGGING_G1_SAFEPOINT = new GCParseRule("", "Safepoint " + SAFE_POINT_CAUSE + ", Time since last: (" + INTEGER + ") ns, Reaching safepoint: (" + INTEGER + ") ns, At safepoint: (" + INTEGER + ") ns, Total: (" + INTEGER + ") ns");

    GCParseRule SIMPLE_APPLICATION_TIME = new GCParseRule("SIMPLE_APPLICATION_TIME", "Application time: " + TIME + " seconds");
    GCParseRule APPLICATION_TIME = new GCParseRule("APPLICATION_TIME", DATE_TIMESTAMP + "Application time: " + TIME + " seconds");
    GCParseRule UNIFIED_LOGGING_APPLICATION_TIME = new GCParseRule("Unified Logging Application Time", " Application time: " + TIME + " seconds");
    GCParseRule GC_PAUSE_CLAUSE = new GCParseRule("GC_PAUSE_CLAUSE", ", " + PAUSE_TIME + "\\]");

    //2016-10-09T01:09:48.895+0800: 82.472: [GC TLAB: gc thread: 0x00007f2f0c011800 [id: 36811] desired_size: 1024KB slow allocs: 0  refill waste: 16384B alloc: 0.00778    26112KB refills: 7 waste  1.1% gc: 77016B slow: 952B fast: 0B
    //2016-10-09T01:00:57.051+0800: 50.628: [GC TLAB: gc thread: 0x00007f2f0c00d800 [id: 27957] desired_size: 1024KB slow allocs: 0  refill waste: 16384B alloc: 0.01526    51200KB refills: 1 waste 97.7% gc: 1024928B slow: 0B fast: 0B
    GCParseRule TLAB_START = new GCParseRule("TLAB_START",
            DATE_TIMESTAMP + "\\[GC TLAB: gc thread: (" + HEX + ") \\[id: (" + INTEGER + ")\\] desired_size: (" + INTEGER + ")KB slow allocs: (" +
                    INTEGER + ")  refill waste: (" + INTEGER + ")B alloc: (" + REAL_NUMBER + ")\\s+(" + INTEGER + ")KB refills: (" +
                    INTEGER + ") waste\\s+" + PERCENTAGE + " gc: (" + INTEGER + ")B slow: (" + INTEGER + ")B fast: (" + INTEGER + ")B"
    );
    //TLAB: gc thread: 0x00007f2e64002000 [id: 27852] desired_size: 1024KB slow allocs: 0  refill waste: 16384B alloc: 0.01526    51200KB refills: 1 waste 100.0% gc: 1048064B slow: 0B fast: 0B
    //TLAB: gc thread: 0x00007f2d60008800 [id: 32657] desired_size: 1024KB slow allocs: 0  refill waste: 16384B alloc: 0.12516   419970KB refills: 351 waste  0.5% gc: 432696B slow: 1424200B fast: 0B
    GCParseRule TLAB_CONT = new GCParseRule("TLAB_CONT",
            "^TLAB: gc thread: (" + HEX + ") \\[id: (" + INTEGER + ")\\] desired_size: (" + INTEGER + ")KB slow allocs: (" +
                    INTEGER + ")  refill waste: (" + INTEGER + ")B alloc: (" + REAL_NUMBER + ")\\s+(" + INTEGER + ")KB refills: (" +
                    INTEGER + ") waste " + PERCENTAGE + " gc: (" + INTEGER + ")B slow: (" + INTEGER + ")B fast: (" + INTEGER + ")B"
    );

    //TLAB totals: thrds: 25  refills: 3201 max: 3155 slow allocs: 821 max 799 waste:  0.7% gc: 19037968B max: 1048456B slow: 3914672B max: 3899656B fast: 0B max: 0B
    GCParseRule TLAB_TOTALS = new GCParseRule("TLAB_TOTALS",
            "TLAB totals: thrds: (" + INTEGER + ")  refills: (" + INTEGER + ") max: (" + INTEGER + ") slow allocs: (" + INTEGER + ") max (" + INTEGER
                    + ") waste:  (" + PERCENTAGE + ") gc: (" + INTEGER + ")B max: (" + INTEGER + ")B slow: (" + INTEGER + ")B max: (" + INTEGER + ")B fast: ("
                    + INTEGER + ")B max: (" + INTEGER + ")B"
    );

    /*
        [0.648s][info][safepoint    ] Entering safepoint region: RevokeBias
        [0.648s][info][safepoint    ] Leaving safepoint region
     */
    GCParseRule SAFEPOINT_REGION = new GCParseRule("Safepoint region", "Entering safepoint region: (\\S+)$");
    GCParseRule LEAVING_SAFEPOINT = new GCParseRule("Leave Safepoint", "Leaving safepoint region");
}
