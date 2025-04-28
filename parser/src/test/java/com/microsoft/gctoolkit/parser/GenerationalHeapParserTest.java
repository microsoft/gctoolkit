// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.generational.AbortablePreClean;
import com.microsoft.gctoolkit.event.generational.CMSRemark;
import com.microsoft.gctoolkit.event.generational.ConcurrentMark;
import com.microsoft.gctoolkit.event.generational.ConcurrentModeFailure;
import com.microsoft.gctoolkit.event.generational.ConcurrentPreClean;
import com.microsoft.gctoolkit.event.generational.ConcurrentReset;
import com.microsoft.gctoolkit.event.generational.ConcurrentSweep;
import com.microsoft.gctoolkit.event.generational.FullGC;
import com.microsoft.gctoolkit.event.generational.InitialMark;
import com.microsoft.gctoolkit.event.generational.PSFullGC;
import com.microsoft.gctoolkit.event.generational.PSYoungGen;
import com.microsoft.gctoolkit.event.generational.ParNew;
import com.microsoft.gctoolkit.event.generational.SystemGC;
import com.microsoft.gctoolkit.event.generational.YoungGC;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedDiarizer;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GenerationalHeapParserTest extends ParserTest {

    //todo: reactivate this code.

    @Test
    public void canParseLFSFullGC() {

        String[] lines = {"2019-11-14T23:50:29.896+0000: 91334.028: [Full GC (Allocation Failure) Before GC:",
                "Statistics for BinaryTreeDictionary:",
                "------------------------------------",
                        "Total Free Space: 1261",
                        "Max   Chunk Size: 1261",
                        "Number of Blocks: 1",
                        "Av.  Block  Size: 1261",
                        "Tree      Height: 1",
                        "2019-11-14T23:50:29.896+0000: 91334.028: [CMSCMS: Large block 0x00000007bfffd898",
                        ": 2097142K->2097142K(2097152K), 1.9092744 secs] 4063215K->4063215K(4063232K), [Metaspace: 99441K->99441K(1140736K)]After GC:",
                        "Statistics for BinaryTreeDictionary:",
                        "------------------------------------",
                        "Total Free Space: 1261",
                        "Max   Chunk Size: 1261",
                        "Number of Blocks: 1",
                        "Av.  Block  Size: 1261",
                        "Tree      Height: 1",
                        ", 1.9094806 secs] [Times: user=1.91 sys=0.00, real=1.91 secs]"
        };

        List<JVMEvent> jvmEvents = feedParser(lines);

        try {
            FullGC fgc = (FullGC) jvmEvents.get(0);
            Assertions.assertEquals(1.9094806d, fgc.getDuration());
            Assertions.assertEquals(4063232, fgc.getHeap().getSizeAfterCollection());
        } catch(Throwable t) {
            fail(t);
        }
    }

    @Test
    public void testConcurrentModeFailure() {
        String[] lines = new String[]{
                "27.537: [GC [1 CMS-initial-mark: 44399K(64768K)] 60362K(83392K), 0.0008727 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]",
                "27.538: [CMS-concurrent-mark-start]",
                "27.626: [CMS-concurrent-mark: 0.070/0.089 secs] [Times: user=0.14 sys=0.00, real=0.09 secs]",
                "27.627: [CMS-concurrent-preclean-start]",
                "27.627: [CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]",
                "27.627: [CMS-concurrent-abortable-preclean-start]",
                "28.437: [GC 28.437: [ParNew (promotion failed)",
                "Desired survivor size 1343488 bytes, new threshold 3 (max 4)",
                "- age   1:     722736 bytes,     722736 total",
                "- age   2:      84464 bytes,     807200 total",
                "- age   3:     616504 bytes,    1423704 total",
                "- age   4:         40 bytes,    1423744 total",
                ": 17194K->17392K(18624K), 0.0023005 secs]28.440: [CMS28.440: [CMS-concurrent-abortable-preclean: 0.032/0.813 secs] [Times: user=1.30 sys=0.06, real=0.81 secs]",
                " (concurrent mode failure): 62354K->8302K(64768K), 0.0931888 secs] 79477K->8302K(83392K), [CMS Perm : 10698K->10698K(21248K)], 0.0956950 secs] [Times: user=0.09 sys=0.00, real=0.09 secs]",
                GCLogParser.END_OF_DATA_SENTINEL
        };

        List<JVMEvent> jvmEvents = feedParser(lines);

        try {
            InitialMark initialMark = (InitialMark) jvmEvents.get(0);
            ConcurrentMark concurrentMark = (ConcurrentMark) jvmEvents.get(1);
            ConcurrentPreClean concurrentPreClean = (ConcurrentPreClean) jvmEvents.get(2);
            AbortablePreClean abortablePreClean = (AbortablePreClean) jvmEvents.get(3);
        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
        assertTrue(true);
    }

    @Test
    public void testReferenceProcessingInConcurrentCycle() {
        String[] lines = new String[]{
                "2016-04-01T15:03:41.107-0700: 11024.572: [GC (CMS Initial Mark) [1 CMS-initial-mark: 3165109K(4218880K)] 3203364K(5946048K), 0.0583850 secs] [Times: user=0.06 sys=0.00, real=0.06 secs]",
                "2016-04-01T15:03:41.167-0700: 11024.632: [CMS-concurrent-mark-start]",
                "2016-04-01T15:03:42.171-0700: 11025.636: [CMS-concurrent-mark: 0.991/1.004 secs] [Times: user=3.04 sys=0.13, real=1.00 secs]",
                "2016-04-01T15:03:42.171-0700: 11025.637: [CMS-concurrent-preclean-start]",
                "2016-04-01T15:03:42.171-0700: 11025.637: [Preclean SoftReferences, 0.0000530 secs]2016-04-01T15:03:42.172-0700: 11025.637: [Preclean WeakReferences, 0.0006860 secs]2016-04-01T15:03:42.172-0700: 11025.638: [Preclean FinalReferences, 0.0005450 secs]2016-04-01T15:03:42.173-0700: 11025.639: [Preclean PhantomReferences, 0.0000230 secs]2016-04-01T15:03:42.197-0700: 11025.663: [CMS-concurrent-preclean: 0.025/0.026 secs] [Times: user=0.04 sys=0.01, real=0.03 secs]",
                "2016-04-01T15:03:42.198-0700: 11025.664: [CMS-concurrent-abortable-preclean-start]",
                " CMS: abort preclean due to time 2016-04-01T15:03:47.227-0700: 11030.693: [CMS-concurrent-abortable-preclean: 5.002/5.029 secs] [Times: user=7.19 sys=0.28, real=5.03 secs]",
                "2016-04-01T15:03:47.240-0700: 11030.706: [GC (CMS Final Remark)[YG occupancy: 1334704 K (1727168 K)]2016-04-01T15:03:47.241-0700: 11030.707: [Rescan (parallel) , 0.2638220 secs]2016-04-01T15:03:47.505-0700: 11030.970: [weak refs processing2016-04-01T15:03:47.505-0700: 11030.970: [SoftReference, 92 refs, 0.0000520 secs]2016-04-01T15:03:47.505-0700: 11030.971: [WeakReference, 1075 refs, 0.0003030 secs]2016-04-01T15:03:47.505-0700: 11030.971: [FinalReference, 5581 refs, 0.0232140 secs]2016-04-01T15:03:47.528-0700: 11030.994: [PhantomReference, 0 refs, 0.0000200 secs]2016-04-01T15:03:47.528-0700: 11030.994: [JNI Weak Reference, 0.0000270 secs], 0.0237120 secs]2016-04-01T15:03:47.528-0700: 11030.994: [scrub string table, 0.0047220 secs] [1 CMS-remark: 3165109K(4218880K)] 4499814K(5946048K), 0.2937310 secs] [Times: user=1.85 sys=0.00, real=0.30 secs]",
                "2016-04-01T15:03:47.535-0700: 11031.001: [CMS-concurrent-sweep-start]",
                "2016-04-01T15:03:48.111-0700: 11031.577: [CMS-concurrent-sweep: 0.576/0.576 secs] [Times: user=0.67 sys=0.04, real=0.57 secs]",
                "2016-04-01T15:03:48.112-0700: 11031.578: [CMS-concurrent-reset-start]",
                "2016-04-01T15:03:48.139-0700: 11031.605: [CMS-concurrent-reset: 0.027/0.027 secs] [Times: user=0.02 sys=0.00, real=0.03 secs]"
        };

        List<JVMEvent> jvmEvents = feedParser(lines);

        try {
            InitialMark initialMark = (InitialMark) jvmEvents.get(0);
            ConcurrentMark concurrentMark = (ConcurrentMark) jvmEvents.get(1);
            ConcurrentPreClean concurrentPreClean = (ConcurrentPreClean) jvmEvents.get(2);
            AbortablePreClean abortablePreClean = (AbortablePreClean) jvmEvents.get(3);
            CMSRemark remark = (CMSRemark) jvmEvents.get(4);
            ConcurrentSweep sweep = (ConcurrentSweep) jvmEvents.get(5);
            ConcurrentReset reset = (ConcurrentReset) jvmEvents.get(6);
        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
        assertTrue(true);
    }

    @Test
    public void testThat2CMFDoNotHappen() {
        String[][] lines = new String[][]{
                {
                        "57721.729: [GC [1 CMS-initial-mark: 763361K(786432K)] 767767K(1022400K), 0.0022735 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]",
                        "57721.732: [CMS-concurrent-mark-start]",
                        "57722.918: [GC 57722.918: [ParNew: 209792K->26176K(235968K), 0.0618431 secs] 973153K->802453K(1022400K), 0.0620347 secs] [Times: user=0.38 sys=0.00, real=0.06 secs]",
                        "57724.218: [Full GC 57724.218: [CMS2010-04-21T10:45:33.367+0100: 57724.319: [CMS-concurrent-mark: 2.519/2.587 secs] [Times: user=12.58 sys=0.09, real=2.59 secs]",
                        "(concurrent mode failure): 776277K->770654K(786432K), 6.0499857 secs] 1012245K->770654K(1022400K), [CMS Perm : 23211K->23211K(38736K)], 6.0501617 secs] [Times: user=6.09 sys=0.00, real=6.05 secs]"
                },
                {
                        "58272.354: [GC [1 CMS-initial-mark: 786431K(786432K)] 794666K(1022400K), 0.0088514 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]",
                        "58272.363: [CMS-concurrent-mark-start]",
                        "58273.778: [Full GC 58273.778: [CMS2010-04-21T10:54:43.688+0100: 58274.663: [CMS-concurrent-mark: 2.299/2.300 secs] [Times: user=8.69 sys=0.11, real=2.30 secs]",
                        "(concurrent mode failure): 786431K->785452K(786432K), 6.5738696 secs] 1022399K->785452K(1022400K), [CMS Perm : 23211K->23211K(38736K)], 6.5740517 secs] [Times: user=7.44 sys=0.00, real=6.56 secs]"
                }
        };

        Class[][] eventTypes = {
                {InitialMark.class, ConcurrentMark.class, ParNew.class, ConcurrentModeFailure.class},
                {InitialMark.class, ConcurrentMark.class, ConcurrentModeFailure.class}
        };

        for (int i = 0; i < lines.length; i++) {
            List<JVMEvent> jvmEvents = feedParser(lines[i]);

            for (int j = 0; j < eventTypes[i].length; j++) {
                assertEquals(jvmEvents.get(j).getClass(), eventTypes[i][j]);
            }
            jvmEvents.clear();
        }
    }

    @Test
    public void basicLogTest() {
        String[] lines = {
                "9.089: [GC 17024K->1751K(2095040K), 0.0308466 secs",
                "21.464: [GC 18775K->3413K(2095040K), 0.0431857 secs",
                "29.141: [GC 20437K->2693K(2095040K), 0.0045342 secs",
                "33.155: [GC 19717K->3974K(2095040K), 0.0115157 secs",
                "36.966: [GC 20998K->5300K(2095040K), 0.0269848 secs",
                "40.351: [GC 22324K->4799K(2095040K), 0.0071082 secs",
                "43.373: [GC 21823K->5862K(2095040K), 0.0105840 secs",
                "46.464: [GC 22886K->6859K(2095040K), 0.0289186 secs",
                "49.185: [GC 23883K->8778K(2095040K), 0.0303312 secs",
                "51.113: [GC 25802K->8674K(2095040K), 0.0119712 secs",
                "54.218: [GC 25698K->9720K(2095040K), 0.0098641 secs",
                "57.311: [GC 26744K->11179K(2095040K), 0.0212367 secs",
                "60.249: [GC 28203K->11381K(2095040K), 0.0087235 secs",
                "62.602: [GC 28405K->12469K(2095040K), 0.0258451 secs",
                "65.245: [GC 29493K->13720K(2095040K), 0.0188990 secs",
                "67.554: [GC 30744K->15788K(2095040K), 0.0296812 secs",
                "69.923: [GC 32812K->16455K(2095040K), 0.0232905 secs",
                "72.100: [GC 33479K->17013K(2095040K), 0.0100926 secs",
                "74.262: [GC 34037K->18471K(2095040K), 0.0247458 secs",
                "76.203: [GC 35495K->20250K(2095040K), 0.0270935 secs",
                "78.319: [GC 37274K->21322K(2095040K), 0.0234226 secs",
                "80.257: [GC 38346K->21604K(2095040K), 0.0097355 secs",
                "82.298: [GC 38628K->22911K(2095040K), 0.0228335 secs",
                "84.129: [GC 39935K->23835K(2095040K), 0.0203206 secs"
        };

        List<JVMEvent> jvmEvents = feedParser(lines);
        assertEquals(24, jvmEvents.size());
        assertEquals(9.089,getParser().diary.getTimeOfFirstEvent().toSeconds());
    }

    @Test
    // jlittle-ptc: Added to validate changes in https://github.com/microsoft/gctoolkit/issues/352
    // Fails without changes, passes with changes.
    public void parallelYoungGenTest() {
    	String[] lines = {
    			"103.387: [GC [PSYoungGen: 138710K->17821K(154112K)] 138710K->17821K(506368K), 0.1040105 secs]",
    			"135.734: [GC [PSYoungGen: 149917K->22007K(154112K)] 149917K->30709K(506368K), 0.2773358 secs]",
    			"147.102: [GC [PSYoungGen: 154103K->22015K(154112K)] 162805K->57046K(506368K), 0.1371908 secs]",
    			"156.646: [GC [PSYoungGen: 154111K->22001K(152320K)] 189142K->84340K(504576K), 0.1508722 secs]",
    			"5989.323: [Full GC [PSYoungGen: 384K->0K(46208K)] [PSOldGen: 351520K->291209K(675840K)] 351904K->291209K(722048K) [PSPermGen: 46324K->46324K(94208K)], 5.6953760 secs]",
    			"12023.260: [Full GC [PSYoungGen: 1463K->0K(172160K)] [PSOldGen: 674819K->546126K(917504K)] 676283K->546126K(1089664K) [PSPermGen: 75971K->75971K(126976K)], 12.4914242 secs]",
    			"132380.027: [Full GC [PSYoungGen: 388637K->0K(581376K)] [PSOldGen: 1707204K->1099190K(1708032K)] 2095842K->1099190K(2289408K) [PSPermGen: 103975K->103975K(122880K)], 25.3055946 secs]"
    	};
    	
        List<JVMEvent> jvmEvents = feedParser(lines);
        assertEquals(7, jvmEvents.size());
        assertEquals(103.387, getParser().diary.getTimeOfFirstEvent().toSeconds());    	
    }

    @Test
    // jlittle-ptc: Added to validate changes in https://github.com/microsoft/gctoolkit/issues/433
    // Fails without changes, passes with changes.
    public void psYoungNoDetailsTest() {
    	String[] lines = {
    			"13.563: [GC (Allocation Failure)  886080K->31608K(1986432K), 0.0392109 secs]",
    	};
    	
        List<JVMEvent> jvmEvents = feedParser(lines);
        assertEquals(1, jvmEvents.size());
        
        assertTrue(jvmEvents.get(0) instanceof PSYoungGen);

        PSYoungGen evt = (PSYoungGen) jvmEvents.get(0);
        assertMemoryPoolValues(evt.getHeap(), 886080, 1986432, 31608, 1986432);
    }
    
	@Test
	void testGenerationalNoDetailsLines() {
		String[] lines = {
				// 0 - Normal GC
				"75.240: [GC 525312K->16552K(2013696K), 0.1105640 secs]",
				
				// 1 - Full GC
				"8357.379: [Full GC 1379524K->1236761K(2787392K), 43.3665438 secs]",
				
				// 2 - InitialMark
				"25293.283: [GC 2548071K(3028408K), 0.0743383 secs]"
		};
		
		int expectedEventCount = 3;
		
    	List<JVMEvent> jvmEvents = feedParser(lines);
    	assertEquals(jvmEvents.size(), expectedEventCount);
    	
		// 0 - YoungGC
    	assertTrue(jvmEvents.get(0) instanceof YoungGC);
    	YoungGC evt0 = ((YoungGC) jvmEvents.get(0));
    	assertEquals(evt0.getDateTimeStamp(), new DateTimeStamp(75.240));
    	assertMemoryPoolValues(evt0.getHeap(), 525312, 2013696, 16552, 2013696);
    	assertDoubleEquals(evt0.getDuration(), 0.1105640);

		// 1 - FullGC
    	assertTrue(jvmEvents.get(1) instanceof FullGC);
    	FullGC evt1 = ((FullGC) jvmEvents.get(1));
    	assertEquals(evt1.getDateTimeStamp(), new DateTimeStamp(8357.379));
    	assertMemoryPoolValues(evt1.getHeap(), 1379524, 2787392, 1236761, 2787392);
    	assertDoubleEquals(evt1.getDuration(), 43.3665438);
    	
    	// 2 - InitialMark
    	assertTrue(jvmEvents.get(2) instanceof InitialMark);
    	InitialMark evt2 = ((InitialMark) jvmEvents.get(2));
    	assertEquals(evt2.getDateTimeStamp(), new DateTimeStamp(25293.283));
    	assertMemoryPoolValues(evt2.getHeap(), 2548071, 3028408, 2548071, 3028408);
    	assertDoubleEquals(evt2.getDuration(), 0.0743383);		    	
	}
	
	@Test
	void testCMSGenerationalNoDetailsLines() {
		String[] lines = {
				"13.563: [GC (Allocation Failure)  886080K->31608K(1986432K), 0.0392109 secs]",
				
				// Currently Ignored
				//"10.254: [GC (CMS Initial Mark)  460775K(1986432K), 0.0346332 secs]",
				//"15.423: [GC (CMS Final Remark)  168951K(1986432K), 0.0388223 secs]"
		};
		
		int expectedEventCount = 1;
		
    	List<JVMEvent> jvmEvents = feedParser(lines);
    	assertEquals(jvmEvents.size(), expectedEventCount);
    	
		// 0 - PSYoungGen
    	assertTrue(jvmEvents.get(0) instanceof PSYoungGen);
    	PSYoungGen evt0 = ((PSYoungGen) jvmEvents.get(0));
    	assertEquals(evt0.getDateTimeStamp(), new DateTimeStamp(13.563));
    	assertMemoryPoolValues(evt0.getHeap(), 886080, 1986432, 31608, 1986432);
    	assertDoubleEquals(evt0.getDuration(), 0.0392109);
	}
    
	@Test
	void testGenerationalDetailsLines() {
		String[] lines = {
				// 0 - PSFullGC
				"2021-12-17T13:34:54.484+0000: 11.445: [Full GC (Metadata GC Threshold) [PSYoungGen: 19070K->0K(475648K)] [ParOldGen: 0K->18752K(1086976K)] 19070K->18752K(1562624K), [Metaspace: 20797K->20797K(1069056K)], 0.3274726 secs] [Times: user=0.11 sys=0.20, real=0.33 secs] ",
				
				// 1 - PSYoungGen
				"2021-12-17T13:35:02.874+0000: 19.835: [GC (Metadata GC Threshold) [PSYoungGen: 189709K->8219K(475648K)] 208461K->26971K(1562624K), 0.1027594 secs] [Times: user=0.09 sys=0.10, real=0.10 secs] ",
				
				// 2 - PSYoungGen
				"2021-12-17T13:35:24.328+0000: 41.289: [GC (Allocation Failure) [PSYoungGen: 408064K->37889K(475648K)] 442767K->72593K(1562624K), 0.2413144 secs] [Times: user=0.00 sys=0.24, real=0.24 secs] ",
				
				// 3 - PSYoungGen
				"2021-12-17T13:36:02.418+0000: 79.379: [GC (System.gc()) [PSYoungGen: 169657K->10443K(423936K)] 329477K->179021K(1510912K), 0.1649078 secs] [Times: user=0.02 sys=0.11, real=0.16 secs] ",
				
				// 4 - SystemGC
				"2021-12-17T13:36:02.583+0000: 79.544: [Full GC (System.gc()) [PSYoungGen: 10443K->0K(423936K)] [ParOldGen: 168577K->168475K(1086976K)] 179021K->168475K(1510912K), [Metaspace: 119955K->118885K(1155072K)], 1.2929194 secs] [Times: user=0.98 sys=0.25, real=1.29 secs] ",
				
				// 5 - PSFullGC
				"2021-12-17T14:58:05.098+0000: 5002.059: [Full GC (Ergonomics) [PSYoungGen: 60759K->0K(414208K)] [ParOldGen: 974100K->1024865K(1086976K)] 1034860K->1024865K(1501184K), [Metaspace: 154915K->154915K(1187840K)], 0.7945447 secs] [Times: user=0.68 sys=0.06, real=0.79 secs] "
		};
		
		int expectedEventCount = 6;
		
    	List<JVMEvent> jvmEvents = feedParser(lines);
    	assertEquals(jvmEvents.size(), expectedEventCount);
    	
		// 0 - PSFullGC
    	assertTrue(jvmEvents.get(0) instanceof PSFullGC);
    	PSFullGC evt0 = ((PSFullGC) jvmEvents.get(0));
    	assertEquals(evt0.getDateTimeStamp(), new DateTimeStamp("2021-12-17T13:34:54.484+0000", 11.445));
    	assertEquals(evt0.getGCCause(), GCCause.METADATA_GENERATION_THRESHOLD);
    	// Memory Pools
    	assertMemoryPoolValues(evt0.getHeap(), 19070, 1562624, 18752, 1562624);
    	assertMemoryPoolValues(evt0.getYoung(), 19070, 475648, 0, 475648);
    	assertMemoryPoolValues(evt0.getTenured(), 0, 1086976, 18752, 1086976);
    	assertMemoryPoolValues(evt0.getPermOrMetaspace(), 20797, 1069056, 20797, 1069056);
    	assertDoubleEquals(evt0.getDuration(), 0.3274726);
		assertCPUSummaryValues(evt0.getCpuSummary(), 0.11, 0.20, 0.33);
		
		// 1 - PSYoungGen
    	assertTrue(jvmEvents.get(1) instanceof PSYoungGen);
    	PSYoungGen evt1 = ((PSYoungGen) jvmEvents.get(1));
    	assertEquals(evt1.getDateTimeStamp(), new DateTimeStamp("2021-12-17T13:35:02.874+0000", 19.835));
    	assertEquals(evt1.getGCCause(), GCCause.METADATA_GENERATION_THRESHOLD);
    	// Memory Pools
    	assertMemoryPoolValues(evt1.getHeap(), 208461, 1562624, 26971, 1562624);
    	assertMemoryPoolValues(evt1.getYoung(), 189709, 475648, 8219, 475648);
    	assertDoubleEquals(evt1.getDuration(), 0.1027594);
		assertCPUSummaryValues(evt1.getCpuSummary(), 0.09, 0.10, 0.10);	

		// 2 - PSYoungGen
    	assertTrue(jvmEvents.get(2) instanceof PSYoungGen);
    	PSYoungGen evt2 = ((PSYoungGen) jvmEvents.get(2));
    	assertEquals(evt2.getDateTimeStamp(), new DateTimeStamp("2021-12-17T13:35:24.328+0000", 41.289));
    	assertEquals(evt2.getGCCause(), GCCause.ALLOCATION_FAILURE);
    	// Memory Pools
    	assertMemoryPoolValues(evt2.getHeap(), 442767, 1562624, 72593, 1562624);
    	assertMemoryPoolValues(evt2.getYoung(), 408064, 475648, 37889, 475648);
    	assertDoubleEquals(evt2.getDuration(), 0.2413144);
		assertCPUSummaryValues(evt2.getCpuSummary(), 0.0, 0.24, 0.24);	
		
		// 3 - PSYoungGen (SystemGC)
    	assertTrue(jvmEvents.get(3) instanceof PSYoungGen);
    	PSYoungGen evt3 = ((PSYoungGen) jvmEvents.get(3));
    	assertEquals(evt3.getDateTimeStamp(), new DateTimeStamp("2021-12-17T13:36:02.418+0000", 79.379));
    	assertEquals(evt3.getGCCause(), GCCause.JAVA_LANG_SYSTEM);
    	// Memory Pools
    	assertMemoryPoolValues(evt3.getHeap(), 329477, 1510912, 179021, 1510912);
    	assertMemoryPoolValues(evt3.getYoung(), 169657, 423936, 10443, 423936);
    	assertDoubleEquals(evt3.getDuration(), 0.1649078);
		assertCPUSummaryValues(evt3.getCpuSummary(), 0.02, 0.11, 0.16);	
		
		// 4 - PSFullGC (SystemGC)
    	assertTrue(jvmEvents.get(4) instanceof SystemGC);
    	SystemGC evt4 = ((SystemGC) jvmEvents.get(4));
    	assertEquals(evt4.getDateTimeStamp(), new DateTimeStamp("2021-12-17T13:36:02.583+0000", 79.544));
    	assertEquals(evt4.getGCCause(), GCCause.JAVA_LANG_SYSTEM);
    	// Memory Pools
    	assertMemoryPoolValues(evt4.getHeap(), 179021, 1510912, 168475, 1510912);
    	assertMemoryPoolValues(evt4.getYoung(), 10443, 423936, 0, 423936);
    	assertMemoryPoolValues(evt4.getTenured(), 168577, 1086976, 168475, 1086976);
    	assertMemoryPoolValues(evt4.getPermOrMetaspace(), 119955, 1155072, 118885, 1155072);
    	assertDoubleEquals(evt4.getDuration(), 1.2929194);
		assertCPUSummaryValues(evt4.getCpuSummary(), 0.98, 0.25, 1.29);

		// 5 - PSFullGC (Ergonomics)
    	assertTrue(jvmEvents.get(5) instanceof PSFullGC);
    	PSFullGC evt5 = ((PSFullGC) jvmEvents.get(5));
    	assertEquals(evt5.getDateTimeStamp(), new DateTimeStamp("2021-12-17T14:58:05.098+0000", 5002.059));
    	assertEquals(evt5.getGCCause(), GCCause.ADAPTIVE_SIZE_POLICY);
    	// Memory Pools
    	assertMemoryPoolValues(evt5.getHeap(), 1034860, 1501184, 1024865, 1501184);
    	assertMemoryPoolValues(evt5.getYoung(), 60759, 414208, 0, 414208);
    	assertMemoryPoolValues(evt5.getTenured(), 974100, 1086976, 1024865, 1086976);
    	assertMemoryPoolValues(evt5.getPermOrMetaspace(), 154915, 1187840, 154915, 1187840);
    	assertDoubleEquals(evt5.getDuration(), 0.7945447);
		assertCPUSummaryValues(evt5.getCpuSummary(), 0.68, 0.06, 0.79);				
	}

	
    @Override
    protected Diarizer diarizer() {
        return new PreUnifiedDiarizer();
    }

    @Override
    protected GCLogParser parser() {
        return new GenerationalHeapParser();
    }

}
