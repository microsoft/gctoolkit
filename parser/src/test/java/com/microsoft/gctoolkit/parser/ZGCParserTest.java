// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.zgc.ZGCAllocatedSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCFullCollection;
import com.microsoft.gctoolkit.event.zgc.ZGCGarbageSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCLiveSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMemoryPoolSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMemorySummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMetaspaceSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCOldCollection;
import com.microsoft.gctoolkit.event.zgc.ZGCPhase;
import com.microsoft.gctoolkit.event.zgc.ZGCReclaimedSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCYoungCollection;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ZGCParserTest extends ParserTest {

    final long M = 1024;

    @Override
    protected Diarizer diarizer() {
        return new UnifiedDiarizer();
    }

    protected GCLogParser parser() {
        return new ZGCParser();
    }

    @Test
    public void infoLevelZGCCycle() {

        String[] eventLogEntries = {
                "[32.121s][info][gc,start    ] GC(2) Garbage Collection (Metadata GC Threshold)",
                "[32.121s][info][gc,phases   ] GC(2) Pause Mark Start 0.023ms",
                "[32.166s][info][gc,phases   ] GC(2) Concurrent Mark 44.623ms",
                "[32.166s][info][gc,phases   ] GC(2) Pause Mark End 0.029ms",
                "[32.166s][info][gc,phases   ] GC(2) Concurrent Mark Free 0.001ms",
                "[32.172s][info][gc,phases   ] GC(2) Concurrent Process Non-Strong References 5.797ms",
                "[32.172s][info][gc,phases   ] GC(2) Concurrent Reset Relocation Set 0.012ms",
                "[32.178s][info][gc,phases   ] GC(2) Concurrent Select Relocation Set 6.446ms",
                "[32.179s][info][gc,phases   ] GC(2) Pause Relocate Start 0.024ms",
                "[32.193s][info][gc,phases   ] GC(2) Concurrent Relocate 14.013ms",
                "[32.193s][info][gc,load     ] GC(2) Load: 7.28/6.63/5.01",
                "[32.193s][info][gc,mmu      ] GC(2) MMU: 2ms/98.2%, 5ms/99.3%, 10ms/99.5%, 20ms/99.7%, 50ms/99.9%, 100ms/99.9%",
                "[32.193s][info][gc,marking  ] GC(2) Mark: 4 stripe(s), 3 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s)",
                "[32.193s][info][gc,marking  ] GC(2) Mark Stack Usage: 32M",
                "[32.193s][info][gc,metaspace] GC(2) Metaspace: 60M used, 60M committed, 1080M reserved",
                "[32.193s][info][gc,ref      ] GC(2) Soft: 5447 encountered, 0 discovered, 0 enqueued",
                "[32.193s][info][gc,ref      ] GC(2) Weak: 5347 encountered, 2016 discovered, 810 enqueued",
                "[32.193s][info][gc,ref      ] GC(2) Final: 1041 encountered, 113 discovered, 105 enqueued",
                "[32.193s][info][gc,ref      ] GC(2) Phantom: 558 encountered, 501 discovered, 364 enqueued",
                "[32.193s][info][gc,reloc    ] GC(2) Small Pages: 235 / 470M, Empty: 32M, Relocated: 40M, In-Place: 0",
                "[32.193s][info][gc,reloc    ] GC(2) Medium Pages: 2 / 64M, Empty: 0M, Relocated: 3M, In-Place: 0",
                "[32.193s][info][gc,reloc    ] GC(2) Large Pages: 3 / 24M, Empty: 8M, Relocated: 0M, In-Place: 0",
                "[32.193s][info][gc,reloc    ] GC(2) Forwarding Usage: 13M",
                "[32.193s][info][gc,heap     ] GC(2) Min Capacity: 8M(0%)",
                "[32.193s][info][gc,heap     ] GC(2) Max Capacity: 28686M(100%)",
                "[32.193s][info][gc,heap     ] GC(2) Soft Max Capacity: 28686M(100%)",
                "[32.193s][info][gc,heap     ] GC(2)                Mark Start          Mark End        Relocate Start      Relocate End           High               Low         ",
                "[32.193s][info][gc,heap     ] GC(2)  Capacity:     1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)",
                "[32.193s][info][gc,heap     ] GC(2)      Free:    28128M (98%)       28110M (98%)       28148M (98%)       28560M (100%)      28560M (100%)      28108M (98%)",
                "[32.193s][info][gc,heap     ] GC(2)      Used:      558M (2%)          576M (2%)          538M (2%)          126M (0%)          578M (2%)          126M (0%)",
                "[32.193s][info][gc,heap     ] GC(2)      Live:         -                71M (0%)           71M (0%)           71M (0%)             -                  -          ",
                "[32.193s][info][gc,heap     ] GC(2) Allocated:         -                18M (0%)           20M (0%)           18M (0%)             -                  -          ",
                "[32.193s][info][gc,heap     ] GC(2)   Garbage:         -               486M (2%)          446M (2%)           35M (0%)             -                  -          ",
                "[32.193s][info][gc,heap     ] GC(2) Reclaimed:         -                  -                40M (0%)          450M (2%)             -                  -          ",
                "[32.193s][info][gc          ] GC(2) Garbage Collection (Metadata GC Threshold) 558M(2%)->126M(0%)",

        };

        List<JVMEvent> singleCycle = feedParser(eventLogEntries);
        try {
            assertEquals(1, singleCycle.size());
            ZGCFullCollection zgc = (ZGCFullCollection) singleCycle.get(0);

            assertEquals(zgc.getGcId(), 2L);

            assertEquals(0.0710d, zgc.getDuration(),0.001d);
            assertEquals(toInt(32.121d, 1000), toInt(zgc.getDateTimeStamp().toSeconds(), 1000));
            assertEquals("Metadata GC Threshold", zgc.getGCCause().getLabel());
            // Durations
            assertEquals(32.121d, zgc.getPauseMarkStartTimeStamp().toSeconds(),  0.001d);
            assertEquals(32.122d, zgc.getConcurrentMarkTimeStamp().toSeconds(), 0.001d);
            assertEquals(32.166d, zgc.getConcurrentMarkFreeTimeStamp().toSeconds(), 0.001d);
            assertEquals(32.166d, zgc.getPauseMarkEndTimeStamp().toSeconds(), 0.001d);
            assertEquals(32.167d, zgc.getConcurrentProcessNonStrongReferencesTimeStamp().toSeconds(), 0.002d);
            assertEquals(32.172d, zgc.getConcurrentResetRelocationSetTimeStamp().toSeconds(), 0.001d);
            assertEquals(32.172d, zgc.getConcurrentSelectRelocationSetTimeStamp().toSeconds(), 0.001d);
            assertEquals(32.179d, zgc.getPauseRelocateStartTimeStamp().toSeconds(), 0.001d);
            assertEquals(32.179d, zgc.getConcurrentRelocateTimeStamp().toSeconds(), 0.001d);

            assertEquals(toInt(0.023d, 1000), toInt(zgc.getPauseMarkStartDuration(), 1000));
            assertEquals(toInt(44.623d, 1000), toInt(zgc.getConcurrentMarkDuration(), 1000));
            assertEquals(toInt(0.001d, 1000), toInt(zgc.getConcurrentMarkFreeDuration(), 1000));
            assertEquals(toInt(0.029d, 1000), toInt(zgc.getPauseMarkEndDuration(), 1000));
            assertEquals(toInt(5.797d, 1000), toInt(zgc.getConcurrentProcessNonStrongReferencesDuration(), 1000));
            assertEquals(toInt(0.012d, 1000), toInt(zgc.getConcurrentResetRelocationSetDuration(), 1000));
            assertEquals(toInt(6.446d, 1000), toInt(zgc.getConcurrentSelectRelocationSetDuration(), 1000));
            assertEquals(toInt(0.024d, 1000), toInt(zgc.getPauseRelocateStartDuration(), 1000));
            assertEquals(toInt(14.013d, 1000), toInt(zgc.getConcurrentRelocateDuration(), 1000));

            //Memory
            assertTrue(checkZGCMemoryPoolSummary(zgc.getMarkStart(), 1837056, 28803072, 571392));
            assertTrue(checkZGCMemoryPoolSummary(zgc.getMarkEnd(), 1837056, 28784640, 589824 ));
            assertTrue(checkZGCMemoryPoolSummary(zgc.getRelocateStart(),1837056, 28823552, 550912));
            assertTrue(checkZGCMemoryPoolSummary(zgc.getRelocateEnd(), 1837056, 29245440, 129024));

            assertTrue(checkZGCMetaSpaceSummary(zgc.getMetaspaceSummary(),61440, 61440, 1105920));

            assertTrue(checkLiveSummary(zgc.getLiveSummary(), 72704, 72704, 72704));
            assertTrue(checkAllocatedSummary(zgc.getAllocatedSummary(), 18432, 20480, 18432));
            assertTrue(checkGarbageSummary(zgc.getGarbageSummary(), 497664, 456704, 35840));

            assertTrue(checkReclaimSummary(zgc.getReclaimedSummary(), 40960, 460800));
            assertTrue(checkMemorySummary(zgc.getMemorySummary(), 571392, 129024));

            assertEquals(7.28, zgc.getLoadAverageAt(1));
            assertEquals(6.63, zgc.getLoadAverageAt(5));
            assertEquals(5.01, zgc.getLoadAverageAt(15));

            assertEquals(98.2, zgc.getMMU(2));
            assertEquals(99.3, zgc.getMMU(5));
            assertEquals(99.5, zgc.getMMU(10));
            assertEquals(99.7, zgc.getMMU(20));
            assertEquals(99.9, zgc.getMMU(50));
            assertEquals(99.9, zgc.getMMU(100));

        } catch (Throwable t) {
            fail(t);
        }
    }
    
    @Test
	void testLegacyZGCNoDetailsLines() {
		String[] lines = {
				// 0 - FullZGCCycle
				"[1.295s][info][gc] GC(0) Garbage Collection (Metadata GC Threshold) 90M(1%)->58M(1%)",

				// 1 - FullZGCCycle
				"[23.507s][info][gc] GC(3) Garbage Collection (Warmup) 826M(10%)->168M(2%)"
		};

        int expectedEventCount = 2;

        List<JVMEvent> jvmEvents = feedParser(lines);
        assertEquals(expectedEventCount, jvmEvents.size());

        // 0 - ZGCFullCollection
        assertTrue(jvmEvents.get(0) instanceof ZGCFullCollection); 
        ZGCFullCollection evt0 = (ZGCFullCollection) jvmEvents.get(0);
        assertEquals(evt0.getDateTimeStamp(), new DateTimeStamp(1.295));
        assertEquals(evt0.getGCCause(), GCCause.METADATA_GENERATION_THRESHOLD);
        assertZGCMemorySummary(evt0.getMemorySummary(), 90*M, 58*M);

        // 1 - ZGCFullCollection
        assertTrue(jvmEvents.get(1) instanceof ZGCFullCollection); 
        ZGCFullCollection evt1 = (ZGCFullCollection) jvmEvents.get(1);
        assertEquals(new DateTimeStamp(23.507), evt1.getDateTimeStamp());
        assertEquals(GCCause.WARMUP, evt1.getGCCause());
        assertZGCMemorySummary(evt1.getMemorySummary(), 826*M, 168*M);        
	}

    @Test
    void testLegacyDetailedEvent() {
    	String[] lines = {
    			"[1.641s][info][gc,start    ] GC(0) Garbage Collection (Metadata GC Threshold)",
    			"[1.641s][info][gc,task     ] GC(0) Using 5 workers",
    			"[1.641s][info][gc,phases   ] GC(0) Pause Mark Start 0.006ms",
    			"[1.652s][info][gc,phases   ] GC(0) Concurrent Mark 10.438ms",
    			"[1.652s][info][gc,phases   ] GC(0) Pause Mark End 0.014ms",
    			"[1.652s][info][gc,phases   ] GC(0) Concurrent Mark Free 0.001ms",
    			"[1.653s][info][gc,phases   ] GC(0) Concurrent Process Non-Strong References 1.408ms",
    			"[1.654s][info][gc,phases   ] GC(0) Concurrent Reset Relocation Set 0.000ms",
    			"[1.660s][info][gc,phases   ] GC(0) Concurrent Select Relocation Set 5.975ms",
    			"[1.660s][info][gc,phases   ] GC(0) Pause Relocate Start 0.011ms",
    			"[1.663s][info][gc,phases   ] GC(0) Concurrent Relocate 3.001ms",
    			"[1.663s][info][gc,load     ] GC(0) Load: 0.00/0.00/0.00",
    			"[1.663s][info][gc,mmu      ] GC(0) MMU: 2ms/99.3%, 5ms/99.7%, 10ms/99.8%, 20ms/99.8%, 50ms/99.9%, 100ms/100.0%",
    			"[1.663s][info][gc,marking  ] GC(0) Mark: 4 stripe(s), 2 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s) ",
    			"[1.663s][info][gc,marking  ] GC(0) Mark Stack Usage: 32M",
    			"[1.663s][info][gc,nmethod  ] GC(0) NMethods: 2721 registered, 0 unregistered",
    			"[1.663s][info][gc,metaspace] GC(0) Metaspace: 21M used, 21M committed, 1088M reserved",
    			"[1.663s][info][gc,ref      ] GC(0) Soft: 4085 encountered, 0 discovered, 0 enqueued",
    			"[1.663s][info][gc,ref      ] GC(0) Weak: 2166 encountered, 436 discovered, 349 enqueued",
    			"[1.663s][info][gc,ref      ] GC(0) Final: 0 encountered, 0 discovered, 0 enqueued",
    			"[1.663s][info][gc,ref      ] GC(0) Phantom: 909 encountered, 794 discovered, 410 enqueued",
    			"[1.663s][info][gc,reloc    ] GC(0) Small Pages: 46 / 92M, Empty: 0M, Relocated: 11M, In-Place: 0",
    			"[1.663s][info][gc,reloc    ] GC(0) Medium Pages: 1 / 32M, Empty: 0M, Relocated: 0M, In-Place: 0",
    			"[1.663s][info][gc,reloc    ] GC(0) Large Pages: 0 / 0M, Empty: 0M, Relocated: 0M, In-Place: 0",
    			"[1.663s][info][gc,reloc    ] GC(0) Forwarding Usage: 4M",
    			"[1.663s][info][gc,heap     ] GC(0) Min Capacity: 8M(0%)",
    			"[1.663s][info][gc,heap     ] GC(0) Max Capacity: 8094M(100%)",
    			"[1.663s][info][gc,heap     ] GC(0) Soft Max Capacity: 8094M(100%)",
    			"[1.663s][info][gc,heap     ] GC(0)                Mark Start          Mark End        Relocate Start      Relocate End           High               Low         ",
    			"[1.663s][info][gc,heap     ] GC(0)  Capacity:      506M (6%)          506M (6%)          506M (6%)          506M (6%)          506M (6%)          506M (6%)     ",
    			"[1.663s][info][gc,heap     ] GC(0)      Free:     7970M (98%)        7968M (98%)        7964M (98%)        8028M (99%)        8028M (99%)        7960M (98%)    ",
    			"[1.663s][info][gc,heap     ] GC(0)      Used:      124M (2%)          126M (2%)          130M (2%)           66M (1%)          134M (2%)           66M (1%)     ",
    			"[1.664s][info][gc,heap     ] GC(0)      Live:         -                19M (0%)           19M (0%)           19M (0%)             -                  -          ",
    			"[1.664s][info][gc,heap     ] GC(0) Allocated:         -                 2M (0%)            6M (0%)            9M (0%)             -                  -          ",
    			"[1.664s][info][gc,heap     ] GC(0)   Garbage:         -               104M (1%)          104M (1%)           36M (0%)             -                  -          ",
    			"[1.664s][info][gc,heap     ] GC(0) Reclaimed:         -                  -                 0M (0%)           67M (1%)             -                  -          ",
    			"[1.664s][info][gc          ] GC(0) Garbage Collection (Metadata GC Threshold) 124M(2%)->66M(1%)"
    	};
    	
        int expectedEventCount = 1;

        List<JVMEvent> jvmEvents = feedParser(lines);
        assertEquals(expectedEventCount, jvmEvents.size());

        // 0 - ZGCFullCollection
        // Note: this is just a light-weight check of the event object at this time to make sure there is no confusion between
        // Legacy ZGC logging and Generational ZGC events with some of the changes added to support no-detail logging.
        assertTrue(jvmEvents.get(0) instanceof ZGCFullCollection); 
        ZGCFullCollection evt0 = (ZGCFullCollection) jvmEvents.get(0);
        assertEquals(new DateTimeStamp(1.641), evt0.getDateTimeStamp());
        assertEquals(GCCause.METADATA_GENERATION_THRESHOLD, evt0.getGCCause());
        assertEquals(ZGCPhase.FULL, evt0.getPhase());
        assertZGCMemorySummary(evt0.getMemorySummary(), 124*M, 66*M);
        assertDoubleEquals(evt0.getDuration(), 1.664 - 1.641);
 	
    }
    
	@Test
	void testGenerationalZGCNoDetailsLines() {
		String[] lines = {
				// 0 - ZGCYoungCollection(Major)
				"[4.252s][info][gc] GC(2) Major Collection (Metadata GC Threshold)",
				"[4.366s][info][gc] GC(2) Major Collection (Metadata GC Threshold) 384M(5%)->186M(2%) 0.114s",

				// 1 - ZGCYoungCollection(Major)
				"[36.326s][info][gc] GC(3) Major Collection (Warmup)",
				"[36.423s][info][gc] GC(3) Major Collection (Warmup) 730M(9%)->258M(3%) 0.097s"
		};

        int expectedEventCount = 2;

        List<JVMEvent> jvmEvents = feedParser(lines);
        assertEquals(jvmEvents.size(), expectedEventCount);

        // 0 - ZGCYoungCollection
        assertTrue(jvmEvents.get(0) instanceof ZGCYoungCollection); 
        ZGCYoungCollection evt0 = (ZGCYoungCollection) jvmEvents.get(0);
        assertEquals(new DateTimeStamp(4.252), evt0.getDateTimeStamp());
        assertEquals(GCCause.METADATA_GENERATION_THRESHOLD, evt0.getGCCause());
        assertEquals(ZGCPhase.MAJOR_YOUNG, evt0.getPhase());
        assertZGCMemorySummary(evt0.getMemorySummary(), 384*M, 186*M);
        assertDoubleEquals(evt0.getDuration(), 0.114);

        // 1 - ZGCYoungCollection(Major)
        assertTrue(jvmEvents.get(1) instanceof ZGCYoungCollection); 
        ZGCYoungCollection evt1 = (ZGCYoungCollection) jvmEvents.get(1);
        assertEquals(new DateTimeStamp(36.326), evt1.getDateTimeStamp());
        assertEquals(GCCause.WARMUP, evt1.getGCCause());
        assertEquals(ZGCPhase.MAJOR_YOUNG, evt1.getPhase());
        assertZGCMemorySummary(evt1.getMemorySummary(), 730*M, 258*M);        
        assertDoubleEquals(evt1.getDuration(), 0.097);
	}

	@Test
	void testMajorGenerationalDetailedEvent() {
		String[] lines = {
				// 0 - 
				"[0.990s][info][gc          ] GC(0) Major Collection (Metadata GC Threshold)",
				"[0.990s][info][gc,task     ] GC(0) Using 1 Workers for Young Generation",
				"[0.990s][info][gc,task     ] GC(0) Using 1 Workers for Old Generation",
				"[0.990s][info][gc,phases   ] GC(0) Y: Young Generation",
				"[0.990s][info][gc,phases   ] GC(0) Y: Pause Mark Start (Major) 0.011ms",
				"[1.000s][info][gc,phases   ] GC(0) Y: Concurrent Mark 9.935ms",
				"[1.000s][info][gc,phases   ] GC(0) Y: Pause Mark End 0.013ms",
				"[1.001s][info][gc,phases   ] GC(0) Y: Concurrent Mark Free 0.001ms",
				"[1.001s][info][gc,phases   ] GC(0) Y: Concurrent Reset Relocation Set 0.000ms",
				"[1.006s][info][gc,reloc    ] GC(0) Y: Using tenuring threshold: 1 (Computed)",
				"[1.007s][info][gc,phases   ] GC(0) Y: Concurrent Select Relocation Set 5.893ms",
				"[1.007s][info][gc,phases   ] GC(0) Y: Pause Relocate Start 0.012ms",
				"[1.012s][info][gc,phases   ] GC(0) Y: Concurrent Relocate 4.910ms",
				"[1.012s][info][gc,alloc    ] GC(0) Y:                         Mark Start        Mark End      Relocate Start    Relocate End   ",
				"[1.012s][info][gc,alloc    ] GC(0) Y: Allocation Stalls:          0                0                0                0         ",
				"[1.012s][info][gc,load     ] GC(0) Y: Load: 0.00 (0%) / 0.00 (0%) / 0.00 (0%)",
				"[1.012s][info][gc,mmu      ] GC(0) Y: MMU: 2ms/99.3%, 5ms/99.7%, 10ms/99.7%, 20ms/99.8%, 50ms/99.9%, 100ms/100.0%",
				"[1.012s][info][gc,marking  ] GC(0) Y: Mark: 1 stripe(s), 2 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s) ",
				"[1.012s][info][gc,marking  ] GC(0) Y: Mark Stack Usage: 32M",
				"[1.012s][info][gc,nmethod  ] GC(0) Y: NMethods: 2166 registered, 0 unregistered",
				"[1.012s][info][gc,metaspace] GC(0) Y: Metaspace: 21M used, 21M committed, 1088M reserved",
				"[1.012s][info][gc,reloc    ] GC(0) Y:                        Candidates     Selected     In-Place         Size        Empty    Relocated ",
				"[1.012s][info][gc,reloc    ] GC(0) Y: Small Pages:                   27           20            0          54M           0M           5M ",
				"[1.012s][info][gc,reloc    ] GC(0) Y: Medium Pages:                   1            0            0          32M           0M           0M ",
				"[1.012s][info][gc,reloc    ] GC(0) Y: Large Pages:                    0            0            0           0M           0M           0M ",
				"[1.012s][info][gc,reloc    ] GC(0) Y: Forwarding Usage: 2M",
				"[1.012s][info][gc,reloc    ] GC(0) Y: Age Table:",
				"[1.012s][info][gc,reloc    ] GC(0) Y:                    Live             Garbage             Small              Medium             Large        ",
				"[1.012s][info][gc,reloc    ] GC(0) Y: Eden              14M (0%)           71M (1%)          27 / 20             1 / 0              0 / 0        ",
				"[1.012s][info][gc,heap     ] GC(0) Y: Min Capacity: 8M(0%)",
				"[1.012s][info][gc,heap     ] GC(0) Y: Max Capacity: 8094M(100%)",
				"[1.012s][info][gc,heap     ] GC(0) Y: Soft Max Capacity: 7284M(90%)",
				"[1.012s][info][gc,heap     ] GC(0) Y: Heap Statistics:",
				"[1.012s][info][gc,heap     ] GC(0) Y:                Mark Start          Mark End        Relocate Start      Relocate End           High               Low         ",
				"[1.012s][info][gc,heap     ] GC(0) Y:  Capacity:      506M (6%)          506M (6%)          506M (6%)          506M (6%)          506M (6%)          506M (6%)     ",
				"[1.012s][info][gc,heap     ] GC(0) Y:      Free:     8008M (99%)        8006M (99%)        8006M (99%)        8040M (99%)        8040M (99%)        8000M (99%)    ",
				"[1.012s][info][gc,heap     ] GC(0) Y:      Used:       86M (1%)           88M (1%)           88M (1%)           54M (1%)           94M (1%)           54M (1%)     ",
				"[1.012s][info][gc,heap     ] GC(0) Y: Young Generation Statistics:",
				"[1.012s][info][gc,heap     ] GC(0) Y:                Mark Start          Mark End        Relocate Start      Relocate End    ",
				"[1.012s][info][gc,heap     ] GC(0) Y:      Used:       86M (1%)           88M (1%)           88M (1%)           54M (1%)     ",
				"[1.012s][info][gc,heap     ] GC(0) Y:      Live:         -                14M (0%)           14M (0%)           14M (0%)     ",
				"[1.012s][info][gc,heap     ] GC(0) Y:   Garbage:         -                71M (1%)           71M (1%)           30M (0%)     ",
				"[1.012s][info][gc,heap     ] GC(0) Y: Allocated:         -                 2M (0%)            2M (0%)            9M (0%)     ",
				"[1.012s][info][gc,heap     ] GC(0) Y: Reclaimed:         -                  -                 0M (0%)           41M (1%)     ",
				"[1.012s][info][gc,heap     ] GC(0) Y:  Promoted:         -                  -                 0M (0%)            0M (0%)     ",
				"[1.012s][info][gc,heap     ] GC(0) Y: Compacted:         -                  -                  -                 8M (0%)     ",
				"[1.012s][info][gc,phases   ] GC(0) Y: Young Generation 86M(1%)->54M(1%) 0.022s",
				
				// 1 - 
				"[1.012s][info][gc,phases   ] GC(0) O: Old Generation",
				"[1.012s][info][gc,phases   ] GC(0) O: Concurrent Mark 0.251ms",
				"[1.012s][info][gc,phases   ] GC(0) O: Pause Mark End 0.008ms",
				"[1.013s][info][gc,phases   ] GC(0) O: Concurrent Mark Free 0.155ms",
				"[1.016s][info][gc,phases   ] GC(0) O: Concurrent Process Non-Strong 3.677ms",
				"[1.016s][info][gc,phases   ] GC(0) O: Concurrent Reset Relocation Set 0.001ms",
				"[1.018s][info][gc,phases   ] GC(0) O: Concurrent Select Relocation Set 1.296ms",
				"[1.018s][info][gc,task     ] GC(0) O: Using 2 Workers for Old Generation",
				"[1.062s][info][gc,task     ] GC(0) O: Using 1 Workers for Old Generation",
				"[1.062s][info][gc,phases   ] GC(0) O: Concurrent Remap Roots 43.778ms",
				"[1.062s][info][gc,phases   ] GC(0) O: Pause Relocate Start 0.018ms",
				"[1.062s][info][gc,phases   ] GC(0) O: Concurrent Relocate 0.033ms",
				"[1.062s][info][gc,alloc    ] GC(0) O:                         Mark Start        Mark End      Relocate Start    Relocate End   ",
				"[1.062s][info][gc,alloc    ] GC(0) O: Allocation Stalls:          0                0                0                0         ",
				"[1.062s][info][gc,load     ] GC(0) O: Load: 0.00 (0%) / 0.00 (0%) / 0.00 (0%)",
				"[1.062s][info][gc,mmu      ] GC(0) O: MMU: 2ms/99.1%, 5ms/99.6%, 10ms/99.7%, 20ms/99.8%, 50ms/99.9%, 100ms/99.9%",
				"[1.062s][info][gc,marking  ] GC(0) O: Mark: 1 stripe(s), 1 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s) ",
				"[1.062s][info][gc,marking  ] GC(0) O: Mark Stack Usage: 0M",
				"[1.062s][info][gc,nmethod  ] GC(0) O: NMethods: 1938 registered, 269 unregistered",
				"[1.062s][info][gc,metaspace] GC(0) O: Metaspace: 21M used, 21M committed, 1088M reserved",
				"[1.062s][info][gc,ref      ] GC(0) O:                       Encountered   Discovered     Enqueued ",
				"[1.062s][info][gc,ref      ] GC(0) O: Soft References:             3704            0            0 ",
				"[1.062s][info][gc,ref      ] GC(0) O: Weak References:             1514            0            0 ",
				"[1.062s][info][gc,ref      ] GC(0) O: Final References:               0            0            0 ",
				"[1.062s][info][gc,ref      ] GC(0) O: Phantom References:           762            0            0 ",
				"[1.062s][info][gc,heap     ] GC(0) O: Min Capacity: 8M(0%)",
				"[1.062s][info][gc,heap     ] GC(0) O: Max Capacity: 8094M(100%)",
				"[1.062s][info][gc,heap     ] GC(0) O: Soft Max Capacity: 7284M(90%)",
				"[1.062s][info][gc,heap     ] GC(0) O: Heap Statistics:",
				"[1.062s][info][gc,heap     ] GC(0) O:                Mark Start          Mark End        Relocate Start      Relocate End           High               Low         ",
				"[1.062s][info][gc,heap     ] GC(0) O:  Capacity:      506M (6%)          506M (6%)          506M (6%)          506M (6%)          506M (6%)          506M (6%)     ",
				"[1.062s][info][gc,heap     ] GC(0) O:      Free:     8008M (99%)        8040M (99%)        8038M (99%)        8038M (99%)        8040M (99%)        8000M (99%)    ",
				"[1.062s][info][gc,heap     ] GC(0) O:      Used:       86M (1%)           54M (1%)           56M (1%)           56M (1%)           94M (1%)           54M (1%)     ",
				"[1.062s][info][gc,heap     ] GC(0) O: Old Generation Statistics:",
				"[1.062s][info][gc,heap     ] GC(0) O:                Mark Start          Mark End        Relocate Start      Relocate End    ",
				"[1.062s][info][gc,heap     ] GC(0) O:      Used:        0M (0%)            0M (0%)            0M (0%)            0M (0%)     ",
				"[1.062s][info][gc,heap     ] GC(0) O:      Live:         -                 0M (0%)            0M (0%)            0M (0%)     ",
				"[1.062s][info][gc,heap     ] GC(0) O:   Garbage:         -                 0M (0%)            0M (0%)            0M (0%)     ",
				"[1.062s][info][gc,heap     ] GC(0) O: Allocated:         -                 0M (0%)            0M (0%)            0M (0%)     ",
				"[1.062s][info][gc,heap     ] GC(0) O: Reclaimed:         -                  -                 0M (0%)            0M (0%)     ",
				"[1.062s][info][gc,heap     ] GC(0) O: Compacted:         -                  -                  -                 0M (0%)     ",
				"[1.062s][info][gc,phases   ] GC(0) O: Old Generation 54M(1%)->56M(1%) 0.050s",
				"[1.062s][info][gc          ] GC(0) Major Collection (Metadata GC Threshold) 86M(1%)->56M(1%) 0.073s",
		};
		
        int expectedEventCount = 2;

        List<JVMEvent> jvmEvents = feedParser(lines);
        assertEquals(jvmEvents.size(), expectedEventCount);

        // TODO: Only basic testing to make sure no extra events were being created by fixes for no-details logs.
        
        // 0 - ZGCYoungCollection
        assertTrue(jvmEvents.get(0) instanceof ZGCYoungCollection); 
        ZGCYoungCollection evt0 = (ZGCYoungCollection) jvmEvents.get(0);
        assertEquals(new DateTimeStamp(0.990), evt0.getDateTimeStamp());
        assertEquals(GCCause.METADATA_GENERATION_THRESHOLD, evt0.getGCCause());
        assertEquals(ZGCPhase.MAJOR_YOUNG, evt0.getPhase());
        assertZGCMemorySummary(evt0.getMemorySummary(), 86*M, 54*M);
        assertDoubleEquals(evt0.getDuration(), 0.022);

        // 1 - ZGCOldCollection(Major)
        assertTrue(jvmEvents.get(1) instanceof ZGCOldCollection); 
        ZGCOldCollection evt1 = (ZGCOldCollection) jvmEvents.get(1);
        assertEquals(new DateTimeStamp(1.012), evt1.getDateTimeStamp());
        assertEquals(GCCause.METADATA_GENERATION_THRESHOLD, evt1.getGCCause());
        assertEquals(ZGCPhase.MAJOR_OLD, evt1.getPhase());
        assertZGCMemorySummary(evt1.getMemorySummary(), 54*M, 56*M);        
        assertDoubleEquals(evt1.getDuration(), 0.050);
		
	}
	
	private void assertZGCMemorySummary(ZGCMemorySummary summary, long before, long after) {
		assertNotNull(summary);
		assertEquals(summary.getOccupancyBefore(), before);
		assertEquals(summary.getOccupancyAfter(), after);		
	}
    
    private boolean checkZGCMemoryPoolSummary(ZGCMemoryPoolSummary summary, long capacity, long free, long used) {
        return summary.getCapacity() == capacity && summary.getFree() == free && summary.getUsed() == used;
    }

    private boolean checkZGCMetaSpaceSummary(ZGCMetaspaceSummary summary, long used, long committed, long reserved) {
        return summary.getUsed() == used && summary.getCommitted() == committed && summary.getReserved() == reserved;
    }

    private boolean checkLiveSummary(ZGCLiveSummary summary, long markEnd, long relocateStart, long relocateEnd) {
                return summary.getMarkEnd() == markEnd && summary.getRelocateStart() == relocateStart && summary.getRelocateEnd() == relocateEnd;
    }

    private boolean checkAllocatedSummary(ZGCAllocatedSummary summary, long markEnd, long relocateStart, long relocateEnd) {
        return summary.getMarkEnd() == markEnd && summary.getRelocateStart() == relocateStart && summary.getRelocateEnd() == relocateEnd;
    }

    private boolean checkGarbageSummary(ZGCGarbageSummary summary, long markEnd, long relocateStart, long relocateEnd) {
        return summary.getMarkEnd() == markEnd && summary.getRelocateStart() == relocateStart && summary.getRelocateEnd() == relocateEnd;
    }

    private boolean checkReclaimSummary(ZGCReclaimedSummary summary, long relocateStart, long relocateEnd) {
        return summary.getRelocateStart() == relocateStart && summary.getRelocateEnd() == relocateEnd;
    }

    private boolean checkMemorySummary(ZGCMemorySummary summary, long relocateStart, long relocateEnd) {
        return summary.getOccupancyBefore() == relocateStart && summary.getOccupancyAfter() == relocateEnd;
    }

    private int toInt(double value, int significantDigits) {
        return (int)(value * (double)significantDigits);
    }
}
