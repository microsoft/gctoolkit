// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.diary;

import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.parser.TestLogFile;
import com.microsoft.gctoolkit.parser.jvm.*;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.fail;

abstract class LogDiaryTest {

    protected JVMConfiguration getJVMConfiguration(GCLogFile gcLogFile) throws IOException {

        final JVMConfiguration jvmConfiguration = gcLogFile.isUnifiedFormat()
                ? new UnifiedJVMConfiguration()
                : new PreUnifiedJVMConfiguration();

        gcLogFile.stream().
                filter(Objects::nonNull).
                map(String::trim).
                filter(s -> s.length() > 0).
                map(jvmConfiguration::diarize).
                filter(completed -> completed).
                findFirst();

        jvmConfiguration.fillInKnowns();
        return jvmConfiguration;
    }

    /**
     * Convenience method to avoid changing test that rely on the other form of this method signature.
     *
     * @param name test log file name
     * @param expectedDiaryResults expected diary results
     * @param expectedDetailsUnKnown expected unknown details
     * @param expectedDetailsKnown expected known details
     */
    void testWith(String name, boolean[] expectedDiaryResults, int[] expectedDetailsUnKnown, int[] expectedDetailsKnown) {
        testWith(new TestLogFile(name).getFile(), name, expectedDiaryResults, expectedDetailsUnKnown, expectedDetailsKnown);
    }

    void testWith(File file, String name, boolean[] expectedDiaryResults, int[] expectedDetailsUnKnown, int[] expectedDetailsKnown) {
        JVMConfiguration jvmConfiguration = null;
        try {
            GCLogFile gcLogFile = new SingleGCLogFile(file.toPath());
            jvmConfiguration = getJVMConfiguration(gcLogFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        interrogateDiary(jvmConfiguration, name, expectedDiaryResults);
        lookForUnknowns(jvmConfiguration, name, expectedDetailsUnKnown);
        lookForKnowns(jvmConfiguration, name, expectedDetailsKnown);
    }

    void performCheck(String name, boolean calculated, boolean expected) {
        org.junit.jupiter.api.Assertions.assertTrue(calculated == expected, name + ", calculated: " + calculated + ", expected: " + expected);
    }

    /**
     * What the deriveConfiguration should look like. Unknown == false so there is a separate test to determine
     * if something is truly false when it should be false
     */
    void interrogateDiary(JVMConfiguration jvmConfiguration, String name, boolean[] expected) {
        LoggingDiary diary = jvmConfiguration.getDiary();
        performCheck(name, diary.isApplicationStoppedTime(), expected[SupportedFlags.APPLICATION_STOPPED_TIME.ordinal()]);
        performCheck(name, diary.isApplicationRunningTime(), expected[SupportedFlags.APPLICATION_CONCURRENT_TIME.ordinal()]);
        performCheck(name, diary.isDefNew(), expected[SupportedFlags.DEFNEW.ordinal()]);
        performCheck(name, diary.isParNew(), expected[SupportedFlags.PARNEW.ordinal()]);
        performCheck(name, diary.isCMS(), expected[SupportedFlags.CMS.ordinal()]);
        performCheck(name, diary.isICMS(), expected[SupportedFlags.ICMS.ordinal()]);
        performCheck(name, diary.isPSYoung(), expected[SupportedFlags.PARALLELGC.ordinal()]);
        performCheck(name, diary.isPSOldGen(), expected[SupportedFlags.PARALLELOLDGC.ordinal()]);
        performCheck(name, diary.isSerialFull(), expected[SupportedFlags.SERIAL.ordinal()]);
        performCheck(name, diary.isG1GC(), expected[SupportedFlags.G1GC.ordinal()]);
        performCheck(name, diary.isZGC(), expected[SupportedFlags.ZGC.ordinal()]);
        performCheck(name, diary.isShenandoah(), expected[SupportedFlags.SHENANDOAH.ordinal()]);
        performCheck(name, diary.isPrintGCDetails(), expected[SupportedFlags.GC_DETAILS.ordinal()]);
        performCheck(name, diary.isTenuringDistribution(), expected[SupportedFlags.TENURING_DISTRIBUTION.ordinal()]);
        performCheck(name, diary.isGCCause(), expected[SupportedFlags.GC_CAUSE.ordinal()]);
        performCheck(name, diary.isCMSDebugLevel1(), expected[SupportedFlags.CMS_DEBUG_LEVEL_1.ordinal()]);
        performCheck(name, diary.isAdaptiveSizing(), expected[SupportedFlags.ADAPTIVE_SIZING.ordinal()]);
        performCheck(name, diary.isJDK70(), expected[SupportedFlags.JDK70.ordinal()]);
        performCheck(name, diary.isPre70_40(), expected[SupportedFlags.PRE_JDK70_40.ordinal()]);
        performCheck(name, diary.isJDK80(), expected[SupportedFlags.JDK80.ordinal()]);
        performCheck(name, diary.isUnifiedLogging(), expected[SupportedFlags.UNIFIED_LOGGING.ordinal()]);
        performCheck(name, diary.isPrintHeapAtGC(), expected[SupportedFlags.PRINT_HEAP_AT_GC.ordinal()]);
        performCheck(name, diary.isRSetStats(), expected[SupportedFlags.RSET_STATS.ordinal()]);
        performCheck(name, diary.hasPrintReferenceGC(), expected[SupportedFlags.PRINT_REFERENCE_GC.ordinal()]);
        performCheck(name, diary.isMaxTenuringThresholdViolation(), expected[SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION.ordinal()]);
    }

    /**
     * Things we shouldn't know
     */
    void lookForUnknowns(JVMConfiguration jvmConfiguration, String name, int[] unknowns) {
        LoggingDiary diary = jvmConfiguration.getDiary();
        for (int unknown : unknowns) {
            if (unknown != -1)
                switch (unknown) {
                    case 0 -> assertTrue(name + ": " + diary.isApplicationStoppedTime(), !diary.isApplicationStoppedTimeKnown());
                    case 1 -> assertTrue(name + ": " + diary.isApplicationRunningTime(), !diary.isApplicationRunningTimeKnown());
                    case 2 -> assertTrue(name + ": " + diary.isDefNew(), !diary.isDefNewKnown());
                    case 3 -> assertTrue(name + ": " + diary.isParNew(), !diary.isParNewKnown());
                    case 4 -> assertTrue(name + ": " + diary.isCMS(), !diary.isCMSKnown());
                    case 5 -> assertTrue(name + ": " + diary.isICMS(), !diary.isICMSKnown());
                    case 6 -> assertTrue(name + ": " + diary.isPSYoung(), !diary.isPSYoungKnown());
                    case 7 -> assertTrue(name + ": " + diary.isPSOldGen(), !diary.isPSOldGenKnown());
                    case 8 -> assertTrue(name + ": " + diary.isSerialFull(), !diary.isSerialFullKnown());
                    case 9 -> assertTrue(name + ": " + diary.isG1GC(), !diary.isG1GCKnown());
                    case 10 -> assertTrue(name + ": " + diary.isZGC(), !diary.isZGCKnown());
                    case 11 -> assertTrue(name + ": " + diary.isShenandoah(), !diary.isShenandoahKnown());
                    case 12 -> assertTrue(name + ": " + diary.isPrintGCDetails(), !diary.isPrintGCDetailsKnown());
                    case 13 -> assertTrue(name + ": " + diary.isTenuringDistribution(), !diary.isTenuringDistributionKnown());
                    case 14 -> assertTrue(name + ": " + diary.isGCCause(), !diary.isGCCauseKnown());
                    case 15 -> assertTrue(name + ": " + diary.isCMSDebugLevel1(), !diary.isCMSDebugLevel1Known());
                    case 16 -> assertTrue(name + ": " + diary.isAdaptiveSizing(), !diary.isAdaptiveSizingKnown());
                    case 17 -> assertTrue(name + ": " + diary.isJDK70(), !diary.isJDK70Known());
                    case 18 -> assertTrue(name + ": " + diary.isPre70_40(), !diary.isPre70_45Known());
                    case 19 -> assertTrue(name + ": " + diary.isJDK80(), !diary.isJDK80Known());
                    case 20 -> assertTrue(name + ": " + diary.isUnifiedLogging(), !diary.isUnifiedLoggingKnown());
                    case 21 -> assertTrue(name + ": " + diary.isPrintHeapAtGC(), !diary.isPrintHeapAtGCKnown());
                    case 22 -> assertTrue(name + ": " + diary.isRSetStats(), !diary.isRSetStatsKnown());
                    case 23 -> assertTrue(name + ": " + diary.hasPrintReferenceGC(), !diary.isPrintReferenceGCKnown());
                    case 24 -> assertTrue(name + ": " + diary.isMaxTenuringThresholdViolation(), !diary.isMaxTenuringThresholdViolationKnown());
                    case 25 -> assertTrue(name + ": ", !diary.isTLABDataKnown());
                    case 26 -> assertTrue(name + ": ", !diary.isPrintPromotionFailureKnown());
                    case 27 -> assertTrue(name + ": ", !diary.isPrintFLSStatisticsKnown());
                    default -> fail("unknown unknown");
                }
        }
    }

    /**
     * Things we should know. Since unknown == false this is primarily to test for things that should be false.
     * IOWs, if the primary test passes, this is a secondary to cover cases where the value should be false and
     * not unknown.
     */
    void lookForKnowns(JVMConfiguration jvmConfiguration, String name, int[] knowns) {
        LoggingDiary diary = jvmConfiguration.getDiary();
        for (int known : knowns) {
            if (known != -1)
                switch (known) {
                    case 0 -> assertTrue(name + ":ApplicationStoppedTimeKnown should be known", diary.isApplicationStoppedTimeKnown());
                    case 1 -> assertTrue(name + ":ApplicationRunningTimeKnown should be known", diary.isApplicationRunningTimeKnown());
                    case 2 -> assertTrue(name + ":DefNewKnown should be known", diary.isDefNewKnown());
                    case 3 -> assertTrue(name + ":ParNewKnown should be known", diary.isParNewKnown());
                    case 4 -> assertTrue(name + ":CMSKnown should be known", diary.isCMSKnown());
                    case 5 -> assertTrue(name + ":ICMSKnown should be known", diary.isICMSKnown());
                    case 6 -> assertTrue(name + ":PSYoungKnown should be known", diary.isPSYoungKnown());
                    case 7 -> assertTrue(name + ":PSOldGenKnown should be known", diary.isPSOldGenKnown());
                    case 8 -> assertTrue(name + ":SerialFullKnown should be known", diary.isSerialFullKnown());
                    case 9 -> assertTrue(name + ":G1GCKnown should be known", diary.isG1GCKnown());
                    case 10 -> assertTrue(name + ":PrintGCDetailsKnown should be known", diary.isZGCKnown());
                    case 11 -> assertTrue(name + ":PrintGCDetailsKnown should be known", diary.isShenandoahKnown());
                    case 12 -> assertTrue(name + ":PrintGCDetailsKnown should be known", diary.isPrintGCDetailsKnown());
                    case 13 -> assertTrue(name + ":TenuringDistributionKnown should be known", diary.isTenuringDistributionKnown());
                    case 14 -> assertTrue(name + ":GCCauseKnown should be known", diary.isGCCauseKnown());
                    case 15 -> assertTrue(name + ":CMSDebugLevel1Known should be known", diary.isCMSDebugLevel1Known());
                    case 16 -> assertTrue(name + ":AdaptiveSizingKnown should be known", diary.isAdaptiveSizingKnown());
                    case 17 -> assertTrue(name + ":JDK70Known should be known", diary.isJDK70Known());
                    case 18 -> assertTrue(name + ":Pre70_45Known should be known", diary.isPre70_45Known());
                    case 19 -> assertTrue(name + ":JDK80Known should be known", diary.isJDK80Known());
                    case 20 -> assertTrue(name + ":JDK90Known should be known", diary.isUnifiedLoggingKnown());
                    case 21 -> assertTrue(name + ":PrintHeapAtGCKnown should be known", diary.isPrintHeapAtGCKnown());
                    case 22 -> assertTrue(name + ":RSetStatsKnown should be known", diary.isRSetStatsKnown());
                    case 23 -> assertTrue(name + ":PrintReferenceGCKnown should be known", diary.isPrintReferenceGCKnown());
                    case 24 -> assertTrue(name + ":MaxTenuringThresholdViolationKnown should be known", diary.isMaxTenuringThresholdViolationKnown());
                    case 25 -> assertTrue(name + ":TLAB_DATA should be known", diary.isTLABDataKnown());
                    case 26 -> assertTrue(name + ":PRINT_PROMOTION_FAILURE should be known", diary.isPrintPromotionFailureKnown());
                    case 27 -> assertTrue(name + ":PRINT_FLS_STATISTICS should be known", diary.isPrintFLSStatisticsKnown());
                    default -> fail("unknown unknown");
                }
        }
    }

    private void assertTrue(String s, boolean b) {
        org.junit.jupiter.api.Assertions.assertTrue(b, s);
    }


}
