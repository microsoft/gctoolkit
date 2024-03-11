// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static com.microsoft.gctoolkit.parser.CommonTestHelper.captureTest;
import static org.junit.jupiter.api.Assertions.*;

public class GenerationalHeapParserRulesTest implements SimplePatterns, SerialPatterns, ParallelPatterns, CMSPatterns, ICMSPatterns {

    private static final Logger LOGGER = Logger.getLogger(GenerationalHeapParserRulesTest.class.getName());

    @Test
    public void testGenerationalRules() {
        assertEquals(rules.length,lines.length);
        for (int i = 0; i < rules.length; i++)
            for (int j = 0; j < lines.length; j++) {
                assertTrue(lines[j].length > 0, "No lines to test for index " + j);
                int captured = captureTest(rules[i], lines[j]);
                if (i == j) {
                    assertEquals(captured, lines[j].length, i + " failed to captured it's lines");
                } else {
                    assertEquals(0, captured, i + " captured " + j);
                }
            }
    }

    /* Code that is useful when testing individual records */

    // @Test
    public void testDebugGenerationalRules() {
        int index = 4;
        GCParseRule rule = rules[index];
        //evaluate( rule, lines[index][0], debugging);
        evaluate(rule, lines[index][1], true);
    }


    private void evaluate(GCParseRule rule, String string, boolean dump) {

        GCLogTrace trace = rule.parse(string);
        assertNotNull(trace);
        if (dump) {
            trace.notYetImplemented();
//            LOGGER.fine("matches groups " + trace.groupCount());
//            for (int i = 0; i <= trace.groupCount(); i++) {
//                LOGGER.fine(i + ": " + trace.getGroup(i));
//            }
        }
    }

    private GCParseRule[] specialCaseRules = {
            CONCURRENT_PHASE_START_BLOCK,
            CONCURRENT_PHASE_END_BLOCK
    };


    private GCParseRule[] rules = {
            PSYOUNGGEN_NO_DETAILS,
            PSFULL,
            PS_FULL_BODY_FLOATING,
            PSFULL_ADAPTIVE_SIZE,
            PS_FULL_REFERENCE,

            SERIAL_FULL_REFERENCE,
            PSYOUNGGEN_REFERENCE,
            PSYOUNGGEN_PROMOTION_FAILED,
            PSYOUNG_ADAPTIVE_SIZE_POLICY,
            PSFULL_ERGONOMICS_PHASES,

            PSFULL_REFERENCE_PHASE,
            DEFNEW,
            DEFNEW_TENURING,
            SERIAL_FULL,
//            SERIAL_FULL80,

//            PARNEW,
//            PARNEW_TENURING,
//            PARNEW_CONCURRENT_MODE_END,
//            PARNEW_CARDTABLE,
//            PARNEW_TO_CMF_PERM,
//
//            PARNEW_TO_CMF_META,
//            PARNEW_REFERENCE,
//            PARNEW_REFERENCE_SPLIT,
//            JVMPatterns.TLAB_START,
//            PARNEW_REFERENCE_SPLIT_BY_TLAB,
//
//            DEFNEW_REFERENCE,
//            PARNEW_PROMOTION_FAILED,
//            PARNEW_PROMOTION_FAILED_DETAILS,
//            PARNEW_PROMOTION_FAILED_REFERENCE,
//            FLOATING_REFERENCE,
//
//            PARNEW_PROMOTION_FAILED_TENURING,
//            PARNEW_PROMOTION_FAILED_IN_CMS_PHASE,
//            CMS_BAILING_TO_FOREGROUND,
//            PROMOTION_FAILED_TO_FULL,
//            PARNEW_PLAB,
//
//            PLAB_ENTRY,
//            PLAB_SUMMARY,
//            FULLGC_FLS_BEFORE,
//            PARNEW_FLS_BEFORE,
//            PARNEW_FLS_AFTER,
//
//            PARNEW_FLS_BODY,
//            PARNEW_PROMOTION_FAILED_DETAILS_AFTER,
//            PARNEW_FLS_TIME,
//            FLS_HEADER,
//            FLS_SEPARATOR,
//
//            FLS_TOTAL_FREE_SPACE,
//            FLS_MAX_CHUNK_SIZE,
//            FLS_NUMBER_OF_BLOCKS,
//            FLS_AVERAGE_BLOCK_SIZE,
//            FLS_TREE_HEIGHT,
//
//            FLS_LARGE_BLOCK_PROXIMITY,
//            FLS_LARGE_BLOCK,
//            PARNEW_PROMOTION_FAILED_TIME_ABORT_PRECLEAN,
//            PARNEW_PROMOTION_FAILED_CONCURRENT_PHASE,
//            CORRUPTED_PARNEW_CONCURRENT_PHASE,
//
//            CORRUPTED_PARNEW_BODY,
//            CONCURRENT_PHASE_START,
//            CONCURRENT_PHASE_END,
//            CONCURRENT_PHASE_END_WITH_CPU_SUMMARY,
//            INITIAL_MARK,
//
//            SCAVENGE_BEFORE_REMARK,
//            SCAVENGE_BEFORE_REMARK_TENURING,
//            PARALLEL_REMARK_WEAK_REF,
//            PARALLEL_REMARK_CLASS_UNLOADING,
//            REMARK_PARNEW_PROMOTION_FAILED,
//
//            PARALLEL_REMARK_STRING_SYMBOL,
//            PARALLEL_REMARK_WEAK_CLASS_SYMBOL_STRING,
//            PARALLEL_REMARK_WEAK_STRING,
//            PARALLEL_RESCAN,
//            REMARK,
//
//            PARALLEL_RESCAN_V2,
//            PARALLEL_RESCAN_WEAK_CLASS_SCRUB,
//            //, 0.1127040 secs]220.624: [weak refs processing, 0.1513820 secs] [1 CMS-remark: 10541305K(16777216K)] 10742883K(18664704K), 0.7371020 secs]
//            //todo: this was capturing records that is shouldn't have so the rule was modified.. now does it work??? Needs through testing now that order of evaluation will change
//            SERIAL_REMARK_SCAN_BREAKDOWNS,
//            REMARK_DETAILS,
//            REMARK_REFERENCE_PROCESSING,
//
//            TENURING_DETAILS,
//            RESCAN_WEAK_CLASS_SYMBOL_STRING,
//            CONCURRENT_MODE_FAILURE_DETAILS,
//            CONCURRENT_MODE_FAILURE_DETAILS_META,
//            PARNEW_DETAILS_CONCURRENT_MODE_FAILURE_PERM,
//
//            PARNEW_DETAILS_CONCURRENT_MODE_FAILURE_META,
//            PARNEW_DETAILS_PROMOTION_FAILED_WITH_CMS_PHASE,
//            PARNEW_DETAILS_WITH_CONCURRENT_MODE_FAILURE,
//            CONCURRENT_MODE_FAILURE_REFERENCE,
//            iCMS_CONCURRENT_MODE_FAILURE,
//
//            iCMS_CONCURRENT_MODE_FAILURE_META,
//            iCMS_CMF_DUIRNG_PARNEW_DEFNEW_DETAILS,
//            FULL_GC_INTERRUPTS_CONCURRENT_PHASE,
//            FULL_PARNEW_START,
//            CMS_FULL_80,
//
//            FULL_GC_REFERENCE_CMF,
//            iCMS_PARNEW,
//            iCMS_PARNEW_PROMOTION_FAILURE_RECORD,
//            iCMS_PARNEW_PROMOTION_FAILURE,
//            FULL_GC_ICMS,
//
//            iCMS_PARNEW_DEFNEW_TENURING_DETAILS,
//            iCMS_FULL,
//            iCMS_PROMOTION_FAILED,
//            iCMS_PROMOTION_FAILED_PERM,
//            iCMS_PROMOTION_FAILED_META,
//
//            iCMS_MISLABELED_FULL,
//            iCMS_FULL_AFTER_CONCURRENT_MODE_FAILURE,
//            iCMS_FULL_AFTER_CONCURRENT_MODE_FAILURE_META,
//            iCMS_CONCURRENT_MODE_INTERRUPTED,
//            PS_FULL_GC_META,
//
//            PS_FULL_GC_V2_META,
//            CMS_FULL_META,
//            FULL_PARNEW_CMF_META,
//            FULL_PARNEW_CMF_PERM,
//            PARNEW_CONCURRENT_MODE_FAILURE_PERM,
//
//            PARNEW_CONCURRENT_MODE_FAILURE_META,
//            PS_FULL_GC_V2_PERM,
//            PS_FULL_GC_PERM,
//            CMS_FULL_PERM,
//            CMS_FULL_PERM_META_REFERENCE,
//
//            PARNEW_NO_DETAILS,
//            YOUNG_NO_DETAILS,
//            CMS_NO_DETAILS,
//            FULL_NO_GC_DETAILS,
//            PARNEW_START,
//
//            GC_START,
//            YOUNG_SPLIT_NO_DETAILS,
//            CMF_SIMPLE,
//            DEFNEW_DETAILS,
//            PRECLEAN_REFERENCE_PAR_NEW_REFERENCE,
//
//            PSYOUNGGEN,
//            PSYOUNGGEN_REFERENCE_SPLIT,
//            PS_TENURING_START,
//            PSFULL_SPLIT,
//            PS_FULL_REFERENCE_SPLIT,
//
//            PS_DETAILS_WITH_TENURING,
//            PS_FAILURE,
//            PSOLD_ADAPTIVE_SIZE_POLICY,
//            PSYOUNG_DETAILS_FLOATING,
//            FULL_REFERENCE_ADAPTIVE_SIZE,
//
//            PS_PROMOTION_FAILED,
//            RESCAN_SPLIT_UNLOADING_STRING,
//            PARNEW_CONCURRENT_PHASE_CARDS,
//            CONC_PHASE_YIELDS,
//            PRECLEAN_TIMED_OUT_WITH_CARDS,
//
//            PARNEW_SHOULD_CONCURRENT_COLLECT,
//            SHOULD_CONCURRENT_COLLECT,
//            REMARK_SPLIT_BY_DEBUG,
//            SCAVENGE_BEFORE_REMARK_PRINT_HEAP_AT_GC,
//            SPLIT_REMARK_REFERENCE_BUG,
//
//            SPLIT_REMARK_REFERENCE,
//            PSYOUNG_ADAPTIVE_SIZE_POLICY_START,
//            PS_ADAPTIVE_SIZE_POLICY_BODY,
//            ADAPTIVE_SIZE_POLICY_BODY,
//            ADAPTIVE_SIZE_POLICY_STOP,
//
//            SCAVENGE_BEFORE_REMARK_REFERENCE,
//            SCAVENGE_BEFORE_REMARK_REFERENCE_SPLIT,
//            PARNEW_TO_CONCURRENT_MODE_FAILURE,
//            SPLIT_PARNEW_PROMOTION_FAILED_IN_CMS_PHASE,
//            FULL_SPLIT_BY_CONCURRENT_PHASE,
//
//            CMF_LARGE_BLOCK,
//            //this rule must be evaluated before CONCURRENT_PHASE_END_BLOCK
//            ABORT_PRECLEAN_DUE_TO_TIME_CLAUSE,
            PRECLEAN_REFERENCE
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
            },
            {   // 11 ver 7.0, 8.0
                "24.677: [GC24.677: [DefNew: 73140K->3189K(78656K), 0.0021028 secs] 76755K->6803K(253440K), 0.0022014 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]",
                "24.677: [GC (Allocation Failure) 24.677: [DefNew: 73140K->3189K(78656K), 0.0021028 secs] 76755K->6803K(253440K), 0.0022014 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]"
            },
            {   // 12 ver 7.0, 8.0
                "24.665: [GC24.665: [DefNew",
                "24.665: [GC (Allocation Failure) 24.665: [DefNew"
            },
            {   // 13 ver 7.0
                "15.287: [Full GC15.287: [Tenured: 0K->4602K(174784K), 0.0371090 secs] 25253K->4602K(253440K), [Perm : 13657K->13657K(21248K)], 0.0373080 secs] [Times: user=0.03 sys=0.00, real=0.04 secs]",
                "13.906: [Full GC (System.gc()) 13.906: [Tenured: 0K->4267K(174784K), 0.0276425 secs] 29492K->4267K(253440K), [Metaspace: 15675K->15675K(1062912K)], 0.0280831 secs] [Times: user=0.02 sys=0.01, real=0.03 secs]"
            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
//            {
//            },
            {
                "2016-04-01T15:03:42.171-0700: 11025.637: [Preclean SoftReferences, 0.0000530 secs]2016-04-01T15:03:42.172-0700: 11025.637: [Preclean WeakReferences, 0.0006860 secs]2016-04-01T15:03:42.172-0700: 11025.638: [Preclean FinalReferences, 0.0005450 secs]2016-04-01T15:03:42.173-0700: 11025.639: [Preclean PhantomReferences, 0.0000230 secs]2016-04-01T15:03:42.197-0700: 11025.663: [CMS-concurrent-preclean: 0.025/0.026 secs] [Times: user=0.04 sys=0.01, real=0.03 secs]"
            }
    };
}
