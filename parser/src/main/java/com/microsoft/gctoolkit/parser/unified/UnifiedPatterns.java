// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unified;


import com.microsoft.gctoolkit.parser.GCLogParser;
import com.microsoft.gctoolkit.parser.GCParseRule;

public interface UnifiedPatterns extends UnifiedLoggingTokens {

    GCParseRule CPU_BREAKOUT = new GCParseRule("CPU_BREAKOUT", "User=" + TIME + "s Sys=" + TIME + "s Real=" + TIME + "s");

    GCParseRule GC_COUNT = new GCParseRule("GC_COUNT", " GC\\((" + INTEGER + ")\\) ");

    String WORKER_TASK = "(for evacuation|to rebuild remembered set)";
    GCParseRule WORKER_SUMMARY = new GCParseRule("WORKER_SUMMARY", "Using " + COUNTER + " workers of " + COUNTER + " " + WORKER_TASK);

    GCParseRule REFERENCES = new GCParseRule("REFERENCES", "(Preclean )?(SoftReference|WeakReference|FinalReference|PhantomReference|JNI Weak Reference)(:?s)? " + PAUSE_TIME);
    GCParseRule REFERENCE_COUNTS = new GCParseRule("REFERENCE_COUNTS", "Ref Counts: Soft: " + COUNTER + " Weak: " + COUNTER + " Final: " + COUNTER + " Phantom: " + COUNTER);

    GCParseRule UNIFIED_META_DATA = new GCParseRule("UNIFIED_META_DATA", UNIFIED_META_RECORD);

    GCParseRule END_OF_FILE = new GCParseRule("END_OF_FILE", GCLogParser.END_OF_DATA_SENTINEL);
    GCParseRule JVM_EXIT = new GCParseRule("JVM_EXIT", "Heap$");

}
