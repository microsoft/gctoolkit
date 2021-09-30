// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.zgc.OccupancySummary;
import com.microsoft.gctoolkit.event.zgc.ReclaimSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCCycle;
import com.microsoft.gctoolkit.event.zgc.ZGCMemoryPoolSummary;
import com.microsoft.gctoolkit.parser.ZGCParser;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZGCParserTest {


    @Test
    public void infoLevelZGCCycle() {


        String[] eventLogEntries = { "[3.558s][info ][gc,start       ] GC(3) Garbage Collection (Warmup)",
                "[3.559s][info ][gc,phases      ] GC(3) Pause Mark Start 0.460ms",
                "[3.573s][info ][gc,phases      ] GC(3) Concurrent Mark 14.621ms",
                "[3.574s][info ][gc,phases      ] GC(3) Pause Mark End 0.830ms",
                "[3.578s][info ][gc,phases      ] GC(3) Concurrent Process Non-Strong References 3.654ms",
                "[3.578s][info ][gc,phases      ] GC(3) Concurrent Reset Relocation Set 0.194ms",
                "[3.582s][info ][gc,phases      ] GC(3) Concurrent Select Relocation Set 3.193ms",
                "[3.583s][info ][gc,phases      ] GC(3) Pause Relocate Start 0.794ms",
                "[3.596s][info ][gc,phases      ] GC(3) Concurrent Relocate 12.962ms",
                "[3.596s][info ][gc,load        ] GC(3) Load: 4.28/3.95/3.22",
                "[3.596s][info ][gc,mmu         ] GC(3) MMU: 2ms/32.7%, 5ms/60.8%, 10ms/80.4%, 20ms/85.4%, 50ms/90.8%, 100ms/95.4%",
                "[3.596s][info ][gc,marking     ] GC(3) Mark: 1 stripe(s), 2 proactive flush(es), 1 terminate flush(es), 1 completion(s), 0 continuation(s)",
                "[3.596s][info ][gc,reloc       ] GC(3) Relocation: Successful, 6M relocated",
                "[3.596s][info ][gc,nmethod     ] GC(3) NMethods: 1163 registered, 0 unregistered",
                "[3.596s][info ][gc,metaspace   ] GC(3) Metaspace: 14M used, 15M capacity, 15M committed, 16M reserved",
                "[3.596s][info ][gc,ref         ] GC(3) Soft: 391 encountered, 0 discovered, 0 enqueued",
                "[3.596s][info ][gc,ref         ] GC(3) Weak: 587 encountered, 466 discovered, 0 enqueued",
                "[3.596s][info ][gc,ref         ] GC(3) Final: 799 encountered, 0 discovered, 0 enqueued",
                "[3.596s][info ][gc,ref         ] GC(3) Phantom: 33 encountered, 1 discovered, 0 enqueued",
                "[3.596s][info ][gc,heap        ] GC(3) Min Capacity: 8M(0%)",
                "[3.596s][info ][gc,heap        ] GC(3) Max Capacity: 4096M(100%)",
                "[3.596s][info ][gc,heap        ] GC(3) Soft Max Capacity: 4096M(100%)",
                "[3.596s][info ][gc,heap        ] GC(3)                Mark Start          Mark End        Relocate Start      Relocate End           High               Low",
                "[3.596s][info ][gc,heap        ] GC(3)  Capacity:      936M (23%)        1074M (26%)        1074M (26%)        1074M (26%)        1074M (26%)         936M (23%)",
                "[3.596s][info ][gc,heap        ] GC(3)   Reserve:       42M (1%)           42M (1%)           42M (1%)           42M (1%)           42M (1%)           42M (1%)",
                "[3.596s][info ][gc,heap        ] GC(3)      Free:     3160M (77%)        3084M (75%)        3852M (94%)        3868M (94%)        3930M (96%)        3022M (74%)",
                "[3.596s][info ][gc,heap        ] GC(3)      Used:      894M (22%)         970M (24%)         202M (5%)          186M (5%)         1032M (25%)         124M (3%)",
                "[3.596s][info ][gc,heap        ] GC(3)      Live:         -                 8M (0%)            8M (0%)            8M (0%)             -                  -",
                "[3.596s][info ][gc,heap        ] GC(3) Allocated:         -               172M (4%)          172M (4%)          376M (9%)             -                  -",
                "[3.596s][info ][gc,heap        ] GC(3)   Garbage:         -               885M (22%)         117M (3%)            5M (0%)             -                  -",
                "[3.596s][info ][gc,heap        ] GC(3) Reclaimed:         -                  -               768M (19%)         880M (21%)            -                  -",
                "[3.596s][info ][gc             ] GC(3) Garbage Collection (Warmup) 894M(22%)->186M(5%)"
        };

        AtomicBoolean eventCreated = new AtomicBoolean(false);
        ZGCParser parser = new ZGCParser(new LoggingDiary(), event -> {
            try {
                ZGCCycle zgc = (ZGCCycle) event;
                Assertions.assertEquals(toInt(0.038d,1000), toInt(zgc.getDuration(),1000));
                Assertions.assertEquals(toInt(3.558d, 1000), toInt(zgc.getDateTimeStamp().getTimeStamp(), 1000));
                Assertions.assertEquals("Warmup", zgc.getGCCause().getLabel());

                // Durations
                Assertions.assertEquals(toInt(3.558d, 1000), toInt(zgc.getPauseMarkStartTimeStamp().getTimeStamp(), 1000));
                Assertions.assertEquals(toInt(3.558d, 1000), toInt(zgc.getConcurrentMarkTimeStamp().getTimeStamp(), 1000));
                Assertions.assertEquals(toInt(3.573d, 1000), toInt(zgc.getPauseMarkEndTimeStamp().getTimeStamp(), 1000));
                Assertions.assertEquals(toInt(3.574d, 1000), toInt(zgc.getConcurrentProcessNonStrongReferencesTimeStamp().getTimeStamp(), 1000));
                Assertions.assertEquals(toInt(3.577d, 1000), toInt(zgc.getConcurrentResetRelocationSetTimeStamp().getTimeStamp(), 1000));
                Assertions.assertEquals(toInt(3.578d, 1000), toInt(zgc.getConcurrentSelectRelocationSetTimeStamp().getTimeStamp(), 1000));
                Assertions.assertEquals(toInt(3.582d, 1000), toInt(zgc.getPauseRelocateStartTimeStamp().getTimeStamp(), 1000));
                Assertions.assertEquals(toInt(3.583d, 1000), toInt(zgc.getConcurrentRelocateTimeStamp().getTimeStamp(), 1000));

                Assertions.assertEquals( toInt(0.460d, 1000), toInt(zgc.getPauseMarkStartDuration(), 1000));
                Assertions.assertEquals(toInt(14.621d, 1000), toInt(zgc.getConcurrentMarkDuration(), 1000));
                Assertions.assertEquals( toInt(0.830d, 1000), toInt(zgc.getPauseMarkEndDuration(), 1000));
                Assertions.assertEquals( toInt(3.654d, 1000), toInt(zgc.getConcurrentProcessNonStrongReferencesDuration(), 1000));
                Assertions.assertEquals( toInt(0.194d, 1000), toInt(zgc.getConcurrentResetRelocationSetDuration(), 1000));
                Assertions.assertEquals( toInt(3.193d, 1000), toInt(zgc.getConcurrentSelectRelocationSetDuration(), 1000));
                Assertions.assertEquals( toInt(0.794d, 1000), toInt(zgc.getPauseRelocateStartDuration(), 1000));
                Assertions.assertEquals(toInt(12.962d, 1000), toInt(zgc.getConcurrentRelocateDuration(), 1000));

                //Memory
                Assertions.assertTrue(checkZGCMemoryPoolSummary(zgc.getMarkStart(), 936L, 42L, 3160L, 894)); //1074L, 1074L, 1074L));
                Assertions.assertTrue(checkZGCMemoryPoolSummary(zgc.getMarkEnd(), 1074L, 42L, 3084L, 970L));
                Assertions.assertTrue(checkZGCMemoryPoolSummary(zgc.getRelocateStart(),1074L, 42L, 3852L, 202L));
                Assertions.assertTrue(checkZGCMemoryPoolSummary(zgc.getRelocateEnd(), 1074L, 42L, 3868L, 186L));

                Assertions.assertTrue(checkOccupancySummary(zgc.getLive(), 8L, 8L, 8L));
                Assertions.assertTrue(checkOccupancySummary(zgc.getAllocated(), 172L, 172L, 376L));
                Assertions.assertTrue(checkOccupancySummary(zgc.getGarbage(), 885L, 117L, 5L));

                Assertions.assertTrue(checkReclaimSummary(zgc.getReclaimed(), 768L, 880L));
                Assertions.assertTrue(checkReclaimSummary(zgc.getMemorySummary(), 894L, 186L));

                Assertions.assertTrue(zgc.getLoadAverageAt(1) == 4.28);
                Assertions.assertTrue(zgc.getLoadAverageAt(5) == 3.95);
                Assertions.assertTrue(zgc.getLoadAverageAt(15) == 3.22);

                Assertions.assertTrue(zgc.getMMU(2) == 32.7);
                Assertions.assertTrue(zgc.getMMU(5) == 60.8);
                Assertions.assertTrue(zgc.getMMU(10) == 80.4);
                Assertions.assertTrue(zgc.getMMU(20) == 85.4);
                Assertions.assertTrue(zgc.getMMU(50) == 90.8);
                Assertions.assertTrue(zgc.getMMU(100) == 95.4);

            } catch (Throwable t) {
                Assertions.fail(t);
            }
            eventCreated.set(true);
        });

        Arrays.stream(eventLogEntries).forEach(parser::receive);
        Assertions.assertTrue(eventCreated.get());

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
