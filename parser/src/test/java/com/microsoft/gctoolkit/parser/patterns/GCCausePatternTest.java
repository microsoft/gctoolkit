package com.microsoft.gctoolkit.parser.patterns;

import com.microsoft.gctoolkit.parser.GCLogTrace;
import com.microsoft.gctoolkit.parser.GCParseRule;
import com.microsoft.gctoolkit.parser.GenericTokens;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GCCausePatternTest implements GenericTokens {

    String[] gcCauses = {
            "(System.gc())",
            "(Diagnostic Command)",
            "(FullGCALot)",
            "(ScavengeAlot)",
            "(Allocation Profiler)",
            "(JvmtiEnv ForceGarbageCollection)",
            "(GCLocker Initiated GC)",
            "(Heap Inspection Initiated GC)",
            "(Heap Dump Initiated GC)",
            "(No GC)",
            "(Allocation Failure)",
            "(Tenured Generation Full)",
            "(Metadata GC Threshold)",
            "(Permanent Generation Full)",
            "(CMS Generation Full)",
            "(CMS Initial Mark)",
            "(CMS Final Remark)",
            "(CMS Concurrent Mark)",
            "(CMS Failure)",
            "(Old Generation Expanded On Last Scavenge)",
            "(Old Generation Too Full To Scavenge)",
            "(Ergonomics)",
            "(G1 Evacuation Pause)",
            "(G1 Humongous Allocation)",
            "(Last ditch collection)",
            "(ILLEGAL VALUE - last gc cause - ILLEGAL VALUE)",
            "(unknown GCCause)",
            "(promotion failed)",
            "(Update Allocation Context Stats)",
            "(Missing GC Cause)",
            "(Concurrent Mark Stack Overflow)",
            "(young)",
            "(WhiteBox Initiated Young GC)",
            "(WhiteBox Initiated Concurrent Mark)",
            "(WhiteBox Initiated Full GC)",
            "(Metadata GC Clear Soft References)",
            "(Timer)",
            "(Warmup)",
            "(Allocation Rate)",
            "(Allocation Stall)",
            "(Proactive)",
            "(G1 Preventive Collection)"
    };

    String[] extraText = {
            "(System.gc()) xxx",
            "(Diagnostic Command) xxx",
            "(ScavengeAlot) xxx",
            "(GCLocker Initiated GC) xxx",
            "(Allocation Failure) xxx",
            "(Metadata GC Threshold) xxx",
            "(CMS Initial Mark) xxx",
            "(CMS Final Remark) xxx",
            "(CMS Concurrent Mark) xxx",
            "(Old Generation Expanded On Last Scavenge) xxx",
            "(Ergonomics) xxx",
            "(G1 Evacuation Pause) xxx",
            "(G1 Humongous Allocation) xxx",
            "(ILLEGAL VALUE - last gc cause - ILLEGAL VALUE) xxx"
    };

    @Test
    public void matchesAllGCCauses() {
        String local = "\\([G1,A-Z,a-z, ,-,.gc\\(\\)]+\\)";
        //\([G1,A-Z,a-z, ,-]+\)
        //GCParseRule cause = new GCParseRule("GC_CAUSE", local);
        GCParseRule cause = new GCParseRule("GC_CAUSE", GC_CAUSE);
        for (int i = 0; i < gcCauses.length; i++) {
            assertNotNull(cause.parse(gcCauses[i]));
        }
    }

    @Test
    public void onlyMatchCause() {
        GCParseRule cause = new GCParseRule("GC_CAUSE", GC_CAUSE);
        for (String s : extraText) {
            GCLogTrace trace = cause.parse(s);
            assertNotNull(trace);
            assertFalse(trace.contains("xxx"));
        }
    }
}
