// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static com.microsoft.gctoolkit.parser.CommonTestHelper.captureTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParallelParserRulesTest implements ParallelPatterns {

    private static final Logger LOGGER = Logger.getLogger(ParallelParserRulesTest.class.getName());

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

    private final boolean debugging = Boolean.getBoolean("microsoft.debug");

    //@Test
    public void testDebugParallelParseRules() {
        int index = 4;
        GCParseRule rule = rules[index];
        //evaluate( rule, lines[index][0], debugging);
        evaluate(rule, lines[index][1], true);
    }


    private void evaluate(GCParseRule rule, String string, boolean dump) {

        GCLogTrace trace = rule.parse(string);
        assertTrue(trace != null);
        if (dump) {
            trace.notYetImplemented();
//            LOGGER.fine("matches groups " + trace.groupCount());
//            for (int i = 0; i <= trace.groupCount(); i++) {
//                LOGGER.fine(i + ": " + trace.getGroup(i));
//            }
        }
    }


    private GCParseRule[] rules = {
            PSYOUNGGEN_NO_DETAILS,
            PSFULL,
            PS_FULL_BODY_FLOATING,
            PSFULL_ADAPTIVE_SIZE,
            PS_FULL_REFERENCE,
            SERIAL_FULL_REFERENCE,        //  5
            PSYOUNGGEN_REFERENCE,
            PSYOUNGGEN_PROMOTION_FAILED,
            PSYOUNG_ADAPTIVE_SIZE_POLICY,
            PSFULL_ERGONOMICS_PHASES,
            PSFULL_REFERENCE_PHASE        // 10

    };

    private String[][] lines = {
            {   //  0
                    "2017-03-28T12:17:34.744+0200: 1.895: [GC (Allocation Failure)  137969K->21806K(491008K), 0.0082985 secs]",
                    "2017-03-28T12:17:33.823+0200: 0.974: [GC (System.gc())  92578K->9947K(491008K), 0.0135426 secs]"  // buggy full gc
            },
            {   //  1
                    "2017-03-28T12:17:33.837+0200: 0.988: [Full GC (System.gc())  9947K->9457K(491008K), 0.0593030 secs]"
            },
            {   // 2
                    "[PSYoungGen: 6149K->0K(305856K)] [PSOldGen: 0K->5876K(699072K)] 6149K->5876K(1004928K) [PSPermGen: 22697K->22697K(262144K)], 0.0654700 secs] [Times: user=0.06 sys=0.00, real=0.06 secs]"
            },
            {   // 3
                    "2015-03-01T11:51:52.629-0500: 138040.983: [Full GC (System)AdaptiveSizeStart: 138072.609 collection: 312"
            },
            {   // 4
                    "12.700: [Full GC (Metadata GC Threshold) 12.720: [SoftReference, 0 refs, 0.0000402 secs]12.720: [WeakReference, 626 refs, 0.0000522 secs]12.720: [FinalReference, 11038 refs, 0.0033807 secs]12.724: [PhantomReference, 0 refs, 0 refs, 0.0000053 secs]12.724: [JNI Weak Reference, 0.0000039 secs][PSYoungGen: 2286K->0K(255488K)] [ParOldGen: 127438K->86892K(175104K)] 129725K->86892K(430592K), [Metaspace: 20902K->20902K(1069056K)], 0.0835405 secs] [Times: user=0.30 sys=0.01, real=0.08 secs]",
                    "2016-12-12T06:49:17.108-0500: 213439.976: [Full GC (Ergonomics)2016-12-12T06:49:17.247-0500: 213440.115: [SoftReference, 588 refs, 0.0001210 secs]2016-12-12T06:49:17.247-0500: 213440.115: [WeakReference, 2157 refs, 0.0001830 secs]2016-12-12T06:49:17.247-0500: 213440.115: [FinalReference, 11619 refs, 0.0010820 secs]2016-12-12T06:49:17.248-0500: 213440.116: [PhantomReference, 1 refs, 36 refs, 0.0000140 secs]2016-12-12T06:49:17.248-0500: 213440.116: [JNI Weak Reference, 0.0000100 secs] [PSYoungGen: 272896K->272894K(310784K)] [ParOldGen: 699382K->699382K(699392K)] 972278K->972276K(1010176K) [PSPermGen: 125994K->125994K(126464K)], 0.3060010 secs] [Times: user=0.00 sys=0.00, real=0.30 secs]"
            },
            {   //  5
                    "47047.534: [Full GC (System.gc()) 47047.534: [Tenured47047.808: [SoftReference, 258 refs, 0.0000630 secs]47047.808: [WeakReference, 628 refs, 0.0000561 secs]47047.808: [FinalReference, 170 refs, 0.0001564 secs]47047.808: [PhantomReference, 0 refs, 3 refs, 0.0000039 secs]47047.808: [JNI Weak Reference, 0.0000800 secs]: 292298K->278286K(699072K), 0.7450752 secs] 350258K->278286K(1013824K), [Metaspace: 99239K->99239K(1140736K)], 0.7451777 secs] [Times: user=0.74 sys=0.00, real=0.75 secs]",
                    "127.920: [Full GC127.920: [Tenured127.952: [SoftReference, 0 refs, 0.0000380 secs]127.952: [WeakReference, 444 refs, 0.0000370 secs]127.952: [FinalReference, 2070 refs, 0.0006330 secs]127.953: [PhantomReference, 0 refs, 2 refs, 0.0000040 secs]127.953: [JNI Weak Reference, 0.0000050 secs]: 100366K->91734K(699072K), 0.0936930 secs] 193212K->91734K(1013632K), [Perm : 26687K->26687K(26688K)], 0.0937770 secs] [Times: user=0.10 sys=0.00, real=0.09 secs]",
                    "756402.356: [Full GC (System.gc()) 756402.356: [Tenured756402.715: [SoftReference, 292 refs, 0.0000726 secs]756402.716: [WeakReference, 808 refs, 0.0000643 secs]756402.716: [FinalReference, 126 refs, 0.0001561 secs]756402.716: [PhantomReference, 0 refs, 2 refs, 0.0000042 secs]756402.716: [JNI Weak Reference, 0.0000796 secs]: 356460K->356882K(699072K), 0.7858180 secs] 391950K->356882K(1013952K), [Metaspace: 111616K->111616K(1153024K)], 0.7859359 secs] [Times: user=0.77 sys=0.00, real=0.79 secs]"
            },
            {   //  6
                    "0.385: [GC (Allocation Failure) 0.388: [SoftReference, 0 refs, 0.0000262 secs]0.388: [WeakReference, 10 refs, 0.0000051 secs]0.388: [FinalReference, 1054 refs, 0.0011154 secs]0.389: [PhantomReference, 0 refs, 0 refs, 0.0000496 secs]0.389: [JNI Weak Reference, 0.0000037 secs][PSYoungGen: 15872K->2532K(18432K)] 15872K->3180K(60928K), 0.0043799 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]"
            },
            {   //  7
                    "2017-08-17T08:14:22.677-0500: 8179.905: [GC (Allocation Failure) --[PSYoungGen: 3990443K->3990443K(4030464K)] 5012990K->5013022K(5079040K), 0.5837002 secs] [Times: user=0.67 sys=0.00, real=0.58 secs] \n"
            },
            {   //  8
                    "2017-08-25T08:49:16.474-0500: 20.296: [GC (Allocation Failure) 2017-08-25T08:49:16.511-0500: 20.333: [SoftReference, 0 refs, 0.0000564 secs]2017-08-25T08:49:16.511-0500: 20.333: [WeakReference, 2902 refs, 0.0002190 secs]2017-08-25T08:49:16.512-0500: 20.334: [FinalReference, 115330 refs, 0.1086704 secs]2017-08-25T08:49:16.620-0500: 20.442: [PhantomReference, 0 refs, 1 refs, 0.0000209 secs]2017-08-25T08:49:16.620-0500: 20.442: [JNI Weak Reference, 0.0000242 secs]AdaptiveSizePolicy::update_averages:  survived: 281180224  promoted: 16384  overflow: false",
                    "2017-08-25T08:49:16.474-0500: 20.296: [GC (Allocation Failure) AdaptiveSizePolicy::update_averages:  survived: 281180224  promoted: 16384  overflow: false",
                    "2015-02-27T21:31:15.258-0500: 3.612: [GCAdaptiveSizePolicy::compute_survivor_space_size_and_thresh:  survived: 6297408  promoted: 0  overflow: falseAdaptiveSizeStart: 3.625 collection: 1"
            },
            {   //  9
                    "2017-08-25T10:38:23.920-0500: 6567.742: [Full GC (Ergonomics) 2017-08-25T10:38:23.921-0500: 6567.743: [marking phase2017-08-25T10:38:23.921-0500: 6567.743: [par mark, 0.1255544 secs]"
            },
            {   // 10
                    "2017-08-25T10:38:24.047-0500: 6567.869: [reference processing2017-08-25T10:38:24.047-0500: 6567.869: [SoftReference, 29583 refs, 0.0025044 secs]2017-08-25T10:38:24.049-0500: 6567.871: [WeakReference, 7594 refs, 0.0006457 secs]2017-08-25T10:38:24.050-0500: 6567.872: [FinalReference, 1038 refs, 0.0009855 secs]2017-08-25T10:38:24.051-0500: 6567.873: [PhantomReference, 0 refs, 0 refs, 0.0000083 secs]2017-08-25T10:38:24.051-0500: 6567.873: [JNI Weak Reference, 0.0000212 secs], 0.0042334 secs]"
            }
    };
}
