// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.parser.unified;


import com.microsoft.censum.parser.GCLogParser;
import com.microsoft.censum.parser.GCParseRule;

public interface UnifiedPatterns extends UnifiedLoggingTokens {

    GCParseRule CPU_BREAKOUT = new GCParseRule("CPU_BREAKOUT", "User=" + TIME + "s Sys=" + TIME + "s Real=" + TIME + "s");

    GCParseRule GC_COUNT = new GCParseRule("GC_COUNT", " GC\\((" + INTEGER + ")\\) ");

    String WORKER_TASK = "(for evacuation|to rebuild remembered set)";
    GCParseRule WORKER_SUMMARY = new GCParseRule("WORKER_SUMMARY", "Using " + COUNTER + " workers of " + COUNTER + " " + WORKER_TASK);

    GCParseRule REFERENCES = new GCParseRule("REFERENCES", "(SoftReference|WeakReference|FinalReference|PhantomReference|JNI Weak Reference) " + PAUSE_TIME);
    GCParseRule REFERENCE_COUNTS = new GCParseRule("REFERENCE_COUNTS", "Ref Counts: Soft: " + COUNTER + " Weak: " + COUNTER + " Final: " + COUNTER + " Phantom: " + COUNTER);

    GCParseRule UNIFIED_META_DATA = new GCParseRule("UNIFIED_META_DATA", UNIFIED_META_RECORD);

    GCParseRule END_OF_FILE = new GCParseRule("END_OF_FILE", GCLogParser.END_OF_DATA_SENTINAL);
    GCParseRule JVM_EXIT = new GCParseRule("JVM_EXIT", "Heap$");

}
