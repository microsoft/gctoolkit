// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.parser.unified;

import com.microsoft.censum.parser.GCParseRule;

public interface ZGCPatterns extends UnifiedPatterns {

    String MEMORY_PERCENT = INT + UNITS + "\\s*\\(" + INT + "%\\)";

    GCParseRule ZGC_TAG = new GCParseRule("ZGC Tag", "Initializing The Z Garbage Collector$");

    //[3.558s][info ][gc,start       ] GC(3) Garbage Collection (Warmup)
    GCParseRule CYCLE_START = new GCParseRule("CYCLE_START", "Garbage Collection " + GC_CAUSE + "$");

    //[3.559s][info ][gc,phases      ] GC(3) Pause Mark Start 0.460ms
    //[3.574s][info ][gc,phases      ] GC(3) Pause Mark End 0.830ms
    //[3.583s][info ][gc,phases      ] GC(3) Pause Relocate Start 0.794ms
    GCParseRule PAUSE_PHASE = new GCParseRule("Pause Phase", "Pause (Mark Start|Mark End|Relocate Start) " + PAUSE_TIME);

    //[3.573s][info ][gc,phases      ] GC(3) Concurrent Mark 14.621ms
    //[3.578s][info ][gc,phases      ] GC(3) Concurrent Process Non-Strong References 3.654ms
    //[3.578s][info ][gc,phases      ] GC(3) Concurrent Reset Relocation Set 0.194ms
    //[3.582s][info ][gc,phases      ] GC(3) Concurrent Select Relocation Set 3.193ms
    //[3.596s][info ][gc,phases      ] GC(3) Concurrent Relocate 12.962ms
    GCParseRule CONCURRENT_PHASE = new GCParseRule("Concurrent Phase","Concurrent (Mark|Process Non-Strong References|Reset Relocation Set|Select Relocation Set|Relocate) " + PAUSE_TIME);

    //[3.596s][info ][gc,load        ] GC(3) Load: 4.28/3.95/3.22
    GCParseRule LOAD = new GCParseRule("Load","Load: " + REAL_VALUE + "/" + REAL_VALUE + "/" + REAL_VALUE);

    //[3.596s][info ][gc,mmu         ] GC(3) MMU: 2ms/32.7%, 5ms/60.8%, 10ms/80.4%, 20ms/85.4%, 50ms/90.8%, 100ms/95.4%
    GCParseRule MMU = new GCParseRule("MMU","MMU: 2ms/" + PERCENTAGE + ", 5ms/" + PERCENTAGE + ", 10ms/" + PERCENTAGE + ", 20ms/" + PERCENTAGE + ", 50ms/" + PERCENTAGE + ", 100ms/" + PERCENTAGE);

    //[3.596s][info ][gc,marking     ] GC(3) Mark: 1 stripe(s), 2 proactive flush(es), 1 terminate flush(es), 1 completion(s), 0 continuation(s)",
    GCParseRule MARK_SUMMARY = new GCParseRule("Mark Summary","Mark: (\\d+) stripe\\(s\\), ");

    //[3.596s][info ][gc,reloc       ] GC(3) Relocation: Successful, 6M relocated
    GCParseRule RELOCATION_SUMMARY = new GCParseRule("Relocation Summary","Relocation: Successful, (\\d+)M relocated");

    //[3.596s][info ][gc,nmethod     ] GC(3) NMethods: 1163 registered, 0 unregistered
    GCParseRule NMETHODS = new GCParseRule("NMethods"," NMethods: " + INT + " registered, " + INT + " unregistered");

    //[3.596s][info ][gc,metaspace   ] GC(3) Metaspace: 14M used, 15M capacity, 15M committed, 16M reserved
    GCParseRule METASPACE = new GCParseRule( "Metaspace", "Metaspace: " + INT + UNITS + " used, " + INT + UNITS + " capacity, " + INT + UNITS + " committed, " + INT + UNITS + " reserved");

    //[3.596s][info ][gc,ref         ] GC(3) Soft: 391 encountered, 0 discovered, 0 enqueued
    //[3.596s][info ][gc,ref         ] GC(3) Weak: 587 encountered, 466 discovered, 0 enqueued
    //[3.596s][info ][gc,ref         ] GC(3) Final: 799 encountered, 0 discovered, 0 enqueued
    //[3.596s][info ][gc,ref         ] GC(3) Phantom: 33 encountered, 1 discovered, 0 enqueued
    GCParseRule REFERENCE_PROCESSING = new GCParseRule("Reference Processing", "(Soft|Weak|Final|Phantom): " + COUNTER + " encountered, " + COUNTER + " discovered, " + COUNTER + " enqueued");

    //[3.596s][info ][gc,heap        ] GC(3) Min Capacity: 8M(0%)
    //[3.596s][info ][gc,heap        ] GC(3) Max Capacity: 4096M(100%)
    //[3.596s][info ][gc,heap        ] GC(3) Soft Max Capacity: 4096M(100%)
    GCParseRule CAPACITY = new GCParseRule("Capacity", "(Min Capacity|Max Capacity|Soft Max Capacity): " + MEMORY_PERCENT);

    //                "[3.596s][info ][gc,heap        ] GC(3)                Mark Start          Mark End        Relocate Start      Relocate End           High               Low",
    GCParseRule MEMORY_TABLE_HEADER = new GCParseRule("Memory Table Header", "Mark Start\\s+Mark End\\s+Relocate Start");

    //[3.596s][info ][gc,heap        ] GC(3)  Capacity:      936M (23%)        1074M (26%)        1074M (26%)        1074M (26%)        1074M (26%)         936M (23%)",
    //[3.596s][info ][gc,heap        ] GC(3)   Reserve:       42M (1%)           42M (1%)           42M (1%)           42M (1%)           42M (1%)           42M (1%)",
    //[3.596s][info ][gc,heap        ] GC(3)      Free:     3160M (77%)        3084M (75%)        3852M (94%)        3868M (94%)        3930M (96%)        3022M (74%)",
    //[3.596s][info ][gc,heap        ] GC(3)      Used:      894M (22%)         970M (24%)         202M (5%)          186M (5%)         1032M (25%)         124M (3%)",
    //************
    //This rule is scary in that it captures CAPACITY IF the spaces are removed from before 'Capacity' in the capture group below.
    //****** WARNING DO NOT REMOVE SPACES FROM THE RULE ******* THINGS WILL BREAK!!!! *************
    GCParseRule MEMORY_TABLE_ENTRY_SIZE = new GCParseRule("Memory table entry size", "\\s*(Capacity|Reserve|Free|Used):\\s+" + MEMORY_PERCENT + "\\s*" + MEMORY_PERCENT + "\\s+" + MEMORY_PERCENT + "\\s+" + MEMORY_PERCENT);

    //[3.596s][info ][gc,heap        ] GC(3)      Live:         -                 8M (0%)            8M (0%)            8M (0%)             -                  -
    //[3.596s][info ][gc,heap        ] GC(3) Allocated:         -               172M (4%)          172M (4%)          376M (9%)             -                  -
    //[3.596s][info ][gc,heap        ] GC(3)   Garbage:         -               885M (22%)         117M (3%)            5M (0%)             -                  -
    GCParseRule MEMORY_TABLE_ENTRY_OCCUPANCY = new GCParseRule("Memory table entry occupancies", "(Live|Allocated|Garbage):\\s+-\\s+"  + MEMORY_PERCENT + "\\s+" + MEMORY_PERCENT + "\\s+" + MEMORY_PERCENT);

    //[3.596s][info ][gc,heap        ] GC(3) Reclaimed:         -                  -               768M (19%)         880M (21%)            -                  -
    GCParseRule MEMORY_TABLE_ENTRY_RECLAIMED = new GCParseRule("Memory table entry reclaimed", "Reclaimed:\\s*-\\s*-\\s*" + MEMORY_PERCENT + "\\s*" + MEMORY_PERCENT);

    //[3.596s][info ][gc             ] GC(3) Garbage Collection (Warmup) 894M(22%)->186M(5%)
    GCParseRule MEMORY_SUMMARY = new GCParseRule("Memory Summary","Garbage Collection " + GC_CAUSE + MEMORY_PERCENT + "->" + MEMORY_PERCENT);

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

}
