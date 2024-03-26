// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.g1gc.G1Young;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.diary.TestLogFile;
import com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
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
}
