// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

/**
 * Patterns in the GC log associated with the Concurrent Mark and Sweep (CMS)
 * Collector.
 * <p>
 * TODO The patterns could be split into separate files, e.g. ParNew, CMF and
 * so on
 */
public interface CMSPatterns extends SharedPatterns {

    //[Rescan (non-parallel) 139439.229:
    //[Rescan (parallel) , 0,1736080 secs]
    String RESCAN_BLOCK = DATE_TIMESTAMP + "\\[Rescan \\((?:non-)?parallel\\) , " + PAUSE_TIME + "\\]";
    String REMARK_BLOCK = "\\[1 CMS-remark: " + OCCUPANCY_CONFIGURED + "\\] " + OCCUPANCY_CONFIGURED_PAUSE + "\\]";
    String CPU_WALLCLOCK = TIME + "/" + TIME + " secs";
    String CMS_TENURED_BLOCK = DATE_TIMESTAMP + "\\[CMS: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]";
    String SERIAL_TENURED_BLOCK = TIMESTAMP + "\\[Tenured: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]";
    String PARNEW_BLOCK = DATE_TIMESTAMP + "\\[ParNew: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]";
    //445909.040: [ParNew (1: promotion failure size = 4629668)  (promotion failed): 460096K->460096K(460096K), 0.8467130 secs]
    String PROMOTION_FAILURE_SIZE_BLOCK = "\\(\\d+: promotion failure size = " + BYTES + "\\)";
    GCParseRule PARNEW_PROMOTION_FAILURE_SIZE_BLOCK = new GCParseRule("PARNEW_PROMOTION_FAILURE_SIZE_BLOCK", PROMOTION_FAILURE_SIZE_BLOCK);
    String PARNEW_WITH_PROMOTION_FAILURE_SIZE_BLOCK = DATE_TIMESTAMP + "\\[ParNew " + PROMOTION_FAILURE_SIZE_BLOCK + "  \\(promotion failed\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]";
    String PARNEW_PROMOTION_FAILED_BLOCK = DATE_TIMESTAMP + "\\[ParNew \\(promotion failed\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]";
    String YOUNG_GEN_BLOCK = "\\[YG occupancy: " + COUNTER + " K \\(" + COUNTER + " K\\)\\]";
    String CMS_PHASE_START = DATE_TIMESTAMP + "\\[CMS-concurrent-(.+)-start\\]";
    String CMS_PHASE_END = DATE_TIMESTAMP + "\\[CMS-concurrent-(.+): " + CPU_WALLCLOCK + "\\]";
    String ABORT_PRECLEAN_DUE_TO_TIME_BLOCK = "CMS: abort preclean due to time " + CMS_PHASE_END;
    String CMS_PHASE_END_YIELD = "\\(CMS-concurrent-(.+) yielded " + COUNTER + " times\\)";
    String CARD = "\\[(\\d+) iterations, (\\d+) waits, (\\d+) cards\\)\\]";
    String CARD_SUMMARY = TIMESTAMP + "\\[CMS \\(cardTable: " + COUNTER + " cards, re-scanned " + COUNTER + " cards, " + COUNTER + " iterations\\)";
    String FLS_LARGE_BLOCK_BODY = "CMS: Large block " + MEMORY_ADDRESS;

    /****** Current working set  *************/

    GCParseRule REMARK_CLAUSE = new GCParseRule("REMARK_CLAUSE", REMARK_BLOCK);
    GCParseRule POOL_OCCUPANCY_HEAP_OCCUPANCY_BLOCK = new GCParseRule("POOL_OCCUPANCY_HEAP_OCCUPANCY_BLOCK", BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", ");

    /**********  CMS Phase records **********/
    //3.307: [GC [1 CMS-initial-mark: 0K(18874368K)] 302009K(20761856K), 0.0994470 secs] [Times: user=0.20 sys=0.00, real=0.10 secs]
    //12.986: [GC[1 CMS-initial-mark: 33532K(62656K)] 49652K(81280K), 0.0014191 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
    GCParseRule INITIAL_MARK = new GCParseRule("INITIAL_MARK", GC_PREFIX + "\\[1 CMS-initial-mark: " + OCCUPANCY_CONFIGURED + "\\] " + OCCUPANCY_CONFIGURED_PAUSE + "\\]");
    GCParseRule CONCURRENT_PHASE_START = new GCParseRule("CONCURRENT_PHASE_START", "^" + CMS_PHASE_START);

    GCParseRule CONCURRENT_PHASE_END = new GCParseRule("CONCURRENT_PHASE_END", "^" + CMS_PHASE_END + "$");
    GCParseRule CONCURRENT_PHASE_END_WITH_CPU_SUMMARY = new GCParseRule("CONCURRENT_PHASE_END", "^" + CMS_PHASE_END + " " + CPU_SUMMARY);

    GCParseRule CONCURRENT_PHASE_START_BLOCK = new GCParseRule("CONCURRENT_PHASE_START_BLOCK", CMS_PHASE_START);
    GCParseRule CONCURRENT_PHASE_END_BLOCK = new GCParseRule("CONCURRENT_PHASE_END_BLOCK", CMS_PHASE_END + " " + CPU_SUMMARY);
    GCParseRule ABORT_PRECLEAN_DUE_TO_TIME_CLAUSE = new GCParseRule("ABORT_PRECLEAN_DUE_TO_TIME_CLAUSE", "^" + ABORT_PRECLEAN_DUE_TO_TIME_BLOCK);

    /********** Remark statements **********/
    //, 0.0404150 secs]204737.599: [weak refs processing, 0.0112190 secs] [1 CMS-remark: 30259925K(60327552K)] 30338613K(60768448K), 0.0528880 secs]
    String REMARK_DETAILS_BLOCK = ", " + PAUSE_TIME + "\\]" + WEAK_REF_BLOCK + "(?:" + STRING_TABLE_SCRUB_BLOCK + ")? " + REMARK_BLOCK;
    GCParseRule REMARK_DETAILS = new GCParseRule("REMARK_DETAILS", "^" + REMARK_DETAILS_BLOCK);
    //, 0.0569803 secs]109.194: [weak refs processing, 0.0000112 secs]109.194: [class unloading, 0.0018913 secs]109.196: [scrub symbol & string tables, 0.0026648 secs] [1 CMS-remark: 3327853K(4194304K)] 3892284K(5872064K), 0.0626457 secs]
    GCParseRule RESCAN_WEAK_CLASS_SYMBOL_STRING = new GCParseRule("RESCAN_WEAK_CLASS_SYMBOL_STRING", "^, " + PAUSE_TIME + "\\]" + WEAK_REF_BLOCK + CLASS_UNLOADING_BLOCK + STRING_AND_SYMBOL_SCRUB_BLOCK + " " + REMARK_BLOCK);

    //2013-01-25T09:52:33.206+0100: 27,658: [GC[YG occupancy: 320093 K (629120 K)]27,658: [Rescan (parallel) , 0,1736080 secs]27,832: [weak refs processing, 0,0000120 secs] [1 CMS-remark: 129666K(1398144K)] 449759K(2027264K), 0,1737410 secs]
    //13.077: [GC[YG occupancy: 3081 K (18624 K)]13.077: [Rescan (parallel) , 0.0009121 secs]13.078: [weak refs processing, 0.0000365 secs] [1 CMS-remark: 35949K(62656K)] 39030K(81280K), 0.0010300 secs]
    GCParseRule REMARK = new GCParseRule("REMARK", DATE_TIMESTAMP + YOUNG_GEN_BLOCK + TIMESTAMP + "\\[Rescan \\(parallel\\) " + REMARK_DETAILS_BLOCK);

    //20.600: [GC[YG occupancy: 1706 K (2304 K)]20.600: [Rescan (parallel) , 0.0007606 secs]20.601: [weak refs processing, 0.0000336 secs] [1 CMS-remark: 2726K(3328K)] 4433K(5632K), 0.0008842 secs]
    GCParseRule PARALLEL_REMARK_WEAK_REF = new GCParseRule("PARALLEL_REMARK_WEAK_REF", GC_PREFIX + YOUNG_GEN_BLOCK + RESCAN_BLOCK + WEAK_REF_BLOCK + " " + REMARK_BLOCK);

    //1.340: [GC (CMS Final Remark)[YG occupancy: 206871 K (349568 K)]41.340: [Rescan (parallel) , 0.0696600 secs]41.410: [weak refs processing, 0.0000440 secs]41.410: [scrub string table, 0.0001880 secs] [1 CMS-remark: 2692K(5376K)] 209564K(354944K), 0.0699640 secs] [Times: user=0.50 sys=0.01, real=0.07 secs]
    GCParseRule PARALLEL_REMARK_STRING_TABLE = new GCParseRule("PARALLEL_REMARK_STRING_TABLE", GC_PREFIX + YOUNG_GEN_BLOCK + RESCAN_BLOCK + WEAK_REF_BLOCK + STRING_TABLE_SCRUB_BLOCK + " " + REMARK_BLOCK);

    //2014-09-23T15:38:04.730+0000: 1720.796: [GC (CMS Final Remark) [YG occupancy: 60332 K (145024 K)]1720.796: [Rescan (parallel) , 0.0074806 secs]1720.804: [weak refs processing, 0.0099667 secs]1720.814: [class unloading, 0.0248026 secs]1720.839: [scrub symbol table, 0.0035960 secs]1720.842: [scrub string table, 0.0005701 secs][1 CMS-remark: 3294934K(4204000K)] 3355266K(4349024K), 0.0569109 secs]
    //2014-09-25T04:31:45.205+0000: 134541.271: [GC (CMS Final Remark) [YG occupancy: 252612 K (996800 K)]134541.271: [Rescan (parallel) , 0.0110878 secs]134541.282: [weak refs processing, 0.0493517 secs]134541.331: [class unloading, 0.0268545 secs]134541.358: [scrub symbol table, 0.0022805 secs]134541.360: [scrub string table, 0.0005249 secs][1 CMS-remark: 6258193K(7281088K)] 6510805K(8277888K), 0.1123854 secs]
    GCParseRule PARALLEL_REMARK_CLASS_UNLOADING = new GCParseRule("PARALLEL_REMARK_CLASS_UNLOADING", GC_PREFIX + YOUNG_GEN_BLOCK + RESCAN_BLOCK + WEAK_REF_BLOCK + CLASS_UNLOADING_BLOCK + SYMBOL_TABLE_SCRUB_BLOCK + STRING_TABLE_SCRUB_BLOCK + REMARK_BLOCK);

    //307.786: [GC[YG occupancy: 177070 K (314624 K)]307.786: [Rescan (parallel) , 0.0916320 secs]307.878: [weak refs processing, 0.0018170 secs]307.880: [class unloading, 0.0290660 secs]307.909: [scrub symbol & string tables, 0.0119490 secs] [1 CMS-remark: 358640K(699072K)] 535710K(1013696K), 0.1398630 secs]
    GCParseRule PARALLEL_REMARK_STRING_SYMBOL = new GCParseRule("PARALLEL_REMARK_STRING_SYMBOL", GC_PREFIX + YOUNG_GEN_BLOCK + RESCAN_BLOCK + WEAK_REF_BLOCK + CLASS_UNLOADING_BLOCK + STRING_AND_SYMBOL_SCRUB_BLOCK + " " + REMARK_BLOCK);
    //15.533: [GC[YG occupancy: 3356 K (94184 K)]15.533: [Rescan (parallel) , 0.0126940 secs]15.546: [weak refs processing, 0.0000090 secs]15.546: [class unloading, 0.0001220 secs]5.546: [scrub symbol table, 0.0004690 secs]5.546: [scrub string table, 0.0000180 secs] [1 CMS-remark: 0K(12582912K)] 335836K(22020096K), 0.0134260 secs]
    GCParseRule PARALLEL_REMARK_WEAK_CLASS_SYMBOL_STRING = new GCParseRule("PARALLEL_REMARK_WEAK_CLASS_SYMBOL_STRING", GC_PREFIX + YOUNG_GEN_BLOCK + RESCAN_BLOCK + WEAK_REF_BLOCK + CLASS_UNLOADING_BLOCK + SYMBOL_TABLE_SCRUB_BLOCK + STRING_TABLE_SCRUB_BLOCK + " " + REMARK_BLOCK);

    ////41.340: [GC (CMS Final Remark)[YG occupancy: 206871 K (349568 K)]41.340: [Rescan (parallel) , 0.0696600 secs]41.410: [weak refs processing, 0.0000440 secs]41.410: [scrub string table, 0.0001880 secs] [1 CMS-remark: 2692K(5376K)] 209564K(354944K), 0.0699640 secs] [Times: user=0.50 sys=0.01, real=0.07 secs]
    //2014-06-10T17:50:11.725-0700: 4981160.497: [GC[YG occupancy: 77784 K (153344 K)]2014-06-10T17:50:11.725-0700: 4981160.498: [Rescan (parallel) , 0.1474240 secs]2014-06-10T17:50:11.873-0700: 4981160.645: [weak refs processing, 0.0000430 secs]2014-06-10T17:50:11.873-0700: 4981160.645: [scrub string table, 0.0009790 secs] [1 CMS-remark: 1897500K(1926784K)] 1975285K(2080128K), 0.1486260 secs]
    GCParseRule PARALLEL_REMARK_WEAK_STRING = new GCParseRule("PARALLEL_REMARK_WEAK_STRING", GC_PREFIX + YOUNG_GEN_BLOCK + RESCAN_BLOCK + WEAK_REF_BLOCK + STRING_TABLE_SCRUB_BLOCK + " " + REMARK_BLOCK);

    //2012-10-21T09:12:43.246+0100: 139439.228: [GC[YG occupancy: 1370 K (19136 K)]139439.229: [Rescan (non-parallel) 139439.229: [grey object rescan, 0.0013400 secs]139439.230: [root rescan, 0.0051750 secs], 0.0065730 secs]139439.235: [weak refs processing, 0.0601980 secs] [1 CMS-remark: 120458K(240896K)] 121829K(260032K), 0.0674900 secs]
    GCParseRule SERIAL_REMARK_SCAN_BREAKDOWNS = new GCParseRule("SERIAL_REMARK_SCAN_BREAKDOWNS", GC_PREFIX + YOUNG_GEN_BLOCK + TIMESTAMP + "\\[Rescan \\(non-parallel\\) " + TIMESTAMP + "\\[grey object rescan, " + PAUSE_TIME + "\\]" + TIMESTAMP + "\\[root rescan, " + PAUSE_TIME + "\\], " + PAUSE_TIME + "\\]" + WEAK_REF_BLOCK + " " + REMARK_BLOCK);

    //14.055: [GC (CMS Final Remark) [YG occupancy: 3910 K (78656 K)]14.055: [Rescan (parallel) , 0.0002109 secs]14.055: [weak refs processing14.055: [SoftReference, 0 refs, 0.0000031 secs]14.055: [WeakReference, 0 refs, 0.0000023 secs]14.055: [FinalReference, 0 refs, 0.0000148 secs]14.055: [PhantomReference, 0 refs, 0 refs, 0.0000028 secs]14.055: [JNI Weak Reference, 0.0000076 secs], 0.0000577 secs]14.055: [class unloading, 0.0011012 secs]14.056: [scrub symbol table, 0.0009175 secs]14.057: [scrub string table, 0.0001351 secs][1 CMS-remark: 174628K(174784K)] 178538K(253440K), 0.0025245 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
    //2016-04-01T15:03:47.240-0700: 11030.706: [GC (CMS Final Remark)[YG occupancy: 1334704 K (1727168 K)]2016-04-01T15:03:47.241-0700: 11030.707: [Rescan (parallel) , 0.2638220 secs]2016-04-01T15:03:47.505-0700: 11030.970: [weak refs processing2016-04-01T15:03:47.505-0700: 11030.970: [SoftReference, 92 refs, 0.0000520 secs]2016-04-01T15:03:47.505-0700: 11030.971: [WeakReference, 1075 refs, 0.0003030 secs]2016-04-01T15:03:47.505-0700: 11030.971: [FinalReference, 5581 refs, 0.0232140 secs]2016-04-01T15:03:47.528-0700: 11030.994: [PhantomReference, 0 refs, 0.0000200 secs]2016-04-01T15:03:47.528-0700: 11030.994: [JNI Weak Reference, 0.0000270 secs], 0.0237120 secs]2016-04-01T15:03:47.528-0700: 11030.994: [scrub string table, 0.0047220 secs] [1 CMS-remark: 3165109K(4218880K)] 4499814K(5946048K), 0.2937310 secs] [Times: user=1.85 sys=0.00, real=0.30 secs]
    //2015-06-17T12:36:11.086+0200: 9.799: [GC[YG occupancy: 1342192 K (7549760 K)]9.799: [Rescan (parallel) , 0.3111790 secs]10.111: [weak refs processing10.111: [SoftReference, 0 refs, 0.0000190 secs]10.111: [WeakReference, 0 refs, 0.0000070 secs]10.111: [FinalReference, 0 refs, 0.0000060 secs]10.111: [PhantomReference, 0 refs, 0.0000060 secs]10.111: [JNI Weak Reference, 0.0000110 secs], 0.0001610 secs]10.111: [class unloading, 0.0195700 secs]10.130: [scrub symbol & string tables, 0.0160610 secs] [1 CMS-remark: 0K(8388608K)] 1342192K(15938368K), 0.3493710 secs] [Times: user=4.24 sys=0.02, real=0.35 secs]10.111: [weak refs processing10.111: [SoftReference, 0 refs, 0.0000190 secs]10.111: [WeakReference, 0 refs, 0.0000070 secs]10.111: [FinalReference, 0 refs, 0.0000060 secs]10.111: [PhantomReference, 0 refs, 0.0000060 secs]10.111: [JNI Weak Reference, 0.0000110 secs], 0.0001610 secs]10.111: [class unloading, 0.0195700 secs]10.130: [scrub symbol & string tables, 0.0160610 secs] [1 CMS-remark: 0K(8388608K)] 1342192K(15938368K), 0.3493710 secs] [Times: user=4.24 sys=0.02, real=0.35 secs]
    GCParseRule REMARK_REFERENCE_PROCESSING = new GCParseRule("REMARK_REFERENCE_PROCESSING", GC_PREFIX + YOUNG_GEN_BLOCK + RESCAN_BLOCK + DATE_TIMESTAMP + "\\[weak refs processing" + REFERENCE_RECORDS + ", " + PAUSE_TIME + "\\]");// + CLASS_UNLOADING_BLOCK + ")?)" + SYMBOL_TABLE_SCRUB_BLOCK + ")?(" + STRING_TABLE_SCRUB_BLOCK + ")?" + REMARK_BLOCK);

    //2367.957: [Rescan (parallel) , 0.0056960 secs]2367.963: [weak refs processing, 0.0016430 secs] [1 CMS-remark: 2119807K(3014656K)] 2123111K(3132672K), 0.0146280 secs]
    //1650.197: [Rescan (parallel) , 0.1249600 secs]1650.322: [weak refs processing, 0.0010950 secs]1650.323: [scrub string table, 0.0028580 secs] [1 CMS-remark: 4234372K(8388608K)] 4930231K(12320768K), 0.4478400 secs]
    //1333.519: [Rescan (parallel) , 0.2126280 secs]1333.732: [weak refs processing, 0.0656060 secs] [1 CMS-remark: 6371822K(18874368K)] 7000942K(24536704K), 1.2301560 secs]
    GCParseRule PARALLEL_RESCAN = new GCParseRule("PARALLEL_RESCAN", "^" + DATE_TIMESTAMP + "\\[Rescan \\(parallel\\) " + REMARK_DETAILS_BLOCK);

    //1737262.122: [GC1737262.122: [Rescan (parallel) , 0.9427801 secs]1737263.065: [weak refs processing, 0.0014950 secs] [1 CMS-remark: 1229512K(1572864K)] 1497632K(2044736K), 0.9450265 secs]
    GCParseRule PARALLEL_RESCAN_V2 = new GCParseRule("PARALLEL_RESCAN_V2", DATE_TIMESTAMP + "\\[GC" + TIMESTAMP + "\\[Rescan \\(parallel\\) " + REMARK_DETAILS_BLOCK);

    //11.980: [Rescan (parallel) , 0.0085120 secs]11.988: [weak refs processing, 0.0000210 secs]11.988: [class unloading, 0.0097120 secs]11.998: [scrub symbol & string tables, 0.0073130 secs] [1 CMS-remark: 0K(18874368K)] 28278K(20761856K), 0.0462600 secs]
    GCParseRule PARALLEL_RESCAN_WEAK_CLASS_SCRUB = new GCParseRule("PARALLEL_RESCAN_WEAK_CLASS_SCRUB", "^" + DATE_TIMESTAMP + "\\[Rescan \\(parallel\\) , " + PAUSE_TIME + "\\]" + WEAK_REF_BLOCK + CLASS_UNLOADING_BLOCK + STRING_AND_SYMBOL_SCRUB_BLOCK + " " + REMARK_BLOCK);

    /**********  ParNew records **********/

    //48.021: [GC48.021: [ParNew: 306686K->34046K(306688K), 0.3196120 secs] 1341473K->1125818K(8669952K), 0.3197540 secs]
    GCParseRule PARNEW = new GCParseRule("PARNEW", "^(" + GC_PREFIX + ")?" + PARNEW_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");
    //rule is too liberal -> GCParseRule PARNEW = new GCParseRule("PREFIXED_PARNEW", "^(" + GC_PREFIX + ")?" + PARNEW_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");
    GCParseRule PREFIXED_PARNEW = new GCParseRule("PREFIXED_PARNEW", "^" + GC_PREFIX + PARNEW_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");
    //9.811: [GC 9.811: [ParNew
    //2014-03-29T10:03:45.431+1030: 19.732: [GC19.732: [ParNew
    GCParseRule PARNEW_TENURING = new GCParseRule("", "^" + GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew$");
    //Rare but we see it.
    //2015-03-20T03:18:49.689+0100: 456.849: [GC (Allocation Failure) 456.849: [ParNew0[3]: 6/61264/16
    GCParseRule PARNEW_PLAB = new GCParseRule("PARNEW_PLAB", GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew" + PLAB);
    GCParseRule PLAB_ENTRY = new GCParseRule("PLAB_ENTRY", "^" + PLAB);
    GCParseRule PLAB_SUMMARY = new GCParseRule("PLAB_SUMMARY", "^\\[\\d+\\]: \\d+$");

    //46.435: [GC 46.435: [ParNew: 19136K->19136K(19136K), 0.0000274 secs]46.435: [CMS46.458: [CMS-concurrent-sweep: 0.060/0.117 secs] [Times: user=0.21 sys=0.01, real=0.12 secs]
    GCParseRule PARNEW_CONCURRENT_MODE_END = new GCParseRule("PARNEW_CONCURRENT_MODE_END", GC_PREFIX + PARNEW_BLOCK + DATE_TIMESTAMP + "\\[CMS" + CMS_PHASE_END);

    //26194.412: [GC 26194.412: [ParNew: 1887488K->1887488K(1887488K), 0.0000290 secs]26194.412: [CMS (cardTable: 848 cards, re-scanned 10457702 cards, 2 iterations)
    GCParseRule PARNEW_CARDTABLE = new GCParseRule("PARNEW_CARDTABLE", GC_PREFIX + PARNEW_BLOCK + CARD_SUMMARY);

    //845.459: [GC845.459: [ParNew (promotion failed): 1887488K->1887488K(1887488K), 2.7652560 secs]848.224: [CMS (cardTable: 1 cards, re-scanned 14249258 cards, 3 iterations)
    GCParseRule PARNEW_PROMOTION_FAILED_CARDTABLE = new GCParseRule("PARNEW_PROMOTION_FAILED_CARDTABLE", GC_PREFIX + TIMESTAMP + "\\[ParNew \\(promotion failed\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + CARD_SUMMARY);

    //"2016-03-22T10:02:41.962-0100: 13.396: [GC (Allocation Failure) 2016-03-22T10:02:41.962-0100: 13.396: [ParNew2016-03-22T10:02:41.970-0100: 13.404: [SoftReference, 0 refs, 0.0000260 secs]2016-03-22T10:02:41.970-0100: 13.404: [WeakReference, 59 refs, 0.0000110 secs]2016-03-22T10:02:41.970-0100: 13.404: [FinalReference, 1407 refs, 0.0025979 secs]2016-03-22T10:02:41.973-0100: 13.407: [PhantomReference, 0 refs, 0 refs, 0.0000131 secs]2016-03-22T10:02:41.973-0100: 13.407: [JNI Weak Reference, 0.0000088 secs]: 69952K->8704K(78656K), 0.0104509 secs] 69952K->11354K(253440K), 0.0105137 secs] [Times: user=0.04 sys=0.01, real=0.01 secs]"
    GCParseRule PARNEW_REFERENCE = new GCParseRule("PARNEW_REFERENCE", "^" + GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew" + REFERENCE_RECORDS + ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + CPU_SUMMARY);
    //6.604: [GC (Allocation Failure) 6.604: [ParNew6.607: [SoftReference, 0 refs, 0.0000178 secs]6.607: [WeakReference, 205 refs, 0.0000131 secs]6.607: [FinalReference, 294 refs, 0.0001246 secs]6.607: [PhantomReference, 29 refs, 1 refs, 0.0000062 secs]6.607: [JNI Weak Reference, 0.0000126 secs]
    GCParseRule PARNEW_REFERENCE_SPLIT = new GCParseRule("PARNEW_REFERENCE_SPLIT", "^" + GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew" + REFERENCE_RECORDS + "$");
    //2.807: [GC (Allocation Failure) 2.807: [DefNew2.831: [SoftReference, 0 refs, 0.0000395 secs]2.831: [WeakReference, 14 refs, 0.0000337 secs]2.831: [FinalReference, 234 refs, 0.0018361 secs]2.833: [PhantomReference, 0 refs, 0 refs, 0.0000135 secs]2.833: [JNI Weak Reference, 239 refs, 0.0000939 secs]
    GCParseRule DEFNEW_REFERENCE = new GCParseRule("DEFNEW_REFERENCE", "^" + GC_PREFIX + DATE_TIMESTAMP + "\\[DefNew" + REFERENCE_RECORDS + "$");
    //50.628: [ParNew62.880: [SoftReference, 0 refs, 0.0005810 secs]62.881: [WeakReference, 7744 refs, 0.0020630 secs]62.883: [FinalReference, 43825 refs, 4.5694050 secs]67.452: [PhantomReference, 0 refs, 0.0004600 secs]67.453: [JNI Weak Reference, 0.1600790 secs]
    //Split ParNew by TLAB with Reference records
    GCParseRule PARNEW_REFERENCE_SPLIT_BY_TLAB = new GCParseRule("PARNEW_REFERENCE_SPLIT_BY_TLAB", "^" + DATE_TIMESTAMP + "\\[ParNew" + REFERENCE_RECORDS + "$");

    //2016-03-10T10:12:11.524-0800: 136848.854: [GC (Allocation Failure) 2016-03-10T10:12:11.524-0800: 136848.854: [ParNew (promotion failed): 2123349K->2128824K(2146944K), 0.4512930 secs]2016-03-10T10:12:11.975-0800: 136849.306: [CMS2016-03-10T10:12:12.238-0800: 136849.568: [CMS-concurrent-abortable-preclean: 0.366/0.858 secs] [Times: user=4.31 sys=0.13, real=0.86 secs]
    GCParseRule PARNEW_PROMOTION_FAILED_CONCURRENT_PHASE = new GCParseRule("PARNEW_PROMOTION_FAILED_CONCURRENT_PHASE", "^" + GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew \\(promotion failed\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + DATE_TIMESTAMP + "\\[CMS" + CMS_PHASE_END);

    //10.099: [GC (Allocation Failure) 10.099: [ParNew10.100: [SoftReference, 0 refs, 0.0000141 secs]10.100: [WeakReference, 0 refs, 0.0000029 secs]10.100: [FinalReference, 0 refs, 0.0000026 secs]10.100: [PhantomReference, 0 refs, 0 refs, 0.0000033 secs]10.100: [JNI Weak Reference, 0.0000045 secs] (promotion failed): 72353K->71961K(78656K), 0.0009583 secs]10.100: [CMS10.100: [CMS-concurrent-abortable-preclean: 0.210/2.397 secs] [Times: user=15.12 sys=0.27, real=2.40 secs]
    //(promotion failed): 72353K->71961K(78656K), 0.0009583 secs]10.100: [CMS10.100: [CMS-concurrent-abortable-preclean: 0.210/2.397 secs] [Times: user=15.12 sys=0.27, real=2.40 secs]
    GCParseRule PARNEW_PROMOTION_FAILED_REFERENCE = new GCParseRule("PARNEW_PROMOTION_FAILED_REFERENCE", GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew" + REFERENCE_RECORDS + " \\(promotion failed\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + TIMESTAMP + "\\[CMS" + CMS_PHASE_END);
    //Sometimes the reference processing gets split out by the CMS closing record
    GCParseRule FLOATING_REFERENCE = new GCParseRule("FLOATING_REFERENCE", "^" + REFERENCE_RECORDS + "$");

    //7.703: [Preclean SoftReferences, 0.0000034 secs]7.703: [Preclean WeakReferences, 0.0000087 secs]7.703: [Preclean FinalReferences, 0.0000021 secs]7.703: [Preclean PhantomReferences, 0.0000034 secs]7.703: [CMS-concurrent-preclean: 0.000/0.000 secs]
    GCParseRule PRECLEAN_REFERENCE = new GCParseRule("PRECLEAN_REFERENCE", PRECLEAN_REFERENCE_RECORDS + CMS_PHASE_END);

    //2020-03-27T17:20:09.966+0000: 61372.980: [Preclean SoftReferences, 0.0000064 secs]2020-03-27T17:20:09.966+0000: 61372.980: [Preclean WeakReferences, 0.0010360 secs]2020-03-27T17:20:09.967+0000: 61372.981: [Preclean FinalReferences, 0.0017374 secs]2020-03-27T17:20:09.969+0000: 61372.983: [Preclean PhantomReferences, 0.0000900 secs]2020-03-27T17:20:16.627+0000: 61379.640: [GC (Allocation Failure) 2020-03-27T17:20:16.627+0000: 61379.640: [ParNew2020-03-27T17:20:16.828+0000: 61379.842: [SoftReference, 0 refs, 0.0006072 secs]2020-03-27T17:20:16.829+0000: 61379.842: [WeakReference, 155571 refs, 0.0025794 secs]2020-03-27T17:20:16.831+0000: 61379.845: [FinalReference, 52557 refs, 0.0025628 secs]2020-03-27T17:20:16.834+0000: 61379.847: [PhantomReference, 0 refs, 4 refs, 0.0007787 secs]2020-03-27T17:20:16.835+0000: 61379.848: [JNI Weak Reference, 0.0731165 secs]
    GCParseRule PRECLEAN_REFERENCE_PAR_NEW_REFERENCE = new GCParseRule("PRECLEAN_REFERENCE_PAR_NEW_REFERENCE", PRECLEAN_REFERENCE_RECORDS + GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew" + REFERENCE_RECORDS);

    //(concurrent mode failure)10.103: [SoftReference, 0 refs, 0.0000148 secs]10.103: [WeakReference, 130 refs, 0.0000052 secs]10.103: [FinalReference, 848 refs, 0.0000175 secs]10.103: [PhantomReference, 0 refs, 0 refs, 0.0000029 secs]10.103: [JNI Weak Reference, 0.0000052 secs]: 172902K->6834K(174784K), 0.0106416 secs] 244864K->6834K(253440K), [Metaspace: 11290K->11290K(1058816K)], 0.0116373 secs] [Times: user=0.01 sys=0.01, real=0.01 secs]
    GCParseRule CONCURRENT_MODE_FAILURE_REFERENCE = new GCParseRule("CONCURRENT_MODE_FAILURE_REFERENCE", "^\\(concurrent mode failure\\)" + REFERENCE_RECORDS + ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "\\]");

    /********** Concurrent Mode Failures (CMF) *********/

    //12525.344: [Full GC 12525.344: [ParNew
    GCParseRule FULL_PARNEW_START = new GCParseRule("FULL_PARNEW_START", FULL_GC_PREFIX + TIMESTAMP + "\\[ParNew$");

    //0.645: [Full GC (System) 0.645: [CMS: 1170K->1606K(3328K), 0.0434830 secs] 2474K->1606K(5504K), [CMS Perm : 9606K->9552K(21248K)], 0.0436041 secs] [Times: user=0.04 sys=0.01, real=0.04 secs]
    //51712.306: [Full GC 51712.307: [CMS: 545513K->511751K(1398144K), 1.9291100 secs] 771779K->511751K(2027264K), [CMS Perm : 151637K->151527K(262144K)], 1.9292970 secs]
    GCParseRule CMS_FULL_PERM = new GCParseRule("CMS_FULL_PERM", FULL_GC_PREFIX + CMS_TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME);
    GCParseRule CMS_FULL_META = new GCParseRule("CMS_FULL_META", FULL_GC_PREFIX + CMS_TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + META_RECORD + ", " + PAUSE_TIME);

    //[CMS2016-04-01T13:01:17.871-0700: 3681.337: [SoftReference
    //[JNI Weak Reference, 0.0000240 secs]: 1265971K->546252K(4218880K), 4.1496100 secs]
    //2016-04-01T12:01:14.771-0700: 78.237: [Full GC (System.gc())2016-04-01T12:01:14.772-0700: 78.237: [CMS2016-04-01T12:01:15.156-0700: 78.622: [SoftReference, 0 refs, 0.0001020 secs]2016-04-01T12:01:15.156-0700: 78.622: [WeakReference, 6475 refs, 0.0009080 secs]2016-04-01T12:01:15.157-0700: 78.623: [FinalReference, 14114 refs, 0.0072970 secs]2016-04-01T12:01:15.165-0700: 78.631: [PhantomReference, 48 refs, 0.0000190 secs]2016-04-01T12:01:15.165-0700: 78.631: [JNI Weak Reference, 0.0000390 secs]: 21101K->196164K(4218880K), 1.8134980 secs] 1517335K->196164K(5946048K), [CMS Perm : 128300K->127561K(1048576K)], 1.8143070 secs] [Times: user=1.73 sys=0.09, real=1.81 secs]
    GCParseRule CMS_FULL_PERM_META_REFERENCE = new GCParseRule("CMS_FULL_PERM_META_REFERENCE", FULL_GC_PREFIX + DATE_TIMESTAMP + "\\[CMS(" + DATE_STAMP + ")?" + REFERENCE_RECORDS + ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + " " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME);

    //14485.485: [Full GC (System.gc()) 14485.485: [CMS: 814216K->566360K(915140K), 2.2459649 secs] 990291K->566360K(1264708K), [Metaspace: 56925K->56925K(1101824K)], 2.2462838 secs]
    GCParseRule CMS_FULL_80 = new GCParseRule("CMS_FULL_80", FULL_GC_PREFIX + CMS_TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + META_RECORD + ", " + PAUSE_TIME);

    //1.147: [Full GC (System) 1.147: [Tenured: 1827K->1889K(5312K), 0.0478658 secs] 2441K->1889K(7616K), [Perm : 10118K->10118K(21248K)], 0.0479395 secs]
    GCParseRule SERIAL_FULL = new GCParseRule("SERIAL_FULL", FULL_GC_PREFIX + SERIAL_TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME);

    //1.244: [Full GC (Metadata GC Threshold) 1.244: [Tenured: 11155K->92012K(21888K), 0.0399330 secs] 18738K->09212K(31680K), [Metaspace: 20778K->20778K(1067008K)], 0.0405118 secs] [Times: user=0.04 sys=0.00, real=0.04 secs]
    //GCParseRule SERIAL_FULL80 = new GCParseRule("SERIAL_FULL80", FULL_GC_PREFIX + SERIAL_TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + META_RECORD + ", " + PAUSE_TIME);

    //11.675: [GC 11.675: [ParNew: 3782K->402K(3904K), 0.0012156 secs]11.676: [Tenured: 8673K->6751K(8840K), 0.1268332 secs] 12373K->6751K(12744K), [Perm : 10729K->10675K(21248K)], 0.1281985 secs]
    //89.260: [GC 89.260: [ParNew: 19135K->19135K(19136K), 0.0000156 secs]89.260: [CMS: 105875K->107775K(107776K), 0.5703972 secs] 125011K->116886K(126912K), [CMS Perm : 15589K->15584K(28412K)], 0.5705219 secs]
    GCParseRule PARNEW_TO_CMF_PERM = new GCParseRule("PARNEW_TO_CMF_PERM", GC_PREFIX + PARNEW_BLOCK + CMS_TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "\\]");
    GCParseRule PARNEW_TO_CMF_META = new GCParseRule("PARNEW_TO_CMF_META", GC_PREFIX + PARNEW_BLOCK + CMS_TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + META_RECORD + ", " + PAUSE_TIME + "\\]");

    //5273.802: [GC 5273.803: [ParNew (promotion failed)
    GCParseRule PARNEW_PROMOTION_FAILED_TENURING = new GCParseRule("PARNEW_PROMOTION_FAILED_TENURING", GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew \\(promotion failed\\)$");
    //ParNew reverts to concurrent-mode-failure
    //: 17395K->17756K(18624K), 0.0027477 secs]15.601: [CMS15.602: [CMS-concurrent-abortable-preclean: 0.026/0.772 secs]
    //: 1090560K->1061366K(1090560K), 0.4291130 secs]2014-09-20T16:11:40.580+0200: 196169.739: [CMS2014-09-20T16:11:40.668+0200: 196169.827: [CMS-concurrent-abortable-preclean: 0.210/0.650 secs] [Times: user=2.21 sys=0.04, real=0.64 secs]
    GCParseRule PARNEW_DETAILS_PROMOTION_FAILED_WITH_CMS_PHASE = new GCParseRule("PARNEW_DETAILS_PROMOTION_FAILED_WITH_CMS_PHASE", "^: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + DATE_TIMESTAMP + "\\[CMS" + CMS_PHASE_END);

    //: 19134K->19136K(19136K), 0.0493809 secs]236.955: [CMS: 107351K->79265K(107776K), 0.2540576 secs] 119733K->79265K(126912K), [CMS Perm : 14256K->14256K(24092K)], 0.3036551 secs]
    //: 2368K->319K(2368K), 0.0063634 secs]5.353: [Tenured: 5443K->5196K(5504K), 0.0830293 secs] 7325K->5196K(7872K), [Perm : 10606K->10606K(21248K)], 0.0895957 secs]
    //: 1069879K->1069879K(1090560K), 0.3135220 secs]2014-09-19T06:07:23.135+0200: 73512.294: [CMS: 1613084K->823344K(2423488K), 3.5186340 secs] 2639961K->823344K(3514048K), [CMS Perm : 205976K->205949K(343356K)], 3.8323790 secs] [Times: user=4.44 sys=0.00, real=3.83 secs]
    GCParseRule PARNEW_DETAILS_WITH_CONCURRENT_MODE_FAILURE = new GCParseRule("PARNEW_DETAILS_WITH_CONCURRENT_MODE_FAILURE", "^: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + CMS_TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "\\]");
    //2015-02-03T23:25:28.669-0500: 134188.001: [GC 134188.001: [ParNew (promotion failed): 912205K->890224K(914240K), 0.9060580 secs] 1931465K->1968577K(1995584K), 0.9065480 secs
    GCParseRule PARNEW_PROMOTION_FAILED = new GCParseRule("PARNEW_PROMOTION_FAILED", GC_PREFIX + PARNEW_PROMOTION_FAILED_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE);

    //2292867K->2293758K(2293760K), 52.5725410 secs] 3013763K->2306588K(3014656K), [CMS Perm : 257812K->257481K(393216K)], 52.5751269 secs] [Times: user=52.82 sys=0.26, real=52.58 secs]
    GCParseRule PARNEW_TO_CONCURRENT_MODE_FAILURE = new GCParseRule("PARNEW_TO_CONCURRENT_MODE_FAILURE", "^: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + " " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "\\]");

    //461816.912: [Full GC 461816.912: [ParNew: 281468K->24344K(471872K), 0.1541117 secs]461817.066: [CMS461821.828: [CMS-concurrent-mark: 11.038/11.203 secs]
    GCParseRule PARNEW_TO_FULL_WITH_CMS_PHASE = new GCParseRule("PARNEW_TO_FULL_WITH_CMS_PHASE", FULL_GC_PREFIX + PARNEW_BLOCK + TIMESTAMP + "\\[CMS" + CMS_PHASE_END);

    //2019-11-14T15:45:27.035+0000: 62231.167: [CMS2019-11-14T15:45:27.379+0000: 62231.511: [CMS-concurrent-preclean: 1.107/1.119 secs] [Times: user=4.99 sys=0.86, real=1.11 secs]

    //888588.899: [GC 888588.899: [ParNew (promotion failed): 440811K->440811K(471872K), 0.4821157 secs]888589.382: [CMS (concurrent mode failure): 1025181K->1021778K(1048576K), 32.3456115 secs] 1465076K->1021778K(1520448K), 32.8286179 secs]
    GCParseRule PARNEW_PROMOTION_FAILED_CMF = new GCParseRule("PARNEW_PROMOTION_FAILED_CMF", GC_PREFIX + PARNEW_PROMOTION_FAILED_BLOCK + TIMESTAMP + "\\[CMS \\(concurrent mode failure\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED_PAUSE);
    //893547.479: [GC 893547.479: [ParNew: 419455K->419455K(471872K), 0.0000525 secs]893547.479: [CMS893554.427: [CMS-concurrent-mark: 10.996/11.071 secs] (concurrent mode failure): 1048233K->1043514K(1048576K), 34.5896603 secs] 1467689K->1043514K(1520448K), 34.5903014 secs]
    GCParseRule PARNEW_CMS_PHASE_CMF = new GCParseRule("PARNEW_CMS_PHASE_CMF", GC_PREFIX + PARNEW_BLOCK + TIMESTAMP + "\\[CMS" + CMS_PHASE_END + " \\(concurrent mode failure\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED_PAUSE);

    //14921.473: [GC14921.473: [ParNew (promotion failed): 609300K->606029K(629120K), 0.1794470 secs]14921.652: [CMS: 1224499K->1100531K(1398144K), 3.4546330 secs] 1829953K->1100531K(2027264K), [CMS Perm : 148729K->148710K(262144K)], 3.6342950 secs]
    //84.977: [GC 84.977: [ParNew (promotion failed): 17024K->19136K(19136K), 0.0389515 secs]85.016: [CMS: 107077K->107775K(107776K), 0.2868956 secs] 109258K->107895K(126912K), [CMS Perm : 10680K->10674K(21248K)], 0.3260017 secs] [Times: user=0.37 sys=0.00, real=0.32 secs]
    //85.073: [GC 85.073: [ParNew (promotion failed): 1090560K->1058731K(1090560K), 0.3111070 secs]85.384: [CMS: 1522562K->935965K(2423488K), 3.6633300 secs] 2607389K->935965K(3514048K), [CMS Perm : 221236K->221171K(368452K)], 3.9746870 secs] [Times: user=4.24 sys=0.00, real=3.98 secs]
    GCParseRule PROMOTION_FAILED_TO_FULL = new GCParseRule("PROMOTION_FAILED_TO_FULL", GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew \\(promotion failed\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + CMS_TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "\\]");
    //7191.408: [GC 7191.408: [ParNew (promotion failed): 153344K->153344K(153344K), 0.2537020 secs]7191.662: [CMS7193.099: [CMS-concurrent-mark: 2.857/9.420 secs]
    GCParseRule PARNEW_PROMOTION_FAILED_IN_CMS_PHASE = new GCParseRule("PARNEW_PROMOTION_FAILED_IN_CMS_PHASE", GC_PREFIX + TIMESTAMP + "\\[ParNew \\(promotion failed\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + TIMESTAMP + "\\[CMS" + CMS_PHASE_END);
    //57037.295: [ParNew: 1966080K->1966080K(1966080K), 0.0000438 secs]2019-11-14T14:18:53.163+0000: 57037.295: [CMS2019-11-14T14:18:53.236+0000: 57037.369: [CMS-concurrent-preclean: 0.155/0.178 secs] [Times: user=1.43 sys=0.17, real=0.18 secs]
    GCParseRule SPLIT_PARNEW_PROMOTION_FAILED_IN_CMS_PHASE = new GCParseRule("SPLIT_PARNEW_PROMOTION_FAILED_IN_CMS_PHASE","^" + DATE_TIMESTAMP + "\\[ParNew: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + DATE_TIMESTAMP + "\\[CMS" + CMS_PHASE_END);

    //185853.508: [GC 185853.508: [ParNew (promotion failed): 153265K->152331K(153344K), 0.1020210 secs]185853.610: [CMS CMS: abort preclean due to time 185853.688: [CMS-concurrent-abortable-preclean: 1.470/5.538 secs]
    //195325.615: [GC 195325.615: [ParNew (promotion failed): 153344K->153146K(153344K), 0.1193410 secs]195325.734: [CMS CMS: abort preclean due to time 195325.811: [CMS-concurrent-abortable-preclean: 3.142/5.277 secs]
    GCParseRule PARNEW_PROMOTION_FAILED_TIME_ABORT_PRECLEAN = new GCParseRule("PARNEW_PROMOTION_FAILED_TIME_ABORT_PRECLEAN",
            GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew \\(promotion failed\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + DATE_TIMESTAMP + "\\[CMS " + ABORT_PRECLEAN_DUE_TO_TIME_BLOCK);

    //: 196768K->18097K(471872K), 0.1321291 secs]198764.460: [CMS (concurrent mode failure): 473195K->376198K(1048576K), 25.1817732 secs] 668966K->376198K(1520448K), [CMS Perm : 131008K->27169K(131072K)], 25.3146647 secs]
    //: 439257K->439257K(471872K), 0.5857972 secs]898606.202: [CMS (concurrent mode failure): 1040282K->1048575K(1048576K), 28.6064288 secs] 1478173K->1055740K(1520448K), 29.1931179 secs]
    //: 189422K->15827K(471872K), 0.1282129 secs]131348.814: [CMS (concurrent mode failure): 927657K->279012K(1572864K), 23.8067389 secs] 1116483K->279012K(2044736K), [CMS Perm : 131071K->27250K(131072K)], 23.9363976 secs]
    String PARNEW_CONCURRENT_MODE_FAILURE_DETAILS_PERM = ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + DATE_TIMESTAMP + "\\[CMS \\(concurrent mode failure\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", (?:" + PERM_RECORD + ", )?" + PAUSE_TIME;
    String PARNEW_CONCURRENT_MODE_FAILURE_DETAILS_META = ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + DATE_TIMESTAMP + "\\[CMS \\(concurrent mode failure\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", (?:" + META_RECORD + ", )?" + PAUSE_TIME;
    GCParseRule PARNEW_DETAILS_CONCURRENT_MODE_FAILURE_PERM = new GCParseRule("PARNEW_DETAILS_CONCURRENT_MODE_FAILURE_PERM", "^" + PARNEW_CONCURRENT_MODE_FAILURE_DETAILS_PERM);
    GCParseRule PARNEW_DETAILS_CONCURRENT_MODE_FAILURE_META = new GCParseRule("PARNEW_DETAILS_CONCURRENT_MODE_FAILURE_META", "^" + PARNEW_CONCURRENT_MODE_FAILURE_DETAILS_META);

    // ParNew with tenuring, no concurrent-mode-failure
    //50.306: [GC (Allocation Failure)50.306: [ParNew:
    //25.172: [GC25.172: [ParNew25.195: [CMS-concurrent-abortable-preclean: 0.325/0.557 secs] [Times: user=0.92 sys=0.00, real=0.56 secs]
    //37.843: [GC 37.843: [ParNew37.843: [CMS-concurrent-abortable-preclean: 0.164/0.661 secs]
    //2015-03-20T03:11:52.110+0100: 39.270: [GC (Allocation Failure) 39.271: [ParNew2015-03-20T03:11:52.210+0100: 39.370: [CMS-concurrent-abortable-preclean: 3.048/3.650 secs] [Times: user=6.67 sys=0.52, real=3.65 secs]
    GCParseRule CORRUPTED_PARNEW_CONCURRENT_PHASE = new GCParseRule("CORRUPTED_PARNEW_CONCURRENT_PHASE", GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew" + CMS_PHASE_END);
    //(promotion failed): 118016K->118016K(118016K), 0.0288030 secs]17740.440: [CMS (concurrent mode failure): 914159K->311550K(917504K), 0.5495730 secs] 985384K->311550K(1035520K), [CMS Perm : 65977K->65950K(131072K)], 0.5785090 secs] [Times: user=0.67 sys=0.01, real=0.58 secs]
    GCParseRule CORRUPTED_PARNEW_BODY = new GCParseRule("CORRUPTED_PARNEW_BODY", "\\(promotion failed\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + DATE_TIMESTAMP + "\\[CMS \\(concurrent mode failure\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME);

    //"32.963: [Full GC (System) 32810.963: [CMS2015-08-07T00:28:24.851+0200: 32811.604: [CMS-concurrent-mark: 0.638/9713.769 secs] [Times: user=342.99 sys=126.60, real=9713.77 secs]"
    //49.191: [Full GC 49.191: [CMS49.230: [CMS-concurrent-preclean: 0.353/1.463 secs]
    GCParseRule FULL_GC_INTERRUPTS_CONCURRENT_PHASE = new GCParseRule("FULL_GC_INTERRUPTS_CONCURRENT_PHASE", FULL_GC_PREFIX + DATE_TIMESTAMP + "\\[CMS" + CMS_PHASE_END);

    // (concurrent mode failure): 62354K->8302K(64768K), 0.0931888 secs] 79477K->8302K(83392K), [CMS Perm : 10698K->10698K(21248K)], 0.0956950 secs] [Times: user=0.09 sys=0.00, real=0.09 secs]
    //(concurrent mode failure): 1044403K->1048509K(1048576K), 26.3929433 secs] 1478365K->1059706K(1520448K), 26.7743013 secs]
    // (concurrent mode failure): 62169K->8780K(64768K), 0.0909138 secs] 79462K->8780K(83392K), [CMS Perm : 10688K->10687K(21248K)], 0.0938215 secs]
    GCParseRule CONCURRENT_MODE_FAILURE_DETAILS = new GCParseRule("CONCURRENT_MODE_FAILURE_DETAILS", "^\\s*\\(concurrent mode (?:failure|interrupted)\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + "(?:, " + PERM_RECORD + ")?, " + PAUSE_TIME + "\\]");
    GCParseRule CONCURRENT_MODE_FAILURE_DETAILS_META = new GCParseRule("CONCURRENT_MODE_FAILURE_DETAILS_META", "^\\s*\\(concurrent mode (?:failure|interrupted)\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + "(?:, " + META_RECORD + ")?, " + PAUSE_TIME + "\\]");

    ////1.147: [Full GC (System) 1.147: [Tenured: 1827K->1889K(5312K), 0.0478658 secs] 2441K->1889K(7616K), [Perm : 10118K->10118K(21248K)], 0.0479395 secs]
    //1.149: [Full GC (System) 1.149: [CMS: 0K->1602K(62656K), 0.0761581 secs] 4659K->1602K(81280K), [CMS Perm : 10118K->10064K(21248K)], 0.0762653 secs] [Times: user=0.06 sys=0.01, real=0.08 secs]
    GCParseRule FULL_GC_CMS = new GCParseRule("FULL_GC_CMS", FULL_GC_PREFIX + PARNEW_BLOCK + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "]");

    //3.529: [GC 3.529: [ParNew: 419456K->419456K(471872K), 0.0000651 secs]3.529: [CMS (concurrent mode failure): 1046568K->1048561K(1048576K), 26.6167509 secs] 1466024K->1048561K(1520448K), 26.6176740 secs]
    GCParseRule PARNEW_CMF = new GCParseRule("PARNEW_CMF", GC_PREFIX + PARNEW_BLOCK + TIMESTAMP + "\\[CMS \\(concurrent mode failure\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED_PAUSE);

    // 2015-02-04T17:36:07.103-0500: 199626.435: [GC 199626.435: [ParNew: 812672K->812672K(914240K), 0.0000400 secs]199626.435: [CMS (concurrent mode failure): 1071394K->1081343K(1081344K), 6.8504740 secs] 1884066K->1092775K(1995584K), [CMS Perm : 99417K->99411K(524288K)], 6.8510440 secs] [Times: user=6.63 sys=0.02, real=6.85 secs]
    // 199626.435: [GC 199626.435: [ParNew: 812672K->812672K(914240K), 0.0000400 secs]199626.435: [CMS (concurrent mode failure): 1071394K->1081343K(1081344K), 6.8504740 secs] 1884066K->1092775K(1995584K), [CMS Perm : 99417K->99411K(524288K)], 6.8510440 secs] [Times: user=6.63 sys=0.02, real=6.85 secs]
    GCParseRule PARNEW_CONCURRENT_MODE_FAILURE_PERM = new GCParseRule("PARNEW_CONCURRENT_MODE_FAILURE_PERM", GC_PREFIX + PARNEW_BLOCK + TIMESTAMP + "\\[CMS \\(concurrent mode failure\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "\\]");
    GCParseRule PARNEW_CONCURRENT_MODE_FAILURE_META = new GCParseRule("PARNEW_CONCURRENT_MODE_FAILURE_META", GC_PREFIX + PARNEW_PROMOTION_FAILED_BLOCK + TIMESTAMP + "\\[CMS: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + META_RECORD + ", " + PAUSE_TIME + "\\]");
    //4.327: [FUll GC 4.328: [ParNew: 196768K->180907K(471872K), 0.1321291 secs]4.460: [CMS (concurrent mode failure): 473195K->376198K(1048576K), 5.1817732 secs] 668966K->376198K(1520448K), [CMS Perm : 13108K->27169K(13172K)], 5.3146647 secs]
    //8.828: [Full GC 8.828: [CMS (concurrent mode failure): 630985K->795001K(6470068K), 0.0895496 secs] 810101K->790051K(8300392K), [CMS Perm : 10696K->10696K(21248K)], 0.0896445 secs]
    GCParseRule FULL_PARNEW_CMF_PERM = new GCParseRule("FULL_PARNEW_CMF_PERM", FULL_GC_PREFIX + "(?:" + PARNEW_BLOCK + ")?" + TIMESTAMP + "\\[CMS \\(concurrent mode failure\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "\\]");
    GCParseRule FULL_PARNEW_CMF_META = new GCParseRule("FULL_PARNEW_CMF_META", FULL_GC_PREFIX + "(?:" + PARNEW_BLOCK + ")?" + TIMESTAMP + "\\[CMS \\(concurrent mode failure\\): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + META_RECORD + ", " + PAUSE_TIME + "\\]");

    //5.711: [Full GC 5.711: [CMS (concurrent mode failure)[YG occupancy: 201329 K (5662336 K)]5.798: [weak refs processing, 0.0000140 secs]: 0K->0K(18874368K), 0.3000260 secs] 201329K->201329K(24536704K), [CMS Perm : 21247K->21247K(21248K)], 0.3002180 secs]
    GCParseRule FULL_GC_CMF = new GCParseRule("FULL_GC_CMF", FULL_GC_PREFIX + TIMESTAMP + "\\[CMS \\(concurrent mode failure\\)" + YOUNG_GEN_BLOCK + WEAK_REF_BLOCK + ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "\\]");

    //2016-04-01T12:01:14.771-0700: 78.237: [Full GC (System.gc())2016-04-01T12:01:14.772-0700: 78.237: [CMS2016-04-01T12:01:15.156-0700: 78.622: [SoftReference, 0 refs, 0.0001020 secs]2016-04-01T12:01:15.156-0700: 78.622: [WeakReference, 6475 refs, 0.0009080 secs]2016-04-01T12:01:15.157-0700: 78.623: [FinalReference, 14114 refs, 0.0072970 secs]2016-04-01T12:01:15.165-0700: 78.631: [PhantomReference, 48 refs, 0.0000190 secs]2016-04-01T12:01:15.165-0700: 78.631: [JNI Weak Reference, 0.0000390 secs]: 21101K->196164K(4218880K), 1.8134980 secs] 1517335K->196164K(5946048K), [CMS Perm : 128300K->127561K(1048576K)], 1.8143070 secs] [Times: user=1.73 sys=0.09, real=1.81 secs]
    //2016-04-01T14:25:57.870-0700: 8761.336: [Full GC (GCLocker Initiated GC)2016-04-01T14:25:57.871-0700: 8761.336: [CMS (concurrent mode failure)2016-04-01T14:25:59.160-0700: 8762.626: [SoftReference, 541 refs, 0.0001800 secs]2016-04-01T14:25:59.160-0700: 8762.626: [WeakReference, 21658 refs, 0.0024960 secs]2016-04-01T14:25:59.163-0700: 8762.628: [FinalReference, 4142 refs, 0.0033170 secs]2016-04-01T14:25:59.166-0700: 8762.632: [PhantomReference, 194 refs, 0.0000550 secs]2016-04-01T14:25:59.166-0700: 8762.632: [JNI Weak Reference, 0.0000220 secs]: 4186495K->665385K(4218880K), 3.8356980 secs] 4448832K->665385K(5946048K), [CMS Perm : 417612K->417612K(1048576K)], 3.8379940 secs] [Times: user=3.84 sys=0.02, real=3.83 secs]
    GCParseRule FULL_GC_REFERENCE_CMF = new GCParseRule("FULL_GC_REFERENCE_CMF", FULL_GC_PREFIX + DATE_TIMESTAMP + "\\[CMS \\(concurrent mode failure\\)" + REFERENCE_RECORDS + ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME);

    /**********
     * Record with debug information
     * *********/

    String CMS_SCANNING = TIMESTAMP + "\\[(?:CMS|Tenured)( \\(concurrent mode failure\\))?Finished (generational|perm) space scanning in " + INTEGER + "th thread: " + PAUSE_TIME;
    //257.311: [GC 257.311: [ParNew (promotion failed): 1887488K->1887488K(1887488K), 20.7438090 secs]26278.055: [CMSFinished generational space scanning in 0th thread: 50.866 sec
    GCParseRule PARNEW_PROMOTION_FAILED_SCANNING = new GCParseRule("PARNEW_PROMOTION_FAILED_SCANNING", GC_PREFIX + PARNEW_PROMOTION_FAILED_BLOCK + CMS_SCANNING);
    //758.603: [GC 758.603: [ParNew: 1887488K->1887488K(1887488K), 0.0000290 secs]758.603: [CMSFinished generational space scanning in 0th thread: 26.749 sec
    GCParseRule PARNEW_SCANNING = new GCParseRule("PARNEW_SCANNING", GC_PREFIX + PARNEW_BLOCK + CMS_SCANNING);
    GCParseRule PARNEW_DETAILS_DEBUG = new GCParseRule("PARNEW_DETAILS_DEBUG", "^: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + CMS_SCANNING);

    //[15 iterations, 12 waits, 1225 cards)] 220824.424: [CMS-concurrent-abortable-preclean: 4.185/5.297 secs] (CMS-concurrent-abortable-preclean yielded 0 times)
    GCParseRule CONCURRENT_PHASE_CARDS = new GCParseRule("CONCURRENT_PHASE_CARDS", "^" + CARD + " " + CMS_PHASE_END + " " + CMS_PHASE_END_YIELD);

    //448293.174: [GC 448293.174: [ParNew [1 iterations, 1 waits, 44 cards)] 448293.190: [CMS-concurrent-abortable-preclean: 0.403/0.504 secs] (CMS-concurrent-abortable-preclean yielded 0 times)
    GCParseRule PARNEW_CONCURRENT_PHASE_CARDS = new GCParseRule("PARNEW_CONCURRENT_PHASE_CARDS", GC_PREFIX + TIMESTAMP + "\\[ParNew " + CARD + " " + CMS_PHASE_END + " " + CMS_PHASE_END_YIELD);

    //CMS: abort preclean due to time  [13 iterations, 9 waits, 17777 cards)] 204737.557: [CMS-concurrent-abortable-preclean: 4.475/5.334 secs] (CMS-concurrent-abortable-preclean yielded 2 times)
    GCParseRule PRECLEAN_TIMED_OUT_WITH_CARDS = new GCParseRule("PRECLEAN_TIMED_OUT_WITH_CARDS", "CMS: abort preclean due to time\\s*" + CARD + " " + CMS_PHASE_END + " " + CMS_PHASE_END_YIELD);

    //196.783: [CMS-concurrent-mark: 3.426/3.796 secs] (CMS-concurrent-mark yielded 128 times)
    GCParseRule CONC_PHASE_YIELDS = new GCParseRule("CONC_PHASE_YIELDS", "^" + CMS_PHASE_END + " " + CMS_PHASE_END_YIELD);

    //1.198: [Full GC 4221.198: [CMSFinished generational space scanning in 1th thread: 22.766 sec
    //5.705: [Full GC 5.706: [CMS (concurrent mode failure)Finished generational space scanning in 0th thread: 0.001 sec
    GCParseRule FULL_GC_CARDS = new GCParseRule("FULL_GC_CARDS", FULL_GC_PREFIX + CMS_SCANNING);

    //[YG occupancy: 134226 K (1887488 K)]5.791: [weak refs processing, 0.0000170 secs]: 0K->0K(16777216K), 0.3209520 secs] 134226K->134226K(18664704K), [CMS Perm : 21247K->21247K(21248K)], 0.3211600 secs]
    GCParseRule FULL_GC_CARDS_DETAILS = new GCParseRule("FULL_GC_CARDS_DETAILS", "^" + YOUNG_GEN_BLOCK + WEAK_REF_BLOCK + ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "\\]");

    //220.511: [Rescan (parallel)  (Survivor:8chunks) Finished young gen rescan work in 0th thread: 0.001 sec
    GCParseRule RESCAN_CARDS = new GCParseRule("RESCAN_CARDS", "^" + DATE_TIMESTAMP + "\\[Rescan \\(parallel\\)\\s*\\(Survivor:" + INTEGER + "chunks\\) Finished young gen rescan work in " + INTEGER + "th thread: " + PAUSE_TIME);

    //, 0.1127040 secs]220.624: [weak refs processing, 0.1513820 secs] [1 CMS-remark: 10541305K(16777216K)] 10742883K(18664704K), 0.7371020 secs]
    GCParseRule RESCAN_CARDS_DETAILS = new GCParseRule("RESCAN_CARDS_DETAILS", "^, " + PAUSE_TIME + "\\]" + WEAK_REF_BLOCK + " " + REMARK_BLOCK);

    //, 0.2020511 secs]34.132: [weak refs processing, 0.0000108 secs]34.132: [class unloading, 0.0026382 secs]34.135: [scrub symbol & string tables, 0.0026847 secs] [1 CMS-remark: 0K(4194304K)] 889188K(5872064K), 0.2081265 secs]
    GCParseRule RESCAN_SPLIT_UNLOADING_STRING = new GCParseRule("RESCAN_SPLIT_UNLOADING_STRING", "^, " + TIMESTAMP + "\\]" + WEAK_REF_BLOCK + CLASS_UNLOADING_BLOCK + STRING_AND_SYMBOL_SCRUB_BLOCK + " " + REMARK_BLOCK);

    //, 1.2157670 secs]42102.265: [weak refs processing, 0.1038920 secs]Work queue overflow (benign) (pmc_rm=1542597, kac=0)
    GCParseRule RESCAN_OVERFLOW_DETAILS = new GCParseRule("RESCAN_OVERFLOW_DETAILS", "^, " + PAUSE_TIME + "\\]" + WEAK_REF_BLOCK + "Work queue overflow");

    //Missed: [1 CMS-remark: 10404644K(16777216K)] 10614308K(18664704K), 2.7717280 secs]
    GCParseRule RESCAN_OVERFLOW_REMARK = new GCParseRule("RESCAN_OVERFLOW_REMARK", "^" + REMARK_BLOCK);

    //CMS marking stack overflow (benign) at 4194304
    GCParseRule STACK_OVERFLOW = new GCParseRule("STACK_OVERFLOW", "CMS marking stack overflow \\(benign\\) at " + COUNTER);

    // unknown debugging flags CMS debug level = 1???
    //ParNew is broken by diagnostic message
    //: 5662336K->629120K(5662336K), 1.9967660 secs] 9893493K->5677322K(24536704K)CMSCollector shouldConcurrentCollect: 536.925
    //, 1.9969670 secs] [Times: ** Need this or the record is too greedy, the time is picked up by the FLS rule
    GCParseRule PARNEW_DEBUG1_DETAILS_NO_PAUSE = new GCParseRule("PARNEW_DEBUG1_DETAILS_NO_PAUSE", "^: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + "CMSCollector shouldConcurrentCollect:");

    GCParseRule PARNEW_FLS_BEFORE = new GCParseRule("PARNEW_FLS_BEFORE", GC_PREFIX + "Before GC:");
    GCParseRule FULLGC_FLS_BEFORE = new GCParseRule("FULLGC_FLS_BEFORE", FULL_GC_PREFIX + "Before GC:");
    GCParseRule PARNEW_FLS_AFTER = new GCParseRule("PARNEW_FLS_AFTER", PARNEW_BLOCK + " " + BEFORE_AFTER_CONFIGURED + "After GC:");
    GCParseRule PARNEW_FLS_BODY = new GCParseRule("PARNEW_FLS_BODY", "^: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + "After GC:");
    //"22134.127: [ParNew (15: promotion failure size = 39716437)  (promotion failed): 7172365K->6836505K(7864320K), 1.7204958 secs]22135.847: [CMSCMS: Large block 0x000000062859cfa0",
    GCParseRule PARNEW_PROMOTION_FAILED_DETAILS = new GCParseRule("PARNEW_PROMOTION_FAILED_DETAILS", DATE_TIMESTAMP + "\\[ParNew " + PROMOTION_FAILURE_SIZE_BLOCK);
    //: 4839952K->3806707K(10485760K), 5.7168591 secs] 11644986K->3806707K(18350080K), [Metaspace: 116615K->116615K(1157120K)]After GC:
    GCParseRule PARNEW_PROMOTION_FAILED_DETAILS_AFTER = new GCParseRule("PARNEW_PROMOTION_FAILED_DETAILS_AFTER", ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + META_RECORD + "After GC:");
    GCParseRule PARNEW_FLS_TIME = new GCParseRule("PARNEW_FLS_TIME", "^, " + PAUSE_TIME + "\\] \\[Times: ");
    GCParseRule FLS_HEADER = new GCParseRule("FLS_HEADER", "^Statistics for BinaryTreeDictionary");
    GCParseRule FLS_SEPARATOR = new GCParseRule("FLS_SEPARATOR", "^------------------------------------");

    GCParseRule FLS_TOTAL_FREE_SPACE = new GCParseRule("FLS_TOTAL_FREE_SPACE", "^Total Free Space: " + COUNTER);
    GCParseRule FLS_MAX_CHUNK_SIZE = new GCParseRule("FLS_MAX_CHUNK_SIZE", "^Max   Chunk Size: " + COUNTER);
    GCParseRule FLS_NUMBER_OF_BLOCKS = new GCParseRule("FLS_NUMBER_OF_BLOCKS", "^Number of Blocks: " + COUNTER);
    GCParseRule FLS_AVERAGE_BLOCK_SIZE = new GCParseRule("FLS_AVERAGE_BLOCK_SIZE", "^Av.  Block  Size: " + COUNTER);
    GCParseRule FLS_TREE_HEIGHT = new GCParseRule("FLS_TREE_HEIGHT", "^Tree      Height: " + COUNTER);
    GCParseRule FLS_LARGE_BLOCK_PROXIMITY = new GCParseRule("FLS_LARGE_BLOCK_PROXIMITY", "^CMS: Large Block: " + MEMORY_ADDRESS + "; Proximity: " + MEMORY_ADDRESS + " -> " + MEMORY_ADDRESS);
    GCParseRule FLS_LARGE_BLOCK = new GCParseRule("FLS_LARGE_BLOCK", "^" + FLS_LARGE_BLOCK_BODY + "$");
    //(concurrent mode failure)CMS: Large block 0x00000007bfffdfd0
    GCParseRule CMF_LARGE_BLOCK = new GCParseRule("CMF_LARGE_BLOCK", "^\\(concurrent mode failure\\)CMS: Large block " + HEX + "$");

    //364.157: [GC 364.157: [ParNew: 235968K->235968K(235968K), 0.0000430 secs]364.157: [CMSbailing out to foreground collection
    GCParseRule CMS_BAILING_TO_FOREGROUND = new GCParseRule("CMS_BAILING_TO_FOREGROUND", GC_PREFIX + PARNEW_BLOCK + TIMESTAMP + "\\[CMSbailing out to foreground collection");
    //58495.730: [Full GC 58495.730: [CMS
    GCParseRule CMF_DIAGNOSTIC_START = new GCParseRule("CMF_DIAGNOSTIC_START", FULL_GC_PREFIX + TIMESTAMP + "\\[CMS$");

    //39262.226: [GC 39262.226: [ParNew: 5662336K->629120K(5662336K), 1.9813420 secs] 11283931K->7025761K(24536704K)CMSCollector shouldConcurrentCollect: 39264.208
    //534.928: [GC 534.928: [ParNew: 5662336K->629120K(5662336K), 1.9967660 secs] 9893493K->5677322K(24536704K)CMSCollector shouldConcurrentCollect: 536.925
    GCParseRule PARNEW_SHOULD_CONCURRENT_COLLECT = new GCParseRule("PARNEW_SHOULD_CONCURRENT_COLLECT", GC_PREFIX + PARNEW_BLOCK + " " + BEFORE_AFTER_CONFIGURED + "CMSCollector shouldConcurrentCollect: " + TIME);

    //81626.911: [GC CMSCollector shouldConcurrentCollect: 81627.388
    GCParseRule SHOULD_CONCURRENT_COLLECT = new GCParseRule("SHOULD_CONCURRENT_COLLECT", GC_PREFIX + "CMSCollector shouldConcurrentCollect: " + TIME);

    //(concurrent mode failure) (concurrent mode failure): 10406836K->10006207K(18874368K), 1.0846480 secs] 16069172K->15668543K(24536704K), [CMS Perm : 145039K->145039K(196608K)], 1.0848820 secs]
    GCParseRule DUP_CMF = new GCParseRule("DUP_CMF", "^\\(concurrent mode failure\\) \\(concurrent mode failure\\)" + ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "\\]");

    //2012-11-20T00:52:22.677+0100: 57.366: [GC[YG occupancy: 879403 K (1677760 K)]57.366: [Rescan (parallel)  (Survivor:19chunks) Finished young gen rescan work in 0th thread: 0.033
    GCParseRule REMARK_SPLIT_BY_DEBUG = new GCParseRule("REMARK_SPLIT_BY_DEBUG", GC_PREFIX + YOUNG_GEN_BLOCK + TIMESTAMP + "\\[Rescan \\(parallel\\)  \\(Survivor:(\\d+)chunks\\) Finished young gen rescan work in (\\d+)th thread: " + REAL_NUMBER);

    //Scavange before remark. Just record the ParNew.
    //11.960: [GC[YG occupancy: 1308733 K (1887488 K)]11.960: [GC 11.960: [ParNew: 1308733K->28278K(1887488K), 0.0196770 secs] 1308733K->28278K(20761856K), 0.0198110 secs] [Times: user=0.23 sys=0.01, real=0.02 secs]
    //203235.155: [GC[YG occupancy: 49482 K (720896 K)]203235.157: [GC 203235.157: [ParNew
    GCParseRule SCAVENGE_BEFORE_REMARK = new GCParseRule("SCAVENGE_BEFORE_REMARK", GC_PREFIX + YOUNG_GEN_BLOCK + GC_PREFIX + PARNEW_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE);
    GCParseRule SCAVENGE_BEFORE_REMARK_TENURING = new GCParseRule("SCAVENGE_BEFORE_REMARK_TENURING", GC_PREFIX + YOUNG_GEN_BLOCK + GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew$");
    //2016-03-22T13:57:02.971-0400: 19.703: [GC (CMS Final Remark) [YG occupancy: 613960 K (996800 K)]
    // 2016-03-22T13:57:02.971-0400: 19.703: [GC (CMS Final Remark) 19.703: [ParNew
    // 19.875: [SoftReference, 0 refs, 0.0001882 secs]19.875: [WeakReference, 228 refs, 0.0001731 secs]19.875: [FinalReference, 17964 refs, 0.0210014 secs]
    // 19.896: [PhantomReference, 0 refs, 0.0000989 secs]19.896: [JNI Weak Reference, 0.0000805 secs]
    //GCParseRule PARNEW = new GCParseRule("PARNEW", "^(" + GC_PREFIX + ")?" + PARNEW_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");
    GCParseRule SCAVENGE_BEFORE_REMARK_REFERENCE_SPLIT = new GCParseRule("SCAVENGE_BEFORE_REMARK_REFERENCE_SPLIT", GC_PREFIX + YOUNG_GEN_BLOCK + GC_PREFIX + DATE_TIMESTAMP + "\\[ParNew" + REFERENCE_RECORDS + "$");

    //2016-03-22T10:02:55.716-0100: 27.150: [GC (CMS Final Remark) [YG occupancy: 55606 K (78656 K)]2016-03-22T10:02:55.716-0100: 27.150: [GC (CMS Final Remark) 2016-03-22T10:02:55.716-0100: 27.150: [ParNew2016-03-22T10:02:55.730-0100: 27.165: [SoftReference, 0 refs, 0.0000269 secs]2016-03-22T10:02:55.730-0100: 27.165: [WeakReference, 0 refs, 0.0000045 secs]2016-03-22T10:02:55.730-0100: 27.165: [FinalReference, 3 refs, 0.0000073 secs]2016-03-22T10:02:55.730-0100: 27.165: [PhantomReference, 0 refs, 0 refs, 0.0000045 secs]2016-03-22T10:02:55.730-0100: 27.165: [JNI Weak Reference, 0.0000035 secs]: 55606K->8704K(78656K), 0.0145788 secs] 143782K->100061K(253440K), 0.0145973 secs] [Times: user=0.10 sys=0.00, real=0.02 secs]
    GCParseRule SCAVENGE_BEFORE_REMARK_REFERENCE = new GCParseRule("SCAVENGE_BEFORE_REMARK_REFERENCE", GC_PREFIX + YOUNG_GEN_BLOCK + GC_PREFIX + " " + DATE_TIMESTAMP + "\\[ParNew" + REFERENCE_RECORDS + ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + CPU_SUMMARY);

    //: 2368K->319K(2368K), 0.0063634 secs]5.353: [Tenured: 5443K->5196K(5504K), 0.0830293 secs] 7325K->5196K(7872K), [Perm : 10606K->10606K(21248K)], 0.0895957 secs]

    //scavange before remark with promotion failure. Just produce a PromotionFailed event.
    //2833.756: [GC[YG occupancy: 1600484 K (1887488 K)]2833.756: [GC 2833.757: [ParNew (promotion failed): 1600484K->1600484K(1887488K), 2.2601630 secs] 19739658K->20165811K(20761856K), 2.2605170 secs]
    GCParseRule REMARK_PARNEW_PROMOTION_FAILED = new GCParseRule("REMARK_PARNEW_PROMOTION_FAILED", GC_PREFIX + YOUNG_GEN_BLOCK + DATE_TIMESTAMP + "\\[GC " + DATE_TIMESTAMP + "\\[ParNew ?(\\(promotion failed\\)): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED_PAUSE);

    //13.077: [GC[YG occupancy: 3081 K (18624 K)]13.077: [Rescan (parallel) , 0.0009121 secs]13.078: [weak refs processing, 0.0000365 secs] [1 CMS-remark: 35949K(62656K)] 39030K(81280K), 0.0010300 secs]
    GCParseRule SPLIT_REMARK = new GCParseRule("SPLIT_REMARK", GC_PREFIX + YOUNG_GEN_BLOCK + DATE_TIMESTAMP + "\\[GC " + TIMESTAMP + "\\[ParNew");

    //19.293: [GC (CMS Final Remark) [YG occupancy: 4110424 K (4456448 K)]{Heap before GC invocations=0 (full 1):
    //19.293: [GC (CMS Final Remark) 19.293: [ParNew: 4110424K->60709K(4456448K), 0.0420987 secs] 4110424K->60709K(7733248K), 0.0422135 secs] [Times: user=0.52 sys=0.12, real=0.04 secs]
    //19.335: [Rescan (parallel) , 0.0050917 secs]19.340: [weak refs processing, 0.0000278 secs]19.340: [class unloading, 0.0233772 secs]19.364: [scrub symbol table, 0.0055223 secs]19.369: [scrub string table, 0.0009215 secs][1 CMS-remark: 0K(3276800K)] 60709K(7733248K), 0.0798048 secs] [Times: user=0.69 sys=0.12, real=0.08 secs]
    GCParseRule SCAVENGE_BEFORE_REMARK_PRINT_HEAP_AT_GC = new GCParseRule("SCAVENGE_BEFORE_REMARK_PRINT_HEAP_AT_GC", GC_PREFIX + " " + YOUNG_GEN_BLOCK + "\\{Heap before GC invocations=");

    //8239.784: [Rescan (parallel) , 0.0444432 secs]8239.828: [weak refs processing8239.828: [SoftReference, 0 refs, 0.0000687 secs]8239.828: [WeakReference, 0 refs, 0.0000638 secs]8239.829: [FinalReference, 556 refs, 0.0008823 secs]8239.829: [PhantomReference, 0 refs, 0.0000657 secs]8239.830: [JNI Weak Reference, 0.0000995 secs], 0.0013332 secs]8239.830: [class unloading, 0.1926177 secs]8240.022: [scrub symbol table, 0.0376581 secs]8240.060: [scrub string table, 0.6722322 secs][1 CMS-remark: 6705100K(12523968K)] 6815820K(13520768K), 1.8735975 secs] [Times: user=7.84 sys=0.08, real=1.87 secs]
    GCParseRule SPLIT_REMARK_REFERENCE_BUG = new GCParseRule("SPLIT_REMARK_REFERENCE_BUG", "^" + DATE_TIMESTAMP + "\\[Rescan \\(parallel\\) , " + PAUSE_TIME + "\\]" + DATE_TIMESTAMP + "\\[weak refs processing" + DATE_TIMESTAMP);
    GCParseRule SPLIT_REMARK_REFERENCE = new GCParseRule("SPLIT_REMARK_REFERENCE", "^" + RESCAN_BLOCK + WEAK_REF_BLOCK + CLASS_UNLOADING_BLOCK + SYMBOL_TABLE_SCRUB_BLOCK + STRING_TABLE_SCRUB_BLOCK + REMARK_BLOCK);
    GCParseRule FULL_SPLIT_BY_CONCURRENT_PHASE = new GCParseRule("FULL_SPLIT_BY_CONCURRENT_PHASE", "^" + DATE_TIMESTAMP + "\\[CMS" + CMS_PHASE_END);

}

