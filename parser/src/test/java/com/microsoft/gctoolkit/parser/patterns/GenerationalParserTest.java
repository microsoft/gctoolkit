// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.patterns;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.generational.CMSRemark;
import com.microsoft.gctoolkit.event.generational.ConcurrentModeFailure;
import com.microsoft.gctoolkit.event.generational.DefNew;
import com.microsoft.gctoolkit.event.generational.FullGC;
import com.microsoft.gctoolkit.event.generational.InitialMark;
import com.microsoft.gctoolkit.event.generational.PSYoungGen;
import com.microsoft.gctoolkit.event.generational.ParNew;
import com.microsoft.gctoolkit.event.generational.ParNewPromotionFailed;
import com.microsoft.gctoolkit.event.generational.SystemGC;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.parser.GenerationalHeapParser;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedJVMConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenerationalParserTest extends ParserTest {

    private static final String END_OF_DATA_SENTINAL = GCLogFile.END_OF_DATA_SENTINAL;

    private GenerationalHeapParser parser;
    private ArrayList<JVMEvent> collection;

    @BeforeEach
    public void setUp() {
        collection = new ArrayList<>();

        parser = new GenerationalHeapParser(new PreUnifiedJVMConfiguration().getDiary(), event -> collection.add(event));
    }

    @Test
    public void testDefNewDetails() {

        String[] lines = {
                "2019-10-22T23:41:21.852+0000: 21.912: [GC (GCLocker Initiated GC) 2019-10-22T23:41:21.853+0000: 21.912: [DefNew2019-10-22T23:41:21.914+0000: 21.974: [SoftReference, 0 refs, 0.0000842 secs]2019-10-22T23:41:21.914+0000: 21.974: [WeakReference, 76 refs, 0.0000513 secs]2019-10-22T23:41:21.914+0000: 21.974: [FinalReference, 91635 refs, 0.0396861 secs]2019-10-22T23:41:21.954+0000: 22.014: [PhantomReference, 0 refs, 3 refs, 0.0000444 secs]2019-10-22T23:41:21.954+0000: 22.014: [JNI Weak Reference, 0.0000281 secs]: 419520K->19563K(471936K), 0.1019514 secs] 502104K->102148K(2044800K), 0.1020469 secs] [Times: user=0.09 sys=0.01, real=0.10 secs]",
                END_OF_DATA_SENTINAL
        };

        feedParser(parser, lines);

        DefNew defNew = (DefNew) collection.get(0);

        // occupancy before(size before)->occupancy after(size)
        assertMemoryPoolValues(defNew.getHeap(), 502104, 2044800, 102148, 2044800);
        assertMemoryPoolValues(defNew.getYoung(), 419520, 471936, 19563, 471936);
        assertMemoryPoolValues(defNew.getTenured(), 82584, 1572864, 82585, 1572864);
    }

    @Test
    public void testParNewCMSWithDetailsTenuringAndSystemGC() {

        String[] lines = new String[]{
                "1.055: [GC 1.055: [ParNew",
                "Desired survivor size 1343488 bytes, new threshold 1 (max 4)",
                "- age   1:    1750392 bytes,    1750392 total",
                ": 16000K->1725K(18624K), 0.0167011 secs] 16000K->1725K(81280K), 0.0167922 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]",
                "1.141: [Full GC (System) 1.141: [CMS: 0K->1602K(62656K), 0.0711019 secs] 4654K->1602K(81280K), [CMS Perm : 10117K->10063K(21248K)], 0.0712086 secs] [Times: user=0.06 sys=0.00, real=0.07 secs]",
                "10.233: [GC 10.233: [ParNew",
                "Desired survivor size 1343488 bytes, new threshold 1 (max 4)",
                "- age   1:    2678952 bytes,    2678952 total",
                ": 16000K->2624K(18624K), 0.0211781 secs] 17599K->5144K(81280K), 0.0212871 secs] [Times: user=0.03 sys=0.01, real=0.03 secs]",
                "12.986: [GC 12.987: [ParNew",
                "Desired survivor size 1343488 bytes, new threshold 4 (max 4)",
                "- age   1:      41544 bytes,      41544 total",
                "        - age   2:    1134064 bytes,    1175608 total",
                ": 18264K->1199K(18624K), 0.0042402 secs] 23578K->6513K(81280K), 0.0043376 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]",
                END_OF_DATA_SENTINAL
        };

        feedParser(parser, lines);

        ParNew parNew = (ParNew) collection.get(0);
        assertMemoryPoolValues(parNew.getHeap(), 16000, 81280, 1725, 81280);
        assertMemoryPoolValues(parNew.getYoung(), 16000, 18624, 1725, 18624);
        assertMemoryPoolValues(parNew.getTenured(), 0, 81280 - 18624, 0, 81280 - 18624);
        assertTrue(parNew.getDuration() == 0.0167922);

        SystemGC full = (SystemGC) collection.get(1);
        assertMemoryPoolValues(full.getHeap(), 4654, 81280, 1602, 81280);
        assertMemoryPoolValues(full.getYoung(), 4654, 81280 - 62656, 0, 81280 - 62656);
        assertMemoryPoolValues(full.getTenured(), 0, 81280 - 18624, 1602, 62656);
        assertTrue(full.getDuration() == 0.0712086);

        parNew = (ParNew) collection.get(2);
        assertTrue(parNew.getHeap().getSizeAfterCollection() == 81280);

        parNew = (ParNew) collection.get(3);
        assertTrue(parNew.getHeap().getSizeAfterCollection() == 81280);
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
                END_OF_DATA_SENTINAL
        };


        feedParser(parser, lines);

        InitialMark initialMark = (InitialMark) collection.get(0);
        ParNewPromotionFailed parNewPromotionFailed = (ParNewPromotionFailed) collection.get(1);
        ConcurrentModeFailure concurrentModeFailure = (ConcurrentModeFailure) collection.get(2);
    }

    @Test
    public void test80ParallelGC() {
        String[] lines = new String[]{
                "1.106: [GC (Allocation Failure)",
                "Desired survivor size 119013376 bytes, new threshold 1 (max 15)",
                "[PSYoungGen: 232960K->116224K(232960K)] 610571K->581588K(819712K), 0.0485326 secs] [Times: user=0.18 sys=0.14, real=0.05 secs] ",
                "1.154: [Full GC (Ergonomics) [PSYoungGen: 116224K->0K(232960K)] [ParOldGen: 465364K->194938K(575488K)] 581588K->194938K(808448K), [Metaspace: 4211K->4211K(1056768K)], 0.0449697 secs] [Times: user=0.29 sys=0.00, real=0.04 secs] ",
                END_OF_DATA_SENTINAL
        };

        feedParser(parser, lines);

        PSYoungGen psYoungGen = (PSYoungGen) collection.get(0);
        assertTrue(psYoungGen.getGCCause() == GCCause.ALLOCATION_FAILURE);
        assertTrue(psYoungGen.getDuration() == 0.0485326);
        assertMemoryPoolValues(psYoungGen.getHeap(), 610571, 819712, 581588, 819712);
        assertMemoryPoolValues(psYoungGen.getTenured(), 610571 - 232960, 819712 - 232960, 581588 - 116224, 819712 - 232960);
        assertMemoryPoolValues(psYoungGen.getYoung(), 232960, 232960, 116224, 232960);

        FullGC fullGC = (FullGC) collection.get(1);
        assertTrue(fullGC.getGCCause() == GCCause.ADAPTIVE_SIZE_POLICY);
        assertTrue(fullGC.getDuration() == 0.0449697);
        // todo: value of heap size before collection is 808448 should be 819712.
        // Parser is not tracking previous size and since we're not doing anything with it at the moment...
        assertMemoryPoolValues(fullGC.getHeap(), 581588, 808448, 194938, 808448);
        assertMemoryPoolValues(fullGC.getTenured(), 465364, 575488, 194938, 575488);
        assertMemoryPoolValues(fullGC.getYoung(), 116224, 232960, 0, 232960);
    }

    @Test
    public void test70_40CMSDetailsCause() {
        String[] lines = new String[]{
                "40.962: [GC (GCLocker Initiated GC)40.962: [ParNew: 32671K->35386K(349568K), 0.0082400 secs] 35230K->38078K(354944K), 0.0082790 secs] [Times: user=0.06 sys=0.00, real=0.01 secs]",
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
                END_OF_DATA_SENTINAL
        };

        feedParser(parser, lines);

        ParNew parNew = (ParNew) collection.get(0);
        assertTrue(parNew.getGCCause() == GCCause.GC_LOCKER);
        assertMemoryPoolValues(parNew.getHeap(), 35230, 354944, 38078, 354944);
        assertMemoryPoolValues(parNew.getYoung(), 32671, 349568, 35386, 349568);
        assertMemoryPoolValues(parNew.getTenured(), 35230 - 32671, 354944 - 349568, 38078 - 35386, 354944 - 349568);
        assertTrue(parNew.getDuration() == 0.0082790);

        InitialMark initialMark = (InitialMark) collection.get(1);
        assertTrue(initialMark.getGCCause() == GCCause.CMS_INITIAL_MARK);

        CMSRemark cmsRemark = (CMSRemark) collection.get(2);
        assertTrue(cmsRemark.getGCCause() == GCCause.CMS_FINAL_REMARK);
        assertTrue(cmsRemark.getDuration() == 0.0699640);
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
                END_OF_DATA_SENTINAL
        };

        feedParser(parser, lines);

        ParNew parNew = (ParNew) collection.get(0);
        assertMemoryPoolValues(parNew.getHeap(), 1856305, 1965056, 851287, 1965056);
        assertMemoryPoolValues(parNew.getYoung(), 1143174, 1188864, 132096, 1188864);
        assertMemoryPoolValues(parNew.getTenured(), 1856305 - 1143174, 1965056 - 1188864, 851287 - 132096, 1965056 - 1188864);
        assertTrue(parNew.getDuration() == 0.1554100);

        InitialMark initialMark = (InitialMark) collection.get(1);
        assertTrue(initialMark.getDuration() == 0.1976100);

        CMSRemark cmsRemark = (CMSRemark) collection.get(2);
        assertTrue(cmsRemark.getDuration() == 0.6306470);
    }
}
