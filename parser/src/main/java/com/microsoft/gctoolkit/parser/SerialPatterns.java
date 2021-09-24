// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

public interface SerialPatterns extends SharedPatterns {

    //DefNew patterns
    //48.021: [GC48.021: [ParNew: 306686K->34046K(306688K), 0.3196120 secs] 1341473K->1125818K(8669952K), 0.3197540 secs]
    GCParseRule DEFNEW = new GCParseRule("DEFNEW", "^(" + GC_PREFIX + ")?" + DATE_TIMESTAMP + "\\[DefNew: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");

    //9.811: [GC 9.811: [ParNew
    GCParseRule DEFNEW_TENURING = new GCParseRule("DEFNEW_TENURING", GC_PREFIX + TIMESTAMP + "\\[DefNew$");

    //2019-10-22T23:41:21.852+0000: 21.912: [GC (GCLocker Initiated GC) 2019-10-22T23:41:21.853+0000: 21.912: [DefNew2019-10-22T23:41:21.914+0000: 21.974: [SoftReference, 0 refs, 0.0000842 secs]2019-10-22T23:41:21.914+0000: 21.974: [WeakReference, 76 refs, 0.0000513 secs]2019-10-22T23:41:21.914+0000: 21.974: [FinalReference, 91635 refs, 0.0396861 secs]2019-10-22T23:41:21.954+0000: 22.014: [PhantomReference, 0 refs, 3 refs, 0.0000444 secs]2019-10-22T23:41:21.954+0000: 22.014: [JNI Weak Reference, 0.0000281 secs]: 419520K->19563K(471936K), 0.1019514 secs] 502104K->102148K(2044800K), 0.1020469 secs] [Times: user=0.09 sys=0.01, real=0.10 secs]
    GCParseRule DEFNEW_DETAILS = new GCParseRule("DEF_NEW_DETAILS", GC_PREFIX + " " + DATE_TIMESTAMP + "\\[DefNew" + REFERENCE_RECORDS + ": " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\] " + "\\[Times: user=" + REAL_VALUE + " sys=" + REAL_VALUE + ", real=" + PAUSE_TIME + "\\]");

}
