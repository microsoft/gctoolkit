// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.jvm;


import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GCCauses;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.parser.CMSPatterns;
import com.microsoft.gctoolkit.parser.G1GCPatterns;
import com.microsoft.gctoolkit.parser.G1GCTokens;
import com.microsoft.gctoolkit.parser.GCLogTrace;
import com.microsoft.gctoolkit.parser.GCParseRule;
import com.microsoft.gctoolkit.parser.JVMPatterns;
import com.microsoft.gctoolkit.parser.ParallelPatterns;
import com.microsoft.gctoolkit.parser.PreUnifiedTokens;
import com.microsoft.gctoolkit.parser.SerialPatterns;
import com.microsoft.gctoolkit.parser.SimplePatterns;
import com.microsoft.gctoolkit.parser.TenuredPatterns;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to answer 3 questions about the GC log;
 * - which collector combination is being used
 * - which JVM version
 * - which flags are being used
 * <p>
 * Collectors
 * - Unknown (no details)
 * - Serial
 * - ParNew/CMS
 * - iCMS
 * - PSYoungGen/PSFull
 * - G1GC
 * <p>
 * Version breaks
 * Can be known precisely if header is present in GC log.
 * prior to 1.7.0_45
 * 1.7.0_45 has the System.gc() regression
 * 1.8.0_05 anything after this version will include G1 Metaspace clause
 * 1.9.0 unknown how to determine this via deriveConfiguration.
 * <p>
 * Are these flags set
 * PrintGCDetails
 * PrintTenuringDistrubtion
 * PrintGCApplicationStoppedTime
 * PrintGCApplicationConcurrentTime
 * <p>
 * Log file characteristics
 * <p>
 * pre 7.0_45
 * - Permspace
 * - (System)
 * <p>
 * 7.0_45+
 * - (System.gc())
 * - Permspace
 * <p>
 * 8.0
 * - Metaspace
 * <p>
 * 8.0_20
 * - G1 prints Metaspace clause
 * <p>
 * Collector clues
 * iCMS
 * - CMS cycle starts early but once completed all further records will contain "icms_dc="
 * <p>
 * ParNew/CMS
 * - ParNew
 * - initial-mark
 * <p>
 * PSYoung/PSFull
 * - PSYoungGen
 * - PSFull
 * <p>
 * Serial
 * - DefNew
 */

public class PreUnifiedJVMConfiguration implements SimplePatterns, CMSPatterns, ParallelPatterns, G1GCPatterns, JVMConfiguration {

    private static final Logger LOGGER = Logger.getLogger(PreUnifiedJVMConfiguration.class.getName());

    private int lineCount = MAXIMUM_LINES_TO_EXAMINE;

    private static final GCParseRule TENURED_BLOCK = new GCParseRule("TENURED_BLOCK", "\\[Tenured: \\d+K->\\d+K\\(\\d+K\\), \\d+[.|,]\\d{7} secs\\]");
    private static final GCParseRule PREFIX = new GCParseRule("PREFIX", PreUnifiedTokens.GC_PREFIX);
    private static final GCParseRule FULL_PREFIX = new GCParseRule("FULL_PREFIX", PreUnifiedTokens.FULL_GC_PREFIX);
    private static final GCParseRule G1GC_PREFIX = new GCParseRule("G1GC_PREFIX", G1GCTokens.G1GC_PREFIX);
    private static final GCParseRule REFERENCE_PROCESSING_BLOCK = new GCParseRule("REFERENCE_PROCESSING_BLOCK", PreUnifiedTokens.REFERENCE_RECORDS);
    private static final Pattern excludeG1Ergonomics = Pattern.compile("^\\d+(\\.|,)\\d+: \\[G1Ergonomics");


    private final LoggingDiary diary;

    @Override
    public LoggingDiary getDiary() {
        return diary;
    }

    private boolean firstCMSCycle = false;
    private int youngCountAfterFirstCMSCycle = 0;
    private int collectionCount = 0;
    private int youngCollectionCount = 0;
    private int tenuringSummary = 0;
    private boolean ageTableDetected = false;
    private int maxTenuringThreshold = 15;
    private int setGCFlags = 0;
    private DateTimeStamp timeOfFirstEvent = null;

    {
        diary = new LoggingDiary();
        diary.setFalse(SupportedFlags.UNIFIED_LOGGING, SupportedFlags.ZGC, SupportedFlags.SHENANDOAH);
    }


    @Override
    public String getCommandLine() {
        // TODO
        return "";
    }

    @Override
    public int getMaxTenuringThreshold() {
        return maxTenuringThreshold;
    }

    @Override
    public boolean hasJVMEvents() {
        return getDiary().isApplicationStoppedTime() ||
                getDiary().isApplicationRunningTime() ||
                getDiary().isApplicationRunningTime() ||
                getDiary().isTLABData();
    }

    private boolean versionIsKnown() {
        return (getDiary().isStateKnown(SupportedFlags.JDK80) && getDiary().isStateKnown(SupportedFlags.JDK70)) && getDiary().isStateKnown(SupportedFlags.PRE_JDK70_40);
    }

    @Override
    public boolean completed() {
        return getDiary().isComplete() || lineCount < 1 || (simpleCMSCycleDetected && (simpleCMSCycleDetected || simpleFullGCDetected));
    }

    // Things that if we've not seen them, we know that they are false.
    public void fillInKnowns() {
        if (simpleCMSCycleDetected) {
            getDiary().setTrue(SupportedFlags.CMS);
            getDiary().setFalse(SupportedFlags.SERIAL, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.ICMS, SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.GC_CAUSE, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.RSET_STATS, SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK80);
            // if we have age table or if no tenuring distribution, assume default ParNew/CMS
            if (youngCollectionCount == 0 || ageTableDetected || !getDiary().isTenuringDistribution()) {
                getDiary().setTrue(SupportedFlags.PARNEW);
                getDiary().setFalse(SupportedFlags.DEFNEW);
            } else { //CMS with no age table implies DEFNEW/CMS
                getDiary().setTrue(SupportedFlags.DEFNEW);
                getDiary().setFalse(SupportedFlags.PARNEW);
            }
        } else if (simpleFullGCDetected) {
            if (ageTableDetected) {
                getDiary().setTrue(SupportedFlags.PARNEW, SupportedFlags.SERIAL);
            } else { //at this point we can't tell if it's serial or parallel so assume defaults
                getDiary().setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
            }
            getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.SERIAL, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.GC_CAUSE, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.RSET_STATS, SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK80);
        }

        //Not much information here, assume defaults
        else if (simpleParallelOrParNewDetected) {
            if (ageTableDetected) {
                getDiary().setTrue(SupportedFlags.PARNEW, SupportedFlags.CMS);
                getDiary().setFalse(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
            } else {
                getDiary().setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
                getDiary().setFalse(SupportedFlags.PARNEW, SupportedFlags.CMS);
            }
            getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.SERIAL, SupportedFlags.ICMS, SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.GC_CAUSE, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.RSET_STATS, SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK80);
        }

        getDiary().setFalse(SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION, SupportedFlags.PRINT_PROMOTION_FAILURE, SupportedFlags.PRINT_FLS_STATISTICS);
    }

    /**
     * Attempt to diarize the GC Log line that's come.  That is, if we collect enough details about
     * this log in oder to categorize it by collector and collector features then we return true
     */
    @Override
    public boolean diarize(String line) {

        timeOfFirstEvent(line);
        lineCount--;

        if (line.startsWith("Java HotSpot(TM)") || line.startsWith("OpenJDK")) {
            parseJVMVersion(line);
        } else if (line.startsWith("CommandLine ")) {
            evaluateCommandLineFlags(line.split(" "));
            /*
             * We're almost done, all we need is a time stamp which should be on the next line.
             * but give it some time to find it in case the log has other stuff in it
             */
            lineCount = 25;
            collectionCount = 3;
        } else if (!jvmActivityFlag(line)) {
            collector(line);
            version(line);
            details(line);
        }

        return this.completed();
    }

    @Override
    public DateTimeStamp getTimeOfFirstEvent() {
        if (timeOfFirstEvent == null)
            timeOfFirstEvent = new DateTimeStamp(0.0d);
        return timeOfFirstEvent;
    }

    private void timeOfFirstEvent(String line) {
        if (timeOfFirstEvent == null) {
            Matcher matcher = excludeG1Ergonomics.matcher(line);
            if (matcher.find()) {
                return;      // G1Ergonomics doesn't respect PrintDateStamp which confuses downstream calculations
            }
            GCLogTrace trace = PreUnifiedTokens.DATE_TIMESTAMP_RECORD.parse(line);
            if (trace != null)
                timeOfFirstEvent = trace.getDateTimeStamp();
        }
    }

    private boolean jvmActivityFlag(String line) {

        if (getDiary().isStateKnown(SupportedFlags.APPLICATION_STOPPED_TIME, SupportedFlags.APPLICATION_CONCURRENT_TIME, SupportedFlags.TLAB_DATA))
            return false;

        if ((JVMPatterns.APPLICATION_STOP_TIME.parse(line) != null) || (JVMPatterns.SIMPLE_APPLICATION_STOP_TIME.parse(line) != null) || JVMPatterns.APPLICATION_STOP_TIME_WITH_STOPPING_TIME.parse(line) != null) {
            getDiary().setTrue(SupportedFlags.APPLICATION_STOPPED_TIME);
            return true;
        } else if ((JVMPatterns.APPLICATION_TIME.parse(line) != null) || (JVMPatterns.SIMPLE_APPLICATION_TIME.parse(line) != null)) {
            getDiary().setTrue(SupportedFlags.APPLICATION_CONCURRENT_TIME);
            return true;
        } else if (JVMPatterns.TLAB_CONT.parse(line) != null) {
            getDiary().setTrue(SupportedFlags.TLAB_DATA);
            return true;
        }
        //This will be reported along size a collection so if we don't see them by 3nd collection....
        // maybe some rubbish lines between collection and log so wait a bit longer than just the reporting of the first collection
        if (collectionCount > 1) {
            getDiary().setFalse(SupportedFlags.APPLICATION_STOPPED_TIME, SupportedFlags.APPLICATION_CONCURRENT_TIME, SupportedFlags.TLAB_DATA);
        }

        return false;
    }

    private boolean simpleParallelOrParNewDetected = false;
    private boolean simpleFullGCDetected = false;
    private boolean simpleCMSCycleDetected = false;

    //TODO: PSYoung -> PSOldGen, DefNew->PSOldGen, so maybe we need to break up Parallel for generational collections
    private void collector(String line) {

        GCLogTrace trace;

        if ((!getDiary().isStateKnown(SupportedFlags.PARNEW, SupportedFlags.DEFNEW, SupportedFlags.PARALLELGC, SupportedFlags.G1GC)) ||
                !getDiary().isStateKnown(SupportedFlags.GC_DETAILS) || collectionCount < 3) {

            if (line.contains("[PSYoungGen:")) {
                getDiary().setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION, SupportedFlags.RSET_STATS);
                collectionCount++;
                youngCollectionCount++;

                if ((trace = ParallelPatterns.PSYOUNGGEN.parse(line)) != null) {
                    getDiary().setTrue(SupportedFlags.GC_DETAILS);
                    getDiary().setFalse(SupportedFlags.TENURING_DISTRIBUTION);
                    getDiary().setFalse(SupportedFlags.PRINT_HEAP_AT_GC);
                    setGCCause(trace.getGroup(3));

                } else if (ParallelPatterns.PS_DETAILS_WITH_TENURING.parse(line) != null) {
                    getDiary().setTrue(SupportedFlags.GC_DETAILS);
                    getDiary().setFalse(SupportedFlags.PRINT_HEAP_AT_GC);
                }
            } else if (line.contains("ParNew")) {
                getDiary().setTrue(SupportedFlags.PARNEW, SupportedFlags.CMS);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                collectionCount++;
                youngCollectionCount++;

                if ((trace = CMSPatterns.PARNEW.parse(line)) != null) {
                    getDiary().setTrue(SupportedFlags.GC_DETAILS);
                    getDiary().setFalse(SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                    setGCCause(trace.getGroup(4));
                } else if ((trace = CMSPatterns.PARNEW_TENURING.parse(line)) != null) {
                    getDiary().setTrue(SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION);
                    getDiary().setFalse(SupportedFlags.CMS_DEBUG_LEVEL_1);
                    setGCCause(trace.getGroup(3));
                } else if ((trace = SimplePatterns.PARNEW_NO_DETAILS.parse(line)) != null) {
                    getDiary().setFalse(SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                    setGCCause(trace.getGroup(3));
                } else if ((trace = SimplePatterns.PARNEW_START.parse(line)) != null) {
                    getDiary().setFalse(SupportedFlags.GC_DETAILS, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.PRINT_HEAP_AT_GC);
                    setGCCause(trace.groupCount() > 2 ? trace.getGroup(3) : null);
                } else if ((trace = CMSPatterns.PARNEW_REFERENCE_SPLIT.parse(line)) != null) {
                    getDiary().setTrue(SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.PRINT_REFERENCE_GC);
                    setGCCause(trace.getGroup(3));
                }
            } else if ((trace = SerialPatterns.DEFNEW.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                getDiary().setTrue(SupportedFlags.DEFNEW, SupportedFlags.GC_DETAILS);
                getDiary().setFalse(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.PARNEW, SupportedFlags.G1GC, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.RSET_STATS, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                setGCCause(trace.getGroup(3));
            } else if ((trace = SerialPatterns.DEFNEW_TENURING.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                getDiary().setTrue(SupportedFlags.DEFNEW, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION);
                getDiary().setFalse(SupportedFlags.PARNEW, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                setGCCause(trace.getGroup(3));
            } else if (SimplePatterns.PARNEW_NO_DETAILS.parse(line) != null) {
                collectionCount++;
                youngCollectionCount++;
                getDiary().setTrue(SupportedFlags.PARNEW);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.GC_CAUSE, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.RSET_STATS, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
            } else if (SimplePatterns.YOUNG_NO_DETAILS.parse(line) != null) {
                collectionCount++;
                youngCollectionCount++;
                simpleParallelOrParNewDetected = true;
            } else if (SimplePatterns.FULL_NO_GC_DETAILS.parse(line) != null) {
                collectionCount++;
                simpleFullGCDetected = true;
                simpleParallelOrParNewDetected = true;
            } else if ((trace = SimplePatterns.GC_START.parse(line)) != null) {
                collectionCount++;
                simpleParallelOrParNewDetected = true;
                youngCollectionCount++;
                getDiary().setFalse(SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                setGCCause(trace.getGroup(3));
            } else if (SimplePatterns.CMS_NO_DETAILS.parse(line) != null) {
                // could be parallel or CMS.. look for Full GC but even that may be a trick
                collectionCount++;
                simpleParallelOrParNewDetected = true;
                simpleCMSCycleDetected = true;
                firstCMSCycle = true;
            } else if ((trace = G1GCPatterns.G1_YOUNG_SPLIT_START.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
                getDiary().setTrue(SupportedFlags.G1GC);
                getDiary().setTrue(SupportedFlags.GC_DETAILS);
                if (trace.gcCause() == GCCause.GCCAUSE_NOT_SET) {
                    getDiary().setFalse(SupportedFlags.GC_CAUSE);
                } else if (trace.gcCause() == GCCause.METADATA_GENERATION_THRESHOLD) {
                    getDiary().setTrue(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
                    getDiary().setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
                } else if ((trace.gcCause() == GCCause.G1_EVACUATION_PAUSE) || (trace.gcCause(3, 0) == GCCause.G1_HUMONGOUS_ALLOCATION)) {
                    getDiary().setTrue(SupportedFlags.GC_CAUSE);
                    getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
                }
            } else if ((trace = G1GCPatterns.G1_DETAILS.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                getDiary().setTrue(SupportedFlags.G1GC, SupportedFlags.GC_DETAILS);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                if (trace.gcCause(3, 0) == GCCause.GCCAUSE_NOT_SET) {
                    getDiary().setFalse(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
                    getDiary().setTrue(SupportedFlags.JDK70);
                } else {
                    getDiary().setFalse(SupportedFlags.GC_CAUSE);
                    getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
                }
            } else if ((trace = G1GCPatterns.YOUNG.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                getDiary().setTrue(SupportedFlags.G1GC);
                checkForGCCause(trace);

                //2014-10-21T11:49:08.954-0500: 12053.551: [GC pause (young)12054.116: [SoftReference, 0 refs, 0.0000070 secs]12054.116: [WeakReference, 234 refs, 0.0000640 secs]12054.116: [FinalReference, 3805 refs, 0.0034010 secs]12054.119: [PhantomReference, 9 refs, 0.0000040 secs]12054.119: [JNI Weak Reference, 0.0001960 secs], 0.58191800 secs]
            } else if ((trace = G1GCPatterns.G1_DETAILS_REFERENCE_GC.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                getDiary().setTrue(SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.PRINT_REFERENCE_GC);
                if (trace.getGroup(3) != null)
                    getDiary().setTrue(SupportedFlags.GC_CAUSE);
                else
                    getDiary().setFalse(SupportedFlags.GC_CAUSE);
            } else if (G1GCPatterns.G1_INITIAL_MARK.parse(line) != null) {
                collectionCount++;
                youngCollectionCount++;
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                getDiary().setTrue(SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.GC_CAUSE, SupportedFlags.TENURING_DISTRIBUTION);
            }
        }

        if (getDiary().isTrue(SupportedFlags.ADAPTIVE_SIZING)) {
            if ((trace = G1GCPatterns.YOUNG_SPLIT_BY_G1ERGONOMICS.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                getDiary().setTrue(SupportedFlags.G1GC, SupportedFlags.GC_DETAILS);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
                checkForGCCause(trace);
            }
        }

        //This has been verified true as of 1.7.0_51. Check later versions to make sure it hasn't been back ported
        if (ParallelPatterns.PS_FULL_GC_PERM.parse(line) != null) {
            collectionCount++;
            getDiary().setTrue(SupportedFlags.JDK70);
            getDiary().setFalse(SupportedFlags.JDK80);
            if (line.contains(" [PSYoungGen: "))
                getDiary().setTrue(SupportedFlags.PARALLELGC);
            getDiary().setTrue(SupportedFlags.PARALLELOLDGC, SupportedFlags.GC_DETAILS);
            getDiary().setFalse(SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.SERIAL, SupportedFlags.G1GC);

            getDiary().setFalse(SupportedFlags.CMS_DEBUG_LEVEL_1);
            getDiary().setFalse(SupportedFlags.PRINT_HEAP_AT_GC);

        }

        //This has been verified true as of 1.7.0_51. Check later versions to make sure it hasn't been back ported
        else if (ParallelPatterns.PS_FULL_GC_META.parse(line) != null) {
            collectionCount++;
            getDiary().setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
            getDiary().setTrue(SupportedFlags.JDK80);
            if (line.contains(" [PSYoungGen: "))
                getDiary().setTrue(SupportedFlags.PARALLELGC);
            getDiary().setTrue(SupportedFlags.PARALLELOLDGC, SupportedFlags.GC_DETAILS);
            getDiary().setFalse(SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.SERIAL, SupportedFlags.ICMS, SupportedFlags.G1GC);

            getDiary().setFalse(SupportedFlags.CMS_DEBUG_LEVEL_1);
            getDiary().setFalse(SupportedFlags.PRINT_HEAP_AT_GC);

        } else if (line.contains("CMS-initial-mark")) {
            collectionCount++;
            getDiary().setFalse(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC);
            getDiary().setTrue(SupportedFlags.CMS, SupportedFlags.GC_DETAILS);
        }

        //todo: this rule is in the wrong place
        else if (CMSPatterns.SERIAL_FULL.parse(line) != null) {
            collectionCount++;
            getDiary().setFalse(SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.G1GC);
            getDiary().setTrue(SupportedFlags.SERIAL);

            getDiary().setTrue(SupportedFlags.GC_DETAILS);
            //todo: private final int SupportedFlags.GC_CAUSE = 12;
            getDiary().setFalse(SupportedFlags.CMS_DEBUG_LEVEL_1);
            if (line.contains("Metaspace")) {
                getDiary().setFalse(SupportedFlags.JDK70);
            }
            if (line.contains("Tenured")) {
                getDiary().setFalse(SupportedFlags.CMS, SupportedFlags.ICMS);
                getDiary().setTrue(SupportedFlags.SERIAL);
            } else if (line.contains("CMS")) {
                getDiary().setFalse(SupportedFlags.SERIAL);
                getDiary().setTrue(SupportedFlags.CMS);
            }
        } else if (CMSPatterns.SERIAL_FULL80.parse(line) != null) {
            collectionCount++;
            getDiary().setFalse(SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELOLDGC, SupportedFlags.G1GC, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.JDK70);
            getDiary().setTrue(SupportedFlags.SERIAL, SupportedFlags.GC_DETAILS, SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
        } else if (TENURED_BLOCK.parse(line) != null) {
            collectionCount++;
            getDiary().setFalse(SupportedFlags.CMS);
            getDiary().setFalse(SupportedFlags.ICMS);
            getDiary().setTrue(SupportedFlags.SERIAL);
            getDiary().setFalse(SupportedFlags.G1GC);
        }

        if (getDiary().isTrue(SupportedFlags.CMS) && !getDiary().isStateKnown(SupportedFlags.ICMS)) {
            if (firstCMSCycle) {
                if (line.contains("ParNew") || line.contains("DefNew"))
                    youngCountAfterFirstCMSCycle++;

                if (line.contains("icms_dc"))
                    getDiary().setTrue(SupportedFlags.ICMS);
                else if (youngCountAfterFirstCMSCycle > 1)
                    getDiary().setFalse(SupportedFlags.ICMS);
            }
            //The first CMS cycle is needed to kick off iCMS
            if (line.contains("concurrent-reset"))
                firstCMSCycle = true;
        } else if (getDiary().isTrue(SupportedFlags.G1GC)) {
            if (G1GCPatterns.G1_MEMORY_SUMMARY.parse(line) != null) {
                if (line.contains("Metaspace")) {
                    getDiary().setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
                    getDiary().setTrue(SupportedFlags.JDK80);
                } else {
                    getDiary().setTrue(SupportedFlags.JDK70);
                    getDiary().setFalse(SupportedFlags.JDK80);
                }
            }
        }
    }

    private void checkForGCCause(GCLogTrace trace) {
        if (trace.gcCause(3, 0) == GCCause.METADATA_GENERATION_THRESHOLD) {
            getDiary().setTrue(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
            getDiary().setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
        } else if ((trace.gcCause(3, 0) == GCCause.G1_EVACUATION_PAUSE) || (trace.gcCause(3, 0) == GCCause.G1_HUMONGOUS_ALLOCATION)) {
            getDiary().setTrue(SupportedFlags.GC_CAUSE);
        }
    }

    /*
     * Mostly for getting version information but we can also capture other information in the deriveConfiguration.
     *
     * if the collector is G1 I don't currently have a good way of saying 7.0 or 8.0 as perm
     * and meta space records are not printed with a Full GC. Metaspace will be emitted to the
     * log in the version after 8.0_05.
     */
    private void version(String line) {

        if (versionIsKnown()) return;

        GCLogTrace trace;
        //Can get a lot of information from the perm space record
        // (CMS Perm |PS Perm |Perm |PSPermGen|Metaspace)
        if ((trace = ParallelPatterns.PERM_SPACE_RECORD.parse(line)) != null) {
            String value = trace.getGroup(1).trim();

            if ("CMS Perm".equals(value)) {
                getDiary().setTrue(SupportedFlags.JDK70);
                getDiary().setFalse(SupportedFlags.JDK80);
            } else if ("PS Perm".equals(value)) {
                getDiary().setTrue(SupportedFlags.JDK70);
                getDiary().setFalse(SupportedFlags.JDK80);
            } else if ("Perm".equals(value)) {
                getDiary().setTrue(SupportedFlags.JDK70);
                getDiary().setFalse(SupportedFlags.JDK80);
            } else if ("PSPermGen".equals(value)) {
                getDiary().setTrue(SupportedFlags.JDK70);
                getDiary().setFalse(SupportedFlags.JDK80);
            } else if ("Metaspace".equals(value)) {
                getDiary().setTrue(SupportedFlags.JDK80);
                getDiary().setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
            }
        } else if (META_SPACE_RECORD.parse(line) != null) {
            getDiary().setTrue(SupportedFlags.JDK80);
            getDiary().setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
        }

        // maybe we'll get lucky, app server often call System.gc() after startup.
        if (line.contains("(System)")) {
            getDiary().setTrue(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
            getDiary().setFalse(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
        } else if (line.contains("(System.gc()")) {
            getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
            getDiary().setTrue(SupportedFlags.GC_CAUSE);
        } else if (getDiary().isGenerationalKnown() && getDiary().isGenerational()) {
            if ((trace = PREFIX.parse(line)) != null) {
                if ((trace.getGroup(3) == null) && getDiary().isTrue(SupportedFlags.GC_DETAILS)) {
                    getDiary().setTrue(SupportedFlags.JDK70);
                    getDiary().setFalse(SupportedFlags.JDK80, SupportedFlags.GC_CAUSE);
                } else if (trace.gcCause(3, 0) != GCCause.GCCAUSE_NOT_SET) {
                    getDiary().setTrue(SupportedFlags.GC_CAUSE);
                    getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
                }
            } else if ((trace = FULL_PREFIX.parse(line)) != null) {
                if ((trace.getGroup(3) == null) && getDiary().isTrue(SupportedFlags.GC_DETAILS)) {
                    getDiary().setTrue(SupportedFlags.JDK70);
                    getDiary().setFalse(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
                } else if (trace.gcCause(3, 0) != GCCause.GCCAUSE_NOT_SET) {
                    getDiary().setTrue(SupportedFlags.GC_CAUSE);
                    getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
                }
            }

        } else if (getDiary().isG1GCKnown() && getDiary().isG1GC()) {
            if ((trace = G1GC_PREFIX.parse(line)) != null) {
                if (getDiary().isTrue(SupportedFlags.GC_DETAILS) && (trace.gcCause() == GCCause.GCCAUSE_NOT_SET)) {
                    getDiary().setTrue(SupportedFlags.JDK70);
                    getDiary().setFalse(SupportedFlags.JDK80);
                } else { //we can't say much else unless we look for 8.0 specific details
                    getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
                }
            }
        }
    }

    /*
     * Currently looking for;
     * 1) PrintGCDetails
     * 2) PrintGCCause
     * 3) PrintTenuringDistribution
     */
    private void details(String line) {

        GCLogTrace trace;

        if ((trace = PREFIX.parse(line)) != null) {
            String cause = trace.getGroup(3);
            if (cause != null) {
                getDiary().setTrue(SupportedFlags.GC_CAUSE);
                if ("(System)".equals(cause)) {
                    getDiary().setFalse(SupportedFlags.JDK80);
                    getDiary().setTrue(SupportedFlags.JDK70);
                    getDiary().setTrue(SupportedFlags.PRE_JDK70_40);
                } else if ("(System.gc())".equals(cause)) {
                    getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
                } else {
                    getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
                }
            } else {
                if ((trace = G1GC_PREFIX.parse(line)) != null) {
                    cause = trace.getGroup(3);
                    if (cause == null)
                        getDiary().setTrue(SupportedFlags.PRE_JDK70_40);
                    else
                        getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
                } else {
                    getDiary().setFalse(SupportedFlags.GC_CAUSE);
                }
            }
            getDiary().setFalse(SupportedFlags.GC_CAUSE);
        } else if (line.contains("promotion failure size ="))
            getDiary().setTrue(SupportedFlags.PRINT_PROMOTION_FAILURE);
        else if (FLS_HEADER.parse(line) != null) {
            getDiary().setTrue(SupportedFlags.PRINT_FLS_STATISTICS);
        }

        //old G1 log file
        //      [GC Worker Start (ms):  12053551.6  12053551.6  12053551.6  12053551.6  12053551.7  12053551.7  12053551.7  12053551.7  12053551.7  12053551.7  12053551.7  12053551.7
        //new G1 log file
        //      [GC Worker Start (ms): Min: 76.3, Avg: 76.3, Max: 76.4, Diff: 0.1]
        if (getDiary().isG1GC()) {
            if (line.startsWith("[GC Worker Start (ms): "))
                if (line.startsWith("[GC Worker Start (ms): Min: "))
                    getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
                else
                    getDiary().setTrue(SupportedFlags.PRE_JDK70_40);
        } else if (line.contains("AdaptiveSizePolicy::")) {
            getDiary().setTrue(SupportedFlags.ADAPTIVE_SIZING);
        }

        //if we've seen a collection and Print Reference GC hasn't been set...
        if (youngCollectionCount > 1 && !getDiary().isStateKnown(SupportedFlags.PRINT_REFERENCE_GC)) {
            getDiary().setFalse(SupportedFlags.PRINT_REFERENCE_GC);
        }

        GCLogTrace gcLogTrace;
        if ((gcLogTrace = MEMORY_SUMMARY_RULE.parse(line)) != null) {
            if (gcLogTrace.next() != null)
                getDiary().setTrue(SupportedFlags.GC_DETAILS);
        }

        // if we've seen a statement than this is false
        if (line.startsWith("{Heap before GC invocations="))
            getDiary().setTrue(SupportedFlags.PRINT_HEAP_AT_GC);
        else if (collectionCount > 1)
            getDiary().setFalse(SupportedFlags.PRINT_HEAP_AT_GC);

        if ((trace = TenuredPatterns.TENURING_SUMMARY.parse(line)) != null) {

            //we have seen at least one good tenuring summary without an age breakdown
            if ((tenuringSummary > 0) && (!ageTableDetected)) {
                getDiary().setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.CMS_DEBUG_LEVEL_1);
            } else
                getDiary().setTrue(SupportedFlags.TENURING_DISTRIBUTION);

            // if calculated tenuring threshold == 0 we won't get an age breakdown so delay evaluation
            if (trace.getIntegerGroup(2) > 0)
                tenuringSummary++;

            //If the MaxTenuringThreshold is set to be greater than 15 then we a configuration bug to report on.
            if (trace.getIntegerGroup(3) > 15) {
                maxTenuringThreshold = trace.getIntegerGroup(3);
                getDiary().setTrue(SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
            } else
                getDiary().setFalse(SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
        } else if (TenuredPatterns.TENURING_AGE_BREAKDOWN.parse(line) != null) {
            ageTableDetected = true;
            getDiary().setFalse(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
        } else if (getDiary().isTenuringDistributionKnown() && collectionCount > 1 && youngCollectionCount > 1) {
            getDiary().setFalse(SupportedFlags.TENURING_DISTRIBUTION);
        }

        if (line.contains("G1Ergonomics")) {
            if (line.contains("CSet Construction") || line.contains("Heap Sizing")) {
                collectionCount++;
                getDiary().setTrue(SupportedFlags.G1GC, SupportedFlags.ADAPTIVE_SIZING);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
            }

        }

        if (REFERENCE_PROCESSING_BLOCK.parse(line) != null)
            getDiary().setTrue(SupportedFlags.PRINT_REFERENCE_GC);

        if (line.startsWith("Concurrent RS processed")) {
            collectionCount++;
            getDiary().setTrue(SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
            getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
        } else if (line.contains(" (cardTable: ")) {
            getDiary().setTrue(SupportedFlags.CMS_DEBUG_LEVEL_1);
        }

        if (collectionCount > 1) {
            if (!getDiary().isCMSDebugLevel1Known()) {
                getDiary().setFalse(SupportedFlags.CMS_DEBUG_LEVEL_1);
            }

            if (!getDiary().isAdaptiveSizingKnown()) {
                getDiary().setFalse(SupportedFlags.ADAPTIVE_SIZING);
            }

            if (!getDiary().isRSetStatsKnown())
                getDiary().setFalse(SupportedFlags.RSET_STATS);
        }
    }

    /*
     * this method is a poster child against enum....
     *
     * The following table bit encodes all of the Collector flags. The flags cannot be acted upon until all of the
     * flags have been seen as not all combinations of flags are valid and certain combinations will yield unexpected
     * configurations. The method evaluateGCLogFlags() will examine the flags and set the corresponding values in the
     * deriveConfiguration.
     *
     * GC Flag                  Bit Value
     * ----------------------------------
     * +UseSerialGC	            0x0001
     * +UseParallelGC	        0x0002
     * +UseParallelOldGC	    0x0004
     * +ParNew	                0x0008
     * +ConcMarkSweepGC     	0x0010
     * +CMSIncrementialModea	0x0020
     * +G1GC	                0x0040     0x1840
     * -UseSerialGC	            0x0100
     * -UseParallelGC	        0x0200
     * -UseParallelOldGC	    0x0400
     * -ParNew	                0x0800
     * -ConcMarkSweepGC	        0x1000
     * -CMSIncrementialMode	    0x2000
     * -G1GC	                0x4000
     *
     * @param commandLineFlags
     */
    private void evaluateCommandLineFlags(String[] commandLineFlags) {
        for (String rawFlag : commandLineFlags) {
            String flag = processRawFlag(rawFlag);
            boolean flagTurnedOn = isSetToTrue(rawFlag);
            CommandLineFlag supportedFlag = CommandLineFlag.fromString(flag);
            if (supportedFlag != null) {
                switch (supportedFlag) {
                    case PrintGCApplicationStoppedTime:
                        getDiary().setState(SupportedFlags.APPLICATION_STOPPED_TIME, flagTurnedOn);
                        break;

                    case PrintGCApplicationConcurrentTime:
                        getDiary().setState(SupportedFlags.APPLICATION_CONCURRENT_TIME, flagTurnedOn);
                        break;

                    case PrintGCTimeStamps:
                        break;

                    case PrintGCDetails:
                        getDiary().setState(SupportedFlags.GC_DETAILS, flagTurnedOn);
                        if (flagTurnedOn && (getDiary().isJDK80() || getDiary().isUnifiedLogging()))
                            getDiary().setTrue(SupportedFlags.GC_CAUSE);
                        break;

                    case PrintGCCause:
                        getDiary().setState(SupportedFlags.GC_CAUSE, flagTurnedOn);
                        break;

                    case PrintTenuringDistribution:
                        getDiary().setState(SupportedFlags.TENURING_DISTRIBUTION, flagTurnedOn);
                        break;

                    case PrintAdaptiveSizePolicy:
                        getDiary().setState(SupportedFlags.ADAPTIVE_SIZING, flagTurnedOn);
                        break;

                    case PrintReferenceGC:
                        getDiary().setState(SupportedFlags.PRINT_REFERENCE_GC, flagTurnedOn);
                        break;

                    case PrintHeapAtGC:
                        getDiary().setState(SupportedFlags.PRINT_HEAP_AT_GC, flagTurnedOn);
                        break;

                    case PrintPromotionFailure:
                        getDiary().setTrue(SupportedFlags.PRINT_PROMOTION_FAILURE);

                    case PrintFLSStatistics:
                        getDiary().setTrue(SupportedFlags.PRINT_FLS_STATISTICS);

                    default:
                        //shouldn't be able to get here....
                }
            } else {

                GarbageCollectorFlag gcFlag = GarbageCollectorFlag.fromString(flag);
                if (gcFlag != null) {
                    switch (gcFlag) {
                        case UseSerialGC:
                            setGCFlags |= (flagTurnedOn) ? 0x1 : 0x100;
                            break;

                        case UseParallelGC:
                            setGCFlags |= (flagTurnedOn) ? 0x2 : 0x200;
                            break;

                        case UseParallelOldGC:
                            setGCFlags |= (flagTurnedOn) ? 0x4 : 0x400;
                            break;

                        case UseParNewGC:
                            setGCFlags |= (flagTurnedOn) ? 0x8 : 0x800;
                            break;

                        case UseConcMarkSweepGC:
                            setGCFlags |= (flagTurnedOn) ? 0x10 : 0x1000;
                            break;

                        case CMSIncrementialMode:
                            setGCFlags |= (flagTurnedOn) ? 0x20 : 0;
                            break;

                        case UseG1GC:
                            setGCFlags |= (flagTurnedOn) ? 0x40 : 0x4000;
                            break;

                        default:
                            // shouldn't be able to get here!!!
                    }
                }
            }
        }

        evaluateGCLogFlags();
        getDiary().setFalse(SupportedFlags.APPLICATION_STOPPED_TIME, SupportedFlags.APPLICATION_CONCURRENT_TIME, SupportedFlags.GC_DETAILS,
                SupportedFlags.GC_CAUSE, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.PRINT_REFERENCE_GC,
                SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.PRINT_PROMOTION_FAILURE, SupportedFlags.PRINT_FLS_STATISTICS);
    }


    private String processRawFlag(String rawFlag) {
        String processedFlag = "";
        if (rawFlag.startsWith("-XX:")) {
            if (rawFlag.charAt(4) == '+' || rawFlag.charAt(4) == '-')
                processedFlag = rawFlag.substring(5);
            else
                processedFlag = rawFlag.substring(4);
            int equalsPosition = processedFlag.indexOf("=");
            if (equalsPosition > 0)
                processedFlag = processedFlag.substring(0, equalsPosition);
        }

        return processedFlag;
    }

    private boolean isSetToTrue(String flag) {
        return (flag.startsWith("-XX:+"));
    }

    //"Java HotSpot(TM) 64-Bit Server VM (25.40-b25) for bsd-amd64 JRE (1.8.0_40-b25), built on Feb 10 2015 21:07:25 by \"java_re\" with gcc 4.2.1 (Based on Apple Inc. build 5658) (LLVM build 2336.11.00)",
    private static final Pattern VERSION = Pattern.compile("(1)\\.([6-9])\\.0(_\\d*)?");

    private void parseJVMVersion(String versionString) {
        Matcher matcher = VERSION.matcher(versionString);
        if (matcher.find()) {
            switch (matcher.group(2).charAt(0)) {
                case '7':
                    getDiary().setTrue(SupportedFlags.JDK70);
                    getDiary().setFalse(SupportedFlags.JDK80);
                    if (matcher.group(3) == null)
                        getDiary().setTrue(SupportedFlags.PRE_JDK70_40);
                    try {
                        int minorVersion = Integer.parseInt(matcher.group(3).substring(1));
                        if (minorVersion < 41)
                            getDiary().setTrue(SupportedFlags.PRE_JDK70_40);
                        else
                            getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
                    } catch (NumberFormatException nfe) {
                        getDiary().setTrue(SupportedFlags.PRE_JDK70_40);
                    }
                    break;
                case '8':
                    getDiary().setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
                    getDiary().setTrue(SupportedFlags.JDK80, SupportedFlags.GC_CAUSE); // doesn't matter so much but may only be true for later versions of 8
                    break;
                case '9':
                    getDiary().setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK80);
                    break;
                default:
            }
        }
    }

    /*
            Flag Combinations                                       Total   Configured         Notes
                                                                    Value   Combination
            +UseSerialGC                                        	1      	Serial
            -UserSerialGC                                       	256 	PSYoung/PSOldGen
            +UseParNewGC                                          	8   	ParNew/Serial
            -UseParNewGC                                          	2050	PSYoung/PSOldGen	+UseParallelGC is added to the log
            +UseConcMarkSweepGC                                 	16	    ParNew/CMS
            -UseConcMarkSweepGC                                     4096    PSYoung/PSOldGen
            +UseParNewGC +UseConcMarkSweepGC                        24	    ParNew/CMS
            -UseParNewGC +UseConcMarkSweepGC                      	2064	DefNew/CMS	        deprecated combination
            +UseParNewGC -UseConcMarkSweepGC                      	4104	ParNew/Serial	    deprecated combination
            -UseParNewGC -UseConcMarkSweepGC                      	6146	PSYoung/PSOldGen	+UseParallelGC is added to the log
            +UseConcMarkSweepGC +CMSIncrementialMode             	48	    ParNew/iCMS	        ICMS is deprecated
            +UseParNewGC +UseConcMarkSweepGC +CMSIncrementialMode	56	    ParNew/iCMS	        ICMS is deprecated
            -UseParNewGC +UseConcMarkSweepGC +CMSIncrementialMode 	2096	DefNew/iCMS	        iCMS is deprecated
            -UseSerialGC +UseConcMarkSweepGC                    	272	    ParNew/CMS	        UseParNewGC is added in log
            -UseSerialGC -UseConcMarkSweepGC                    	4354	PSYoung/PSOldGen	+UseParallelGC is added to the log
            +UseParallelGC                                      	2	    PSYoung/PSOldGen	ParallelOld is turned on
            -UseParallelGC                                      	2	    PSYoung/PSOldGen	- is reversed to + in log
            +UseParallelOldGC                                   	4	    PSYoungGen/PSOldGen	parallel in tenured
            -UseParallelOldGC                                   	1024	PSYoungGen/PSOlGen	parallel is turned off in tenured
            +UseParallelGC +UseParallelOldGC                    	6	    PSYoung/PSOldGen	parallel default
            +UseParallelGC -UseParallelOldGC                    	1026	PSYoung/PSOldGen	serial Full
            -UseParallelGC +UseParallelOldGC                    	518	    PSYoung/PSOldGen	+UseParallelGC is added to the log
            -UseParallelGC -UseParallelOldGC                    	1026	PSYoung/PSOldGen	is changed to +UseParallelGC and uses serial full
            -UseSerialGC +UseParallelGC                         	258	    PSYoung/PSOldGen	same as default
            -UseSerialGC +UseParallelOldGC                      	260	    PSYoungGen/PSOldGen	same as default
            +UseG1GC                                            	64	    G1GC
            -UseG1GC                                            	16384	PSYoung/PSOldGen	same as default
            -UseConcMarkSweepGC +UseG1GC -UseParNewGC               6210    G1GC  4096 + 64 + 2050

    */

    private void evaluateGCLogFlags() {

        switch (setGCFlags) {

            // PSYoung/PSOldGen
            case 0:
            case 2:
            case 4:
            case 6:
            case 256:
            case 258:
            case 260:
            case 518:
            case 2050:
            case 4354:
            case 6146:
            case 16384:
                getDiary().setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 1024: // parallel/serial
            case 1026:
                getDiary().setTrue(SupportedFlags.PARALLELGC);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 1: // DefNew/Serial
                getDiary().setTrue(SupportedFlags.DEFNEW, SupportedFlags.SERIAL);
                getDiary().setFalse(SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 8: //ParNew/Serial Deprecated
            case 4101:
                getDiary().setTrue(SupportedFlags.PARNEW, SupportedFlags.SERIAL);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 16: //ParNew/CMS
            case 24:
            case 272:
                getDiary().setTrue(SupportedFlags.PARNEW, SupportedFlags.CMS);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                break;

            case 2064: //DefNew/CMS Deprecated
                getDiary().setTrue(SupportedFlags.DEFNEW, SupportedFlags.CMS);
                getDiary().setFalse(SupportedFlags.PARNEW, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                break;

            case 48: //ParNew/iCMS
            case 56:
                getDiary().setTrue(SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                break;

            case 2096: //DefNew/iCMS
                getDiary().setTrue(SupportedFlags.DEFNEW, SupportedFlags.CMS, SupportedFlags.ICMS);
                getDiary().setFalse(SupportedFlags.PARNEW, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                break;

            case 6208:
            case 64: //G1GC
                getDiary().setTrue(SupportedFlags.G1GC);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 4096:
                getDiary().setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
                getDiary().setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 4160:
                getDiary().setTrue(SupportedFlags.G1GC);
                getDiary().setFalse(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            default:
                LOGGER.severe("Illegal internal state: GCToolKit was unable to properly identify this log. Results will be corrupted.");
        }
    }

    private void setGCCause(String gcCause) {
        if (gcCause == null && getDiary().isPrintGCDetails()) {
            getDiary().setFalse(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
        } else if (gcCause != null) {
            if (GCCauses.get(gcCause) == GCCause.GCCAUSE_NOT_SET) {
                if (getDiary().isPrintGCDetailsKnown() && getDiary().isPrintGCDetails()) {
                    getDiary().setFalse(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
                    getDiary().setTrue(SupportedFlags.JDK70);
                }
            } else {
                if (gcCause.contains("System.gc()") || !gcCause.contains("System")) {
                    getDiary().setTrue(SupportedFlags.GC_CAUSE);
                    getDiary().setFalse(SupportedFlags.PRE_JDK70_40);
                } else {
                    getDiary().setTrue(SupportedFlags.PRE_JDK70_40);
                    getDiary().setTrue(SupportedFlags.JDK70);
                }
            }
        }
    }
}