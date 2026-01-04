package com.microsoft.gctoolkit.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GCLogParserTest {
    @Test
    void extractPackedGCCycleIdAndTextualLength() {
        long packed = GCLogParser.extractGCCycleIdAndTextualLength("[2025-10-21T16:44:29.311+0200][3645.640s] GC(35) Pause Young (Allocation Failure)");
        assertEquals(35, GCLogParser.extractGCCycleId(packed));
        assertEquals(48, GCLogParser.extractGCCycleIdTextualLength(packed));
    }

    @Test
    void extractGCID() {
        assertEquals(35, GCLogParser.extractGCID("[2025-10-21T16:44:29.311+0200][3645.640s] GC(35) Pause Young (Allocation Failure)"));
    }

    @Test
    void extractPackedGCCycleIdAndTextualLength_malformed() {
        long packed = GCLogParser.extractGCCycleIdAndTextualLength("[2025-10-21T16:44:29.311+0200][3645.640s] GC(3");
        assertEquals(-1 , packed);
        assertEquals(-1, GCLogParser.extractGCCycleId(packed));
        assertEquals(0, GCLogParser.extractGCCycleIdTextualLength(packed));
    }

    @Test
    void extractGCID_malformed() {
        assertEquals(-1, GCLogParser.extractGCID("[2025-10-21T16:44:29.311+0200][3645.640s] GC(3"));
    }
}
