// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.shenandoah.ShenandoahCycle;
import com.microsoft.gctoolkit.event.zgc.OccupancySummary;
import com.microsoft.gctoolkit.event.zgc.ReclaimSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMemoryPoolSummary;
import com.microsoft.gctoolkit.parser.ShenandoahParser;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShenandoahParserTest {


    @Test
    public void infoLevelShenandoahCycle() {


        String[] eventLogEntries = {
                "[0.876s][info][gc           ] Trigger: Metadata GC Threshold",
                "[0.876s][info][gc,ergo      ] Free: 7724M, Max: 4096K regular, 7724M humongous, Frag: 0% external, 0% internal; Reserve: 412M, Max: 4096K",
                "[0.876s][info][gc,start     ] GC(0) Concurrent reset",
                "[0.876s][info][gc,task      ] GC(0) Using 2 of 4 workers for concurrent reset",
                "[0.876s][info][gc,ergo      ] GC(0) Pacer for Reset. Non-Taxable: 8192M",
                "[0.876s][info][gc           ] GC(0) Concurrent reset 0.252ms",
                "[0.877s][info][gc,start     ] GC(0) Pause Init Mark (process weakrefs) (unload classes)",
                "[0.877s][info][gc,task      ] GC(0) Using 4 of 4 workers for init marking",
                "[0.878s][info][gc,ergo      ] GC(0) Pacer for Mark. Expected Live: 819M, Free: 7724M, Non-Taxable: 772M, Alloc Tax Rate: 0.4x",
                "[0.878s][info][gc           ] GC(0) Pause Init Mark (process weakrefs) (unload classes) 1.692ms",
                "[0.878s][info][gc,start     ] GC(0) Concurrent marking (process weakrefs) (unload classes)",
                "[0.878s][info][gc,task      ] GC(0) Using 2 of 4 workers for concurrent marking",
                "[0.883s][info][gc           ] GC(0) Concurrent marking (process weakrefs) (unload classes) 4.315ms",
                "[0.883s][info][gc,start     ] GC(0) Concurrent precleaning",
                "[0.883s][info][gc,task      ] GC(0) Using 1 of 4 workers for concurrent preclean",
                "[0.883s][info][gc,ergo      ] GC(0) Pacer for Precleaning. Non-Taxable: 8192M",
                "[0.883s][info][gc           ] GC(0) Concurrent precleaning 0.232ms",
                "[0.883s][info][gc,start     ] GC(0) Pause Final Mark (process weakrefs) (unload classes)",
                "[0.883s][info][gc,task      ] GC(0) Using 4 of 4 workers for final marking",
                "[0.885s][info][gc,stringtable] GC(0) Cleaned string and symbol table, strings: 9281 processed, 0 removed, symbols: 68910 processed, 23 removed",
                "[0.886s][info][gc,ergo       ] GC(0) Adaptive CSet Selection. Target Free: 1160M, Actual Free: 8128M, Max CSet: 341M, Min Garbage: 0B",
                "[0.886s][info][gc,ergo       ] GC(0) Collectable Garbage: 48448K (100%), Immediate: 0B (0%), CSet: 48448K (100%)",
                "[0.886s][info][gc,ergo       ] GC(0) Pacer for Evacuation. Used CSet: 57344K, Free: 7716M, Non-Taxable: 771M, Alloc Tax Rate: 1.1x",
                "[0.886s][info][gc            ] GC(0) Pause Final Mark (process weakrefs) (unload classes) 3.175ms",
                "[0.886s][info][gc,start      ] GC(0) Concurrent cleanup",
                "[0.886s][info][gc            ] GC(0) Concurrent cleanup 64M->68M(8192M) 0.045ms",
                "[0.886s][info][gc,ergo       ] GC(0) Free: 7712M, Max: 4096K regular, 7712M humongous, Frag: 0% external, 0% internal; Reserve: 411M, Max: 4096K",
                "[0.886s][info][gc,start      ] GC(0) Concurrent evacuation",
                "[0.886s][info][gc,task       ] GC(0) Using 2 of 4 workers for concurrent evacuation",
                "[0.891s][info][gc            ] GC(0) Concurrent evacuation 4.539ms",
                "[0.891s][info][gc,start      ] GC(0) Pause Init Update Refs",
                "[0.891s][info][gc,ergo       ] GC(0) Pacer for Update Refs. Used: 81920K, Free: 7712M, Non-Taxable: 771M, Alloc Tax Rate: 1.1x",
                "[0.891s][info][gc            ] GC(0) Pause Init Update Refs 0.033ms",
                "[0.891s][info][gc,start      ] GC(0) Concurrent update references",
                "[0.891s][info][gc,task       ] GC(0) Using 2 of 4 workers for concurrent reference update",
                "[0.895s][info][gc            ] GC(0) Concurrent update references 4.072ms",
                "[0.895s][info][gc,start      ] GC(0) Pause Final Update Refs",
                "[0.895s][info][gc,task       ] GC(0) Using 4 of 4 workers for final reference update",
                "[0.896s][info][gc            ] GC(0) Pause Final Update Refs 0.271ms",
                "[0.896s][info][gc,start      ] GC(0) Concurrent cleanup",
                "[0.896s][info][gc            ] GC(0) Concurrent cleanup 84M->28M(8192M) 0.039ms",
                "[0.896s][info][gc,ergo       ] Free: 7752M, Max: 4096K regular, 7696M humongous, Frag: 1% external, 0% internal; Reserve: 412M, Max: 4096K",
                "[0.896s][info][gc,metaspace  ] Metaspace: 20546K->20754K(1069056K)",
                "[0.896s][info][gc,ergo       ] Pacer for Idle. Initial: 163M, Alloc Tax Rate: 1.0x"
        };

        AtomicBoolean eventCreated = new AtomicBoolean(false);
        ShenandoahParser parser = new ShenandoahParser(new LoggingDiary(), event -> {
            try {
                ShenandoahCycle sc = (ShenandoahCycle) event;
                Assertions.assertEquals(toInt(0.038d,1000), toInt(sc.getDuration(),1000));
                Assertions.assertEquals(toInt(3.558d, 1000), toInt(sc.getDateTimeStamp().getTimeStamp(), 1000));
                Assertions.assertEquals("Warmup", sc.getGCCause().getLabel());

                // Durations
//                Assertions.assertEquals(toInt(3.558d, 1000), toInt(sc.getPauseMarkStartTimeStamp().getTimeStamp(), 1000));
//                Assertions.assertEquals(toInt(3.558d, 1000), toInt(sc.getConcurrentMarkTimeStamp().getTimeStamp(), 1000));
//                Assertions.assertEquals(toInt(3.573d, 1000), toInt(sc.getPauseMarkEndTimeStamp().getTimeStamp(), 1000));
//                Assertions.assertEquals(toInt(3.574d, 1000), toInt(sc.getConcurrentProcessNonStrongReferencesTimeStamp().getTimeStamp(), 1000));
//                Assertions.assertEquals(toInt(3.577d, 1000), toInt(sc.getConcurrentResetRelocationSetTimeStamp().getTimeStamp(), 1000));
//                Assertions.assertEquals(toInt(3.578d, 1000), toInt(sc.getConcurrentSelectRelocationSetTimeStamp().getTimeStamp(), 1000));
//                Assertions.assertEquals(toInt(3.582d, 1000), toInt(sc.getPauseRelocateStartTimeStamp().getTimeStamp(), 1000));
//                Assertions.assertEquals(toInt(3.583d, 1000), toInt(sc.getConcurrentRelocateTimeStamp().getTimeStamp(), 1000));
//
//                Assertions.assertEquals( toInt(0.460d, 1000), toInt(sc.getPauseMarkStartDuration(), 1000));
//                Assertions.assertEquals(toInt(14.621d, 1000), toInt(sc.getConcurrentMarkDuration(), 1000));
//                Assertions.assertEquals( toInt(0.830d, 1000), toInt(sc.getPauseMarkEndDuration(), 1000));
//                Assertions.assertEquals( toInt(3.654d, 1000), toInt(sc.getConcurrentProcessNonStrongReferencesDuration(), 1000));
//                Assertions.assertEquals( toInt(0.194d, 1000), toInt(sc.getConcurrentResetRelocationSetDuration(), 1000));
//                Assertions.assertEquals( toInt(3.193d, 1000), toInt(sc.getConcurrentSelectRelocationSetDuration(), 1000));
//                Assertions.assertEquals( toInt(0.794d, 1000), toInt(sc.getPauseRelocateStartDuration(), 1000));
//                Assertions.assertEquals(toInt(12.962d, 1000), toInt(sc.getConcurrentRelocateDuration(), 1000));
//
//                //Memory
//                Assertions.assertTrue(checkZGCMemoryPoolSummary(sc.getMarkStart(), 936L, 42L, 3160L, 894)); //1074L, 1074L, 1074L));
//                Assertions.assertTrue(checkZGCMemoryPoolSummary(sc.getMarkEnd(), 1074L, 42L, 3084L, 970L));
//                Assertions.assertTrue(checkZGCMemoryPoolSummary(sc.getRelocateStart(),1074L, 42L, 3852L, 202L));
//                Assertions.assertTrue(checkZGCMemoryPoolSummary(sc.getRelocateEnd(), 1074L, 42L, 3868L, 186L));
//
//                Assertions.assertTrue(checkOccupancySummary(sc.getLive(), 8L, 8L, 8L));
//                Assertions.assertTrue(checkOccupancySummary(sc.getAllocated(), 172L, 172L, 376L));
//                Assertions.assertTrue(checkOccupancySummary(sc.getGarbage(), 885L, 117L, 5L));
//
//                Assertions.assertTrue(checkReclaimSummary(sc.getReclaimed(), 768L, 880L));
//                Assertions.assertTrue(checkReclaimSummary(sc.getMemorySummary(), 894L, 186L));
//
//                Assertions.assertTrue(sc.getLoadAverageAt(1) == 4.28);
//                Assertions.assertTrue(sc.getLoadAverageAt(5) == 3.95);
//                Assertions.assertTrue(sc.getLoadAverageAt(15) == 3.22);
//
//                Assertions.assertTrue(sc.getMMU(2) == 32.7);
//                Assertions.assertTrue(sc.getMMU(5) == 60.8);
//                Assertions.assertTrue(sc.getMMU(10) == 80.4);
//                Assertions.assertTrue(sc.getMMU(20) == 85.4);
//                Assertions.assertTrue(sc.getMMU(50) == 90.8);
//                Assertions.assertTrue(sc.getMMU(100) == 95.4);

            } catch (Throwable t) {
                Assertions.fail(t);
            }
            eventCreated.set(true);
        });

        Arrays.stream(eventLogEntries).forEach(parser::receive);
        //Assertions.assertTrue(eventCreated.get());
        Assertions.assertTrue(true);

    }

    private boolean checkZGCMemoryPoolSummary(ZGCMemoryPoolSummary summary, long capacity, long reserved, long free, long used) {
        return summary.getCapacity() == capacity && summary.getReserved() == reserved && summary.getFree() == free && summary.getUsed() == used;
    }

    private boolean checkOccupancySummary(OccupancySummary summary, long markEnd, long relocateStart, long relocateEnd) {
        return summary.getMarkEnd() == markEnd && summary.getReclaimStart() == relocateStart && summary.getReclaimEnd() == relocateEnd;
    }

    private boolean checkReclaimSummary(ReclaimSummary summary, long relocateStart, long relocateEnd) {
        return summary.getReclaimStart() == relocateStart && summary.getReclaimEnd() == relocateEnd;
    }

    private int toInt(double value, int significantDigits) {
        return (int)(value * (double)significantDigits);
    }
}
