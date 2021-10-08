// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.jvm;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.parser.unified.ShenandoahPatterns;
import com.microsoft.gctoolkit.parser.unified.UnifiedG1GCPatterns;
import com.microsoft.gctoolkit.parser.unified.UnifiedGenerationalPatterns;
import com.microsoft.gctoolkit.parser.unified.UnifiedLoggingLevel;
import com.microsoft.gctoolkit.parser.unified.ZGCPatterns;

import java.util.TreeSet;
import java.util.logging.Logger;

import static com.microsoft.gctoolkit.parser.jvm.SupportedFlags.*;

public class UnifiedJVMConfiguration implements ShenandoahPatterns, ZGCPatterns, UnifiedG1GCPatterns, UnifiedGenerationalPatterns, JVMConfiguration {

    private static final Logger LOGGER = Logger.getLogger(UnifiedJVMConfiguration.class.getName());

    private static final int CYCLES_TO_EXAMINE_BEFORE_GIVING_UP = 10;
    private static final int CYCLES_TO_EXAMINE_FOR_SAFEPOINT = 2;

    private int lineCount = MAXIMUM_LINES_TO_EXAMINE;

    private final LoggingDiary diary;
    private final TreeSet<String> tagsAndLevels = new TreeSet<>();
    private DateTimeStamp startTime = null;
    private int stopTheWorldEvents = 0;

    {
        diary = new LoggingDiary();
        diary.setTrue(UNIFIED_LOGGING);
        diary.setFalse(ICMS, PRE_JDK70_40, JDK70, PRE_JDK70_40, JDK80, MAX_TENURING_THRESHOLD_VIOLATION);

    }

    public LoggingDiary getDiary() {
        return diary;
    }

    //todo: @Override
    public String getCommandLine() {
        // TODO
        return "";
    }

    @Override
    public int getMaxTenuringThreshold() {
        return 0;
    }

    @Override
    public boolean hasJVMEvents() {
        return getDiary().isApplicationStoppedTime() ||
                getDiary().isApplicationRunningTime() ||
                getDiary().isTLABData();
    }

    @Override
    public void fillInKnowns() {
        getDiary().setFalse(ADAPTIVE_SIZING);
    }

    @Override
    public boolean completed() {
        return getDiary().isComplete() || lineCount < 1;
    }

    @Override
    public boolean diarize(String line) {
        if (!line.startsWith("["))
            return false;
        lineCount--;
        extractDecorators(line);
        if (!getDiary().isCollectorKnown())
            discoverCollector(line);
        if (!getDiary().isDetailsKnown())
            discoverDetails(line);
        if (!getDiary().isJVMEventsKnown())
            discoverJVMEvents(line);
        if ((CPU_BREAKOUT.parse(line) != null) || line.contains("gc,start"))
            stopTheWorldEvents++;
        return this.completed();
    }

    /**
     * Extract decorators (from a GC log line tag) and set the corresponding diary flags accordingly
     *
     * @param line GC log line
     */
    private void extractDecorators(String line) {
        Decorators decorators = new Decorators(line);
        timeOfFirstEvent(decorators);
        extractTagsAndLevels(decorators);
        // -Xlog:gc*,gc+ref=debug,gc+phases=debug,gc+age=trace,safepoint
        if (decorators.getLogLevel().isPresent()) {
            UnifiedLoggingLevel logLevel = decorators.getLogLevel().get();
            if (decorators.tagsContain("gc,age"))
                getDiary().setTrue(TENURING_DISTRIBUTION);
            else if (decorators.tagsContain("ref") && logLevel.isGreaterThanOrEqualTo(UnifiedLoggingLevel.debug))
                getDiary().setTrue(PRINT_REFERENCE_GC);
            else if (decorators.tagsContain("gc,phases") && logLevel.isGreaterThanOrEqualTo(UnifiedLoggingLevel.debug))
                getDiary().setTrue(GC_DETAILS);
            else if ( decorators.tagsContain("gc,ergo"))
                getDiary().setTrue(ADAPTIVE_SIZING);
            if (decorators.tagsContain("safepoint"))
                getDiary().setTrue(APPLICATION_STOPPED_TIME, APPLICATION_CONCURRENT_TIME);

            if (getDiary().isZGC()) {
                if (decorators.tagsContain("task"))
                    getDiary().setTrue(GC_DETAILS);
                else if (decorators.tagsContain("heap"))
                    getDiary().setTrue(PRINT_HEAP_AT_GC);
                else if (decorators.tagsContain("tlab"))
                    getDiary().setTrue(TLAB_DATA);
                else if (decorators.tagsContain("gc,start") && line.contains("Garbage Collection ("))
                    getDiary().setTrue(SupportedFlags.GC_CAUSE);
                else if (decorators.tagsContain("gc,heap")) {
                    if (line.contains("Heap before GC"))
                        getDiary().setTrue(PRINT_HEAP_AT_GC);
                    getDiary().setTrue(GC_DETAILS);
                } else if (decorators.tagsContain("gc,ref"))
                    getDiary().setTrue(PRINT_REFERENCE_GC);
                else if (decorators.tagsContain("gc,heap") && decorators.getLogLevel().get() == UnifiedLoggingLevel.debug)
                    getDiary().setTrue(PRINT_HEAP_AT_GC);
            } else if (getDiary().isShenandoah()) {
                if (decorators.tagsContain("gc,task") || decorators.tagsContain("gc,start"))
                    getDiary().setTrue(GC_DETAILS);
                else if (decorators.tagsContain("gc,ergo"))
                    getDiary().setTrue(ADAPTIVE_SIZING);
                else if (decorators.tagsContain("gc") && line.contains("Trigger"))
                    getDiary().setTrue(SupportedFlags.GC_CAUSE);
            }
        }
    }

    private void timeOfFirstEvent(Decorators decorator) {
        if (startTime == null) {
            startTime = decorator.getDateTimeStamp();
        }

    }

    private void extractTagsAndLevels(Decorators decorators) {
        tagsAndLevels.add(decorators.getLogLevel() + ":" + decorators.getTags());
    }

    /*
    G1 flags
        ADAPTIVE_SIZING,RSET_STATS,PRINT_PROMOTION_FAILURE, PRINT_FLS_STATISTICS
        todo: test for log segments that are missing the header lines that will contain the "use" keyword
        ZGC - x
        Shenandoah - x
        G1 - done
        CMS - x
        Parallel - x
        Serial - x
     */

    private void discoverCollector(String line) {

        if ( ZGC_TAG.parse(line) != null || CYCLE_START.parse(line) != null) {
            getDiary().setTrue(ZGC);
            getDiary().setFalse(DEFNEW, SERIAL, PARALLELGC, PARALLELOLDGC, PARNEW, CMS, ICMS, G1GC, RSET_STATS, SHENANDOAH, CMS_DEBUG_LEVEL_1, PRE_JDK70_40, JDK70, JDK80, TENURING_DISTRIBUTION, MAX_TENURING_THRESHOLD_VIOLATION, PRINT_PROMOTION_FAILURE, PRINT_FLS_STATISTICS);
            return;
        }

        if ( SHENANDOAH_TAG.parse(line) != null) {
            getDiary().setTrue(SHENANDOAH);
            getDiary().setFalse(DEFNEW, SERIAL, PARALLELGC, PARALLELOLDGC, PARNEW, CMS, ICMS, G1GC, RSET_STATS, ZGC, CMS_DEBUG_LEVEL_1, PRE_JDK70_40, JDK70, JDK80, TENURING_DISTRIBUTION, MAX_TENURING_THRESHOLD_VIOLATION, PRINT_PROMOTION_FAILURE, PRINT_FLS_STATISTICS, PRINT_HEAP_AT_GC);
            return;
        }

        if (G1_TAG.parse(line) != null || line.contains("G1 Evacuation Pause") || (line.contains("Humongous regions: "))) {
            getDiary().setTrue(G1GC);
            getDiary().setFalse(DEFNEW, SERIAL, PARALLELGC, PARALLELOLDGC, PARNEW, CMS, ICMS, ZGC, SHENANDOAH, CMS_DEBUG_LEVEL_1, PRE_JDK70_40, JDK70, JDK80, PRINT_FLS_STATISTICS);
            return;
        }

        if (CMS_TAG.parse(line) != null ||
                PARNEW_TAG.parse(line) != null ||
                line.contains("ParNew")) {
            getDiary().setTrue(PARNEW, CMS);
            getDiary().setFalse(DEFNEW, SERIAL, PARALLELGC, PARALLELOLDGC, ICMS, CMS_DEBUG_LEVEL_1, G1GC, ZGC, SHENANDOAH, PRE_JDK70_40, JDK70, JDK80, RSET_STATS);
            return;
        }

        if (PARALLEL_TAG.parse(line) != null ||
                line.contains("ParOldGen") ||
                line.contains("PSYoungGen")) {
            getDiary().setTrue(PARALLELGC, PARALLELOLDGC, SupportedFlags.GC_CAUSE);
            getDiary().setFalse(DEFNEW, SERIAL, PARNEW, CMS, ICMS, CMS_DEBUG_LEVEL_1, G1GC, ZGC, SHENANDOAH, PRE_JDK70_40, JDK70, JDK80, RSET_STATS);
            return;
        }

        if (SERIAL_TAG.parse(line) != null || line.contains("DefNew")) {
            getDiary().setTrue(DEFNEW, SERIAL, SupportedFlags.GC_CAUSE);
            getDiary().setFalse(PARALLELGC, PARALLELOLDGC, PARNEW, CMS, ICMS, CMS_DEBUG_LEVEL_1, G1GC, ZGC, SHENANDOAH, PRE_JDK70_40, JDK70, JDK80, RSET_STATS);
            return;
        }
    }

    /**
     *
     * @param line
     */
    private void discoverDetails(String line) {

        if (stopTheWorldEvents > CYCLES_TO_EXAMINE_BEFORE_GIVING_UP)
            getDiary().setFalse(ADAPTIVE_SIZING, TLAB_DATA, PRINT_REFERENCE_GC, PRINT_PROMOTION_FAILURE, PRINT_FLS_STATISTICS, PRINT_HEAP_AT_GC);

        if (CYCLE_START.parse(line) != null) {
            getDiary().setTrue(SupportedFlags.GC_CAUSE);
        }
    }

    private void discoverJVMEvents(String line) {
        if (stopTheWorldEvents > CYCLES_TO_EXAMINE_FOR_SAFEPOINT) {
            getDiary().setFalse(APPLICATION_STOPPED_TIME, APPLICATION_CONCURRENT_TIME);
        }
    }

    @Override
    public DateTimeStamp getTimeOfFirstEvent() {
        return startTime;
    }
}