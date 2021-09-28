// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.jvm;

public enum GarbageCollectorAlgorithm {
    /** -XX:+UseConcMarkSweepGC */
    DEFNEW,                                     //  1
    /** -XX:+UseConcMarkSweepGC -XX:+UseParNew */
    PARNEW,                                     //  2
    /** -XX:+UseConcMarkSweepGC */
    CMS,                                        //  3
    /** -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode */
    ICMS,                                       //  4
    /** -XX:+UseParallelGC */
    PARALLELGC,                                 //  5
    /** -XX:+UseParallelGC -XX:+UseParallelOldGC <br>-XX:+UseParallelOldGC */
    PARALLELOLDGC,                              //  6
    /** -XX:+UseSerialGC */
    SERIAL,                                     //  7
    /** -XX:+UseSerialGC */
    G1GC,                                       //  8
    /** -XX:+UseZGC */
    ZGC,                                        //  9
    /** -XX:+UseShenandoahGC */
    SHENANDOAH                                  // 10
}
