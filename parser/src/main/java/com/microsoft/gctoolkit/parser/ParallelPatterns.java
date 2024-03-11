// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

public interface ParallelPatterns extends SharedPatterns {

    String PS_BLOCK = "\\[(PSYoungGen|ParOldGen|Tenured|PSOldGen): " + BEFORE_AFTER_CONFIGURED + "\\]";
    String TENURED_BLOCK = "\\[Tenured: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]";

    //2017-03-28T12:17:34.744+0200: 1.895: [GC (Allocation Failure)  137969K->21806K(491008K), 0.0082985 secs]
    //"2017-03-28T12:17:33.823+0200: 0.974: [GC (System.gc())  92578K->9947K(491008K), 0.0135426 secs]"
    GCParseRule PSYOUNGGEN_NO_DETAILS = new GCParseRule("PSYOUNGGEN_NO_DETAILS", GC_PREFIX + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]$");

    //939.183: [GC [PSYoungGen: 523744K->844K(547584K)] 657668K->135357K(1035008K), 0.0157986 secs] [Times: user=0.30 sys=0.01, real=0.02 secs]
    GCParseRule PSYOUNGGEN = new GCParseRule("PSYOUNGGEN", GC_PREFIX + PS_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");
    //"2017-08-17T08:14:22.677-0500: 8179.905: [GC (Allocation Failure) --[PSYoungGen: 3990443K->3990443K(4030464K)] 5012990K->5013022K(5079040K), 0.5837002 secs] [Times: user=0.67 sys=0.00, real=0.58 secs] \n"
    GCParseRule PSYOUNGGEN_PROMOTION_FAILED = new GCParseRule("PSYOUNGGEN_PROMOTION_FAILED", GC_PREFIX + "--" + PS_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");
    GCParseRule PSFULL = new GCParseRule("PSFULL", FULL_GC_PREFIX + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]$");
    //1.090: [GC (Allocation Failure) 1.095: [SoftReference, 0 refs, 0.0000208 secs]1.095: [WeakReference, 1098 refs, 0.0000495 secs]1.095: [FinalReference, 1518 refs, 0.0010004 secs]1.096: [PhantomReference, 0 refs, 1 refs, 0.0000041 secs]1.096: [JNI Weak Reference, 0.0000084 secs]
    // [PSYoungGen: 34275K->2536K(34304K)] 36334K->8407K(76800K), 0.0073027 secs] [Times: user=0.01 sys=0.00, real=0.01 secs]
    GCParseRule PSYOUNGGEN_REFERENCE = new GCParseRule("PSYOUNGGEN_REFERENCE", GC_PREFIX + REFERENCE_RECORDS + PS_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE);
    //18.814: [GC (System.gc()) 18.819: [SoftReference, 0 refs, 0.0000308 secs]18.819: [WeakReference, 305 refs, 0.0000138 secs]18.819: [FinalReference, 1504 refs, 0.0013137 secs]18.821: [PhantomReference, 0 refs, 0 refs, 0.0000059 secs]18.821: [JNI Weak Reference, 0.0000067 secs]
    GCParseRule PSYOUNGGEN_REFERENCE_SPLIT = new GCParseRule("PSYOUNGGEN_REFERENCE_SPLIT", GC_PREFIX + REFERENCE_RECORDS + "$");
    GCParseRule PS_FULL_REFERENCE_SPLIT = new GCParseRule("PS_FULL_REFERENCE_SPLIT", FULL_GC_PREFIX + REFERENCE_RECORDS + "$");
    //43446.876: [Full GC (System.gc()) 43446.876: [Tenured43447.145: [SoftReference, 347 refs, 0.0000739 secs]43447.146: [WeakReference, 637 refs, 0.0000575 secs]43447.146: [FinalReference, 460 refs, 0.0004814 secs]43447.146: [PhantomReference, 0 refs, 4 refs, 0.0000047 secs]43447.146: [JNI Weak Reference, 0.0000713 secs]: 290960K->292298K(699072K), 0.6541540 secs] 460438K->292298K(1013824K), [Metaspace: 99359K->99359K(1140736K)], 0.6543427 secs] [Times: user=0.64 sys=0.00, real=0.65 secs]
    //47047.534: [Full GC (System.gc()) 47047.534: [Tenured47047.808: [SoftReference, 258 refs, 0.0000630 secs]47047.808: [WeakReference, 628 refs, 0.0000561 secs]47047.808: [FinalReference, 170 refs, 0.0001564 secs]47047.808: [PhantomReference, 0 refs, 3 refs, 0.0000039 secs]47047.808: [JNI Weak Reference, 0.0000800 secs]: 292298K->278286K(699072K), 0.7450752 secs] 350258K->278286K(1013824K), [Metaspace: 99239K->99239K(1140736K)], 0.7451777 secs] [Times: user=0.74 sys=0.00, real=0.75 secs]
    GCParseRule PS_FULL_REFERENCE_8 = new GCParseRule("PS_FULL_REFERENCE_8", FULL_GC_PREFIX + " " + DATE_TIMESTAMP + "\\[Tenured" + REFERENCE_RECORDS + ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME);
    /*
    34.518: [Full GC (System.gc()) 34.518: [Tenured34.525: [SoftReference, 0 refs, 0.0000520 secs]34.525: [WeakReference, 479 refs, 0.0000505 secs]34.525: [FinalReference, 1051 refs, 0.0000654 secs]34.525: [PhantomReference, 0 refs, 1 refs, 0.0000067 secs]34.525: [JNI Weak Reference, 0.0000216 secs]: 3614K->5193K(174784K), 0.0235099 secs] 14881K->5193K(253440K), [Metaspace: 16161K->16161K(1064960K)], 0.0236206 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
     */
    GCParseRule SERIAL_FULL_REFERENCE = new GCParseRule("SERIAL_FULL_REFERENCE", FULL_GC_PREFIX + DATE_TIMESTAMP + "\\[Tenured" + REFERENCE_RECORDS + ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + "\\, " + PERM_RECORD + ", " + PAUSE_TIME);
    GCParseRule PS_FULL_REFERENCE = new GCParseRule("PS_FULL_REFERENCE", FULL_GC_PREFIX + REFERENCE_RECORDS + "\\s*" + PS_BLOCK + " \\[(?:PSFull|PSOldGen|ParOldGen): " + BEFORE_AFTER_CONFIGURED + "\\] " + BEFORE_AFTER_CONFIGURED + "(?:,)? " + PERM_RECORD + ", " + PAUSE_TIME);

    //Moronic HotSpot/GC developers decided on yet another arbitrary format change... Why we need the comma... beyond me!
    //22.014: [Full GC (Metadata GC Threshold) [PSYoungGen: 16865K->0K(107008K)] [ParOldGen: 108907K->103881K(124416K)] 125773K->103881K(231424K), [Metaspace: 21089K->21089K(1067008K)], 0.2715010 secs]
    //0.221: [Full GC (Ergonomics) [PSYoungGen: 10736K->0K(76288K)] [ParOldGen: 120258K->115108K(175104K)] 130995K->115108K(251392K), [Metaspace: 4209K->4207K(1056768K)], 0.0235254 secs]
    //42384.024: [Full GC [PSYoungGen: 1696K->0K(70464K)] [PSFull: 928442K->125867K(932096K)] 930139K->125867K(1002560K) [PSPermGen: 37030K->37030K(65536K)], 117.0312620 secs]
    GCParseRule PS_FULL_GC_PERM = new GCParseRule("PS_FULL_GC_PERM", FULL_GC_PREFIX + PS_BLOCK + " \\[(?:PSFull|PSOldGen|ParOldGen): " + BEFORE_AFTER_CONFIGURED + "\\] " + BEFORE_AFTER_CONFIGURED + " " + PERM_RECORD + ", " + PAUSE_TIME);
    GCParseRule PS_FULL_GC_META = new GCParseRule("PS_FULL_GC_META", FULL_GC_PREFIX + PS_BLOCK + " \\[(?:PSFull|PSOldGen|ParOldGen): " + BEFORE_AFTER_CONFIGURED + "\\] " + BEFORE_AFTER_CONFIGURED + "(?:,)? " + META_RECORD + ", " + PAUSE_TIME);

    //1.147: [Full GC (System) 1.147: [Tenured: 1636K->1765K(5312K), 0.0488106 secs] 2894K->1765K(7616K), [Perm : 10112K->10112K(21248K)], 0.0488934 secs]
    GCParseRule PS_FULL_GC_V2_PERM = new GCParseRule("PS_FULL_GC_V2_PERM", FULL_GC_PREFIX + TIMESTAMP + "\\[(PSYoungGen|ParOldGen|Tenured): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME);
    GCParseRule PS_FULL_GC_V2_META = new GCParseRule("PS_FULL_GC_V2_META", FULL_GC_PREFIX + TIMESTAMP + "\\[(PSYoungGen|ParOldGen|Tenured): " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + META_RECORD + ", " + PAUSE_TIME);

    //: 4864K->512K(4864K), 0.0107187 secs]4.203: [Tenured: 11310K->10683K(11392K), 0.1046824 secs] 14990K->10683K(16256K), [Perm : 9880K->9880K(12288K)], 0.1157555 secs] [Times: user=0.11 sys=0.00, real=0.11 secs]
    GCParseRule PS_PROMOTION_FAILED = new GCParseRule("PS_PROMOTION_FAILED", "^: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + TIMESTAMP + TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME + "\\]");

    //2011-02-12T14:13:59.378+0000: 50128.698: [GC
    //[PSYoungGen: 2763743K->13920K(2766144K)] 8155872K->5406505K(8358592K), 0.0572110 secs]
    //12288K->1648K(47104K), 0.0084652 secs]
    GCParseRule PS_TENURING_START = new GCParseRule("PS_TENURING_START", GC_PREFIX + "$");
    GCParseRule PSFULL_SPLIT = new GCParseRule("PSFULL_SPLIT", FULL_GC_PREFIX + "$");
    GCParseRule PS_DETAILS_WITH_TENURING = new GCParseRule("PS_DETAILS_WITH_TENURING", "^" + PS_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");
    GCParseRule PS_SIMPLE_TENURING_END = new GCParseRule("PS_SIMPLE_TENURING_END", "^" + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");

    //13.056: [GC-- [PSYoungGen: 4194240K->4194240K(4194240K)] 4280449K->4360823K(4361088K), 0.4589570 secs]
    GCParseRule PS_FAILURE = new GCParseRule("PS_FAILURE", GC_PREFIX + "-- " + PS_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");

    /*
        260962.621: [Full GC260962.902: [SoftReference, 8776 refs, 0.0013422 secs]260962.904: [WeakReference, 36789 refs, 0.0037704 secs]260962.907: [FinalReference, 1676 refs, 0.0048222 secs]260962.912: [PhantomReference, 0 refs, 279 refs, 0.0000250 secs]260962.912: [JNI Weak Reference, 0.0000196 secs]AdaptiveSizeStart: 260963.411 collection: 14063
        81.705: [GC (Allocation Failure) AdaptiveSizePolicy::update_averages:  survived: 116398488  promoted: 90128  overflow: false
        2015-03-01T08:50:29.070-0500: 127157.424: [Full GC (System)AdaptiveSizeStart: 127187.287 collection: 298
        AdaptiveSizeStart: 81.832 collection: 1
        PSAdaptiveSizePolicy::compute_eden_space_size: costs minor_time: 0.001543 major_cost: 0.000000 mutator_cost: 0.998457 throughput_goal: 0.990000 live_space: 384833952 free_space: 4296015872 old_eden_size: 2148007936 desired_eden_size: 2148007936
        AdaptiveSizeStop: collection: 1
        [PSYoungGen: 2097664K->113670K(2446848K)] 2097664K->113758K(8039424K), 0.1274148 secs] [Times: user=0.57 sys=1.03, real=0.13 secs]
     */
    //1.8.0 Adaptive sizing
    GCParseRule PSYOUNG_ADAPTIVE_SIZE_POLICY = new GCParseRule("PSYOUNG_ADAPTIVE_SIZE_POLICY", GC_PREFIX + "(" + REFERENCE_RECORDS + ")?\\s*AdaptiveSizePolicy::");
    GCParseRule PSOLD_ADAPTIVE_SIZE_POLICY = new GCParseRule("PSOLD_ADAPTIVE_SIZE_POLICY", FULL_GC_PREFIX + " AdaptiveSizePolicy::");
    GCParseRule PSYOUNG_ADAPTIVE_SIZE_POLICY_START = new GCParseRule("PSYOUNG_ADAPTIVE_SIZE_POLICY_START", "^AdaptiveSizeStart: ");
    GCParseRule PS_ADAPTIVE_SIZE_POLICY_BODY = new GCParseRule("PS_ADAPTIVE_SIZE_POLICY_BODY", "^PSAdaptiveSizePolicy::");
    GCParseRule ADAPTIVE_SIZE_POLICY_BODY = new GCParseRule("ADAPTIVE_SIZE_POLICY_BODY", "^AdaptiveSizePolicy::");
    GCParseRule ADAPTIVE_SIZE_POLICY_STOP = new GCParseRule("ADAPTIVE_SIZE_POLICY_STOP", "^AdaptiveSizeStop: collection: ");
    GCParseRule PSYOUNG_DETAILS_FLOATING = new GCParseRule("PSYOUNG_DETAILS_FLOATING", "^" + PS_BLOCK + " " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");
    GCParseRule FULL_REFERENCE_ADAPTIVE_SIZE = new GCParseRule("FULL_REFERENCE_ADAPTIVE_SIZE", FULL_GC_PREFIX + REFERENCE_RECORDS + "AdaptiveSizeStart");

    //Full GC
    /*
        457.459: [Full GC (System.gc()) AdaptiveSizeStart: 457.499 collection: 5682
        AdaptiveSizeStop: collection: 5682
        [PSYoungGen: 192K->0K(428032K)] [ParOldGen: 7054K->7020K(73216K)] 7246K->7020K(501248K), [Metaspace: 16195K->16195K(1064960K)], 0.0399825 secs] [Times: user=0.19 sys=0.01, real=0.04 secs]
     */
    GCParseRule PSFULL_ADAPTIVE_SIZE = new GCParseRule("PSFULL_ADAPTIVE_SIZE", FULL_GC_PREFIX + "AdaptiveSizeStart: ");
    GCParseRule PS_FULL_BODY_FLOATING = new GCParseRule("PS_FULL_BODY_FLOATING", "^" + PS_BLOCK + " " + PS_BLOCK + " " + BEFORE_AFTER_CONFIGURED + "(?:,)? " + PERM_RECORD + ", " + PAUSE_TIME);
    GCParseRule PSFULL_ERGONOMICS_PHASES = new GCParseRule("PSFULL_ERGONOMICS_PHASES", FULL_GC_PREFIX + DATE_TIMESTAMP + "\\[marking phase");
    GCParseRule PSFULL_REFERENCE_PHASE = new GCParseRule("PSFULL_REFERENCE_PHASE", DATE_TIMESTAMP + "\\[reference processing" + DATE_TIMESTAMP);

}
