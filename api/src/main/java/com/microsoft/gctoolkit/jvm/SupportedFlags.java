// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.jvm;

public enum SupportedFlags {
    APPLICATION_STOPPED_TIME,                   //  0
    APPLICATION_CONCURRENT_TIME,                //  1

    DEFNEW,                                     //  2
    PARNEW,                                     //  3
    CMS,                                        //  4
    ICMS,                                       //  5
    PARALLELGC,                                 //  6
    PARALLELOLDGC,                              //  7
    SERIAL,                                     //  8
    G1GC,                                       //  9
    ZGC,                                        // 10
    SHENANDOAH,                                 // 11

    GC_DETAILS,                                 // 12
    TENURING_DISTRIBUTION,                      // 13
    GC_CAUSE,                                   // 14
    CMS_DEBUG_LEVEL_1,                          // 15
    ADAPTIVE_SIZING,                            // 16

    JDK70,                                      // 17
    PRE_JDK70_40,                               // 18
    JDK80,                                      // 19
    UNIFIED_LOGGING,                            // 20

    PRINT_HEAP_AT_GC,                           // 21
    RSET_STATS,                                 // 22

    PRINT_REFERENCE_GC,                         // 23
    MAX_TENURING_THRESHOLD_VIOLATION,           // 24
    TLAB_DATA,                                  // 25
    PRINT_PROMOTION_FAILURE,                    // 26
    PRINT_FLS_STATISTICS,                       // 27
    
    PRINT_CPU_TIMES								// 28

}
