// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

/**
 * Interface that contains String and ParseRule representations of common
 * data types within GC Log data.These tokens are used in Regular Expressions
 * Pattern matching in the GC Log parser(s)
 */
public interface PreUnifiedTokens extends GenericTokens {

    // Time values
    String TIMESTAMP = "(" + INTEGER + DECIMAL_POINT + "\\d{3}): ";
    String MS_TIME_STAMP = TIME + " ms";

    String DATE_STAMP = "(" + DATE + "): ";
    //        String dateStampPattern = "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}-\\d{4}: )";
    //        String timeStampPattern = "(\\d+.\\d{3}: )";
    //
    //        Pattern combined = Pattern.compile(dateStampPattern + timeStampPattern + "|" + timeStampPattern + "|" + dateStampPattern);
    int TIME_DATE_OFFSET = 6;
    String DATE_TIMESTAMP = "(" + DATE_STAMP + TIMESTAMP + "|" + TIMESTAMP + "|" + DATE_STAMP + ")";
    GCParseRule DATE_TIMESTAMP_RECORD = new GCParseRule("DATE_TIMESTAMP_RECORD", DATE_TIMESTAMP);

    // Memory values
    String META_PERM_MEMORY_SUMMARY = "(?:" + MEMORY_SIZE + "->)" + MEMORY_SIZE + "\\(" + MEMORY_SIZE + "\\)";

    // Fractional memory values
    String FRACTIONAL_MEMORY_SIZE = "(" + REAL_NUMBER + ")" + UNITS;
    String FRACTIONAL_BEFORE_AFTER_CONFIGURED = FRACTIONAL_MEMORY_SIZE + "->" + FRACTIONAL_MEMORY_SIZE + "\\(" + FRACTIONAL_MEMORY_SIZE + "\\)";

    // GC Prefixes
    String GC_ID = "(?:#\\\\d+: )?";
    String GC_PREFIX = DATE_TIMESTAMP + GC_ID + "\\[GC ?" + GC_CAUSE;
    String FULL_GC_PREFIX = DATE_TIMESTAMP + GC_ID + "\\[Full GC ?" + GC_CAUSE;

    // Pre 1.8.0 records
    // Update: no longer only pre 1.8.0, updated to fix Metaspace in CMF records
    String PERM_RECORD = "\\[(CMS Perm |PS Perm |Perm |PSPermGen|Metaspace): " + META_PERM_MEMORY_SUMMARY + "\\]";

    String USED_CAPACITY_COMMITTED_RESERVED = "used " + MEMORY_SIZE + ", capacity " + MEMORY_SIZE + ", committed " + MEMORY_SIZE + ", reserved " + MEMORY_SIZE;
    GCParseRule METASPACE_FINAL = new GCParseRule("METASPACE_FINAL", "Metaspace\\s+" + USED_CAPACITY_COMMITTED_RESERVED);
    GCParseRule CLASSPACE_FINAL = new GCParseRule("CLASSPACE_FINAL", "class space\\s+" + USED_CAPACITY_COMMITTED_RESERVED);

    // HeapAtGC tokens
    String MEMORY_ADDRESS = "(0x[0-9,a-f]{16})";
    String MEMORY_POOL_BOUNDS = "\\[" + MEMORY_ADDRESS + "," + MEMORY_ADDRESS + "," + MEMORY_ADDRESS + "\\]";

    // CPU record
    // [Times: user=7.96 sys=0.03, real=4.38 secs]
    GCParseRule CPU_BREAKDOWN = new GCParseRule("CPU_BREAKDOWN", CPU_SUMMARY);

    // Reference processing block
    // 11906.881: [SoftReference, 0 refs, 0.0000060 secs]
    // 11906.881: [WeakReference, 0 refs, 0.0000020 secs]
    // 11906.881: [FinalReference, 0 refs, 0.0000010 secs]
    // 11906.881: [PhantomReference, 0 refs, 0.0000020 secs]
    String REFERENCE_PROCESSING_BLOCK = DATE_TIMESTAMP + "\\[(Soft|Weak|Final)Reference, " + COUNTER + " refs, " + PAUSE_TIME + "\\]";
    GCParseRule SOFT_REFERENCE = new GCParseRule("SOFT_REFERENCE", DATE_TIMESTAMP + "\\[SoftReference, " + COUNTER + " refs, " + PAUSE_TIME + "\\]");
    GCParseRule WEAK_REFERENCE = new GCParseRule("WEAK_REFERENCE", DATE_TIMESTAMP + "\\[WeakReference, " + COUNTER + " refs, " + PAUSE_TIME + "\\]");
    GCParseRule FINAL_REFERENCE = new GCParseRule("FINAL_REFERENCE", DATE_TIMESTAMP + "\\[FinalReference, " + COUNTER + " refs, " + PAUSE_TIME + "\\]");

    //[PhantomReference, 0 refs, 0 refs, 0.0006649 secs]
    String PHANTOM_REFERENCE_PROCESSING = DATE_TIMESTAMP + "\\[PhantomReference, " + COUNTER + " refs, (?:" + COUNTER + " refs, )?" + PAUSE_TIME + "\\]";
    GCParseRule PHANTOM_REFERENCE = new GCParseRule("PHANTOM_REFERENCE", PHANTOM_REFERENCE_PROCESSING);
    // JNI Reference processing
    // 11906.881: [JNI Weak Reference, 0.0002710 secs]
    //2.833: [JNI Weak Reference, 239 refs, 0.0000939 secs]"
    String JNI_REFERENCE_PROCESSING = DATE_TIMESTAMP + "\\[JNI Weak Reference, (?:" + COUNTER + " refs, )?" + PAUSE_TIME + "\\]";
    GCParseRule JNI_REFERENCE = new GCParseRule("JNI_REFERENCE", JNI_REFERENCE_PROCESSING);

    String REFERENCE_RECORDS = REFERENCE_PROCESSING_BLOCK + REFERENCE_PROCESSING_BLOCK + REFERENCE_PROCESSING_BLOCK + PHANTOM_REFERENCE_PROCESSING + JNI_REFERENCE_PROCESSING;

    //2016-10-06T08:48:07.320+0200: 2002,085: [Preclean SoftReferences, 0,0000050 secs]2016-10-06T08:48:07.320+0200: 2002,085: [Preclean WeakReferences, 0,0000283 secs]2016-10-06T08:48:07.320+0200: 2002,085: [Preclean FinalReferences, 0,0000036 secs]2016-10-06T08:48:07.320+0200: 2002,085: [Preclean PhantomReferences, 0,0000354 secs]2016-10-06T08:48:07.330+0200: 2002,095: [CMS-concurrent-preclean: 0,010/0,011 secs] [Times: user=0,01 sys=0,00, real=0,01 secs]
    //11.943: [Preclean SoftReferences, 0.0000081 secs]11.943: [Preclean WeakReferences, 0.0000145 secs]11.943: [Preclean FinalReferences, 0.0000276 secs]11.943: [Preclean PhantomReferences, 0.0000081 secs]
    String PRECLEAN_REFERENCE_PROCESSING = DATE_TIMESTAMP + "\\[Preclean (Soft|Weak|Final|Phantom)References, " + PAUSE_TIME + "\\]";
    String PRECLEAN_REFERENCE_RECORDS = PRECLEAN_REFERENCE_PROCESSING + PRECLEAN_REFERENCE_PROCESSING + PRECLEAN_REFERENCE_PROCESSING + PRECLEAN_REFERENCE_PROCESSING;

    //(plab_sz = 17978  desired_plab_sz = 17978)  (plab_sz = 0  desired_plab_sz = 259)
    String PLAB_RECORD = "\\(plab_sz = " + COUNTER + "\\s+desired_plab_sz = " + COUNTER + "\\)";
    // PrintOldPLAB
    // 0[3]: 6/61264/16
    String PLAB = COUNTER + "\\[" + COUNTER + "\\]: " + COUNTER + "\\/" + COUNTER + "\\/" + COUNTER;

    //PrintReferenceGC
    //[SoftReference, 0 refs, 0.0006965 secs]7.643: [WeakReference, 40 refs, 0.0003039 secs]7.643: [FinalReference, 3477 refs, 0.0036191 secs]7.647: [PhantomReference, 0 refs, 0 refs, 0.0006649 secs]7.647: [JNI Weak Reference, 0.0000119 secs]


}
