// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.patterns;

import com.microsoft.gctoolkit.parser.JVMPatterns;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JVMPatternsTest implements JVMPatterns {

    @Test
    public void testEuropeanFormatedApplicationTime() {

        String string;

        string = "2014-06-18T11:21:33.592+0200: 215483,566: Application time: 0,0001550 seconds";
        assertNotNull(APPLICATION_TIME.parse(string));

        string = "2016-10-10T14:53:38.731+0100: 968823.430: Total time for which application threads were stopped: 0.0152181 seconds, Stopping threads took: 0.0001862 seconds";
        assertNotNull(APPLICATION_STOP_TIME_WITH_STOPPING_TIME.parse(string));

        string = "TLAB: gc thread: 0x00007f2f64119800 [id: 27349] desired_size: 1024KB slow allocs: 0  refill waste: 16384B alloc: 0.01526    51200KB refills: 1 waste 99.8% gc: 1046944B slow: 0B fast: 0B";
        assertNotNull(TLAB_CONT.parse(string));

        string = "TLAB totals: thrds: 25  refills: 3201 max: 3155 slow allocs: 821 max 799 waste:  0.7% gc: 19037968B max: 1048456B slow: 3914672B max: 3899656B fast: 0B max: 0B";
        assertNotNull(TLAB_TOTALS.parse(string));

        string = "2016-10-09T01:00:57.051+0800: 50.628: [GC TLAB: gc thread: 0x00007f2f0c00d800 [id: 27957] desired_size: 1024KB slow allocs: 0  refill waste: 16384B alloc: 0.01526    51200KB refills: 1 waste 97.7% gc: 1024928B slow: 0B fast: 0B";
        assertNotNull(TLAB_START.parse(string));

        string = "Total time for which application threads were stopped: 0.0006115 seconds, Stopping threads took: 0.0003832 seconds";
        assertNotNull(UNIFIED_LOGGING_APPLICATION_STOP_TIME_WITH_STOPPING_TIME.parse(string));

        string = "Safepoint \"G1CollectForAllocation\", Time since last: 295590960 ns, Reaching safepoint: 238882 ns, At safepoint: 23888872 ns, Total: 24127754 ns)";
        assertNotNull(UNIFIED_LOGGING_G1_SAFEPOINT.parse(string));

    }
}
