// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class G1GCParserRulesTest implements G1GCPatterns {

    private static final Logger LOGGER = Logger.getLogger(G1GCParserRulesTest.class.getName());

    @Test
    public void testG1GCParseRules() {
        for (int i = 0; i < rules.length; i++)
            for (int j = 0; j < lines.length; j++) {
                int captured = CommonTestHelper.captureTest(rules[i], lines[j]);
                if (i == j) {
                    assertTrue(captured == lines[j].length, i + " failed to captured it's lines");
                } else {
                    assertTrue(captured == 0, i + " captured " + j);
                }
            }

        assertTrue(true);
    }

    //@Test
    public void testSingeRule() {
        int index = 35;
        assertTrue(CommonTestHelper.captureTest(rules[index], lines[index]) == 1);
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

    String FIXUP_STATS = "Min: " + REAL_VALUE + ", Avg: " + REAL_VALUE + ", Max: " + REAL_VALUE + ", Diff: " + REAL_VALUE + ", Sum: " + REAL_VALUE;
    private GCParseRule[] rules = {
            G1_YOUNG_SPLIT_START,                     //  0
            G1_YOUNG_RS_SUMMARY,
            CONCURRENT_STRING_DEDUP,
            STRING_DEDUP_FIXUP,
            QUEUE_FIXUP,
            TABLE_FIXUP,                              //  5
            FREE_FLOATING_REFERENCE_RECORDS,
            FLOATING_REFERENCE_WITH_ADAPTIVE_SIZING,
            G1_FULL_ADAPTIVE_SIZING,
            FULLGC_WITH_CONCURRENT_PHASE,
            G1_180_REMARK_REF_DETAILS,                // 10
            G1_CORRUPTED_CONCURRENT_ROOT_REGION_SCAN_END,
            G1_FLOATING_CONCURRENT_PHASE_START,
            PARALLEL_TIME,
            G1_PARALLEL_PHASE_SUMMARY,
            TERMINATION_ATTEMPTS,                    // 15
            PROCESSED_BUFFERS,
            WORKER_PARALLEL_BLOCK,
            WORKER_ACTIVITY,
            G1_SOLARIS_PARALLEL_PHASE,
            PROCESSED_BUFFER,                        // 20
            SOLARIS_WORKER_PARALLEL_BLOCK,
            SOLARIS_WORKER_PARALLEL_ACTIVITY,
            G1_MEMORY_SUMMARY,
            G1_REMARK_REFERENCE_GC,
            FULL_WITH_CONCURRENT_PHASE_CORRUPTED,     // 25
            FULL_WITH_CONCURRENT_PHASE_INTERLEAVED,
            CORRUPTED_CONCURRENT_START_V4,
            CORRUPTED_CONCURRENT_START,
            CORRUPTED_CONCURRENT_START_V3,
            FULL_WITH_CONCURRENT_END,                 // 30
            CORRUPTED_CONCURRENT_START_V5,
            CORRUPTED_CONCURRENT_START_V6,
            FULL_MISSING_TIMESTAMP_CONCURRENT_START,
            CORRUPTED_CONCURRENT_START_V7,
            CORRUPTED_CONCURRENT_START_V8,            // 35
            CORRUPTED_CONCURRENT_START_V9,
            FULL_GC_FRAGMENT,
            CORRUPTED_CONCURRENT_START_V2,
            CONCURRENT_START_V3,
            CONCURRENT_START_V4,                     // 40
            CONCURRENT_START_V5,
            G1_CONCURRENT_ABORT
    };

    private String[][] lines = {

            {   //  0
                    "2015-09-10T11:05:53.786+0200: 10718.451: [GC pause (G1 Evacuation Pause) (young)"
            },
            {   //  1
                    "2015-10-17T20:03:51.747-0400: 1.211: [GC pause (G1 Evacuation Pause) (young)Before GC RS summary"
            },
            {   //  2
                    "2015-04-09T14:28:38.515+0100: 0.876: [GC concurrent-string-deduplication, 112.0B->112.0B(0.0B), avg 0.0%, 0.0000058 secs]"
            },
            {   //  3
                    "[String Dedup Fixup: 0.1 ms, GC Workers: 4]"
            },
            {   //  4
                    "[Queue Fixup (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]"
            },
            {   //  5
                    "[Table Fixup (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.1]"
            },
            {   //  6
                    "13925.724: [SoftReference, 0 refs, 0.0000060 secs]13925.724: [WeakReference, 367 refs, 0.0001090 secs]13925.724: [FinalReference, 3077 refs, 0.0116620 secs]13925.736: [PhantomReference, 8 refs, 0.0000040 secs]13925.736: [JNI Weak Reference, 0.0001390 secs] (to-space overflow), 4.63695400 secs]"
            },
            {   //  7
                    "2015-09-10T08:07:26.101+0200: 10.765: [SoftReference, 0 refs, 0.0004281 secs]2015-09-10T08:07:26.101+0200: 10.766: [WeakReference, 370 refs, 0.0002043 secs]2015-09-10T08:07:26.101+0200: 10.766: [FinalReference, 8896 refs, 0.0044365 secs]2015-09-10T08:07:26.106+0200: 10.770: [PhantomReference, 0 refs, 0 refs, 0.0004730 secs]2015-09-10T08:07:26.106+0200: 10.771: [JNI Weak Reference, 0.0000250 secs] 10.771: [G1Ergonomics (Mixed GCs) do not start mixed GCs, reason: candidate old regions not available]"
            },
            {   //  8
                    "Missed: 2015-04-28T13:03:20.395-0400: 3884258.323: [Full GC (Allocation Failure) 3884278.603: [G1Ergonomics (Heap Sizing) attempt heap shrinking, reason: capacity higher than max desired capacity after Full GC, capacity: 212600881152 bytes, occupancy: 6570037320 bytes, max desired capacity: 137438953472 bytes (70.00 %)]\n"
            },
            {   //  9
                    "100081.540: [Full GC100081.540: [GC concurrent-root-region-scan-start]",
                    "2018-01-29T17:34:24.984+0000: 5115.588: [Full GC (Metadata GC Threshold) 2018-01-29T17:34:24.984+0000: 5115.588: [GC concurrent-root-region-scan-start]"
            },
            {   // 10
                    "2015-12-21T10:26:34.913-0500: 35835.169: [GC remark 35835.169: [Finalize Marking, 0.8985905 secs] 35836.068: [GC ref-proc35836.068: [SoftReference, 74 refs, 0.0002156 secs]35836.068: [WeakReference, 243 refs, 0.0001747 secs]35836.068: [FinalReference, 4154 refs, 0.0012360 secs]35836.070: [PhantomReference, 0 refs, 0 refs, 0.0002882 secs]35836.070: [JNI Weak Reference, 0.0000227 secs], 0.0026411 secs] 35836.071: [Unloading, 0.0048615 secs], 0.9142368 secs]",
                    "2016-05-31T12:49:30.282-0400: 2328.858: [GC remark 2016-05-31T12:49:30.282-0400: 2328.858: [Finalize Marking, 0.0020922 secs] 2016-05-31T12:49:30.284-0400: 2328.860: [GC ref-proc2016-05-31T12:49:30.284-0400: 2328.860: [SoftReference, 0 refs, 0.0000754 secs]2016-05-31T12:49:30.284-0400: 2328.860: [WeakReference, 3894 refs, 0.0012230 secs]2016-05-31T12:49:30.285-0400: 2328.861: [FinalReference, 102 refs, 0.0002524 secs]2016-05-31T12:49:30.286-0400: 2328.861: [PhantomReference, 2 refs, 231 refs, 0.0004237 secs]2016-05-31T12:49:30.286-0400: 2328.862: [JNI Weak Reference, 0.0003428 secs], 0.0023558 secs] 2016-05-31T12:49:30.286-0400: 2328.862: [Unloading, 0.0457079 secs], 0.0556017 secs]"
            },
            {   // 11
                    "2017-01-24T22:50:27.724-0500: 21438.188: 2017-01-24T22:50:27.724-0500: [GC concurrent-root-region-scan-end, 0.0303627 secs]"
            },
            {   // 12
                    "[GC concurrent-mark-start]"
            },
            {   // 13
                    "[Parallel Time: 12.3 ms, GC Workers: 1]"
            },
            {   // 14
                    "[Ext Root Scanning (ms): Min: 0.6, Avg: 2.9, Max: 10.1, Diff: 9.5, Sum: 66.2]",
                    "[Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]",
                    "[Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.5]",
                    "[Code Root Scanning (ms): Min: 0.0, Avg: 0.5, Max: 3.4, Diff: 3.4, Sum: 12.5]",
                    "[Object Copy (ms): Min: 127.9, Avg: 134.4, Max: 136.9, Diff: 9.1, Sum: 3092.0]",
                    "[Termination (ms): Min: 0.0, Avg: 0.6, Max: 0.7, Diff: 0.7, Sum: 13.6]"
            },
            {   // 15
                    "[Termination Attempts: Min: 1, Avg: 4.1, Max: 8, Diff: 7, Sum: 73]"
            },
            {   // 16
                    "[Processed Buffers: Min: 0, Avg: 0.0, Max: 0, Diff: 0, Sum: 0]"
            },
            {   // 17
                    "[GC Worker Start (ms): Min: 3946.7, Avg: 3946.9, Max: 3947.2, Diff: 0.5]",
                    "[GC Worker End (ms): Min: 4085.4, Avg: 4085.4, Max: 4085.5, Diff: 0.1]"
            },
            {   // 18
                    "[GC Worker Other (ms): Min: 0.0, Avg: 0.1, Max: 0.1, Diff: 0.1, Sum: 1.4]",
                    "[GC Worker Total (ms): Min: 138.2, Avg: 138.5, Max: 138.7, Diff: 0.6, Sum: 3186.2]"
            },
            {   // 19
                    "[Ext Root Scanning (ms):  4.2]",
                    "[Update RS (ms):  0.1]",
                    "[Scan RS (ms):  0.2]",
                    "[Code Root Scanning (ms):  0.0]",
                    "[Object Copy (ms):  28.0]",
                    "[Termination (ms):  0.0]",
            },
            {   // 20
                    "[Processed Buffers:  1]",
            },
            {   // 21
                    "[GC Worker Start (ms):  6394.7]",
                    "[GC Worker End (ms):  3812.6]"
            },
            {   // 22
                    "[GC Worker Other (ms):  0.0]",
                    "[GC Worker Total (ms):  18.9]",
            },
            {   // 23
                    "[Eden: 512.0M(512.0M)->0.0B(505.0M) Survivors: 0.0B->7168.0K Heap: 512.0M(518.0M)->6418.4K(519.0M)]",
                    "[Eden: 89.0M(89.0M)->0.0B(89.0M) Survivors: 13.0M->13.0M Heap: 103.7M(2048.0M)->30.5M(2048.0M)]",
                    "[Eden: 128.0M(512.0M)->0.0B(512.0M) Survivors: 0.0B->0.0B Heap: 147.8M(518.0M)->23.1M(518.0M)], [Metaspace: 35052K->35051K(1079296K)]"
            },
            {   // 24
                    "2014-10-21T11:47:52.016-0500: 11976.613: [GC remark 11976.615: [GC ref-proc11976.615: [SoftReference, 834 refs, 0.0002100 secs]11976.615: [WeakReference, 14842 refs, 0.0021790 secs]11976.617: [FinalReference, 181 refs, 0.0004430 secs]11976.618: [PhantomReference, 475 refs, 0.0001160 secs]11976.618: [JNI Weak Reference, 0.0002910 secs], 0.0035310 secs], 0.0565900 secs]",
                    "46.465: [GC remark 46.465: [GC ref-proc46.465: [SoftReference, 0 refs, 0.0000180 secs]46.465: [WeakReference, 15 refs, 0.0000240 secs]46.465: [FinalReference, 0 refs, 0.0000110 secs]46.465: [PhantomReference, 0 refs, 0.0000110 secs]46.465: [JNI Weak Reference, 0.0000050 secs], 0.0000960 secs], 0.0012290 secs]"
            },
            {   // 25
                    "2018-02-08T19:13:30.878+0000: 2018-02-08T19:13:30.878+0000: 875061.483: 875061.483: [Full GC (Metadata GC Threshold) [GC concurrent-root-region-scan-start]"
            },
            {   // 26
                    "2018-02-08T20:03:08.475+0000: 878039.079: 2018-02-08T20:03:08.475+0000[Full GC (Metadata GC Threshold) : 878039.079: [GC concurrent-root-region-scan-start]"
            },
            {   // 27
                    "2018-02-08T20:13:02.246+0000: 878632.850: 2018-02-08T20:13:02.246+0000: [Full GC (Metadata GC Threshold) 878632.850: [GC concurrent-root-region-scan-start]"
            },
            {   // 28
                    "2018-02-08T20:04:14.978+0000: 2018-02-08T20:04:14.978+0000: 878105.583878105.583: [GC concurrent-root-region-scan-start]"
            },
            {   // 29
                    "2018-02-08T20:13:00.911+0000: 878631.5152018-02-08T20:13:00.911+0000: : [Full GC (Metadata GC Threshold) 878631.515: [GC concurrent-root-region-scan-start]"
            },
            {   // 30
                    ": [Full GC (Metadata GC Threshold) 2018-02-08T20:04:14.979+0000: 878105.583: [GC concurrent-root-region-scan-end, 0.0000770 secs]",
            },
            {   // 32
                    "2018-02-08T20:21:26.246+0000: 2018-02-08T20:21:26.246+0000879136.850: [Full GC (Metadata GC Threshold) : 879136.850: [GC concurrent-root-region-scan-start]"
            },
            {   // 33
                    "2018-02-08T20:27:32.297+0000: 879502.901: 2018-02-08T20:27:32.297+0000: 879502.901: [GC concurrent-root-region-scan-end, 0.0000375 secs]"
            },
            {   // 34
                    "[Full GC (Metadata GC Threshold) 2018-02-08T20:27:32.297+0000: 879502.902: [GC concurrent-mark-start]"
            },
            {
                    "2018-02-08T20:31:33.633+0000: 2018-02-08T20:31:33.633+0000: 879744.238879744.238: [Full GC (Metadata GC Threshold) : [GC concurrent-root-region-scan-start]"
            },
            {
                    "2018-02-08T20:31:54.234+0000: 2018-02-08T20:31:54.234+0000879764.839: 879764.839: [Full GC (Metadata GC Threshold) : [GC concurrent-root-region-scan-start]"
            },
            {
                    "2018-02-08T20:32:51.258+0000: 2018-02-08T20:32:51.258+0000: 879821.862: 879821.862[Full GC (Metadata GC Threshold) : [GC concurrent-root-region-scan-start]"
            },
            {
                    "2018-02-08T20:35:03.232+0000: 879953.837: [Full GC (Metadata GC Threshold)"
            },
            {
                    "2018-02-08T20:05:15.505+0000: 878166.1102018-02-08T20:05:15.505+0000: : 878166.110: [GC concurrent-root-region-scan-start]"
            },
            {
                    "2018-02-08T20:32:54.965+0000: 2018-02-08T20:32:54.965+0000: 879825.569: [GC concurrent-root-region-scan-start]"
            },
            {
                    "2018-02-08T20:33:01.419+0000: 2018-02-08T20:33:01.419+0000879832.023: [GC concurrent-root-region-scan-start]"
            },
            {
                    "2018-02-08T20:34:59.393+0000: 2018-02-08T20:34:59.393+0000: 879949.998: 879949.998[GC concurrent-root-region-scan-start]"
            },
            {
                    "27105.565: [GC concurrent-mark-abort]"
            }
    };
}
