// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.zgc.OccupancySummary;
import com.microsoft.gctoolkit.event.zgc.ReclaimSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCCycle;
import com.microsoft.gctoolkit.event.zgc.ZGCMemoryPoolSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMetaspaceSummary;
import com.microsoft.gctoolkit.jvm.Diary;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class ZGCParserTest {
    @Test
    public void infoLevelZGCCycle() {

        String[] eventLogEntries = {
                "[32.121s][info][gc,start    ] GC(2) Garbage Collection (Metadata GC Threshold)",
                "[32.121s][info][gc,phases   ] GC(2) Pause Mark Start 0.023ms",
                "[32.166s][info][gc,phases   ] GC(2) Concurrent Mark 44.623ms",
                "[32.166s][info][gc,phases   ] GC(2) Pause Mark End 0.029ms",
                "[32.166s][info][gc,phases   ] GC(2) Concurrent Mark Free 0.001ms",
                "[32.172s][info][gc,phases   ] GC(2) Concurrent Process Non-Strong References 5.797ms",
                "[32.172s][info][gc,phases   ] GC(2) Concurrent Reset Relocation Set 0.012ms",
                "[32.178s][info][gc,phases   ] GC(2) Concurrent Select Relocation Set 6.446ms",
                "[32.179s][info][gc,phases   ] GC(2) Pause Relocate Start 0.024ms",
                "[32.193s][info][gc,phases   ] GC(2) Concurrent Relocate 14.013ms",
                "[32.193s][info][gc,load     ] GC(2) Load: 7.28/6.63/5.01",
                "[32.193s][info][gc,mmu      ] GC(2) MMU: 2ms/98.2%, 5ms/99.3%, 10ms/99.5%, 20ms/99.7%, 50ms/99.9%, 100ms/99.9%",
                "[32.193s][info][gc,marking  ] GC(2) Mark: 4 stripe(s), 3 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s)",
                "[32.193s][info][gc,marking  ] GC(2) Mark Stack Usage: 32M",
                "[32.193s][info][gc,metaspace] GC(2) Metaspace: 60M used, 60M committed, 1080M reserved",
                "[32.193s][info][gc,ref      ] GC(2) Soft: 5447 encountered, 0 discovered, 0 enqueued",
                "[32.193s][info][gc,ref      ] GC(2) Weak: 5347 encountered, 2016 discovered, 810 enqueued",
                "[32.193s][info][gc,ref      ] GC(2) Final: 1041 encountered, 113 discovered, 105 enqueued",
                "[32.193s][info][gc,ref      ] GC(2) Phantom: 558 encountered, 501 discovered, 364 enqueued",
                "[32.193s][info][gc,reloc    ] GC(2) Small Pages: 235 / 470M, Empty: 32M, Relocated: 40M, In-Place: 0",
                "[32.193s][info][gc,reloc    ] GC(2) Medium Pages: 2 / 64M, Empty: 0M, Relocated: 3M, In-Place: 0",
                "[32.193s][info][gc,reloc    ] GC(2) Large Pages: 3 / 24M, Empty: 8M, Relocated: 0M, In-Place: 0",
                "[32.193s][info][gc,reloc    ] GC(2) Forwarding Usage: 13M",
                "[32.193s][info][gc,heap     ] GC(2) Min Capacity: 8M(0%)",
                "[32.193s][info][gc,heap     ] GC(2) Max Capacity: 28686M(100%)",
                "[32.193s][info][gc,heap     ] GC(2) Soft Max Capacity: 28686M(100%)",
                "[32.193s][info][gc,heap     ] GC(2)                Mark Start          Mark End        Relocate Start      Relocate End           High               Low         ",
                "[32.193s][info][gc,heap     ] GC(2)  Capacity:     1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)         1794M (6%)",
                "[32.193s][info][gc,heap     ] GC(2)      Free:    28128M (98%)       28110M (98%)       28148M (98%)       28560M (100%)      28560M (100%)      28108M (98%)",
                "[32.193s][info][gc,heap     ] GC(2)      Used:      558M (2%)          576M (2%)          538M (2%)          126M (0%)          578M (2%)          126M (0%)",
                "[32.193s][info][gc,heap     ] GC(2)      Live:         -                71M (0%)           71M (0%)           71M (0%)             -                  -          ",
                "[32.193s][info][gc,heap     ] GC(2) Allocated:         -                18M (0%)           20M (0%)           18M (0%)             -                  -          ",
                "[32.193s][info][gc,heap     ] GC(2)   Garbage:         -               486M (2%)          446M (2%)           35M (0%)             -                  -          ",
                "[32.193s][info][gc,heap     ] GC(2) Reclaimed:         -                  -                40M (0%)          450M (2%)             -                  -          ",
                "[32.193s][info][gc          ] GC(2) Garbage Collection (Metadata GC Threshold) 558M(2%)->126M(0%)",

        };

        AtomicBoolean eventCreated = new AtomicBoolean(false);
        ZGCParser parser = new ZGCParser(new Diary(), event -> {
            try {
                ZGCCycle zgc = (ZGCCycle) event;

                assertEquals(zgc.getGcId(), 3L);

                assertEquals(toInt(0.0719d,1000), toInt(zgc.getDuration(),1000));
                assertEquals(toInt(32.121d, 1000), toInt(zgc.getDateTimeStamp().getTimeStamp(), 1000));
                assertEquals("Metadata GC Threshold", zgc.getGCCause().getLabel());

                // Durations
                assertEquals(toInt(32.120977d, 1000), toInt(zgc.getPauseMarkStartTimeStamp().getTimeStamp(), 1000));
                assertEquals(toInt(32.121377d, 1000), toInt(zgc.getConcurrentMarkTimeStamp().getTimeStamp(), 1000));
                assertEquals(toInt(32.165999d, 1000), toInt(zgc.getConcurrentMarkFreeTimeStamp().getTimeStamp(), 1000));
                assertEquals(toInt(32.165971d, 1000), toInt(zgc.getPauseMarkEndTimeStamp().getTimeStamp(), 1000));
                assertEquals(toInt(32.166203d, 1000), toInt(zgc.getConcurrentProcessNonStrongReferencesTimeStamp().getTimeStamp(), 1000));
                assertEquals(toInt(32.171988d, 1000), toInt(zgc.getConcurrentResetRelocationSetTimeStamp().getTimeStamp(), 1000));
                assertEquals(toInt(32.171554d, 1000), toInt(zgc.getConcurrentSelectRelocationSetTimeStamp().getTimeStamp(), 1000));
                assertEquals(toInt(32.178976d, 1000), toInt(zgc.getPauseRelocateStartTimeStamp().getTimeStamp(), 1000));
                assertEquals(toInt(32.178987d, 1000), toInt(zgc.getConcurrentRelocateTimeStamp().getTimeStamp(), 1000));

                assertEquals(toInt(0.023d, 1000), toInt(zgc.getPauseMarkStartDuration(), 1000));
                assertEquals(toInt(44.623d, 1000), toInt(zgc.getConcurrentMarkDuration(), 1000));
                assertEquals(toInt(0.001d, 1000), toInt(zgc.getConcurrentMarkFreeDuration(), 1000));
                assertEquals(toInt(0.029d, 1000), toInt(zgc.getPauseMarkEndDuration(), 1000));
                assertEquals(toInt(5.797d, 1000), toInt(zgc.getConcurrentProcessNonStrongReferencesDuration(), 1000));
                assertEquals(toInt(0.012d, 1000), toInt(zgc.getConcurrentResetRelocationSetDuration(), 1000));
                assertEquals(toInt(6.446d, 1000), toInt(zgc.getConcurrentSelectRelocationSetDuration(), 1000));
                assertEquals(toInt(0.024d, 1000), toInt(zgc.getPauseRelocateStartDuration(), 1000));
                assertEquals(toInt(14.013d, 1000), toInt(zgc.getConcurrentRelocateDuration(), 1000));

                //Memory
                assertTrue(checkZGCMemoryPoolSummary(zgc.getMarkStart(), 1837056, 28803072, 571392));
                assertTrue(checkZGCMemoryPoolSummary(zgc.getMarkEnd(), 1837056, 28784640, 589824 ));
                assertTrue(checkZGCMemoryPoolSummary(zgc.getRelocateStart(),1837056, 28823552, 550912));
                assertTrue(checkZGCMemoryPoolSummary(zgc.getRelocateEnd(), 1837056, 29245440, 129024));

                assertTrue(checkZGCMetaSpaceSummary(zgc.getMetaspace(),61440, 61440, 1105920));

                assertTrue(checkOccupancySummary(zgc.getLive(), 72704, 72704, 72704));
                assertTrue(checkOccupancySummary(zgc.getAllocated(), 18432, 20480, 18432));
                assertTrue(checkOccupancySummary(zgc.getGarbage(), 497664, 456704, 35840));

                assertTrue(checkReclaimSummary(zgc.getReclaimed(), 40960, 460800));
                assertTrue(checkReclaimSummary(zgc.getMemorySummary(), 571392, 129024));

                assertEquals(7.28, zgc.getLoadAverageAt(1));
                assertEquals(6.63, zgc.getLoadAverageAt(5));
                assertEquals(5.01, zgc.getLoadAverageAt(15));

                assertEquals(98.2, zgc.getMMU(2));
                assertEquals(99.3, zgc.getMMU(5));
                assertEquals(99.5, zgc.getMMU(10));
                assertEquals(99.7, zgc.getMMU(20));
                assertEquals(99.9, zgc.getMMU(50));
                assertEquals(99.9, zgc.getMMU(100));

            } catch (Throwable t) {
                fail(t);
            }
            eventCreated.set(true);
        });

        Arrays.stream(eventLogEntries).forEach(parser::receive);
        assertTrue(eventCreated.get());

    }

    private boolean checkZGCMetaSpaceSummary(ZGCMetaspaceSummary summary, long used, long committed, long reserved) {
        return summary.getUsed() == used && summary.getCommitted() == committed && summary.getReserved() == reserved;
    }

    private boolean checkZGCMemoryPoolSummary(ZGCMemoryPoolSummary summary, long capacity, long free, long used) {
        return summary.getCapacity() == capacity && summary.getFree() == free && summary.getUsed() == used;
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
