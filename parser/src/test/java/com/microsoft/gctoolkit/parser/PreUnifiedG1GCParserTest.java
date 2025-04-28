package com.microsoft.gctoolkit.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.g1gc.ConcurrentScanRootRegion;
import com.microsoft.gctoolkit.event.g1gc.G1Cleanup;
import com.microsoft.gctoolkit.event.g1gc.G1ConcurrentCleanup;
import com.microsoft.gctoolkit.event.g1gc.G1ConcurrentMark;
import com.microsoft.gctoolkit.event.g1gc.G1Mixed;
import com.microsoft.gctoolkit.event.g1gc.G1Remark;
import com.microsoft.gctoolkit.event.g1gc.G1Young;
import com.microsoft.gctoolkit.event.g1gc.G1YoungInitialMark;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedDiarizer;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class PreUnifiedG1GCParserTest extends ParserTest {

	protected long M = 1024;
	protected long G = 1024*1024;
	
    protected Diarizer diarizer() {
        return new PreUnifiedDiarizer();
    };

    protected GCLogParser parser() {
        return new PreUnifiedG1GCParser();
    };

	/**
	 * jlittle-ptc: Collection of different pre-unified events found in a log generated without -XX:+PrintGCDetails enabled.
	 * 
	 * Tests to ensure all events are parsed correctly.
	 */
	@Test
	void testPreUnifiedG1GCLinesNoDetails() {
		String[] lines = {
				// 0 - G1Young
				"1.303: [GC pause (G1 Evacuation Pause) (young) 57260K->14150K(1024M), 0.0148808 secs]",
								
				// 1 - ConcurrentScanRootRegion
				"1.496: [GC concurrent-root-region-scan-start]",
				"1.499: [GC concurrent-root-region-scan-end, 0.0033801 secs]",
								
				// 2 - G1ConcurrentMark
				"1.499: [GC concurrent-mark-start]",
				"1.509: [GC concurrent-mark-end, 0.0096200 secs]",
				
				// 3 - G1Remark
				"1.509: [GC remark, 0.0045439 secs]",				
				
				// 4 - G1Cleanup
				"1.513: [GC cleanup 15686K->13638K(1024M), 0.0014924 secs]",

				// 5 - G1ConcurrentCleanup
				"1.515: [GC concurrent-cleanup-start]",
				"1.515: [GC concurrent-cleanup-end, 0.0000053 secs]",
				
				// 6 - G1Mixed
				"24.383: [GC pause (G1 Evacuation Pause) (mixed) 269M->153M(1149M), 0.0457592 secs]",
				
				// 7 - G1 Mixed
    		    "1566.108: [GC pause (mixed) 7521K->5701K(13M), 0.0030090 secs]",				
				
				// Currently Ignored
				//"1.488: [GC pause (Metadata GC Threshold) (young) (initial-mark) 31558K->14662K(1024M), 0.0073758 secs]",
				//"1469798.061: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 397M->167M(1089M), 0.0313965 secs]",

		};
		
		int expectedEventCount = 8;
		
    	List<JVMEvent> jvmEvents = feedParser(lines);
    	assertEquals(jvmEvents.size(), expectedEventCount);
    	
		// 0 - G1Young
    	assertTrue(jvmEvents.get(0) instanceof G1Young);
    	G1Young evt0 = ((G1Young) jvmEvents.get(0));
    	assertEquals(evt0.getDateTimeStamp(), new DateTimeStamp(1.303));
    	assertEquals(evt0.getGCCause(), GCCause.G1_EVACUATION_PAUSE);
    	assertMemoryPoolValues(evt0.getHeap(), 57260, 1024*M, 14150, 1024*M);
    	assertDoubleEquals(evt0.getDuration(), 0.0148808);
    	
		// 1 - ConcurrentScanRootRegion - Duration/Timestamp only.
    	assertTrue(jvmEvents.get(1) instanceof ConcurrentScanRootRegion);
    	ConcurrentScanRootRegion evt1 = ((ConcurrentScanRootRegion) jvmEvents.get(1));
    	assertEquals(evt1.getDateTimeStamp(), new DateTimeStamp(1.496));
    	assertDoubleEquals(evt1.getDuration(), 0.0033801);
    	
		// 2 - G1ConcurrentMark - Duration/Timestamp only
    	assertTrue(jvmEvents.get(2) instanceof G1ConcurrentMark);
    	G1ConcurrentMark evt2 = ((G1ConcurrentMark) jvmEvents.get(2));
    	assertEquals(evt2.getDateTimeStamp(), new DateTimeStamp(1.499));
    	assertDoubleEquals(evt2.getDuration(), 0.0096200);

		// 3 - G1Remark - Duration/Timestamp only
    	assertTrue(jvmEvents.get(3) instanceof G1Remark);
    	G1Remark evt3 = ((G1Remark) jvmEvents.get(3));
    	assertEquals(evt3.getDateTimeStamp(), new DateTimeStamp(1.509));
    	assertDoubleEquals(evt3.getDuration(), 0.0045439);
    	
		// 4 - G1Cleanup
    	assertTrue(jvmEvents.get(4) instanceof G1Cleanup);
    	G1Cleanup evt4 = ((G1Cleanup) jvmEvents.get(4));
    	assertEquals(evt4.getDateTimeStamp(), new DateTimeStamp(1.513));    	
    	assertMemoryPoolValues(evt4.getHeap(), 15686, 1024*M, 13638, 1024*M);
    	assertDoubleEquals(evt4.getDuration(), 0.0014924);
    	
		// 5 - G1ConcurrentCleanup - Duration/Timestamp only
    	assertTrue(jvmEvents.get(5) instanceof G1ConcurrentCleanup);
    	G1ConcurrentCleanup evt5 = ((G1ConcurrentCleanup) jvmEvents.get(5));
    	assertEquals(evt5.getDateTimeStamp(), new DateTimeStamp(1.515));    	
    	assertDoubleEquals(evt5.getDuration(), 0.0000053);
    	
		// 6 - G1Mixed
    	assertTrue(jvmEvents.get(6) instanceof G1Mixed);
    	G1Mixed evt6 = ((G1Mixed) jvmEvents.get(6));
    	assertEquals(evt6.getDateTimeStamp(), new DateTimeStamp(24.383));
    	assertMemoryPoolValues(evt6.getHeap(), 269*M, 1149*M, 153*M, 1149*M);
    	assertDoubleEquals(evt6.getDuration(), 0.0457592);
    	
		// 7 - G1Mixed
    	assertTrue(jvmEvents.get(7) instanceof G1Mixed);
    	G1Mixed evt7 = ((G1Mixed) jvmEvents.get(7));
    	assertEquals(evt7.getDateTimeStamp(), new DateTimeStamp(1566.108));
    	assertMemoryPoolValues(evt7.getHeap(), 7521, 13*M, 5701, 13*M);
    	assertDoubleEquals(evt7.getDuration(), 0.0030090);    	

	}
    
    /*
     * jlittle-ptc: We could probably avoid a whole bunch of duplicated code in the following event checks with some thought,
     * but for now, they're quick and sufficient for testing. 
     */
    
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
    	assertEquals(evt0.getDateTimeStamp(), new DateTimeStamp("2025-03-23T03:46:46.582+0000", 27.619));
    	assertEquals(evt0.getGCCause(), GCCause.G1_EVACUATION_PAUSE);    	
    	// Memory Pools
    	assertMemoryPoolValues(evt0.getHeap(), (long) (539.6*M), 10*G, (long) (128.5*M), 10*G);
    	assertMemoryPoolValues(evt0.getEden(), 468*M, 468*M, 0, 448*M);
    	assertSurvivorMemoryPoolValues(evt0.getSurvivor(), 44*M, 64*M);    	
    	assertDoubleEquals(evt0.getDuration(), 0.0552009);
    	assertCPUSummaryValues(evt0.getCpuSummary(), 0.27, 0.01, 0.05);
	}
	
	@Test
	void testG1DetailedYoungMetadataGCThresholdPauseEvent() {
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
    	assertEquals(evt0.getDateTimeStamp(), new DateTimeStamp("2025-03-23T03:46:27.139+0000", 8.176));    	
    	assertEquals(evt0.getGCCause(), GCCause.METADATA_GENERATION_THRESHOLD);
    	// Memory Pools    	
    	assertMemoryPoolValues(evt0.getHeap(), (long) (222.4*M), 10*G, (long) (19.6*M), 10*G);
    	assertMemoryPoolValues(evt0.getEden(), 224*M, 512*M, 0, 492*M);
    	assertSurvivorMemoryPoolValues(evt0.getSurvivor(), 0, 20*M);    	
    	assertDoubleEquals(evt0.getDuration(), 0.0286166);
    	assertCPUSummaryValues(evt0.getCpuSummary(), 0.07, 0.02, 0.03);
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
    	assertEquals(evt0.getDateTimeStamp(), new DateTimeStamp("2025-04-18T20:35:53.067+0000", 2306974.104));
    	assertEquals(evt0.getGCCause(), GCCause.JAVA_LANG_SYSTEM);
    	// Memory Pools
    	assertMemoryPoolValues(evt0.getHeap(), (long) (988.7*M), 10*G, (long) (843.8*M), 10*G);
    	assertMemoryPoolValues(evt0.getEden(), 148*M, 6136*M, 0, 6136*M);
    	assertSurvivorMemoryPoolValues(evt0.getSurvivor(), 8192, 8192);
    	assertDoubleEquals(evt0.getDuration(), 0.0081646);
    	assertCPUSummaryValues(evt0.getCpuSummary(), 0.03, 0.00, 0.01);
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
    	assertEquals(evt0.getDateTimeStamp(), new DateTimeStamp("2025-03-23T03:47:20.309+0000", 61.346));
    	assertEquals(evt0.getGCCause(), GCCause.GC_LOCKER);
    	// Memory Pools
    	assertMemoryPoolValues(evt0.getHeap(), (long) (646.8*M), 10*G, (long) (475.6*M), 10*G);
    	assertMemoryPoolValues(evt0.getEden(), 236*M, 400*M, 0, 848*M);
    	assertSurvivorMemoryPoolValues(evt0.getSurvivor(), 112*M, 44*M);
    	assertDoubleEquals(evt0.getDuration(), 0.1288985);
    	assertCPUSummaryValues(evt0.getCpuSummary(), 0.59, 0.05, 0.13);
	}

	@Test
	void testG1DetailedMixedPauseEvent() {
		String[] lines = {
			"879630.318: [GC pause (G1 Evacuation Pause) (mixed), 0.0266434 secs]",
			"   [Parallel Time: 23.9 ms, GC Workers: 10]",
			"      [GC Worker Start (ms): Min: 879630318.6, Avg: 879630318.7, Max: 879630318.7, Diff: 0.1]",
			"      [Ext Root Scanning (ms): Min: 4.2, Avg: 4.9, Max: 6.9, Diff: 2.8, Sum: 48.8]",
			"      [Update RS (ms): Min: 0.0, Avg: 1.8, Max: 4.3, Diff: 4.3, Sum: 18.3]",
			"         [Processed Buffers: Min: 0, Avg: 12.9, Max: 30, Diff: 30, Sum: 129]",
			"      [Scan RS (ms): Min: 0.0, Avg: 0.3, Max: 0.5, Diff: 0.5, Sum: 3.2]",
			"      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]",
			"      [Object Copy (ms): Min: 14.7, Avg: 16.3, Max: 16.9, Diff: 2.2, Sum: 163.2]",
			"      [Termination (ms): Min: 0.0, Avg: 0.3, Max: 0.4, Diff: 0.4, Sum: 3.4]",
			"         [Termination Attempts: Min: 1, Avg: 1208.9, Max: 1468, Diff: 1467, Sum: 12089]",
			"      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.2]",
			"      [GC Worker Total (ms): Min: 23.6, Avg: 23.7, Max: 23.8, Diff: 0.1, Sum: 237.0]",
			"      [GC Worker End (ms): Min: 879630342.4, Avg: 879630342.4, Max: 879630342.4, Diff: 0.0]",
			"   [Code Root Fixup: 0.2 ms]",
			"   [Code Root Purge: 0.0 ms]",
			"   [Clear CT: 0.2 ms]",
			"   [Other: 2.4 ms]",
			"      [Choose CSet: 0.0 ms]",
			"      [Ref Proc: 0.3 ms]",
			"      [Ref Enq: 0.0 ms]",
			"      [Redirty Cards: 0.2 ms]",
			"      [Humongous Register: 0.1 ms]",
			"      [Humongous Reclaim: 0.1 ms]",
			"      [Free CSet: 0.3 ms]",
			"   [Eden: 340.0M(340.0M)->0.0B(340.0M) Survivors: 20.0M->20.0M Heap: 6712.4M(7232.0M)->6366.2M(7232.0M)]",
			" [Times: user=0.27 sys=0.00, real=0.03 secs]"
		};
		
		int expectedEventCount = 1;
		
    	List<JVMEvent> jvmEvents = feedParser(lines);
    	assertEquals(expectedEventCount, jvmEvents.size());

		// 0 - G1Mixed
    	assertTrue(jvmEvents.get(0) instanceof G1Mixed);
    	G1Mixed evt0 = ((G1Mixed) jvmEvents.get(0));
    	assertEquals(evt0.getDateTimeStamp(), new DateTimeStamp(879630.318));
    	assertEquals(evt0.getGCCause(), GCCause.G1_EVACUATION_PAUSE);
    	// Memory Pools
    	assertMemoryPoolValues(evt0.getHeap(), (long) (6712.4*M), 7232*M, (long) (6366.2*M), 7232*M);
    	assertMemoryPoolValues(evt0.getEden(), 340*M, 340*M, 0, 340*M);
    	assertSurvivorMemoryPoolValues(evt0.getSurvivor(), 20*M, 20*M);
    	assertDoubleEquals(evt0.getDuration(), 0.0266434);
    	assertCPUSummaryValues(evt0.getCpuSummary(), 0.27, 0.00, 0.03);    	
	}
	
}
