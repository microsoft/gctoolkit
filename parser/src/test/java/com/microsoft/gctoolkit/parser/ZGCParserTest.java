// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.zgc.FullZGCCycle;
import com.microsoft.gctoolkit.event.zgc.ZGCAllocatedSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCGarbageSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCLiveSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMemoryPoolSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMemorySummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMetaspaceSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCReclaimSummary;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ZGCParserTest extends ParserTest {

    @Override
    protected Diarizer diarizer() {
        return new UnifiedDiarizer();
    }

    protected GCLogParser parser() {
        return new ZGCParser();
    }

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

        List<JVMEvent> singleCycle = feedParser(eventLogEntries);
        try {
            assertEquals(1, singleCycle.size());
            FullZGCCycle zgc = (FullZGCCycle) singleCycle.get(0);

            assertEquals(zgc.getGcId(), 2L);

            assertEquals(0.0710d, zgc.getDuration(),0.001d);
            assertEquals(toInt(32.121d, 1000), toInt(zgc.getDateTimeStamp().getTimeStamp(), 1000));
            assertEquals("Metadata GC Threshold", zgc.getGCCause().getLabel());
            // Durations
            assertEquals(32.121d, zgc.getPauseMarkStartTimeStamp().getTimeStamp(),  0.001d);
            assertEquals(32.122d, zgc.getConcurrentMarkTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(32.166d, zgc.getConcurrentMarkFreeTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(32.166d, zgc.getPauseMarkEndTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(32.167d, zgc.getConcurrentProcessNonStrongReferencesTimeStamp().getTimeStamp(), 0.002d);
            assertEquals(32.172d, zgc.getConcurrentResetRelocationSetTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(32.172d, zgc.getConcurrentSelectRelocationSetTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(32.179d, zgc.getPauseRelocateStartTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(32.179d, zgc.getConcurrentRelocateTimeStamp().getTimeStamp(), 0.001d);

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

            assertTrue(checkZGCMetaSpaceSummary(zgc.getMetaspaceSummary(),61440, 61440, 1105920));

            assertTrue(checkLiveSummary(zgc.getLiveSummary(), 72704, 72704, 72704));
            assertTrue(checkAllocatedSummary(zgc.getAllocatedSummary(), 18432, 20480, 18432));
            assertTrue(checkGarbageSummary(zgc.getGarbageSummary(), 497664, 456704, 35840));

            assertTrue(checkReclaimSummary(zgc.getReclaimSummary(), 40960, 460800));
            assertTrue(checkMemorySummary(zgc.getMemorySummary(), 571392, 129024));

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
    }

    private boolean checkZGCMemoryPoolSummary(ZGCMemoryPoolSummary summary, long capacity, long free, long used) {
        return summary.getCapacity() == capacity && summary.getFree() == free && summary.getUsed() == used;
    }

    private boolean checkZGCMetaSpaceSummary(ZGCMetaspaceSummary summary, long used, long committed, long reserved) {
        return summary.getUsed() == used && summary.getCommitted() == committed && summary.getReserved() == reserved;
    }

    private boolean checkLiveSummary(ZGCLiveSummary summary, long markEnd, long relocateStart, long relocateEnd) {
                return summary.getMarkEnd() == markEnd && summary.getRelocateStart() == relocateStart && summary.getRelocateEnd() == relocateEnd;
    }

    private boolean checkAllocatedSummary(ZGCAllocatedSummary summary, long markEnd, long relocateStart, long relocateEnd) {
        return summary.getMarkEnd() == markEnd && summary.getRelocateStart() == relocateStart && summary.getRelocateEnd() == relocateEnd;
    }

    private boolean checkGarbageSummary(ZGCGarbageSummary summary, long markEnd, long relocateStart, long relocateEnd) {
        return summary.getMarkEnd() == markEnd && summary.getRelocateStart() == relocateStart && summary.getRelocateEnd() == relocateEnd;
    }

    private boolean checkReclaimSummary(ZGCReclaimSummary summary, long relocateStart, long relocateEnd) {
        return summary.getReclaimStart() == relocateStart && summary.getReclaimEnd() == relocateEnd;
    }

    private boolean checkMemorySummary(ZGCMemorySummary summary, long relocateStart, long relocateEnd) {
        return summary.getOccupancyBefore() == relocateStart && summary.getOccupancyAfter() == relocateEnd;
    }

    private int toInt(double value, int significantDigits) {
        return (int)(value * (double)significantDigits);
    }
}
