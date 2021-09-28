// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.diary;

import com.microsoft.gctoolkit.parser.jvm.JVMConfiguration;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedJVMConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandLineFlagsLogDiaryTest {

    private static final String[][] gcLogFileHeader = {
            {
                    "Java HotSpot(TM) 64-Bit Server VM (25.40-b25) for bsd-amd64 JRE (1.8.0_40-b25), built on Feb 10 2015 21:07:25 by \"java_re\" with gcc 4.2.1 (Based on Apple Inc. build 5658) (LLVM build 2336.11.00)",
                    "Memory: 4k page, physical 16777216k(920952k free)",
                    "/proc/meminfo:",
                    "CommandLine flags: -XX:InitialHeapSize=268435456 -XX:MaxHeapSize=1073741824 -XX:MaxNewSize=357916672 -XX:MaxTenuringThreshold=6 -XX:OldPLABSize=16 -XX:+PrintAdaptiveSizePolicy -XX:PrintFLSStatistics=2 -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintReferenceGC -XX:+PrintTenuringDistribution -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"

            },
            {
                    "Java HotSpot(TM) 64-Bit Server VM (25.40-b25) for bsd-amd64 JRE (1.8.0_40-b25), built on Feb 10 2015 21:07:25 by \"java_re\" with gcc 4.2.1 (Based on Apple Inc. build 5658) (LLVM build 2336.11.00)",
                    "Memory: 4k page, physical 16777216k(1811760k free)",
                    "/proc/meminfo:",
                    "CommandLine flags: -XX:InitialHeapSize=268435456 -XX:MaxHeapSize=1073741824 -XX:+PrintAdaptiveSizePolicy -XX:+PrintCMSInitiationStatistics -XX:PrintFLSCensus=2 -XX:PrintFLSStatistics=2 -XX:+PrintGC -XX:+PrintGCCause -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintPromotionFailure -XX:+PrintReferenceGC -XX:+PrintTenuringDistribution -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseParallelGC"
            }
    };

    @Test
    public void testForCMSDefNewDetails() {
        JVMConfiguration jvmConfiguration = new PreUnifiedJVMConfiguration();
        jvmConfiguration.diarize(gcLogFileHeader[0][0]);
        jvmConfiguration.diarize(gcLogFileHeader[0][3]);
        LoggingDiary diary = jvmConfiguration.getDiary();
        assertTrue(diary.isJDK80());
        assertTrue(diary.isGCCause());
        assertTrue(diary.isAdaptiveSizing());
        assertTrue(diary.isPrintGCDetails());
        assertTrue(diary.isTenuringDistribution());
        assertTrue(diary.isCMS());
        assertTrue(diary.isParNew());
        assertTrue(diary.isGenerational());
        assertTrue(diary.hasPrintReferenceGC());

        assertTrue(!diary.isDefNew());
        assertTrue(!diary.isSerialFull());
        assertTrue(!diary.isICMS());
        assertTrue(!diary.isPSYoung());
        assertTrue(!diary.isPSOldGen());
        assertTrue(!diary.isG1GC());
        assertTrue(!diary.isZGC());
        assertTrue(!diary.isShenandoah());
        assertTrue(!diary.isCMSDebugLevel1());
        assertTrue(!diary.isApplicationStoppedTime());
        assertTrue(!diary.isApplicationRunningTime());
        assertTrue(!diary.isJDK70());
        assertTrue(!diary.isPre70_40());
        assertTrue(!diary.isPrintHeapAtGC());
        assertTrue(!diary.isRSetStats());
        assertTrue(!diary.isMaxTenuringThresholdViolation());
    }

    @Test
    public void testForParallelDetails() {
        JVMConfiguration jvmConfiguration = new PreUnifiedJVMConfiguration();
        jvmConfiguration.diarize(gcLogFileHeader[1][0]);
        jvmConfiguration.diarize(gcLogFileHeader[1][3]);
        LoggingDiary diary = jvmConfiguration.getDiary();
        assertTrue(diary.isJDK80());
        assertTrue(diary.isGCCause());
        assertTrue(diary.isAdaptiveSizing());
        assertTrue(diary.isPrintGCDetails());
        assertTrue(diary.isTenuringDistribution());
        assertTrue(!diary.isCMS());
        assertTrue(!diary.isParNew());
        assertTrue(diary.isGenerational());
        assertTrue(diary.hasPrintReferenceGC());

        assertTrue(!diary.isDefNew());
        assertTrue(!diary.isSerialFull());
        assertTrue(!diary.isICMS());
        assertTrue(diary.isPSYoung());
        assertTrue(diary.isPSOldGen());
        assertTrue(!diary.isG1GC());
        assertTrue(!diary.isG1GC());
        assertTrue(!diary.isZGC());
        assertTrue(!diary.isCMSDebugLevel1());
        assertTrue(!diary.isApplicationStoppedTime());
        assertTrue(!diary.isApplicationRunningTime());
        assertTrue(!diary.isJDK70());
        assertTrue(!diary.isPre70_40());
        assertTrue(!diary.isPrintHeapAtGC());
        assertTrue(!diary.isRSetStats());
        assertTrue(!diary.isMaxTenuringThresholdViolation());
    }
}
