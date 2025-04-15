// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unified;

import com.microsoft.gctoolkit.parser.GCParseRule;
import com.microsoft.gctoolkit.parser.GenericTokens;

public interface ZGCPatterns extends UnifiedPatterns {

    // Optional Generation tag, Generation ZGC has tags where legacy ZGC does not
    // Y -> Major Young gen
    // O -> Major Old gen
    // y -> Minor Young gen
    String OPT_GEN = "(?:(y|O|Y):)?\\s*";

    String MEMORY_PERCENT = GenericTokens.INT + GenericTokens.UNITS + "\\s*\\(" + GenericTokens.INT + "%\\)";

    GCParseRule ZGC_TAG = new GCParseRule("ZGC Tag", "Using The Z Garbage Collector$");

    //[3.558s][info ][gc,start       ] GC(3) Garbage Collection (Warmup)
    GCParseRule CYCLE_START = new GCParseRule("CYCLE_START", "GC\\(" + GenericTokens.INT + "\\) (Garbage|Major|Minor) Collection " + GenericTokens.GC_CAUSE + "$");

    //[3.559s][info ][gc,phases      ] GC(3) Pause Mark Start 0.460ms
    //[3.574s][info ][gc,phases      ] GC(3) Pause Mark End 0.830ms
    //[3.583s][info ][gc,phases      ] GC(3) Pause Relocate Start 0.794ms
    GCParseRule PAUSE_PHASE = new GCParseRule("Pause Phase", OPT_GEN + "Pause (Mark Start|Mark Start \\(Major\\)|Mark End|Relocate Start) " + GenericTokens.PAUSE_TIME);

    //[3.573s][info ][gc,phases      ] GC(3) Concurrent Mark 14.621ms
    //[3.578s][info ][gc,phases      ] GC(3) Concurrent Process Non-Strong References 3.654ms
    //[3.578s][info ][gc,phases      ] GC(3) Concurrent Reset Relocation Set 0.194ms
    //[3.582s][info ][gc,phases      ] GC(3) Concurrent Select Relocation Set 3.193ms
    //[3.596s][info ][gc,phases      ] GC(3) Concurrent Relocate 12.962ms
    GCParseRule CONCURRENT_PHASE = new GCParseRule("Concurrent Phase", OPT_GEN + "Concurrent (Mark|Mark Continue|Mark Free|Process Non-Strong|Process Non-Strong References|Reset Relocation Set|Select Relocation Set|Relocate|Remap Roots|Mark Roots|Mark Follow|Remap Roots Colored|Remap Roots Uncolored|Remap Remembered) " + GenericTokens.PAUSE_TIME);

    //[3.596s][info ][gc,load        ] GC(3) Load: 4.28/3.95/3.22
    GCParseRule LOAD = new GCParseRule("Load", OPT_GEN + "Load: " + GenericTokens.REAL_VALUE + "/" + GenericTokens.REAL_VALUE + "/" + GenericTokens.REAL_VALUE);

    //[2025-02-11T13:21:06.542-0800][info][gc,load     ] GC(49) O: Load: 18.77 (52%) / 17.33 (48%) / 15.99 (44%)
    GCParseRule LOAD_GEN = new GCParseRule("Load Gen", OPT_GEN + "Load: " + GenericTokens.REAL_VALUE +  " \\(\\d+%?\\) \\/ " + GenericTokens.REAL_VALUE + " \\(\\d+%?\\) \\/ " + GenericTokens.REAL_VALUE + " \\(\\d+%?\\)");

    //[3.596s][info ][gc,mmu         ] GC(3) MMU: 2ms/32.7%, 5ms/60.8%, 10ms/80.4%, 20ms/85.4%, 50ms/90.8%, 100ms/95.4%
    GCParseRule MMU = new GCParseRule("MMU", OPT_GEN + "MMU: 2ms/" + GenericTokens.PERCENTAGE + ", 5ms/" + GenericTokens.PERCENTAGE + ", 10ms/" + GenericTokens.PERCENTAGE + ", 20ms/" + GenericTokens.PERCENTAGE + ", 50ms/" + GenericTokens.PERCENTAGE + ", 100ms/" + GenericTokens.PERCENTAGE);

    //[3.596s][info ][gc,marking     ] GC(3) Mark: 1 stripe(s), 2 proactive flush(es), 1 terminate flush(es), 1 completion(s), 0 continuation(s)",
    GCParseRule MARK_SUMMARY = new GCParseRule("Mark Summary",OPT_GEN + "Mark: " + INT + " stripe\\(s\\), " + INT + " proactive flush\\(es\\), " + INT + " terminate flush\\(es\\), " + INT + " completion\\(s\\), " + INT + " continuation\\(s\\)");

    //[3.596s][info ][gc,reloc       ] GC(3) Relocation: Successful, 6M relocated
    GCParseRule RELOCATION_SUMMARY = new GCParseRule("Relocation Summary",OPT_GEN + "Relocation: Successful, (\\d+)M relocated");

    //[2025-03-07T17:46:45.563-0800][info][gc,reloc    ] GC(380) Y:                        Candidates     Selected     In-Place         Size        Empty    Relocated
    //[2025-03-07T17:46:45.563-0800][info][gc,reloc    ] GC(380) Y: Small Pages:                53993        26715            0      107986M       53924M         180M
    //[2025-03-07T17:46:45.563-0800][info][gc,reloc    ] GC(380) Y: Medium Pages:                   2            0            0          64M          32M           0M
    //[2025-03-07T17:46:45.563-0800][info][gc,reloc    ] GC(380) Y: Large Pages:                    0            0            0           0M           0M           0M
    GCParseRule PAGES_GEN = new GCParseRule("Page Summary",OPT_GEN + "(Small|Medium|Large) Pages:\\s+" + INT + "\\s+" + INT + "\\s+" + INT + "\\s+" + INT + UNITS + "\\s+" + INT + UNITS + "\\s+" + INT + UNITS);

    //[2025-03-07T17:46:50.397-0800][info][gc,reloc    ] GC(380) O: Forwarding Usage: 125M
    GCParseRule FORWARDING_USAGE_GEN = new GCParseRule("Forwarding Usage",OPT_GEN + "Forwarding Usage: " + INT + UNITS);

    // [2025-03-07T17:49:56.358-0800][info][gc,reloc    ] GC(389) y:                    Live             Garbage             Small              Medium             Large
    //[2025-03-07T17:49:56.358-0800][info][gc,reloc    ] GC(389) y: Eden             234M (0%)       156765M (79%)      78484 / 36719          1 / 0              0 / 0
    //[2025-03-07T17:49:56.358-0800][info][gc,reloc    ] GC(389) y: Survivor 1        52M (0%)          649M (0%)         335 / 275            1 / 1              0 / 0
    //[2025-03-07T17:49:56.358-0800][info][gc,reloc    ] GC(389) y: Survivor 2        40M (0%)          255M (0%)         148 / 91             0 / 0              0 / 0
    //[2025-03-07T17:49:56.359-0800][info][gc,reloc    ] GC(389) y: Survivor 3        38M (0%)           65M (0%)          52 / 20             0 / 0              0 / 0
    GCParseRule AGE_TABLE_GEN = new GCParseRule("Age Table",OPT_GEN + "(Eden|Survivor \\d+)\\s+" + MEMORY_PERCENT + "\\s+" + MEMORY_PERCENT + "\\s+" + INT + " \\/ " + INT + "\\s+" + INT + " \\/ " + INT + "\\s+" + INT + " \\/ " + INT );

    //[3.596s][info ][gc,nmethod     ] GC(3) NMethods: 1163 registered, 0 unregistered
    GCParseRule NMETHODS = new GCParseRule("NMethods",OPT_GEN + " NMethods: " + GenericTokens.INT + " registered, " + GenericTokens.INT + " unregistered");

    //[3.596s][info ][info ][gc,metaspace   ] GC(3) Metaspace: 84M used, 85M committed, 1104M reserved
    GCParseRule METASPACE = new GCParseRule( "Metaspace", OPT_GEN + "Metaspace: " + GenericTokens.INT + GenericTokens.UNITS + " used, " + GenericTokens.INT + GenericTokens.UNITS + " committed, " + GenericTokens.INT + GenericTokens.UNITS + " reserved");

    //[3.596s][info ][gc,ref         ] GC(3) Soft: 391 encountered, 0 discovered, 0 enqueued
    //[3.596s][info ][gc,ref         ] GC(3) Weak: 587 encountered, 466 discovered, 0 enqueued
    //[3.596s][info ][gc,ref         ] GC(3) Final: 799 encountered, 0 discovered, 0 enqueued
    //[3.596s][info ][gc,ref         ] GC(3) Phantom: 33 encountered, 1 discovered, 0 enqueued
    GCParseRule REFERENCE_PROCESSING = new GCParseRule("Reference Processing", OPT_GEN + "(Soft|Weak|Final|Phantom): " + GenericTokens.COUNTER + " encountered, " + GenericTokens.COUNTER + " discovered, " + GenericTokens.COUNTER + " enqueued");

    // Generation ZGC
    //[2025-02-11T13:06:17.709-0800][info][gc,ref      ] GC(0) O:                       Encountered   Discovered     Enqueued
    //[2025-02-11T13:06:17.709-0800][info][gc,ref      ] GC(0) O: Soft References:             3642            0            0
    //[2025-02-11T13:06:17.709-0800][info][gc,ref      ] GC(0) O: Weak References:             1507            0            0
    //[2025-02-11T13:06:17.709-0800][info][gc,ref      ] GC(0) O: Final References:               0            0            0
    //[2025-02-11T13:06:17.709-0800][info][gc,ref      ] GC(0) O: Phantom References:           176            0            0
    GCParseRule REFERENCE_PROCESSING_GEN = new GCParseRule("Reference Processing", OPT_GEN + "(Soft|Weak|Final|Phantom) References:\\s+" + GenericTokens.COUNTER + "\\s+" + GenericTokens.COUNTER + "\\s+" + GenericTokens.COUNTER);

    //[3.596s][info ][gc,heap        ] GC(3) Min Capacity: 8M(0%)
    //[3.596s][info ][gc,heap        ] GC(3) Max Capacity: 4096M(100%)
    //[3.596s][info ][gc,heap        ] GC(3) Soft Max Capacity: 4096M(100%)
    GCParseRule CAPACITY = new GCParseRule("Capacity", OPT_GEN + "(Min Capacity|Max Capacity|Soft Max Capacity): " + MEMORY_PERCENT);

    //[3.596s][info ][gc,heap        ] GC(3)                Mark Start          Mark End        Relocate Start      Relocate End           High               Low
    GCParseRule MEMORY_TABLE_HEADER = new GCParseRule("Memory Table Header", OPT_GEN + "Mark Start\\s+Mark End\\s+Relocate Start");

    //[3.596s][info ][gc,heap        ] GC(3)  Capacity:    36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)      36864M (100%)
    //[3.596s][info ][gc,heap        ] GC(3)      Free:    30614M (83%)       30460M (83%)       30988M (84%)       35938M (97%)       35938M (97%)       30458M (83%)
    //[3.596s][info ][gc,heap        ] GC(3)      Used:     6250M (17%)        6404M (17%)        5876M (16%)         926M (3%)         6406M (17%)         926M (3%)
    //************
    //This rule is scary in that it captures CAPACITY IF the spaces are removed from before 'Capacity' in the capture group below.
    //****** WARNING DO NOT REMOVE SPACES FROM THE RULE ******* THINGS WILL BREAK!!!! *************
    GCParseRule MEMORY_TABLE_ENTRY_SIZE = new GCParseRule("Memory table entry size", OPT_GEN + "\\s*(Capacity||Free|Used):\\s+" + MEMORY_PERCENT + "\\s*" + MEMORY_PERCENT + "\\s+" + MEMORY_PERCENT + "\\s+" + MEMORY_PERCENT);

    // Gen ZGC
    // [info][gc,heap     ] GC(2) O: Old Generation Statistics:
    GCParseRule MARK_OLD_GEN_HEAP_STATS = new GCParseRule("Mark Old Gen Stats", OPT_GEN + "Old Generation Statistics");

    // Gen ZGC
    // [info][gc,heap     ] GC(4) Y: Young Generation Statistics:
    GCParseRule MARK_YOUNG_GEN_HEAP_STATS = new GCParseRule("Mark Young Gen Stats", OPT_GEN + "Young Generation Statistics");

    //[3.596s][info ][gc,heap        ] GC(3)      Live:         -                 8M (0%)            8M (0%)            8M (0%)             -                  -
    //[3.596s][info ][gc,heap        ] GC(3) Allocated:         -               172M (4%)          172M (4%)          376M (9%)             -                  -
    //[3.596s][info ][gc,heap        ] GC(3)   Garbage:         -               885M (22%)         117M (3%)            5M (0%)             -                  -
    GCParseRule MEMORY_TABLE_ENTRY_OCCUPANCY = new GCParseRule("Memory table entry occupancies", OPT_GEN + "(Live|Allocated|Garbage):\\s+-\\s+"  + MEMORY_PERCENT + "\\s+" + MEMORY_PERCENT + "\\s+" + MEMORY_PERCENT);

    //[info][gc,heap     ] GC(7) y: Reclaimed:         -                  -              2554M (7%)        14009M (38%)
    //[info][gc,heap     ] GC(7) y:  Promoted:         -                  -                 0M (0%)            0M (0%)
    GCParseRule MEMORY_TABLE_ENTRY_RECLAIMED_PROMOTED = new GCParseRule("Memory table entry reclaimed", OPT_GEN + "(Reclaimed|Promoted):\\s*-\\s*-\\s*" + MEMORY_PERCENT + "\\s*" + MEMORY_PERCENT);

    // Generational ZGC
    // [2025-02-11T13:21:45.856-0800][info][gc,heap     ] GC(51) y: Compacted:         -                  -                  -                88M (0%)
    GCParseRule MEMORY_TABLE_ENTRY_COMPACTED = new GCParseRule("Memory table entry compacted", OPT_GEN + "Compacted:\\s*-\\s*-\\s*-\\s*" + MEMORY_PERCENT);

    // [info][gc,phases   ] GC(58) Y: Young Generation 16052M(44%)->3022M(8%) 1.017s
    // [info][gc,phases   ] GC(58) O: Old Generation 3022M(8%)->5486M(15%) 1.757s
    GCParseRule END_OF_PHASE_SUMMARY_GEN = new GCParseRule("End of Phase Summary", OPT_GEN + "(Old|Young) Generation " + MEMORY_PERCENT + "->" + MEMORY_PERCENT + "\\s*" + PAUSE_TIME);


    //[3.596s][info ][gc         ] GC(3) Garbage Collection (Warmup) 894M(22%)->186M(5%)
    // or
    // Gen GC
    //[3.596s][info][gc          ] GC(7) Minor Collection (Allocation Rate) 14720M(40%)->2054M(6%) 0.689s
    GCParseRule MEMORY_SUMMARY = new GCParseRule("Memory Summary", "(Garbage|Minor|Major) Collection " + GenericTokens.GC_CAUSE + MEMORY_PERCENT + "->" + MEMORY_PERCENT + "(?:\\s*" + PAUSE_TIME + ")?");

    /*
    todo: capture and report on these log entries
        [0.009s][debug][gc,heap] Minimum heap 8388608  Initial heap 268435456  Maximum heap 4294967296
        [0.009s][info ][gc,init] Initializing The Z Garbage Collector
        [0.009s][info ][gc,init] Version: 14+36-1461 (release)
        [0.009s][info ][gc,init] NUMA Support: Disabled
        [0.009s][info ][gc,init] CPUs: 8 total, 8 available
        [0.009s][info ][gc,init] Memory: 16384M
        [0.009s][info ][gc,init] Large Page Support: Disabled
        [0.009s][info ][gc,init] Medium Page Size: 32M
        [0.009s][info ][gc,init] Workers: 5 parallel, 1 concurrent
        [0.010s][debug][gc,task] Executing Task: ZWorkersInitializeTask, Active Workers: 5
        [0.010s][info ][gc,init] Address Space Type: Contiguous/Unrestricted/Complete
        [0.010s][info ][gc,init] Address Space Size: 65536M x 3 = 196608M
        [0.010s][info ][gc,init] Min Capacity: 8M
        [0.010s][info ][gc,init] Initial Capacity: 256M
        [0.010s][info ][gc,init] Max Capacity: 4096M
        [0.010s][info ][gc,init] Max Reserve: 42M
        [0.010s][info ][gc,init] Pre-touch: Disabled
        [0.010s][info ][gc,init] Uncommit: Enabled, Delay: 300s
     */

    /*
        [4.179s][info][gc,heap     ] GC(0) Y: Heap Statistics:
        [4.179s][info][gc,heap     ] GC(0) Y: Young Generation Statistics:
        [4.220s][info][gc,heap     ] GC(0) O: Heap Statistics:
        [4.220s][info][gc,heap     ] GC(0) O: Old Generation Statistics:
     */
    GCParseRule GENERATIONAL_MEMORY_STATISTICS = new GCParseRule("Generational Heap Statistics","(Young Generation|Old Generation|Heap) Statistics:");
}
