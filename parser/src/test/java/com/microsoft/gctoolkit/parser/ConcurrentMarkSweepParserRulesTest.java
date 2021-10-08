// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrentMarkSweepParserRulesTest implements CMSPatterns {

    /**
     * Run all rules over all log entries looking for both misses and greedy rules.
     */
    @Test
    public void testCMSParseRules() {
        assertTrue( rules.length == lines.length, "Number of rules differs from the numbers of log entries");
        for (int i = 0; i < rules.length; i++) {
            for (int j = 0; j < lines.length; j++) {
                int captured = CommonTestHelper.captureTest(rules[i], lines[j]);
                if (i == j) {
                    assertTrue(captured == lines[j].length, i + " failed to captured it's lines");
                } else {
                    assertTrue(captured == 0, "rule " + i + " is greedy in that is captured dataset " + j);
                }
            }
        }

        assertTrue(true);
    }

    /* Code that is useful when testing individual records */

    private final boolean debugging = Boolean.getBoolean("microsoft.debug");

    //@Test
    //@Ignore("Not a real test, only for debugging")
    public void testDebugCMSParseRule() {
        int index = rules.length-1; // awesome fix from David.. thanks :-)
        //index = 36;
        GCParseRule rule = rules[index];
        evaluate(rule, lines[index][0], debugging);
    }

    private void evaluate(GCParseRule rule, String string, boolean dump) {
        //The IDE eats messages printed to the log file.. thus this information *is* printed to stout
        GCLogTrace trace = rule.parse(string);
        assertTrue(trace != null);
        if (dump) {
            System.out.println("matches groups " + trace.groupCount());
            for (int i = 0; i <= trace.groupCount(); i++) {
                System.out.println(i + ": " + trace.getGroup(i));
            }
        }
    }

    private GCParseRule[] rules = {
            FULL_GC_INTERRUPTS_CONCURRENT_PHASE,
            FULL_GC_REFERENCE_CMF,
            PARNEW_PROMOTION_FAILED,
            PARNEW_TO_CONCURRENT_MODE_FAILURE,
            PARNEW_CONCURRENT_MODE_FAILURE_PERM,
            PARNEW_CONCURRENT_MODE_FAILURE_META,    //5
            PARNEW_FLS_BEFORE,
            PARNEW_FLS_AFTER,
            PARNEW_FLS_BODY,
            PARNEW_PROMOTION_FAILED_DETAILS,
            PARNEW_PROMOTION_FAILED_DETAILS_AFTER, //10
            PARNEW_FLS_TIME,
            FLS_HEADER,
            FLS_SEPARATOR,
            FLS_TOTAL_FREE_SPACE,
            FLS_MAX_CHUNK_SIZE,  //15
            FLS_NUMBER_OF_BLOCKS,
            FLS_AVERAGE_BLOCK_SIZE,
            FLS_TREE_HEIGHT,
            FLS_LARGE_BLOCK_PROXIMITY,
            FLS_LARGE_BLOCK, //20
            CORRUPTED_PARNEW_BODY,
            PARNEW_PROMOTION_FAILED_REFERENCE,
            SCAVENGE_BEFORE_REMARK_REFERENCE_SPLIT,
            SPLIT_REMARK_REFERENCE_BUG,
            PARNEW_PROMOTION_FAILED_CONCURRENT_PHASE, //25
            CONCURRENT_MODE_FAILURE_REFERENCE,
            PARALLEL_REMARK_WEAK_REF,
            REMARK_REFERENCE_PROCESSING,
            DEFNEW_REFERENCE,
            CMS_FULL_PERM_META_REFERENCE,             //30
            SCAVENGE_BEFORE_REMARK_PRINT_HEAP_AT_GC,
            SPLIT_REMARK_REFERENCE,
            PARNEW_REFERENCE_SPLIT,
            CONCURRENT_MODE_FAILURE_DETAILS,
            PARNEW_REFERENCE,                          // 35
            SCAVENGE_BEFORE_REMARK_REFERENCE,
            SPLIT_PARNEW_PROMOTION_FAILED_IN_CMS_PHASE,
            FULL_SPLIT_BY_CONCURRENT_PHASE,
            CMF_LARGE_BLOCK,
            PRECLEAN_REFERENCE_PAR_NEW_REFERENCE
    };

    private String[][] lines = {
            {       // 0
                    "2015-08-07T00:28:24.210+0200: 32810.963: [Full GC (System) 2015-08-07T00:28:24.210+0200: 32810.963: [CMS2015-08-07T00:28:24.851+0200: 32811.604: [CMS-concurrent-mark: 0.638/9713.769 secs] [Times: user=342.99 sys=126.60, real=9713.77 secs]"
            },
            {       // 1
                    "2016-04-01T14:25:57.870-0700: 8761.336: [Full GC (GCLocker Initiated GC)2016-04-01T14:25:57.871-0700: 8761.336: [CMS (concurrent mode failure)2016-04-01T14:25:59.160-0700: 8762.626: [SoftReference, 541 refs, 0.0001800 secs]2016-04-01T14:25:59.160-0700: 8762.626: [WeakReference, 21658 refs, 0.0024960 secs]2016-04-01T14:25:59.163-0700: 8762.628: [FinalReference, 4142 refs, 0.0033170 secs]2016-04-01T14:25:59.166-0700: 8762.632: [PhantomReference, 194 refs, 0.0000550 secs]2016-04-01T14:25:59.166-0700: 8762.632: [JNI Weak Reference, 0.0000220 secs]: 4186495K->665385K(4218880K), 3.8356980 secs] 4448832K->665385K(5946048K), [CMS Perm : 417612K->417612K(1048576K)], 3.8379940 secs] [Times: user=3.84 sys=0.02, real=3.83 secs]"
            },
            {       // 2
                    "2015-07-01T13:54:21.259-0700: 46222.388: [GC (Allocation Failure) 46222.388: [ParNew (promotion failed): 2146944K->2146944K(2146944K), 2.8555290 secs] 20521617K->20721929K(20732992K), 2.8558620 secs] [Times: user=5.80 sys=0.48, real=2.85 secs]"
            },
            {       // 3
                    ": 2292867K->2293758K(2293760K), 52.5725410 secs] 3013763K->2306588K(3014656K), [CMS Perm : 257812K->257481K(393216K)], 52.5751269 secs] [Times: user=52.82 sys=0.26, real=52.58 secs]"
            },
            {       // 4
                    "2015-02-04T17:36:07.103-0500: 199626.435: [GC 199626.435: [ParNew: 812672K->812672K(914240K), 0.0000400 secs]199626.435: [CMS (concurrent mode failure): 1071394K->1081343K(1081344K), 6.8504740 secs] 1884066K->1092775K(1995584K), [CMS Perm : 99417K->99411K(524288K)], 6.8510440 secs] [Times: user=6.63 sys=0.02, real=6.85 secs]"
            },
            {       // 5
                    "2015-09-15T08:19:04.488-0500: 4741.519: [GC (Allocation Failure) 4741.519: [ParNew (promotion failed): 9437183K->9349732K(9437184K), 31.8428677 secs]4773.362: [CMS: 8231197K->2849841K(10485760K), 15.2211389 secs] 17074542K->2849841K(19922944K), [Metaspace: 69283K->69283K(1112064K)], 47.0642095 secs] [Times: user=56.18 sys=16.32, real=47.06 secs]"
            },
            {       // 6
                    "2015-06-05T00:30:43.510-0500: 6.022: [GC (Allocation Failure) Before GC:"
            },
            {       // 7
                    "6.023: [ParNew: 5242880K->58946K(7864320K), 0.0785903 secs] 5242880K->58946K(18350080K)After GC:"
            },
            {       // 8
                    ": 2359296K->23477K(2490368K), 0.0691730 secs] 2359296K->23477K(3538944K)After GC:"
            },
            {       // 9
                    "22134.127: [ParNew (15: promotion failure size = 39716437)  (promotion failed): 7172365K->6836505K(7864320K), 1.7204958 secs]22135.847: [CMSCMS: Large block 0x000000062859cfa0",
                    "2017-04-16T12:35:12.797-0700: 142237.056: [ParNew (0: promotion failure size = 3)  (1: promotion failure size = 3)  (2: promotion failure size = 3)  (3: promotion failure size = 3)  (4: promotion failure size = 3)  (5: promotion failure size = 3)  (6: promotion failure size = 3)  (7: promotion failure size = 131074)  (8: promotion failure size = 3)  (9: promotion failure size = 3)  (10: promotion failure size = 3)  (11: promotion failure size = 3)  (12: promotion failure size = 3)  (13: promotion failure size = 3)  (14: promotion failure size = 3)  (15: promotion failure size = 3)  (16: promotion failure size = 3)  (17: promotion failure size = 3)  (18: promotion failure size = 3)  (19: promotion failure size = 3)  (20: promotion failure size = 3)  (21: promotion failure size = 3)  (22: promotion failure size = 3)  (23: promotion failure size = 3)  (24: promotion failure size = 3)  (25: promotion failure size = 4)  (26: promotion failure size = 3)  (27: promotion failure size = 3)  (promotion failed): 1611303K->1607853K(1800000K), 0.4413433 secs]2017-04-16T12:35:13.238-0700: 142237.498: [CMSCMS: Large block 0x00000004bc5d2380",
                    "2014-10-24T06:04:47.413-0400: 748196.080: [GC2014-10-24T06:04:47.413-0400: 748196.080: [ParNew (0: promotion failure size = 8)  (1: promotion failure size = 8)  (promotion failed)"
            },
            {       // 10
                    ": 4839952K->3806707K(10485760K), 5.7168591 secs] 11644986K->3806707K(18350080K), [Metaspace: 116615K->116615K(1157120K)]After GC:"
            },
            {       // 11
                    ", 0.0787331 secs] [Times: user=1.23 sys=0.23, real=0.08 secs]"
            },
            {       // 12
                    "Statistics for BinaryTreeDictionary:"
            },
            {       // 13
                    "------------------------------------"
            },
            {       // 14
                    "Total Free Space: 349520988"
            },
            {       // 15
                    "Max   Chunk Size: 40787514"
            },
            {       // 16
                    "Number of Blocks: 10040"
            },
            {       // 17
                    "Av.  Block  Size: 34812"
            },
            {       // 18
                    "Tree      Height: 48"
            },
            {       // 19
                    "CMS: Large Block: 0x0000000540020000; Proximity: 0x0000000000000000 -> 0x000000054001fac8"
            },
            {       // 20
                    "CMS: Large block 0x000000077386a778"
            },
            {       // 21
                    "(promotion failed): 118016K->118016K(118016K), 0.0288030 secs]17740.440: [CMS (concurrent mode failure): 914159K->311550K(917504K), 0.5495730 secs] 985384K->311550K(1035520K), [CMS Perm : 65977K->65950K(131072K)], 0.5785090 secs] [Times: user=0.67 sys=0.01, real=0.58 secs]"
            },
            {       // 22
                    "2016-03-30T13:17:36.164-0400: 1786.927: [GC (Allocation Failure) 1786.927: [ParNew1787.831: [SoftReference, 0 refs, 0.0003186 secs]1787.831: [WeakReference, 920 refs, 0.0005255 secs]1787.832: [FinalReference, 4 refs, 0.0000851 secs]1787.832: [PhantomReference, 0 refs, 0.0000719 secs]1787.832: [JNI Weak Reference, 0.0000860 secs] (promotion failed): 1747648K->1747648K(1747648K), 1.4600799 secs]1788.387: [CMS2016-03-30T13:17:48.760-0400: 1799.522: [CMS-concurrent-sweep: 12.494/13.961 secs] [Times: user=27.43 sys=0.25, real=13.96 secs]"
            },
            {       // 23
                    "2016-03-22T13:57:02.971-0400: 19.703: [GC (CMS Final Remark) [YG occupancy: 613960 K (996800 K)]2016-03-22T13:57:02.971-0400: 19.703: [GC (CMS Final Remark) 19.703: [ParNew19.875: [SoftReference, 0 refs, 0.0001882 secs]19.875: [WeakReference, 228 refs, 0.0001731 secs]19.875: [FinalReference, 17964 refs, 0.0210014 secs]19.896: [PhantomReference, 0 refs, 0.0000989 secs]19.896: [JNI Weak Reference, 0.0000805 secs]",
                    "2020-03-27T17:00:07.725+0000: 60170.738: [GC (CMS Final Remark) [YG occupancy: 13268309 K (24536704 K)]2020-03-27T17:00:07.725+0000: 60170.739: [GC (CMS Final Remark) 2020-03-27T17:00:07.725+0000: 60170.739: [ParNew2020-03-27T17:00:07.836+0000: 60170.850: [SoftReference, 0 refs, 0.0006379 secs]2020-03-27T17:00:07.837+0000: 60170.850: [WeakReference, 94981 refs, 0.0016141 secs]2020-03-27T17:00:07.838+0000: 60170.852: [FinalReference, 22953 refs, 0.0011567 secs]2020-03-27T17:00:07.840+0000: 60170.853: [PhantomReference, 0 refs, 5 refs, 0.0007871 secs]2020-03-27T17:00:07.840+0000: 60170.854: [JNI Weak Reference, 0.0002440 secs]"
            },
            {       // 24
                    "22.690: [Rescan (parallel) , 0.0135887 secs]22.704: [weak refs processing22.704: [SoftReference, 0 refs, 0.0000769 secs]22.704: [WeakReference, 0 refs, 0.0000869 secs]22.704: [FinalReference, 4049 refs, 0.0038791 secs]22.708: [PhantomReference, 0 refs, 0.0000943 secs]22.708: [JNI Weak Reference, 0.0000829 secs], 0.0044520 secs]22.708: [class unloading, 0.0205276 secs]22.729: [scrub symbol table, 0.0038738 secs]22.733: [scrub string table, 0.0046405 secs][1 CMS-remark: 243393K(9378240K)] 310268K(10375040K), 0.2151395 secs] [Times: user=1.12 sys=0.09, real=0.21 secs]",
                    "2016-03-22T10:02:55.730-0100: 27.165: [Rescan (parallel) , 0.0108559 secs]2016-03-22T10:02:55.741-0100: 27.176: [weak refs processing2016-03-22T10:02:55.741-0100: 27.176: [SoftReference, 0 refs, 0.0000050 secs]2016-03-22T10:02:55.741-0100: 27.176: [WeakReference, 1 refs, 0.0000041 secs]2016-03-22T10:02:55.741-0100: 27.176: [FinalReference, 101 refs, 0.0000787 secs]2016-03-22T10:02:55.741-0100: 27.176: [PhantomReference, 0 refs, 0 refs, 0.0000042 secs]2016-03-22T10:02:55.741-0100: 27.176: [JNI Weak Reference, 0.0000031 secs], 0.0001313 secs]2016-03-22T10:02:55.741-0100: 27.176: [class unloading, 0.0020614 secs]2016-03-22T10:02:55.743-0100: 27.178: [scrub symbol table, 0.0011067 secs]2016-03-22T10:02:55.745-0100: 27.179: [scrub string table, 0.0002172 secs][1 CMS-remark: 91357K(174784K)] 100061K(253440K), 0.0293410 secs] [Times: user=0.19 sys=0.00, real=0.03 secs]"
            },
            {       // 25
                    "2016-03-10T10:12:11.524-0800: 136848.854: [GC (Allocation Failure) 2016-03-10T10:12:11.524-0800: 136848.854: [ParNew (promotion failed): 2123349K->2128824K(2146944K), 0.4512930 secs]2016-03-10T10:12:11.975-0800: 136849.306: [CMS2016-03-10T10:12:12.238-0800: 136849.568: [CMS-concurrent-abortable-preclean: 0.366/0.858 secs] [Times: user=4.31 sys=0.13, real=0.86 secs]"
            },
            {       // 26
                    "(concurrent mode failure)2016-04-01T14:25:19.343-0700: 8722.809: [SoftReference, 614 refs, 0.0001950 secs]2016-04-01T14:25:19.343-0700: 8722.809: [WeakReference, 13254 refs, 0.0018480 secs]2016-04-01T14:25:19.345-0700: 8722.811: [FinalReference, 7595 refs, 0.0122480 secs]2016-04-01T14:25:19.357-0700: 8722.823: [PhantomReference, 197 refs, 0.0000370 secs]2016-04-01T14:25:19.357-0700: 8722.823: [JNI Weak Reference, 0.0000220 secs]: 4172448K->688726K(4218880K), 4.7132540 secs] 5658948K->688726K(5946048K), [CMS Perm : 417636K->417572K(1048576K)], 4.7161730 secs] [Times: user=5.17 sys=0.02, real=4.72 secs]"
            },
            {       // 27
                    "13.077: [GC[YG occupancy: 3081 K (18624 K)]13.077: [Rescan (parallel) , 0.0009121 secs]13.078: [weak refs processing, 0.0000365 secs] [1 CMS-remark: 35949K(62656K)] 39030K(81280K), 0.0010300 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]"
            },
            {       // 28
                    "2015-06-17T12:36:11.086+0200: 9.799: [GC[YG occupancy: 1342192 K (7549760 K)]9.799: [Rescan (parallel) , 0.3111790 secs]10.111: [weak refs processing10.111: [SoftReference, 0 refs, 0.0000190 secs]10.111: [WeakReference, 0 refs, 0.0000070 secs]10.111: [FinalReference, 0 refs, 0.0000060 secs]10.111: [PhantomReference, 0 refs, 0.0000060 secs]10.111: [JNI Weak Reference, 0.0000110 secs], 0.0001610 secs]10.111: [class unloading, 0.0195700 secs]10.130: [scrub symbol & string tables, 0.0160610 secs] [1 CMS-remark: 0K(8388608K)] 1342192K(15938368K), 0.3493710 secs] [Times: user=4.24 sys=0.02, real=0.35 secs]10.111: [weak refs processing10.111: [SoftReference, 0 refs, 0.0000190 secs]10.111: [WeakReference, 0 refs, 0.0000070 secs]10.111: [FinalReference, 0 refs, 0.0000060 secs]10.111: [PhantomReference, 0 refs, 0.0000060 secs]10.111: [JNI Weak Reference, 0.0000110 secs], 0.0001610 secs]10.111: [class unloading, 0.0195700 secs]10.130: [scrub symbol & string tables, 0.0160610 secs] [1 CMS-remark: 0K(8388608K)] 1342192K(15938368K), 0.3493710 secs] [Times: user=4.24 sys=0.02, real=0.35 secs]",
                    "14.055: [GC (CMS Final Remark) [YG occupancy: 3910 K (78656 K)]14.055: [Rescan (parallel) , 0.0002109 secs]14.055: [weak refs processing14.055: [SoftReference, 0 refs, 0.0000031 secs]14.055: [WeakReference, 0 refs, 0.0000023 secs]14.055: [FinalReference, 0 refs, 0.0000148 secs]14.055: [PhantomReference, 0 refs, 0 refs, 0.0000028 secs]14.055: [JNI Weak Reference, 0.0000076 secs], 0.0000577 secs]14.055: [class unloading, 0.0011012 secs]14.056: [scrub symbol table, 0.0009175 secs]14.057: [scrub string table, 0.0001351 secs][1 CMS-remark: 174628K(174784K)] 178538K(253440K), 0.0025245 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]",
                    "2016-04-01T15:03:47.240-0700: 11030.706: [GC (CMS Final Remark)[YG occupancy: 1334704 K (1727168 K)]2016-04-01T15:03:47.241-0700: 11030.707: [Rescan (parallel) , 0.2638220 secs]2016-04-01T15:03:47.505-0700: 11030.970: [weak refs processing2016-04-01T15:03:47.505-0700: 11030.970: [SoftReference, 92 refs, 0.0000520 secs]2016-04-01T15:03:47.505-0700: 11030.971: [WeakReference, 1075 refs, 0.0003030 secs]2016-04-01T15:03:47.505-0700: 11030.971: [FinalReference, 5581 refs, 0.0232140 secs]2016-04-01T15:03:47.528-0700: 11030.994: [PhantomReference, 0 refs, 0.0000200 secs]2016-04-01T15:03:47.528-0700: 11030.994: [JNI Weak Reference, 0.0000270 secs], 0.0237120 secs]2016-04-01T15:03:47.528-0700: 11030.994: [scrub string table, 0.0047220 secs] [1 CMS-remark: 3165109K(4218880K)] 4499814K(5946048K), 0.2937310 secs] [Times: user=1.85 sys=0.00, real=0.30 secs]",
            },
            {       // 29
                    "2.807: [GC (Allocation Failure) 2.807: [DefNew2.831: [SoftReference, 0 refs, 0.0000395 secs]2.831: [WeakReference, 14 refs, 0.0000337 secs]2.831: [FinalReference, 234 refs, 0.0018361 secs]2.833: [PhantomReference, 0 refs, 0 refs, 0.0000135 secs]2.833: [JNI Weak Reference, 239 refs, 0.0000939 secs]",
                    "106.105: [GC106.105: [DefNew106.159: [SoftReference, 0 refs, 0.0000460 secs]106.159: [WeakReference, 284 refs, 0.0000190 secs]106.159: [FinalReference, 12141 refs, 0.0230340 secs]106.182: [PhantomReference, 0 refs, 2 refs, 0.0000090 secs]106.182: [JNI Weak Reference, 0.0000050 secs]"
            },
            {       // 30
                    "2016-04-01T12:01:14.771-0700: 78.237: [Full GC (System.gc())2016-04-01T12:01:14.772-0700: 78.237: [CMS2016-04-01T12:01:15.156-0700: 78.622: [SoftReference, 0 refs, 0.0001020 secs]2016-04-01T12:01:15.156-0700: 78.622: [WeakReference, 6475 refs, 0.0009080 secs]2016-04-01T12:01:15.157-0700: 78.623: [FinalReference, 14114 refs, 0.0072970 secs]2016-04-01T12:01:15.165-0700: 78.631: [PhantomReference, 48 refs, 0.0000190 secs]2016-04-01T12:01:15.165-0700: 78.631: [JNI Weak Reference, 0.0000390 secs]: 21101K->196164K(4218880K), 1.8134980 secs] 1517335K->196164K(5946048K), [CMS Perm : 128300K->127561K(1048576K)], 1.8143070 secs] [Times: user=1.73 sys=0.09, real=1.81 secs]",
                    "2016-04-01T12:01:14.771-0700: 78.237: [Full GC (System.gc())2016-04-01T12:01:14.772-0700: 78.237: [CMS2016-04-01T12:01:15.156-0700: 78.622: [SoftReference, 0 refs, 0.0001020 secs]2016-04-01T12:01:15.156-0700: 78.622: [WeakReference, 6475 refs, 0.0009080 secs]2016-04-01T12:01:15.157-0700: 78.623: [FinalReference, 14114 refs, 0.0072970 secs]2016-04-01T12:01:15.165-0700: 78.631: [PhantomReference, 48 refs, 0.0000190 secs]2016-04-01T12:01:15.165-0700: 78.631: [JNI Weak Reference, 0.0000390 secs]: 21101K->196164K(4218880K), 1.8134980 secs] 1517335K->196164K(5946048K), [Metaspace: 128300K->127561K(1048576K)], 1.8143070 secs] [Times: user=1.73 sys=0.09, real=1.81 secs]"
            },
            {       // 31
                    "19.293: [GC (CMS Final Remark) [YG occupancy: 4110424 K (4456448 K)]{Heap before GC invocations=0 (full 1):"
            },
            {       // 32
                    "19.335: [Rescan (parallel) , 0.0050917 secs]19.340: [weak refs processing, 0.0000278 secs]19.340: [class unloading, 0.0233772 secs]19.364: [scrub symbol table, 0.0055223 secs]19.369: [scrub string table, 0.0009215 secs][1 CMS-remark: 0K(3276800K)] 60709K(7733248K), 0.0798048 secs] [Times: user=0.69 sys=0.12, real=0.08 secs]"
            },
            {       // 33
                    "2016-04-01T13:47:08.272-0700: 6431.738: [GC (Allocation Failure)2016-04-01T13:47:08.273-0700: 6431.739: [ParNew2016-04-01T13:47:08.302-0700: 6431.768: [SoftReference, 0 refs, 0.0000880 secs]2016-04-01T13:47:08.302-0700: 6431.768: [WeakReference, 915 refs, 0.0001130 secs]2016-04-01T13:47:08.302-0700: 6431.768: [FinalReference, 1507 refs, 0.0017270 secs]2016-04-01T13:47:08.304-0700: 6431.770: [PhantomReference, 0 refs, 0.0000130 secs]2016-04-01T13:47:08.304-0700: 6431.770: [JNI Weak Reference, 0.0000230 secs]"
            },
            {       // 34
                    "(concurrent mode failure): 62354K->8302K(64768K), 0.0931888 secs] 79477K->8302K(83392K), [CMS Perm : 10698K->10698K(21248K)], 0.0956950 secs] [Times: user=0.09 sys=0.00, real=0.09 secs]"
            },
            {       // 35
                    "2016-03-22T10:02:41.962-0100: 13.396: [GC (Allocation Failure) 2016-03-22T10:02:41.962-0100: 13.396: [ParNew2016-03-22T10:02:41.970-0100: 13.404: [SoftReference, 0 refs, 0.0000260 secs]2016-03-22T10:02:41.970-0100: 13.404: [WeakReference, 59 refs, 0.0000110 secs]2016-03-22T10:02:41.970-0100: 13.404: [FinalReference, 1407 refs, 0.0025979 secs]2016-03-22T10:02:41.973-0100: 13.407: [PhantomReference, 0 refs, 0 refs, 0.0000131 secs]2016-03-22T10:02:41.973-0100: 13.407: [JNI Weak Reference, 0.0000088 secs]: 69952K->8704K(78656K), 0.0104509 secs] 69952K->11354K(253440K), 0.0105137 secs] [Times: user=0.04 sys=0.01, real=0.01 secs]",
            },
            {       // 36
                    "2016-03-22T10:02:55.716-0100: 27.150: [GC (CMS Final Remark) [YG occupancy: 55606 K (78656 K)]2016-03-22T10:02:55.716-0100: 27.150: [GC (CMS Final Remark) 2016-03-22T10:02:55.716-0100: 27.150: [ParNew2016-03-22T10:02:55.730-0100: 27.165: [SoftReference, 0 refs, 0.0000269 secs]2016-03-22T10:02:55.730-0100: 27.165: [WeakReference, 0 refs, 0.0000045 secs]2016-03-22T10:02:55.730-0100: 27.165: [FinalReference, 3 refs, 0.0000073 secs]2016-03-22T10:02:55.730-0100: 27.165: [PhantomReference, 0 refs, 0 refs, 0.0000045 secs]2016-03-22T10:02:55.730-0100: 27.165: [JNI Weak Reference, 0.0000035 secs]: 55606K->8704K(78656K), 0.0145788 secs] 143782K->100061K(253440K), 0.0145973 secs] [Times: user=0.10 sys=0.00, real=0.02 secs]"
            },
            {       // 37
                    "2019-11-14T14:18:53.162+0000: 57037.295: [ParNew: 1966080K->1966080K(1966080K), 0.0000438 secs]2019-11-14T14:18:53.163+0000: 57037.295: [CMS2019-11-14T14:18:53.236+0000: 57037.369: [CMS-concurrent-preclean: 0.155/0.178 secs] [Times: user=1.43 sys=0.17, real=0.18 secs]"
            },
            {       // 38
                    "2019-11-14T15:45:27.035+0000: 62231.167: [CMS2019-11-14T15:45:27.379+0000: 62231.511: [CMS-concurrent-preclean: 1.107/1.119 secs] [Times: user=4.99 sys=0.86, real=1.11 secs]"
            },
            {       // 39
                    "(concurrent mode failure)CMS: Large block 0x00000007bfffdfd0"
            },
            {       // 40
                    "2020-03-27T17:20:09.966+0000: 61372.980: [Preclean SoftReferences, 0.0000064 secs]2020-03-27T17:20:09.966+0000: 61372.980: [Preclean WeakReferences, 0.0010360 secs]2020-03-27T17:20:09.967+0000: 61372.981: [Preclean FinalReferences, 0.0017374 secs]2020-03-27T17:20:09.969+0000: 61372.983: [Preclean PhantomReferences, 0.0000900 secs]2020-03-27T17:20:16.627+0000: 61379.640: [GC (Allocation Failure) 2020-03-27T17:20:16.627+0000: 61379.640: [ParNew2020-03-27T17:20:16.828+0000: 61379.842: [SoftReference, 0 refs, 0.0006072 secs]2020-03-27T17:20:16.829+0000: 61379.842: [WeakReference, 155571 refs, 0.0025794 secs]2020-03-27T17:20:16.831+0000: 61379.845: [FinalReference, 52557 refs, 0.0025628 secs]2020-03-27T17:20:16.834+0000: 61379.847: [PhantomReference, 0 refs, 4 refs, 0.0007787 secs]2020-03-27T17:20:16.835+0000: 61379.848: [JNI Weak Reference, 0.0731165 secs]"
            }
    };
}



