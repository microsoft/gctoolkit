// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.zgc.MajorZGCCycle;
import com.microsoft.gctoolkit.event.zgc.MinorZGCCycle;
import com.microsoft.gctoolkit.event.zgc.OccupancySummary;
import com.microsoft.gctoolkit.event.zgc.ZGCAllocatedSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCCompactedSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCCycle;
import com.microsoft.gctoolkit.event.zgc.ZGCGarbageSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCLiveSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMemoryPoolSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMemorySummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMetaspaceSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCPromotedSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCReclaimSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCReferenceSummary;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class GenerationalZGCParserTest extends ParserTest {

    @Override
    protected Diarizer diarizer() {
        return new UnifiedDiarizer();
    }

    protected GCLogParser parser() {
        return new ZGCParser();
    }

    @Test
    public void testZgcMajorCycle() {
        String[] eventLogEntries = {
                "[2025-02-11T13:06:19.476-0800][info][gc          ] GC(1) Major Collection (Metadata GC Threshold)",
                "[2025-02-11T13:06:19.476-0800][info][gc,phases   ] GC(1) Y: Young Generation",
                "[2025-02-11T13:06:19.476-0800][info][gc,phases   ] GC(1) Y: Pause Mark Start (Major) 0.023ms",
                "[2025-02-11T13:06:19.534-0800][info][gc,phases   ] GC(1) Y: Concurrent Mark 57.581ms",
                "[2025-02-11T13:06:19.534-0800][info][gc,phases   ] GC(1) Y: Pause Mark End 0.016ms",
                "[2025-02-11T13:06:19.534-0800][info][gc,phases   ] GC(1) Y: Concurrent Mark Free 0.001ms",
                "[2025-02-11T13:06:19.534-0800][info][gc,phases   ] GC(1) Y: Concurrent Reset Relocation Set 0.005ms",
                "[2025-02-11T13:06:19.536-0800][info][gc,reloc    ] GC(1) Y: Using tenuring threshold: 2 (Computed)",
                "[2025-02-11T13:06:19.538-0800][info][gc,phases   ] GC(1) Y: Concurrent Select Relocation Set 3.879ms",
                "[2025-02-11T13:06:19.538-0800][info][gc,phases   ] GC(1) Y: Pause Relocate Start 0.008ms",
                "[2025-02-11T13:06:19.551-0800][info][gc,phases   ] GC(1) Y: Concurrent Relocate 13.129ms",
                "[2025-02-11T13:06:19.551-0800][info][gc,load     ] GC(1) Y: Load: 7.61 (21%) / 12.83 (36%) / 13.76 (38%)",
                "[2025-02-11T13:06:19.551-0800][info][gc,mmu      ] GC(1) Y: MMU: 2ms/98.9%, 5ms/99.5%, 10ms/99.8%, 20ms/99.8%, 50ms/99.9%, 100ms/99.9%",
                "[2025-02-11T13:06:19.551-0800][info][gc,marking  ] GC(1) Y: Mark: 1 stripe(s), 2 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s) ",
                "[2025-02-11T13:06:19.551-0800][info][gc,marking  ] GC(1) Y: Mark Stack Usage: 32M",
                "[2025-02-11T13:06:19.551-0800][info][gc,metaspace] GC(1) Y: Metaspace: 36M used, 37M committed, 1088M reserved",
                "[2025-02-11T13:06:19.551-0800][info][gc,reloc    ] GC(1) Y:                        Candidates     Selected     In-Place         Size        Empty    Relocated ",
                "[2025-02-11T13:06:19.551-0800][info][gc,reloc    ] GC(1) Y: Small Pages:                  112           85            0         224M          36M           6M ",
                "[2025-02-11T13:06:19.551-0800][info][gc,reloc    ] GC(1) Y: Medium Pages:                   2            0            0          64M          32M           0M ",
                "[2025-02-11T13:06:19.551-0800][info][gc,reloc    ] GC(1) Y: Large Pages:                    1            0            0           8M           0M           0M ",
                "[2025-02-11T13:06:19.551-0800][info][gc,reloc    ] GC(1) Y: Forwarding Usage: 3M",
                "[2025-02-11T13:06:19.551-0800][info][gc,reloc    ] GC(1) Y: Age Table:",
                "[2025-02-11T13:06:19.551-0800][info][gc,reloc    ] GC(1) Y:                    Live             Garbage             Small              Medium             Large        ",
                "[2025-02-11T13:06:19.551-0800][info][gc,reloc    ] GC(1) Y: Eden               8M (0%)          223M (1%)         100 / 78             1 / 0              0 / 0        ",
                "[2025-02-11T13:06:19.551-0800][info][gc,reloc    ] GC(1) Y: Survivor 1        16M (0%)           47M (0%)          12 / 7              1 / 0              1 / 0        ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y: Min Capacity: 36864M(100%)",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y: Max Capacity: 36864M(100%)",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y: Soft Max Capacity: 36864M(100%)",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y: Heap Statistics:",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y:                Mark Start          Mark End        Relocate Start      Relocate End           High               Low         ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y:  Capacity:    36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)   ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y:      Free:    36568M (99%)       36556M (99%)       36624M (99%)       36788M (100%)      36788M (100%)      36556M (99%)    ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y:      Used:      296M (1%)          308M (1%)          240M (1%)           76M (0%)          308M (1%)           76M (0%)     ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y: Young Generation Statistics:",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y:                Mark Start          Mark End        Relocate Start      Relocate End    ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y:      Used:      296M (1%)          308M (1%)          240M (1%)           76M (0%)     ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y:      Live:         -                24M (0%)           24M (0%)           24M (0%)     ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y:   Garbage:         -               271M (1%)          203M (1%)           31M (0%)     ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y: Allocated:         -                12M (0%)           12M (0%)           19M (0%)     ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y: Reclaimed:         -                  -                68M (0%)          239M (1%)     ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y:  Promoted:         -                  -                 0M (0%)            0M (0%)     ",
                "[2025-02-11T13:06:19.551-0800][info][gc,heap     ] GC(1) Y: Compacted:         -                  -                  -                10M (0%)     ",
                "[2025-02-11T13:06:19.551-0800][info][gc,phases   ] GC(1) Y: Young Generation 296M(1%)->76M(0%) 0.075s",
                "[2025-02-11T13:06:19.551-0800][info][gc,phases   ] GC(1) O: Old Generation",
                "[2025-02-11T13:06:19.553-0800][info][gc,phases   ] GC(1) O: Concurrent Mark 1.286ms",
                "[2025-02-11T13:06:19.553-0800][info][gc,phases   ] GC(1) O: Pause Mark End 0.017ms",
                "[2025-02-11T13:06:19.553-0800][info][gc,phases   ] GC(1) O: Concurrent Mark Free 0.000ms",
                "[2025-02-11T13:06:19.562-0800][info][gc,phases   ] GC(1) O: Concurrent Process Non-Strong 9.598ms",
                "[2025-02-11T13:06:19.562-0800][info][gc,phases   ] GC(1) O: Concurrent Reset Relocation Set 0.001ms",
                "[2025-02-11T13:06:19.564-0800][info][gc,phases   ] GC(1) O: Concurrent Select Relocation Set 2.012ms",
                "[2025-02-11T13:06:19.580-0800][info][gc,phases   ] GC(1) O: Concurrent Remap Roots 15.512ms",
                "[2025-02-11T13:06:19.580-0800][info][gc,phases   ] GC(1) O: Pause Relocate Start 0.013ms",
                "[2025-02-11T13:06:19.580-0800][info][gc,phases   ] GC(1) O: Concurrent Relocate 0.050ms",
                "[2025-02-11T13:06:19.580-0800][info][gc,load     ] GC(1) O: Load: 7.61 (21%) / 12.83 (36%) / 13.76 (38%)",
                "[2025-02-11T13:06:19.580-0800][info][gc,mmu      ] GC(1) O: MMU: 2ms/98.9%, 5ms/99.5%, 10ms/99.8%, 20ms/99.8%, 50ms/99.9%, 100ms/99.9%",
                "[2025-02-11T13:06:19.580-0800][info][gc,marking  ] GC(1) O: Mark: 1 stripe(s), 1 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s) ",
                "[2025-02-11T13:06:19.580-0800][info][gc,marking  ] GC(1) O: Mark Stack Usage: 0M",
                "[2025-02-11T13:06:19.580-0800][info][gc,metaspace] GC(1) O: Metaspace: 36M used, 37M committed, 1088M reserved",
                "[2025-02-11T13:06:19.580-0800][info][gc,ref      ] GC(1) O:                       Encountered   Discovered     Enqueued ",
                "[2025-02-11T13:06:19.580-0800][info][gc,ref      ] GC(1) O: Soft References:             4193            0            0 ",
                "[2025-02-11T13:06:19.580-0800][info][gc,ref      ] GC(1) O: Weak References:             2798            0            0 ",
                "[2025-02-11T13:06:19.580-0800][info][gc,ref      ] GC(1) O: Final References:             719            0            0 ",
                "[2025-02-11T13:06:19.580-0800][info][gc,ref      ] GC(1) O: Phantom References:           497            0            0 ",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O: Min Capacity: 36864M(100%)",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O: Max Capacity: 36864M(100%)",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O: Soft Max Capacity: 36864M(100%)",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O: Heap Statistics:",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O:                Mark Start          Mark End        Relocate Start      Relocate End           High               Low         ",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O:  Capacity:    36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)   ",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O:      Free:    36568M (99%)       36788M (100%)      36784M (100%)      36784M (100%)      36788M (100%)      36556M (99%)    ",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O:      Used:      296M (1%)           76M (0%)           80M (0%)           80M (0%)          308M (1%)           76M (0%)     ",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O: Old Generation Statistics:",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O:                Mark Start          Mark End        Relocate Start      Relocate End    ",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O:      Used:        0M (0%)            0M (0%)            0M (0%)            0M (0%)     ",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O:      Live:         -                 0M (0%)            0M (0%)            0M (0%)     ",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O:   Garbage:         -                 0M (0%)            0M (0%)            0M (0%)     ",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O: Allocated:         -                 0M (0%)            0M (0%)            0M (0%)     ",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O: Reclaimed:         -                  -                 0M (0%)            0M (0%)     ",
                "[2025-02-11T13:06:19.580-0800][info][gc,heap     ] GC(1) O: Compacted:         -                  -                  -                 0M (0%)     ",
                "[2025-02-11T13:06:19.580-0800][info][gc,phases   ] GC(1) O: Old Generation 76M(0%)->80M(0%) 0.029s",
                "[2025-02-11T13:06:19.580-0800][info][gc          ] GC(1) Major Collection (Metadata GC Threshold) 296M(1%)->80M(0%) 0.105s",

        };

        List<JVMEvent> singleCycle = feedParser(eventLogEntries);
        try {
            assertEquals(1, singleCycle.size());
            MajorZGCCycle zgc = (MajorZGCCycle) singleCycle.get(0);

            assertEquals(zgc.getGcId(), 1L);

            assertEquals(toInt(0.1039d,1000), toInt(zgc.getDuration(),1000));
            assertEquals(toInt(1.739307979476E9, 1000), toInt(zgc.getDateTimeStamp().getTimeStamp(), 1000));
            assertEquals("Metadata GC Threshold", zgc.getGCCause().getLabel());

            /*
             * Young Phase Checks
             */
            ZGCCycle young = zgc.getYoungCycle();
            // Durations
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.476-0800", 0.023, young.getPauseMarkStartTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.534-0800", 57.581, young.getConcurrentMarkTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.534-0800", 0.001, young.getConcurrentMarkFreeTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.534-0800", 0.016, young.getPauseMarkEndTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.534-0800", 0.005, young.getConcurrentResetRelocationSetTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.538-0800", 3.879, young.getConcurrentSelectRelocationSetTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.538-0800", 0.008, young.getPauseRelocateStartTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.551-0800", 13.129, young.getConcurrentRelocateTimeStamp()));

            assertEquals(toInt(0.023d, 1000), toInt(young.getPauseMarkStartDuration(), 1000));
            assertEquals(toInt(57.581d, 1000), toInt(young.getConcurrentMarkDuration(), 1000));
            assertEquals(toInt(0.001d, 1000), toInt(young.getConcurrentMarkFreeDuration(), 1000));
            assertEquals(toInt(0.016d, 1000), toInt(young.getPauseMarkEndDuration(), 1000));
            assertEquals(toInt(0.005d, 1000), toInt(young.getConcurrentResetRelocationSetDuration(), 1000));
            assertEquals(toInt(3.879d, 1000), toInt(young.getConcurrentSelectRelocationSetDuration(), 1000));
            assertEquals(toInt(0.008d, 1000), toInt(young.getPauseRelocateStartDuration(), 1000));
            assertEquals(toInt(13.129d, 1000), toInt(young.getConcurrentRelocateDuration(), 1000));

            //Memory
            assertTrue(checkZGCMemoryPoolSummary(young.getMarkStart(), 36864, 36568, 296));
            assertTrue(checkZGCMemoryPoolSummary(young.getMarkEnd(), 36864, 36556, 308));
            assertTrue(checkZGCMemoryPoolSummary(young.getRelocateStart(),36864, 36624, 240));
            assertTrue(checkZGCMemoryPoolSummary(young.getRelocateEnd(), 36864, 36788, 76));

            assertTrue(checkZGCMetaSpaceSummary(young.getMetaspaceSummary(),36, 37, 1088));

            assertTrue(checkUsedSummary(young.getUsedOccupancySummary(), 296, 308, 240, 76));
            assertTrue(checkLiveSummary(young.getLiveSummary(), 24, 24, 24));
            assertTrue(checkGarbageSummary(young.getGarbageSummary(), 271, 203, 31));
            assertTrue(checkAllocatedSummary(young.getAllocatedSummary(), 12, 12, 19));

            assertTrue(checkReclaimSummary(young.getReclaimSummary(), 68, 239));
            assertTrue(checkPromotedSummary(young.getPromotedSummary(), 0, 0));
            assertTrue(checkCompactedSummary(young.getCompactedSummary(), 10));

            assertEquals(7.61, young.getLoadAverageAt(1));
            assertEquals(12.83, young.getLoadAverageAt(5));
            assertEquals(13.76, young.getLoadAverageAt(15));

            assertEquals(98.9, young.getMMU(2));
            assertEquals(99.5, young.getMMU(5));
            assertEquals(99.8, young.getMMU(10));
            assertEquals(99.8, young.getMMU(20));
            assertEquals(99.9, young.getMMU(50));
            assertEquals(99.9, young.getMMU(100));

            assertTrue(checkMemorySummary(young.getMemorySummary(), 296, 76));

            /*
             * Old Phase Checks
             */
            ZGCCycle old = zgc.getOldCycle();
            // Durations
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.553-0800", 1.286, old.getConcurrentMarkTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.553-0800", 0.017, old.getPauseMarkEndTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.553-0800", 0.000, old.getConcurrentMarkFreeTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.562-0800", 9.598, old.getConcurrentProcessNonStrongReferencesTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.562-0800", 0.001, old.getConcurrentResetRelocationSetTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.564-0800", 2.012, old.getConcurrentSelectRelocationSetTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.580-0800", 15.512, old.getConcurrentRemapRootsStart()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.580-0800", 0.013, old.getPauseRelocateStartTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:06:19.580-0800", 0.05, old.getConcurrentRelocateTimeStamp()));

            assertEquals(toInt(1.286d, 1000), toInt(old.getConcurrentMarkDuration(), 1000));
            assertEquals(toInt(0.017d, 1000), toInt(old.getPauseMarkEndDuration(), 1000));
            assertEquals(toInt(0.000d, 1000), toInt(old.getConcurrentMarkFreeDuration(), 1000));
            assertEquals(toInt(9.598d, 1000), toInt(old.getConcurrentProcessNonStrongReferencesDuration(), 1000));
            assertEquals(toInt(0.001d, 1000), toInt(old.getConcurrentResetRelocationSetDuration(), 1000));
            assertEquals(toInt(2.012d, 1000), toInt(old.getConcurrentSelectRelocationSetDuration(), 1000));
            assertEquals(toInt(15.512d, 1000), toInt(old.getConcurrentRemapRootsDuration(), 1000));
            assertEquals(toInt(0.013d, 1000), toInt(old.getPauseRelocateStartDuration(), 1000));
            assertEquals(toInt(0.05d, 1000), toInt(old.getConcurrentRelocateDuration(), 1000));

            assertTrue(checkReferenceSummary(old.getSoftRefSummary(), 4193, 0, 0));
            assertTrue(checkReferenceSummary(old.getWeakRefSummary(), 2798, 0, 0));
            assertTrue(checkReferenceSummary(old.getFinalRefSummary(), 719, 0, 0));
            assertTrue(checkReferenceSummary(old.getPhantomRefSummary(), 497, 0, 0));

            //Memory
            assertTrue(checkZGCMemoryPoolSummary(old.getMarkStart(), 36864, 36568, 296));
            assertTrue(checkZGCMemoryPoolSummary(old.getMarkEnd(), 36864, 36788, 76 ));
            assertTrue(checkZGCMemoryPoolSummary(old.getRelocateStart(),36864, 36784, 80));
            assertTrue(checkZGCMemoryPoolSummary(old.getRelocateEnd(), 36864, 36784, 80));

            assertTrue(checkZGCMetaSpaceSummary(old.getMetaspaceSummary(),36, 37, 1088));

            assertTrue(checkUsedSummary(old.getUsedOccupancySummary(), 0, 0, 0, 0));
            assertTrue(checkLiveSummary(old.getLiveSummary(), 0, 0, 0));
            assertTrue(checkGarbageSummary(old.getGarbageSummary(), 0, 0, 0));
            assertTrue(checkAllocatedSummary(old.getAllocatedSummary(), 0, 0, 0));

            assertTrue(checkReclaimSummary(old.getReclaimSummary(), 0, 0));
            assertNull(old.getPromotedSummary());
            assertTrue(checkCompactedSummary(old.getCompactedSummary(), 0));

            assertEquals(7.61, old.getLoadAverageAt(1));
            assertEquals(12.83, old.getLoadAverageAt(5));
            assertEquals(13.76, old.getLoadAverageAt(15));

            assertEquals(98.9, old.getMMU(2));
            assertEquals(99.5, old.getMMU(5));
            assertEquals(99.8, old.getMMU(10));
            assertEquals(99.8, old.getMMU(20));
            assertEquals(99.9, old.getMMU(50));
            assertEquals(99.9, old.getMMU(100));

            assertTrue(checkMemorySummary(old.getMemorySummary(), 76, 80));

        } catch (Throwable t) {
            fail(t);
        }
    }

    @Test
    public void testZgcMinorCycle() {
        String[] eventLogEntries = {
                "[2025-02-11T13:07:12.566-0800][info][gc          ] GC(7) Minor Collection (Allocation Rate)",
                "[2025-02-11T13:07:12.566-0800][info][gc,phases   ] GC(7) y: Young Generation",
                "[2025-02-11T13:07:12.566-0800][info][gc,phases   ] GC(7) y: Pause Mark Start 0.025ms",
                "[2025-02-11T13:07:13.042-0800][info][gc,phases   ] GC(7) y: Concurrent Mark 475.464ms",
                "[2025-02-11T13:07:13.042-0800][info][gc,phases   ] GC(7) y: Pause Mark End 0.022ms",
                "[2025-02-11T13:07:13.042-0800][info][gc,phases   ] GC(7) y: Concurrent Mark Free 0.001ms",
                "[2025-02-11T13:07:13.043-0800][info][gc,phases   ] GC(7) y: Concurrent Reset Relocation Set 0.652ms",
                "[2025-02-11T13:07:13.049-0800][info][gc,reloc    ] GC(7) y: Using tenuring threshold: 3 (Computed)",
                "[2025-02-11T13:07:13.056-0800][info][gc,phases   ] GC(7) y: Concurrent Select Relocation Set 13.394ms",
                "[2025-02-11T13:07:13.057-0800][info][gc,phases   ] GC(7) y: Pause Relocate Start 0.017ms",
                "[2025-02-11T13:07:13.255-0800][info][gc,phases   ] GC(7) y: Concurrent Relocate 198.752ms",
                "[2025-02-11T13:07:13.255-0800][info][gc,load     ] GC(7) y: Load: 17.42 (48%) / 14.37 (40%) / 14.21 (39%)",
                "[2025-02-11T13:07:13.255-0800][info][gc,mmu      ] GC(7) y: MMU: 2ms/97.7%, 5ms/99.1%, 10ms/99.5%, 20ms/99.8%, 50ms/99.9%, 100ms/99.9%",
                "[2025-02-11T13:07:13.255-0800][info][gc,marking  ] GC(7) y: Mark: 1 stripe(s), 2 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s) ",
                "[2025-02-11T13:07:13.255-0800][info][gc,marking  ] GC(7) y: Mark Stack Usage: 32M",
                "[2025-02-11T13:07:13.255-0800][info][gc,metaspace] GC(7) y: Metaspace: 100M used, 101M committed, 1152M reserved",
                "[2025-02-11T13:07:13.255-0800][info][gc,reloc    ] GC(7) y:                        Candidates     Selected     In-Place         Size        Empty    Relocated ",
                "[2025-02-11T13:07:13.255-0800][info][gc,reloc    ] GC(7) y: Small Pages:                 7066         5720            0       14132M        2554M          74M ",
                "[2025-02-11T13:07:13.255-0800][info][gc,reloc    ] GC(7) y: Medium Pages:                   0            0            0           0M           0M           0M ",
                "[2025-02-11T13:07:13.255-0800][info][gc,reloc    ] GC(7) y: Large Pages:                    0            0            0           0M           0M           0M ",
                "[2025-02-11T13:07:13.255-0800][info][gc,reloc    ] GC(7) y: Forwarding Usage: 36M",
                "[2025-02-11T13:07:13.255-0800][info][gc,reloc    ] GC(7) y: Age Table:",
                "[2025-02-11T13:07:13.255-0800][info][gc,reloc    ] GC(7) y:                    Live             Garbage             Small              Medium             Large        ",
                "[2025-02-11T13:07:13.255-0800][info][gc,reloc    ] GC(7) y: Eden              79M (0%)        13800M (37%)       6940 / 5622           0 / 0              0 / 0        ",
                "[2025-02-11T13:07:13.255-0800][info][gc,reloc    ] GC(7) y: Survivor 1        25M (0%)          132M (0%)          79 / 61             0 / 0              0 / 0        ",
                "[2025-02-11T13:07:13.255-0800][info][gc,reloc    ] GC(7) y: Survivor 2        15M (0%)           78M (0%)          47 / 37             0 / 0              0 / 0        ",
                "[2025-02-11T13:07:13.255-0800][info][gc,heap     ] GC(7) y: Min Capacity: 36864M(100%)",
                "[2025-02-11T13:07:13.255-0800][info][gc,heap     ] GC(7) y: Max Capacity: 36864M(100%)",
                "[2025-02-11T13:07:13.255-0800][info][gc,heap     ] GC(7) y: Soft Max Capacity: 36864M(100%)",
                "[2025-02-11T13:07:13.255-0800][info][gc,heap     ] GC(7) y: Heap Statistics:",
                "[2025-02-11T13:07:13.255-0800][info][gc,heap     ] GC(7) y:                Mark Start          Mark End        Relocate Start      Relocate End           High               Low         ",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y:  Capacity:    36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)   ",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y:      Free:    22144M (60%)       21376M (58%)       23912M (65%)       34810M (94%)       34908M (95%)       21370M (58%)    ",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y:      Used:    14720M (40%)       15488M (42%)       12952M (35%)        2054M (6%)        15494M (42%)        1956M (5%)     ",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y: Young Generation Statistics:",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y:                Mark Start          Mark End        Relocate Start      Relocate End    ",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y:      Used:    14132M (38%)       14900M (40%)       12364M (34%)        1466M (4%)     ",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y:      Live:         -               120M (0%)          120M (0%)          120M (0%)     ",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y:   Garbage:         -             14011M (38%)       11457M (31%)           2M (0%)     ",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y: Allocated:         -               768M (2%)          786M (2%)         1343M (4%)     ",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y: Reclaimed:         -                  -              2554M (7%)        14009M (38%)    ",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y:  Promoted:         -                  -                 0M (0%)            0M (0%)     ",
                "[2025-02-11T13:07:13.256-0800][info][gc,heap     ] GC(7) y: Compacted:         -                  -                  -               104M (0%)     ",
                "[2025-02-11T13:07:13.256-0800][info][gc,phases   ] GC(7) y: Young Generation 14720M(40%)->2054M(6%) 0.689s",
                "[2025-02-11T13:07:13.256-0800][info][gc          ] GC(7) Minor Collection (Allocation Rate) 14720M(40%)->2054M(6%) 0.689s"

        };

        List<JVMEvent> singleCycle = feedParser(eventLogEntries);
        try {
            assertEquals(1, singleCycle.size());
            MinorZGCCycle zgc = (MinorZGCCycle) singleCycle.get(0);

            assertEquals(zgc.getGcId(), 7L);

            assertEquals(toInt(0.690d,1000), toInt(zgc.getDuration(),1000));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:07:12.566-0800", 0, zgc.getDateTimeStamp()));
            assertEquals("Allocation Rate", zgc.getGCCause().getLabel());

            /*
             * Young Phase Checks
             */
            ZGCCycle young = zgc.getYoungCycle();
            // Durations
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:07:12.566-0800", 0.025, young.getPauseMarkStartTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:07:13.042-0800", 475.464, young.getConcurrentMarkTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:07:13.042-0800", 0.022, young.getPauseMarkEndTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:07:13.042-0800", 0.001, young.getConcurrentMarkFreeTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:07:13.043-0800", 0.652, young.getConcurrentResetRelocationSetTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:07:13.056-0800", 13.394, young.getConcurrentSelectRelocationSetTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:07:13.057-0800", 0.017, young.getPauseRelocateStartTimeStamp()));
            assertTrue(checkDateTimeStampMatch("2025-02-11T13:07:13.255-0800", 198.752, young.getConcurrentRelocateTimeStamp()));

            assertEquals(toInt(0.025d, 1000), toInt(young.getPauseMarkStartDuration(), 1000));
            assertEquals(toInt(475.464d, 1000), toInt(young.getConcurrentMarkDuration(), 1000));
            assertEquals(toInt(0.022d, 1000), toInt(young.getPauseMarkEndDuration(), 1000));
            assertEquals(toInt(0.001d, 1000), toInt(young.getConcurrentMarkFreeDuration(), 1000));
            assertEquals(toInt(0.652d, 1000), toInt(young.getConcurrentResetRelocationSetDuration(), 1000));
            assertEquals(toInt(13.394d, 1000), toInt(young.getConcurrentSelectRelocationSetDuration(), 1000));
            assertEquals(toInt(0.017d, 1000), toInt(young.getPauseRelocateStartDuration(), 1000));
            assertEquals(toInt(198.752d, 1000), toInt(young.getConcurrentRelocateDuration(), 1000));

            //Memory
            assertTrue(checkZGCMemoryPoolSummary(young.getMarkStart(), 36864, 22144, 14720));
            assertTrue(checkZGCMemoryPoolSummary(young.getMarkEnd(), 36864, 21376, 15488));
            assertTrue(checkZGCMemoryPoolSummary(young.getRelocateStart(),36864, 23912, 12952));
            assertTrue(checkZGCMemoryPoolSummary(young.getRelocateEnd(), 36864, 34810, 2054));

            assertTrue(checkZGCMetaSpaceSummary(young.getMetaspaceSummary(),100, 101, 1152));

            assertTrue(checkUsedSummary(young.getUsedOccupancySummary(), 14132, 14900, 12364, 1466));
            assertTrue(checkLiveSummary(young.getLiveSummary(), 120, 120, 120));
            assertTrue(checkGarbageSummary(young.getGarbageSummary(), 14011, 11457, 2));
            assertTrue(checkAllocatedSummary(young.getAllocatedSummary(), 768, 786, 1343));

            assertTrue(checkReclaimSummary(young.getReclaimSummary(), 2554, 14009));
            assertTrue(checkPromotedSummary(young.getPromotedSummary(), 0, 0));
            assertTrue(checkCompactedSummary(young.getCompactedSummary(), 104));

            assertEquals(17.42, young.getLoadAverageAt(1));
            assertEquals(14.37, young.getLoadAverageAt(5));
            assertEquals(14.21, young.getLoadAverageAt(15));

            assertEquals(97.7, young.getMMU(2));
            assertEquals(99.1, young.getMMU(5));
            assertEquals(99.5, young.getMMU(10));
            assertEquals(99.8, young.getMMU(20));
            assertEquals(99.9, young.getMMU(50));
            assertEquals(99.9, young.getMMU(100));

            assertTrue(checkMemorySummary(young.getMemorySummary(), 14720, 2054));
        } catch (Throwable t) {
            fail(t);
        }
    }

    private boolean checkReferenceSummary(ZGCReferenceSummary refSummary, long encounderted, long discovered, long enqueued) {
        return refSummary.getEncountered() == encounderted && refSummary.getDiscovered() == discovered && refSummary.getEnqueued() == enqueued;
    }

    private boolean checkUsedSummary(OccupancySummary summary, long markStart, long markEnd, long relocateStart, long relocateEnd) {
        return summary.getMarkStart() == (markStart * 1024) && summary.getMarkEnd() == (markEnd * 1024) && summary.getReclaimStart() == (relocateStart * 1024) && summary.getReclaimEnd() == (relocateEnd * 1024);

    }

    private boolean checkDateTimeStampMatch(String expected, double offsetMs, DateTimeStamp dateTimeStamp) {
       return new DateTimeStamp(expected).minus(offsetMs/1000).compareTo(dateTimeStamp) == 0;
    }

    private boolean checkZGCMetaSpaceSummary(ZGCMetaspaceSummary summary, long usedMb, long committedMb, long reservedMb) {
        return summary.getUsed() ==(usedMb * 1024) && summary.getCommitted() == (committedMb * 1024) && summary.getReserved() == (reservedMb * 1024);
    }

    private boolean checkZGCMemoryPoolSummary(ZGCMemoryPoolSummary summary, long capacityMb, long freeMb , long usedMb) {
        return summary.getCapacity() == (capacityMb * 1024) && summary.getFree() == (freeMb * 1024) && summary.getUsed() == (usedMb * 1024);
    }

    private boolean checkLiveSummary(ZGCLiveSummary summary, long markEnd, long relocateStart, long relocateEnd) {
        return summary.getMarkEnd() == (markEnd * 1024) && summary.getRelocateStart() == (relocateStart *1024) && summary.getRelocateEnd() == (relocateEnd * 1024);
    }

    private boolean checkAllocatedSummary(ZGCAllocatedSummary summary, long markEnd, long relocateStart, long relocateEnd) {
        return summary.getMarkEnd() == (markEnd * 1024) && summary.getRelocateStart() == (relocateStart * 1024) && summary.getRelocateEnd() == (relocateEnd * 1024);
    }

    private boolean checkGarbageSummary(ZGCGarbageSummary summary, long markEnd, long relocateStart, long relocateEnd) {
        return summary.getMarkEnd() == (markEnd * 1024) && summary.getRelocateStart() == (relocateStart * 1024) && summary.getRelocateEnd() == (relocateEnd * 1024);
    }

    private boolean checkReclaimSummary(ZGCReclaimSummary summary, long relocateStart, long relocateEnd) {
        return summary.getReclaimStart() == (relocateStart * 1024) && summary.getReclaimEnd() == (relocateEnd * 1024);
    }

    private boolean checkPromotedSummary(ZGCPromotedSummary summary, long relocateStart, long relocateEnd) {
        return summary.getRelocateStart() == (relocateStart * 1024) && summary.getRelocateEnd() == (relocateEnd * 1024);
    }

    private boolean checkCompactedSummary(ZGCCompactedSummary summary, long relocateEnd) {
        return summary.getRelocateEnd() == (relocateEnd * 1024);
    }

    private boolean checkMemorySummary(ZGCMemorySummary summary, long start, long end) {
        return summary.getStart() == (start * 1024) && summary.getEnd() == (end * 1024);
    }

    private int toInt(double value, int significantDigits) {
        return (int)(value * (double)significantDigits);
    }
}
