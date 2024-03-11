// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.generational.AbortablePreClean;
import com.microsoft.gctoolkit.event.generational.CMSRemark;
import com.microsoft.gctoolkit.event.generational.ConcurrentMark;
import com.microsoft.gctoolkit.event.generational.ConcurrentPreClean;
import com.microsoft.gctoolkit.event.generational.ConcurrentReset;
import com.microsoft.gctoolkit.event.generational.ConcurrentSweep;
import com.microsoft.gctoolkit.event.generational.InitialMark;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.parser.GCLogParser;
import com.microsoft.gctoolkit.parser.GenerationalHeapParser;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedDiarizer;
import com.microsoft.gctoolkit.parser.ParserTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class GenerationalHeapParserTest extends ParserTest {

    //todo: reactivate this code.

    @Test
    public void canParseLFSFullGC() {

        String fragment = "2019-11-14T23:50:29.896+0000: 91334.028: [Full GC (Allocation Failure) Before GC:\n" +
                "Statistics for BinaryTreeDictionary:\n" +
                "------------------------------------\n" +
                "Total Free Space: 1261\n" +
                "Max   Chunk Size: 1261\n" +
                "Number of Blocks: 1\n" +
                "Av.  Block  Size: 1261\n" +
                "Tree      Height: 1\n" +
                "2019-11-14T23:50:29.896+0000: 91334.028: [CMSCMS: Large block 0x00000007bfffd898\n" +
                ": 2097142K->2097142K(2097152K), 1.9092744 secs] 4063215K->4063215K(4063232K), [Metaspace: 99441K->99441K(1140736K)]After GC:\n" +
                "Statistics for BinaryTreeDictionary:\n" +
                "------------------------------------\n" +
                "Total Free Space: 1261\n" +
                "Max   Chunk Size: 1261\n" +
                "Number of Blocks: 1\n" +
                "Av.  Block  Size: 1261\n" +
                "Tree      Height: 1\n" +
                ", 1.9094806 secs] [Times: user=1.91 sys=0.00, real=1.91 secs]\n" +
                "2019-11-14T23:50:31.806+0000: 91335.938: Total time for which application threads were stopped: 4.0762194 seconds, Stopping threads took: 0.0000522 seconds\n";

        AtomicBoolean eventCreated = new AtomicBoolean(false);
        GenerationalHeapParser parser = new GenerationalHeapParser(); //, event -> {
        parser.diary(new Diary());
//        parser.
//            Assertions.assertTrue(event instanceof FullGC);
//            FullGC fgc = (FullGC) event;
//            Assertions.assertEquals(1.9094806d, fgc.getDuration());
//            Assertions.assertEquals(4063232, fgc.getHeap().getSizeAfterCollection());
//            eventCreated.set(true);
//        });
//
//        Arrays.stream(fragment.split("\n")).forEach(parser::receive);
//
//        Assertions.assertTrue(eventCreated.get());

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

    @Override
    protected Diarizer diarizer() {
        return new PreUnifiedDiarizer();
    }

    @Override
    protected GCLogParser parser() {
        return new GenerationalHeapParser();
    }
}
