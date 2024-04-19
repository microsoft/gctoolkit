// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

/**
 * Supported flags
 * -XX:+PrintGCDateStamps
 * -XX:+PrintGCDetails
 * -XX:+PrintTenuringDistribution
 * -XX:+PrintAdaptiveSizePolicy
 * -XX:+PrintReferenceGC
 * -XX:+G1SummarizeRSetStats -XX:+G1SumerizeRsetStatsPeriod=1
 */

public interface G1GCPatterns extends G1GCTokens {

    String FINALIZE_MARKING = DATE_TIMESTAMP + "\\[Finalize Marking, " + PAUSE_TIME + "\\]";
    String REF_PROC = DATE_TIMESTAMP + "\\[GC ref-proc, " + PAUSE_TIME + "\\]";
    String REF_PROC_DETAILS = DATE_TIMESTAMP + "\\[GC ref-proc" + REFERENCE_RECORDS + ", " + PAUSE_TIME + "\\]";
    String UNLOADING = DATE_TIMESTAMP + "\\[Unloading, " + PAUSE_TIME + "\\]";
    String TO_SPACE_OVERFLOW = "( \\(to-space (exhausted|overflow)\\))?";
    String FIXUP_STATS = "Min: " + REAL_VALUE + ", Avg: " + REAL_VALUE + ", Max: " + REAL_VALUE + ", Diff: " + REAL_VALUE + ", Sum: " + REAL_VALUE;
    String YOUNG_MIXED_INITIAL_MARK_BLOCK = "\\((young|mixed)\\)( \\((initial-mark)\\))?( \\(to-space (exhausted|overflow)\\))?\\s*";



    /*  ***********
     *  Rules with no details
     */

    //369310.802: [GC pause (young) 485M->240M(512M), 0.0558340 secs]
    //369447.597: [GC pause (young) (initial-mark) 485M->239M(512M), 0.0719290 secs]
    //369674.919: [GC pause (mixed) 482M->185M(512M), 0.0679470 secs]
    //0.583: [GC pause (G1 Evacuation Pause) (young) 24M->4561K(256M), 0.0047007 secs]
    GCParseRule YOUNG = new GCParseRule("YOUNG", G1GC_PREFIX + YOUNG_MIXED_INITIAL_MARK_BLOCK + BEFORE_AFTER_CONFIGURED_PAUSE);

    //"16.603: [Full GC (System.gc())  14M->3334K(11M), 0.0230975 secs]
    GCParseRule SIMPLE_FULL = new GCParseRule("SIMPLE_FULL", G1GC_PREFIX);

    //2014-02-22T10:49:26.487-0100: 7.477: [GC pause (G1 Evacuation Pause) (young) (initial-mark), 0.0042120 secs]
    //2014-02-22T10:49:26.501-0100: 7.490: [GC pause (G1 Evacuation Pause) (young), 0.0025420 secs]
    //2014-02-22T10:49:26.508-0100: 7.498: [GC pause (G1 Evacuation Pause) (mixed), 0.0026410 secs]
    //26.893: [GC pause (G1 Evacuation Pause) (young) (to-space exhausted), 0.1709670 secs]
    GCParseRule G1_DETAILS = new GCParseRule("G1_DETAILS", G1GC_PREFIX + YOUNG_MIXED_INITIAL_MARK_BLOCK + ", " + PAUSE_TIME);
    GCParseRule G1_YOUNG_SPLIT_START = new GCParseRule("G1_YOUNG_SPLIT_START", G1GC_PREFIX + "(\\(young\\)|\\(mixed\\))$");
    GCParseRule G1_YOUNG_SPLIT_END = new GCParseRule("G1_YOUNG_SPLIT_END", "^" + "(\\(to-space (exhausted|overflow)\\))?, " + PAUSE_TIME + "\\]");

    GCParseRule G1_INITIAL_MARK = new GCParseRule("G1_INITIAL_MARK", G1GC_PREFIX + "\\((young|mixed)\\) \\(initial-mark\\)$");

    //****** Records for PrintReferenceGC
    //2015-09-10T08:07:15.806+0200: 0.470: [SoftReference, 0 refs, 0.0005710 secs]2015-09-10T08:07:15.806+0200: 0.471: [WeakReference, 4 refs, 0.0002995 secs]2015-09-10T08:07:15.806+0200: 0.471: [FinalReference, 846 refs, 0.0011795 secs]2015-09-10T08:07:15.808+0200: 0.472: [PhantomReference, 0 refs, 0 refs, 0.0005331 secs]2015-09-10T08:07:15.808+0200: 0.473: [JNI Weak Reference, 0.0000145 secs], 0.0055187 secs]
    //13925.724: [SoftReference, 0 refs, 0.0000060 secs]13925.724: [WeakReference, 367 refs, 0.0001090 secs]13925.724: [FinalReference, 3077 refs, 0.0116620 secs]13925.736: [PhantomReference, 8 refs, 0.0000040 secs]13925.736: [JNI Weak Reference, 0.0001390 secs] (to-space overflow), 4.63695400 secs]
    GCParseRule FREE_FLOATING_REFERENCE_RECORDS = new GCParseRule("FREE_FLOATING_REFERENCE_RECORDS", "^" + REFERENCE_RECORDS + "( \\(to-space (exhausted|overflow)\\))?, " + PAUSE_TIME + "\\]");
    //Missed: 2015-09-10T10:47:58.284+0200: 9642.949: [SoftReference, 0 refs, 0.0004200 secs]2015-09-10T10:47:58.284+0200: 9642.949: [WeakReference, 2 refs, 0.0002457 secs]2015-09-10T10:47:58.285+0200: 9642.949: [FinalReference, 1245 refs, 0.0004931 secs]2015-09-10T10:47:58.285+0200: 9642.950: [PhantomReference, 0 refs, 0 refs, 0.0004229 secs]2015-09-10T10:47:58.286+0200: 9642.950: [JNI Weak Reference, 0.0000150 secs] 9642.951: [G1Ergonomics (Concurrent Cycles) do not request concurrent cycle initiation, reason: still doing mixed collections, occupancy: 805306368 bytes, allocation request: 0 bytes, threshold: 805306350 bytes (75.00 %), source: end of GC]
    GCParseRule FLOATING_REFERENCE_WITH_ADAPTIVE_SIZING = new GCParseRule("FLOATING_REFERENCE_WITH_ADAPTIVE_SIZING", REFERENCE_RECORDS + " \\d+\\.\\d{3}: \\[G1Ergonomics");

    //998.590: [GC pause (mixed)12038.693: [SoftReference, 0 refs, 0.0000070 secs]12038.693: [WeakReference, 3 refs, 0.0000060 secs]12038.693: [FinalReference, 0 refs, 0.0000010 secs]12038.693: [PhantomReference, 1 refs, 0.0000020 secs]12038.693: [JNI Weak Reference, 0.0002030 secs] (to-space overflow), 41.52888100 secs]
    //300.634: [GC pause (young) (initial-mark)300.742: [SoftReference, 0 refs, 0.0000230 secs]300.742: [WeakReference, 0 refs, 0.0000030 secs]300.742: [FinalReference, 0 refs, 0.0000030 secs]300.742: [PhantomReference, 0 refs, 0.0000020 secs]300.742: [JNI Weak Reference, 0.0000020 secs] (to-space exhausted), 0.1146540 secs]
    GCParseRule G1_DETAILS_REFERENCE_GC = new GCParseRule("G1_DETAILS_REFERENCE_GC", G1GC_PREFIX + "\\((young||mixed)\\)( \\(initial-mark\\))?" + REFERENCE_RECORDS + TO_SPACE_OVERFLOW + ", " + PAUSE_TIME + "\\]");
    //2017-10-26T20:05:18.850+0000: 1.786: [SoftReference, 0 refs, 0.0022943 secs]2017-10-26T20:05:18.852+0000: 1.789: [WeakReference, 8 refs, 0.0026570 secs]2017-10-26T20:05:18.855+0000: 1.791: [FinalReference, 961 refs, 0.0033445 secs]2017-10-26T20:05:18.858+0000: 1.795: [PhantomReference, 0 refs, 0 refs, 0.0048559 secs]2017-10-26T20:05:18.863+0000: 1.799: [JNI Weak Reference, 0.0000274 secs] (plab_sz = 17978  desired_plab_sz = 17978)  (plab_sz = 0  desired_plab_sz = 259) , 0.0241882 secs]
    GCParseRule G1_FLOATING_REFERENCE_PLAB = new GCParseRule("G1_FLOATING_REFERENCE_PLAB", REFERENCE_RECORDS + " " + PLAB_RECORD);

    //2014-10-21T11:47:46.879-0500: 11971.476: [GC pause (young)11972.105: [SoftReference, 0 refs, 0.0000050 secs]11972.105: [WeakReference, 1 refs, 0.0000020 secs]11972.105: [FinalReference, 94 refs, 0.0000360 secs]11972.105: [PhantomReference, 5 refs, 0.0000030 secs]11972.105: [JNI Weak Reference, 0.0002010 secs] (initial-mark), 0.63448400 secs]
    GCParseRule G1_DETAILS_REFERENCE_INITIAL_MARK = new GCParseRule("G1_DETAILS_REFERENCE_INITIAL_MARK", G1GC_PREFIX + "\\(young\\)" + REFERENCE_RECORDS + TO_SPACE_OVERFLOW + " \\(initial-mark\\), " + PAUSE_TIME + "\\]");

    //2014-10-21T11:47:52.016-0500: 11976.613: [GC remark 11976.615: [GC ref-proc11976.615: [SoftReference, 834 refs, 0.0002100 secs]11976.615: [WeakReference, 14842 refs, 0.0021790 secs]11976.617: [FinalReference, 181 refs, 0.0004430 secs]11976.618: [PhantomReference, 475 refs, 0.0001160 secs]11976.618: [JNI Weak Reference, 0.0002910 secs], 0.0035310 secs], 0.0565900 secs]
    //46.465: [GC remark 46.465: [GC ref-proc46.465: [SoftReference, 0 refs, 0.0000180 secs]46.465: [WeakReference, 15 refs, 0.0000240 secs]46.465: [FinalReference, 0 refs, 0.0000110 secs]46.465: [PhantomReference, 0 refs, 0.0000110 secs]46.465: [JNI Weak Reference, 0.0000050 secs], 0.0000960 secs], 0.0012290 secs]
    GCParseRule G1_REMARK_REFERENCE_GC = new GCParseRule("G1_REMARK_REFERENCE_GC", DATE_TIMESTAMP + "\\[GC remark " + DATE_TIMESTAMP + "\\[GC ref-proc" + REFERENCE_RECORDS + ", " + PAUSE_TIME);

    //0.844: [Full GC0.855: [SoftReference, 0 refs, 0.0000230 secs]0.855: [WeakReference, 183 refs, 0.0000090 secs]0.855: [FinalReference, 1110 refs, 0.0002100 secs]0.855: [PhantomReference, 1 refs, 0.0000030 secs]0.855: [JNI Weak Reference, 0.0000090 secs] 17M->3756K(13M), 0.0236280 secs]
    //2014-10-21T11:50:54.618-0500: 12159.216: [Full GC12160.052: [SoftReference, 1453 refs, 0.0002060 secs]12160.052: [WeakReference, 24676 refs, 0.0020110 secs]12160.054: [FinalReference, 751 refs, 0.0002570 secs]12160.054: [PhantomReference, 514 refs, 0.0000650 secs]12160.054: [JNI Weak Reference, 0.0001910 secs] 11414M->1104M(12000M), 4.6872730 secs]
    GCParseRule G1_FULL_DETAILS_REFERENCE_GC = new GCParseRule("G1_FULL_DETAILS_REFERENCE_GC", FULL_GC_PREFIX + REFERENCE_RECORDS + " " + BEFORE_AFTER_CONFIGURED_PAUSE);

    //2014-10-22T07:40:16.844-0500: 83521.441: [Full GC83522.569: [SoftReference, 1653 refs, 0.0003300 secs]83522.569: [WeakReference, 36226 refs, 0.0042550 secs]83522.573: [FinalReference, 1826 refs, 0.0006410 secs]83522.574: [PhantomReference, 560 refs, 0.0000630 secs]83522.574: [JNI Weak Reference, 0.0001650 secs]2014-10-22T07:40:21.212-0500: 83525.810: [GC concurrent-mark-end, 2.9456090 sec]
    GCParseRule G1_FULL_INTERRUPTS_CONCURRENT_WITH_REFERENCES = new GCParseRule("G1_FULL_INTERRUPTS_CONCURRENT_WITH_REFERENCES", FULL_GC_PREFIX + REFERENCE_RECORDS + GC_PREFIX + "concurrent-(.+)-end, " + PAUSE_TIME);
    // 11593M->1413M(12000M), 5.6594060 secs]
    GCParseRule G1_FULL_MEMORY_SPLIT_BY_CONCURRENT = new GCParseRule("G1_FULL_MEMORY_SPLIT_BY_CONCURRENT", "^" + BEFORE_AFTER_CONFIGURED_PAUSE);

    //2015-04-09T14:28:44.235+0100: 6.597: [GC remark 6.597: [Finalize Marking, 0.0091510 secs] 6.606: [GC ref-proc, 0.0014102 secs] 6.608: [Unloading, 0.0044869 secs], 0.0153351 secs]
    GCParseRule G1_180_REMARK = new GCParseRule("G1_180_REMARK", DATE_TIMESTAMP + "\\[GC remark " + FINALIZE_MARKING + " " + REF_PROC + " " + UNLOADING + ", " + PAUSE_TIME + "\\]");
    //2015-12-21T10:26:34.913-0500: 35835.169: [GC remark 35835.169: [Finalize Marking, 0.8985905 secs] 35836.068: [GC ref-proc35836.068: [SoftReference, 74 refs, 0.0002156 secs]35836.068: [WeakReference, 243 refs, 0.0001747 secs]35836.068: [FinalReference, 4154 refs, 0.0012360 secs]35836.070: [PhantomReference, 0 refs, 0 refs, 0.0002882 secs]35836.070: [JNI Weak Reference, 0.0000227 secs], 0.0026411 secs] 35836.071: [Unloading, 0.0048615 secs], 0.9142368 secs]
    GCParseRule G1_180_REMARK_REF_DETAILS = new GCParseRule("G1_180_REMARK_REF_DETAILS", DATE_TIMESTAMP + "\\[GC remark " + FINALIZE_MARKING + " " + REF_PROC_DETAILS + " " + UNLOADING + ", " + PAUSE_TIME + "\\]");

    GCParseRule PROCESSED_BUFFERS = new GCParseRule("PROCESSED_BUFFERS", "\\[Processed Buffers: " + G1_PHASE_COUNTER_SUMMARY + "\\]");
    GCParseRule PROCESSED_BUFFER = new GCParseRule("PROCESSED_BUFFER", "\\[Processed Buffers:\\s*" + COUNTER + "\\]");

    /*
     "[GC Worker Start (ms):  6394.7]",
                "[GC Worker End (ms):  3812.6]"
     */
    String WORKER_SUMMARY = "(GC Worker Start|GC Worker End)";
    GCParseRule WORKER_PARALLEL_BLOCK = new GCParseRule("WORKER_PARALLEL_BLOCK", "\\[" + WORKER_SUMMARY + " \\(ms\\):\\s*" + G1_PHASE_TIME_SUMMARY + "\\]");
    GCParseRule SOLARIS_WORKER_PARALLEL_BLOCK = new GCParseRule("SOLARIS_WORKER_PARALLEL_BLOCK", "\\[" + WORKER_SUMMARY + " \\(ms\\):\\s*" + TIME + "\\]");
    GCParseRule WORKER_ACTIVITY = new GCParseRule("WORKER_ACTIVITY", "\\[(GC Worker Other|GC Worker Total) \\(ms\\): " + G1_PHASE_TIME_SUMMARY_SUM + "\\]");
    GCParseRule SOLARIS_WORKER_PARALLEL_ACTIVITY = new GCParseRule("SOLARIS_WORKER_PARALLEL_ACTIVITY", "\\[(GC Worker Other|GC Worker Total) \\(ms\\):\\s*" + TIME + "\\]");

    //2015-04-09T14:28:38.515+0100: 0.876: [GC concurrent-string-deduplication, 112.0B->112.0B(0.0B), avg 0.0%, 0.0000058 secs]
    //2015-10-17T21:10:23.673-0400: 3.137: [GC concurrent-string-deduplication, 79.3K->792.0B(78.6K), avg 96.2%, 0.0024351 secs]
    //"2015-04-09T14:28:38.515+0100: 0.876: [GC concurrent-string-deduplication, 112.0B->112.0B(0.0B), avg 0.0%, 0.0000058 secs]",
    //2015-12-21T10:26:33.282-0500: 35833.538: [GC concurrent-string-deduplication, 113.9K->1152.0B(112.8K), avg 90.9%, 0.0022797 secs]
    GCParseRule CONCURRENT_STRING_DEDUP = new GCParseRule("CONCURRENT_STRING_DEDUP", GC_PREFIX + "concurrent-string-deduplication, " + FRACTIONAL_BEFORE_AFTER_CONFIGURED + ", avg " + PERCENTAGE + ", " + PAUSE_TIME);

    //[String Dedup Fixup: 0.1 ms, GC Workers: 4]
    GCParseRule STRING_DEDUP_FIXUP = new GCParseRule("STRING_DEDUP_FIXUP", "^\\[String Dedup Fixup: " + PAUSE_TIME + ", GC Workers: " + COUNTER + "\\]");
    //[Queue Fixup (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
    GCParseRule QUEUE_FIXUP = new GCParseRule("QUEUE_FIXUP", "^\\[Queue Fixup \\(ms\\): " + FIXUP_STATS + "\\]");
    //[Table Fixup (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.1]
    GCParseRule TABLE_FIXUP = new GCParseRule("TABLE_FIXUP", "^\\[Table Fixup \\(ms\\): " + FIXUP_STATS + "\\]");

    //[Parallel Time: 32.7 ms, GC Workers: 1]
    GCParseRule PARALLEL_TIME = new GCParseRule("PARALLEL_TIME", "\\[Parallel Time: " + TIME + " ms, GC Workers: (" + INTEGER + ")\\]");
    String PARALLEL_ACTIVITY = "(Ext Root Scanning|Code Root Marking|SATB Filtering|Update RS|Scan RS|Code Root Scanning|Object Copy|Termination)";
    GCParseRule TERMINATION_ATTEMPTS = new GCParseRule("TERMINATION_ATTEMPTS", "\\[Termination Attempts: " + G1_PHASE_COUNTER_SUMMARY + "\\]");
    GCParseRule G1_PARALLEL_PHASE_SUMMARY = new GCParseRule("G1_PARALLEL_PHASE_SUMMARY", "\\[" + PARALLEL_ACTIVITY + " \\(ms\\): " + G1_PHASE_TIME_SUMMARY_SUM + "\\]");
    GCParseRule G1_SOLARIS_PARALLEL_PHASE = new GCParseRule("G1_SOLARIS_PARALLEL_PHASE", "\\[" + PARALLEL_ACTIVITY + " \\(ms\\):\\s*" + REAL_VALUE + "\\]");

    GCParseRule G1GC_PHASE = new GCParseRule("G1GC_PHASE", "\\[(Code Root Fixup|Code Root Migration|Code Root Purge|Clear CT|Expand Heap|Other):\\s+" + TIME);

    String G1GC_PHASE_DETAIL_KEYS = "(Root Region Scan Waiting|Evacuation Failure|Choose CSet|Ref Proc|Ref Enq|Free CSet|Redirty Cards|Humongous Reclaim|Humongous Register)";
    GCParseRule G1GC_PHASE_DETAIL_CLAUSE = new GCParseRule("G1GC_PHASE_DETAIL_CLAUSE", "\\[" + G1GC_PHASE_DETAIL_KEYS + ":\\s+" + TIME);

    //[Eden: 512.0M(512.0M)->0.0B(505.0M) Survivors: 0.0B->7168.0K Heap: 512.0M(518.0M)->6418.4K(519.0M)]
    //[Eden: 89.0M(89.0M)->0.0B(89.0M) Survivors: 13.0M->13.0M Heap: 103.7M(2048.0M)->30.5M(2048.0M)]
    //[Eden: 128.0M(512.0M)->0.0B(512.0M) Survivors: 0.0B->0.0B Heap: 147.8M(518.0M)->23.1M(518.0M)], [Metaspace: 35052K->35051K(1079296K)]
    GCParseRule G1_MEMORY_SUMMARY = new GCParseRule("G1_MEMORY_SUMMARY", "\\[Eden: " + G1_FROM_TO + " Survivors: " + G1_SURVIVOR_FROM_TO + " Heap: " + G1_FROM_TO + "\\](, \\[" + META_PERM_MEMORY_SUMMARY + "\\])?");
    //Tenuring with no details -> 23M->3973K(472M), 0.0047248 secs]
    GCParseRule G1_NO_DETAILS_MEMORY_SUMMARY = new GCParseRule("G1_NO_DETAILS_MEMORY_SUMMARY", "^" + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]$");
    GCParseRule G1_PRE17040_SUMMARY = new GCParseRule("G1_PRE17040_SUMMARY", "\\[Eden: " + PRE17040_G1_FROM_TO + " Survivors: " + PRE17040_G1_SURVIVOR_FROM_TO + " Heap: " + PRE17040_G1_FROM_TO + "\\]");

    //113.325: [Full GC (System.gc()) 37M->32M(96M), 0.0943030 secs]
    //2015-08-31T09:23:16.635+0100: 678431.946: [Full GC (Allocation Failure)  17G->7270M(18G), 15.5646820 secs]
    GCParseRule FULL_GC = new GCParseRule("FULL_GC", FULL_GC_PREFIX + "\\s+" + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]$");

    //112.230: [Full GC (System.gc()) 112.327: [G1Ergonomics (Heap Sizing) attempt heap shrinking, reason: capacity higher than max desired capacity after Full GC, capacity: 128974848 bytes, occupancy: 29902320 bytes, max desired capacity: 99674399 bytes (70.00 %)]
    GCParseRule G1_FULL_ADAPTIVE_SIZING = new GCParseRule("G1_FULL_ADAPTIVE_SIZING", FULL_GC_PREFIX + TIMESTAMP + "\\[G1Ergonomics \\(Heap Sizing\\) attempt heap shrinking, reason: capacity higher than max desired capacity after Full GC, capacity: " + COUNTER + " bytes, occupancy: " + COUNTER + " bytes, max desired capacity: " + COUNTER + " bytes \\(" + REAL_VALUE + " %\\)\\]");

    //17M->3758K(13M), 0.0235300 secs]
    GCParseRule FREE_FLOATING_OCCUPANCY_SUMMARY = new GCParseRule("FREE_FLOATING_OCCUPANCY_SUMMARY", "^" + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");

    //Adaptive sizing records
    //18.298: [GC pause (G1 Evacuation Pause) (young) 18.298: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 0, predicted base time: 10.00 ms, remaining time: 190.00 ms, target pause time: 200.00 ms]
    GCParseRule G1_YOUNG_WITH_CSET_CONSTRUCTION_START = new GCParseRule("G1_YOUNG_WITH_CSET_CONSTRUCTION_START", G1GC_PREFIX + "\\(young\\) " +
            TIMESTAMP + "\\[G1Ergonomics \\(CSet Construction\\) start choosing CSet, _pending_cards: " + COUNTER + ", predicted base time: " + MS_TIME_STAMP + ", remaining time: " + MS_TIME_STAMP + ", target pause time: " + MS_TIME_STAMP + "\\]");

    // 7.793: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 1395, predicted base time: 14.19 ms, remaining time: 185.81 ms, target pause time: 200.00 ms]
    GCParseRule G1_CSET_CONSTRUCTION_START = new GCParseRule("G1_CSET_CONSTRUCTION_START", "^" + TIMESTAMP + "\\[G1Ergonomics \\(CSet Construction\\) start choosing CSet, _pending_cards: " + COUNTER + ", predicted base time: " + MS_TIME_STAMP);// + ", remaining time: " + MS_TIME_STAMP + ", target pause time: " + MS_TIME_STAMP + "\\]");

    //18.298: [G1Ergonomics (CSet Construction) add young regions to CSet, eden: 512 regions, survivors: 0 regions, predicted young region time: 7833.33 ms]
    GCParseRule CSET_CONSTRUCTION = new GCParseRule("CSET_CONSTRUCTION", TIMESTAMP + "\\[G1Ergonomics \\(CSet Construction\\) add young regions to CSet, eden: " + COUNTER + " regions, survivors: " + COUNTER + " regions, predicted young region time: " + MS_TIME_STAMP + "\\]");

    //18.298: [G1Ergonomics (CSet Construction) finish choosing CSet, eden: 512 regions, survivors: 0 regions, old: 0 regions, predicted pause time: 7843.33 ms, target pause time: 200.00 ms]
    GCParseRule CSET_CONSTRUCTION_END = new GCParseRule("CSET_CONSTRUCTION_END", TIMESTAMP + "\\[G1Ergonomics \\(CSet Construction\\) finish choosing CSet, eden: " + COUNTER + " regions, survivors: " + COUNTER + " regions, old: " + COUNTER + " regions, predicted pause time: " + MS_TIME_STAMP + ", target pause time: " + MS_TIME_STAMP + "\\]");


    //0.772: [G1Ergonomics (Heap Sizing) shrink the heap, requested shrinking amount: 255605857 bytes, aligned shrinking amount: 254803968 bytes, attempted shrinking amount: 254803968 bytes]
    GCParseRule HEAP_SHRINK = new GCParseRule("HEAP_SHRINK", TIMESTAMP + "\\[G1Ergonomics \\(Heap Sizing\\) shrink the heap, requested shrinking amount: " + COUNTER + " bytes, aligned shrinking amount: " + COUNTER + " bytes, attempted shrinking amount: " + COUNTER + " bytes\\]");

    //18.303: [G1Ergonomics (Heap Sizing) expand the heap, requested expansion amount: 845600000 bytes, attempted expansion amount: 1048576 bytes]
    GCParseRule HEAP_EXPAND = new GCParseRule("HEAP_EXPAND", TIMESTAMP + "\\[G1Ergonomics \\(Heap Sizing\\) expand the heap, requested expansion amount: " + COUNTER + " bytes, attempted expansion amount: " + COUNTER + " bytes\\]");

    //18.303: [G1Ergonomics (Heap Sizing) attempt heap expansion, reason: region allocation request failed, allocation request: 8456 bytes]
    GCParseRule ATTEMPT_HEAP_EXPANSION_ALLOC_FAILURE = new GCParseRule("ATTEMPT_HEAP_EXPANSION_ALLOC_FAILURE", TIMESTAMP + "\\[G1Ergonomics \\(Concurrent Cyclesing\\) attempt heap expansion, reason: region allocation request failed, allocation request: " + COUNTER + " bytes\\]");

    //7.942: [G1Ergonomics (Heap Sizing) attempt heap expansion, reason: recent GC overhead higher than threshold after GC, recent GC overhead: 22.02 %, threshold: 10.00 %, uncommitted: 1060110336 bytes, calculated expansion amount: 212022067 bytes (20.00 %)]
    GCParseRule ATTEMPT_HEAP_EXPANSION_OVERHEAD = new GCParseRule("ATTEMPT_HEAP_EXPANSION_OVERHEAD", TIMESTAMP + "\\[G1Ergonomics \\(Heap Sizing\\) attempt heap expansion, reason: recent GC overhead higher than threshold after GC, recent GC overhead: " + PERCENTAGE + ", threshold: " + PERCENTAGE + ", uncommitted: " + COUNTER + " bytes, calculated expansion amount: " + COUNTER + " bytes \\(" + PERCENTAGE + "\\)\\]");

    //0.785: [G1Ergonomics (Heap Sizing) did not shrink the heap, reason: heap shrinking operation failed]
    GCParseRule HEAP_SHRINKING_FAILED = new GCParseRule("HEAP_SHRINKING_FAILED", DATE_TIMESTAMP + "\\[G1Ergonomics \\(Heap Sizing\\) did not shrink the heap, reason: heap shrinking operation failed\\]");

    //7.835: [G1Ergonomics (Concurrent Cycles) request concurrent cycle initiation, reason: occupancy higher than threshold, occupancy: 7340032 bytes, allocation request: 0 bytes, threshold: 6134130 bytes (45.00 %), source: end of GC]
    GCParseRule HIGH_OCCUPANCY_TRIGGERS_CONC = new GCParseRule("HIGH_OCCUPANCY_TRIGGERS_CONC", DATE_TIMESTAMP + "\\[G1Ergonomics \\(Concurrent Cycles\\) request concurrent cycle initiation, reason: occupancy higher than threshold, occupancy: " + COUNTER + " bytes, allocation request: " + COUNTER + " bytes, threshold: " + COUNTER + " bytes \\(" + PERCENTAGE + "\\), source: end of GC\\]");

    //Generalize rule to catch all Concurrent Cycle messages
    //7.842: [G1Ergonomics (Concurrent Cycles) initiate concurrent cycle, reason: concurrent cycle initiation requested]
    GCParseRule INITIATE_CONC_CYCLE = new GCParseRule("INITIATE_CONC_CYCLE", DATE_TIMESTAMP + "\\[G1Ergonomics \\(Concurrent Cycles\\)");

    //7.916: [G1Ergonomics (Concurrent Cycles) do not request concurrent cycle initiation, reason: still doing mixed collections, occupancy: 9437184 bytes, allocation request: 0 bytes, threshold: 6134130 bytes (45.00 %), source: end of GC]
    //7.916: [G1Ergonomics (Concurrent Cycles) do not request concurrent cycle initiation, reason: still doing mixed collections, occupancy: 9437184 bytes, allocation request: 0 bytes, threshold: 6134130 bytes (45.00 %), source: end of GC]
    GCParseRule DO_NOT_REQUEST_CONC_CYCLE = new GCParseRule("DO_NOT_REQUEST_CONC_CYCLE", DATE_TIMESTAMP + "\\[G1Ergonomics \\(Concurrent Cycles\\) do not request concurrent cycle initiation, reason: still doing mixed collections, occupancy: " + COUNTER + " bytes, allocation request: " + COUNTER + " bytes, threshold: " + COUNTER + " bytes \\(" + PERCENTAGE + "\\), source: end of GC\\]");
    //7.859: [G1Ergonomics (Mixed GCs) start mixed GCs, reason: candidate old regions available, candidate old regions: 4 regions, reclaimable: 2406696 bytes (17.66 %), threshold: 10.00 %]
    //7.869: [G1Ergonomics (Mixed GCs) do not continue mixed GCs, reason: reclaimable percentage not over threshold, candidate old regions: 2 regions, reclaimable: 1031104 bytes (7.56 %), threshold: 10.00 %]
    //7.986: [G1Ergonomics (Mixed GCs) do not start mixed GCs, reason: reclaimable percentage not over threshold, candidate old regions: 3 regions, reclaimable: 1380376 bytes (2.53 %), threshold: 10.00 %]
    GCParseRule START_MIXED_GC = new GCParseRule("START_MIXED_GC", DATE_TIMESTAMP + "\\[G1Ergonomics \\(Mixed GCs\\) (.+), reason: (.+), candidate old regions: " + COUNTER + " regions, reclaimable: " + COUNTER + " bytes \\(" + PERCENTAGE + "\\), threshold: " + PERCENTAGE + "\\]");

    //121.597: [G1Ergonomics (Mixed GCs) do not start mixed GCs, reason: concurrent cycle is about to start]
    GCParseRule DELAY_MIXED_GC = new GCParseRule("DELAY_MIXED_GC", DATE_TIMESTAMP + "\\[G1Ergonomics \\(Mixed GCs\\) (.+), reason: concurrent cycle is about to start]");
    //7.866: [G1Ergonomics (CSet Construction) finish adding old regions to CSet, reason: old CSet region num reached max, old: 2 regions, max: 2 regions]

    //There are a number of reasons for CSET_FINISHED that should be handled outside of overly specific parse rules.
    //todo: Ergonomic support is next on the list.
    //GCParseRule CSET_FINISH = new GCParseRule("CSET_FINISH",DATE_TIMESTAMP + "\\[G1Ergonomics \\(CSet Construction\\) finish adding old regions to CSet, reason: old CSet region num reached max, old: " + COUNTER + " regions, max: " + COUNTER + " regions\\]");
    GCParseRule CSET_FINISH = new GCParseRule("CSET_FINISH", DATE_TIMESTAMP + "\\[G1Ergonomics \\(CSet Construction\\) finish adding old regions to CSet, reason: ");
    //7.922: [G1Ergonomics (CSet Construction) finish adding old regions to CSet, reason: reclaimable percentage not over threshold, old: 1 regions, max: 2 regions, reclaimable: 973976 bytes (7.15 %), threshold: 10.00 %]
    //GCParseRule CSET_FINISH_THRESHOLD = new GCParseRule("CSET_FINISH_THRESHOLD",DATE_TIMESTAMP + "\\[G1Ergonomics \\(CSet Construction\\) finish adding old regions to CSet, reason: reclaimable percentage not over threshold, old: " + COUNTER + " regions, max: " + COUNTER + " regions, reclaimable: " + COUNTER + " bytes \\(" + PERCENTAGE + "\\), threshold: " + PERCENTAGE+ "\\]");
    //Missed: 69305.636: [G1Ergonomics (CSet Construction) finish adding old regions to CSet, reason: predicted time is too high, predicted time: 8.92 ms, remaining time: 0.00 ms, old: 133 regions, min: 133 regions]
    GCParseRule CSET_ADDING = new GCParseRule("CSET_ADDING", DATE_TIMESTAMP + "\\[G1Ergonomics \\(CSet Construction\\) added expensive regions to CSet, reason: ");
    //Missed: 69305.636: [G1Ergonomics (CSet Construction) added expensive regions to CSet, reason: old CSet region num not reached min, old: 133 regions, expensive: 133 regions, min: 133 regions, remaining time: 0.00 ms]

    //Pre 1.7.0_40 records

    //public final GCParseRule STATS_INT_170PRE40 = new GCParseRule("STATS_INT_170PRE40", "Sum: " + COUNTER + ", Avg: " + COUNTER + ", Min: " + COUNTER + ", Max: " + COUNTER + ", Diff: " + COUNTER + "\\]");
    //public final GCParseRule STATS_REAL_170PRE40 = new GCParseRule("STATS_REAL_170PRE40", "Avg:\\s+" + REAL_VALUE + ", Min:\\s+" + REAL_VALUE + ", Max:\\s+" + REAL_VALUE + ", Diff:\\s+" + REAL_VALUE);

    //[Termination Attempts : 1 1 1 1 1 1 1 1 1 1 1 1
    //GCParseRule TERMINATION_ATTEMPTS_PRE17040 = new GCParseRule("TERMINATION_ATTEMPTS_PRE17040", "\\[Termination Attempts : " + INTEGER);

    //[GC Worker Start (ms):  11906844.4  11906844.4  11906844.4  11906844.5  11906844.5  11906844.5  11906844.6  11906844.6  11906844.7  11906844.8  11906844.8  11906844.8
    //[GC Worker (ms):  19.6  19.5  19.5  19.5  19.4  19.5  19.4  19.4  19.4  19.4  19.4  19.4
    //[GC Worker End (ms):  11906727.5  11906727.5  11906727.5  11906727.5  11906727.5  11906727.5  11906727.5  11906727.5  11906727.5  11906727.5  11906727.5  11906727.5
    //String PHASE_TIME_SUMMARY_CLAUSES_PRE17040 = "(GC Worker Start|GC Worker|GC Worker End|Termination|GC Worker Other|SATB Filtering|Ext Root Scanning|Update RS|Scan RS|Object Copy)";
    //GCParseRule G1_PHASE_SUMMARY_PRE17040 = new GCParseRule("G1_PHASE_SUMMARY_PRE17040","\\[" + PHASE_TIME_SUMMARY_CLAUSES_PRE17040 + " \\(ms\\):\\s+" + REAL_NUMBER);

    //[Complete CSet Marking:   0.0 ms]
    //[Parallel Time:  35.0 ms]
    //String G1_PHASE_TIMES_PRE17040_PHASES = "(Complete CSet Marking|Parallel Time)";
    //GCParseRule G1_PHASE_TIMES_PRE17040 = new GCParseRule("G1_PHASE_TIMES_PRE17040","\\[" + G1_PHASE_TIMES_PRE17040_PHASES + ":\\s+" + REAL_NUMBER + " ms\\]");

    //2014-10-21T13:21:45.534-0500: 2014-10-21T13:21:45.534-0500: 17610.131: [GC concurrent-root-region-scan-end, 0.0021980]
    //GCParseRule G1_CONCURRENT_END_PRE17040 = new GCParseRule("G1_CONCURRENT_END_PRE17040", "^" + GC_PREFIX + "concurrent-(.+)-end, " + TIME);


    //Concurrent phase

    //549.246: [GC concurrent-root-region-scan-start]
    //657.426: [GC concurrent-root-region-scan-end, 0.0052220 secs]
    //549.246: [GC concurrent-mark-start]
    GCParseRule G1_CONCURRENT_START = new GCParseRule("G1_CONCURRENT_START", "^" + GC_PREFIX + "concurrent-(.+)-start\\]$");
    GCParseRule G1_CONCURRENT_START_WITHOUT_PREFIX = new GCParseRule("G1_CONCURRENT_START_WITHOUT_PREFIX", "^" + "concurrent-(.+)-start\\]$");

    //549.246: [GC concurrent-root-region-scan-end, 0.0000700 secs]
    //549.251: [GC concurrent-mark-end, 0.0055240 secs]
    GCParseRule G1_CONCURRENT_END = new GCParseRule("G1_CONCURRENT_END", "^" + GC_PREFIX + "concurrent-(.+)-end, " + PAUSE_TIME);
    GCParseRule G1_CORRUPTED_CONCURRENT_END = new GCParseRule("G1_CORRUPTED_CONCURRENT_END", "^\\[GC concurrent-(.+)-end, " + PAUSE_TIME);
    GCParseRule G1_CORRUPTED_CONCURRENT_ROOT_REGION_SCAN_END = new GCParseRule("G1_CORRUPTED_CONCURRENT_ROOT_REGION_SCAN_END", "^" + DATE_TIMESTAMP + GC_PREFIX + "concurrent-(.+)-end, " + PAUSE_TIME);
    GCParseRule G1_FLOATING_CONCURRENT_PHASE_START = new GCParseRule("G1_FLOATING_CONCURRENT_PHASE_START", "^\\[GC concurrent(.+)\\]$");
    GCParseRule G1_FULL_INTERRUPTS_CONCURRENT_CYCLE = new GCParseRule("G1_FULL_INTERRUPTS_CONCURRENT_CYCLE", FULL_GC_PREFIX + GC_PREFIX + " concurrent-(.+)-end, " + PAUSE_TIME);
    //Strange Full GC corruption
    //2018-01-29T17:34:24.984+0000: 5115.588: [Full GC (Metadata GC Threshold) 2018-01-29T17:34:24.984+0000: 5115.588: [GC concurrent-root-region-scan-start]
    GCParseRule FULL_WITH_CONCURRENT_PHASE_START = new GCParseRule("FULL_WITH_CONCURRENT_PHASE_START", FULL_GC_PREFIX + GC_PREFIX + "concurrent-(.+)-start\\]");
    //more corruption
    //"2018-02-08T19:13:30.878+0000: 2018-02-08T19:13:30.878+0000: 875061.483: 875061.483: [Full GC (Metadata GC Threshold) [GC concurrent-root-region-scan-start]"
    //GC_CAUSE +
    GCParseRule FULL_WITH_CONCURRENT_PHASE_CORRUPTED = new GCParseRule("FULL_WITH_CONCURRENT_PHASE_CORRUPTED", "^" + DATE_STAMP + DATE_TIMESTAMP + TIMESTAMP + "\\[Full GC ?" + "(\\(.{4,50}\\))?\\s*" + "\\[GC concurrent-(.+)-start\\]");

    //2018-02-08T20:03:08.475+0000: 878039.079: 2018-02-08T20:03:08.475+0000[Full GC (Metadata GC Threshold) : 878039.079: [GC concurrent-root-region-scan-start]
    //2018-02-08T20:03:08.475+0000[Full GC (Metadata GC Threshold) : 878039.079: [GC concurrent-root-region-scan-start]
    GCParseRule FULL_WITH_CONCURRENT_PHASE_INTERLEAVED = new GCParseRule("FULL_WITH_CONCURRENT_PHASE_INTERLEAVED", DATE_TIMESTAMP + "(" + DATE + ")\\[Full GC ?" + "(\\(.{4,50}\\))? : " + TIMESTAMP + "\\[GC concurrent-(.+)-start\\]");
    //2018-02-08T20:04:14.978+0000: 2018-02-08T20:04:14.978+0000: 878105.583878105.583: [GC concurrent-root-region-scan-start]
    GCParseRule CORRUPTED_CONCURRENT_START = new GCParseRule("CORRUPTED_CONCURRENT_START", DATE_STAMP + DATE_STAMP + INTEGER + DECIMAL_POINT + "\\d{3}" + TIMESTAMP + "\\[GC concurrent-(.+)-start\\]");
    //2018-02-08T20:05:15.505+0000: 878166.1102018-02-08T20:05:15.505+0000: : 878166.110: [GC concurrent-root-region-scan-start]
    GCParseRule CORRUPTED_CONCURRENT_START_V2 = new GCParseRule("CORRUPTED_CONCURRENT_START_V2", DATE_STAMP + "(" + INTEGER + DECIMAL_POINT + "\\d{3})" + DATE_STAMP + ": " + TIMESTAMP + "\\[GC concurrent-(.+)-start\\]");

    //2018-02-08T20:32:54.965+0000: 2018-02-08T20:32:54.965+0000: 879825.569: [GC concurrent-root-region-scan-start]
    GCParseRule CONCURRENT_START_V3 = new GCParseRule("CONCURRENT_START_V3", DATE_STAMP + DATE_STAMP + TIMESTAMP + "\\[GC concurrent-(.+)-start\\]");
    //2018-02-08T20:33:01.419+0000: 2018-02-08T20:33:01.419+0000879832.023: [GC concurrent-root-region-scan-start]
    GCParseRule CONCURRENT_START_V4 = new GCParseRule("CONCURRENT_START_V4", DATE_STAMP + DATE + TIMESTAMP + "\\[GC concurrent-(.+)-start\\]");
    //2018-02-08T20:34:59.393+0000: 2018-02-08T20:34:59.393+0000: 879949.998: 879949.998[GC concurrent-root-region-scan-start]
    GCParseRule CONCURRENT_START_V5 = new GCParseRule("CONCURRENT_START_V5", DATE_STAMP + DATE_STAMP + TIMESTAMP + INTEGER + DECIMAL_POINT + "\\d{3}\\[GC concurrent-(.+)-start\\]");


    //"2018-02-08T20:13:00.911+0000: 878631.5152018-02-08T20:13:00.911+0000: : [Full GC (Metadata GC Threshold) 878631.515: [GC concurrent-root-region-scan-start]"
    GCParseRule CORRUPTED_CONCURRENT_START_V3 = new GCParseRule("CORRUPTED_CONCURRENT_START_V3", DATE_STAMP + "(" + INTEGER + DECIMAL_POINT + "\\d{3})" + DATE_STAMP + ": \\[Full GC " + GC_CAUSE + TIMESTAMP + "\\[GC concurrent-(.+)-start\\]");
    //2018-02-08T20:13:02.246+0000: 878632.850: 2018-02-08T20:13:02.246+0000: [Full GC (Metadata GC Threshold) 878632.850: [GC concurrent-root-region-scan-start]
    GCParseRule CORRUPTED_CONCURRENT_START_V4 = new GCParseRule("CORRUPTED_CONCURRENT_START_V4", DATE_TIMESTAMP + DATE_STAMP + "\\[Full GC " + GC_CAUSE + GC_PREFIX + "concurrent-(.+)-start\\]");
    //2018-02-08T20:21:26.246+0000: 2018-02-08T20:21:26.246+0000879136.850: [Full GC (Metadata GC Threshold) : 879136.850: [GC concurrent-root-region-scan-start]
    GCParseRule CORRUPTED_CONCURRENT_START_V5 = new GCParseRule("CORRUPTED_CONCURRENT_START_V5", DATE_STAMP + DATE + FULL_GC_PREFIX + ": " + GC_PREFIX + "concurrent-(.+)-start\\]");
    //2018-02-08T20:31:33.633+0000: 2018-02-08T20:31:33.633+0000: 879744.238879744.238: [Full GC (Metadata GC Threshold) : [GC concurrent-root-region-scan-start]
    GCParseRule CORRUPTED_CONCURRENT_START_V7 = new GCParseRule("CORRUPTED_CONCURRENT_START_V7", DATE_STAMP + DATE_STAMP + INTEGER + DECIMAL_POINT + "\\d{3}" + TIMESTAMP + "\\[Full GC " + GC_CAUSE + ": \\[GC concurrent-(.+)-start\\]");
    //2018-02-08T20:31:54.234+0000: 2018-02-08T20:31:54.234+0000879764.839: 879764.839: [Full GC (Metadata GC Threshold) : [GC concurrent-root-region-scan-start]
    GCParseRule CORRUPTED_CONCURRENT_START_V8 = new GCParseRule("CORRUPTED_CONCURRENT_START_V8", DATE_STAMP + DATE + TIMESTAMP + TIMESTAMP + "\\[Full GC " + GC_CAUSE + ": \\[GC concurrent-(.+)-start\\]");/**/
    //2018-02-08T20:32:51.258+0000: 2018-02-08T20:32:51.258+0000: 879821.862: 879821.862[Full GC (Metadata GC Threshold) : [GC concurrent-root-region-scan-start]
    //2018-02-08T20:32:51.258+0000: 879821.862: 879821.862[Full GC (Metadata GC Threshold) : [GC concurrent-root-region-scan-start]
    GCParseRule CORRUPTED_CONCURRENT_START_V9 = new GCParseRule("CORRUPTED_CONCURRENT_START_V9", DATE_STAMP + DATE_TIMESTAMP + TIME + "\\[Full GC " + GC_CAUSE + ": \\[GC concurrent-(.+)-start\\]");/**/
    //: [Full GC (Metadata GC Threshold) 2018-02-08T20:04:14.979+0000: 878105.583: [GC concurrent-root-region-scan-end, 0.0000770 secs]
    GCParseRule FULL_WITH_CONCURRENT_END = new GCParseRule("FULL_WITH_CONCURRENT_END", "(?:: )?\\[Full GC " + GC_CAUSE + GC_PREFIX + " concurrent-(.+)-end, " + PAUSE_TIME);
    //[Full GC (Metadata GC Threshold) 2018-02-08T20:27:32.297+0000: 879502.902: [GC concurrent-mark-start]"
    GCParseRule FULL_MISSING_TIMESTAMP_CONCURRENT_START = new GCParseRule("FULL_MISSING_TIMESTAMP_CONCURRENT_START", "^\\[Full GC " + GC_CAUSE + DATE_TIMESTAMP + "\\[GC concurrent-(.+)-start\\]");
    GCParseRule FULL_GC_FRAGMENT = new GCParseRule("FULL_GC_FRAGMENT", "^" + FULL_GC_PREFIX + "$");

    //511.628: [GC concurrent-mark-abort]
    GCParseRule G1_CONCURRENT_ABORT = new GCParseRule("G1_CONCURRENT_ABORT", GC_PREFIX + "concurrent-(.+)-abort");

    //604.395: [GC concurrent-mark-reset-for-overflow]
    GCParseRule CONCURRENT_MARK_OVERFLOW = new GCParseRule("CONCURRENT_MARK_OVERFLOW", DATE_TIMESTAMP + "\\[GC concurrent-mark-reset-for-overflow\\]");

    //9.251: [GC remark, 0.0012190 secs]
    //6.298: [GC remark 6.298: [GC ref-proc, 0.0000570 secs], 0.0010940 secs]
    //2014-02-21T16:04:24.321-0100: 7.852: [GC remark 2014-02-21T16:04:24.322-0100: 7.853: [GC ref-proc, 0.0000640 secs], 0.0013310 secs]
    GCParseRule G1_REMARK = new GCParseRule("G1_REMARK", GC_PREFIX + "remark( " + G1_REF_PROC_BLOCK + ")?, " + PAUSE_TIME);

    //549.253: [GC cleanup 7826K->7826K(13M), 0.0004750 secs]
    GCParseRule G1_CLEANUP = new GCParseRule("G1_CLEANUP", GC_PREFIX + "cleanup " + BEFORE_AFTER_CONFIGURED_PAUSE);
    //13965.696: [GC cleanup, 0.0000010 secs]
    GCParseRule G1_CLEANUP_NO_MEMORY = new GCParseRule("G1_CLEANUP_NO_MEMORY", GC_PREFIX + "cleanup, " + PAUSE_TIME + "\\]");
    GCParseRule SPLIT_CLEANUP = new GCParseRule("SPLIT_CLEANUP", GC_PREFIX + " cleanup$");

    //jdk 1.7.0, missing cause
    //1577.259: [Full GC 8718K->3548K(12M), 0.0333810 secs]
    GCParseRule G1_FUll = new GCParseRule("G1_FUll", FULL_GC_PREFIX + BEFORE_AFTER_CONFIGURED_PAUSE);

    //Records found at the end of a GC log produced by normally terminated JVM

    GCParseRule HEAP_START = new GCParseRule("HEAP_START", "^Heap$");
    GCParseRule GARBAGE_FIRST_HEAP = new GCParseRule("GARBAGE_FIRST_HEAP", "garbage-first heap\\s+total " + MEMORY_SIZE + ", used " + MEMORY_SIZE);
    GCParseRule REGION_SIZE = new GCParseRule("REGION_SIZE", "region size " + MEMORY_SIZE + ", " + COUNTER + " young (" + MEMORY_SIZE + "), " + COUNTER + " survivors (" + MEMORY_SIZE + ")");

    //Corruption

    //2014-10-21T14:27:15.405-0500: 21540.002: [GC pause (young)2014-10-21T14:27:15.415-0500: 21540.013: [GC concurrent-root-region-scan-end, 0.0148270]
    //2014-07-21T18:18:26.677+0500: 947286.767: [GC pause (young)2014-07-21T18:18:26.678+0500: 947286.768: [GC concurrent-cleanup-end, 0.0005220 secs]
    //tag secs may or may not be there so lets not be greedy.
    GCParseRule YOUNG_WITH_CONCURRENT_END = new GCParseRule("YOUNG_WITH_CONCURRENT_END", G1GC_PREFIX + "\\((young|mixed)\\)" + GC_PREFIX + "concurrent-(.+)-end, " + "(-?" + REAL_NUMBER + ")");
    //100081.540: [Full GC100081.540: [GC concurrent-root-region-scan-start]
    GCParseRule FULLGC_WITH_CONCURRENT_PHASE = new GCParseRule("FULLGC_WITH_CONCURRENT_PHASE", "^" + FULL_GC_PREFIX + GC_PREFIX + "concurrent-(.+)-start\\]");

    //28983.087: [GC pause (young)28984.641: [SoftReference, 0 refs, 0.0000060 secs]28984.641: [WeakReference, 651 refs, 0.0001330 secs]28984.641: [FinalReference, 4358 refs, 0.0198770 secs]28984.661: [PhantomReference, 20 refs, 0.0000100 secs]28984.661: [JNI Weak Reference, 0.0002120 secs]2014-10-21T16:31:20.256-0500: 28984.853: [GC concurrent-mark-end, 0.8720730 sec] (to-space overflow), 1.88262800 secs]
    GCParseRule YOUNG_REFERENCE_WITH_CONCURRENT_END = new GCParseRule("YOUNG_REFERENCE_WITH_CONCURRENT_END", G1GC_PREFIX + "\\((young||mixed)\\)( \\(initial-mark\\))?" + REFERENCE_RECORDS + GC_PREFIX + " concurrent-(.+)-end, " + PAUSE_TIME + "\\]");
    //2014-07-29T14:43:29.630+05002014-07-29T14:43:29.630+0500: : 1625589.7201625589.720: : [GC concurrent-cleanup-end, 0.0013630 secs]
    //[GC pause (young), 0.0181330 secs]
    //2014-07-29T14:43:29.630+05002014-07-29T14:43:29.630+0500: : 1625589.7201625589.720: : [GC concurrent-cleanup-end, 0.0013630 secs]
    GCParseRule YOUNG_SPLIT_AT_DATESTAMP = new GCParseRule("YOUNG_SPLIT_AT_DATESTAMP", "(" + DATE + ")(" + DATE + "): : (\\d+[,|.]\\d{3})(\\d+[,|.]\\d{3}): : \\[GC concurrent-(.+)-end, " + PAUSE_TIME);
    GCParseRule YOUNG_SPLIT_AT_TIMESTAMP = new GCParseRule("YOUNG_SPLIT_AT_TIMESTAMP", "(\\d+[,|.]\\d{3})(\\d+[,|.]\\d{3}): : \\[GC concurrent-(.+)-end, " + PAUSE_TIME);
    GCParseRule FREE_FLOATING_YOUNG_BLOCK = new GCParseRule("FREE_FLOATING_YOUNG_BLOCK", "^\\[GC pause " + GC_CAUSE + "(\\(young||mixed)\\)( \\(initial-mark\\)| \\(to-space exhausted\\))?, " + PAUSE_TIME + "\\]$");


    //G1Ergonomics

    /*

    @ Young collection
    0.028: [G1Ergonomics (Heap Sizing) expand the heap, requested expansion amount: 2361393152 bytes, attempted expansion amount: 2361393152 bytes]
    2014-08-01T09:04:46.201-0500: 506.644: [GC pause (young) 506.645: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 15995, predicted base time: 57.98 ms, remaining time: 42.02 ms, target pause time: 100.00 ms]
    506.645: [G1Ergonomics (CSet Construction) add young regions to CSet, eden: 1576 regions, survivors: 0 regions, predicted young region time: 24111.98 ms]
    506.645: [G1Ergonomics (CSet Construction) finish choosing CSet, eden: 1576 regions, survivors: 0 regions, old: 0 regions, predicted pause time: 24169.97 ms, target pause time: 100.00 ms]

    @ initial-mark
     3088.158: [G1Ergonomics (Concurrent Cycles) initiate concurrent cycle, reason: concurrent cycle initiation requested]
     2014-08-01T09:47:47.714-0500: 3088.158: [GC pause (young) (initial-mark) 3088.158: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 53730, predicted base time: 38.04 ms, remaining time: 61.96 ms, target pause time: 100.00 ms]
     3088.158: [G1Ergonomics (CSet Construction) add young regions to CSet, eden: 500 regions, survivors: 0 regions, predicted young region time: 19.95 ms]
     3088.158: [G1Ergonomics (CSet Construction) finish choosing CSet, eden: 500 regions, survivors: 0 regions, old: 0 regions, predicted pause time: 57.99 ms, target pause time: 100.00 ms]
     3088.160: [G1Ergonomics (Heap Sizing) attempt heap expansion, reason: region allocation request failed, allocation request: 50344 bytes]
     3088.160: [G1Ergonomics (Heap Sizing) expand the heap, requested expansion amount: 50344 bytes, attempted expansion amount: 1048576 bytes]
     3088.160: [G1Ergonomics (Heap Sizing) did not expand the heap, reason: heap expansion operation failed]

     @end of concurrent phase
     2014-08-01T09:47:50.657-0500: 3091.100: [GC pause (young)3091.100: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 59842, predicted base time: 39.77 ms, remaining time: 60.23 ms, target pause time: 100.00 ms]
     translates to
     2014-08-01T09:47:50.657-0500: 3091.100: [GC pause (young)
     3091.100: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 59842, predicted base time: 39.77 ms, remaining time: 60.23 ms, target pause time: 100.00 ms]
     3091.100: [G1Ergonomics (CSet Construction) add young regions to CSet, eden: 694 regions, survivors: 0 regions, predicted young region time: 7.55 ms]
     3091.100: [G1Ergonomics (CSet Construction) finish choosing CSet, eden: 694 regions, survivors: 0 regions, old: 0 regions, predicted pause time: 47.32 ms, target pause time: 100.00 ms]
     3091.102: [G1Ergonomics (Heap Sizing) attempt heap expansion, reason: region allocation request failed, allocation request: 12584 bytes]
     3091.102: [G1Ergonomics (Heap Sizing) expand the heap, requested expansion amount: 12584 bytes, attempted expansion amount: 1048576 bytes]
     3091.102: [G1Ergonomics (Heap Sizing) did not expand the heap, reason: heap expansion operation failed]
     3091.269: [G1Ergonomics (Concurrent Cycles) do not request concurrent cycle initiation, reason: still doing mixed collections, occupancy: 2170552320 bytes, allocation request: 0 bytes, threshold: 1062626895 bytes (45.00 %), source: end of GC]
     3091.269: [G1Ergonomics (Mixed GCs) start mixed GCs, reason: candidate old regions available, candidate old regions: 1442 regions, reclaimable: 1490152376 bytes (63.10 %), threshold: 10.00 %]

     @ Full GC
     1536.996: [G1Ergonomics (Heap Sizing) attempt heap expansion, reason: allocation request failed, allocation request: 39448 bytes]
     1536.996: [G1Ergonomics (Heap Sizing) expand the heap, requested expansion amount: 1048576 bytes, attempted expansion amount: 1048576 bytes]
     1536.996: [G1Ergonomics (Heap Sizing) did not expand the heap, reason: heap expansion operation failed]

     @mixed
     2014-08-01T09:47:51.677-0500: 3092.120: [GC pause (mixed) 3092.120: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 67088, predicted base time: 41.82 ms, remaining time: 58.18 ms, target pause time: 100.00 ms]
     3092.120: [G1Ergonomics (CSet Construction) add young regions to CSet, eden: 44 regions, survivors: 0 regions, predicted young region time: 2.15 ms]
     3092.121: [G1Ergonomics (CSet Construction) finish adding old regions to CSet, reason: predicted time is too high, predicted time: 0.64 ms, remaining time: 0.00 ms, old: 181 regions, min: 181 regions]
     3092.121: [G1Ergonomics (CSet Construction) added expensive regions to CSet, reason: old CSet region num not reached min, old: 181 regions, expensive: 93 regions, min: 181 regions, remaining time: 0.00 ms]
     3092.121: [G1Ergonomics (CSet Construction) finish choosing CSet, eden: 44 regions, survivors: 0 regions, old: 181 regions, predicted pause time: 159.09 ms, target pause time: 100.00 ms]
     3092.123: [G1Ergonomics (Heap Sizing) attempt heap expansion, reason: region allocation request failed, allocation request: 2048 bytes]
     3092.123: [G1Ergonomics (Heap Sizing) expand the heap, requested expansion amount: 2048 bytes, attempted expansion amount: 1048576 bytes]
     3092.123: [G1Ergonomics (Heap Sizing) did not expand the heap, reason: heap expansion operation failed]
     3092.190: [G1Ergonomics (Concurrent Cycles) do not request concurrent cycle initiation, reason: still doing mixed collections, occupancy: 2217738240 bytes, allocation request: 0 bytes, threshold: 1062626895 bytes (45.00 %), source: end of GC]
     3092.190: [G1Ergonomics (Mixed GCs) continue mixed GCs, reason: candidate old regions available, candidate old regions: 1080 regions, reclaimable: 1110840296 bytes (47.04 %), threshold: 10.00 %]
    */

    GCParseRule G1ERGONOMICS = new GCParseRule("G1ERGONOMICS", "^" + DATE_TIMESTAMP + "\\[G1Ergonomics \\(");
    GCParseRule YOUNG_SPLIT_BY_G1ERGONOMICS = new GCParseRule("YOUNG_SPLIT_BY_G1ERGONOMICS", G1GC_PREFIX + "\\((young||mixed)\\) " + DATE_TIMESTAMP + "\\[G1Ergonomics \\(");
    GCParseRule G1_INITIAL_MARK_ERGONOMICS = new GCParseRule("G1_INITIAL_MARK_ERGONOMICS", G1GC_PREFIX + "\\((young||mixed)\\) \\(initial-mark\\) " + DATE_TIMESTAMP + "\\[G1Ergonomics \\(");

    //RSetStats
    /*
     Concurrent RS processed 0 cards
      Of 259 completed buffers:
          259 (100.0%) by conc RS threads.
            0 (  0.0%) by mutator threads.
      Conc RS threads times(s)
              0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00
      Total heap region rem set sizes = 6087K.  Max = 2K.
      Static structures = 369K, free_lists = 0K.
        0 occupied cards represented.
        Max size region = 0:(F)[0x0000000763400000,0x0000000763400000,0x0000000763500000], size = 3K, occupied = 0K.
        Did 0 coarsenings.
     */

    /*
     Recent concurrent refinement statistics
  Processed 4907 cards
  Of 32 completed buffers:
           32 ( 93.8%) by concurrent RS threads.
            2 (  6.2%) by mutator threads.
  Did 0 coarsenings.
  Concurrent RS threads times (s)
          0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00
  Concurrent sampling threads times (s)
          0.00

 Current rem set statistics
  Total per region rem sets sizes = 230532K. Max = 1880K.
         105K (  0.0%) by 2 Young regions
          39K (  0.0%) by 4 Humonguous regions
       15506K (  6.7%) by 1569 Free regions
      214881K ( 93.2%) by 985 Old regions
   Static structures = 600K, free_lists = 10371K.
    9617950 occupied cards represented.
         1515 (  0.0%) entries by 2 Young regions
            4 (  0.0%) entries by 4 Humonguous regions
            0 (  0.0%) entries by 1569 Free regions
      9616431 (100.0%) entries by 985 Old regions
    Region with largest rem set = 0:(O)[0x00000002c0000000,0x00000002c0800000,0x00000002c0800000], size = 1880K, occupied = 1410K.
  Total heap region code root sets sizes = 171K.  Max = 97K.
           0K (  0.0%) by 2 Young regions
           0K (  0.0%) by 4 Humonguous regions
          24K ( 14.3%) by 1569 Free regions
         147K ( 85.7%) by 985 Old regions
    5416 code roots represented.
            0 (  0.0%) elements by 2 Young regions
            0 (  0.0%) elements by 4 Humonguous regions
            0 (  0.0%) elements by 1569 Free regions
         5416 (100.0%) elements by 985 Old regions
    Region with largest amount of code roots = 0:(O)[0x00000002c0000000,0x00000002c0800000,0x00000002c0800000], size = 97K, num_elems = 4.
     */
    GCParseRule RSET_HEADER = new GCParseRule("RSET_HEADER", "Concurrent RS processed " + INTEGER + " cards");
    GCParseRule RSET_CONCONCURRENT_HEADER = new GCParseRule("RSET_CONCONCURRENT_HEADER", "Of " + INTEGER + " completed buffers:");
    GCParseRule RSET_CONCURRENT_RS_Threads = new GCParseRule("RSET_CONCURRENT_RS_Threads", COUNTER + " \\(\\s*" + PERCENTAGE + "\\) by conc(?:urrent)? RS threads\\.");
    GCParseRule RSET_CONCURRENT_MUTATOR = new GCParseRule("RSET_CONCURRENT_MUTATOR", INTEGER + " \\(\\s+" + REAL_NUMBER + "\\%\\) by mutator threads.");
    GCParseRule RSET_CONCURRENT_TIMES_HEADER = new GCParseRule("RSET_CONCURRENT_TIMES_HEADER", "Conc(?:urrent)? RS threads times\\s?\\(s\\)");
    GCParseRule RSET_THREAD_TIMES = new GCParseRule("RSET_THREAD_TIMES", "^(" + REAL_VALUE + "\\s+)+");
    GCParseRule RSET_RS_SIZE = new GCParseRule("RSET_RS_SIZE", "^Total (?:per |heap )?region rem set(?:s)? sizes = " + MEMORY_SIZE + "\\.\\s*Max = " + MEMORY_SIZE + "\\.");//new ParseRule("Total heap region rem set sizes = " + INTEGER + "K.  Max = " + INTEGER + "K.");
    GCParseRule RSET_RS_STATIC_STRUCTURES = new GCParseRule("RSET_RS_STATIC_STRUCTURES", "Static structures = " + INTEGER + "K, free_lists = " + INTEGER + "K.");
    GCParseRule RSET_RS_OCCUPIED_CARDS = new GCParseRule("RSET_RS_OCCUPIED_CARDS", INTEGER + " occupied cards represented.");
    //todo: fill out rule when it finally gets used.
    GCParseRule RSET_MAX_REGION_SIZE = new GCParseRule("RSET_MAX_REGION_SIZE", "Max size region = ");//0:(F)[0x0000000763400000,0x0000000763400000,0x0000000763500000], size = 3K, occupied = 0K.");
    GCParseRule RSET_COARSENINGS = new GCParseRule("RSET_COARSENINGS", "Did " + INTEGER + " coarsenings.");


    //todo: Review and integrate where needed as these are rules we don't have a log to test with.
    //2015-10-17T20:03:51.747-0400: 1.211: [GC pause (G1 Evacuation Pause) (young)Before GC RS summary
    //2015-10-17T21:07:31.429-0400: 3820.892: [GC pause (G1 Evacuation Pause) (mixed)Before GC RS summary
    GCParseRule G1_YOUNG_RS_SUMMARY = new GCParseRule("G1_YOUNG_RS_SUMMARY", G1GC_PREFIX + "(\\(young\\)|\\(mixed\\))Before GC RS summary");
    GCParseRule RS_AFTER_GC_RS_SUMMARY = new GCParseRule("RS_AFTER_GC_RS_SUMMARY", "After GC RS summary");
    GCParseRule RS_REFINEMENT_STATS = new GCParseRule("RS_REFINEMENT_STATS", "Recent concurrent refinement statistics");
    GCParseRule CONCURRENT_SAMPLING_THREADS_TIMES = new GCParseRule("CONCURRENT_SAMPLING_THREADS_TIMES", "Concurrent sampling threads times \\(s\\)");
    GCParseRule RS_VALUE = new GCParseRule("RS_VALUE", "^" + REAL_VALUE + "$");
    GCParseRule RS_CURRENT_RS_STATS = new GCParseRule("RS_CURRENT_RS_STATS", "Current rem set statistics");
    GCParseRule PROCESSED_CARDS = new GCParseRule("PROCESSED_CARDS", "Processed " + COUNTER + " cards");

    GCParseRule RS_REGION_WITH_LARGEST_RS = new GCParseRule("RS_REGION_WITH_LARGEST_RS", "^Region with largest rem set = " + COUNTER + ":\\([F|S|E]\\)" + MEMORY_POOL_BOUNDS + ", size = " + MEMORY_SIZE + ", occupied = " + MEMORY_SIZE + "\\.");
    GCParseRule RS_TOTAL_CODE_ROOT_SET_SIZES = new GCParseRule("RS_TOTAL_CODE_ROOT_SET_SIZES", "Total heap region code root sets sizes = " + MEMORY_SIZE + "\\.  Max = " + MEMORY_SIZE + "\\.");

    GCParseRule RS_REGION_BREAKDOWN = new GCParseRule("RS_REGION_BREAKDOWN", "^" + MEMORY_SIZE + "? \\(\\s*" + PERCENTAGE + "\\) [by|entries by|elements by]+? " + COUNTER + " ([Young|Humonguous|Free|Old]+?) regions");
    GCParseRule RS_CODE_ROOTS_REPRESENTED = new GCParseRule("RS_CODE_ROOTS_REPRESENTED", COUNTER + " code roots represented\\.");
    GCParseRule RS_REGION_WITH_LARGEST_CODE_ROOTS = new GCParseRule("RS_REGION_WITH_LARGEST_CODE_ROOTS", "Region with largest amount of code roots = " + COUNTER + ":\\([F|S|E]\\)" + MEMORY_POOL_BOUNDS + ", size = " + MEMORY_SIZE + ", num_elems = " + COUNTER + "\\.");

    /*
    2015-10-17T21:07:37.826-0400: 3827.289: [GC concurrent-string-deduplication, 79.9K->496.0B(79.4K), avg 96.1%, 0.0009247 secs]
   [Last Exec: 0.0009247 secs, Idle: 1.0836873 secs, Blocked: 0/0.0000000 secs]
      [Inspected:            3214]
         [Skipped:              0(  0.0%)]
         [Hashed:            1968( 61.2%)]
         [Known:                0(  0.0%)]
         [New:               3214(100.0%)     79.9K]
      [Deduplicated:         3200( 99.6%)     79.4K( 99.4%)]
         [Young:                0(  0.0%)      0.0B(  0.0%)]
         [Old:               3200(100.0%)     79.4K(100.0%)]
   [Total Exec: 3306/2.8905467 secs, Idle: 3306/3823.4388640 secs, Blocked: 9/0.0054346 secs]
      [Inspected:         9734415]
         [Skipped:              0(  0.0%)]
         [Hashed:         5909768( 60.7%)]
         [Known:            17680(  0.2%)]
         [New:            9716735( 99.8%)    239.6M]
      [Deduplicated:      9495912( 97.7%)    230.2M( 96.1%)]
         [Young:               32(  0.0%)    856.0B(  0.0%)]
         [Old:            9495880(100.0%)    230.2M(100.0%)]
   [Table]
      [Memory Usage: 5862.3K]
      [Size: 131072, Min: 1024, Max: 16777216]
      [Entries: 193404, Load: 147.6%, Cached: 13030, Added: 237300, Removed: 43896]
      [Resize Count: 7, Shrink Threshold: 87381(66.7%), Grow Threshold: 262144(200.0%)]
      [Rehash Count: 0, Rehash Threshold: 120, Hash Seed: 0x0]
      [Age Threshold: 3]
   [Queue]
      [Dropped: 0]
     */

}
