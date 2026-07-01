// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.jvm;

import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.jvm.SupportedFlags;
import com.microsoft.gctoolkit.parser.unified.UnifiedLoggingLevel;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.Set;
import java.util.TreeSet;

import com.microsoft.gctoolkit.jvm.SupportedFlags;
import static com.microsoft.gctoolkit.parser.unified.ShenandoahPatterns.SHENANDOAH_TAG;
import static com.microsoft.gctoolkit.parser.unified.UnifiedG1GCPatterns.G1_COLLECTION;
import static com.microsoft.gctoolkit.parser.unified.UnifiedG1GCPatterns.G1_TAG;
import static com.microsoft.gctoolkit.parser.unified.UnifiedGenerationalPatterns.*;
import static com.microsoft.gctoolkit.parser.unified.UnifiedPatterns.CPU_BREAKOUT;
import static com.microsoft.gctoolkit.parser.unified.ZGCPatterns.CYCLE_START;

//ShenandoahPatterns, ZGCPatterns, UnifiedG1GCPatterns, UnifiedGenerationalPatterns,
public class UnifiedDiarizer implements Diarizer {

    private static final int CYCLES_TO_EXAMINE_BEFORE_GIVING_UP = 10;
    private static final int CYCLES_TO_EXAMINE_FOR_SAFEPOINT = 2;

    private int lineCount = MAXIMUM_LINES_TO_EXAMINE;

    private final Diary diary;
    private final Set<String> tagsAndLevels = new TreeSet<>();
    private int stopTheWorldEvents = 0;

    {
        diary = new Diary();
        diary.setTrue(SupportedFlags.UNIFIED_LOGGING);
        diary.setFalse(SupportedFlags.ICMS, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK80, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);

    }

    public UnifiedDiarizer() {}

    @Override
    public Diary getDiary() {
        fillInKnowns();
        return diary;
    }


    @Override
    public boolean isUnified() {
        return true;
    }

    @Override
    public String getCommandLine() {
        // TODO #147 extract command line from the log file
        return "";
    }

    @Override
    public int getMaxTenuringThreshold() {
        return 0;
    }

    @Override
    public boolean hasJVMEvents() {
        return diary.isApplicationStoppedTime() ||
                diary.isApplicationRunningTime() ||
                diary.isTLABData();
    }

    private void fillInKnowns() {
        diary.setFalse(SupportedFlags.ADAPTIVE_SIZING);
    }

    @Override
    public boolean completed() {
        return diary.isComplete() || lineCount < 1;
    }

    @Override
    public boolean diarize(String line) {
        if ( ! this.completed()) {
            if (!line.startsWith("["))
                return false;
            lineCount--;
            extractDecorators(line);
            if (!diary.isCollectorKnown())
                discoverCollector(line);
            if (!diary.isDetailsKnown())
                discoverDetails(line);
            if (!diary.isJVMEventsKnown())
                discoverJVMEvents(line);
            if ((CPU_BREAKOUT.parse(line) != null) || line.contains("gc,start"))
                stopTheWorldEvents++;
        }
        return this.completed();
    }

    /// Extract decorators (from a GC log line tag) and set the corresponding diary flags accordingly
    ///
    /// @param line GC log line
    private void extractDecorators(String line) {
        var decorators = new Decorators(line);
        timeOfFirstEvent(decorators);
        extractTagsAndLevels(decorators);
        // -Xlog:gc*,gc+ref=debug,gc+phases=debug,gc+age=trace,safepoint
        if (decorators.getLogLevel().isPresent()) {
            UnifiedLoggingLevel logLevel = decorators.getLogLevel().get();
            if (decorators.tagsContain("gc,age"))
                diary.setTrue(SupportedFlags.TENURING_DISTRIBUTION);
            else if (decorators.tagsContain("ref") && logLevel.isGreaterThanOrEqualTo(UnifiedLoggingLevel.debug))
                diary.setTrue(SupportedFlags.PRINT_REFERENCE_GC);
            else if (decorators.tagsContain("gc,phases") && logLevel.isGreaterThanOrEqualTo(UnifiedLoggingLevel.debug))
                diary.setTrue(SupportedFlags.GC_DETAILS);
            else if ( decorators.tagsContain("gc,ergo"))
                diary.setTrue(SupportedFlags.ADAPTIVE_SIZING);
            else if (decorators.tagsContain("gc,cpu"))
            	diary.setTrue(SupportedFlags.PRINT_CPU_TIMES);
            
            if (decorators.tagsContain("safepoint"))
                diary.setTrue(SupportedFlags.APPLICATION_STOPPED_TIME, SupportedFlags.APPLICATION_CONCURRENT_TIME);

            if (diary.isZGC()) {
                if (decorators.tagsContain("task"))
                    diary.setTrue(SupportedFlags.GC_DETAILS);
                else if (decorators.tagsContain("heap"))
                    diary.setTrue(SupportedFlags.PRINT_HEAP_AT_GC);
                else if (decorators.tagsContain("tlab"))
                    diary.setTrue(SupportedFlags.TLAB_DATA);
                else if (decorators.tagsContain("gc,start") && line.contains("Garbage Collection ("))
                    diary.setTrue(SupportedFlags.GC_CAUSE);
                else if (decorators.tagsContain("gc,heap")) {
                    if (line.contains("Heap before GC"))
                        diary.setTrue(SupportedFlags.PRINT_HEAP_AT_GC);
                    diary.setTrue(SupportedFlags.GC_DETAILS);
                } else if (decorators.tagsContain("gc,ref"))
                    diary.setTrue(SupportedFlags.PRINT_REFERENCE_GC);
                else if (decorators.tagsContain("gc,heap") && decorators.getLogLevel().get() == UnifiedLoggingLevel.debug)
                    diary.setTrue(SupportedFlags.PRINT_HEAP_AT_GC);
            } else if (diary.isShenandoah()) {
                if (decorators.tagsContain("gc,task") || decorators.tagsContain("gc,start"))
                    diary.setTrue(SupportedFlags.GC_DETAILS);
                else if (decorators.tagsContain("gc,ergo"))
                    diary.setTrue(SupportedFlags.ADAPTIVE_SIZING);
                else if (decorators.tagsContain("gc") && line.contains("Trigger"))
                    diary.setTrue(SupportedFlags.GC_CAUSE);
            }
        }
    }

    private void timeOfFirstEvent(Decorators decorator) {
        if ( ! diary.hasTimeOfFirstEvent())
            diary.setTimeOfFirstEvent(decorator.getDateTimeStamp());
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

        if (CYCLE_START.parse(line) != null) {
            String cycleType = CYCLE_START.parse(line).getGroup(2);
            diary.setTrue(SupportedFlags.ZGC);
            if("Minor".equals(cycleType) || "Major".equals(cycleType))
                diary.setTrue(SupportedFlags.GENERATIONAL_ZGC);
            else
                diary.setFalse(SupportedFlags.GENERATIONAL_ZGC);
            diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.SERIAL, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.SHENANDOAH, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK70, SupportedFlags.JDK80, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION, SupportedFlags.TLAB_DATA, SupportedFlags.PRINT_PROMOTION_FAILURE, SupportedFlags.PRINT_FLS_STATISTICS, SupportedFlags.ADAPTIVE_SIZING);
            return;
        }

        if ( SHENANDOAH_TAG.parse(line) != null) {
            diary.setTrue(SupportedFlags.SHENANDOAH);
            diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.SERIAL, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.ZGC, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK70, SupportedFlags.JDK80, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION, SupportedFlags.TLAB_DATA, SupportedFlags.PRINT_PROMOTION_FAILURE, SupportedFlags.PRINT_FLS_STATISTICS, SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.GENERATIONAL_ZGC);
            return;
        }

        if (G1_TAG.parse(line) != null || line.contains("G1 Evacuation Pause") || (line.contains("Humongous regions: "))) {
            diary.setTrue(SupportedFlags.G1GC);
            diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.SERIAL, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.ZGC, SupportedFlags.SHENANDOAH, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK70, SupportedFlags.JDK80, SupportedFlags.TLAB_DATA, SupportedFlags.PRINT_PROMOTION_FAILURE, SupportedFlags.PRINT_FLS_STATISTICS, SupportedFlags.GENERATIONAL_ZGC);
            return;
        }

        if (CMS_TAG.parse(line) != null ||
                PARNEW_TAG.parse(line) != null ||
                line.contains("ParNew")) {
            diary.setTrue(SupportedFlags.PARNEW, SupportedFlags.CMS);
            diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.SERIAL, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.ICMS, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.G1GC, SupportedFlags.ZGC, SupportedFlags.SHENANDOAH, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK70, SupportedFlags.JDK80, SupportedFlags.RSET_STATS, SupportedFlags.GENERATIONAL_ZGC);
            return;
        }

        if (PARALLEL_TAG.parse(line) != null ||
                line.contains("ParOldGen") ||
                line.contains("PSYoungGen")) {
            diary.setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.GC_CAUSE);
            diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.SERIAL, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.G1GC, SupportedFlags.ZGC, SupportedFlags.SHENANDOAH, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK70, SupportedFlags.JDK80, SupportedFlags.RSET_STATS, SupportedFlags.GENERATIONAL_ZGC);
            return;
        }

        if (SERIAL_TAG.parse(line) != null || line.contains("DefNew")) {
            diary.setTrue(SupportedFlags.DEFNEW, SupportedFlags.SERIAL, SupportedFlags.GC_CAUSE);
            diary.setFalse(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.G1GC, SupportedFlags.ZGC, SupportedFlags.SHENANDOAH, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK70, SupportedFlags.JDK80, SupportedFlags.RSET_STATS, SupportedFlags.GENERATIONAL_ZGC);
            return;
        }
    }

    /// @param line
    private void discoverDetails(String line) {

        //todo: RSET_STATS for G1 not looked for...
        if (G1_COLLECTION.parse(line) != null) {
            diary.setTrue(SupportedFlags.GC_CAUSE);
        }

        else if (CYCLE_START.parse(line) != null) {
            diary.setTrue(SupportedFlags.GC_CAUSE);
        }

        if (stopTheWorldEvents > CYCLES_TO_EXAMINE_BEFORE_GIVING_UP)
            diary.setFalse(SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.GC_CAUSE, SupportedFlags.TLAB_DATA, SupportedFlags.PRINT_REFERENCE_GC, SupportedFlags.PRINT_PROMOTION_FAILURE, SupportedFlags.PRINT_FLS_STATISTICS, SupportedFlags.RSET_STATS, SupportedFlags.PRINT_HEAP_AT_GC);
    }

    private void discoverJVMEvents(String line) {
        if (stopTheWorldEvents > CYCLES_TO_EXAMINE_FOR_SAFEPOINT) {
            diary.setFalse(SupportedFlags.APPLICATION_STOPPED_TIME, SupportedFlags.APPLICATION_CONCURRENT_TIME);
        }
    }

    @Override
    public DateTimeStamp getTimeOfFirstEvent() {
        return diary.getTimeOfFirstEvent();
    }

}