package com.microsoft.gctoolkit.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.g1gc.G1Mixed;
import com.microsoft.gctoolkit.event.g1gc.G1Young;
import com.microsoft.gctoolkit.event.g1gc.G1YoungInitialMark;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedDiarizer;

public class PreUnifiedG1GCParserTest extends ParserTest {

	protected long M = 1024;
	protected long G = 1024*1024;
	
    protected Diarizer diarizer() {
        return new PreUnifiedDiarizer();
    };

    protected GCLogParser parser() {
        return new PreUnifiedG1GCParser();
    };

    @Test
    // jlittle-ptc: Added to validate changes in https://github.com/microsoft/gctoolkit/issues/433
    // Fails without changes, passes with changes.
    public void testJava8PreUnifiedG1GCYoungEvents() {
    	String[] lines = new String[] {
    		    "0.867: [GC pause (G1 Evacuation Pause) (young) 52816K->9563K(1024M), 0.0225122 secs]",
    		    "5.478: [GC pause (young) 8878K->5601K(13M), 0.0027650 secs]",
    		    "1566.108: [GC pause (mixed) 7521K->5701K(13M), 0.0030090 secs]",
    			"1834339.155: [GC pause (G1 Evacuation Pause) (mixed) 309M->141M(1111M), 0.0188779 secs]"
    	};

    	List<JVMEvent> jvmEvents = feedParser(lines);
    	assertEquals(4, jvmEvents.size());
    	
    	// First two lines are G1Young, followed by G1Mixed
    	assertMemoryPoolValues(((G1Young) jvmEvents.get(0)).getHeap(), 52816, 1024*1024, 9563, 1024*1024);
    	assertMemoryPoolValues(((G1Young) jvmEvents.get(1)).getHeap(), 8878, 13*1024, 5601, 13*1024);
    	
    	assertMemoryPoolValues(((G1Mixed) jvmEvents.get(2)).getHeap(), 7521, 13*1024, 5701, 13*1024);
    	assertMemoryPoolValues(((G1Mixed) jvmEvents.get(3)).getHeap(), 309*1024, 1111*1024, 141*1024, 1111*1024);
    	
    }

	@Test
	void testG1DetailedEvacuationPauseEvent() {
		String[] lines = {
				"2025-03-23T03:46:46.582+0000: 27.619: [GC pause (G1 Evacuation Pause) (young), 0.0552009 secs]",
				"   [Parallel Time: 44.5 ms, GC Workers: 6]",
				"      [GC Worker Start (ms): Min: 27622.0, Avg: 27622.1, Max: 27622.2, Diff: 0.3]",
				"      [Ext Root Scanning (ms): Min: 0.9, Avg: 2.5, Max: 9.9, Diff: 8.9, Sum: 14.9]",
				"      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]",
				"         [Processed Buffers: Min: 0, Avg: 0.0, Max: 0, Diff: 0, Sum: 0]",
				"      [Scan RS (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 0.7]",
				"      [Code Root Scanning (ms): Min: 0.0, Avg: 0.3, Max: 0.8, Diff: 0.8, Sum: 1.8]",
				"      [Object Copy (ms): Min: 34.4, Avg: 41.0, Max: 42.6, Diff: 8.2, Sum: 246.1]",
				"      [Termination (ms): Min: 0.0, Avg: 0.2, Max: 0.3, Diff: 0.3, Sum: 1.3]",
				"         [Termination Attempts: Min: 1, Avg: 184.0, Max: 256, Diff: 255, Sum: 1104]",
				"      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.0, Sum: 0.2]",
				"      [GC Worker Total (ms): Min: 44.0, Avg: 44.2, Max: 44.3, Diff: 0.3, Sum: 265.1]",
				"      [GC Worker End (ms): Min: 27666.3, Avg: 27666.3, Max: 27666.3, Diff: 0.0]",
				"   [Code Root Fixup: 0.1 ms]",
				"   [Code Root Purge: 0.0 ms]",
				"   [Clear CT: 0.4 ms]",
				"   [Other: 10.3 ms]",
				"      [Choose CSet: 0.0 ms]",
				"      [Ref Proc: 5.6 ms]",
				"      [Ref Enq: 0.0 ms]",
				"      [Redirty Cards: 0.2 ms]",
				"      [Humongous Register: 3.2 ms]",
				"      [Humongous Reclaim: 0.2 ms]",
				"      [Free CSet: 0.5 ms]",
				"   [Eden: 468.0M(468.0M)->0.0B(448.0M) Survivors: 44.0M->64.0M Heap: 539.6M(10.0G)->128.5M(10.0G)]",
				" [Times: user=0.27 sys=0.01, real=0.05 secs] "
		};
		
		int expectedEventCount = 1;
		
    	List<JVMEvent> jvmEvents = feedParser(lines);
    	assertEquals(expectedEventCount, jvmEvents.size());

		// 0 - G1Young
    	assertTrue(jvmEvents.get(0) instanceof G1Young);
    	G1Young evt0 = ((G1Young) jvmEvents.get(0));
    	assertEquals(evt0.getGCCause(), GCCause.G1_EVACUATION_PAUSE);
    	assertMemoryPoolValues(evt0.getHeap(), (long) (539.6*M), 10*G, (long) (128.5*M), 10*G);
    	assertMemoryPoolValues(evt0.getEden(), 468*M, 468*M, 0, 448*M);
    	assertEquals(evt0.getDuration(), 0.0552009);
    	
    	// TODO: What else do we pull out of the block that would be verifiable?	
	}
	
	@Test
	void testG1DetailedYoungMetadataGCThresholdPauseEvent() {
		// Sourced from BackgroundCADPublish-G1-WC11.log.1
		String[] lines = {
				"2025-03-23T03:46:27.139+0000: 8.176: [GC pause (Metadata GC Threshold) (young) (initial-mark), 0.0286166 secs]",
				"   [Parallel Time: 13.9 ms, GC Workers: 6]",
				"      [GC Worker Start (ms): Min: 8187.3, Avg: 8187.5, Max: 8187.8, Diff: 0.6]",
				"      [Ext Root Scanning (ms): Min: 3.1, Avg: 3.7, Max: 4.6, Diff: 1.5, Sum: 22.1]",
				"      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]",
				"         [Processed Buffers: Min: 0, Avg: 0.0, Max: 0, Diff: 0, Sum: 0]",
				"      [Scan RS (ms): Min: 0.1, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 0.9]",
				"      [Code Root Scanning (ms): Min: 0.0, Avg: 0.2, Max: 0.7, Diff: 0.7, Sum: 1.0]",
				"      [Object Copy (ms): Min: 8.6, Avg: 9.2, Max: 9.6, Diff: 1.0, Sum: 55.4]",
				"      [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]",
				"         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 6]",
				"      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]",
				"      [GC Worker Total (ms): Min: 13.0, Avg: 13.3, Max: 13.6, Diff: 0.6, Sum: 79.6]",
				"      [GC Worker End (ms): Min: 8200.8, Avg: 8200.8, Max: 8200.8, Diff: 0.0]",
				"   [Code Root Fixup: 0.0 ms]",
				"   [Code Root Purge: 0.0 ms]",
				"   [Clear CT: 0.5 ms]",
				"   [Other: 14.2 ms]",
				"      [Choose CSet: 0.0 ms]",
				"      [Ref Proc: 2.3 ms]",
				"      [Ref Enq: 0.0 ms]",
				"      [Redirty Cards: 0.5 ms]",
				"      [Humongous Register: 0.0 ms]",
				"      [Humongous Reclaim: 0.0 ms]",
				"      [Free CSet: 0.3 ms]",
				"   [Eden: 224.0M(512.0M)->0.0B(492.0M) Survivors: 0.0B->20.0M Heap: 222.4M(10.0G)->19.6M(10.0G)]",
				" [Times: user=0.07 sys=0.02, real=0.03 secs]"
		};
		
		int expectedEventCount = 1;
		
    	List<JVMEvent> jvmEvents = feedParser(lines);
    	assertEquals(expectedEventCount, jvmEvents.size());

		// 0 - G1YoungInitialMark
    	assertTrue(jvmEvents.get(0) instanceof G1YoungInitialMark);
    	G1YoungInitialMark evt0 = ((G1YoungInitialMark) jvmEvents.get(0));
    	assertEquals(evt0.getGCCause(), GCCause.METADATA_GENERATION_THRESHOLD);
    	assertMemoryPoolValues(evt0.getHeap(), (long) (222.4*M), 10*G, (long) (19.6*M), 10*G);
    	assertMemoryPoolValues(evt0.getEden(), 224*M, 512*M, 0, 492*M);
    	assertEquals(evt0.getDuration(), 0.0286166);
    	
    	// TODO: What else do we pull out of the block that would be verifiable?
	}

	@Test
	void testG1DetailedSystemGCYoung() {
		String[] lines = {
				"2025-04-18T20:35:53.067+0000: 2306974.104: [GC pause (System.gc()) (young) (initial-mark), 0.0081646 secs]",
				"   [Parallel Time: 6.4 ms, GC Workers: 6]",
				"      [GC Worker Start (ms): Min: 2306974105.2, Avg: 2306974105.4, Max: 2306974105.5, Diff: 0.3]",
				"      [Ext Root Scanning (ms): Min: 3.7, Avg: 3.9, Max: 4.1, Diff: 0.5, Sum: 23.4]",
				"      [Update RS (ms): Min: 0.3, Avg: 0.3, Max: 0.4, Diff: 0.1, Sum: 1.9]",
				"         [Processed Buffers: Min: 1, Avg: 8.0, Max: 16, Diff: 15, Sum: 48]",
				"      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]",
				"      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]",
				"      [Object Copy (ms): Min: 1.3, Avg: 1.6, Max: 1.8, Diff: 0.5, Sum: 9.7]",
				"      [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]",
				"         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 6]",
				"      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]",
				"      [GC Worker Total (ms): Min: 5.7, Avg: 5.9, Max: 6.0, Diff: 0.3, Sum: 35.2]",
				"      [GC Worker End (ms): Min: 2306974111.2, Avg: 2306974111.2, Max: 2306974111.2, Diff: 0.0]",
				"   [Code Root Fixup: 0.0 ms]",
				"   [Code Root Purge: 0.0 ms]",
				"   [Clear CT: 0.3 ms]",
				"	[Other: 1.4 ms]",
				"      [Choose CSet: 0.0 ms]",
				"      [Ref Proc: 0.4 ms]",
				"      [Ref Enq: 0.0 ms]",
				"      [Redirty Cards: 0.1 ms]",
				"      [Humongous Register: 0.0 ms]",
				"      [Humongous Reclaim: 0.1 ms]",
				"      [Free CSet: 0.2 ms]",
				"   [Eden: 148.0M(6136.0M)->0.0B(6136.0M) Survivors: 8192.0K->8192.0K Heap: 988.7M(10.0G)->843.8M(10.0G)]",
				" [Times: user=0.03 sys=0.00, real=0.01 secs]" 	
		};
		
		int expectedEventCount = 1;
		
    	List<JVMEvent> jvmEvents = feedParser(lines);
    	assertEquals(expectedEventCount, jvmEvents.size());

		// 0 - G1YoungInitialMark
    	assertTrue(jvmEvents.get(0) instanceof G1YoungInitialMark);
    	G1YoungInitialMark evt0 = ((G1YoungInitialMark) jvmEvents.get(0));
    	assertEquals(evt0.getGCCause(), GCCause.JAVA_LANG_SYSTEM);
    	assertMemoryPoolValues(evt0.getHeap(), (long) (988.7*M), 10*G, (long) (843.8*M), 10*G);
    	assertMemoryPoolValues(evt0.getEden(), 148*M, 6136*M, 0, 6136*M);
    	assertEquals(evt0.getDuration(), 0.0081646);
    	
    	// TODO: What else do we pull out of the block that would be verifiable?		
	}
	
	@Test
	void testG1DetailedGCLockerInitiatedPauseEvent() {
		String[] lines = {
			"2025-03-23T03:47:20.309+0000: 61.346: [GC pause (GCLocker Initiated GC) (young) (initial-mark), 0.1288985 secs]",
			"   [Parallel Time: 119.0 ms, GC Workers: 6]",
			"      [GC Worker Start (ms): Min: 61351.6, Avg: 61351.8, Max: 61351.9, Diff: 0.2]",
			"      [Ext Root Scanning (ms): Min: 17.5, Avg: 18.6, Max: 20.3, Diff: 2.9, Sum: 111.6]",
			"      [Update RS (ms): Min: 3.3, Avg: 3.9, Max: 4.6, Diff: 1.3, Sum: 23.6]",
			"         [Processed Buffers: Min: 2, Avg: 5.5, Max: 8, Diff: 6, Sum: 33]",
			"      [Scan RS (ms): Min: 5.5, Avg: 6.0, Max: 6.4, Diff: 1.0, Sum: 36.2]",
			"      [Code Root Scanning (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 0.5]",
			"      [Object Copy (ms): Min: 86.9, Avg: 87.8, Max: 88.5, Diff: 1.6, Sum: 526.6]",
			"      [Termination (ms): Min: 0.0, Avg: 0.9, Max: 1.3, Diff: 1.3, Sum: 5.3]",
			"         [Termination Attempts: Min: 1, Avg: 620.0, Max: 919, Diff: 918, Sum: 3720]",
			"      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.0, Sum: 0.2]",
			"      [GC Worker Total (ms): Min: 117.2, Avg: 117.3, Max: 117.4, Diff: 0.2, Sum: 704.0]",
			"      [GC Worker End (ms): Min: 61469.1, Avg: 61469.1, Max: 61469.1, Diff: 0.0]",
			"   [Code Root Fixup: 0.0 ms]",
			"   [Code Root Purge: 0.0 ms]",
			"   [Clear CT: 1.2 ms]",
			"   [Other: 8.7 ms]",
			"      [Choose CSet: 0.0 ms]",
			"      [Ref Proc: 3.0 ms]",
			"      [Ref Enq: 0.1 ms]",
			"      [Redirty Cards: 0.6 ms]",
			"      [Humongous Register: 0.1 ms]",
			"      [Humongous Reclaim: 0.0 ms]",
			"      [Free CSet: 0.4 ms]",
			"   [Eden: 236.0M(400.0M)->0.0B(848.0M) Survivors: 112.0M->44.0M Heap: 646.8M(10.0G)->475.6M(10.0G)]",
			" [Times: user=0.59 sys=0.05, real=0.13 secs] "
		};
		
		int expectedEventCount = 1;
		
    	List<JVMEvent> jvmEvents = feedParser(lines);
    	assertEquals(expectedEventCount, jvmEvents.size());

		// 0 - G1YoungInitialMark
    	assertTrue(jvmEvents.get(0) instanceof G1Young);
    	G1Young evt0 = ((G1Young) jvmEvents.get(0));
    	assertEquals(evt0.getGCCause(), GCCause.GC_LOCKER);
    	assertMemoryPoolValues(evt0.getHeap(), (long) (646.8*M), 10*G, (long) (475.6*M), 10*G);
    	assertMemoryPoolValues(evt0.getEden(), 236*M, 400*M, 0, 848*M);
    	assertEquals(evt0.getDuration(), 0.1288985);
    	
    	// TODO: What else do we pull out of the block that would be verifiable?				
	}

	
}
