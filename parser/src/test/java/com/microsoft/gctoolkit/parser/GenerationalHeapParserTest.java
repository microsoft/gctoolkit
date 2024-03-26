// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.generational.AbortablePreClean;
import com.microsoft.gctoolkit.event.generational.CMSRemark;
import com.microsoft.gctoolkit.event.generational.ConcurrentMark;
import com.microsoft.gctoolkit.event.generational.ConcurrentModeFailure;
import com.microsoft.gctoolkit.event.generational.ConcurrentPreClean;
import com.microsoft.gctoolkit.event.generational.ConcurrentReset;
import com.microsoft.gctoolkit.event.generational.ConcurrentSweep;
import com.microsoft.gctoolkit.event.generational.FullGC;
import com.microsoft.gctoolkit.event.generational.InitialMark;
import com.microsoft.gctoolkit.event.generational.ParNew;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedDiarizer;
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


    @Override
    protected Diarizer diarizer() {
        return new PreUnifiedDiarizer();
    }

    @Override
    protected GCLogParser parser() {
        return new GenerationalHeapParser();
    }

}
