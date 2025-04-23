// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;


import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.generational.AbortablePreClean;
import com.microsoft.gctoolkit.event.generational.CMSRemark;
import com.microsoft.gctoolkit.event.generational.ConcurrentMark;
import com.microsoft.gctoolkit.event.generational.ConcurrentPreClean;
import com.microsoft.gctoolkit.event.generational.ConcurrentReset;
import com.microsoft.gctoolkit.event.generational.ConcurrentSweep;
import com.microsoft.gctoolkit.event.generational.DefNew;
import com.microsoft.gctoolkit.event.generational.FullGC;
import com.microsoft.gctoolkit.event.generational.InitialMark;
import com.microsoft.gctoolkit.event.generational.PSFullGC;
import com.microsoft.gctoolkit.event.generational.PSYoungGen;
import com.microsoft.gctoolkit.event.generational.ParNew;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.SurvivorRecord;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class UnifiedGenerationalEventsTest extends ParserTest {

    @Override
    protected Diarizer diarizer() {
        return new UnifiedDiarizer();
    }

    @Override
    protected GCLogParser parser() {
        return new UnifiedGenerationalParser();
    }

    @Test
    public void testParallelYoungCollection() {
        String[] lines = new String[]{
                "[10.020s][info][gc,start     ] GC(0) Pause Young (Allocation Failure)",
                "[10.025s][debug][gc,phases    ] GC(0) Scavenge 4.410ms",
                "[10.025s][debug][gc,phases    ] GC(0) Reference Processing 0.048ms",
                "[10.025s][debug][gc,phases    ] GC(0) Weak Processing 0.038ms",
                "[10.026s][debug][gc,phases    ] GC(0) Scrub String Table 0.098ms",
                "[10.026s][debug][gc,age       ] GC(0) Desired survivor size 2621440 bytes, new threshold 7 (max threshold 15)",
                "[10.026s][info ][gc,heap      ] GC(0) PSYoungGen: 16384K->2559K(18944K)",
                "[10.026s][info ][gc,heap      ] GC(0) ParOldGen: 0K->2121K(44032K)",
                "[10.026s][info ][gc,metaspace ] GC(0) Metaspace: 15746K->15746K(1062912K)",
                "[10.026s][info ][gc           ] GC(0) Pause Young (Allocation Failure) 16M->4M(61M) 5.423ms",
                "[10.026s][info ][gc,cpu       ] GC(0) User=0.02s Sys=0.01s Real=0.00s",
        };
        List<JVMEvent> jvmEvents = feedParser(lines);
        try {
            assertEquals(1, jvmEvents.size(), "Should be 1 event... ");
            PSYoungGen youngGen = (PSYoungGen) jvmEvents.get(0);
            MemoryPoolSummary poolSummary = youngGen.getHeap();
            assertEquals(poolSummary.getOccupancyBeforeCollection(), 16 * 1024);
            assertEquals(poolSummary.getOccupancyAfterCollection(), 4 * 1024);
            assertEquals(poolSummary.getSizeAfterCollection(), 61 * 1024);

            poolSummary = youngGen.getYoung();
            assertEquals(poolSummary.getOccupancyBeforeCollection(), 16384);
            assertEquals(poolSummary.getOccupancyAfterCollection(), 2559);
            assertEquals(poolSummary.getSizeAfterCollection(), 18944);

            poolSummary = youngGen.getTenured();
            assertEquals(poolSummary.getOccupancyBeforeCollection(), 0);
            assertEquals(poolSummary.getOccupancyAfterCollection(), 2121);
            assertEquals(poolSummary.getSizeAfterCollection(), 44032);

            MemoryPoolSummary metaspaceSummary = youngGen.getPermOrMetaspace();
            assertEquals(metaspaceSummary.getOccupancyBeforeCollection(), 15746);
            assertEquals(metaspaceSummary.getOccupancyAfterCollection(), 15746);
            assertEquals(metaspaceSummary.getSizeAfterCollection(), 1062912);

        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
    }

    @Test
    public void testParallelFUllCollection() {
        String[] lines = new String[]{
                "[10.115s][debug][gc,phases    ] GC(25) Pre Compact 0.022ms",
                "[10.115s][info ][gc,start     ] GC(25) Pause Full (Ergonomics)",
                "[10.116s][info ][gc,phases,start] GC(25) Marking Phase",
                "[10.118s][debug][gc,phases      ] GC(25) Par Mark 2.420ms",
                "[10.118s][debug][gc,phases      ] GC(25) Reference Processing 0.110ms",
                "[10.118s][debug][gc,phases      ] GC(25) Weak Processing 0.011ms",
                "[10.118s][debug][gc,phases      ] GC(25) ClassLoaderData 0.076ms",
                "[10.118s][debug][gc,phases      ] GC(25) ProtectionDomainCacheTable 0.002ms",
                "[10.118s][debug][gc,phases      ] GC(25) ResolvedMethodTable 0.016ms",
                "[10.119s][debug][gc,phases      ] GC(25) Class Unloading 0.795ms",
                "[10.119s][debug][gc,phases      ] GC(25) Scrub String Table 0.060ms",
                "[10.120s][debug][gc,phases      ] GC(25) Scrub Symbol Table 0.980ms",
                "[10.120s][info ][gc,phases      ] GC(25) Marking Phase 4.518ms",
                "[10.120s][info ][gc,phases,start] GC(25) Summary Phase",
                "[10.120s][info ][gc,phases      ] GC(25) Summary Phase 0.013ms",
                "[10.120s][info ][gc,phases,start] GC(25) Adjust Roots",
                "[10.122s][info ][gc,phases      ] GC(25) Adjust Roots 2.423ms",
                "[10.122s][info ][gc,phases,start] GC(25) Compaction Phase",
                "[10.128s][info ][gc,phases      ] GC(25) Compaction Phase 5.461ms",
                "[10.128s][info ][gc,phases,start] GC(25) Post Compact",
                "[10.129s][info ][gc,phases      ] GC(25) Post Compact 0.974ms",
                "[10.130s][info ][gc,heap        ] GC(25) PSYoungGen: 13467K->0K(16896K)",
                "[10.130s][info ][gc,heap        ] GC(25) ParOldGen: 42920K->7823K(26624K)",
                "[10.130s][info ][gc,metaspace   ] GC(25) Metaspace: 15855K->15855K(1064960K)",
                "[10.130s][info ][gc             ] GC(25) Pause Full (Ergonomics) 55M->7M(42M) 14.092ms",
                "[10.130s][info ][gc,cpu         ] GC(25) User=0.04s Sys=0.00s Real=0.02s"
        };
        List<JVMEvent> jvmEvents = feedParser(lines);
        try {
            assertEquals(1, jvmEvents.size(), "Should be 1 event... ");
            PSFullGC full = (PSFullGC) jvmEvents.get(0);
            assertEquals(full.getHeap().getOccupancyBeforeCollection(), 55 * 1024);
            assertEquals(full.getHeap().getOccupancyAfterCollection(), 7 * 1024);
            assertEquals(full.getHeap().getSizeAfterCollection(), 42 * 1024);
            assertEquals(0.014092, full.getDuration());
        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
    }

    @Test
    public void testParNewCollection() {
        String[] lines = new String[]{
                "[31.191s][info ][gc,start     ] GC(17) Pause Young (Allocation Failure)",
                "[31.192s][info ][gc,task      ] GC(17) Using 8 workers of 8 for evacuation",
                "[31.193s][debug][gc,age       ] GC(17) Desired survivor size 1114112 bytes, new threshold 1 (max threshold 6)",
                "[31.193s][trace][gc,age       ] GC(17) Age table with threshold 1 (max threshold 6)",
                "[31.193s][trace][gc,age       ] GC(17) - age   1:    2005920 bytes,    2005920 total",
                "[31.193s][info ][gc,heap      ] GC(17) ParNew: 18767K->1967K(19648K)",
                "[31.193s][info ][gc,heap      ] GC(17) CMS: 13279K->14072K(43712K)",
                "[31.193s][info ][gc,metaspace ] GC(17) Metaspace: 15819K->15819K(1064960K)",
                "[31.193s][info ][gc           ] GC(17) Pause Young (Allocation Failure) 31M->15M(61M) 1.877ms",
                "[31.193s][info ][gc,cpu       ] GC(17) User=0.01s Sys=0.00s Real=0.00s"
        };

        List<JVMEvent> jvmEvents = feedParser(lines);
        try {
            assertEquals(1, jvmEvents.size());
            ParNew collection = (ParNew) jvmEvents.get(0);
            assertEquals(31 * 1024, collection.getHeap().getOccupancyBeforeCollection());
            assertEquals(15 * 1024, collection.getHeap().getOccupancyAfterCollection());
            assertEquals(61 * 1024, collection.getHeap().getSizeAfterCollection());
            assertEquals(1877, Math.round(collection.getDuration() * 1000000.0d));

            MemoryPoolSummary poolSummary = collection.getYoung();
            assertEquals(poolSummary.getOccupancyBeforeCollection(), 18767);
            assertEquals(poolSummary.getOccupancyAfterCollection(), 1967);
            assertEquals(poolSummary.getSizeAfterCollection(), 19648);

            poolSummary = collection.getTenured();
            assertEquals(poolSummary.getOccupancyBeforeCollection(), 13279);
            assertEquals(poolSummary.getOccupancyAfterCollection(), 14072);
            assertEquals(poolSummary.getSizeAfterCollection(), 43712);

            MemoryPoolSummary metaspaceSummary = collection.getPermOrMetaspace();
            assertEquals(metaspaceSummary.getOccupancyBeforeCollection(), 15819);
            assertEquals(metaspaceSummary.getOccupancyAfterCollection(), 15819);
            assertEquals(metaspaceSummary.getSizeAfterCollection(), 1064960);
        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
        assertTrue(true);
    }

    @Test
    public void testCMSCycleCollection() {
        String[] lines = new String[]{
                "[31.228s][info ][gc,start     ] GC(29) Pause Initial Mark",
                "[31.229s][info ][gc           ] GC(29) Pause Initial Mark 24M->24M(61M) 0.240ms",
                "[31.229s][info ][gc,cpu       ] GC(29) User=0.00s Sys=0.00s Real=0.00s",
                "[31.229s][info ][gc           ] GC(29) Concurrent Mark",
                "[31.230s][info ][gc,task      ] GC(29) Using 2 workers of 2 for marking",
                "[31.259s][info ][gc           ] GC(29) Concurrent Mark 30.285ms",
                "[31.259s][info ][gc,cpu       ] GC(29) User=0.16s Sys=0.01s Real=0.03s",
                "[31.259s][info ][gc           ] GC(29) Concurrent Preclean",
                "[31.262s][info ][gc           ] GC(29) Concurrent Preclean 2.368ms",
                "[31.262s][info ][gc,cpu       ] GC(29) User=0.01s Sys=0.00s Real=0.00s",
                "[31.262s][info ][gc           ] GC(29) Concurrent Abortable Preclean",
                "[31.264s][info ][gc           ] GC(29) Concurrent Abortable Preclean 2.394ms",
                "[31.264s][info ][gc,cpu       ] GC(29) User=0.01s Sys=0.00s Real=0.00s",
                "[31.264s][info ][gc,start     ] GC(29) Pause Remark",
                "[31.265s][debug][gc,phases    ] GC(29) Rescan (parallel) 0.862ms",
                "[31.265s][debug][gc,phases    ] GC(29) Reference Processing 0.082ms",
                "[31.265s][debug][gc,phases    ] GC(29) Weak Processing 0.014ms",
                "[31.265s][debug][gc,phases    ] GC(29) ClassLoaderData 0.080ms",
                "[31.265s][debug][gc,phases    ] GC(29) ProtectionDomainCacheTable 0.003ms",
                "[31.265s][debug][gc,phases    ] GC(29) ResolvedMethodTable 0.014ms",
                "[31.266s][debug][gc,phases    ] GC(29) Class Unloading 0.787ms",
                "[31.267s][debug][gc,phases    ] GC(29) Scrub Symbol Table 0.978ms",
                "[31.267s][debug][gc,phases    ] GC(29) Scrub String Table 0.065ms",
                "[31.267s][info ][gc           ] GC(29) Pause Remark 26M->26M(61M) 2.919ms",
                "[31.267s][info ][gc,cpu       ] GC(29) User=0.01s Sys=0.00s Real=0.00s",
                "[31.267s][info ][gc           ] GC(29) Concurrent Sweep",
                "[31.271s][info ][gc           ] GC(29) Concurrent Sweep 4.130ms",
                "[31.271s][info ][gc,cpu       ] GC(29) User=0.02s Sys=0.00s Real=0.01s",
                "[31.273s][info ][gc           ] GC(29) Concurrent Reset",
                "[31.276s][info ][gc           ] GC(29) Concurrent Reset 2.716ms",
                "[31.276s][info ][gc,cpu       ] GC(29) User=0.01s Sys=0.00s Real=0.00s"
        };
        List<JVMEvent> jvmEvents = feedParser(lines);
        try {
            assertEquals(7, jvmEvents.size());
            InitialMark initialMark = (InitialMark) jvmEvents.get(0);
            assertEquals(24 * 1024, initialMark.getHeap().getOccupancyBeforeCollection());
            assertEquals(24 * 1024, initialMark.getHeap().getOccupancyAfterCollection());
            assertEquals(61 * 1024, initialMark.getHeap().getSizeAfterCollection());
            assertEquals(240, Math.round(initialMark.getDuration() * 1000000.0d));

            ConcurrentMark concurrentMark = (ConcurrentMark) jvmEvents.get(1);
            assertEquals(30285, Math.round(concurrentMark.getDuration() * 1000000.0d));
            ConcurrentPreClean preClean = (ConcurrentPreClean) jvmEvents.get(2);
            assertEquals(2368, Math.round(preClean.getDuration() * 1000000.0d));
            AbortablePreClean abortablePreClean = (AbortablePreClean) jvmEvents.get(3);
            assertEquals(2394, Math.round(abortablePreClean.getDuration() * 1000000.0d));

            CMSRemark remark = (CMSRemark) jvmEvents.get(4);
            assertEquals(26 * 1024, remark.getHeap().getOccupancyBeforeCollection());
            assertEquals(26 * 1024, remark.getHeap().getOccupancyAfterCollection());
            assertEquals(61 * 1024, remark.getHeap().getSizeAfterCollection());
            assertEquals(2919, (int) (remark.getDuration() * 1000000.0d));

            ConcurrentSweep sweep = (ConcurrentSweep) jvmEvents.get(5);
            assertEquals(4130, Math.round(sweep.getDuration() * 1000000.0d));
            ConcurrentReset reset = (ConcurrentReset) jvmEvents.get(6);
            assertEquals(2716, Math.round(reset.getDuration() * 1000000.0d));
        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
    }

    @Test
    public void testDefNewCollection() {
        String[] lines = new String[]{
                "[11.900s][info][gc,start     ] GC(0) Pause Young (Allocation Failure)",
                "[11.910s][debug][gc,age       ] GC(0) Desired survivor size 1114112 bytes, new threshold 1 (max threshold 15)",
                "[11.910s][info ][gc,heap      ] GC(0) DefNew: 17294K->2176K(19648K)",
                "[11.910s][info ][gc,heap      ] GC(0) Tenured: 0K->1387K(43712K)",
                "[11.910s][info ][gc,metaspace ] GC(0) Metaspace: 16282K->16282K(1064960K)",
                "[11.910s][info ][gc           ] GC(0) Pause Young (Allocation Failure) 16M->3M(61M) 10.585ms",
                "[11.910s][info ][gc,cpu       ] GC(0) User=0.01s Sys=0.00s Real=0.01s"
        };
        List<JVMEvent> jvmEvents = feedParser(lines);
        try {
            assertEquals(1, jvmEvents.size());
            DefNew collection = (DefNew) jvmEvents.get(0);
            assertEquals(16 * 1024, collection.getHeap().getOccupancyBeforeCollection());
            assertEquals(3 * 1024, collection.getHeap().getOccupancyAfterCollection());
            assertEquals(61 * 1024, collection.getHeap().getSizeAfterCollection());
            assertEquals(10585, Math.round(collection.getDuration() * 1000000.0d));

            MemoryPoolSummary poolSummary = collection.getYoung();
            assertEquals(17294, poolSummary.getOccupancyBeforeCollection());
            assertEquals(2176, poolSummary.getOccupancyAfterCollection());
            assertEquals(19648, poolSummary.getSizeAfterCollection());

            poolSummary = collection.getTenured();
            assertEquals(0, poolSummary.getOccupancyBeforeCollection());
            assertEquals(1387, poolSummary.getOccupancyAfterCollection());
            assertEquals(43712, poolSummary.getSizeAfterCollection());

            MemoryPoolSummary metaspaceSummary = collection.getPermOrMetaspace();
            assertEquals(16282, metaspaceSummary.getOccupancyBeforeCollection());
            assertEquals(16282, metaspaceSummary.getOccupancyAfterCollection());
            assertEquals(1064960, metaspaceSummary.getSizeAfterCollection());
        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
    }

    @Test
    public void testSerialFullCollection() {
        String[] lines = new String[]{
                "[12.188s][info ][gc,start     ] GC(112) Pause Young (Allocation Failure)",
                "[12.188s][info ][gc,start     ] GC(113) Pause Full (Allocation Failure)",
                "[12.188s][info ][gc,phases,start] GC(113) Phase 1: Mark live objects",
                "[12.191s][debug][gc,phases      ] GC(113) Reference Processing 0.073ms",
                "[12.191s][debug][gc,phases      ] GC(113) Weak Processing 0.018ms",
                "[12.192s][debug][gc,phases      ] GC(113) ClassLoaderData 0.114ms",
                "[12.192s][debug][gc,phases      ] GC(113) ProtectionDomainCacheTable 0.003ms",
                "[12.192s][debug][gc,phases      ] GC(113) ResolvedMethodTable 0.019ms",
                "[12.192s][debug][gc,phases      ] GC(113) Class Unloading 0.874ms",
                "[12.192s][debug][gc,phases      ] GC(113) Scrub String Table 0.064ms",
                "[12.193s][debug][gc,phases      ] GC(113) Scrub Symbol Table 0.899ms",
                "[12.193s][info ][gc,phases      ] GC(113) Phase 1: Mark live objects 5.234ms",
                "[12.193s][info ][gc,phases,start] GC(113) Phase 2: Compute new object addresses",
                "[12.194s][info ][gc,phases      ] GC(113) Phase 2: Compute new object addresses 0.965ms",
                "[12.194s][info ][gc,phases,start] GC(113) Phase 3: Adjust pointers",
                "[12.198s][info ][gc,phases      ] GC(113) Phase 3: Adjust pointers 3.706ms",
                "[12.198s][info ][gc,phases,start] GC(113) Phase 4: Move objects",
                "[12.199s][info ][gc,phases      ] GC(113) Phase 4: Move objects 0.523ms",
                "[12.199s][info ][gc             ] GC(113) Pause Full (Allocation Failure) 61M->6M(61M) 10.749ms",
                "[12.199s][info ][gc,heap        ] GC(112) DefNew: 19131K->0K(19648K)",
                "[12.199s][info ][gc,heap        ] GC(112) Tenured: 43531K->6759K(43712K)",
                "[12.199s][info ][gc,metaspace   ] GC(112) Metaspace: 16397K->16397K(1064960K)",
                "[12.199s][info ][gc             ] GC(112) Pause Young (Allocation Failure) 61M->6M(61M) 10.878ms",
                "[12.199s][info ][gc,cpu         ] GC(112) User=0.02s Sys=0.00s Real=0.01s"
        };
        List<JVMEvent> jvmEvents = feedParser(lines);
        try {
            assertEquals(1, jvmEvents.size());
            FullGC collection = (FullGC) jvmEvents.get(0);
            assertEquals(61 * 1024, collection.getHeap().getOccupancyBeforeCollection());
            assertEquals(6 * 1024, collection.getHeap().getOccupancyAfterCollection());
            assertEquals(61 * 1024, collection.getHeap().getSizeAfterCollection());
            assertEquals(10878, Math.round(collection.getDuration() * 1000000.0d));
        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
    }

    @Test
    public void testEnhancedMetaspaceRecord() {
        String[] lines = new String[]{
                "[0.563s][info][gc,start     ] GC(2) Pause Young (Allocation Failure)",
                "[0.694s][info][gc,heap      ] GC(2) PSYoungGen: 53228K(53248K)->7148K(53248K) Eden: 46080K(46080K)->0K(46080K) From: 7148K(7168K)->7148K(7168K)",
                "[0.694s][info][gc,heap      ] GC(2) ParOldGen: 65568K(121856K)->111512K(121856K)",
                "[0.694s][info][gc,metaspace ] GC(2) Metaspace: 3646K(4864K)->3646K(4864K) NonClass: 3271K(4352K)->3271K(4352K) Class: 375K(512K)->375K(512K)",
                "[0.694s][info][gc           ] GC(2) Pause Young (Allocation Failure) 116M->115M(171M) 131.613ms",
                "[0.694s][info][gc,cpu       ] GC(2) User=0.33s Sys=0.02s Real=0.13s"
        };
        List<JVMEvent> jvmEvents = feedParser(lines);
        try {
            assertEquals(1, jvmEvents.size());
            PSYoungGen collection = (PSYoungGen) jvmEvents.get(0);
            assertEquals(53228, collection.getYoung().getOccupancyBeforeCollection());
            assertEquals(7148, collection.getYoung().getOccupancyAfterCollection());
            assertEquals(53248, collection.getYoung().getSizeAfterCollection());
            assertEquals(65568, collection.getTenured().getOccupancyBeforeCollection());
            assertEquals(111512, collection.getTenured().getOccupancyAfterCollection());
            assertEquals(121856, collection.getTenured().getSizeAfterCollection());
            assertEquals(116 * 1024, collection.getHeap().getOccupancyBeforeCollection());
            assertEquals(115 * 1024, collection.getHeap().getOccupancyAfterCollection());
            assertEquals(171 * 1024, collection.getHeap().getSizeAfterCollection());
            assertEquals(3646, collection.getPermOrMetaspace().getOccupancyBeforeCollection());
            assertEquals(4864, collection.getPermOrMetaspace().getSizeBeforeCollection());
            assertEquals(3646, collection.getPermOrMetaspace().getOccupancyAfterCollection());
            assertEquals(4864, collection.getPermOrMetaspace().getSizeAfterCollection());
            assertEquals(3271, collection.getNonClassspace().getOccupancyBeforeCollection());
            assertEquals(4352, collection.getNonClassspace().getSizeBeforeCollection());
            assertEquals(3271, collection.getNonClassspace().getOccupancyAfterCollection());
            assertEquals(4352, collection.getNonClassspace().getSizeAfterCollection());
            assertEquals(375, collection.getClassspace().getOccupancyBeforeCollection());
            assertEquals(512, collection.getClassspace().getSizeBeforeCollection());
            assertEquals(375, collection.getClassspace().getOccupancyAfterCollection());
            assertEquals(512, collection.getClassspace().getSizeAfterCollection());

        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
    }


    @Test
    public void parallelWithSurvivorRecordsTest() {
        String[] lines = {
                "[9.371s][info ][gc,start       ] GC(18) Pause Young (Allocation Failure)",
                "[9.371s][debug][gc,age         ] GC(18) Desired survivor size 2097152 bytes, new threshold 1 (max threshold 15)",
                "[9.371s][info ][gc,heap        ] GC(18) PSYoungGen: 309244K(311296K)->454K(298496K) Eden: 308694K(308736K)->0K(297984K) From: 550K(2560K)->454K(512K)",
                "[9.371s][info ][gc,heap        ] GC(18) ParOldGen: 4018K(349696K)->4042K(349696K)",
                "[9.371s][info ][gc,metaspace   ] GC(18) Metaspace: 9573K(9856K)->9573K(9856K) NonClass: 8504K(8640K)->8504K(8640K) Class: 1069K(1216K)->1069K(1216K)",
                "[9.371s][info ][gc             ] GC(18) Pause Young (Allocation Failure) 305M->4M(633M) 0.242ms",
                "[9.371s][info ][gc,cpu         ] GC(18) User=0.01s Sys=0.00s Real=0.00s"
        };

        List<JVMEvent> jvmEvents = feedParser(lines);
        assertEquals(1, jvmEvents.size());
        PSYoungGen collection = (PSYoungGen) jvmEvents.get(0);
        assertEquals(9.371, collection.getDateTimeStamp().toSeconds());
        SurvivorRecord record = collection.getSurvivorRecord();
        assertNotNull(record);
        assertEquals(15, record.getMaxTenuringThreshold());
        assertEquals(1, record.getCalculatedTenuringThreshold());
        assertEquals(2097152, record.getDesiredOccupancyAfterCollection());
        assertEquals(0, record.getBytesAtEachAge().length);
    }

    @Test
    public void serialWithSurvivorRecordsTest() {

        String[] lines = {"[8.726s][info ][gc,start       ] GC(9) Pause Young (Allocation Failure)",
                "[8.726s][debug][gc,age         ] GC(9) Desired survivor size 8945664 bytes, new threshold 15 (max threshold 15)",
                "[8.726s][trace][gc,age         ] GC(9) Age table with threshold 15 (max threshold 15)",
                "[8.726s][trace][gc,age         ] GC(9) - age   1:     400368 bytes,     400368 total",
                "[8.726s][trace][gc,age         ] GC(9) - age   3:        304 bytes,     400672 total",
                "[8.726s][trace][gc,age         ] GC(9) - age   8:         32 bytes,     400704 total",
                "[8.726s][trace][gc,age         ] GC(9) - age   9:     554176 bytes,     954880 total",
                "[8.726s][info ][gc,heap        ] GC(9) DefNew: 140789K(157376K)->932K(157376K) Eden: 139857K(139904K)->0K(139904K) From: 932K(17472K)->932K(17472K)",
                "[8.726s][info ][gc,heap        ] GC(9) Tenured: 3357K(349568K)->3357K(349568K)",
                "[8.726s][info ][gc,metaspace   ] GC(9) Metaspace: 9628K(9856K)->9628K(9856K) NonClass: 8545K(8640K)->8545K(8640K) Class: 1082K(1216K)->1082K(1216K)",
                "[8.727s][info ][gc             ] GC(9) Pause Young (Allocation Failure) 140M->4M(495M) 0.445ms",
                "[8.727s][info ][gc,cpu         ] GC(9) User=0.00s Sys=0.00s Real=0.00s"
        };

        List<JVMEvent> jvmEvents = feedParser(lines);
        assertEquals(1, jvmEvents.size());
        DefNew collection = (DefNew) jvmEvents.get(0);
        assertEquals(8.726, collection.getDateTimeStamp().toSeconds());
        SurvivorRecord record = collection.getSurvivorRecord();
        assertNotNull(record);
        assertEquals(15, record.getMaxTenuringThreshold());
        assertEquals(15, record.getCalculatedTenuringThreshold());
        assertEquals(8945664, record.getDesiredOccupancyAfterCollection());
        assertEquals(16, record.getBytesAtEachAge().length);
        int[] bytesAtAge = {0, 400368, 0, 304, 0, 0, 0, 0, 32, 554176, 0, 0, 0, 0, 0, 0};
        for (int i = 1; i < bytesAtAge.length; i++) {
            assertEquals(bytesAtAge[i], record.getBytesAtAge(i));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> record.getBytesAtAge(16));
    }

    @Test
    public void cmsWithSurvivorRecordsTest() {

        String[] lines = {"[11.633s][info ][gc,start       ] GC(12) Pause Young (Allocation Failure)",
                "[11.633s][info ][gc,task        ] GC(12) Using 9 workers of 9 for evacuation",
                "[11.634s][debug][gc,age         ] GC(12) Desired survivor size 8945664 bytes, new threshold 6 (max threshold 6)",
                "[11.634s][trace][gc,age         ] GC(12) Age table with threshold 6 (max threshold 6)",
                "[11.634s][trace][gc,age         ] GC(12) - age   1:     400208 bytes,     400208 total",
                "[11.634s][trace][gc,age         ] GC(12) - age   6:        304 bytes,     400512 total",
                "[11.634s][info ][gc,heap        ] GC(12) ParNew: 140026K->398K(157376K)",
                "[11.634s][info ][gc,heap        ] GC(12) CMS: 3662K->3662K(349568K)",
                "[11.634s][info ][gc,metaspace   ] GC(12) Metaspace: 15714K(16256K)->15714K(16256K) NonClass: 14027K(14336K)->14027K(14336K) Class: 1686K(1920K)->1686K(1920K)",
                "[11.634s][info ][gc             ] GC(12) Pause Young (Allocation Failure) 140M->3M(495M) 0.430ms",
                "[11.634s][info ][gc,cpu         ] GC(12) User=0.00s Sys=0.00s Real=0.00s",
        };
        List<JVMEvent> jvmEvents = feedParser(lines);
        assertEquals(1, jvmEvents.size());
        ParNew collection = (ParNew) jvmEvents.get(0);

        assertEquals(11.633, collection.getDateTimeStamp().toSeconds());
        SurvivorRecord record = collection.getSurvivorRecord();
        assertNotNull(record);
        assertEquals(6, record.getMaxTenuringThreshold());
        assertEquals(6, record.getCalculatedTenuringThreshold());
        assertEquals(8945664, record.getDesiredOccupancyAfterCollection());
        assertEquals(7, record.getBytesAtEachAge().length);
        int[] bytesAtAge = {0, 400208, 0, 0, 0, 0, 304};
        for (int i = 1; i < bytesAtAge.length; i++) {
            assertEquals(bytesAtAge[i], record.getBytesAtAge(i));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> record.getBytesAtAge(7));
    }
}
