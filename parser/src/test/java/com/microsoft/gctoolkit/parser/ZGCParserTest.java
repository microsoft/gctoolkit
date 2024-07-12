// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.zgc.OccupancySummary;
import com.microsoft.gctoolkit.event.zgc.ReclaimSummary;
import com.microsoft.gctoolkit.event.zgc.MajorZGCCycle;
import com.microsoft.gctoolkit.event.zgc.ZGCMemoryPoolSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMetaspaceSummary;
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
            MajorZGCCycle zgc = (MajorZGCCycle) singleCycle.get(0);

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
    }

    @Test
    public void infoLevelGenerationalZGCCycle() {

        String[] eventLogEntries = {
                "[4.120s][info][gc          ] GC(0) Major Collection (Metadata GC Threshold)" ,
                "[4.120s][info][gc,task     ] GC(0) Using 1 Workers for Young Generation" ,
                "[4.120s][info][gc,task     ] GC(0) Using 1 Workers for Old Generation" ,
                "[4.120s][info][gc,phases   ] GC(0) Y: Young Generation" ,
                "[4.120s][info][gc,phases   ] GC(0) Y: Pause Mark Start (Major) 0.066ms" ,
                "[4.155s][info][gc,phases   ] GC(0) Y: Concurrent Mark 34.619ms" ,
                "[4.155s][info][gc,phases   ] GC(0) Y: Pause Mark End 0.016ms" ,
                "[4.155s][info][gc,phases   ] GC(0) Y: Concurrent Mark Free 0.001ms" ,
                "[4.155s][info][gc,phases   ] GC(0) Y: Concurrent Reset Relocation Set 0.001ms" ,
                "[4.163s][info][gc,reloc    ] GC(0) Y: Using tenuring threshold: 1 (Computed)" ,
                "[4.165s][info][gc,phases   ] GC(0) Y: Concurrent Select Relocation Set 10.037ms" ,
                "[4.165s][info][gc,phases   ] GC(0) Y: Pause Relocate Start 0.014ms" ,
                "[4.179s][info][gc,phases   ] GC(0) Y: Concurrent Relocate 13.784ms" ,
                "[4.179s][info][gc,alloc    ] GC(0) Y:                         Mark Start        Mark End      Relocate Start    Relocate End" ,
                "[4.179s][info][gc,alloc    ] GC(0) Y: Allocation Stalls:          0                0                0                0" ,
                "[4.179s][info][gc,load     ] GC(0) Y: Load: 9.07 (19%) / 9.48 (20%) / 10.07 (21%)" ,
                "[4.179s][info][gc,mmu      ] GC(0) Y: MMU: 2ms/96.7%, 5ms/98.7%, 10ms/99.3%, 20ms/99.7%, 50ms/99.8%, 100ms/99.9%" ,
                "[4.179s][info][gc,marking  ] GC(0) Y: Mark: 1 stripe(s), 2 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s)" ,
                "[4.179s][info][gc,marking  ] GC(0) Y: Mark Stack Usage: 32M" ,
                "[4.179s][info][gc,nmethod  ] GC(0) Y: NMethods: 2191 registered, 0 unregistered" ,
                "[4.179s][info][gc,metaspace] GC(0) Y: Metaspace: 21M used, 21M committed, 1088M reserved" ,
                "[4.179s][info][gc,reloc    ] GC(0) Y:                        Candidates     Selected     In-Place         Size        Empty    Relocated" ,
                "[4.179s][info][gc,reloc    ] GC(0) Y: Small Pages:                   49           40            0          98M           0M           6M" ,
                "[4.179s][info][gc,reloc    ] GC(0) Y: Medium Pages:                   1            0            0          32M           0M           0M" ,
                "[4.179s][info][gc,reloc    ] GC(0) Y: Large Pages:                    0            0            0           0M           0M           0M" ,
                "[4.179s][info][gc,reloc    ] GC(0) Y: Forwarding Usage: 2M" ,
                "[4.179s][info][gc,reloc    ] GC(0) Y: Age Table:" ,
                "[4.179s][info][gc,reloc    ] GC(0) Y:                    Live             Garbage             Small              Medium             Large" ,
                "[4.179s][info][gc,reloc    ] GC(0) Y: Eden              16M (1%)          113M (4%)          49 / 40             1 / 0              0 / 0" ,
                "[4.179s][info][gc,heap     ] GC(0) Y: Min Capacity: 3000M(100%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y: Max Capacity: 3000M(100%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y: Soft Max Capacity: 3000M(100%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y: Heap Statistics:" ,
                "[4.179s][info][gc,heap     ] GC(0) Y:                Mark Start          Mark End        Relocate Start      Relocate End           High               Low" ,
                "[4.179s][info][gc,heap     ] GC(0) Y:  Capacity:     3000M (100%)       3000M (100%)       3000M (100%)       3000M (100%)       3000M (100%)       3000M (100%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y:      Free:     2870M (96%)        2866M (96%)        2866M (96%)        2928M (98%)        2934M (98%)        2862M (95%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y:      Used:      130M (4%)          134M (4%)          134M (4%)           72M (2%)          138M (5%)           66M (2%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y: Young Generation Statistics:" ,
                "[4.179s][info][gc,heap     ] GC(0) Y:                Mark Start          Mark End        Relocate Start      Relocate End" ,
                "[4.179s][info][gc,heap     ] GC(0) Y:      Used:      130M (4%)          134M (4%)          134M (4%)           72M (2%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y:      Live:         -                16M (1%)           16M (1%)           16M (1%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y:   Garbage:         -               113M (4%)          113M (4%)           32M (1%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y: Allocated:         -                 4M (0%)            4M (0%)           23M (1%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y: Reclaimed:         -                  -                 0M (0%)           81M (3%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y:  Promoted:         -                  -                 0M (0%)            0M (0%)" ,
                "[4.179s][info][gc,heap     ] GC(0) Y: Compacted:         -                  -                  -                 8M (0%)" ,
                "[4.179s][info][gc,phases   ] GC(0) Y: Young Generation 130M(4%)->72M(2%) 0.059s" ,
                "[4.179s][info][gc,phases   ] GC(0) O: Old Generation" ,
                "[4.182s][info][gc,phases   ] GC(0) O: Concurrent Mark 2.407ms" ,
                "[4.182s][info][gc,phases   ] GC(0) O: Pause Mark End 0.014ms" ,
                "[4.182s][info][gc,phases   ] GC(0) O: Concurrent Mark Free 0.009ms" ,
                "[4.189s][info][gc,phases   ] GC(0) O: Concurrent Process Non-Strong 7.001ms" ,
                "[4.189s][info][gc,phases   ] GC(0) O: Concurrent Reset Relocation Set 0.001ms" ,
                "[4.193s][info][gc,phases   ] GC(0) O: Concurrent Select Relocation Set 4.466ms" ,
                "[4.193s][info][gc,task     ] GC(0) O: Using 1 Workers for Old Generation" ,
                "[4.218s][info][gc,task     ] GC(0) O: Using 1 Workers for Old Generation" ,
                "[4.218s][info][gc,phases   ] GC(0) O: Concurrent Remap Roots 24.683ms" ,
                "[4.220s][info][gc,phases   ] GC(0) O: Pause Relocate Start 0.013ms" ,
                "[4.220s][info][gc,phases   ] GC(0) O: Concurrent Relocate 0.043ms" ,
                "[4.220s][info][gc,alloc    ] GC(0) O:                         Mark Start        Mark End      Relocate Start    Relocate End" ,
                "[4.220s][info][gc,alloc    ] GC(0) O: Allocation Stalls:          0                0                0                0" ,
                "[4.220s][info][gc,load     ] GC(0) O: Load: 9.07 (19%) / 9.48 (20%) / 10.07 (21%)" ,
                "[4.220s][info][gc,mmu      ] GC(0) O: MMU: 2ms/96.7%, 5ms/98.7%, 10ms/99.3%, 20ms/99.7%, 50ms/99.8%, 100ms/99.9%" ,
                "[4.220s][info][gc,marking  ] GC(0) O: Mark: 1 stripe(s), 1 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s)" ,
                "[4.220s][info][gc,marking  ] GC(0) O: Mark Stack Usage: 0M" ,
                "[4.220s][info][gc,nmethod  ] GC(0) O: NMethods: 1891 registered, 334 unregistered" ,
                "[4.220s][info][gc,metaspace] GC(0) O: Metaspace: 22M used, 22M committed, 1088M reserved" ,
                "[4.220s][info][gc,ref      ] GC(0) O:                       Encountered   Discovered     Enqueued" ,
                "[4.220s][info][gc,ref      ] GC(0) O: Soft References:             3666            0            0" ,
                "[4.220s][info][gc,ref      ] GC(0) O: Weak References:             1214            0            0" ,
                "[4.220s][info][gc,ref      ] GC(0) O: Final References:              19            0            0" ,
                "[4.220s][info][gc,ref      ] GC(0) O: Phantom References:           767            0            0" ,
                "[4.220s][info][gc,heap     ] GC(0) O: Min Capacity: 3000M(100%)" ,
                "[4.220s][info][gc,heap     ] GC(0) O: Max Capacity: 3000M(100%)" ,
                "[4.220s][info][gc,heap     ] GC(0) O: Soft Max Capacity: 3000M(100%)" ,
                "[4.220s][info][gc,heap     ] GC(0) O: Heap Statistics:" ,
                "[4.220s][info][gc,heap     ] GC(0) O:                Mark Start          Mark End        Relocate Start      Relocate End           High               Low" ,
                "[4.220s][info][gc,heap     ] GC(0) O:  Capacity:     3000M (100%)       3000M (100%)       3000M (100%)       3000M (100%)       3000M (100%)       3000M (100%)" ,
                "[4.220s][info][gc,heap     ] GC(0) O:      Free:     2870M (96%)        2928M (98%)        2926M (98%)        2926M (98%)        2934M (98%)        2862M (95%)" ,
                "[4.220s][info][gc,heap     ] GC(0) O:      Used:      130M (4%)           72M (2%)           74M (2%)           74M (2%)          138M (5%)           66M (2%)" ,
                "[4.220s][info][gc,heap     ] GC(0) O: Old Generation Statistics:" ,
                "[4.220s][info][gc,heap     ] GC(0) O:                Mark Start          Mark End        Relocate Start      Relocate End" ,
                "[4.220s][info][gc,heap     ] GC(0) O:      Used:        0M (0%)            0M (0%)            0M (0%)            0M (0%)" ,
                "[4.220s][info][gc,heap     ] GC(0) O:      Live:         -                 0M (0%)            0M (0%)            0M (0%)" ,
                "[4.220s][info][gc,heap     ] GC(0) O:   Garbage:         -                 0M (0%)            0M (0%)            0M (0%)" ,
                "[4.220s][info][gc,heap     ] GC(0) O: Allocated:         -                 0M (0%)            0M (0%)            0M (0%)" ,
                "[4.220s][info][gc,heap     ] GC(0) O: Reclaimed:         -                  -                 0M (0%)            0M (0%)" ,
                "[4.220s][info][gc,heap     ] GC(0) O: Compacted:         -                  -                  -                 0M (0%)" ,
                "[4.220s][info][gc,phases   ] GC(0) O: Old Generation 72M(2%)->74M(2%) 0.041s" ,
                "[4.220s][info][gc          ] GC(0) Major Collection (Metadata GC Threshold) 130M(4%)->74M(2%) 0.100s"
        };

        List<JVMEvent> singleCycle = feedParser(eventLogEntries);
        try {
            assertEquals(1, singleCycle.size());
            MajorZGCCycle zgc = (MajorZGCCycle) singleCycle.get(0);

            assertEquals(zgc.getGcId(), 0L);

            assertEquals(0.1d, zgc.getDuration(),0.001d);
            assertEquals(toInt(4.120d, 1000), toInt(zgc.getDateTimeStamp().getTimeStamp(), 1000));
            assertEquals("Metadata GC Threshold", zgc.getGCCause().getLabel());
            // Durations
            assertEquals(4.120d, zgc.getPauseMarkStartTimeStamp().getTimeStamp(),  0.001d);
            assertEquals(4.120d, zgc.getConcurrentMarkTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(4.155d, zgc.getConcurrentMarkFreeTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(4.155d, zgc.getPauseMarkEndTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(4.182d, zgc.getConcurrentProcessNonStrongReferencesTimeStamp().getTimeStamp(), 0.002d);
            assertEquals(4.155d, zgc.getConcurrentResetRelocationSetTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(4.155d, zgc.getConcurrentSelectRelocationSetTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(4.165d, zgc.getPauseRelocateStartTimeStamp().getTimeStamp(), 0.001d);
            assertEquals(4.165d, zgc.getConcurrentRelocateTimeStamp().getTimeStamp(), 0.001d);

            assertEquals(0.066d, zgc.getPauseMarkStartDuration(),  0.001);
            assertEquals(37.026d, zgc.getConcurrentMarkDuration(),  0.001);
            assertEquals(0.010d, zgc.getConcurrentMarkFreeDuration(),  0.001);
            assertEquals(0.030, zgc.getPauseMarkEndDuration(),  0.001);
            assertEquals(7.001d , zgc.getConcurrentProcessNonStrongReferencesDuration(),  0.001);
            assertEquals(0.002d,zgc.getConcurrentResetRelocationSetDuration(),  0.001);
            assertEquals(14.503d, zgc.getConcurrentSelectRelocationSetDuration(),  0.001);
            assertEquals(0.027d, zgc.getPauseRelocateStartDuration(),  0.001);
            assertEquals(13.827d,zgc.getConcurrentRelocateDuration(),  0.001);

            //Memory
            assertTrue(checkZGCMemoryPoolSummary(zgc.getMarkStart(), 3072000, 2938880, 133120));
            assertTrue(checkZGCMemoryPoolSummary(zgc.getMarkEnd(), 3072000, 2998272, 73728 ));
            assertTrue(checkZGCMemoryPoolSummary(zgc.getRelocateStart(),3072000, 2996224, 75776));
            assertTrue(checkZGCMemoryPoolSummary(zgc.getRelocateEnd(), 3072000, 2996224, 75776));

            assertTrue(checkZGCMetaSpaceSummary(zgc.getMetaspace(),22528, 22528, 1114112));

            assertTrue(checkOccupancySummary(zgc.getLive(), 16384, 16384, 16384));
            assertTrue(checkOccupancySummary(zgc.getAllocated(), 4096, 4096, 23552));
            assertTrue(checkOccupancySummary(zgc.getGarbage(), 115712, 115712, 32768));

            assertTrue(checkReclaimSummary(zgc.getReclaimed(), 0, 82944));
            assertTrue(checkReclaimSummary(zgc.getMemorySummary(), 133120, 75776));

            assertEquals(9.07, zgc.getLoadAverageAt(1));
            assertEquals(9.48, zgc.getLoadAverageAt(5));
            assertEquals(10.07, zgc.getLoadAverageAt(15));

            assertEquals(96.7, zgc.getMMU(2));
            assertEquals(98.7, zgc.getMMU(5));
            assertEquals(99.3, zgc.getMMU(10));
            assertEquals(99.7, zgc.getMMU(20));
            assertEquals(99.8, zgc.getMMU(50));
            assertEquals(99.9, zgc.getMMU(100));

        } catch (Throwable t) {
            fail(t);
        }
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
