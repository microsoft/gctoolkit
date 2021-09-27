// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.patterns;


import com.microsoft.gctoolkit.event.generational.AbortablePreClean;
import com.microsoft.gctoolkit.event.generational.CMSRemark;
import com.microsoft.gctoolkit.event.generational.ConcurrentMark;
import com.microsoft.gctoolkit.event.generational.ConcurrentPreClean;
import com.microsoft.gctoolkit.event.generational.ConcurrentReset;
import com.microsoft.gctoolkit.event.generational.ConcurrentSweep;
import com.microsoft.gctoolkit.event.generational.InitialMark;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.parser.CMSTenuredPoolParser;
import com.microsoft.gctoolkit.parser.GCLogParser;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedJVMConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CMSTenuredParserTest extends ParserTest {

    private CMSTenuredPoolParser parser;

    private ArrayList<JVMEvent> jvmEvents;

    @BeforeEach
    public void setUp() {

        jvmEvents = new ArrayList<>();

        parser = new CMSTenuredPoolParser(new PreUnifiedJVMConfiguration().getDiary(), event -> jvmEvents.add(event));
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
                GCLogParser.END_OF_DATA_SENTINAL
        };

        feedParser(parser, lines);

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
    public void test70_40CMSDetailsCause() {
        String[] lines = new String[]{
                "40.971: [GC (CMS Initial Mark) [1 CMS-initial-mark: 2692K(5376K)] 38078K(354944K), 0.0147940 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]",
                "40.986: [CMS-concurrent-mark-start]",
                "40.991: [CMS-concurrent-mark: 0.005/0.005 secs] [Times: user=0.03 sys=0.00, real=0.00 secs]",
                "40.991: [CMS-concurrent-preclean-start]",
                "40.991: [CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]",
                "40.991: [CMS-concurrent-abortable-preclean-start]",
                "41.340: [CMS-concurrent-abortable-preclean: 0.249/0.349 secs] [Times: user=2.29 sys=0.03, real=0.35 secs]",
                "41.340: [GC (CMS Final Remark)[YG occupancy: 206871 K (349568 K)]41.340: [Rescan (parallel) , 0.0696600 secs]41.410: [weak refs processing, 0.0000440 secs]41.410: [scrub string table, 0.0001880 secs] [1 CMS-remark: 2692K(5376K)] 209564K(354944K), 0.0699640 secs] [Times: user=0.50 sys=0.01, real=0.07 secs]",
                "41.410: [CMS-concurrent-sweep-start]",
                "41.411: [CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]",
                "41.411: [CMS-concurrent-reset-start]",
                "41.416: [CMS-concurrent-reset: 0.005/0.005 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]",
                GCLogParser.END_OF_DATA_SENTINAL
        };

        feedParser(parser, lines);

        try {
            InitialMark initialMark = (InitialMark) jvmEvents.get(0);
            ConcurrentMark concurrentMark = (ConcurrentMark) jvmEvents.get(1);
            ConcurrentPreClean concurrentPreClean = (ConcurrentPreClean) jvmEvents.get(2);
            AbortablePreClean abortablePreClean = (AbortablePreClean) jvmEvents.get(3);
            CMSRemark cmsRemark = (CMSRemark) jvmEvents.get(4);
            ConcurrentSweep concurrentSweep = (ConcurrentSweep) jvmEvents.get(5);
            ConcurrentReset concurrentReset = (ConcurrentReset) jvmEvents.get(6);
        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
        assertTrue(true);

    }

    @Test
    public void test70CMSDetailsNoCauseDateStamps() {
        String[] lines = new String[]{
                "2011-08-25T08:11:08.288+0100: 8449.764: [GC 8449.765: [ParNew: 1143174K->132096K(1188864K), 0.1551440 secs] 1856305K->851287K(1965056K), 0.1554100 secs] [Times: user=0.99 sys=0.04, real=0.16 secs]",
                "2011-08-25T08:11:08.448+0100: 8449.924: [GC [1 CMS-initial-mark: 719191K(776192K)] 863034K(1965056K), 0.1976100 secs] [Times: user=0.18 sys=0.01, real=0.20 secs]",
                "2011-08-25T08:11:08.646+0100: 8450.122: [CMS-concurrent-mark-start]",
                "2011-08-25T08:11:09.646+0100: 8451.122: [CMS-concurrent-mark: 0.993/1.000 secs] [Times: user=4.63 sys=0.13, real=1.00 secs]",
                "2011-08-25T08:11:09.646+0100: 8451.123: [CMS-concurrent-preclean-start]",
                "2011-08-25T08:11:09.653+0100: 8451.129: [CMS-concurrent-preclean: 0.005/0.007 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]",
                "2011-08-25T08:11:09.653+0100: 8451.129: [CMS-concurrent-abortable-preclean-start]",
                " CMS: abort preclean due to time 2011-08-25T08:11:15.001+0100: 8456.477: [CMS-concurrent-abortable-preclean: 5.340/5.348 secs] [Times: user=5.60 sys=0.28, real=5.35 secs]",
                "2011-08-25T08:11:15.001+0100: 8456.478: [GC[YG occupancy: 436180 K (1188864 K)]8456.478: [Rescan (parallel) , 0.6302730 secs]8457.108: [weak refs processing, 0.0002410 secs] [1 CMS-remark: 719191K(776192K)] 1155372K(1965056K), 0.6306470 secs] [Times: user=1.04 sys=0.95, real=0.63 secs]",
                "2011-08-25T08:11:15.632+0100: 8457.109: [CMS-concurrent-sweep-start]",
                "2011-08-25T08:11:16.818+0100: 8458.295: [CMS-concurrent-sweep: 1.183/1.186 secs] [Times: user=1.71 sys=0.08, real=1.18 secs]",
                "2011-08-25T08:11:16.819+0100: 8458.295: [CMS-concurrent-reset-start]",
                "2011-08-25T08:11:16.828+0100: 8458.304: [CMS-concurrent-reset: 0.009/0.009 secs] [Times: user=0.01 sys=0.00, real=0.01 secs]",
                GCLogParser.END_OF_DATA_SENTINAL
        };

        feedParser(parser, lines);

        try {
            InitialMark initialMark = (InitialMark) jvmEvents.get(0);
            ConcurrentMark concurrentMark = (ConcurrentMark) jvmEvents.get(1);
            ConcurrentPreClean concurrentPreClean = (ConcurrentPreClean) jvmEvents.get(2);
            AbortablePreClean abortablePreClean = (AbortablePreClean) jvmEvents.get(3);
            CMSRemark cmsRemark = (CMSRemark) jvmEvents.get(4);
            ConcurrentSweep concurrentSweep = (ConcurrentSweep) jvmEvents.get(5);
            ConcurrentReset concurrentReset = (ConcurrentReset) jvmEvents.get(6);
        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
        assertTrue(true);
    }

    @Test
    public void testPreCleanReferenceWithParNew() {
        String[] lines = new String[] {
                "2020-03-27T17:40:29.039+0000: 62546.510: [CMS-concurrent-mark-start] ",
                "2020-03-27T17:40:29.039+0000: 62592.053: [CMS-concurrent-mark: 44.198/45.543 secs] [Times: user=543.12 sys=21.08, real=45.54 secs]",
                "2020-03-27T17:40:29.040+0000: 62592.053: [CMS-concurrent-preclean-start]",
                "2020-03-27T17:40:29.040+0000: 62592.053: [Preclean SoftReferences, 0.0000103 secs]2020-03-27T17:40:29.040+0000: 62592.053: [Preclean WeakReferences, 0.0010134 secs]2020-03-27T17:40:29.041+0000: 62592.054: [Preclean FinalReferences, 0.0019776 secs]2020-03-27T17:40:29.043+0000: 62592.056: [Preclean PhantomReferences, 0.0001103 secs]2020-03-27T17:40:31.322+0000: 62594.336: [GC (Allocation Failure) 2020-03-27T17:40:31.323+0000: 62594.336: [ParNew2020-03-27T17:40:31.499+0000: 62594.512: [SoftReference, 0 refs, 0.0004947 secs]2020-03-27T17:40:31.499+0000: 62594.512: [WeakReference, 112722 refs, 0.0017882 secs]2020-03-27T17:40:31.501+0000: 62594.514: [FinalReference, 41995 refs, 0.0019855 secs]2020-03-27T17:40:31.503+0000: 62594.516: [PhantomReference, 0 refs, 0 refs, 0.0007100 secs]2020-03-27T17:40:31.504+0000: 62594.517: [JNI Weak Reference, 0.0501765 secs]",
                "Desired survivor size 1395851264 bytes, new threshold 1 (max 1)",
                "- age   1:  200254008 bytes,  200254008 total",
                ": 22098122K->287427K(24536704K), 0.2322818 secs] 163101800K->141441029K(199648896K), 0.2330707 secs] [Times: user=3.69 sys=0.00, real=0.23 secs] ",
                "2020-03-27T17:40:31.556+0000: 62594.569: Total time for which application threads were stopped: 0.2393021 seconds, Stopping threads took: 0.0005320 seconds",
                "2020-03-27T17:40:38.605+0000: 62601.619: [GC (Allocation Failure) 2020-03-27T17:40:38.606+0000: 62601.619: [ParNew2020-03-27T17:40:38.771+0000: 62601.785: [SoftReference, 0 refs, 0.0006750 secs]2020-03-27T17:40:38.772+0000: 62601.785: [WeakReference, 95084 refs, 0.0016015 secs]2020-03-27T17:40:38.774+0000: 62601.787: [FinalReference, 34266 refs, 0.0016741 secs]2020-03-27T17:40:38.775+0000: 62601.789: [PhantomReference, 0 refs, 1 refs, 0.0009205 secs]2020-03-27T17:40:38.776+0000: 62601.790: [JNI Weak Reference, 0.0427279 secs]",
                "Desired survivor size 1395851264 bytes, new threshold 1 (max 1)",
                "- age   1:  266131624 bytes,  266131624 total",
                ": 22097859K->360095K(24536704K), 0.2143928 secs] 163251461K->141651455K(199648896K), 0.2151749 secs] [Times: user=3.46 sys=0.00, real=0.22 secs] ",
                "2020-03-27T17:40:38.821+0000: 62601.835: Total time for which application threads were stopped: 0.2217700 seconds, Stopping threads took: 0.0007343 seconds",
                "2020-03-27T17:40:43.648+0000: 62606.661: [CMS-concurrent-preclean: 13.328/14.608 secs] [Times: user=131.70 sys=7.81, real=14.61 secs] ",
                "2020-03-27T17:40:43.648+0000: 62606.662: [CMS-concurrent-abortable-preclean-start]",
                "2020-03-27T17:40:43.648+0000: 62606.662: [CMS-concurrent-abortable-preclean-start]",
                "Desired survivor size 1395851264 bytes, new threshold 1 (max 1)",
                "- age   1:  201222024 bytes,  201222024 total",
                ": 22170527K->325362K(24536704K), 0.2519645 secs] 163461887K->141842088K(199648896K), 0.2527980 secs] [Times: user=3.30 sys=0.02, real=0.25 secs] ",
                "2020-03-27T17:40:46.583+0000: 62609.597: Total time for which application threads were stopped: 0.2594511 seconds, Stopping threads took: 0.0006756 seconds",
                "2020-03-27T17:40:46.829+0000: 62609.842: [CMS-concurrent-abortable-preclean: 2.855/3.180 secs] [Times: user=25.14 sys=1.51, real=3.18 secs] "
        };
        
        feedParser(parser, lines);

        try {
            ConcurrentMark concurrentMark = (ConcurrentMark) jvmEvents.get(0);
            ConcurrentPreClean concurrentPreClean = (ConcurrentPreClean) jvmEvents.get(1);
            AbortablePreClean abortablePreClean = (AbortablePreClean) jvmEvents.get(2);
        } catch (ClassCastException cce) {
            fail(cce.getMessage());
        }
    }
}
