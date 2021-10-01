// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.diary;

import com.microsoft.gctoolkit.parser.TestLogFile;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.parser.jvm.JVMConfiguration;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedJVMConfiguration;
import com.microsoft.gctoolkit.parser.jvm.UnifiedJVMConfiguration;
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
                    case 0:
                        assertTrue(name + ": " + diary.isApplicationStoppedTime(), !diary.isApplicationStoppedTimeKnown());
                        break;
                    case 1:
                        assertTrue(name + ": " + diary.isApplicationRunningTime(), !diary.isApplicationRunningTimeKnown());
                        break;
                    case 2:
                        assertTrue(name + ": " + diary.isDefNew(), !diary.isDefNewKnown());
                        break;
                    case 3:
                        assertTrue(name + ": " + diary.isParNew(), !diary.isParNewKnown());
                        break;
                    case 4:
                        assertTrue(name + ": " + diary.isCMS(), !diary.isCMSKnown());
                        break;
                    case 5:
                        assertTrue(name + ": " + diary.isICMS(), !diary.isICMSKnown());
                        break;
                    case 6:
                        assertTrue(name + ": " + diary.isPSYoung(), !diary.isPSYoungKnown());
                        break;
                    case 7:
                        assertTrue(name + ": " + diary.isPSOldGen(), !diary.isPSOldGenKnown());
                        break;
                    case 8:
                        assertTrue(name + ": " + diary.isSerialFull(), !diary.isSerialFullKnown());
                        break;
                    case 9:
                        assertTrue(name + ": " + diary.isG1GC(), !diary.isG1GCKnown());
                        break;
                    case 10:
                        assertTrue(name + ": " + diary.isZGC(), !diary.isZGCKnown());
                    case 11:
                        assertTrue(name + ": " + diary.isShenandoah(), !diary.isShenandoahKnown());
                    case 12:
                        assertTrue(name + ": " + diary.isPrintGCDetails(), !diary.isPrintGCDetailsKnown());
                        break;
                    case 13:
                        assertTrue(name + ": " + diary.isTenuringDistribution(), !diary.isTenuringDistributionKnown());
                        break;
                    case 14:
                        assertTrue(name + ": " + diary.isGCCause(), !diary.isGCCauseKnown());
                        break;
                    case 15:
                        assertTrue(name + ": " + diary.isCMSDebugLevel1(), !diary.isCMSDebugLevel1Known());
                        break;
                    case 16:
                        assertTrue(name + ": " + diary.isAdaptiveSizing(), !diary.isAdaptiveSizingKnown());
                        break;
                    case 17:
                        assertTrue(name + ": " + diary.isJDK70(), !diary.isJDK70Known());
                        break;
                    case 18:
                        assertTrue(name + ": " + diary.isPre70_40(), !diary.isPre70_45Known());
                        break;
                    case 19:
                        assertTrue(name + ": " + diary.isJDK80(), !diary.isJDK80Known());
                        break;
                    case 20:
                        assertTrue(name + ": " + diary.isUnifiedLogging(), !diary.isUnifiedLoggingKnown());
                        break;
                    case 21:
                        assertTrue(name + ": " + diary.isPrintHeapAtGC(), !diary.isPrintHeapAtGCKnown());
                        break;
                    case 22:
                        assertTrue(name + ": " + diary.isRSetStats(), !diary.isRSetStatsKnown());
                        break;
                    case 23:
                        assertTrue(name + ": " + diary.hasPrintReferenceGC(), !diary.isPrintReferenceGCKnown());
                        break;
                    case 24:
                        assertTrue(name + ": " + diary.isMaxTenuringThresholdViolation(), !diary.isMaxTenuringThresholdViolationKnown());
                        break;
                    case 25:
                        assertTrue(name + ": ", !diary.isTLABDataKnown());
                        break;
                    case 26:
                        assertTrue(name + ": ", !diary.isPrintPromotionFailureKnown());
                        break;
                    case 27:
                        assertTrue(name + ": ", !diary.isPrintFLSStatisticsKnown());
                        break;
                    default:
                        fail("unknown unknown");
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
                    case 0:
                        assertTrue(name + ":ApplicationStoppedTimeKnown should be known", diary.isApplicationStoppedTimeKnown());
                        break;
                    case 1:
                        assertTrue(name + ":ApplicationRunningTimeKnown should be known", diary.isApplicationRunningTimeKnown());
                        break;
                    case 2:
                        assertTrue(name + ":DefNewKnown should be known", diary.isDefNewKnown());
                        break;
                    case 3:
                        assertTrue(name + ":ParNewKnown should be known", diary.isParNewKnown());
                        break;
                    case 4:
                        assertTrue(name + ":CMSKnown should be known", diary.isCMSKnown());
                        break;
                    case 5:
                        assertTrue(name + ":ICMSKnown should be known", diary.isICMSKnown());
                        break;
                    case 6:
                        assertTrue(name + ":PSYoungKnown should be known", diary.isPSYoungKnown());
                        break;
                    case 7:
                        assertTrue(name + ":PSOldGenKnown should be known", diary.isPSOldGenKnown());
                        break;
                    case 8:
                        assertTrue(name + ":SerialFullKnown should be known", diary.isSerialFullKnown());
                        break;
                    case 9:
                        assertTrue(name + ":G1GCKnown should be known", diary.isG1GCKnown());
                        break;
                    case 10:
                        assertTrue(name + ":PrintGCDetailsKnown should be known", diary.isZGCKnown());
                        break;
                    case 11:
                        assertTrue(name + ":PrintGCDetailsKnown should be known", diary.isShenandoahKnown());
                        break;
                    case 12:
                        assertTrue(name + ":PrintGCDetailsKnown should be known", diary.isPrintGCDetailsKnown());
                        break;
                    case 13:
                        assertTrue(name + ":TenuringDistributionKnown should be known", diary.isTenuringDistributionKnown());
                        break;
                    case 14:
                        assertTrue(name + ":GCCauseKnown should be known", diary.isGCCauseKnown());
                        break;
                    case 15:
                        assertTrue(name + ":CMSDebugLevel1Known should be known", diary.isCMSDebugLevel1Known());
                        break;
                    case 16:
                        assertTrue(name + ":AdaptiveSizingKnown should be known", diary.isAdaptiveSizingKnown());
                        break;
                    case 17:
                        assertTrue(name + ":JDK70Known should be known", diary.isJDK70Known());
                        break;
                    case 18:
                        assertTrue(name + ":Pre70_45Known should be known", diary.isPre70_45Known());
                        break;
                    case 19:
                        assertTrue(name + ":JDK80Known should be known", diary.isJDK80Known());
                        break;
                    case 20:
                        assertTrue(name + ":JDK90Known should be known", diary.isUnifiedLoggingKnown());
                        break;
                    case 21:
                        assertTrue(name + ":PrintHeapAtGCKnown should be known", diary.isPrintHeapAtGCKnown());
                        break;
                    case 22:
                        assertTrue(name + ":RSetStatsKnown should be known", diary.isRSetStatsKnown());
                        break;
                    case 23:
                        assertTrue(name + ":PrintReferenceGCKnown should be known", diary.isPrintReferenceGCKnown());
                        break;
                    case 24:
                        assertTrue(name + ":MaxTenuringThresholdViolationKnown should be known", diary.isMaxTenuringThresholdViolationKnown());
                        break;
                    case 25:
                        assertTrue(name + ":TLAB_DATA should be known", diary.isTLABDataKnown());
                        break;
                    case 26:
                        assertTrue(name + ":PRINT_PROMOTION_FAILURE should be known", diary.isPrintPromotionFailureKnown());
                        break;
                    case 27:
                        assertTrue(name + ":PRINT_FLS_STATISTICS should be known", diary.isPrintFLSStatisticsKnown());
                        break;
                    default:
                        fail("unknown unknown");
                }
        }
    }

    private void assertTrue(String s, boolean b) {
        org.junit.jupiter.api.Assertions.assertTrue(b, s);
    }


}
