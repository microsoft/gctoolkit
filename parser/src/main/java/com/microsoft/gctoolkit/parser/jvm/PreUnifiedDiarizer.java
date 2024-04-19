// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.jvm;


import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GCCauses;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.jvm.SupportedFlags;
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
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.gctoolkit.parser.CMSPatterns.FLS_HEADER;
import static com.microsoft.gctoolkit.parser.SharedPatterns.MEMORY_SUMMARY_RULE;
import static com.microsoft.gctoolkit.parser.SharedPatterns.META_SPACE_RECORD;

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

//SimplePatterns, CMSPatterns, ParallelPatterns, G1GCPatterns,
public class PreUnifiedDiarizer implements Diarizer {

    private static final Logger LOGGER = Logger.getLogger(PreUnifiedDiarizer.class.getName());

    private int lineCount = MAXIMUM_LINES_TO_EXAMINE;

    private static final GCParseRule TENURED_BLOCK = new GCParseRule("TENURED_BLOCK", "\\[Tenured: \\d+K->\\d+K\\(\\d+K\\), \\d+[.|,]\\d{7} secs\\]");
    private static final GCParseRule PREFIX = new GCParseRule("PREFIX", PreUnifiedTokens.GC_PREFIX);
    private static final GCParseRule FULL_PREFIX = new GCParseRule("FULL_PREFIX", PreUnifiedTokens.FULL_GC_PREFIX);
    private static final GCParseRule G1GC_PREFIX = new GCParseRule("G1GC_PREFIX", G1GCTokens.G1GC_PREFIX);
    private static final GCParseRule REFERENCE_PROCESSING_BLOCK = new GCParseRule("REFERENCE_PROCESSING_BLOCK", PreUnifiedTokens.REFERENCE_RECORDS);
    private static final Pattern excludeG1Ergonomics = Pattern.compile("^\\d+(\\.|,)\\d+: \\[G1Ergonomics");


    private final Diary diary;

    public PreUnifiedDiarizer() {}

    @Override
    public Diary getDiary() {
        fillInKnowns();
        return diary;
    }

    @Override
    public boolean isUnified() {
        return false;
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
        diary = new Diary();
        diary.setFalse(SupportedFlags.UNIFIED_LOGGING, SupportedFlags.ZGC, SupportedFlags.SHENANDOAH);
    }

    @Override
    public String getCommandLine() {
        // TODO #149 Extract command line from PreUnifiedJVMConfiguration log file
        return "";
    }

    @Override
    public int getMaxTenuringThreshold() {
        return maxTenuringThreshold;
    }

    @Override
    public boolean hasJVMEvents() {
        return diary.isApplicationStoppedTime() ||
                diary.isApplicationRunningTime() ||
                diary.isApplicationRunningTime() ||
                diary.isTLABData();
    }

    private boolean versionIsKnown() {
        return (diary.isStateKnown(SupportedFlags.JDK80) && diary.isStateKnown(SupportedFlags.JDK70)) && diary.isStateKnown(SupportedFlags.PRE_JDK70_40);
    }

    @Override
    public boolean completed() {
        return diary.isComplete() || lineCount < 1 || (simpleCMSCycleDetected && (simpleCMSCycleDetected || simpleFullGCDetected));
    }

    // Things that if we've not seen them, we know that they are false.
    private void fillInKnowns() {
        if (simpleCMSCycleDetected) {
            diary.setTrue(SupportedFlags.CMS);
            diary.setFalse(SupportedFlags.SERIAL, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.ICMS, SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.GC_CAUSE, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.RSET_STATS, SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK80);
            // if we have age table or if no tenuring distribution, assume default ParNew/CMS
            if (youngCollectionCount == 0 || ageTableDetected || !diary.isTenuringDistribution()) {
                diary.setTrue(SupportedFlags.PARNEW);
                diary.setFalse(SupportedFlags.DEFNEW);
            } else { //CMS with no age table implies DEFNEW/CMS
                diary.setTrue(SupportedFlags.DEFNEW);
                diary.setFalse(SupportedFlags.PARNEW);
            }
        } else if (simpleFullGCDetected) {
            if (ageTableDetected) {
                diary.setTrue(SupportedFlags.PARNEW, SupportedFlags.SERIAL);
            } else { //at this point we can't tell if it's serial or parallel so assume defaults
                diary.setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
            }
            diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.SERIAL, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.GC_CAUSE, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.RSET_STATS, SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK80);
        }

        //Not much information here, assume defaults
        else if (simpleParallelOrParNewDetected) {
            if (ageTableDetected) {
                diary.setTrue(SupportedFlags.PARNEW, SupportedFlags.CMS);
                diary.setFalse(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
            } else {
                diary.setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
                diary.setFalse(SupportedFlags.PARNEW, SupportedFlags.CMS);
            }
            diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.SERIAL, SupportedFlags.ICMS, SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.GC_CAUSE, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.RSET_STATS, SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK80);
        }

        diary.setFalse(SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION, SupportedFlags.PRINT_PROMOTION_FAILURE, SupportedFlags.PRINT_FLS_STATISTICS);
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
            if (trace != null) {
                timeOfFirstEvent = trace.getDateTimeStamp();
                diary.setTimeOfFirstEvent(timeOfFirstEvent);
            }
        }
    }

    private boolean jvmActivityFlag(String line) {

        if (diary.isStateKnown(SupportedFlags.APPLICATION_STOPPED_TIME, SupportedFlags.APPLICATION_CONCURRENT_TIME, SupportedFlags.TLAB_DATA))
            return false;

        if ((JVMPatterns.APPLICATION_STOP_TIME.parse(line) != null) || (JVMPatterns.SIMPLE_APPLICATION_STOP_TIME.parse(line) != null) || JVMPatterns.APPLICATION_STOP_TIME_WITH_STOPPING_TIME.parse(line) != null) {
            diary.setTrue(SupportedFlags.APPLICATION_STOPPED_TIME);
            return true;
        } else if ((JVMPatterns.APPLICATION_TIME.parse(line) != null) || (JVMPatterns.SIMPLE_APPLICATION_TIME.parse(line) != null)) {
            diary.setTrue(SupportedFlags.APPLICATION_CONCURRENT_TIME);
            return true;
        } else if (JVMPatterns.TLAB_CONT.parse(line) != null) {
            diary.setTrue(SupportedFlags.TLAB_DATA);
            return true;
        }
        //This will be reported along size a collection so if we don't see them by 3nd collection....
        // maybe some rubbish lines between collection and log so wait a bit longer than just the reporting of the first collection
        if (collectionCount > 1) {
            diary.setFalse(SupportedFlags.APPLICATION_STOPPED_TIME, SupportedFlags.APPLICATION_CONCURRENT_TIME, SupportedFlags.TLAB_DATA);
        }

        return false;
    }

    private boolean simpleParallelOrParNewDetected = false;
    private boolean simpleFullGCDetected = false;
    private boolean simpleCMSCycleDetected = false;

    //TODO: #148 PSYoung -> PSOldGen, DefNew->PSOldGen, so maybe we need to break up Parallel for generational collections
    private void collector(String line) {

        GCLogTrace trace;

        if ((!diary.isStateKnown(SupportedFlags.PARNEW, SupportedFlags.DEFNEW, SupportedFlags.PARALLELGC, SupportedFlags.G1GC)) ||
                !diary.isStateKnown(SupportedFlags.GC_DETAILS) || collectionCount < 3) {

            if (line.contains("[PSYoungGen:")) {
                diary.setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION, SupportedFlags.RSET_STATS);
                collectionCount++;
                youngCollectionCount++;

                if ((trace = ParallelPatterns.PSYOUNGGEN.parse(line)) != null) {
                    diary.setTrue(SupportedFlags.GC_DETAILS);
                    diary.setFalse(SupportedFlags.TENURING_DISTRIBUTION);
                    diary.setFalse(SupportedFlags.PRINT_HEAP_AT_GC);
                    setGCCause(trace.getGroup(6));

                } else if (ParallelPatterns.PS_DETAILS_WITH_TENURING.parse(line) != null) {
                    diary.setTrue(SupportedFlags.GC_DETAILS);
                    diary.setFalse(SupportedFlags.PRINT_HEAP_AT_GC);
                }
            } else if (line.contains("ParNew")) {
                diary.setTrue(SupportedFlags.PARNEW, SupportedFlags.CMS);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                collectionCount++;
                youngCollectionCount++;

                if ((trace = CMSPatterns.PARNEW.parse(line)) != null) {
                    diary.setTrue(SupportedFlags.GC_DETAILS);
                    diary.setFalse(SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                    setGCCause(trace.getGroup(7));
                } else if ((trace = CMSPatterns.PARNEW_TENURING.parse(line)) != null) {
                    diary.setTrue(SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION);
                    diary.setFalse(SupportedFlags.CMS_DEBUG_LEVEL_1);
                    setGCCause(trace.getGroup(6));
                } else if ((trace = SimplePatterns.PARNEW_NO_DETAILS.parse(line)) != null) {
                    diary.setFalse(SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                    setGCCause(trace.getGroup(6));
                } else if ((trace = SimplePatterns.PARNEW_START.parse(line)) != null) {
                    diary.setFalse(SupportedFlags.GC_DETAILS, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.PRINT_HEAP_AT_GC);
                    setGCCause(trace.groupCount() > 5 ? trace.getGroup(6) : null);
                } else if ((trace = CMSPatterns.PARNEW_REFERENCE_SPLIT.parse(line)) != null) {
                    diary.setTrue(SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.PRINT_REFERENCE_GC);
                    setGCCause(trace.getGroup(6));
                }
            } else if ((trace = SerialPatterns.DEFNEW.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                diary.setTrue(SupportedFlags.DEFNEW, SupportedFlags.GC_DETAILS);
                diary.setFalse(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.PARNEW, SupportedFlags.G1GC, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.RSET_STATS, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                setGCCause(trace.getGroup(6));
            } else if ((trace = SerialPatterns.DEFNEW_TENURING.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                diary.setTrue(SupportedFlags.DEFNEW, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION);
                diary.setFalse(SupportedFlags.PARNEW, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                setGCCause(trace.getGroup(6));
            } else if (SimplePatterns.PARNEW_NO_DETAILS.parse(line) != null) {
                collectionCount++;
                youngCollectionCount++;
                diary.setTrue(SupportedFlags.PARNEW);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.GC_CAUSE, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.ADAPTIVE_SIZING, SupportedFlags.PRINT_HEAP_AT_GC, SupportedFlags.RSET_STATS, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
            } else if (SimplePatterns.YOUNG_NO_DETAILS.parse(line) != null) {
                collectionCount++;
                youngCollectionCount++;
                simpleParallelOrParNewDetected = true;
                diary.setFalse(SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION,
                        SupportedFlags.GC_CAUSE, SupportedFlags.JDK80, SupportedFlags.PRINT_HEAP_AT_GC,
                        SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION, SupportedFlags.ADAPTIVE_SIZING);
            } else if (SimplePatterns.FULL_NO_GC_DETAILS.parse(line) != null) {
                collectionCount++;
                simpleFullGCDetected = true;
                simpleParallelOrParNewDetected = true;
            } else if ((trace = SimplePatterns.GC_START.parse(line)) != null) {
                collectionCount++;
                simpleParallelOrParNewDetected = true;
                youngCollectionCount++;
                diary.setFalse(SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                setGCCause(trace.getGroup(6));
            } else if (SimplePatterns.CMS_NO_DETAILS.parse(line) != null) {
                // could be parallel or CMS.. look for Full GC but even that may be a trick
                collectionCount++;
                simpleParallelOrParNewDetected = true;
                simpleCMSCycleDetected = true;
                firstCMSCycle = true;
            } else if ((trace = G1GCPatterns.G1_YOUNG_SPLIT_START.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
                diary.setTrue(SupportedFlags.G1GC);
                diary.setTrue(SupportedFlags.GC_DETAILS);
                if (trace.gcCause() == GCCause.GCCAUSE_NOT_SET) {
                    diary.setFalse(SupportedFlags.GC_CAUSE);
                } else if (trace.gcCause() == GCCause.METADATA_GENERATION_THRESHOLD) {
                    diary.setTrue(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
                    diary.setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
                } else if ((trace.gcCause() == GCCause.G1_EVACUATION_PAUSE) || (trace.gcCause() == GCCause.G1_HUMONGOUS_ALLOCATION)) {
                    diary.setTrue(SupportedFlags.GC_CAUSE);
                    diary.setFalse(SupportedFlags.PRE_JDK70_40);
                }
            } else if ((trace = G1GCPatterns.G1_DETAILS.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                diary.setTrue(SupportedFlags.G1GC, SupportedFlags.GC_DETAILS);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                if (trace.gcCause() == GCCause.GCCAUSE_NOT_SET) {
                    diary.setFalse(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
                    diary.setTrue(SupportedFlags.JDK70);
                } else {
                    diary.setFalse(SupportedFlags.GC_CAUSE);
                    diary.setFalse(SupportedFlags.PRE_JDK70_40);
                }
            } else if ((trace = G1GCPatterns.YOUNG.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.GC_DETAILS, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                diary.setTrue(SupportedFlags.G1GC);
                checkForGCCause(trace);

                //2014-10-21T11:49:08.954-0500: 12053.551: [GC pause (young)12054.116: [SoftReference, 0 refs, 0.0000070 secs]12054.116: [WeakReference, 234 refs, 0.0000640 secs]12054.116: [FinalReference, 3805 refs, 0.0034010 secs]12054.119: [PhantomReference, 9 refs, 0.0000040 secs]12054.119: [JNI Weak Reference, 0.0001960 secs], 0.58191800 secs]
            } else if ((trace = G1GCPatterns.G1_DETAILS_REFERENCE_GC.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.TENURING_DISTRIBUTION, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                diary.setTrue(SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.PRINT_REFERENCE_GC);
                if (trace.getGroup(6) != null)
                    diary.setTrue(SupportedFlags.GC_CAUSE);
                else
                    diary.setFalse(SupportedFlags.GC_CAUSE);
            } else if (G1GCPatterns.G1_INITIAL_MARK.parse(line) != null) {
                collectionCount++;
                youngCollectionCount++;
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
                diary.setTrue(SupportedFlags.G1GC, SupportedFlags.GC_DETAILS, SupportedFlags.GC_CAUSE, SupportedFlags.TENURING_DISTRIBUTION);
            }
        }

        if (diary.isTrue(SupportedFlags.ADAPTIVE_SIZING)) {
            if ((trace = G1GCPatterns.YOUNG_SPLIT_BY_G1ERGONOMICS.parse(line)) != null) {
                collectionCount++;
                youngCollectionCount++;
                diary.setTrue(SupportedFlags.G1GC, SupportedFlags.GC_DETAILS);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
                checkForGCCause(trace);
            }
        }

        //This has been verified true as of 1.7.0_51. Check later versions to make sure it hasn't been back ported
        if (ParallelPatterns.PS_FULL_GC_PERM.parse(line) != null) {
            collectionCount++;
            diary.setTrue(SupportedFlags.JDK70);
            diary.setFalse(SupportedFlags.JDK80);
            if (line.contains(" [PSYoungGen: "))
                diary.setTrue(SupportedFlags.PARALLELGC);
            diary.setTrue(SupportedFlags.PARALLELOLDGC, SupportedFlags.GC_DETAILS);
            diary.setFalse(SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.SERIAL, SupportedFlags.G1GC);

            diary.setFalse(SupportedFlags.CMS_DEBUG_LEVEL_1);
            diary.setFalse(SupportedFlags.PRINT_HEAP_AT_GC);

        }

        //This has been verified true as of 1.7.0_51. Check later versions to make sure it hasn't been back ported
        else if (ParallelPatterns.PS_FULL_GC_META.parse(line) != null) {
            collectionCount++;
            diary.setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
            diary.setTrue(SupportedFlags.JDK80);
            if (line.contains(" [PSYoungGen: "))
                diary.setTrue(SupportedFlags.PARALLELGC);
            diary.setTrue(SupportedFlags.PARALLELOLDGC, SupportedFlags.GC_DETAILS);
            diary.setFalse(SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.SERIAL, SupportedFlags.ICMS, SupportedFlags.G1GC);

            diary.setFalse(SupportedFlags.CMS_DEBUG_LEVEL_1);
            diary.setFalse(SupportedFlags.PRINT_HEAP_AT_GC);

        } else if (line.contains("CMS-initial-mark")) {
            collectionCount++;
            diary.setFalse(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC);
            diary.setTrue(SupportedFlags.CMS, SupportedFlags.GC_DETAILS);
        }

        //todo: this rule is in the wrong place
        else if (CMSPatterns.SERIAL_FULL.parse(line) != null) {
            collectionCount++;
            diary.setFalse(SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELOLDGC, SupportedFlags.G1GC, SupportedFlags.CMS_DEBUG_LEVEL_1);
            diary.setTrue(SupportedFlags.SERIAL, SupportedFlags.GC_DETAILS);
            //todo: private final int SupportedFlags.GC_CAUSE = 12;
            diary.setFalse(SupportedFlags.CMS_DEBUG_LEVEL_1);
            if (line.contains("Metaspace")) {
                diary.setFalse(SupportedFlags.JDK70);
                diary.setTrue(SupportedFlags.JDK80, SupportedFlags.GC_CAUSE);
            } else if ( line.contains("Perm")) { // todo: maybe look for GC_CAUSE in JDK 7???
                diary.setFalse(SupportedFlags.JDK80);
            }
            if (line.contains("Tenured")) {
                diary.setFalse(SupportedFlags.CMS, SupportedFlags.ICMS);
                diary.setTrue(SupportedFlags.SERIAL);
            } else if (line.contains("CMS")) {
                diary.setFalse(SupportedFlags.SERIAL);
                diary.setTrue(SupportedFlags.CMS);
            }
        } else if (CMSPatterns.SERIAL_FULL.parse(line) != null) {
            collectionCount++;
            diary.setFalse(SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELOLDGC, SupportedFlags.G1GC, SupportedFlags.CMS_DEBUG_LEVEL_1, SupportedFlags.JDK70);
            diary.setTrue(SupportedFlags.SERIAL, SupportedFlags.GC_DETAILS, SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
        } else if (TENURED_BLOCK.parse(line) != null) {
            collectionCount++;
            diary.setFalse(SupportedFlags.CMS);
            diary.setFalse(SupportedFlags.ICMS);
            diary.setTrue(SupportedFlags.SERIAL);
            diary.setFalse(SupportedFlags.G1GC);
        }

        if (diary.isTrue(SupportedFlags.CMS) && !diary.isStateKnown(SupportedFlags.ICMS)) {
            if (firstCMSCycle) {
                if (line.contains("ParNew") || line.contains("DefNew"))
                    youngCountAfterFirstCMSCycle++;

                if (line.contains("icms_dc"))
                    diary.setTrue(SupportedFlags.ICMS);
                else if (youngCountAfterFirstCMSCycle > 1)
                    diary.setFalse(SupportedFlags.ICMS);
            }
            //The first CMS cycle is needed to kick off iCMS
            if (line.contains("concurrent-reset"))
                firstCMSCycle = true;
        } else if (diary.isTrue(SupportedFlags.G1GC)) {
            if (G1GCPatterns.G1_MEMORY_SUMMARY.parse(line) != null) {
                if (line.contains("Metaspace")) {
                    diary.setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
                    diary.setTrue(SupportedFlags.JDK80);
                } else {
                    diary.setTrue(SupportedFlags.JDK70);
                    diary.setFalse(SupportedFlags.JDK80);
                }
            }
        }
    }

    private void checkForGCCause(GCLogTrace trace) {
        if (trace.gcCause() == GCCause.METADATA_GENERATION_THRESHOLD) {
            diary.setTrue(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
            diary.setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
        } else if ((trace.gcCause() == GCCause.G1_EVACUATION_PAUSE) || (trace.gcCause() == GCCause.G1_HUMONGOUS_ALLOCATION)) {
            diary.setTrue(SupportedFlags.GC_CAUSE);
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

            switch (value) {
                case "CMS Perm":
                case "PS Perm":
                case "Perm":
                case "PSPermGen":
                    diary.setTrue(SupportedFlags.JDK70);
                    diary.setFalse(SupportedFlags.JDK80);
                    break;
                case "Metaspace":
                    diary.setTrue(SupportedFlags.JDK80);
                    diary.setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
                    break;
            }
        } else if (META_SPACE_RECORD.parse(line) != null) {
            diary.setTrue(SupportedFlags.JDK80);
            diary.setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
        }

        // maybe we'll get lucky, app server often call System.gc() after startup.
        if (line.contains("(System)")) {
            diary.setTrue(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
            diary.setFalse(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
        } else if (line.contains("(System.gc()")) {
            diary.setFalse(SupportedFlags.PRE_JDK70_40);
            diary.setTrue(SupportedFlags.GC_CAUSE);
        } else if (diary.isGenerationalKnown() && diary.isGenerational()) {
            if ((trace = PREFIX.parse(line)) != null) {
                if ((trace.getGroup(6) == null) && diary.isTrue(SupportedFlags.GC_DETAILS)) {
                    diary.setTrue(SupportedFlags.JDK70);
                    diary.setFalse(SupportedFlags.JDK80, SupportedFlags.GC_CAUSE);
                } else if (trace.gcCause() != GCCause.GCCAUSE_NOT_SET) {
                    diary.setTrue(SupportedFlags.GC_CAUSE);
                    diary.setFalse(SupportedFlags.PRE_JDK70_40);
                }
            } else if ((trace = FULL_PREFIX.parse(line)) != null) {
                if ((trace.getGroup(6) == null) && diary.isTrue(SupportedFlags.GC_DETAILS)) {
                    diary.setTrue(SupportedFlags.JDK70);
                    diary.setFalse(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
                } else if (trace.gcCause() != GCCause.GCCAUSE_NOT_SET) {
                    diary.setTrue(SupportedFlags.GC_CAUSE);
                    diary.setFalse(SupportedFlags.PRE_JDK70_40);
                }
            }

        } else if (diary.isG1GCKnown() && diary.isG1GC()) {
            if ((trace = G1GC_PREFIX.parse(line)) != null) {
                if (diary.isTrue(SupportedFlags.GC_DETAILS) && (trace.gcCause() == GCCause.GCCAUSE_NOT_SET)) {
                    diary.setTrue(SupportedFlags.JDK70);
                    diary.setFalse(SupportedFlags.JDK80);
                } else { //we can't say much else unless we look for 8.0 specific details
                    diary.setFalse(SupportedFlags.PRE_JDK70_40);
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
            String cause = trace.getGroup(6);
            if (cause != null) {
                diary.setTrue(SupportedFlags.GC_CAUSE);
                if ("(System)".equals(cause)) {
                    diary.setFalse(SupportedFlags.JDK80);
                    diary.setTrue(SupportedFlags.JDK70);
                    diary.setTrue(SupportedFlags.PRE_JDK70_40);
                } else if ("(System.gc())".equals(cause)) {
                    diary.setFalse(SupportedFlags.PRE_JDK70_40);
                } else {
                    diary.setFalse(SupportedFlags.PRE_JDK70_40);
                }
            } else {
                if ((trace = G1GC_PREFIX.parse(line)) != null) {
                    cause = trace.getGroup(6);
                    if (cause == null)
                        diary.setTrue(SupportedFlags.PRE_JDK70_40);
                    else
                        diary.setFalse(SupportedFlags.PRE_JDK70_40);
                } else {
                    diary.setFalse(SupportedFlags.GC_CAUSE);
                }
            }
            diary.setFalse(SupportedFlags.GC_CAUSE);
        } else if (line.contains("promotion failure size ="))
            diary.setTrue(SupportedFlags.PRINT_PROMOTION_FAILURE);
        else if (FLS_HEADER.parse(line) != null) {
            diary.setTrue(SupportedFlags.PRINT_FLS_STATISTICS);
        }

        //old G1 log file
        //      [GC Worker Start (ms):  12053551.6  12053551.6  12053551.6  12053551.6  12053551.7  12053551.7  12053551.7  12053551.7  12053551.7  12053551.7  12053551.7  12053551.7
        //new G1 log file
        //      [GC Worker Start (ms): Min: 76.3, Avg: 76.3, Max: 76.4, Diff: 0.1]
        if (diary.isG1GC()) {
            if (line.startsWith("[GC Worker Start (ms): "))
                if (line.startsWith("[GC Worker Start (ms): Min: "))
                    diary.setFalse(SupportedFlags.PRE_JDK70_40);
                else
                    diary.setTrue(SupportedFlags.PRE_JDK70_40);
        } else if (line.contains("AdaptiveSizePolicy::")) {
            diary.setTrue(SupportedFlags.ADAPTIVE_SIZING);
        }

        //if we've seen a collection and Print Reference GC hasn't been set...
        if (youngCollectionCount > 1 && !diary.isStateKnown(SupportedFlags.PRINT_REFERENCE_GC)) {
            diary.setFalse(SupportedFlags.PRINT_REFERENCE_GC);
        }

        GCLogTrace gcLogTrace;
        if ((gcLogTrace = MEMORY_SUMMARY_RULE.parse(line)) != null) {
            if (gcLogTrace.next() != null)
                diary.setTrue(SupportedFlags.GC_DETAILS);
        }

        // if we've seen a statement than this is false
        if (line.startsWith("{Heap before GC invocations="))
            diary.setTrue(SupportedFlags.PRINT_HEAP_AT_GC);
        else if (collectionCount > 1)
            diary.setFalse(SupportedFlags.PRINT_HEAP_AT_GC);

        if ((trace = TenuredPatterns.TENURING_SUMMARY.parse(line)) != null) {

            //we have seen at least one good tenuring summary without an age breakdown
            if ((tenuringSummary > 0) && (!ageTableDetected)) {
                diary.setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.CMS_DEBUG_LEVEL_1);
            } else
                diary.setTrue(SupportedFlags.TENURING_DISTRIBUTION);

            // if calculated tenuring threshold == 0 we won't get an age breakdown so delay evaluation
            if (trace.getIntegerGroup(2) > 0)
                tenuringSummary++;

            //If the MaxTenuringThreshold is set to be greater than 15 then we a configuration bug to report on.
            if (trace.getIntegerGroup(3) > 15) {
                maxTenuringThreshold = trace.getIntegerGroup(3);
                diary.setTrue(SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
            } else
                diary.setFalse(SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
        } else if (TenuredPatterns.TENURING_AGE_BREAKDOWN.parse(line) != null) {
            ageTableDetected = true;
            diary.setFalse(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
        } else if (diary.isTenuringDistributionKnown() && collectionCount > 1 && youngCollectionCount > 1) {
            diary.setFalse(SupportedFlags.TENURING_DISTRIBUTION);
        }

        if (line.contains("G1Ergonomics")) {
            if (line.contains("CSet Construction") || line.contains("Heap Sizing")) {
                collectionCount++;
                diary.setTrue(SupportedFlags.G1GC, SupportedFlags.ADAPTIVE_SIZING);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
            }

        }

        if (REFERENCE_PROCESSING_BLOCK.parse(line) != null)
            diary.setTrue(SupportedFlags.PRINT_REFERENCE_GC);

        if (line.startsWith("Concurrent RS processed")) {
            collectionCount++;
            diary.setTrue(SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
            diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
        } else if (line.contains(" (cardTable: ")) {
            diary.setTrue(SupportedFlags.CMS_DEBUG_LEVEL_1);
        }

        if (collectionCount > 1) {
            if (!diary.isCMSDebugLevel1Known()) {
                diary.setFalse(SupportedFlags.CMS_DEBUG_LEVEL_1);
            }

            if (!diary.isAdaptiveSizingKnown()) {
                diary.setFalse(SupportedFlags.ADAPTIVE_SIZING);
            }

            if (!diary.isRSetStatsKnown())
                diary.setFalse(SupportedFlags.RSET_STATS);
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
                        diary.setState(SupportedFlags.APPLICATION_STOPPED_TIME, flagTurnedOn);
                        break;

                    case PrintGCApplicationConcurrentTime:
                        diary.setState(SupportedFlags.APPLICATION_CONCURRENT_TIME, flagTurnedOn);
                        break;

                    case PrintGCTimeStamps:
                        break;

                    case PrintGCDetails:
                        diary.setState(SupportedFlags.GC_DETAILS, flagTurnedOn);
                        if (flagTurnedOn && (diary.isJDK80() || diary.isUnifiedLogging()))
                            diary.setTrue(SupportedFlags.GC_CAUSE);
                        break;

                    case PrintGCCause:
                        diary.setState(SupportedFlags.GC_CAUSE, flagTurnedOn);
                        break;

                    case PrintTenuringDistribution:
                        diary.setState(SupportedFlags.TENURING_DISTRIBUTION, flagTurnedOn);
                        break;

                    case PrintAdaptiveSizePolicy:
                        diary.setState(SupportedFlags.ADAPTIVE_SIZING, flagTurnedOn);
                        break;

                    case PrintReferenceGC:
                        diary.setState(SupportedFlags.PRINT_REFERENCE_GC, flagTurnedOn);
                        break;

                    case PrintHeapAtGC:
                        diary.setState(SupportedFlags.PRINT_HEAP_AT_GC, flagTurnedOn);
                        break;

                    case PrintPromotionFailure:
                        diary.setTrue(SupportedFlags.PRINT_PROMOTION_FAILURE);

                    case PrintFLSStatistics:
                        diary.setTrue(SupportedFlags.PRINT_FLS_STATISTICS);

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
        diary.setFalse(SupportedFlags.APPLICATION_STOPPED_TIME, SupportedFlags.APPLICATION_CONCURRENT_TIME, SupportedFlags.GC_DETAILS,
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
                    diary.setTrue(SupportedFlags.JDK70);
                    diary.setFalse(SupportedFlags.JDK80);
                    if (matcher.group(3) == null)
                        diary.setTrue(SupportedFlags.PRE_JDK70_40);
                    try {
                        int minorVersion = Integer.parseInt(matcher.group(3).substring(1));
                        if (minorVersion < 41)
                            diary.setTrue(SupportedFlags.PRE_JDK70_40);
                        else
                            diary.setFalse(SupportedFlags.PRE_JDK70_40);
                    } catch (NumberFormatException nfe) {
                        diary.setTrue(SupportedFlags.PRE_JDK70_40);
                    }
                    break;
                case '8':
                    diary.setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40);
                    diary.setTrue(SupportedFlags.JDK80, SupportedFlags.GC_CAUSE); // doesn't matter so much but may only be true for later versions of 8
                    break;
                case '9':
                    diary.setFalse(SupportedFlags.JDK70, SupportedFlags.PRE_JDK70_40, SupportedFlags.JDK80);
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
                diary.setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 1024: // parallel/serial
            case 1026:
                diary.setTrue(SupportedFlags.PARALLELGC);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 1: // DefNew/Serial
                diary.setTrue(SupportedFlags.DEFNEW, SupportedFlags.SERIAL);
                diary.setFalse(SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 8: //ParNew/Serial Deprecated
            case 4101:
                diary.setTrue(SupportedFlags.PARNEW, SupportedFlags.SERIAL);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 16: //ParNew/CMS
            case 24:
            case 272:
                diary.setTrue(SupportedFlags.PARNEW, SupportedFlags.CMS);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                break;

            case 2064: //DefNew/CMS Deprecated
                diary.setTrue(SupportedFlags.DEFNEW, SupportedFlags.CMS);
                diary.setFalse(SupportedFlags.PARNEW, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                break;

            case 48: //ParNew/iCMS
            case 56:
                diary.setTrue(SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                break;

            case 2096: //DefNew/iCMS
                diary.setTrue(SupportedFlags.DEFNEW, SupportedFlags.CMS, SupportedFlags.ICMS);
                diary.setFalse(SupportedFlags.PARNEW, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.G1GC, SupportedFlags.RSET_STATS);
                break;

            case 6208:
            case 64: //G1GC
                diary.setTrue(SupportedFlags.G1GC);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 4096:
                diary.setTrue(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC);
                diary.setFalse(SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.G1GC, SupportedFlags.RSET_STATS, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            case 4160:
                diary.setTrue(SupportedFlags.G1GC);
                diary.setFalse(SupportedFlags.PARALLELGC, SupportedFlags.PARALLELOLDGC, SupportedFlags.DEFNEW, SupportedFlags.PARNEW, SupportedFlags.CMS, SupportedFlags.ICMS, SupportedFlags.SERIAL, SupportedFlags.CMS_DEBUG_LEVEL_1);
                break;

            default:
                LOGGER.severe("Illegal internal state: GCToolKit was unable to properly identify this log. Results will be corrupted.");
        }
    }

    private void setGCCause(String gcCause) {
        if (gcCause == null && diary.isPrintGCDetails()) {
            diary.setFalse(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
        } else if (gcCause != null) {
            if (GCCauses.get(gcCause) == GCCause.GCCAUSE_NOT_SET) {
                if (diary.isPrintGCDetailsKnown() && diary.isPrintGCDetails()) {
                    diary.setFalse(SupportedFlags.GC_CAUSE, SupportedFlags.JDK80);
                    diary.setTrue(SupportedFlags.JDK70);
                }
            } else {
                if (gcCause.contains("System.gc()") || !gcCause.contains("System")) {
                    diary.setTrue(SupportedFlags.GC_CAUSE);
                    diary.setFalse(SupportedFlags.PRE_JDK70_40);
                } else {
                    diary.setTrue(SupportedFlags.PRE_JDK70_40);
                    diary.setTrue(SupportedFlags.JDK70);
                }
            }
        }
    }
}