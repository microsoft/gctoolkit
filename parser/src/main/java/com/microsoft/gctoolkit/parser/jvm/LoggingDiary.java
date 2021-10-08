// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.jvm;

import com.microsoft.gctoolkit.parser.datatype.TripleState;

import java.util.Arrays;

/*
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
    PRINT_FLS_STATISTICS                        // 27
 */

public class LoggingDiary {

    private final TripleState[] states;

    public LoggingDiary() {
        states = new TripleState[SupportedFlags.values().length];
        for (int i = 0; i < states.length; i++) states[i] = TripleState.UNKNOWN;
    }

    public void setTrue(SupportedFlags flag) {
        if (states[flag.ordinal()] == TripleState.UNKNOWN) states[flag.ordinal()] = TripleState.TRUE;
    }

    public void setTrue(SupportedFlags... flags) {
        for (SupportedFlags flag : flags) {
            setTrue(flag);
        }
    }

    public void setFalse(SupportedFlags flag) {
        if (states[flag.ordinal()] == TripleState.UNKNOWN) states[flag.ordinal()] = TripleState.FALSE;
    }

    public void setFalse(SupportedFlags... flags) {
        for (SupportedFlags flag : flags) {
            setFalse(flag);
        }
    }

    public boolean isStateKnown(SupportedFlags flag) {
        return states[flag.ordinal()].isKnown();
    }

    public boolean isStateKnown(SupportedFlags... flags) {
        boolean value = true;
        for (SupportedFlags flag : flags) {
            value &= states[flag.ordinal()].isTrue();
        }
        return value;
    }

    public void setState(SupportedFlags flag, boolean flagTurnedOn) {
        if ((flagTurnedOn)) {
            setTrue(flag);
        } else {
            setFalse(flag);
        }
    }

    public boolean isTrue(SupportedFlags flag) {
        return (isStateKnown(flag)) && states[flag.ordinal()].isTrue();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("LoggingDiary{");
        boolean first = true;
        for(SupportedFlags flag : SupportedFlags.values()) {
            if (!first || (first = false)) {
                buffer.append(", ");
            }
            buffer.append(flag.name()).append("=").append(states[flag.ordinal()]);
        }
        return buffer.append("}").toString();
    }

    public boolean isComplete() {
        return Arrays.stream(states).allMatch(TripleState::isKnown);
    }

    public boolean isCollectorKnown() {
        return isGenerationalKnown() || isG1GCKnown() || isZGCKnown() || isShenandoahKnown();
    }

    public boolean isVersionKnown() {
        return isUnifiedLogging() || isJDK80() || (isJDK70() && isPre70_40Known());

    }

    public boolean isDetailsKnown() {
        return isApplicationStoppedTimeKnown() && isPrintReferenceGCKnown() && isPrintGCDetailsKnown() &&
                isAdaptiveSizingKnown() && isTLABDataKnown() && isPrintPromotionFailureKnown() &&
                isPrintFLSStatisticsKnown() && isPrintHeapAtGCKnown();
    }

    public boolean isDefNew() {
        return isTrue(SupportedFlags.DEFNEW);
    }

    public boolean isSerialFull() {
        return isTrue(SupportedFlags.SERIAL);
    }

    public boolean isParNew() {
        return isTrue(SupportedFlags.PARNEW);
    }

    public boolean isCMS() {
        return isTrue(SupportedFlags.CMS);
    }

    public boolean isICMS() {
        return isTrue(SupportedFlags.ICMS);
    }

    public boolean isPSYoung() {
        return isTrue(SupportedFlags.PARALLELGC);
    }

    public boolean isPSOldGen() {
        return isTrue(SupportedFlags.PARALLELOLDGC);
    }

    public boolean isG1GC() {
        return isTrue(SupportedFlags.G1GC);
    }

    public boolean isZGC() {
        return isTrue(SupportedFlags.ZGC);
    }

    public boolean isShenandoah() {
        return isTrue(SupportedFlags.SHENANDOAH);
    }

    public boolean isGenerational() {
        return isCollectorKnown() && !(isG1GC() || isZGC() || isShenandoah());
    }

    public boolean isPrintGCDetails() {
        return isTrue(SupportedFlags.GC_DETAILS);
    }

    public boolean isTenuringDistribution() {
        return isTrue(SupportedFlags.TENURING_DISTRIBUTION);
    }

    public boolean isGCCause() {
        return isTrue(SupportedFlags.GC_CAUSE);
    }

    public boolean isAdaptiveSizing() {
        return isTrue(SupportedFlags.ADAPTIVE_SIZING);
    }

    public boolean isCMSDebugLevel1() {
        return isTrue(SupportedFlags.CMS_DEBUG_LEVEL_1);
    }

    public boolean isApplicationStoppedTime() {
        return isTrue(SupportedFlags.APPLICATION_STOPPED_TIME);
    }

    public boolean isApplicationRunningTime() {
        return isTrue(SupportedFlags.APPLICATION_CONCURRENT_TIME);
    }

    public boolean isTLABData() {
        return isTrue(SupportedFlags.TLAB_DATA);
    }

    public boolean isTLABDataKnown() {
        return isStateKnown(SupportedFlags.TLAB_DATA);
    }

    public boolean isJDK70() {
        return isTrue(SupportedFlags.JDK70);
    }

    public boolean isPre70_40() {
        return isTrue(SupportedFlags.PRE_JDK70_40);
    } // GCCause with perm is 7.0, (System) is pre _40, System.gc() is _40+

    public boolean isPre70_40Known() {
        return isStateKnown(SupportedFlags.PRE_JDK70_40);
    }

    public boolean isJDK80() {
        return isTrue(SupportedFlags.JDK80);
    } //look for metaspace record...

    public boolean isUnifiedLogging() {
        return isTrue(SupportedFlags.UNIFIED_LOGGING);
    }

    public boolean isPrintHeapAtGC() {
        return isTrue(SupportedFlags.PRINT_HEAP_AT_GC);
    }

    public boolean isRSetStats() {
        return isTrue(SupportedFlags.RSET_STATS);
    }

    public boolean hasPrintReferenceGC() {
        return isTrue(SupportedFlags.PRINT_REFERENCE_GC);
    }

    public boolean isMaxTenuringThresholdViolation() {
        return isTrue(SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
    }

    public boolean isDefNewKnown() {
        return isStateKnown(SupportedFlags.DEFNEW);
    }

    public boolean isSerialFullKnown() {
        return isStateKnown(SupportedFlags.SERIAL);
    }

    public boolean isParNewKnown() {
        return isStateKnown(SupportedFlags.PARNEW);
    }

    public boolean isCMSKnown() {
        return isStateKnown(SupportedFlags.CMS);
    }

    public boolean isICMSKnown() {
        return isStateKnown(SupportedFlags.ICMS);
    }

    public boolean isPSYoungKnown() {
        return isStateKnown(SupportedFlags.PARALLELGC);
    }

    public boolean isPSOldGenKnown() {
        return isStateKnown(SupportedFlags.PARALLELOLDGC);
    }

    public boolean isG1GCKnown() {
        return isStateKnown(SupportedFlags.G1GC);
    }

    public boolean isZGCKnown() {
        return isStateKnown(SupportedFlags.ZGC);
    }

    public boolean isShenandoahKnown() {
        return isStateKnown(SupportedFlags.SHENANDOAH);
    }

    public boolean isGenerationalKnown() {
        return isStateKnown(SupportedFlags.G1GC);
    }

    public boolean isPrintGCDetailsKnown() {
        return isStateKnown(SupportedFlags.GC_DETAILS);
    }

    public boolean isTenuringDistributionKnown() {
        return isStateKnown(SupportedFlags.TENURING_DISTRIBUTION);
    }

    public boolean isGCCauseKnown() {
        return isStateKnown(SupportedFlags.GC_CAUSE);
    }

    public boolean isAdaptiveSizingKnown() {
        return isStateKnown(SupportedFlags.ADAPTIVE_SIZING);
    }

    public boolean isCMSDebugLevel1Known() {
        return isStateKnown(SupportedFlags.CMS_DEBUG_LEVEL_1);
    }

    public boolean isApplicationStoppedTimeKnown() {
        return isStateKnown(SupportedFlags.APPLICATION_STOPPED_TIME);
    }

    public boolean isApplicationRunningTimeKnown() {
        return isStateKnown(SupportedFlags.APPLICATION_CONCURRENT_TIME);
    }

    public boolean isJDK70Known() {
        return isStateKnown(SupportedFlags.JDK70);
    }

    public boolean isPre70_45Known() {
        return isStateKnown(SupportedFlags.PRE_JDK70_40);
    } // GCCause with perm is 7.0, (System) is pre _45, System.gc() is _45+

    public boolean isJDK80Known() {
        return isStateKnown(SupportedFlags.JDK80);
    } //look for metaspace record...

    public boolean isUnifiedLoggingKnown() {
        return isStateKnown(SupportedFlags.UNIFIED_LOGGING);
    } //Unsure how to know this for sure.

    public boolean isPrintHeapAtGCKnown() {
        return isStateKnown(SupportedFlags.PRINT_HEAP_AT_GC);
    }

    public boolean isRSetStatsKnown() {
        return isStateKnown(SupportedFlags.RSET_STATS);
    }

    public boolean isPrintReferenceGCKnown() {
        return isStateKnown(SupportedFlags.PRINT_REFERENCE_GC);
    }

    public boolean isPrintPromotionFailure() {
        return isTrue(SupportedFlags.PRINT_PROMOTION_FAILURE);
    }

    public boolean isPrintPromotionFailureKnown() {
        return isStateKnown(SupportedFlags.PRINT_PROMOTION_FAILURE);
    }

    public boolean isPrintFLSStatistics() {
        return isTrue(SupportedFlags.PRINT_FLS_STATISTICS);
    }

    public boolean isPrintFLSStatisticsKnown() {
        return isStateKnown(SupportedFlags.PRINT_FLS_STATISTICS);
    }

    public boolean isMaxTenuringThresholdViolationKnown() {
        return isStateKnown(SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
    }

    public boolean isJVMEventsKnown() {
        return isApplicationStoppedTimeKnown() && isApplicationRunningTime();
    }
}
