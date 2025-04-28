// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.g1gc.G1Young;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.SurvivorRecord;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.diary.TestLogFile;
import com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class UnifiedG1GCParserFragmentTest extends ParserTest {

    @Override
    protected Diarizer diarizer() {
        return new UnifiedDiarizer();
    }

    @Override
    protected GCLogParser parser() {
        return new UnifiedG1GCParser();
    }


    @Test
    public void testNewDecoratorCombination() {
        String[] lines = {
                "[2023-12-06T07:32:54.113+0000][25ms] Using G1",
                "[2023-12-06T07:32:54.117+0000][29ms] Version: 17.0.2+8-86 (release)",
                "[2023-12-06T07:32:54.117+0000][29ms] CPUs: 2 total, 2 available",
                "[2023-12-06T07:32:54.117+0000][29ms] Memory: 6531M",
                "[2023-12-06T07:32:54.117+0000][29ms] Large Page Support: Disabled",
                "[2023-12-06T07:32:54.117+0000][29ms] NUMA Support: Disabled",
                "[2023-12-06T07:32:54.117+0000][29ms] Compressed Oops: Enabled (32-bit)",
                "[2023-12-06T07:32:54.117+0000][29ms] Heap Region Size: 1M",
                "[2023-12-06T07:32:54.117+0000][29ms] Heap Min Capacity: 8M",
                "[2023-12-06T07:32:54.117+0000][29ms] Heap Initial Capacity: 104M",
                "[2023-12-06T07:32:54.117+0000][29ms] Heap Max Capacity: 1634M",
                "[2023-12-06T07:32:54.117+0000][29ms] Pre-touch: Disabled",
                "[2023-12-06T07:32:54.117+0000][29ms] Parallel Workers: 2",
                "[2023-12-06T07:32:54.117+0000][29ms] Concurrent Workers: 1",
                "[2023-12-06T07:32:54.117+0000][29ms] Concurrent Refinement Workers: 2",
                "[2023-12-06T07:32:54.117+0000][29ms] Periodic GC: Disabled",
                "[2023-12-06T07:32:54.117+0000][29ms] CDS archive(s) mapped at: [0x0000000800000000-0x0000000800bdf000-0x0000000800bdf000), size 12447744, SharedBaseAddress: 0x0000000800000000, ArchiveRelocationMode: 0.",
                "[2023-12-06T07:32:54.117+0000][29ms] Compressed class space mapped at: 0x0000000800c00000-0x0000000840c00000, reserved size: 1073741824",
                "[2023-12-06T07:32:54.117+0000][29ms] Narrow klass base: 0x0000000800000000, Narrow klass shift: 0, Narrow klass range: 0x100000000",
                "[2023-12-06T07:32:54.359+0000][270ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)",
                "[2023-12-06T07:32:54.359+0000][270ms] GC(0) Using 2 workers of 2 for evacuation",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Pre Evacuate Collection Set: 0.1ms",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Merge Heap Roots: 0.0ms",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Evacuate Collection Set: 5.2ms:",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Post Evacuate Collection Set: 3.9ms",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Other: 0.2ms",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Eden regions: 6->0(8)",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Survivor regions: 0->1(1)",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Old regions: 0->0",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Archive regions: 2->2",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Humongous regions: 3->3",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Metaspace: 858K(1024K)->858K(1024K) NonClass: 780K(832K)->780K(832K) Class: 77K(192K)->77K(192K)",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 9M->4M(106M) 9.598ms",
                "[2023-12-06T07:32:54.368+0000][280ms] GC(0) User=0.00s Sys=0.01s Real=0.01s",
                "[2023-12-06T07:32:54.696+0000][608ms] GC(1) Pause Young (Normal) (G1 Evacuation Pause)",
                "[2023-12-06T07:32:54.696+0000][608ms] GC(1) Using 2 workers of 2 for evacuation",
                "[2023-12-06T07:32:54.701+0000][612ms] GC(1) Pre Evacuate Collection Set: 0.1ms",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) Merge Heap Roots: 0.0ms",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) Evacuate Collection Set: 4.3ms",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) Post Evacuate Collection Set: 0.3ms",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) Other: 0.1ms",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) Eden regions: 8->0(60)",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) Survivor regions: 1->2(2)",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) Old regions: 0->0",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) Archive regions: 2->2",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) Humongous regions: 3->3",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) Metaspace: 3551K(3712K)->3551K(3712K) NonClass: 3113K(3200K)->3113K(3200K) Class: 438K(512K)->438K(512K)",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) Pause Young (Normal) (G1 Evacuation Pause) 12M->5M(106M) 4.985ms",
                "[2023-12-06T07:32:54.701+0000][613ms] GC(1) User=0.00s Sys=0.00s Real=0.00s"
        };

        List<JVMEvent> jvmEvents = feedParser(lines);

        try {
            Assertions.assertEquals(2, jvmEvents.size());
            Assertions.assertEquals(G1Young.class, jvmEvents.get(0).getClass());
            Assertions.assertEquals(G1Young.class, jvmEvents.get(1).getClass());
        } catch(Throwable t) {
            fail(t);
        }
    }

    @Test
    public void testSurvivorRecord() {
        String[] lines = {"[0.016s][info][gc,heap] Heap region size: 1M",
                "[0.018s][info][gc     ] Using G1",
                "[0.018s][info][gc,heap,coops] Heap address: 0x00000007fc000000, size: 64 MB, Compressed Oops mode: Zero based, Oop shift amount: 3\n" +
                "[10.749s][info][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)",
                "[10.749s][info][gc,task      ] GC(0) Using 8 workers of 8 for evacuation",
                "[10.749s][debug][gc,age       ] GC(0) Desired survivor size 1572864 bytes, new threshold 15 (max threshold 15)",
                "[10.753s][trace][gc,age       ] GC(0) Age table with threshold 15 (max threshold 15)",
                "[10.754s][trace][gc,age       ] GC(0) - age   1:    2579584 bytes,    2579584 total",
                "[10.754s][info ][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Prepare TLABs: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Choose Collection Set: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Humongous Register: 0.0ms",
                "[10.754s][info ][gc,phases    ] GC(0)   Evacuate Collection Set: 4.1ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Ext Root Scanning (ms):   Min:  0.2, Avg:  0.9, Max:  1.7, Diff:  1.6, Sum:  7.3, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)     Update RS (ms):           Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)       Processed Buffers:        Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)       Scanned Cards:            Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)       Skipped Cards:            Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)     Scan RS (ms):             Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)       Scanned Cards:            Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)       Claimed Cards:            Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)       Skipped Cards:            Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)     Code Root Scanning (ms):  Min:  0.0, Avg:  0.2, Max:  0.4, Diff:  0.4, Sum:  1.8, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)     AOT Root Scanning (ms):   skipped",
                "[10.754s][debug][gc,phases    ] GC(0)     Object Copy (ms):         Min:  1.9, Avg:  2.8, Max:  3.5, Diff:  1.6, Sum: 22.7, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)     Termination (ms):         Min:  0.0, Avg:  0.1, Max:  0.1, Diff:  0.1, Sum:  0.5, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)       Termination Attempts:     Min: 1, Avg: 71.4, Max: 95, Diff: 94, Sum: 571, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)     GC Worker Other (ms):     Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.1, Workers: 8",
                "[10.754s][debug][gc,phases    ] GC(0)     GC Worker Total (ms):     Min:  4.0, Avg:  4.1, Max:  4.1, Diff:  0.0, Sum: 32.5, Workers: 8",
                "[10.754s][info ][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.3ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Code Roots Fixup: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Clear Card Table: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Reference Processing: 0.1ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Weak Processing: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Merge Per-Thread State: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Code Roots Purge: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Redirty Cards: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     DerivedPointerTable Update: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Free Collection Set: 0.1ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Humongous Reclaim: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Start New Collection Set: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Resize TLABs: 0.0ms",
                "[10.754s][debug][gc,phases    ] GC(0)     Expand Heap After Collection: 0.0ms",
                "[10.754s][info ][gc,phases    ] GC(0)   Other: 0.7ms",
                "[10.754s][info ][gc,heap      ] GC(0) Eden regions: 24->0(32)",
                "[10.754s][info ][gc,heap      ] GC(0) Survivor regions: 0->3(3)",
                "[10.754s][info ][gc,heap      ] GC(0) Old regions: 0->3",
                "[10.754s][info ][gc,heap      ] GC(0) Humongous regions: 0->0",
                "[10.754s][info ][gc,metaspace ] GC(0) Metaspace: 15753K->15753K(1062912K)",
                "[10.754s][info ][gc           ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 23M->5M(64M) 5.662ms",
                "[10.754s][info ][gc,cpu       ] GC(0) User=0.03s Sys=0.01s Real=0.00s"
        };
        List<JVMEvent> jvmEvents = feedParser(lines);

        try {
            Assertions.assertEquals(1, jvmEvents.size());
            Assertions.assertEquals(G1Young.class, jvmEvents.get(0).getClass());
            G1Young cycle = (G1Young) jvmEvents.get(0);
            SurvivorRecord survivorRecord = cycle.getSurvivorRecord();
            Assertions.assertEquals(1572864, survivorRecord.getDesiredOccupancyAfterCollection());
            Assertions.assertEquals(15, survivorRecord.getMaxTenuringThreshold());
            Assertions.assertEquals(15, survivorRecord.getCalculatedTenuringThreshold());
            Assertions.assertEquals(2579584, survivorRecord.getBytesAtAge(1));
            Assertions.assertEquals(0.0, cycle.phaseDurationFor("Pre Evacuate Collection"));
            Assertions.assertEquals(0.0041, cycle.phaseDurationFor("Evacuate Collection"), 0.0001);
            Assertions.assertEquals(0.0003, cycle.phaseDurationFor("Post Evacuate Collection Set"), 0.00001);
            Assertions.assertEquals(0.0007, cycle.phaseDurationFor("Other"), 0.00001);
            Assertions.assertEquals(0.005662, cycle.getDuration(), 0.0000001);
            Assertions.assertEquals(GCCause.G1_EVACUATION_PAUSE, cycle.getGCCause());
            MemoryPoolSummary memoryPoolSummary = cycle.getHeap();
            Assertions.assertEquals(23*1024, memoryPoolSummary.getOccupancyBeforeCollection());
            Assertions.assertEquals(5*1024, memoryPoolSummary.getOccupancyAfterCollection());
            Assertions.assertEquals(64*1024, memoryPoolSummary.getSizeAfterCollection());
            CPUSummary cpuSummary = cycle.getCpuSummary();
            Assertions.assertEquals(0.03, cpuSummary.getUser());
            Assertions.assertEquals(0.01, cpuSummary.getKernel());
            Assertions.assertEquals(0.00, cpuSummary.getWallClock());
        } catch(Throwable t) {
            fail(t);
        }
    }
}
