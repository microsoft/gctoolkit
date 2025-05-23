// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.ReferenceGCSummary;
import com.microsoft.gctoolkit.event.generational.AbortablePreClean;
import com.microsoft.gctoolkit.event.generational.BinaryTreeDictionary;
import com.microsoft.gctoolkit.event.generational.CMSConcurrentEvent;
import com.microsoft.gctoolkit.event.generational.CMSRemark;
import com.microsoft.gctoolkit.event.generational.ConcurrentMark;
import com.microsoft.gctoolkit.event.generational.ConcurrentModeFailure;
import com.microsoft.gctoolkit.event.generational.ConcurrentModeInterrupted;
import com.microsoft.gctoolkit.event.generational.ConcurrentPreClean;
import com.microsoft.gctoolkit.event.generational.ConcurrentReset;
import com.microsoft.gctoolkit.event.generational.ConcurrentSweep;
import com.microsoft.gctoolkit.event.generational.DefNew;
import com.microsoft.gctoolkit.event.generational.FullGC;
import com.microsoft.gctoolkit.event.generational.GenerationalGCPauseEvent;
import com.microsoft.gctoolkit.event.generational.InitialMark;
import com.microsoft.gctoolkit.event.generational.PSFullGC;
import com.microsoft.gctoolkit.event.generational.PSYoungGen;
import com.microsoft.gctoolkit.event.generational.ParNew;
import com.microsoft.gctoolkit.event.generational.ParNewPromotionFailed;
import com.microsoft.gctoolkit.event.generational.SystemGC;
import com.microsoft.gctoolkit.event.generational.YoungGC;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.parser.collection.MRUQueue;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.gctoolkit.parser.unified.UnifiedG1GCPatterns.WEAK_PROCESSING;

/**
 * Time of GC
 * GCType
 * Collect total heap values
 * Heap before collection
 * Heap after collection
 * Heap configured size
 * total pause time
 * CMS failures
 * System.gc() calls
 */
public class GenerationalHeapParser extends PreUnifiedGCLogParser implements SimplePatterns, ICMSPatterns, SerialPatterns, ParallelPatterns {

    private static final Logger LOGGER = Logger.getLogger(GenerationalHeapParser.class.getName());

    private ParNew parNewForwardReference;
    private GarbageCollectionTypes garbageCollectionTypeForwardReference;
    private GCCause gcCauseForwardReference;
    private DateTimeStamp scavengeTimeStamp;
    private DateTimeStamp fullGCTimeStamp;
    private DateTimeStamp remarkTimeStamp;
    private MemoryPoolSummary youngMemoryPoolSummaryForwardReference;
    private MemoryPoolSummary tenuredForwardReference;
    private MemoryPoolSummary heapForwardReference;
    private MemoryPoolSummary metaSpaceForwardReference;
    private double scavengeDurationForwardReference;
    private ReferenceGCSummary referenceGCForwardReference;
    private CPUSummary scavengeCPUSummaryForwardReference;
    private int[] promotionFailureSizesForwardReference;

    private long totalFreeSpaceForwardReference;
    private long maxChunkSizeForwardReference;
    private int numberOfBlocksForwardReference;
    private long averageBlockSizeForwardReference;
    private int treeHeightForwardReference;

    //Expect Remark
    private boolean expectRemark = false;
    private boolean inConcurrentPhase = false;
    private boolean abortPrecleanDueToTime = false;

    /* Rules not used....
        GCParseRule FULL_PARNEW_CMF_META
        GCParseRule FULL_GC_CMF
        GCParseRule FULL_GC_CARDS_DETAILS
        GCParseRule PARNEW_PROMOTION_FAILED_DETAILS_AFTER
        GCParseRule DUP_CMF
        GCParseRule FULL_PARNEW_CMF_PERM
        GCParseRule FULL_GC_CMS
        GCParseRule PARNEW_DETAILS_WITH_CONCURRENT_MODE_FAILURE_META
        GCParseRule CMS_FULL_80
     */


    private final MRUQueue<GCParseRule, BiConsumer<GCLogTrace, String>> parseRules;

    {
        parseRules = new MRUQueue<>();
        parseRules.put(DEFNEW, this::defNew);
        parseRules.put(DEFNEW_TENURING, this::defNewWithTenuring);
        parseRules.put(SERIAL_FULL, this::serialFull);
        parseRules.put(PARNEW, this::parNew);
        parseRules.put(PARNEW_TENURING, this::parNewWithTenuring);
        parseRules.put(PARNEW_CONCURRENT_MODE_END, this::parNewConcurrentModeEnd);
        parseRules.put(PARNEW_CARDTABLE, this::parNewCardTable);
        parseRules.put(PARNEW_TO_CMF_PERM, this::parNewToConcurrentModeFailure);
        parseRules.put(PARNEW_TO_CMF_META, this::parNewToConcurrentModeFailure);
        parseRules.put(PARNEW_REFERENCE,this::parNewReference);
        parseRules.put(PARNEW_REFERENCE_SPLIT, this::parNewReferenceSplit);
        parseRules.put(JVMPatterns.TLAB_START, this::gcSplitByTLAB);
        parseRules.put(PARNEW_REFERENCE_SPLIT_BY_TLAB, this::parNewReferenceSplitByTLAB);
        parseRules.put(DEFNEW_REFERENCE, this::defNewReference);
        parseRules.put(PARNEW_PROMOTION_FAILED, this::parNewPromotionFailed);
        parseRules.put(PARNEW_PROMOTION_FAILED_DETAILS, this::parNewPromotionFailedDetails);
        parseRules.put(PARNEW_PROMOTION_FAILED_REFERENCE, this::parNewPromotionFailedReference);
        parseRules.put(FLOATING_REFERENCE, this::parNewFloatingReference);
        parseRules.put(PARNEW_PROMOTION_FAILED_TENURING, this::parNewPromotionFailedTenuring);
        parseRules.put(PARNEW_PROMOTION_FAILED_IN_CMS_PHASE, this::parNewPromotionFailedInConcurrentMarkSweepPhase);
        parseRules.put(CMS_BAILING_TO_FOREGROUND, this::concurrentMarkSweepBailingToForeground);
        parseRules.put(PROMOTION_FAILED_TO_FULL, this::promotionFailedToFull);
        parseRules.put(PARNEW_PLAB, this::parNewPLAB);
        parseRules.put(PLAB_ENTRY, this::plabEntry);
        parseRules.put(PLAB_SUMMARY, this::plabSummary);
        parseRules.put(FULLGC_FLS_BEFORE, this::fullGCFLSBefore);
        parseRules.put(PARNEW_FLS_BEFORE, this::parNewFLSBefore);
        parseRules.put(PARNEW_FLS_AFTER, this::parNewFLSAfter);
        parseRules.put(PARNEW_FLS_BODY, this::parNewFLSBody);
        parseRules.put(PARNEW_PROMOTION_FAILED_DETAILS_AFTER, this::parNewConcurrentModeFailureFLSAfter);
        parseRules.put(PARNEW_FLS_TIME, this::parNewFLSTime);

        parseRules.put(FLS_HEADER, this::parNewFLSHeader);
        parseRules.put(FLS_SEPARATOR, this::parNewFLSSeparator);
        parseRules.put(FLS_TOTAL_FREE_SPACE, this::parNewFLSTotalFreeSpace);
        parseRules.put(FLS_MAX_CHUNK_SIZE, this::parNewFLSMaxChunkSize);
        parseRules.put(FLS_NUMBER_OF_BLOCKS, this::parNewFLSNumberOfBlocks);
        parseRules.put(FLS_AVERAGE_BLOCK_SIZE, this::parNewFLSAveBlockSize);
        parseRules.put(FLS_TREE_HEIGHT, this::parNewFLSTreeHeight);
        parseRules.put(FLS_LARGE_BLOCK_PROXIMITY, this::flsLargeBlockProximity);
        parseRules.put(FLS_LARGE_BLOCK, this::flsLargeBlock);

        parseRules.put(PARNEW_PROMOTION_FAILED_TIME_ABORT_PRECLEAN, this::parNewPromotionFailedTimeAbortPreclean);
        parseRules.put(PARNEW_PROMOTION_FAILED_CONCURRENT_PHASE, this::parNewPromotionFailedConcurrentPhase);
        parseRules.put(CORRUPTED_PARNEW_CONCURRENT_PHASE, this::corruptedParNewConcurrentPhase);
        parseRules.put(CORRUPTED_PARNEW_BODY, this::corruptedParNewBody);
        parseRules.put(CONCURRENT_PHASE_START, this::concurrentPhaseStart);
        parseRules.put(CONCURRENT_PHASE_END, this::concurrentPhaseEnd);
        parseRules.put(CONCURRENT_PHASE_END_WITH_CPU_SUMMARY, this::concurrentPhaseEnd);
        parseRules.put(INITIAL_MARK, this::initialMark);
        parseRules.put(SCAVENGE_BEFORE_REMARK, this::scavengeBeforeRemark);
        parseRules.put(SCAVENGE_BEFORE_REMARK_TENURING, this::scavengeBeforeRemarkTenuring);
        parseRules.put(PARALLEL_REMARK_WEAK_REF, this::recordRemark);
        parseRules.put(PARALLEL_REMARK_CLASS_UNLOADING, this::recordRemark);
        parseRules.put(REMARK_PARNEW_PROMOTION_FAILED, this::remarkParNewPromotionFailed);
        parseRules.put(PARALLEL_REMARK_STRING_SYMBOL, this::parallelRemarkStringSymbolClause);
        parseRules.put(PARALLEL_REMARK_WEAK_CLASS_SYMBOL_STRING, this::recordRemark);
        parseRules.put(PARALLEL_REMARK_WEAK_STRING, this::recordRemark);
        parseRules.put(PARALLEL_RESCAN, this::parallelRescan);
        parseRules.put(REMARK, this::recordRemark);
        parseRules.put(PARALLEL_RESCAN_V2, this::recordRemark);
        parseRules.put(PARALLEL_RESCAN_WEAK_CLASS_SCRUB, this::recordRemark);
        //, 0.1127040 secs]220.624: [weak refs processing, 0.1513820 secs] [1 CMS-remark: 10541305K(16777216K)] 10742883K(18664704K), 0.7371020 secs]
        //todo: this was capturing records that is shouldn't have so the rule was modified.. now does it work??? Needs through testing now that order of evaluation will change
        parseRules.put(SERIAL_REMARK_SCAN_BREAKDOWNS, this::recordRemark);
        parseRules.put(REMARK_DETAILS, this::recordRemark);
        parseRules.put(REMARK_REFERENCE_PROCESSING, this::recordRemarkWithReferenceProcessing);
        parseRules.put(TENURING_DETAILS, this::tenuringDetails);
        parseRules.put(RESCAN_WEAK_CLASS_SYMBOL_STRING, this::rescanWeakClassSymbolString);
        parseRules.put(CONCURRENT_MODE_FAILURE_DETAILS, this::concurrentModeFailureDetails);
        parseRules.put(CONCURRENT_MODE_FAILURE_DETAILS_META, this::concurrentModeFailureDetails);
        parseRules.put(PARNEW_DETAILS_CONCURRENT_MODE_FAILURE_PERM, this::parNewDetailsConcurrentModeFailure);
        parseRules.put(PARNEW_DETAILS_CONCURRENT_MODE_FAILURE_META, this::parNewDetailsConcurrentModeFailure);
        parseRules.put(PARNEW_DETAILS_PROMOTION_FAILED_WITH_CMS_PHASE, this::parNewDetailsPromotionFailedWithConcurrentMarkSweepPhase);
        parseRules.put(PARNEW_DETAILS_WITH_CONCURRENT_MODE_FAILURE, this::parNewDetailsWithConcurrentModeFailure);
        parseRules.put(CONCURRENT_MODE_FAILURE_REFERENCE, this::concurrentModeFailureReference);
        parseRules.put(iCMS_CONCURRENT_MODE_FAILURE, this::iCMSConcurrentModeFailure);
        parseRules.put(iCMS_CONCURRENT_MODE_FAILURE_META, this::iCMSConcurrentModeFailure);
        parseRules.put(iCMS_CMF_DUIRNG_PARNEW_DEFNEW_DETAILS, this::iCMSConcurrentModeFailureDuringParNewDefNewDetails);
        parseRules.put(FULL_GC_INTERRUPTS_CONCURRENT_PHASE, this::fullGCInterruptsConcurrentPhase);
        parseRules.put(FULL_PARNEW_START, this::fullParNewStart);
        parseRules.put(CMS_FULL_80, this::psFullReferenceJDK8);
        parseRules.put(FULL_GC_REFERENCE_CMF, this::fullGCReferenceConcurrentModeFailure);

        parseRules.put(iCMS_PARNEW, this::iCMSParNew);
        parseRules.put(iCMS_PARNEW_PROMOTION_FAILURE_RECORD, this::iCMSParNewPromotionFailureRecord);
        parseRules.put(iCMS_PARNEW_PROMOTION_FAILURE, this::iCMSParNewPromotionFailure);
        parseRules.put(FULL_GC_ICMS, this::fullGCiCMS);
        parseRules.put(iCMS_PARNEW_DEFNEW_TENURING_DETAILS, this::iCMSParNewDefNewTenuringDetails);
        parseRules.put(iCMS_FULL, this::iCMSFullGC);
        parseRules.put(iCMS_PROMOTION_FAILED, this::iCMSPromotionFailed);
//        parseRules.put(iCMS_PROMOTION_FAILED_PERM, this::iCMSPromotionFailedPermMeta); // todo: this rule and the next appear to be a duplication of iCMS_PARNEW_PROMOTION_FAILURE
//        parseRules.put(iCMS_PROMOTION_FAILED_META, this::iCMSPromotionFailedPermMeta);
        parseRules.put(iCMS_MISLABELED_FULL, this::iCMSMislabeledFull);
        parseRules.put(iCMS_FULL_AFTER_CONCURRENT_MODE_FAILURE, this::iCMSFullAfterConcurrentModeFailure);
        parseRules.put(iCMS_FULL_AFTER_CONCURRENT_MODE_FAILURE_META, this::iCMSFullAfterConcurrentModeFailure);
        parseRules.put(iCMS_CONCURRENT_MODE_INTERRUPTED, this::iCMSConcurrentModeInterrupted);
        parseRules.put(PS_FULL_GC_META, this::psFullGCMeta);
        parseRules.put(PS_FULL_GC_V2_META, this::psFullGCV2Meta);
        parseRules.put(CMS_FULL_META, this::cmsFullPermOrMeta);
        parseRules.put(FULL_PARNEW_CMF_META, this::fullParNewConcurrentModeFailureMeta);
        parseRules.put(FULL_PARNEW_CMF_PERM, this::fullParNewConcurrentModeFailurePerm);
        parseRules.put(PARNEW_CONCURRENT_MODE_FAILURE_PERM, this::parNewConcurrentModeFailurePerm);
        parseRules.put(PARNEW_CONCURRENT_MODE_FAILURE_META, this::parNewConcurrentModeFailurePerm);
        parseRules.put(PS_FULL_GC_V2_PERM, this::psFullGCV2Perm);
        parseRules.put(PS_FULL_GC_PERM, this::psFullGCPerm);
        parseRules.put(CMS_FULL_PERM, this::cmsFullPermOrMeta);
        parseRules.put(CMS_FULL_PERM_META_REFERENCE, this::cmsFullPermOrMeta);
        parseRules.put(PARNEW_NO_DETAILS, this::parNewNoDetails);
        parseRules.put(YOUNG_NO_DETAILS, this::youngNoDetails);
        parseRules.put(CMS_NO_DETAILS, this::cmsNoDetails);
        parseRules.put(FULL_NO_GC_DETAILS, this::fullNoGCDetails);
        parseRules.put(PARNEW_START, this::parNewStart);
        parseRules.put(GC_START, this::gcStart);
        parseRules.put(YOUNG_SPLIT_NO_DETAILS, this::youngSplitNoDetails);
        parseRules.put(CMF_SIMPLE, this::cmfSimple);
        parseRules.put(DEFNEW_DETAILS, this::defNewDetails);
        parseRules.put(PRECLEAN_REFERENCE_PAR_NEW_REFERENCE, this::preCleanReferenceParNewReference);

        parseRules.put(PSYOUNGGEN, this::psYoungGen);
        parseRules.put(PSYOUNGGEN_PROMOTION_FAILED, this::psYoungGen);
        parseRules.put(PSFULL, this::psFull);
        parseRules.put(PSYOUNGGEN_NO_DETAILS, this::psYoungNoDetails);
        parseRules.put(PSYOUNGGEN_REFERENCE_SPLIT, this::psYoungGenReferenceProcessingSplit);
        parseRules.put(PSYOUNGGEN_REFERENCE, this::psYoungGenReferenceProcessing);
        parseRules.put(PS_TENURING_START, this::psTenuringStart);
        parseRules.put(PSFULL_SPLIT, this::psFullSPlit);
        parseRules.put(PS_FULL_REFERENCE_SPLIT, this::psFullReferenceSplit);
        parseRules.put(PS_FULL_REFERENCE, this::psFullReference);
        parseRules.put(PS_DETAILS_WITH_TENURING, this::psDetailsWithTenuring);
        parseRules.put(PS_FAILURE, this::psFailure);
        parseRules.put(PSYOUNG_ADAPTIVE_SIZE_POLICY, this::psYoungAdaptiveSizePolicy);
        parseRules.put(PSOLD_ADAPTIVE_SIZE_POLICY, this::psFullAdaptiveSizePolicy);
        parseRules.put(PSYOUNG_DETAILS_FLOATING, this::psYoungDetailsFloating);
        parseRules.put(PSFULL_ADAPTIVE_SIZE, this::psFullAdaptiveSize);
        parseRules.put(PS_FULL_BODY_FLOATING, this::psFullBodyFloating);
        parseRules.put(FULL_REFERENCE_ADAPTIVE_SIZE, this::psFullReferenceAdaptiveSize);
        parseRules.put(PS_PROMOTION_FAILED, this::psPromotionFailed);

        parseRules.put(RESCAN_SPLIT_UNLOADING_STRING, this::rescanSplitUnloadingString);

        parseRules.put(PARNEW_CONCURRENT_PHASE_CARDS, this::parNewConcurrentPhaseCards);
        parseRules.put(CONC_PHASE_YIELDS, this::concurrentPhaseYields);
        parseRules.put(PRECLEAN_TIMED_OUT_WITH_CARDS, this::precleanTimedoutWithCards);
        parseRules.put(PARNEW_SHOULD_CONCURRENT_COLLECT, this::parNewShouldConcurrentCollect);
        parseRules.put(SHOULD_CONCURRENT_COLLECT, this::shouldCollectConcurrent);
        parseRules.put(REMARK_SPLIT_BY_DEBUG, this::remarkSplitByDebug);
        parseRules.put(SCAVENGE_BEFORE_REMARK_PRINT_HEAP_AT_GC, this::scavengeBeforeRemarkPrintHeapAtGC);
        parseRules.put(SPLIT_REMARK_REFERENCE_BUG, this::splitRemarkReferenceWithWeakReferenceSplitBug);
        parseRules.put(SPLIT_REMARK_REFERENCE, this::splitRemarkReference);
        parseRules.put(PSYOUNG_ADAPTIVE_SIZE_POLICY_START, this::psYoungAdaptivePolicySizeStart);
        parseRules.put(PS_ADAPTIVE_SIZE_POLICY_BODY, this::psYoungAdaptivePolicySizeBody);
        parseRules.put(ADAPTIVE_SIZE_POLICY_BODY, this::adaptivePolicySizeBody);
        parseRules.put(ADAPTIVE_SIZE_POLICY_STOP, this::adaptiveSizePolicyStop);

        parseRules.put(SCAVENGE_BEFORE_REMARK_REFERENCE, this::scavengeBeforeRemarkReference);
        parseRules.put(SCAVENGE_BEFORE_REMARK_REFERENCE_SPLIT, this::scavengeBeforeRemarkReferenceSplit);
        parseRules.put(PARNEW_TO_CONCURRENT_MODE_FAILURE, this::parNewToPsudoConcurrentModeFailure);
        parseRules.put(SERIAL_FULL_REFERENCE, this::serialFullReference);
        parseRules.put(PSFULL_ERGONOMICS_PHASES, this::psFullErgonomicsPhases);
        parseRules.put(PSFULL_REFERENCE_PHASE, this::psFullReferencePhase);
        parseRules.put(SPLIT_PARNEW_PROMOTION_FAILED_IN_CMS_PHASE, this::parNewPromotionFailedInConcurrentMarkSweepPhase);
        parseRules.put(FULL_SPLIT_BY_CONCURRENT_PHASE, this::fullSplitByConcurrentPhase);
        parseRules.put(CMF_LARGE_BLOCK, this::concurrentModeFailureSplitByLargeBlock);
        parseRules.put(WEAK_PROCESSING, this::noop);

        //this rule must be evaluated before CONCURRENT_PHASE_END_BLOCK
        parseRules.put(ABORT_PRECLEAN_DUE_TO_TIME_CLAUSE, this::abortPrecleanDueToTime);
        parseRules.put(CONCURRENT_PHASE_CARDS, this::abortPrecleanWithCards);
        parseRules.put(PRECLEAN_REFERENCE, this::endConcurrentPrecleanWithReferenceProcessing);
        parseRules.put( new GCParseRule("FLOATING_CPU_BREAKOUT", "^" + CPU_SUMMARY), this::noop);
        parseRules.put(new GCParseRule("END_OF_DATA_SENTINEL", END_OF_DATA_SENTINEL), this::endOfFile);
    }

    public GenerationalHeapParser() {
    }

    @Override
    public Set<EventSource> eventsProduced() {
        return Set.of(EventSource.GENERATIONAL);
    }

    @Override
    public String getName() {
        return "GenerationalHeapParser";
    }

    @Override
    protected void process(String line) {

        if (ignoreFrequentButUnwantedEntries(line)) return;

        try {
            Optional<AbstractMap.SimpleEntry<GCParseRule, GCLogTrace>> optional = parseRules.keys()
                    .stream()
                    .map(rule -> new AbstractMap.SimpleEntry<>(rule, rule.parse(line)))
                    .filter(tuple -> tuple.getValue() != null)
                    .findFirst();
            if ( optional.isPresent()) {
                AbstractMap.SimpleEntry<GCParseRule, GCLogTrace> ruleAndTrace = optional.get();
                parseRules.get(ruleAndTrace.getKey()).accept(ruleAndTrace.getValue(), line);
                return;
            }
        } catch (Throwable t) {
            LOGGER.throwing(this.getName(), "process", t);
        }

        log(line);
    }

    private boolean ignoreFrequentButUnwantedEntries(String line) {

        if (JVMPatterns.APPLICATION_TIME.parse(line) != null) return true;
        if (JVMPatterns.SIMPLE_APPLICATION_TIME.parse(line) != null) return true;

        if (JVMPatterns.APPLICATION_STOP_TIME.parse(line) != null) return true;
        if (JVMPatterns.APPLICATION_STOP_TIME_WITH_STOPPING_TIME.parse(line) != null) return true;
        if (JVMPatterns.SIMPLE_APPLICATION_STOP_TIME.parse(line) != null) return true;

        if (TenuredPatterns.TENURING_SUMMARY.parse(line) != null) return true;
        if (TenuredPatterns.TENURING_AGE_BREAKDOWN.parse(line) != null) return true;

        if (line.startsWith("TLAB: gc thread: ")) return true;
        if (line.startsWith("TLAB totals: thrds: ")) return true;

        if (line.startsWith("{Heap before")) {
            return true;
        }
        if (line.equals("}")) {
            return true;
        }

        if (line.startsWith("Heap")) return true;
        if (line.startsWith("Heap after")) return true;
        if (line.startsWith("PSYoungGen")) return true;
        if (line.startsWith("ParOldGen")) return true;
        if (line.startsWith("PSOldGen")) return true;
        if (line.startsWith("PSPermGen")) return true;
        if (line.startsWith("concurrent mark-sweep")) return true;
        if (line.startsWith("object space")) return true;
        if (line.startsWith("eden space")) return true;
        if (line.startsWith("from space")) return true;
        if (line.startsWith("to   space")) return true;
        if (line.contains("[0xffff") && line.endsWith("000)")) return true;

        if (line.startsWith("Finished ")) return true;

        if (line.startsWith("GC locker: Trying a full collection because scavenge failed")) return true;

        if (line.startsWith("Java HotSpot(TM)")) return true;
        if (line.startsWith("OpenJDK 64")) return true;
        if (line.startsWith("Memory: ")) return true;
        if (line.startsWith("CommandLine flags: ")) return true;
        if (line.startsWith("/proc/meminfo")) return true;
        return line.startsWith("avg_survived_padded_avg");
    }

    public void endOfFile(GCLogTrace trace, String line) {
        publish(new JVMTermination(getClock(),diary.getTimeOfFirstEvent()), true);
    }

    public void defNew(GCLogTrace trace, String line) {
        DefNew defNew = new DefNew(getClock(), trace.gcCause(), trace.getDuration());
        defNew.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(13), this.getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 20));
        defNew.add(extractCPUSummary(line));
        publish(defNew);
    }

    //2019-10-22T23:41:21.852+0000: 21.912: [GC (GCLocker Initiated GC) 2019-10-22T23:41:21.853+0000: 21.912: [DefNew2019-10-22T23:41:21.914+0000: 21.974: [SoftReference, 0 refs, 0.0000842 secs]2019-10-22T23:41:21.914+0000: 21.974: [WeakReference, 76 refs, 0.0000513 secs]2019-10-22T23:41:21.914+0000: 21.974: [FinalReference, 91635 refs, 0.0396861 secs]2019-10-22T23:41:21.954+0000: 22.014: [PhantomReference, 0 refs, 3 refs, 0.0000444 secs]2019-10-22T23:41:21.954+0000: 22.014: [JNI Weak Reference, 0.0000281 secs]: 419520K->19563K(471936K), 0.1019514 secs] 502104K->102148K(2044800K), 0.1020469 secs] [Times: user=0.09 sys=0.01, real=0.10 secs]
    public void defNewDetails(GCLogTrace trace, String line) {
        DefNew defNew = new DefNew(getClock(), trace.gcCause(), trace.getDuration());
        MemoryPoolSummary heap = this.getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 58);
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(51);
        defNew.add(young, heap.minus(young), heap);
        defNew.add(extractCPUSummary(line));
        defNew.add(extractPrintReferenceGC(line));
        publish(defNew);
    }

    public void defNewWithTenuring(GCLogTrace trace, String line) {
        scavengeTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.DefNew;
    }

    //3.299: [Full GC (Metadata GC Threshold) 3.299: [Tenured: 21006K->20933K(21888K), 0.0230475 secs] 29003K->20933K(31680K), [Metapace: 12111K->12111K(12672K)], 0.0231557 secs]
    public void serialFull(GCLogTrace trace, String line) {
        FullGC collection = new FullGC(getClock(), trace.gcCause(), trace.getDuration());
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(5);
        MemoryPoolSummary heap = this.getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 12);
        collection.add(heap.minus(tenured), tenured, heap);
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //62.616: [GC 62.616: [ParNew: 5033216K->129451K(5662336K), 0.2536590 secs] 5097075K->193310K(24536704K), 0.2538510 secs]
    //48.021: [GC48.021: [ParNew: 306686K->34046K(306688K), 0.3196120 secs] 1341473K->1125818K(8669952K), 0.3197540 secs]
    public void parNew(GCLogTrace trace, String line) {
        ParNew collection = new ParNew(getClock(), trace.gcCause(1), trace.getDuration());
        collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(13), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 20));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //9.811: [GC 9.811: [ParNew
    private void parNewWithTenuring(GCLogTrace trace, String line) {
        scavengeTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        gcCauseForwardReference = trace.gcCause();
    }

    private void fullGCFLSBefore(GCLogTrace trace, String line) {
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.FullGC;
        fullGCTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
    }


    private void parNewFLSBefore(GCLogTrace trace, String line) {
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        scavengeTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
    }

    private void parNewFLSAfter(GCLogTrace trace, String line) {
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        youngMemoryPoolSummaryForwardReference = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(3);
        heapForwardReference = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 10);
    }

    private void parNewFLSBody(GCLogTrace trace, String line) {
        youngMemoryPoolSummaryForwardReference = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        heapForwardReference = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8);
    }

    private void parNewConcurrentModeFailureFLSAfter(GCLogTrace trace, String line) {
        if (garbageCollectionTypeForwardReference == null)
            garbageCollectionTypeForwardReference = GarbageCollectionTypes.ConcurrentModeFailure;
        if (gcCauseForwardReference == null)
            gcCauseForwardReference = GCCause.LAST_GC_CAUSE;
        tenuredForwardReference = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        heapForwardReference = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8);
        metaSpaceForwardReference = extractPermOrMetaspaceRecord(line);
    }

    private void parNewFLSTime(GCLogTrace trace, String line) {
        if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.ParNewPromotionFailed) {
            ParNewPromotionFailed parNew = new ParNewPromotionFailed(getClock(), gcCauseForwardReference, scavengeDurationForwardReference);
            MemoryPoolSummary heap = new MemoryPoolSummary(heapForwardReference.getOccupancyBeforeCollection(), heapForwardReference.getOccupancyBeforeCollection(), heapForwardReference.getSizeAfterCollection());
            parNew.add(youngMemoryPoolSummaryForwardReference, heap);
            parNew.add(scavengeCPUSummaryForwardReference);
            ConcurrentModeFailure cmf = new ConcurrentModeFailure(getClock().add(scavengeDurationForwardReference), GCCause.CMS_FAILURE, trace.getDuration());
            cmf.add(heapForwardReference.minus(tenuredForwardReference), tenuredForwardReference, heapForwardReference);
            cmf.add(extractCPUSummary(line));
            cmf.addBinaryTreeDictionary(new BinaryTreeDictionary(totalFreeSpaceForwardReference, maxChunkSizeForwardReference, numberOfBlocksForwardReference, averageBlockSizeForwardReference, treeHeightForwardReference));
            publish(parNew);
            publish(cmf);
        } else if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.ParNew) {
            ParNew collection = new ParNew(scavengeTimeStamp, gcCauseForwardReference, trace.getPauseTime());
            collection.add(youngMemoryPoolSummaryForwardReference, heapForwardReference);
            collection.add(extractCPUSummary(line));
            collection.addBinaryTreeDictionary(new BinaryTreeDictionary(totalFreeSpaceForwardReference, maxChunkSizeForwardReference, numberOfBlocksForwardReference, averageBlockSizeForwardReference, treeHeightForwardReference));
            publish(collection);
        } else if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.ConcurrentModeFailure) {
            MemoryPoolSummary heap = new MemoryPoolSummary(heapForwardReference.getOccupancyBeforeCollection(), heapForwardReference.getSizeAfterCollection(), heapForwardReference.getOccupancyBeforeCollection(), heapForwardReference.getSizeAfterCollection());
            double portionOfPauseToSubtract =  ( parNewForwardReference == null) ? 0.0d : parNewForwardReference.getDuration();
            ConcurrentModeFailure concurrentModeFailure = new ConcurrentModeFailure(fullGCTimeStamp, gcCauseForwardReference, trace.getPauseTime() - portionOfPauseToSubtract);
            concurrentModeFailure.add(heapForwardReference.minus(tenuredForwardReference), tenuredForwardReference, heapForwardReference);
            concurrentModeFailure.add(extractCPUSummary(line));
            concurrentModeFailure.add(metaSpaceForwardReference);
            concurrentModeFailure.addBinaryTreeDictionary(new BinaryTreeDictionary(totalFreeSpaceForwardReference, maxChunkSizeForwardReference, numberOfBlocksForwardReference, averageBlockSizeForwardReference, treeHeightForwardReference));
            // some concurrent mode failure conditions do not involve a young generational collection
            if ( parNewForwardReference != null) {
                parNewForwardReference.add(youngMemoryPoolSummaryForwardReference, heap.minus(youngMemoryPoolSummaryForwardReference), heap);
                publish(parNewForwardReference);
            }
            publish(concurrentModeFailure);
        } else if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.FullGC) {
            MemoryPoolSummary heap = new MemoryPoolSummary(heapForwardReference.getOccupancyBeforeCollection(), heapForwardReference.getSizeAfterCollection(), heapForwardReference.getOccupancyBeforeCollection(), heapForwardReference.getSizeAfterCollection());
            FullGC fullGc = new FullGC(fullGCTimeStamp, gcCauseForwardReference, trace.getDuration());
            fullGc.add(heap);
            fullGc.add(extractCPUSummary(line));
            publish(fullGc);
        } else
            LOGGER.warning("Unable to parse -> " + trace);
    }

    private void parNewFLSHeader(GCLogTrace trace, String line) {
    }

    private void parNewFLSSeparator(GCLogTrace trace, String line) {
    }

    private void parNewFLSTotalFreeSpace(GCLogTrace trace, String line) {
        totalFreeSpaceForwardReference = trace.getLongGroup(1);
    }

    private void parNewFLSMaxChunkSize(GCLogTrace trace, String line) {
        maxChunkSizeForwardReference = trace.getLongGroup(1);
    }

    private void parNewFLSNumberOfBlocks(GCLogTrace trace, String line) {
        numberOfBlocksForwardReference = trace.getIntegerGroup(1);
    }

    private void parNewFLSAveBlockSize(GCLogTrace trace, String line) {
        averageBlockSizeForwardReference = trace.getLongGroup(1);
    }

    private void parNewFLSTreeHeight(GCLogTrace trace, String line) {
        treeHeightForwardReference = trace.getIntegerGroup(1);
    }

    private void flsLargeBlockProximity(GCLogTrace trace, String line) {
    }

    private void flsLargeBlock(GCLogTrace trace, String line) {
    }
    
    // Do not remove. This method is used as a development aid when adding a new parse rule.
    // For example, parseRules.put(MY_NEW_PARSE_RULE, this::noop);
    // It is then possible to set a breakpoint in this method to examine the details of the GCLogTrace.
    private void noop(GCLogTrace trace, String line) {
    }

    //46.435: [GC 46.435: [ParNew: 19136K->19136K(19136K), 0.0000274 secs]46.435: [CMS46.458: [CMS-concurrent-sweep: 0.060/0.117 secs] [Times: user=0.21 sys=0.01, real=0.12 secs]
    //public static final ParseRule PARNEW_CONCURRENT_MODE_END = new ParseRule (GC_PREFIX + PARNEW_BLOCK + TIMESTAMP + "\\[CMS" + CMS_PHASE_END + "(?: " + CPU_BREAKDOWN + ")?$");
    //public static final String CMS_PHASE_END = DATE_TIMESTAMP + "\\[CMS-concurrent-(.+): " + CPU_WALLCLOCK + "\\]";
    public void parNewConcurrentModeEnd(GCLogTrace trace, String line) {
        concurrentPhaseEnd(trace,line,23);
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        scavengeTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
        scavengeDurationForwardReference = trace.getDoubleGroup(18);
        parNewForwardReference = new ParNew(scavengeTimeStamp, garbageCollectionTypeForwardReference, gcCauseForwardReference, scavengeDurationForwardReference);
        youngMemoryPoolSummaryForwardReference = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(12);
    }

    public void parNewCardTable(GCLogTrace trace, String line) {
        LOGGER.warning("Not Yet Implemented -> " + trace);
    }


    //11.675: [GC 11.675: [ParNew: 3782K->402K(3904K), 0.0012156 secs]11.676: [Tenured: 8673K->6751K(8840K), 0.1268332 secs] 12373K->6751K(12744K), [Perm : 10729K->10675K(21248K)], 0.1281985 secs]
    //89.260: [GC 89.260: [ParNew: 19135K->19135K(19136K), 0.0000156 secs]89.260: [CMS: 105875K->107775K(107776K), 0.5703972 secs] 125011K->116886K(126912K), [CMS Perm : 15589K->15584K(28412K)], 0.5705219 secs]
    public void parNewToConcurrentModeFailure(GCLogTrace trace, String line) {
        ConcurrentModeFailure collection = new ConcurrentModeFailure(getClock(), trace.gcCause(), trace.getDoubleGroup(trace.groupCount()));
        collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(12), trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(24), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 31));
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //Very rare occurrence where ParNew suffers from a promotion failure during the reset. This is, and isn't a CMF but will be treated as one.
    public void parNewToPsudoConcurrentModeFailure(GCLogTrace trace, String line) {
        ConcurrentModeFailure collection = new ConcurrentModeFailure(scavengeTimeStamp, GCCause.PROMOTION_FAILED, trace.getDoubleGroup(trace.groupCount()));
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8);
        collection.add(heap.minus(tenured), tenured, heap);
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);

    }

    public void parNewReference(GCLogTrace trace, String line) {
        ParNew gcEvent = new ParNew(trace.getDateTimeStamp(), trace.gcCause(), trace.getDuration());
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(51);
        MemoryPoolSummary heap = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(58);
        gcEvent.add(young, heap.minus(young), heap);
        gcEvent.addReferenceGCSummary(extractPrintReferenceGC(line));
        gcEvent.add(extractCPUSummary(line));
        publish(gcEvent);
    }

    public void parNewReferenceSplit(GCLogTrace trace, String line) {
        scavengeTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        gcCauseForwardReference = trace.gcCause();
        referenceGCForwardReference = extractPrintReferenceGC(line);
    }

    public void gcSplitByTLAB(GCLogTrace trace, String line) {
        //GC_Locker bug allows Remark and a ParNew to run together. We need the date stamp for the remark
        if (line.contains("[GC[YG occupancy")) {
            remarkTimeStamp = getClock();
        }
        scavengeTimeStamp = getClock();
    }

    public void parNewReferenceSplitByTLAB(GCLogTrace trace, String line) {
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        gcCauseForwardReference = trace.gcCause();
        referenceGCForwardReference = extractPrintReferenceGC(line);
    }

    public void defNewReference(GCLogTrace trace, String line) {
        scavengeTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.DefNew;
        gcCauseForwardReference = trace.gcCause();
        referenceGCForwardReference = extractPrintReferenceGC(line);
    }

    //GenerationalHeapParser, not implemented: 2015-07-06T15:18:19.465-0700: 483260.591: [GC (Allocation Failure) 483260.592: [ParNew (promotion failed): 2146944K->2146944K(2146944K), 0.9972460 secs] 20182045K->20340243K(20732992K), 0.9975730 secs
    public void parNewPromotionFailed(GCLogTrace trace, String line) {
        ParNewPromotionFailed collection = new ParNewPromotionFailed(getClock(), GCCause.PROMOTION_FAILED, trace.getDuration());
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(12);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 19);
        collection.add(young, heap);
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    public void parNewPromotionFailedDetails(GCLogTrace trace, String line) {
        scavengeTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNewPromotionFailed;
        gcCauseForwardReference = GCCause.PROMOTION_FAILED;
        ArrayList<Integer> blocks = new ArrayList<>();
        GCLogTrace block = PARNEW_PROMOTION_FAILURE_SIZE_BLOCK.parse(line);
        do {
            blocks.add(block.getIntegerGroup(1));
        } while (block.hasNext());
        promotionFailureSizesForwardReference = new int[blocks.size()];
        for (int index = 0; index < blocks.size(); index++)
            promotionFailureSizesForwardReference[index] = blocks.get(index);
        GCLogTrace memorySummary = BEFORE_AFTER_CONFIGURED_PAUSE_RULE.parse(line);
        youngMemoryPoolSummaryForwardReference = memorySummary.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        scavengeDurationForwardReference = memorySummary.getDuration();
    }

    //"10.099: [GC (Allocation Failure) 10.099: [ParNew10.100: [SoftReference, 0 refs, 0.0000141 secs]10.100: [WeakReference, 0 refs, 0.0000029 secs]10.100: [FinalReference, 0 refs, 0.0000026 secs]10.100: [PhantomReference, 0 refs, 0 refs, 0.0000033 secs]10.100: [JNI Weak Reference, 0.0000045 secs] (promotion failed): 72353K->71961K(78656K), 0.0009583 secs]10.100: [CMS10.100: [CMS-concurrent-abortable-preclean: 0.210/2.397 secs]
    public void parNewPromotionFailedReference(GCLogTrace trace, String line) {
        parNewForwardReference = new ParNew(trace.getDateTimeStamp(), GCCause.PROMOTION_FAILED, trace.getDoubleGroup(35));
        youngMemoryPoolSummaryForwardReference = trace.getOccupancyWithMemoryPoolSizeSummary(29);
        parNewForwardReference.add(extractPrintReferenceGC(line));
    }

    public void parNewFloatingReference(GCLogTrace trace, String line) {
        referenceGCForwardReference = extractPrintReferenceGC(line);
    }

    //5273.802: [GC 5273.803: [ParNew (promotion failed)
    public void parNewPromotionFailedTenuring(GCLogTrace trace, String line) {
        scavengeTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNewPromotionFailed;
        gcCauseForwardReference = GCCause.PROMOTION_FAILED;
    }

    //7191.408: [GC 7191.408: [ParNew (promotion failed): 153344K->153344K(153344K), 0.2537020 secs]7191.662: [CMS7193.099: [CMS-concurrent-mark: 2.857/9.420 secs]
    //ParNew followed by a concurrent mode failure. Set the time of the CMF to be the beginning of the aborted CMS phase
    //not completely correct but good enough.

    /**
     * This rule is linked to 2 similar but not identical rules.
     * The group count for 1 rule is 18 and the group count for the other rule is 16.
     * @param trace The chunk of GC log that we are attempting to match to a known GC log pattern
     * @param line The GC log line being parsed
     */
    public void parNewPromotionFailedInConcurrentMarkSweepPhase(GCLogTrace trace, String line) {
        concurrentPhaseEnd(trace,line,15);
        int offset = (trace.groupCount() == 23) ? 0 : 2;
        scavengeTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNewPromotionFailed;
        gcCauseForwardReference = GCCause.PROMOTION_FAILED;
        youngMemoryPoolSummaryForwardReference = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(8 - offset);
        scavengeDurationForwardReference = trace.getDoubleGroup(14 - offset);
    }

    //364.157: [GC 364.157: [ParNew: 235968K->235968K(235968K), 0.0000430 secs]364.157: [CMSbailing out to foreground collection
    //364.261: [CMS-concurrent-mark: 1.616/1.617 secs] [Times: user=5.08 sys=0.01, real=1.63 secs]
    //(concurrent mode failure): 702521K->594052K(786432K), 4.9052810 secs] 938489K->594052K(1022400K), [CMS Perm : 22623K->22606K(37688K)], 4.9054799 secs] [Times: user=5.02 sys=0.00, real=4.91 secs]
    /*
    Record failed ParNew as forward reference
    can ignore termination of concurrent phase
    use forward reference to finish ParNew and then record concurrent mode failure.
     */

    public void concurrentMarkSweepBailingToForeground(GCLogTrace trace, String line) {
        scavengeTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        gcCauseForwardReference = trace.gcCause();
        youngMemoryPoolSummaryForwardReference = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(12);
        scavengeDurationForwardReference = trace.getDoubleGroup(trace.groupCount() - 1);
    }

    //14921.473: [GC14921.473: [ParNew (promotion failed): 609300K->606029K(629120K), 0.1794470 secs]14921.652: [CMS: 1224499K->1100531K(1398144K), 3.4546330 secs] 1829953K->1100531K(2027264K), [CMS Perm : 148729K->148710K(262144K)], 3.6342950 secs]
    //84.977: [GC 84.977: [ParNew (promotion failed): 17024K->19136K(19136K), 0.0389515 secs]85.016: [CMS: 107077K->107775K(107776K), 0.2868956 secs] 109258K->107895K(126912K), [CMS Perm : 10680K->10674K(21248K)], 0.3260017 secs] [Times: user=0.37 sys=0.00, real=0.32 secs]
    public void promotionFailedToFull(GCLogTrace trace, String line) {
        double youngDuration = trace.getDoubleGroup(18);
        ParNewPromotionFailed youngCollection = new ParNewPromotionFailed(getClock(), GCCause.PROMOTION_FAILED, youngDuration);
        MemoryPoolSummary youngGen = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(12);
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(24);
        MemoryPoolSummary totalHeap = this.getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 31);
        // This calculation is a best guess as the data is improperly recorded and details are lost
        youngCollection.add(youngGen,
                new MemoryPoolSummary(tenured.getOccupancyBeforeCollection(), tenured.getOccupancyBeforeCollection(), tenured.getSizeAfterCollection()),
                new MemoryPoolSummary(youngGen.getOccupancyBeforeCollection() + tenured.getOccupancyBeforeCollection(),
                        youngGen.getOccupancyAfterCollection() + tenured.getOccupancyBeforeCollection(),
                        youngGen.getSizeAfterCollection() + tenured.getSizeAfterCollection()));
        publish(youngCollection);

        FullGC collection;
        DateTimeStamp fullStart = trace.getDateTimeStamp(3);
        double duration = trace.getDuration() - youngDuration;
        collection = new FullGC(fullStart, GCCause.PROMOTION_FAILED, duration);
        collection.add(totalHeap.minus(tenured), tenured, totalHeap);
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    public void parNewPLAB(GCLogTrace trace, String line) {
        scavengeTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        gcCauseForwardReference = trace.gcCause();
    }

    public void plabEntry(GCLogTrace trace, String line) {
    }

    public void plabSummary(GCLogTrace trace, String line) {
    }

    //2015-05-13T15:55:30.523-0500: 1127831.113: [GC2015-05-13T15:55:30.523-0500: 1127831.113: [ParNew (promotion failed): 873856K->848310K(873856K), 0.8676350 secs]2015-05-13T15:55:31.391-0500: 1127831.981: [CMS CMS: abort preclean due to time 2015-05-13T15:55:32.148-0500: 1127832.738: [CMS-concurrent-abortable-preclean: 5.960/6.935 secs] [Times: user=13.64 sys=0.62, real=6.93 secs]
    //185853.508: [GC 185853.508: [ParNew (promotion failed): 153265K->152331K(153344K), 0.1020210 secs]185853.610: [CMS CMS: abort preclean due to time 185853.688: [CMS-concurrent-abortable-preclean: 1.470/5.538 secs]
    //195325.615: [GC 195325.615: [ParNew (promotion failed): 153344K->153146K(153344K), 0.1193410 secs]195325.734: [CMS CMS: abort preclean due to time 195325.811: [CMS-concurrent-abortable-preclean: 3.142/5.277 secs]
    public void parNewPromotionFailedTimeAbortPreclean(GCLogTrace trace, String line) {
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNewPromotionFailed;
        gcCauseForwardReference = GCCause.PROMOTION_FAILED;
        scavengeDurationForwardReference = trace.getDoubleGroup((2 * TIME_DATE_OFFSET) + 6);
        fullGCTimeStamp = getClock().add(scavengeDurationForwardReference);
        scavengeTimeStamp = trace.getDateTimeStamp();
        youngMemoryPoolSummaryForwardReference = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(2 * TIME_DATE_OFFSET);
    }

    // todo: doesn't seem to be test coverage for this
    public void parNewPromotionFailedConcurrentPhase(GCLogTrace trace, String line) {
        scavengeTimeStamp = trace.getDateTimeStamp();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        gcCauseForwardReference = trace.gcCause();
        scavengeDurationForwardReference = trace.getDoubleGroup(18);
        youngMemoryPoolSummaryForwardReference = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(12);
        concurrentPhaseEnd(trace,line,19);
    }

    // ParNew with tenuring, no concurrent-mode-failure
    //50.306: [GC (Allocation Failure)50.306: [ParNew:
    //25.172: [GC25.172: [ParNew25.195: [CMS-concurrent-abortable-preclean: 0.325/0.557 secs] [Times: user=0.92 sys=0.00, real=0.56 secs]
    // 37.843: [GC 37.843: [ParNew37.843: [CMS-concurrent-abortable-preclean: 0.164/0.661 secs]
    public void corruptedParNewConcurrentPhase(GCLogTrace trace, String line) {
        scavengeTimeStamp = trace.getDateTimeStamp();
        gcCauseForwardReference = trace.gcCause();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        concurrentPhaseEnd(trace,line,11);
    }

    //(promotion failed): 118016K->118016K(118016K), 0.0288030 secs]17740.440: [CMS (concurrent mode failure): 914159K->311550K(917504K), 0.5495730 secs] 985384K->311550K(1035520K),
    // [CMS Perm : 65977K->65950K(131072K)], 0.5785090 secs] [Times: user=0.67 sys=0.01, real=0.58 secs]
    public void corruptedParNewBody(GCLogTrace trace, String line) {
        ParNewPromotionFailed parNewPromotionFailed = new ParNewPromotionFailed(scavengeTimeStamp, GCCause.PROMOTION_FAILED, trace.getPauseTime() - trace.getDoubleGroup(16));
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 20);
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(13);
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);

        MemoryPoolSummary parNewHeap = new MemoryPoolSummary(heap.getOccupancyBeforeCollection(), heap.getSizeAfterCollection(), heap.getOccupancyBeforeCollection(), heap.getSizeAfterCollection());
        parNewPromotionFailed.add(young, parNewHeap.minus(young), parNewHeap);

        ConcurrentModeFailure concurrentModeFailure = new ConcurrentModeFailure(scavengeTimeStamp.add(trace.getDoubleGroup(9) - scavengeTimeStamp.getTimeStamp()), GCCause.LAST_GC_CAUSE, trace.getDoubleGroup(16));
        concurrentModeFailure.add(heap.minus(tenured), tenured, heap);
        concurrentModeFailure.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        concurrentModeFailure.add(extractCPUSummary(line));

        publish(parNewPromotionFailed);
        publish(concurrentModeFailure);
    }

    public void concurrentPhaseStart(GCLogTrace trace, String line) {
        startOfConcurrentPhase = trace.getDateTimeStamp();
        inConcurrentPhase = true;
    }

    public void concurrentPhaseEnd(GCLogTrace trace, String line) {
        concurrentPhaseEnd(trace, line, 0);
    }

    public void concurrentPhaseEnd(GCLogTrace trace, String line, int offset) {
        try {
            double cpuTime = trace.getDoubleGroup(7 + offset);
            double wallClock = trace.getDoubleGroup(8 + offset);
            switch (trace.getGroup(6 + offset)) {
                case "mark":
                    publish(new ConcurrentMark(startOfConcurrentPhase, wallClock, cpuTime, wallClock));
                    break;
                case "preclean" :
                    publish(new ConcurrentPreClean(startOfConcurrentPhase, wallClock, cpuTime, wallClock));
                    break;
                case "abortable-preclean" :
                    publish(new AbortablePreClean(startOfConcurrentPhase, wallClock, cpuTime, wallClock, cpuTime > 0.0d));
                    break;
                case "sweep" :
                    publish(new ConcurrentSweep(startOfConcurrentPhase, wallClock, cpuTime, wallClock));
                    break;
                case "reset" :
                    publish(new ConcurrentReset(startOfConcurrentPhase, wallClock, cpuTime, wallClock));
                    break;
                default:
                    LOGGER.warning("concurrent phase not recognized end statement -> " + trace);
            }
        } catch (Exception e) {
            LOGGER.warning("concurrent phase end throws " + e.getMessage() + " for " + trace);
        }
    }

    //12.986: [GC[1 CMS-initial-mark: 33532K(62656K)] 49652K(81280K), 0.0014191 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
    //todo: Initial-mark rule maybe too greedy???
    public void initialMark(GCLogTrace trace, String line) {
        InitialMark initialMark = new InitialMark(getClock(), trace.gcCause(), trace.getDoubleGroup(trace.groupCount()));
        initialMark.add(trace.getOccupancyWithMemoryPoolSizeSummary(7), getTotalOccupancyWithTotalHeapSizeSummary(trace, 11));
        initialMark.add(extractCPUSummary(line));
        publish(initialMark);
    }

    //2399564.221: [GC[YG occupancy: 143736 K (2052672 K)]2399564.221: [GC 2399564.222: [ParNew: 143736K->43849K(2052672K), 10.4702845 secs] 2214360K->2115172K(6193728K), 10.4716661 secs
    //4981160.497: [GC[YG occupancy: 77784 K (153344 K)]4981160.498: [Rescan (parallel) , 0.1474240 secs]2014-06-10T17:50:11.873-0700: 4981160.645: [weak refs processing, 0.0000430 secs]2014-06-10T17:50:11.873-0700: 4981160.645: [scrub string table, 0.0009790 secs] [1 CMS-remark: 1897500K(1926784K)] 1975285K(2080128K), 0.1486260 secs] [Times: user=0.29 sys=0.00, real=0.15 secs]
    public void scavengeBeforeRemark(GCLogTrace trace, String line) {
        DateTimeStamp parNewStart = trace.getDateTimeStamp(2);
        ParNew parNew = new ParNew(parNewStart, GCCause.UNKNOWN_GCCAUSE, trace.getDuration());
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(20);
        MemoryPoolSummary tenured = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 27);
        parNew.add(young, tenured.minus(young), tenured);
        parNew.add(extractCPUSummary(line));
        publish(parNew);
    }

    public void scavengeBeforeRemarkTenuring(GCLogTrace trace, String line) {
        scavengeTimeStamp = trace.getDateTimeStamp(2);
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
    }

    public void parallelRemarkStringSymbolClause(GCLogTrace trace, String line) {
        recordRemark(trace, line, trace.gcCause());
    }

    //todo: outstanding question.. is ParNew scavenge before remark pause time included in the remark pause time
    //For the moment, assume not though it appears as it is.
    public void parallelRescan(GCLogTrace trace, String line) {
        CMSRemark collection;
        if (remarkTimeStamp != null) {
            collection = new CMSRemark(remarkTimeStamp, GCCause.CMS_FINAL_REMARK, trace.getDoubleGroup(trace.groupCount()));
            remarkTimeStamp = null;
        } else {
            collection = new CMSRemark(getClock(), GCCause.CMS_FINAL_REMARK, trace.getDuration());
        }

        MemoryPoolSummary tenured = trace.getOccupancyWithMemoryPoolSizeSummary(19);
        MemoryPoolSummary heap = getTotalOccupancyWithTotalHeapSizeSummary(trace, 23);
        collection.add(heap.minus(tenured), tenured, heap);
        recordRescanStepTimes(collection, line);
        collection.addReferenceGCSummary(extractPrintReferenceGC(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
        if (parNewForwardReference != null) {
            publish(parNewForwardReference);
            parNewForwardReference = null;
        }
    }

    public void rescanWeakClassSymbolString(GCLogTrace trace, String line) {
        CMSRemark remark = new CMSRemark(getClock(), GCCause.CMS_FINAL_REMARK, trace.getDuration());
        MemoryPoolSummary tenured = new MemoryPoolSummary(trace.toKBytes(20), trace.toKBytes(22), trace.toKBytes(20), trace.toKBytes(22));
        MemoryPoolSummary heap = new MemoryPoolSummary(trace.toKBytes(24), trace.toKBytes(26), trace.toKBytes(24), trace.toKBytes(26));
        remark.add(youngMemoryPoolSummaryForwardReference, tenured, heap);
        recordRescanStepTimes(remark, line);
        remark.add(extractCPUSummary(line));
        publish(remark);
    }

    public void recordRemark(GCLogTrace trace, String line) {
        recordRemark(trace, line, trace.gcCause());
    }

    public void remarkParNewPromotionFailed(GCLogTrace trace, String line) {
        ParNewPromotionFailed parNewPromotionFailed = new ParNewPromotionFailed(getClock(), GCCause.PROMOTION_FAILED, trace.getDoubleGroup(trace.groupCount()));
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(11);
        MemoryPoolSummary tenured = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 18);
        parNewPromotionFailed.add(young, tenured.minus(young), tenured);
        parNewPromotionFailed.add(extractCPUSummary(line));
        publish(parNewPromotionFailed);
    }

    //: 17337K->347K(18624K), 0.0007142 secs] 23862K->6872K(81280K), 0.0008003 secs]
    public void tenuringDetails(GCLogTrace trace, String line) {
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8);
        if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.ParNew) {
            ParNew collection = new ParNew(scavengeTimeStamp, gcCauseForwardReference, trace.getDuration());
            collection.add(young, heap);
            collection.add(referenceGCForwardReference);
            collection.add(extractCPUSummary(line));
            publish(collection);
        } else if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.DefNew) {
            DefNew collection = new DefNew(scavengeTimeStamp, gcCauseForwardReference, trace.getDoubleGroup(14));
            collection.add(young, heap);
            collection.add(referenceGCForwardReference);
            collection.add(extractCPUSummary(line));
            publish(collection);
        }
    }

    //(concurrent mode failure): 62169K->8780K(64768K), 0.0909138 secs] 79462K->8780K(83392K), [CMS Perm : 10688K->10687K(21248K)], 0.0938215 secs]
    //(concurrent mode failure): 1044403K->1048509K(1048576K), 26.3929433 secs] 1478365K->1059706K(1520448K), 26.7743013 secs]
    public void concurrentModeFailureDetails(GCLogTrace trace, String line) {
        FullGC failure;
        MemoryPoolSummary tenuredPoolSummary = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        MemoryPoolSummary heapSummary = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8);
        //Must be preceded by a young gen collection
        if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.ParNew) {
            ParNew collection = new ParNew(scavengeTimeStamp, gcCauseForwardReference, scavengeDurationForwardReference);
            collection.add(youngMemoryPoolSummaryForwardReference,
                    new MemoryPoolSummary(tenuredPoolSummary.getOccupancyBeforeCollection(), tenuredPoolSummary.getSizeBeforeCollection(), tenuredPoolSummary.getOccupancyBeforeCollection(), tenuredPoolSummary.getSizeAfterCollection()),
                    new MemoryPoolSummary(heapSummary.getOccupancyBeforeCollection(), heapSummary.getSizeBeforeCollection(), heapSummary.getOccupancyBeforeCollection(), heapSummary.getSizeAfterCollection()));
            collection.add(extractCPUSummary(line));
            publish(collection, false);
            failure = new ConcurrentModeFailure(getClock(), gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
            youngMemoryPoolSummaryForwardReference = heapSummary.minus(tenuredPoolSummary);
        } else if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.DefNew) {
            DefNew collection = new DefNew(scavengeTimeStamp, gcCauseForwardReference, scavengeDurationForwardReference);
            collection.add(youngMemoryPoolSummaryForwardReference,
                    new MemoryPoolSummary(tenuredPoolSummary.getOccupancyBeforeCollection(), tenuredPoolSummary.getSizeBeforeCollection(), tenuredPoolSummary.getOccupancyBeforeCollection(), tenuredPoolSummary.getSizeAfterCollection()),
                    new MemoryPoolSummary(heapSummary.getOccupancyBeforeCollection(), heapSummary.getSizeBeforeCollection(), heapSummary.getOccupancyBeforeCollection(), heapSummary.getSizeAfterCollection()));
            collection.add(extractCPUSummary(line));
            publish(collection, false);
            failure = new ConcurrentModeFailure(getClock(), gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
            youngMemoryPoolSummaryForwardReference = heapSummary.minus(tenuredPoolSummary);
        } else if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.ParNewPromotionFailed) {
            ParNewPromotionFailed collection = new ParNewPromotionFailed(scavengeTimeStamp, gcCauseForwardReference, scavengeDurationForwardReference);
            collection.add(youngMemoryPoolSummaryForwardReference,
                    new MemoryPoolSummary(tenuredPoolSummary.getOccupancyBeforeCollection(), tenuredPoolSummary.getSizeBeforeCollection(), tenuredPoolSummary.getOccupancyBeforeCollection(), tenuredPoolSummary.getSizeAfterCollection()),
                    new MemoryPoolSummary(heapSummary.getOccupancyBeforeCollection(), heapSummary.getSizeBeforeCollection(), heapSummary.getOccupancyBeforeCollection(), heapSummary.getSizeAfterCollection()));
            collection.add(extractCPUSummary(line));
            publish(collection, false);
            failure = new ConcurrentModeFailure(getClock(), gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
            youngMemoryPoolSummaryForwardReference = heapSummary.minus(tenuredPoolSummary);
        } else if ((garbageCollectionTypeForwardReference == GarbageCollectionTypes.ConcurrentModeFailure) ||
                (garbageCollectionTypeForwardReference == GarbageCollectionTypes.FullGC)) {
            if (line.contains("interrupted")) {
                failure = new ConcurrentModeInterrupted(fullGCTimeStamp, gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
            } else {
                failure = new ConcurrentModeFailure(fullGCTimeStamp, gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
            }
            if (youngMemoryPoolSummaryForwardReference == null)
                youngMemoryPoolSummaryForwardReference = heapSummary.minus(tenuredPoolSummary);
        } else {
            LOGGER.warning("Unable to parse -> " + trace);
            return;
        }

        failure.add(youngMemoryPoolSummaryForwardReference, tenuredPoolSummary, heapSummary);
        failure.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        failure.add(extractCPUSummary(line));
        publish(failure);
    }

    //: 196768K->18097K(471872K), 0.1321291 secs]198764.460: [CMS (concurrent mode failure): 473195K->376198K(1048576K), 25.1817732 secs] 668966K->376198K(1520448K), [CMS Perm : 131008K->27169K(131072K)], 25.3146647 secs]
    //: 439257K->439257K(471872K), 0.5857972 secs]898606.202: [CMS (concurrent mode failure): 1040282K->1048575K(1048576K), 28.6064288 secs] 1478173K->1055740K(1520448K), 29.1931179 secs]
    //: 189422K->15827K(471872K), 0.1282129 secs]131348.814: [CMS (concurrent mode failure): 927657K->279012K(1572864K), 23.8067389 secs] 1116483K->279012K(2044736K), [CMS Perm : 131071K->27250K(131072K)], 23.9363976 secs]
    //this one is tricky, the GC is caused by CMS Perm filling up. Take heap before GC as heap after GC for the ParNew

    /**
     * Internal memory pools have to be derived for the ParNew.
     * The following calculations were used to derive all pool sizes for the log entry below
     * : 189422K->15827K(471872K), 0.1282129 secs]131348.814: [CMS (concurrent mode failure): 927657K->279012K(1572864K), 23.8067389 secs] 1116483K->279012K(2044736K), [CMS Perm : 131071K->27250K(131072K)], 23.9363976 secs]
     * total heap for young = 189422K + 927657K -> 1116483K (2044736K), @ indexes   1 + 13 -> 20 (24)
     * total heap for CMF =   1116483K->279012K(2044736K)                               20 -> 22 (24)
     * tenured for CMF =      927657K->279012K(1572864K)                                13 -> 15 (17)
     * young for CMF = 15827K -> 0K (471872K)                                            3 -> 0K (5)
     * @param trace representing the log line
     * @param line the text for the log line (can be extracted from the trace using trace.group(0);
     *
     */
    public void parNewDetailsConcurrentModeFailure(GCLogTrace trace, String line) {
        ParNew parNew = new ParNew(scavengeTimeStamp, GCCause.UNKNOWN_GCCAUSE, trace.getDoubleGroup(7));
        MemoryPoolSummary heap = new MemoryPoolSummary(trace.toKBytes(1) + trace.toKBytes(13), trace.toKBytes(24), trace.toKBytes(20), trace.toKBytes(24));
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        parNew.add(young, heap);
        parNew.add(extractCPUSummary(line));
        publish(parNew, false);
        if ( inConcurrentPhase) {
            // todo: publish closing out concurrent phase
            LOGGER.warning("concurrent phase not closed");
        }

        ConcurrentModeFailure collection = new ConcurrentModeFailure(fullGCTimeStamp, gcCauseForwardReference, trace.getDuration());
        collection.add(
                new MemoryPoolSummary(
                        trace.toKBytes(3), trace.toKBytes(5), 0L, trace.toKBytes(5)),
                        trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(13),
                        trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(20));
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    /*
    1590434.953: [Full GC 1590434.953: [ParNew
    : 67099K->15426K(471872K), 0.1365978 secs]1590435.090: [CMS1590450.859: [CMS-concurrent-mark: 17.641/17.926 secs]
     (concurrent mode failure): 1324520K->1267118K(1572864K), 66.4047026 secs] 1390729K->1267118K(2044736K), [CMS Perm : 131038K->28988K(131072K)], 66.5420941 secs]
     */
    //ParNew reverts to concurrent-mode-failure so we're not done yet so don't set lookingForStartOfRecord
    //: 17395K->17756K(18624K), 0.0027477 secs]15.601: [CMS15.602: [CMS-concurrent-abortable-preclean: 0.026/0.772 secs]
    public void parNewDetailsPromotionFailedWithConcurrentMarkSweepPhase(GCLogTrace trace, String line) {
        youngMemoryPoolSummaryForwardReference = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        scavengeDurationForwardReference = trace.getDoubleGroup(7);
        GCLogTrace concurrentPhase = CONCURRENT_PHASE_END_BLOCK.parse(line);
        if (concurrentPhase != null)
            endOfConcurrentPhase(concurrentPhase,line);
    }

    //: 1069879K->1069879K(1090560K), 0.3135220 secs]2014-09-19T06:07:23.135+0200: 73512.294: [CMS: 1613084K->823344K(2423488K), 3.5186340 secs] 2639961K->823344K(3514048K), [CMS Perm : 205976K->205949K(343356K)], 3.8323790 secs] [Times: user=4.44 sys=0.00, real=3.83 secs]
    //: 19134K->19136K(19136K), 0.0493809 secs]236.955: [CMS: 107351K->79265K(107776K), 0.2540576 secs] 119733K->79265K(126912K), [CMS Perm : 14256K->14256K(24092K)], 0.3036551 secs]
    //: 2368K->319K(2368K), 0.0063634 secs]5.353: [Tenured: 5443K->5196K(5504K), 0.0830293 secs] 7325K->5196K(7872K), [Perm : 10606K->10606K(21248K)], 0.0895957 secs]
    public void parNewDetailsWithConcurrentModeFailure(GCLogTrace trace, String line) {
        ParNewPromotionFailed collection = new ParNewPromotionFailed(scavengeTimeStamp, gcCauseForwardReference, trace.getDoubleGroup(7));
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        MemoryPoolSummary heap = new MemoryPoolSummary(trace.toKBytes(20), trace.toKBytes(24), trace.toKBytes(20), trace.toKBytes(24));
        MemoryPoolSummary tenured = heap.minus(young);
        collection.add(young, tenured, heap);
        collection.add(extractCPUSummary(line));
        publish(collection, false);

        ConcurrentModeFailure fullCollection = new ConcurrentModeFailure(getClock(), gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
        tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(13);
        heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 20);
        young = heap.minus(tenured);
        fullCollection.add(young, tenured, heap);
        fullCollection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        fullCollection.add(extractCPUSummary(line));
        publish(fullCollection);
    }

    //concerned about this rule as the occupancy here is at 26 but I have a test at 31 and
    public void concurrentModeFailureReference(GCLogTrace trace, String line) {
        GCLogTrace memoryPoolSummary = POOL_OCCUPANCY_HEAP_OCCUPANCY_BLOCK.parse(line);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(memoryPoolSummary, 8);
        MemoryPoolSummary tenured = memoryPoolSummary.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        FullGC collection;
        if (fullGCTimeStamp == null) {
            fullGCTimeStamp = getClock().add(parNewForwardReference.getDuration());
            //need to estimate the pool occupancies and sizes for the ParNew
            MemoryPoolSummary parNewHeap = new MemoryPoolSummary(heap.getOccupancyBeforeCollection(), heap.getSizeAfterCollection(), heap.getOccupancyBeforeCollection(), heap.getSizeAfterCollection());
            MemoryPoolSummary parNewTenured = parNewHeap.minus(youngMemoryPoolSummaryForwardReference);
            parNewForwardReference.add(youngMemoryPoolSummaryForwardReference, parNewTenured, parNewHeap);
            publish(parNewForwardReference, false);
            collection = new ConcurrentModeFailure(fullGCTimeStamp, GCCause.CMS_FAILURE, trace.getDoubleGroup(trace.groupCount()));
        } else {
            collection = new FullGC(fullGCTimeStamp, GCCause.CMS_FAILURE, trace.getDoubleGroup(trace.groupCount()));
        }
        collection.add(heap.minus(tenured), tenured, heap);
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractPrintReferenceGC(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }


    public void iCMSConcurrentModeFailureDuringParNewDefNewTenuringDetails(GCLogTrace trace, String line) {
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8);
        int dutyCycle = trace.getIntegerGroup(14);

        if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.ParNew) {
            ParNew collection = new ParNew(scavengeTimeStamp, gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
            collection.add(young, heap.minus(young), heap);
            collection.recordDutyCycle(dutyCycle);
            collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
            collection.add(extractCPUSummary(line));
            publish(collection);
        }

        /*
         This is really a System.gc() call.
         12525.344: [Full GC 12525.344: [ParNew
         Desired survivor size 32768 bytes, new threshold 0 (max 0)
         : 214K->0K(81856K), 0.0211649 secs] 1423543K->1423340K(2097088K) icms_dc=39 , 0.0218647 secs]
         */
        else if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.FullGC) {
            SystemGC collection = new SystemGC(fullGCTimeStamp, GCCause.JAVA_LANG_SYSTEM, trace.getDoubleGroup(trace.groupCount()));
            collection.add(young, heap.minus(young), heap);
            collection.recordDutyCycle(dutyCycle);
            collection.add(extractCPUSummary(line));
            publish(collection);
        } else if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.ParNewPromotionFailed) {
            ParNewPromotionFailed collection = new ParNewPromotionFailed(scavengeTimeStamp, GCCause.PROMOTION_FAILED, trace.getDoubleGroup(trace.groupCount()));
            collection.add(young, heap.minus(young), heap);
            collection.recordDutyCycle(dutyCycle);
            collection.add(extractCPUSummary(line));
            publish(collection);
        } else
            LOGGER.log(Level.WARNING, "@" + scavengeTimeStamp + ". ParNew details not preceded by a ParNew: " + garbageCollectionTypeForwardReference);
    }

    //(concurrent mode failure): 8465K->22006K(1926784K), 0.3222180 secs] 28136K->22006K(2080128K), [CMS Perm : 51702K->51627K(52016K)] icms_dc=5 , 0.3224000 secs]
    //(concurrent mode failure): 1696350K->613432K(2015232K), 22.0030939 secs] 1772267K->613432K(2097088K) icms_dc=100 , 22.3467890 secs]
    public void iCMSConcurrentModeFailure(GCLogTrace trace, String line) {
        if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.FullGC || garbageCollectionTypeForwardReference == GarbageCollectionTypes.ConcurrentModeFailure) {
            ConcurrentModeFailure failure = new ConcurrentModeFailure(fullGCTimeStamp, GCCause.UNKNOWN_GCCAUSE, trace.getDuration());
            failure.add(getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8));
            failure.recordDutyCycle(trace.getIntegerGroup(21));
            failure.add(extractCPUSummary(line));
            publish(failure);
        } else if ((garbageCollectionTypeForwardReference == GarbageCollectionTypes.ParNew) || (garbageCollectionTypeForwardReference == GarbageCollectionTypes.ParNewPromotionFailed)) {
            ParNew collection;
            if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.ParNew)
                collection = new ParNew(scavengeTimeStamp, gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
            else
                collection = new ParNewPromotionFailed(scavengeTimeStamp, trace.getDoubleGroup(trace.groupCount()) - trace.getDoubleGroup(7));

            collection.add(youngMemoryPoolSummaryForwardReference, new MemoryPoolSummary(trace.getLongGroup(8), trace.getLongGroup(12), trace.getLongGroup(8), trace.getLongGroup(12)));
            collection.add(extractCPUSummary(line));
            publish(collection, false);
            if (fullGCTimeStamp == null) {
                fullGCTimeStamp = scavengeTimeStamp.add(trace.getDoubleGroup(7));
            }
            ConcurrentModeFailure failure = new ConcurrentModeFailure(fullGCTimeStamp, gcCauseForwardReference, trace.getDoubleGroup(7));
            MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8);
            MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
            failure.add(heap.minus(tenured), tenured, heap);
            failure.recordDutyCycle(trace.getIntegerGroup(21));
            failure.add(extractCPUSummary(line));
            publish(failure);
        } else {
            LOGGER.warning("Unable to parse -> " + trace);
        }
    }

    /*
     * Record is produced *after* CMS-reset completed.
     * 16253.906: [GC 16253.906: [ParNew (promotion failed)
     * Desired survivor size 32768 bytes, new threshold 0 (max 0)
     * : 81792K->81792K(81856K), 0.2765545 secs]16254.183: [CMS (concurrent mode failure): 1552479K->933994K(2015232K), 22.9932464 secs] 1611199K->933994K(2097088K) icms_dc=57 , 23.2713493 secs]
     */
    public void iCMSConcurrentModeFailureDuringParNewDefNewDetails(GCLogTrace trace, String line) {
        {
            ParNewPromotionFailed collection = new ParNewPromotionFailed(scavengeTimeStamp, trace.getDoubleGroup(7));
            MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
            MemoryPoolSummary heap = new MemoryPoolSummary(trace.toKBytes(20), trace.toKBytes(24), trace.toKBytes(20), trace.toKBytes(24));
            collection.add(young, heap.minus(young), heap);
            collection.add(extractCPUSummary(line));
            publish(collection);
        }

        {
            ConcurrentModeFailure collection = new ConcurrentModeFailure(getClock(), GCCause.CMS_FAILURE, trace.getDuration());
            MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(13);
            MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 20);
            collection.add(heap.minus(tenured), tenured, heap);
            collection.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
            collection.add(extractCPUSummary(line));
            publish(collection);
        }
    }

    //49.191: [Full GC 49.191: [CMS49.230: [CMS-concurrent-preclean: 0.353/1.463 secs]
    public void fullGCInterruptsConcurrentPhase(GCLogTrace trace, String line) {
        fullGCTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.FullGC;
        gcCauseForwardReference = trace.gcCause();
        endOfConcurrentPhase(trace,trace.getDateTimeStamp(3), 11);
    }

    public void fullGCReferenceConcurrentModeFailure(GCLogTrace trace, String line) {
        ConcurrentModeFailure collection = new ConcurrentModeFailure(getClock(), trace.gcCause(), trace.getDoubleGroup(trace.groupCount()));
        GCLogTrace memorySummary = MEMORY_SUMMARY_RULE.parse(line);
        MemoryPoolSummary tenured = memorySummary.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(memorySummary.next(), 1);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(memorySummary.next(), 1);
        collection.add(heap.minus(tenured), tenured, heap);
        collection.addReferenceGCSummary(extractPrintReferenceGC(line));
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //12525.344: [Full GC 12525.344: [ParNew
    public void fullParNewStart(GCLogTrace trace, String line) {
        fullGCTimeStamp = getClock();
        scavengeTimeStamp = trace.getDateTimeStamp(2);
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.FullGC;
        gcCauseForwardReference = trace.gcCause();
    }

    public void iCMSParNew(GCLogTrace trace, String line) {
        int offset = TIME_DATE_OFFSET * 2;
        ParNew parNew = new ParNew(getClock(), trace.gcCause(), trace.getDuration());
        parNew.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(offset), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, offset + 7));
        parNew.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
        parNew.add(extractCPUSummary(line));
        publish(parNew);
    }

    //Full GC with ParNew promotion failed.
    //"445909.040: [GC 445909.040: [ParNew (1: promotion failure size = 4629668)  (promotion failed): 460096K->460096K(460096K), 0.8467130 secs]445909.887: [CMS: 6252252K->2709964K(7877440K), 23.9213270 secs] 6637399K->2709964K(8337536K), [CMS Perm : 1201406K->68157K(2097152K)] icms_dc=0 , 24.7685320 secs]
    public void iCMSParNewPromotionFailureRecord(GCLogTrace trace, String line) {
        ParNewPromotionFailed parNewPromotionFailed = new ParNewPromotionFailed(getClock(), GCCause.PROMOTION_FAILED, trace.getDoubleGroup(19));
        MemoryPoolSummary heap = new MemoryPoolSummary(trace.toKBytes(32), trace.toKBytes(36), trace.toKBytes(32), trace.toKBytes(36));
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(13);
        MemoryPoolSummary tenured = heap.minus(young);
        parNewPromotionFailed.add(young, tenured, heap);
        parNewPromotionFailed.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 2));
        publish(parNewPromotionFailed);
        //Synthesis a Full GC
        DateTimeStamp fullGCStart = trace.getDateTimeStamp(3);
        fullGCStart = getClock().add(fullGCStart.getTimeStamp() - getClock().getTimeStamp());
        FullGC fullGC = new FullGC(fullGCStart, GCCause.PROMOTION_FAILED, trace.getDoubleGroup(31));
        heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 32);
        tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(25);
        young = new MemoryPoolSummary(0L, trace.toKBytes(17), 0L, trace.toKBytes(17));
        fullGC.add(young, tenured, heap);
        fullGC.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        fullGC.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 2));
        publish(fullGC);
    }

    public void iCMSParNewPromotionFailure(GCLogTrace trace, String line) {
        double parnewDuration = trace.getDoubleGroup((2 * TIME_DATE_OFFSET) + 6);
        ParNewPromotionFailed parNewPromotionFailed = new ParNewPromotionFailed(getClock(), GCCause.PROMOTION_FAILED, parnewDuration);
        MemoryPoolSummary heap = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary((3 * TIME_DATE_OFFSET) + 13);
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(2 * TIME_DATE_OFFSET);
        MemoryPoolSummary tenured = heap.minus(young);
        parNewPromotionFailed.add(young, tenured, new MemoryPoolSummary(heap.getOccupancyBeforeCollection(),heap.getSizeBeforeCollection(),heap.getOccupancyBeforeCollection(),heap.getSizeBeforeCollection()));
        parNewPromotionFailed.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
        publish(parNewPromotionFailed);

        //Synthesis a Full GC
        DateTimeStamp fullGCStart = trace.getDateTimeStamp(3);
        ConcurrentModeFailure fullGC = new ConcurrentModeFailure(fullGCStart, GCCause.PROMOTION_FAILED, trace.getDuration() - parnewDuration);
        tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary((3 * TIME_DATE_OFFSET) + 6);
        young = new MemoryPoolSummary(young.getOccupancyBeforeCollection(), young.getSizeBeforeCollection(), 0L, young.getSizeAfterCollection());
        fullGC.add(young, tenured, heap);
        fullGC.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        fullGC.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
        publish(fullGC);
    }

    //85.577: [Full GC 85.577: [CMS: 5K->1883K(1926784K), 9.8521080 secs] 20369K->1883K(2080128K), [CMS Perm : 13770K->13604K(22970K)] icms_dc=100 , 9.8522900 secs]
    //25.846: [Full GC 25.847: [CMS: 0K->7019K(2015232K), 5.6129510 secs] 11161K->7019K(2097088K), [CMS Perm : 68104K->67271K(13520K)] icms_dc=0 , 25.6146105 secs]
    public void fullGCiCMS(GCLogTrace trace, String line) {
        FullGC collection;
        if (trace.gcCause() == GCCause.JAVA_LANG_SYSTEM)
            collection = new SystemGC(getClock(), trace.gcCause(), trace.getDuration());
        else
            collection = new FullGC(getClock(), trace.gcCause(), trace.getDuration());
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(2 + TIME_DATE_OFFSET);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, (TIME_DATE_OFFSET) + 9);
        collection.add(heap.minus(tenured), tenured, heap);
        collection.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //: 81792K->0K(81856K), 0.0431036 secs] 90187K->11671K(2097088K) icms_dc=5 , 0.0435503 secs]
    public void iCMSParNewDefNewTenuringDetails(GCLogTrace trace, String line) {
        if (GarbageCollectionTypes.ParNew == garbageCollectionTypeForwardReference) {
            ParNew collection = new ParNew(scavengeTimeStamp, gcCauseForwardReference, trace.getDuration());
            collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8));
            collection.recordDutyCycle(trace.getIntegerGroup(14));
            collection.add(extractCPUSummary(line));
            publish(collection);
        } else if (GarbageCollectionTypes.ParNewPromotionFailed == garbageCollectionTypeForwardReference) {
            ParNewPromotionFailed collection = new ParNewPromotionFailed(scavengeTimeStamp, gcCauseForwardReference, trace.getDuration());
            MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
            MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8);
            collection.add(young, heap.minus(young), heap);
            collection.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
            publish(collection);
        } else if (GarbageCollectionTypes.FullGC == garbageCollectionTypeForwardReference) {
            FullGC collection = new FullGC(fullGCTimeStamp, gcCauseForwardReference, trace.getDuration());
            MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
            MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8);
            collection.add(young, heap.minus(young), heap);
            collection.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
            publish(collection);
        } else {
            LOGGER.warning("Not reported: " + line);
        }
    }

    //2013-06-06T14:12:49.554+0200: 534744,148: [Full GC (System) 534744,148: [CMS: 1513767K->410320K(3512768K), 2,1361260 secs] 1598422K->410320K(4126208K), [CMS Perm : 120074K->119963K(200424K)] icms_dc=0 , 2,1363550 secs]

    //35305.590: [GC 35305.590: [ParNew (promotion failed): 10973K->3147K(153344K), 0.0295730 secs] 1763339K->1763817K(2080128K) icms_dc=32 , 0.0297680 secs]
    public void iCMSPromotionFailed(GCLogTrace trace, String line) {
        ParNewPromotionFailed collection = new ParNewPromotionFailed(getClock(), trace.getDuration());
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(TIME_DATE_OFFSET + 2);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, TIME_DATE_OFFSET + 9);
        collection.add(young, heap.minus(young), heap);
        collection.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
        publish(collection);
    }

    //2015-08-06T16:26:47.553+0200: 3913.750: [Full GC (System) 2015-08-06T16:26:47.553+0200: 3913.751: [CMS: 1637962K->788069K(2359296K), 5.9688430 secs] 1680897K->788069K(3122624K), [CMS Perm : 147747K->147352K(200188K)] icms_dc=0 , 5.9729544 secs]
    private void iCMSFullGC(GCLogTrace trace, String line) {
        FullGC collection;
        GCCause cause = trace.gcCause();
        if (cause == GCCause.JAVA_LANG_SYSTEM)
            collection = new SystemGC(getClock(), trace.getDuration());
        else if ((cause == GCCause.UNKNOWN_GCCAUSE) || (cause == GCCause.GCCAUSE_NOT_SET)) {
            collection = new FullGC(getClock(), cause, trace.getDuration());
        } else {
            LOGGER.warning("Unable to parse -> " + trace);
            return;
        }

        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(12);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 19);
        collection.add(heap.minus(tenured), tenured, heap);
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
        publish(collection);
    }

    //91057.643: [GC 91057.643: [ParNew: 153344K->153344K(153344K), 0.0000250 secs]91057.643: [CMS: 1895319K->1754894K(1926784K), 9.6910090 secs] 2048663K->1754894K(2080128K), [CMS Perm : 135228K->134975K(230652K)] icms_dc=65 , 9.6912470 secs]
    public void iCMSMislabeledFull(GCLogTrace trace, String line) {
        double parNewDuration = trace.getDoubleGroup((TIME_DATE_OFFSET * 2) + 6);
        ParNew parNew = new ParNew(trace.getDateTimeStamp(2), trace.gcCause(), parNewDuration);
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(TIME_DATE_OFFSET * 2);
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary((TIME_DATE_OFFSET * 2) + 12);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, (TIME_DATE_OFFSET * 2) + 19);
        parNew.add(young,
                new MemoryPoolSummary(tenured.getOccupancyBeforeCollection(), tenured.getSizeAfterCollection(), tenured.getOccupancyBeforeCollection(), tenured.getSizeAfterCollection()),
                new MemoryPoolSummary(heap.getOccupancyBeforeCollection(), heap.getSizeAfterCollection(), heap.getOccupancyBeforeCollection(), heap.getSizeAfterCollection()));
        publish(parNew);

        FullGC full = new FullGC(trace.getDateTimeStamp(3), trace.gcCause(), trace.getDuration());
        full.add(heap.minus(tenured), tenured, heap);
        full.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
        full.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        full.add(extractCPUSummary(line));
        publish(full);
    }

    //10110.232: [Full GC 10110.232: [CMS (concurrent mode failure): 2715839K->1659203K(2752512K), 39.3188254 secs] 2740830K->1659203K(3106432K), [CMS Perm : 206079K->204904K(402264K)] icms_dc=100 , 39.3199631 secs] [Times: user=39.37 sys=0.02, real=39.32 secs]
    public void iCMSFullAfterConcurrentModeFailure(GCLogTrace trace, String line) {
        ConcurrentModeFailure failure = new ConcurrentModeFailure(getClock(), GCCause.UNKNOWN_GCCAUSE, trace.getDuration());
        failure.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(TIME_DATE_OFFSET * 2), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, (TIME_DATE_OFFSET * 2) + 7));
        failure.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
        failure.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        failure.add(extractCPUSummary(line));
        publish(failure);
    }

    //2015-07-26T00:18:34.603+0200: 86048.777: [Full GC (System) 86048.777: [CMS2015-07-26T00:18:35.590+0200: 86049.763: [CMS-concurrent-mark: 0.982/41377.748 secs] [Times: user=4426.99 sys=2507.75, real=41377.75 secs]
    //(concurrent mode interrupted): 2286052K->845009K(3908584K), 6.1999933 secs] 2673522K->845009K(4671912K), [CMS Perm : 137744K->133435K(228656K)] icms_dc=0 , 6.2007727 secs] [Times: user=7.11 sys=0.02, real=6.20 secs]
    public void iCMSConcurrentModeInterrupted(GCLogTrace trace, String line) {
        if (fullGCTimeStamp == null) {
            fullGCTimeStamp = getClock();
            logMissedFirstRecordForEvent(line);
        }
        ConcurrentModeInterrupted collection = new ConcurrentModeInterrupted(fullGCTimeStamp, garbageCollectionTypeForwardReference, gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8);
        collection.add(heap.minus(tenured), tenured, heap);
        collection.recordDutyCycle(trace.getIntegerGroup(trace.groupCount() - 1));
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //42384.024: [Full GC [PSYoungGen: 1696K->0K(70464K)] [PSFull: 928442K->125867K(932096K)] 930139K->125867K(1002560K) [PSPermGen: 37030K->37030K(65536K)], 117.0312620 secs]
    public void psFullGCMeta(GCLogTrace trace, String line) {
        GCCause cause = trace.gcCause();
        FullGC collection;
        if (cause == GCCause.JAVA_LANG_SYSTEM || cause == GCCause.HEAP_DUMP) {
            collection = new SystemGC(getClock(), cause, trace.getDuration());
        } else {
            collection = new PSFullGC(getClock(), cause, trace.getDuration());
        }
        collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(TIME_DATE_OFFSET + 2), trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(TIME_DATE_OFFSET + 8), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, TIME_DATE_OFFSET + 14));
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //1.147: [Full GC (System) 1.147: [Tenured: 1636K->1765K(5312K), 0.0488106 secs] 2894K->1765K(7616K), [Perm : 10112K->10112K(21248K)], 0.0488934 secs]
    public void psFullGCV2Meta(GCLogTrace trace, String line) {
        SystemGC collection = new SystemGC(getClock(), trace.getDoubleGroup(trace.groupCount()));
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(5);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 12);
        collection.add(heap.minus(tenured), tenured, heap);
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    public void parNewConcurrentModeFailurePerm(GCLogTrace trace, String line) {
        double concurrentModeFailurePause = trace.getDoubleGroup(26);
        double parNewPause = trace.getDoubleGroup(18);
        double startTimeGap = trace.getDoubleGroup(13) - getClock().getTimeStamp();

        ParNewPromotionFailed parNewPromotionFailed = new ParNewPromotionFailed(getClock(), trace.gcCause(), parNewPause);
        MemoryPoolSummary heap = new MemoryPoolSummary(trace.toKBytes(27), trace.toKBytes(31), trace.toKBytes(27), trace.toKBytes(31));
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(12);

        parNewPromotionFailed.add(young, heap.minus(young), heap);
        publish(parNewPromotionFailed);

        ConcurrentModeFailure concurrentModeFailure = new ConcurrentModeFailure(getClock().add(startTimeGap), GCCause.PROMOTION_FAILED, concurrentModeFailurePause);

        heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 27);
        young = new MemoryPoolSummary( trace.toKBytes(14), trace.toKBytes(16), 0, trace.toKBytes(16));
        concurrentModeFailure.add(young, heap.minus(young), heap);
        concurrentModeFailure.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        concurrentModeFailure.add(extractCPUSummary(line));
        publish(concurrentModeFailure);

    }

    public void fullSplitByConcurrentPhase(GCLogTrace trace, String line) {
    }

    public void concurrentModeFailureSplitByLargeBlock(GCLogTrace trace, String line) {
        if ( garbageCollectionTypeForwardReference == GarbageCollectionTypes.FullGC)
            garbageCollectionTypeForwardReference = GarbageCollectionTypes.ConcurrentModeFailure;
    }

    public void fullParNewConcurrentModeFailurePerm(GCLogTrace trace, String line) {
        extractPermOrMetaspaceRecord(line);
    }

    public void fullParNewConcurrentModeFailureMeta(GCLogTrace trace, String line) {
        extractPermOrMetaspaceRecord(line);
    }

    // TODO: #153 Concurrent mode failures not yet implemented
    //4.327: [FUll GC 4.328: [ParNew: 196768K->180907K(471872K), 0.1321291 secs]4.460: [CMS (concurrent mode failure): 473195K->376198K(1048576K), 5.1817732 secs] 668966K->376198K(1520448K), [CMS Perm : 13108K->27169K(13172K)], 5.3146647 secs]
    //8.828: [Full GC 8.828: [CMS (concurrent mode failure): 630985K->795001K(6470068K), 0.0895496 secs] 810101K->790051K(8300392K), [CMS Perm : 10696K->10696K(21248K)], 0.0896445 secs]


    //1.147: [Full GC (System) 1.147: [Tenured: 1636K->1765K(5312K), 0.0488106 secs] 2894K->1765K(7616K), [Perm : 10112K->10112K(21248K)], 0.0488934 secs]
    public void psFullGCV2Perm(GCLogTrace trace, String line) {
        SystemGC collection = new SystemGC(getClock(), trace.getDoubleGroup(trace.groupCount()));
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(5);
        MemoryPoolSummary heap = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 12);
        collection.add(heap.minus(tenured), tenured, heap);
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //42384.024: [Full GC [PSYoungGen: 1696K->0K(70464K)] [PSFull: 928442K->125867K(932096K)] 930139K->125867K(1002560K) [PSPermGen: 37030K->37030K(65536K)], 117.0312620 secs]
    public void psFullGCPerm(GCLogTrace trace, String line) {
        GCCause cause = trace.gcCause();
        FullGC collection;
        if (cause == GCCause.JAVA_LANG_SYSTEM || cause == GCCause.HEAP_DUMP) {
            collection = new SystemGC(getClock(), cause, trace.getDuration());
        } else {
            collection = new PSFullGC(getClock(), cause, trace.getDuration());
        }
        collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(8), trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(14), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 20));
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //0.645: [Full GC (System) 0.645: [CMS: 1170K->1606K(3328K), 0.0434830 secs] 2474K->1606K(5504K), [CMS Perm : 9606K->9552K(21248K)], 0.0436041 secs] [Times: user=0.04 sys=0.01, real=0.04 secs]
    //51712.306: [Full GC 51712.307: [CMS: 545513K->511751K(1398144K), 1.9291100 secs] 771779K->511751K(2027264K), [CMS Perm : 151637K->151527K(262144K)], 1.9292970 secs]
    //1.147: [Full GC (System) 1.147: [Tenured: 1827K->1889K(5312K), 0.0478658 secs] 2441K->1889K(7616K), [Perm : 10118K->10118K(21248K)], 0.0479395 secs]
    public void cmsFullPermOrMeta(GCLogTrace trace, String line) {
        try {
            FullGC collection;
            GCCause cause = trace.gcCause();
            if (cause == GCCause.JAVA_LANG_SYSTEM || cause == GCCause.HEAP_DUMP) {
                collection = new SystemGC(getClock(), cause, trace.getDoubleGroup(trace.groupCount()));
            } else {
                collection = new FullGC(getClock(), cause, trace.getDoubleGroup(trace.groupCount()));
            }
            GCLogTrace memorySummary = MEMORY_SUMMARY_RULE.parse(line);
            MemoryPoolSummary tenuredPoolSummary = memorySummary.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
            MemoryPoolSummary heapSummary = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(memorySummary.next(), 1);
            collection.add(heapSummary.minus(tenuredPoolSummary), tenuredPoolSummary, heapSummary);
            collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
            collection.add(extractPrintReferenceGC(line));
            collection.add(extractCPUSummary(line));
            publish(collection);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, getClock().getTimeStamp() + " : parsing record resulted in an exception", t);
        }
    }


    //285.945: [ParNew 173250K->163849K(190460K), 0.0044482 secs]
    public void parNewNoDetails(GCLogTrace trace, String line) {
        ParNew parNew = new ParNew(getClock(), GarbageCollectionTypes.Young, GCCause.UNKNOWN_GCCAUSE, trace.getDoubleGroup(trace.groupCount()));
        parNew.add(getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 3));
        parNew.add(extractCPUSummary(line));
        publish(parNew);
    }

    public void youngNoDetails(GCLogTrace trace, String line) {
        YoungGC youngGC = new YoungGC(getClock(), GarbageCollectionTypes.Young, GCCause.UNKNOWN_GCCAUSE, trace.getDuration());
        youngGC.add(getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 6));
        youngGC.add(extractCPUSummary(line));
        publish(youngGC);
    }

    public void cmsNoDetails(GCLogTrace trace, String line) {
        if (expectRemark) {
            expectRemark = false;
            CMSRemark remark = new CMSRemark(getClock(), trace.getDoubleGroup(trace.groupCount()));
            remark.add(getTotalOccupancyWithTotalHeapSizeSummary(trace, 6));
            remark.add(extractCPUSummary(line));
            publish(remark);
        } else {
            expectRemark = true;
            InitialMark initialMark = new InitialMark(getClock(), trace.getDoubleGroup(trace.groupCount()));
            initialMark.add(getTotalOccupancyWithTotalHeapSizeSummary(trace, 6));
            initialMark.add(extractCPUSummary(line));
            publish(initialMark);
        }
    }

    // 29.975: [Full GC 155252K->44872K(205568K), 0.3398460 secs]
    public void fullNoGCDetails(GCLogTrace trace, String line) {
        expectRemark = false;
        FullGC fullGC = new FullGC(getClock(), GCCause.UNKNOWN_GCCAUSE, trace.getDuration());
        fullGC.add(getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 6));
        publish(fullGC);
    }

    //81627.388: [ParNew
    //17552K->6881K(79808K), 0.0025012 secs]
    public void parNewStart(GCLogTrace trace, String line) {
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        if (gcCauseForwardReference == null)
            gcCauseForwardReference = GCCause.UNKNOWN_GCCAUSE;
        scavengeTimeStamp = getClock();
    }

    //0.839: [GC
    //split by tenuring distribution records or misc debug statements.
    public void gcStart(GCLogTrace trace, String line) {
        scavengeTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.Young;
        gcCauseForwardReference = trace.gcCause();
    }

    public void youngSplitNoDetails(GCLogTrace trace, String line) {
        if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.ParNew) {
            ParNew parNew = new ParNew(scavengeTimeStamp, GarbageCollectionTypes.ParNew, GCCause.UNKNOWN_GCCAUSE, trace.getDoubleGroup(trace.groupCount()));
            parNew.add(getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 1));
            parNew.add(extractCPUSummary(line));
            publish(parNew);
        } else if (garbageCollectionTypeForwardReference == GarbageCollectionTypes.Young) {
            YoungGC youngGC = new YoungGC(scavengeTimeStamp, GarbageCollectionTypes.Young, GCCause.UNKNOWN_GCCAUSE, trace.getDoubleGroup(trace.groupCount()));
            youngGC.add(getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 1));
            youngGC.add(extractCPUSummary(line));
            publish(youngGC);
        }
    }

    //134883.104: [GC-- 2411642K->2456159K(2500288K), 0.1478340 secs]
    //rarely seen!
    public void cmfSimple(GCLogTrace trace, String line) {
        ConcurrentModeFailure concurrentModeFailure = new ConcurrentModeFailure(getClock(), trace.gcCause(), trace.getDoubleGroup(trace.groupCount()));
        concurrentModeFailure.add(extractCPUSummary(line));
        publish(concurrentModeFailure);
    }

    //939.183: [GC [PSYoungGen: 523744K->844K(547584K)] 657668K->135357K(1035008K), 0.0157986 secs] [Times: user=0.30 sys=0.01, real=0.02 secs]
    public void psYoungGen(GCLogTrace trace, String line) {
        PSYoungGen collection = new PSYoungGen(getClock(), trace.gcCause(), trace.getDoubleGroup(trace.groupCount()));
        collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(8), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 14));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    // #433 - Offset for this call needed to be incremented by 3 to 7 (previously 4)
    //23.331: [Full GC (Metadata GC Threshold)  17386K->16928K(415232K), 0.0498462 secs]
    public void psFull(GCLogTrace trace, String line) {
        FullGC collection;
        GCCause cause = trace.gcCause();
        if ((cause == GCCause.JAVA_LANG_SYSTEM) || (cause == GCCause.HEAP_DUMP) || (cause == GCCause.HEAP_INSPECTION)) {
            collection = new SystemGC(trace.getDateTimeStamp(), cause, trace.getDuration());
        } else {
            collection = new FullGC(trace.getDateTimeStamp(), cause, trace.getDuration());
        }
        collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(7));
        publish(collection);
    }

    // #433 - Offset for this call needed to be incremented by 3 to 7. (previously 4)
    //13.563: [GC (Allocation Failure)  886080K->31608K(1986432K), 0.0392109 secs]
    public void psYoungNoDetails(GCLogTrace trace, String line) {
        GCCause cause = trace.gcCause();
        if (GCCause.JAVA_LANG_SYSTEM == cause) { // bug in 1.8.0_121 makes Full System.gc() look like a young collection
            SystemGC collection = new SystemGC(trace.getDateTimeStamp(), GCCause.JAVA_LANG_SYSTEM, trace.getDuration());
            collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(7));
            publish(collection);
        } else {
            PSYoungGen collection = new PSYoungGen(trace.getDateTimeStamp(), cause, trace.getDuration());
            collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(7));
            publish(collection);
        }
    }

    public void psYoungGenReferenceProcessingSplit(GCLogTrace trace, String line) {
        scavengeTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
        referenceGCForwardReference = extractPrintReferenceGC(line);
    }

    public void psYoungGenReferenceProcessing(GCLogTrace trace, String line) {
        PSYoungGen collection = new PSYoungGen(getClock(), trace.gcCause(), trace.getDuration());
        referenceGCForwardReference = extractPrintReferenceGC(line);
        collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(28), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 34));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //2011-02-12T14:13:59.378+0000: 50128.698: [GC
    //[PSYoungGen: 2763743K->13920K(2766144K)] 8155872K->5406505K(8358592K), 0.0572110 secs]
    //12288K->1648K(47104K), 0.0084652 secs]
    public void psTenuringStart(GCLogTrace trace, String line) {
        scavengeTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
    }

    public void psFullSPlit(GCLogTrace trace, String line) {
        fullGCTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
        if (gcCauseForwardReference == GCCause.JAVA_LANG_SYSTEM)
            garbageCollectionTypeForwardReference = GarbageCollectionTypes.SystemGC;
        else
            garbageCollectionTypeForwardReference = GarbageCollectionTypes.PSFull;
    }

    public void psFullReferenceSplit(GCLogTrace trace, String line) {
        fullGCTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
        if (gcCauseForwardReference == GCCause.JAVA_LANG_SYSTEM)
            garbageCollectionTypeForwardReference = GarbageCollectionTypes.SystemGC;
        else if (gcCauseForwardReference == GCCause.HEAP_DUMP)
            garbageCollectionTypeForwardReference = GarbageCollectionTypes.SystemGC;
        else
            garbageCollectionTypeForwardReference = GarbageCollectionTypes.PSFull;
    }

    public void psFullReference(GCLogTrace trace, String line) {
        FullGC collection;
        if (trace.gcCause() == GCCause.JAVA_LANG_SYSTEM)
            collection = new SystemGC(trace.getDateTimeStamp(), trace.gcCause(), trace.getPauseTime());
        else
            collection = new PSFullGC(trace.getDateTimeStamp(), trace.gcCause(), trace.getPauseTime());
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(29);
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(35);
        MemoryPoolSummary heap = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(41);
        collection.add(young, tenured, heap);
        collection.add(extractPrintReferenceGC(line));
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //47047.534: [Full GC (System.gc()) 47047.534: [Tenured: 292298K->278286K(699072K), 0.7450752 secs] 350258K->278286K(1013824K), [Metaspace: 99239K->99239K(1140736K)], 0.7451777 secs] [Times: user=0.74 sys=0.00, real=0.75 secs]
    public void psFullReferenceJDK8(GCLogTrace trace, String line) {
        FullGC fullGC = new FullGC(getClock(), GarbageCollectionTypes.FullGC, trace.gcCause(), trace.getDuration());
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(30);
        MemoryPoolSummary heap = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(37);
        fullGC.add(heap.minus(tenured), tenured, heap);
        fullGC.add(extractPrintReferenceGC(line));
        fullGC.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        fullGC.add(extractCPUSummary(line));
        publish(fullGC);
    }

    public void serialFullReference(GCLogTrace trace, String line) {
        FullGC collection = (trace.gcCause().equals(GCCause.JAVA_LANG_SYSTEM)) ? new SystemGC(trace.getDateTimeStamp(), trace.gcCause(), trace.getDuration()) : new FullGC(trace.getDateTimeStamp(), trace.gcCause(), trace.getDuration());
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(30);
        MemoryPoolSummary heap = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(37);
        MemoryPoolSummary young = new MemoryPoolSummary(heap.getOccupancyBeforeCollection() - tenured.getOccupancyBeforeCollection(), 0L, heap.getSizeAfterCollection() - tenured.getSizeAfterCollection());
        collection.add(young, tenured, heap);
        collection.add(extractPrintReferenceGC(line));
        collection.add(extractCPUSummary(line));
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        publish(collection);
    }

    public void psFullErgonomicsPhases(GCLogTrace trace, String line) {
        fullGCTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
        garbageCollectionTypeForwardReference = (gcCauseForwardReference != GCCause.JAVA_LANG_SYSTEM) ? GarbageCollectionTypes.FullGC : GarbageCollectionTypes.SystemGC;
    }

    public void psFullReferencePhase(GCLogTrace trace, String line) {
        referenceGCForwardReference = extractPrintReferenceGC(line);
    }

    public void psDetailsWithTenuring(GCLogTrace trace, String line) {
        PSYoungGen collection = new PSYoungGen(scavengeTimeStamp, gcCauseForwardReference, trace.getDuration());
        collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(2), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 8));
        if (referenceGCForwardReference != null)
            collection.add(referenceGCForwardReference);
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    //13.056: [GC-- [PSYoungGen: 4194240K->4194240K(4194240K)] 4280449K->4360823K(4361088K), 0.4589570 secs]
    //GC-- indicated a promotion failed
    public void psFailure(GCLogTrace trace, String line) {
        PSYoungGen collection = new PSYoungGen(getClock(), GCCause.PROMOTION_FAILED, trace.getDoubleGroup(trace.groupCount()));
        collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(8), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 14));
    }

    public void psYoungAdaptiveSizePolicy(GCLogTrace trace, String line) {
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.PSYoungGen;
        scavengeTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
        if (line.contains("SoftReference"))
            referenceGCForwardReference = extractPrintReferenceGC(line);
    }

    public void psFullAdaptiveSizePolicy(GCLogTrace trace, String line) {
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.PSFull;
        fullGCTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
    }

    public void psYoungDetailsFloating(GCLogTrace trace, String line) {
        PSYoungGen collection = new PSYoungGen(scavengeTimeStamp, gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
        collection.add(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1), getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 7));
        publish(collection);
    }

    public void psFullAdaptiveSize(GCLogTrace trace, String line) {
        fullGCTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
    }

    public void psFullBodyFloating(GCLogTrace trace, String line) {
        FullGC collection;
        if (gcCauseForwardReference == GCCause.JAVA_LANG_SYSTEM || gcCauseForwardReference == GCCause.HEAP_DUMP) {
            collection = new SystemGC(fullGCTimeStamp, gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
        } else
            collection = new PSFullGC(fullGCTimeStamp, gcCauseForwardReference, trace.getPauseTime());

        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(2);
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(9);
        MemoryPoolSummary heap = this.getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 15);
        collection.add(young, tenured, heap);
        collection.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        if (referenceGCForwardReference != null)
            collection.add(referenceGCForwardReference);
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    public void psFullReferenceAdaptiveSize(GCLogTrace trace, String line) {
        fullGCTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
        referenceGCForwardReference = extractPrintReferenceGC(line);
    }

    public void psPromotionFailed(GCLogTrace trace, String line) {
        if (GarbageCollectionTypes.DefNew == garbageCollectionTypeForwardReference) {
            DefNew youngGC = new DefNew(scavengeTimeStamp, gcCauseForwardReference, trace.getDoubleGroup(7));
            MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
            MemoryPoolSummary heap = new MemoryPoolSummary(young.getOccupancyBeforeCollection() + trace.toKBytes(9), trace.toKBytes(20), trace.toKBytes(16), trace.toKBytes(20));
            youngGC.add(young, heap);
            double cpuBreakDownRatio = trace.getDoubleGroup(7) / trace.getDoubleGroup(trace.groupCount());
            CPUSummary cpuSummary = extractCPUSummary(line);
            if (cpuSummary != null) {
                double user = cpuSummary.getUser();
                double kernel = cpuSummary.getKernel();
                double real = cpuSummary.getWallClock();
                youngGC.add(new CPUSummary(user * cpuBreakDownRatio, kernel * cpuBreakDownRatio, real * cpuBreakDownRatio));
                cpuBreakDownRatio = 1.0d - cpuBreakDownRatio;
                cpuSummary = new CPUSummary(user * cpuBreakDownRatio, kernel * cpuBreakDownRatio, real * cpuBreakDownRatio);
            }
            publish(youngGC, false);

            DateTimeStamp timeOfFullCollection = new DateTimeStamp(trace.getDoubleGroup(8)); //todo: adjust date to match and add it in.
            FullGC fullGC = new FullGC(timeOfFullCollection, gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()) - trace.getDoubleGroup(7));
            //young
            young = new MemoryPoolSummary(young.getOccupancyAfterCollection(), young.getSizeAfterCollection(), 0L, young.getSizeAfterCollection());
            fullGC.add(young, trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(16));
            fullGC.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
            fullGC.add(cpuSummary);
            publish(fullGC);
        } else {
            LOGGER.warning("Unable to parse -> " + trace);
        }
        scavengeTimeStamp = getClock();
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.DefNew;
    }

    public void rescanSplitUnloadingString(GCLogTrace trace, String line) {
        LOGGER.warning("Not Yet Implemented -> " + trace);
    }

    public void parNewConcurrentPhaseCards(GCLogTrace trace, String line) {
        LOGGER.warning("Not Yet Implemented -> " + trace);
    }

    public void concurrentPhaseYields(GCLogTrace trace, String line) {
        endOfConcurrentPhase( trace, line);
    }

    private void precleanTimedoutWithCards(GCLogTrace trace, String line) {
        abortPrecleanDueToTime = true;
        GCLogTrace concurrentPhase = new GCParseRule("X",CMS_PHASE_END).parse(line);
        endOfConcurrentPhase(concurrentPhase, concurrentPhase.getDateTimeStamp(), 0);
    }

    private void shouldCollectConcurrent(GCLogTrace trace, String line) {
        LOGGER.warning("Not Yet Implemented -> " + trace);
    }

    public void parNewShouldConcurrentCollect(GCLogTrace trace, String line) {
        LOGGER.warning("Not yet Implemented -> " + trace);
    }

    private void psYoungAdaptivePolicySizeStart(GCLogTrace trace, String line) {
        LOGGER.warning("Not yet Implemented -> " + trace);
    }

    private void psYoungAdaptivePolicySizeBody(GCLogTrace trace, String line) {
        LOGGER.warning("Not yet Implemented -> " + trace);
    }

    private void adaptivePolicySizeBody(GCLogTrace trace, String line) {
        LOGGER.warning("Not yet Implemented -> " + trace);
    }

    public void remarkSplitByDebug(GCLogTrace trace, String line) {
        long youngOccupancy = trace.getLongGroup(7);
        long youngConfiguredSize = trace.getLongGroup(8);
        youngMemoryPoolSummaryForwardReference = new MemoryPoolSummary(youngOccupancy, youngConfiguredSize, youngOccupancy, youngConfiguredSize);
    }

    public void scavengeBeforeRemarkReference(GCLogTrace trace, String line) {
        parNewForwardReference = new ParNew(trace.getDateTimeStamp(), trace.gcCause(), trace.getDoubleGroup(trace.groupCount() - 3));
        MemoryPoolSummary young = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(59);
        MemoryPoolSummary tenured = getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 66);
        parNewForwardReference.add(young, tenured.minus(young), tenured);
        parNewForwardReference.add(extractPrintReferenceGC(line));
        parNewForwardReference.add(extractCPUSummary(line));
        publish(parNewForwardReference, false);
    }

    public void preCleanReferenceParNewReference(GCLogTrace trace, String line) {
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        scavengeTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause(16);
        referenceGCForwardReference = extractPrintReferenceGC(line);
    }

    public void scavengeBeforeRemarkReferenceSplit(GCLogTrace trace, String line) {
        garbageCollectionTypeForwardReference = GarbageCollectionTypes.ParNew;
        remarkTimeStamp = getClock();
        scavengeTimeStamp = getClock();
        gcCauseForwardReference = GCCause.CMS_FINAL_REMARK;
        referenceGCForwardReference = extractPrintReferenceGC(line);
    }

    //todo: start of Remark which is interrupted by a young gen. Record the data
    // 19.293: [GC (CMS Final Remark) [YG occupancy: 4110424 K (4456448 K)]
    public void scavengeBeforeRemarkPrintHeapAtGC(GCLogTrace trace, String line) {
        remarkTimeStamp = getClock();
        gcCauseForwardReference = trace.gcCause();
    }

    //22.690: [Rescan (parallel) , 0.0135887 secs]22.704: [weak refs processing
    // 22.704: [SoftReference, 0 refs, 0.0000769 secs]22.704: [WeakReference, 0 refs, 0.0000869 secs]
    // 22.704: [FinalReference, 4049 refs, 0.0038791 secs]22.708: [PhantomReference, 0 refs, 0.0000943 secs]
    // 22.708: [JNI Weak Reference, 0.0000829 secs], 0.0044520 secs]22.708: [class unloading, 0.0205276 secs]
    // 22.729: [scrub symbol table, 0.0038738 secs]22.733: [scrub string table, 0.0046405 secs]
    // [1 CMS-remark: 243393K(9378240K)] 310268K(10375040K), 0.2151395 secs] [Times: user=1.12 sys=0.09, real=0.21 secs]
    public void splitRemarkReference(GCLogTrace trace, String line) {
        CMSRemark remark = new CMSRemark(getClock(), gcCauseForwardReference, trace.getPauseTime());
        GCLogTrace remarkClause = REMARK_CLAUSE.parse(line);
        MemoryPoolSummary tenured = getTotalOccupancyWithTotalHeapSizeSummary(remarkClause, 1);
        MemoryPoolSummary heap = getTotalOccupancyWithTotalHeapSizeSummary(remarkClause, 5);
        remark.add(heap.minus(tenured), tenured, heap);
        recordRescanStepTimes(remark, line);
        remark.addReferenceGCSummary(extractPrintReferenceGC(line));
        remark.add(extractCPUSummary(line));
        publish(remark);
        remarkTimeStamp = null;
    }

    //Bug: weak reference processing record is fragmented.
    private final GCParseRule weakReferenceFragmentRule = new GCParseRule("weakReferenceFragmentRule", "\\], " + PAUSE_TIME);

    /**
     *     public void remarkWithReferenceAndScavenge(GCLogTrace trace, String line) {
     *         CMSRemark remark = new CMSRemark(getClock(), trace.getDoubleGroup(trace.groupCount() - 3));
     *         GCLogTrace remarkClause = REMARK_CLAUSE.parse(line);
     *         MemoryPoolSummary tenured = getTotalOccupancyWithTotalHeapSizeSummary(remarkClause, 1);
     *         MemoryPoolSummary heap = getTotalOccupancyWithTotalHeapSizeSummary(remarkClause, 5);
     *         remark.add(heap.minus(tenured), tenured, heap);
     *         recordRescanStepTimes(remark, line);
     *         remark.addReferenceGCSummary(extractPrintReferenceGC(line));
     *         remark.add(extractCPUSummary(line));
     *         publish(remark);
     *     }
     * @param trace The chunk of GC log that we are attempting to match to a known GC log pattern
     * @param line The GC log line being parsed
     */
    public void splitRemarkReferenceWithWeakReferenceSplitBug(GCLogTrace trace, String line) {
        GCLogTrace remarkTrace = REMARK_CLAUSE.parse(line);
        Pattern durationGroupPattern = Pattern.compile(".* " + PAUSE_TIME);
        Matcher matcher = durationGroupPattern.matcher(line);
        double duration = 0.0d;
        if (matcher.find()) {
            duration = Double.parseDouble(matcher.group(matcher.groupCount()));
        }
        CMSRemark collection = new CMSRemark(getClock(), GCCause.CMS_FINAL_REMARK, duration);
        MemoryPoolSummary tenured = getTotalOccupancyWithTotalHeapSizeSummary(remarkTrace, 1);
        MemoryPoolSummary heap = getTotalOccupancyWithTotalHeapSizeSummary(remarkTrace, 5);
        collection.add(heap.minus(tenured), tenured, heap);
        recordRescanStepTimes(collection, line);
        collection.addReferenceGCSummary(extractPrintReferenceGC(line));
        GCLogTrace weakReferenceFragment = weakReferenceFragmentRule.parse(line);
        if (weakReferenceFragment != null) {
            collection.getReferenceGCSummary().addWeakReferences(trace.getDateTimeStamp(2), 0, weakReferenceFragment.getPauseTime());
        }
        collection.add(extractCPUSummary(line));
        publish(collection);
        remarkTimeStamp = null;
    }

    public void adaptiveSizePolicyStop(GCLogTrace trace, String line) {
        //todo ignored until we collect adaptive size data.
    }


    //************************************ end of direct support for parsing rules
    private final GCParseRule cmsRemarkBlockRule = new GCParseRule("CMS_REMARK_BLOCK", REMARK_BLOCK);
    private void recordRemark(GCLogTrace trace, String line, GCCause gcCause) {

        CMSRemark collection =  (gcCause == GCCause.UNKNOWN_GCCAUSE)
                ? new CMSRemark(getClock(), trace.getDuration())
                : new CMSRemark(getClock(), gcCause, trace.getDuration());

        // stabilizes memory pool sizing information
        GCLogTrace remarkBlock = cmsRemarkBlockRule.parse(line);
        if ( remarkBlock == null) {
            LOGGER.warning("Internal error, unable to parse CMS information from " + line);
        } else {
            MemoryPoolSummary tenured = remarkBlock.getOccupancyWithMemoryPoolSizeSummary();
            MemoryPoolSummary heap = remarkBlock.getOccupancyWithMemoryPoolSizeSummary();
            collection.add(heap.minus(tenured), tenured, heap);
            recordRescanStepTimes(collection, line);
            collection.add(extractCPUSummary(line));
            publish(collection);
        }
    }

    //124.310: [GC (CMS Final Remark) [YG occupancy: 670314 K (737280 K)]124.310: [Rescan (parallel) , 0.1178559 secs]124.428: [weak refs processing124.428: [SoftReference, 0 refs, 0.0000046 secs]124.428: [WeakReference, 0 refs, 0.0000028 secs]124.428: [FinalReference, 0 refs, 0.0000026 secs]124.428: [PhantomReference, 0 refs, 0 refs, 0.0000032 secs]124.428: [JNI Weak Reference, 0.0000094 secs], 0.0000365 secs]124.428: [class unloading, 0.0056223 secs]124.434: [scrub symbol table, 0.0028174 secs]124.437: [scrub string table, 0.0021200 secs][1 CMS-remark: 2896239K(3375104K)] 3566554K(4112384K), 0.1290335 secs] [Times: user=0.94 sys=0.01, real=0.12 secs]
    //Can manage 3 different type of remark formats including 1 buggy version (not well tested).
    private void recordRemarkWithReferenceProcessing(GCLogTrace trace, String line) {
        CMSRemark collection;
        GCCause gcCause = trace.gcCause();
        if (gcCause == GCCause.UNKNOWN_GCCAUSE)
            gcCause = GCCause.CMS_FINAL_REMARK;

        collection = new CMSRemark(getClock(), gcCause, trace.getDoubleGroup(trace.groupCount()));
        GCLogTrace memorySummary = OCCUPANCY_CONFIGURED_RULE.parse(line);
        MemoryPoolSummary tenured = memorySummary.getOccupancyWithMemoryPoolSizeSummary(1);
        MemoryPoolSummary heap = getTotalOccupancyWithTotalHeapSizeSummary(memorySummary.next(), 1);
        collection.add(heap.minus(tenured), tenured, heap);
        recordRescanStepTimes(collection, line);
        collection.addReferenceGCSummary(extractPrintReferenceGC(line));
        collection.add(extractCPUSummary(line));
        publish(collection);
    }

    /*
    private MemoryPoolSummary extractPermspaceRecord(GCLogTrace trace) {
        int index = (trace.getGroup(2) == null) ? 2 : 4;
        return new PermGenSummary(trace.getLongGroup(index), trace.getLongGroup(4), trace.getLongGroup(6));
    }
    */

    private void log(String line) {
//        if (CONCURRENT_PHASE_START.parse(line) != null) return;
//        if (CONCURRENT_PHASE_END.parse(line) != null) return;
//        if (ABORT_PRECLEAN_DUE_TO_TIME_CLAUSE.parse(line) != null) return;
//        if (PRECLEAN_REFERENCE.parse(line) != null) return;
//        if (line.startsWith("Missed: GC locker: Trying a full collection because scavenge failed")) return;
//
//        if (line.startsWith("PSYoungGen")) return;
//        if (line.startsWith("eden space")) return;
//        if (line.startsWith("to")) return;
//        if (line.startsWith("from")) return;
//        if (line.startsWith("ParOldGen")) return;
//        if (line.startsWith("PSOldGen")) return;
//        if (line.startsWith("space")) return;
//        if (line.startsWith("object space")) return;
//        if (line.startsWith("PSPermGen")) return;
//        if (line.startsWith("{Heap")) return;
//        if (line.startsWith("}")) return;
//        if (line.startsWith("Heap")) return;
//        if (line.startsWith("[Times: user")) return;
        if ( line.startsWith("Metaspace       used")) return;
        if ( line.startsWith("class space    used")) return;
        if (line.startsWith("par new generation   total")) return;
//        if (line.startsWith("concurrent mark-sweep generation total")) return;
        if (line.startsWith("concurrent-mark-sweep perm gen total")) return;
        if (line.startsWith("(cardTable: ")) return;
//        if (line.contains("CMS-concurrent-abortable-preclean")) return;
//        if (line.contains("committed")) return;
        if (line.startsWith("def new generation   total")) return;
//        if (line.startsWith("Before GC:")) return;
//        if (line.startsWith("After GC:")) return;
//        if (line.contains("GC log file created")) return;
//        if (line.contains("GC log file has reached the maximum size")) return;
//        if (line.contains("Large block")) return;
//
//        GCToolKit.LOG_DEBUG_MESSAGE(() -> "GenerationalHeapParser missed: " + line);
        if (line.contains("CMSCMS: Large block")) return;
        LOGGER.log(Level.WARNING, "Missed: {0}", line);

    }

    private DateTimeStamp startOfConcurrentPhase;

    //this rule must be evaluated before CONCURRENT_PHASE_END_BLOCK
    private void abortPrecleanDueToTime(GCLogTrace trace, String line) {
        try {
            double cpuTime = trace.getDoubleGroup(7);
            double wallClock = trace.getDoubleGroup(8);
            publish(new AbortablePreClean(startOfConcurrentPhase, wallClock, cpuTime, wallClock, true));
            abortPrecleanDueToTime = false;
        } catch (Exception e) {
            LOGGER.warning("concurrent phase end choked on " + trace);
        }
    }

    private void abortPrecleanWithCards(GCLogTrace trace, String line) {
        try {
            double cpuTime = trace.getDoubleGroup(10);
            double wallClock = trace.getDoubleGroup(11);
            publish(new AbortablePreClean(startOfConcurrentPhase, wallClock, cpuTime, wallClock, false));
            abortPrecleanDueToTime = false;
        } catch (Exception e) {
            LOGGER.warning("concurrent phase end choked on " + trace);
        }
    }

    private void startOfConcurrentPhase(GCLogTrace trace, String line) {
        startOfConcurrentPhase = trace.getDateTimeStamp();
    }
    private void endOfConcurrentPhase(GCLogTrace trace, String line) {
        DateTimeStamp endOfPhase = trace.getDateTimeStamp();
        endOfConcurrentPhase(trace, endOfPhase, 0);
    }

    private void endConcurrentPrecleanWithReferenceProcessing(GCLogTrace trace, String line) {
        GCLogTrace concurrentBlock = CONCURRENT_PHASE_END_BLOCK.parse(line);
        try {
            publish(new ConcurrentPreClean(startOfConcurrentPhase, concurrentBlock.getDoubleGroup(11), concurrentBlock.getDoubleGroup(7), concurrentBlock.getDoubleGroup(8)));
        } catch (Throwable t) {
            LOGGER.warning("Unable to extract data from " + trace.toString());
        }
    }

    private void endOfConcurrentPhase(GCLogTrace trace, DateTimeStamp timeStamp, int offset) {
        String phase = trace.getGroup(6 + offset);
        double cpuTime = trace.getDoubleGroup(7 + offset);
        double wallTime = trace.getDoubleGroup(8 + offset);
        double duration = timeStamp.toSeconds() - startOfConcurrentPhase.toSeconds();

        switch (phase) {
            case "mark":
                publish(new ConcurrentMark(startOfConcurrentPhase, duration, cpuTime, wallTime));
                break;
            case "preclean":
                publish(new ConcurrentPreClean(startOfConcurrentPhase, duration, cpuTime, wallTime));
                break;
            case "abortable-preclean":
                publish(new AbortablePreClean(startOfConcurrentPhase, duration, cpuTime, wallTime, abortPrecleanDueToTime));
                abortPrecleanDueToTime = false;
                break;
            case "sweep":
                publish(new ConcurrentSweep(startOfConcurrentPhase, duration, cpuTime, wallTime));
                break;
            case "reset":
                publish(new ConcurrentReset(startOfConcurrentPhase, duration, cpuTime, wallTime));
                break;
            default:
                LOGGER.warning("concurrent phase choked on " + trace);
        }
    }

    public void logMissedFirstRecordForEvent(String line) {
        LOGGER.log(Level.WARNING, "Missing initial record for: {0}", line);
    }

    private void clear() {
        garbageCollectionTypeForwardReference = null;
        gcCauseForwardReference = GCCause.UNKNOWN_GCCAUSE;
        fullGCTimeStamp = null;
        scavengeTimeStamp = null;
        youngMemoryPoolSummaryForwardReference = null;
        tenuredForwardReference = null;
        heapForwardReference = null;
        scavengeDurationForwardReference = 0.0;
        scavengeCPUSummaryForwardReference = null;
        referenceGCForwardReference = null;
        totalFreeSpaceForwardReference = 0;
        maxChunkSizeForwardReference = 0;
        numberOfBlocksForwardReference = 0;
        averageBlockSizeForwardReference = 0;
        treeHeightForwardReference = 0;
    }

    public void publish( JVMEvent event, boolean drain, boolean clear) {
        publish(event,clear);
        if (drain) {
            for( GenerationalGCPauseEvent pauseEvent : queue)
                publish(pauseEvent, false);
            queue.clear();
        }
    }

    public void publish(JVMEvent event, boolean clear) {
        if (clear)
            clear();
        super.publish(ChannelName.GENERATIONAL_HEAP_PARSER_OUTBOX, event);
    }

    final private ArrayList<GenerationalGCPauseEvent> queue = new ArrayList<>();
    public void publish(GenerationalGCPauseEvent event) {
        if ( inConcurrentPhase) {
            queue.add(event);
            clear();
        } else
            this.publish(event, true);
    }

    public void publish(CMSConcurrentEvent event) {
        inConcurrentPhase = false;
        publish( event, true, false);
    }

    @Override
    public boolean accepts(Diary diary) {
        return (diary.isGenerational() || diary.isCMS() ) && ! diary.isUnifiedLogging();
    }

    @Override
    public void publishTo(JVMEventChannel bus) {
        super.publishTo(bus);
    }
}

/*
  todo: this is a very old but bad corruption...
  ParNew starts, the output is interrupted by the start of an abortable-preclean but then a CMF happens smashing everything
  At the moment the abortable-preclean and the CMF is reported on, the ParNew is missed.
  In all honesty, missing a single ParNew isn't going to change the conclusion of any analysis so I'm opt-ing to leave it.
  2014-06-10T22:56:26.242-0700: 4999535.014: [GC2014-06-10T22:56:26.242-0700: 4999535.014: [ParNew2014-06-10T22:56:26.256-0700: 4999535.028: [CMS-concurrent-abortable-preclean: 1.948/3.114 secs] [Times: user=1.93 sys=0.12, real=3.11 secs]
 (promotion failed)
Desired survivor size 8716288 bytes, new threshold 6 (max 6)
- age   1:    1036320 bytes,    1036320 total
- age   2:     825248 bytes,    1861568 total
- age   3:     119024 bytes,    1980592 total
- age   4:     113784 bytes,    2094376 total
- age   5:     129024 bytes,    2223400 total
- age   6:     154976 bytes,    2378376 total
: 141769K->140729K(153344K), 0.3807730 secs]2014-06-10T22:56:26.623-0700: 4999535.395: [CMS
 (concurrent mode failure): 1920816K->50773K(1926784K), 28.3938140 secs] 2062055K->50773K(2080128K), [CMS Perm : 48657K->41071K(262144K)], 28.7750370 secs] [Times: user=1.65 sys=0.03, real=28.78 secs]

 A normal record looks like this
 2014-06-10T23:00:18.728-0700: 4999767.501: [GC2014-06-10T23:00:18.728-0700: 4999767.501: [ParNew
Desired survivor size 8716288 bytes, new threshold 6 (max 6)
- age   1:     775312 bytes,     775312 total
- age   2:     497168 bytes,    1272480 total
: 138559K->1370K(153344K), 0.0215080 secs] 189332K->52143K(2080128K), 0.0217400 secs] [Times: user=0.04 sys=0.00, real=0.02 secs]
 */
