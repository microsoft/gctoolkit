package com.microsoft.gctoolkit.parser.unified;

public interface GenZPatterns extends UnifiedPatterns {
    /*
[2024-04-17T00:42:12.516+0000][0.034s][info][gc,init] Initializing The Z Garbage Collector
[2024-04-17T00:42:12.516+0000][0.034s][info][gc,init] Version: 21+35-LTS (release)
[2024-04-17T00:42:12.516+0000][0.034s][info][gc,init] NUMA Support: Disabled
[2024-04-17T00:42:12.516+0000][0.034s][info][gc,init] CPUs: 32 total, 32 available
[2024-04-17T00:42:12.517+0000][0.035s][info][gc,init] Memory: 257107M
[2024-04-17T00:42:12.517+0000][0.035s][info][gc,init] Large Page Support: Enabled (Transparent)
[2024-04-17T00:42:12.517+0000][0.035s][info][gc,init] Address Space Type: Contiguous/Unrestricted/Complete
[2024-04-17T00:42:12.517+0000][0.035s][info][gc,init] Address Space Size: 2834432M
[2024-04-17T00:42:12.517+0000][0.035s][info][gc,init] Heap Backing File: /memfd:java_heap
[2024-04-17T00:42:12.517+0000][0.035s][info][gc,init] Heap Backing Filesystem: tmpfs (0x1021994)
[2024-04-17T00:42:12.518+0000][0.036s][info][gc,init] Min Capacity: 177152M
[2024-04-17T00:42:12.518+0000][0.036s][info][gc,init] Initial Capacity: 177152M
[2024-04-17T00:42:12.518+0000][0.036s][info][gc,init] Max Capacity: 177152M
[2024-04-17T00:42:12.518+0000][0.036s][info][gc,init] Soft Max Capacity: 177152M
[2024-04-17T00:42:12.518+0000][0.036s][info][gc,init] Medium Page Size: 32M
[2024-04-17T00:42:12.518+0000][0.036s][info][gc,init] Pre-touch: Enabled
[2024-04-17T00:42:12.518+0000][0.036s][info][gc,init] Available space on backing filesystem: N/A
[2024-04-17T00:42:12.518+0000][0.036s][info][gc,init] Uncommit: Implicitly Disabled (-Xms equals -Xmx)
[2024-04-17T00:42:12.518+0000][0.037s][info][gc,init] GC Workers for Old Generation: 8 (dynamic)
[2024-04-17T00:42:12.523+0000][0.041s][info][gc,init] GC Workers for Young Generation: 8 (dynamic)
[2024-04-17T00:42:36.105+0000][23.623s][info][gc,init] GC Workers Max: 8 (dynamic)
[2024-04-17T00:42:36.107+0000][23.626s][info][gc,init] Runtime Workers: 20
[2024-04-17T00:42:36.113+0000][23.631s][info][gc     ] Using The Z Garbage Collector
[2024-04-17T00:42:36.152+0000][23.670s][info][gc,metaspace] CDS archive(s) mapped at: [0x000078cb23000000-0x000078cb23ca8000-0x000078cb23ca8000), size 13271040, SharedBaseAddress: 0x000078cb23000000, ArchiveRelocationMode: 1.
[2024-04-17T00:42:36.152+0000][23.670s][info][gc,metaspace] Compressed class space mapped at: 0x000078cb24000000-0x000078cb64000000, reserved size: 1073741824
[2024-04-17T00:42:36.152+0000][23.670s][info][gc,metaspace] Narrow klass base: 0x000078cb23000000, Narrow klass shift: 0, Narrow klass range: 0x100000000
[2024-04-17T00:42:37.620+0000][25.139s][info][gc          ] GC(0) Major Collection (Metadata GC Threshold)
[2024-04-17T00:42:37.620+0000][25.139s][info][gc,task     ] GC(0) Using 1 Workers for Young Generation
[2024-04-17T00:42:37.620+0000][25.139s][info][gc,task     ] GC(0) Using 1 Workers for Old Generation
[2024-04-17T00:42:37.621+0000][25.139s][info][gc,phases   ] GC(0) Y: Young Generation
[2024-04-17T00:42:37.621+0000][25.139s][info][gc,phases   ] GC(0) Y: Pause Mark Start (Major) 0.027ms
[2024-04-17T00:42:37.625+0000][25.143s][debug][gc,phases   ] GC(0) Y: Concurrent Mark Roots 3.985ms
[2024-04-17T00:42:37.722+0000][25.240s][debug][gc,phases   ] GC(0) Y: Concurrent Mark Follow 97.035ms
[2024-04-17T00:42:37.722+0000][25.240s][info ][gc,phases   ] GC(0) Y: Concurrent Mark 101.079ms
[2024-04-17T00:42:37.722+0000][25.241s][info ][gc,phases   ] GC(0) Y: Pause Mark End 0.012ms
[2024-04-17T00:42:37.722+0000][25.241s][info ][gc,phases   ] GC(0) Y: Concurrent Mark Free 0.001ms
[2024-04-17T00:42:37.722+0000][25.241s][info ][gc,phases   ] GC(0) Y: Concurrent Reset Relocation Set 0.000ms
[2024-04-17T00:42:37.728+0000][25.246s][info ][gc,reloc    ] GC(0) Y: Using tenuring threshold: 1 (Computed)
[2024-04-17T00:42:37.729+0000][25.247s][info ][gc,phases   ] GC(0) Y: Concurrent Select Relocation Set 6.475ms
[2024-04-17T00:42:37.729+0000][25.247s][info ][gc,phases   ] GC(0) Y: Pause Relocate Start 0.008ms
[2024-04-17T00:42:37.744+0000][25.263s][debug][gc,phases   ] GC(0) Y: Concurrent Relocate Remset FP 0.034ms
[2024-04-17T00:42:37.744+0000][25.263s][info ][gc,phases   ] GC(0) Y: Concurrent Relocate 15.270ms
[2024-04-17T00:42:37.744+0000][25.263s][info ][gc,alloc    ] GC(0) Y:                         Mark Start        Mark End      Relocate Start    Relocate End
[2024-04-17T00:42:37.744+0000][25.263s][info ][gc,alloc    ] GC(0) Y: Allocation Stalls:          0                0                0                0
[2024-04-17T00:42:37.744+0000][25.263s][info ][gc,load     ] GC(0) Y: Load: 12.70 (40%) / 29.18 (91%) / 34.98 (109%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,mmu      ] GC(0) Y: MMU: 2ms/98.6%, 5ms/99.5%, 10ms/99.7%, 20ms/99.9%, 50ms/99.9%, 100ms/100.0%
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,marking  ] GC(0) Y: Mark: 1 stripe(s), 2 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,marking  ] GC(0) Y: Mark Stack Usage: 32M
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,nmethod  ] GC(0) Y: NMethods: 3467 registered, 0 unregistered
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,metaspace] GC(0) Y: Metaspace: 22M used, 23M committed, 1088M reserved
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,reloc    ] GC(0) Y:                        Candidates     Selected     In-Place         Size        Empty    Relocated
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,reloc    ] GC(0) Y: Small Pages:                   82           40            0         164M           0M           8M
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,reloc    ] GC(0) Y: Medium Pages:                   1            0            0          32M           0M           0M
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,reloc    ] GC(0) Y: Large Pages:                    0            0            0           0M           0M           0M
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,reloc    ] GC(0) Y: Forwarding Usage: 4M
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,reloc    ] GC(0) Y: Age Table:
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,reloc    ] GC(0) Y:                    Live             Garbage             Small              Medium             Large
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,reloc    ] GC(0) Y: Eden              88M (0%)          107M (0%)          82 / 40             1 / 0              0 / 0
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y: Min Capacity: 177152M(100%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y: Max Capacity: 177152M(100%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y: Soft Max Capacity: 177152M(100%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y: Heap Statistics:
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y:                Mark Start          Mark End        Relocate Start      Relocate End           High               Low
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y:  Capacity:   177152M (100%)     177152M (100%)     177152M (100%)     177152M (100%)     177152M (100%)     177152M (100%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y:      Free:   176956M (100%)     176938M (100%)     176938M (100%)     177000M (100%)     177004M (100%)     176928M (100%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y:      Used:      196M (0%)          214M (0%)          214M (0%)          152M (0%)          224M (0%)          148M (0%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y: Young Generation Statistics:
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y:                Mark Start          Mark End        Relocate Start      Relocate End
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y:      Used:      196M (0%)          214M (0%)          214M (0%)          152M (0%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y:      Live:         -                88M (0%)           88M (0%)           88M (0%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y:   Garbage:         -               107M (0%)          107M (0%)           24M (0%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y: Allocated:         -                18M (0%)           18M (0%)           38M (0%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y: Reclaimed:         -                  -                 0M (0%)           82M (0%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y:  Promoted:         -                  -                 0M (0%)            0M (0%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,heap     ] GC(0) Y: Compacted:         -                  -                  -                11M (0%)
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,phases   ] GC(0) Y: Young Generation 196M(0%)->152M(0%) 0.124s
[2024-04-17T00:42:37.745+0000][25.263s][info ][gc,phases   ] GC(0) O: Old Generation
[2024-04-17T00:42:37.745+0000][25.264s][debug][gc,phases   ] GC(0) O: Concurrent Mark Roots 0.361ms
[2024-04-17T00:42:37.746+0000][25.264s][debug][gc,phases   ] GC(0) O: Concurrent Mark Follow 0.283ms
[2024-04-17T00:42:37.746+0000][25.264s][info ][gc,phases   ] GC(0) O: Concurrent Mark 0.654ms
[2024-04-17T00:42:37.746+0000][25.264s][info ][gc,phases   ] GC(0) O: Pause Mark End 0.012ms
[2024-04-17T00:42:37.746+0000][25.264s][info ][gc,phases   ] GC(0) O: Concurrent Mark Free 0.010ms
[2024-04-17T00:42:37.746+0000][25.264s][debug][gc,phases   ] GC(0) O: Concurrent References Process 0.019ms
[2024-04-17T00:42:37.748+0000][25.266s][debug][gc,phases   ] GC(0) O: ClassLoaderData 0.006ms
[2024-04-17T00:42:37.748+0000][25.266s][debug][gc,phases   ] GC(0) O: Trigger cleanups 0.000ms
[2024-04-17T00:42:37.754+0000][25.272s][debug][gc,phases   ] GC(0) O: Concurrent Classes Unlink 6.076ms
[2024-04-17T00:42:37.754+0000][25.273s][debug][gc,phases   ] GC(0) O: Concurrent Classes Purge 0.413ms
[2024-04-17T00:42:37.754+0000][25.273s][debug][gc,phases   ] GC(0) O: Concurrent References Enqueue 0.000ms
[2024-04-17T00:42:37.754+0000][25.273s][info ][gc,phases   ] GC(0) O: Concurrent Process Non-Strong 8.647ms
[2024-04-17T00:42:37.754+0000][25.273s][info ][gc,phases   ] GC(0) O: Concurrent Reset Relocation Set 0.000ms
[2024-04-17T00:42:37.757+0000][25.275s][info ][gc,phases   ] GC(0) O: Concurrent Select Relocation Set 2.354ms
[2024-04-17T00:42:37.757+0000][25.275s][info ][gc,task     ] GC(0) O: Using 2 Workers for Old Generation
[2024-04-17T00:42:37.768+0000][25.286s][info ][gc,task     ] GC(0) O: Using 1 Workers for Old Generation
[2024-04-17T00:42:37.768+0000][25.286s][info ][gc,phases   ] GC(0) O: Concurrent Remap Roots 11.203ms
[2024-04-17T00:42:37.768+0000][25.286s][info ][gc,phases   ] GC(0) O: Pause Relocate Start 0.010ms
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,phases   ] GC(0) O: Concurrent Relocate 0.088ms
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,alloc    ] GC(0) O:                         Mark Start        Mark End      Relocate Start    Relocate End
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,alloc    ] GC(0) O: Allocation Stalls:          0                0                0                0
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,load     ] GC(0) O: Load: 12.70 (40%) / 29.18 (91%) / 34.98 (109%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,mmu      ] GC(0) O: MMU: 2ms/98.6%, 5ms/99.5%, 10ms/99.7%, 20ms/99.9%, 50ms/99.9%, 100ms/100.0%
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,marking  ] GC(0) O: Mark: 1 stripe(s), 1 proactive flush(es), 1 terminate flush(es), 0 completion(s), 0 continuation(s)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,marking  ] GC(0) O: Mark Stack Usage: 0M
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,nmethod  ] GC(0) O: NMethods: 3068 registered, 412 unregistered
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,metaspace] GC(0) O: Metaspace: 23M used, 23M committed, 1088M reserved
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,ref      ] GC(0) O:                       Encountered   Discovered     Enqueued
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,ref      ] GC(0) O: Soft References:             4151            0            0
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,ref      ] GC(0) O: Weak References:             2271            0            0
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,ref      ] GC(0) O: Final References:               0            0            0
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,ref      ] GC(0) O: Phantom References:          1029            0            0
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O: Min Capacity: 177152M(100%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O: Max Capacity: 177152M(100%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O: Soft Max Capacity: 177152M(100%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O: Heap Statistics:
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O:                Mark Start          Mark End        Relocate Start      Relocate End           High               Low
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O:  Capacity:   177152M (100%)     177152M (100%)     177152M (100%)     177152M (100%)     177152M (100%)     177152M (100%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O:      Free:   176956M (100%)     177000M (100%)     176996M (100%)     176996M (100%)     177004M (100%)     176928M (100%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O:      Used:      196M (0%)          152M (0%)          156M (0%)          156M (0%)          224M (0%)          148M (0%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O: Old Generation Statistics:
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O:                Mark Start          Mark End        Relocate Start      Relocate End
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O:      Used:        0M (0%)            0M (0%)            0M (0%)            0M (0%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O:      Live:         -                 0M (0%)            0M (0%)            0M (0%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O:   Garbage:         -                 0M (0%)            0M (0%)            0M (0%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O: Allocated:         -                 0M (0%)            0M (0%)            0M (0%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O: Reclaimed:         -                  -                 0M (0%)            0M (0%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,heap     ] GC(0) O: Compacted:         -                  -                  -                 0M (0%)
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc,phases   ] GC(0) O: Old Generation 152M(0%)->156M(0%) 0.023s
[2024-04-17T00:42:37.768+0000][25.287s][info ][gc          ] GC(0) Major Collection (Metadata GC Threshold) 196M(0%)->156M(0%) 0.148s
     */
}
