// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.MalformedEvent;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.RegionSummary;
import com.microsoft.gctoolkit.event.g1gc.G1ConcurrentUndoCycle;
import com.microsoft.gctoolkit.event.g1gc.G1GCConcurrentEvent;
import com.microsoft.gctoolkit.event.g1gc.G1GCEvent;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.parser.collection.RuleSet;
import com.microsoft.gctoolkit.parser.jvm.Decorators;
import com.microsoft.gctoolkit.parser.unified.UnifiedG1GCPatterns;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.fromLabel;

/**
 * TODO No reports or views generated from this data yet.
 * <p>
 * Result on
 * - when GC started
 * - type of GC triggered
 * - from, to, configured
 * - pause time if it is reported or can be calculated
 * todo: me
 */
public class UnifiedG1GCParser extends UnifiedGCLogParser implements UnifiedG1GCPatterns {

    private static final Logger LOGGER = Logger.getLogger(UnifiedG1GCParser.class.getName());

    private final Map<Integer, G1GCForwardReference> collectionsUnderway = new ConcurrentHashMap<>();

    // state variables
    private boolean before = false; //todo what happens if this gets out of sync. #IHateState
    private int gcInvocations = 0;
    private int fullGCInvocations = 0;
    private DateTimeStamp jvmTerminationEventTime = new DateTimeStamp(-1.0d);

    private G1GCForwardReference forwardReference;
    private boolean concurrentPhaseActive = false;

    private final RuleSet<GCParseRule, BiConsumer<GCLogTrace, String>> parseRules;

    {
        parseRules = new RuleSet<>();
        parseRules.put(G1_COLLECTION, this::g1Collection);
        parseRules.put(CPU_BREAKOUT, this::cpuBreakout);
        parseRules.put(HEAP_BEFORE_AFTER_GC_INVOCATION_COUNT, this::heapBeforeAfterGCInvocationCount);
        parseRules.put(HEAP_SUMMARY, this::heapSummary);
        parseRules.put(REGION_DISBURSEMENT, this::youngRegionAllotment);
        parseRules.put(META_CLASS_SPACE, this::metaClassSpace);
        parseRules.put(WORKER_SUMMARY, this::workSummary);
        parseRules.put(REFERENCES, this::references);
        parseRules.put(REFERENCE_COUNTS, this::referenceCounts);
        parseRules.put(PRE_EVACUATE_COLLECTION_SET, this::evacuateCollectionSetTime);
        parseRules.put(PRE_EVACUATION_SUBPHASE, this::preEvacuateCSetPhaseDuration);
        parseRules.put(EVACUATION_PHASE, this::evacuateCSetPhase);

        parseRules.put(PARALLEL_COUNT, this::parallelCount);
        parseRules.put(POST_EVACUATE_PHASE, this::postEvacuatePhaseDuration);
        parseRules.put(REFERENCE_PROCESSING, this::postEvacuatePhaseDuration);
        parseRules.put(TO_SPACE_EXHAUSTED, this::toSpaceExhausted);
        parseRules.put(OTHER, this::other);
        parseRules.put(REGION_SUMMARY, this::regionSummary);
        parseRules.put(UNIFIED_META_DATA, this::unifiedMetaData);
        parseRules.put(YOUNG_DETAILS, this::youngDetails);
        parseRules.put(META_SPACE_BREAKOUT, this::metaNonClassClassSpace);
        parseRules.put(HEAP_REGION_SIZE, this::heapRegionSize);
        parseRules.put(HEAP_SIZE, this::heapSize);
        parseRules.put(G1_TAG, this::ignore);

        parseRules.put(CONCURRENT_CYCLE_START, this::concurrentCycleStart);
        parseRules.put(CONCURRENT_CYCLE_END, this::concurrentCycleEnd);
        parseRules.put(CONCURRENT_UNDO_CYCLE_START, this::concurrentUndoCycleStart);
        parseRules.put(CONCURRENT_UNDO_CYCLE_END, this::concurrentUndoCycleEnd);
        parseRules.put(CONCURRENT_PHASE, this::concurrentPhase);
        parseRules.put(CONCURRENT_PHASE_DURATION, this::concurrentPhaseDuration);
        parseRules.put(CONCURRENT_MARK_PHASE, this::concurrentMarkInternalPhases);
        parseRules.put(CONCURRENT_MARK_PHASE_DURATION, this::concurrentMarkInternalPhaseDuration);
        parseRules.put(CONCURRENT_MARK_START, this::concurrentPhase);
        parseRules.put(CONCURRENT_MARK_WORKERS, this::concurrentMarkWorkers);
        parseRules.put(CONCURRENT_MARK_ABORTED, this::concurrentMarkAborted);
        parseRules.put(CONCURRENT_MARK_END, this::concurrentMarkEnd);
        parseRules.put(PAUSE_REMARK_START, this::remarkStart);
        parseRules.put(FINALIZE_MARKING, this::finalizeMarking);
        parseRules.put(SYSTEM_DICTIONARY_UNLOADING, this::systemDictionaryUnloading);
        parseRules.put(STRING_SYMBOL_TABLE, this::stringSymbolTableCleaning);
        parseRules.put(PARALLEL_UNLOADING, this::parallelUnloading);
        parseRules.put(PAUSE_REMARK_END, this::pausePhaseDuringConcurrentCycleDurationEnd);
        parseRules.put(CLEANUP_START, this::cleanupStart);
        parseRules.put(CLEANUP__FINALIZE_CONC_MARK,this::noop);
        parseRules.put(CLEANUP_END, this::pausePhaseDuringConcurrentCycleDurationEnd);
        parseRules.put(FULL_PHASE, this::fullPhase);
        parseRules.put(FULL_CLASS_UNLOADING, this::fullClassUnloading);
        parseRules.put(FULL_STRING_SYMBOL_TABLE, this::fullStringSymbolTable);
        parseRules.put(JVM_EXIT, this::jvmExit);
        parseRules.put(new GCParseRule("END_OF_DATA_SENTINEL", END_OF_DATA_SENTINEL), this::endOfFile);
        // New rules to process, currently noop'ed
        parseRules.put(CONCATENATE_DIRTY_CARD_LOGS, this::noop);
        parseRules.put(REGION_REGISTER, this::noop);
        parseRules.put(HEAP_ROOTS, this::noop);
        parseRules.put(EAGER_RECLAIM, this::noop);
        parseRules.put(REMEMBERED_SETS, this::noop);
        parseRules.put(EAGER_RECLAIM_STEP, this::noop);
        parseRules.put(CARDS, this::noop);
        parseRules.put(HOT_CARD_CACHE, this::noop);
        parseRules.put(LOG_BUFFERS, this::noop);
        parseRules.put(SCAN_HEAP_ROOTS, this::noop);

        parseRules.put(SCANS, this::noop);
        parseRules.put(CLAIMED_CHUNKS, this::noop);
        parseRules.put(CODE_ROOT_SCAN, this::noop);
        parseRules.put(STRING_DEDUP, this::noop);
        parseRules.put(WEAK_JFR_SAMPLES, this::noop);

        parseRules.put(POST_EVAC_CLEANUP, this::noop);
        parseRules.put(MERGE_THREAD_STATE, this::noop);
        parseRules.put(COPIED_BYTES, this::noop);
        parseRules.put(LAB, this::noop);
        parseRules.put(CLEAR_LOGGED_CARDS, this::noop);

        parseRules.put(RECALC_USED_MEM, this::noop);
        parseRules.put(PURGE_CODE_ROOTS, this::noop);
        parseRules.put(UPDATE_DERIVED_POINTERS, this::noop);
        parseRules.put(EAGER_HUMONGOUS_RECLAIM, this::noop);
        parseRules.put(HUMONGOUS, this::noop);

        parseRules.put(REDIRTY_CARDS, this::noop);
        parseRules.put(REDIRTIED_CARDS, this::noop);
        parseRules.put(FREE_CSET, this::noop);
        parseRules.put(REBUILD_FREELIST, this::noop);
        parseRules.put(NEW_CSET, this::noop);
        parseRules.put(RESIZE_TLAB, this::noop);
    }

    public UnifiedG1GCParser() {
    }

    @Override
    public Set<EventSource> eventsProduced() {
        return Set.of(EventSource.G1GC);
    }

    public String getName() {
        return "UnifiedG1GCParser";
    }

    @Override
    protected void process(String line) {
        if (! ignoreFrequentlySeenButUnwantedLines(line))
            parse(line);
    }

    private static final Pattern gcIdPattern = GCLogParser.GCID_COUNTER.pattern();

    private void parse(String line) {

        // Minor optimization. The parse rule only applies to what comes after the GC ID.
        final int end;
        final int gcid;
        final Matcher gcIdMatcher = gcIdPattern.matcher(line);
        if (gcIdMatcher.find()) {
            gcid = Integer.parseInt(gcIdMatcher.group(1));
            end = gcIdMatcher.end();
        } else {
            gcid = -1;
            end = 0;
        }

        final String lineAfterGcId = line.substring(end);

        parseRules.stream()
                .map(Map.Entry::getKey)
                .map(rule -> new AbstractMap.SimpleEntry<>(rule, rule.parse(lineAfterGcId)))
                .filter(tuple -> tuple.getValue() != null)
                .findAny()
                .ifPresentOrElse(
                        tuple -> {
                            // Typically, "end" will be greater than zero, but not always.
                            setForwardReference(gcid, end > 0 ? line.substring(0, end) : line);
                            applyRule(tuple.getKey(), tuple.getValue(), line);
                        },
                        () -> log(line)
                );
    }


    private void applyRule(GCParseRule ruleToApply, GCLogTrace trace, String line) {
        try {
            parseRules.select(ruleToApply).accept(trace, line);
        } catch (Throwable t) {
            LOGGER.throwing(this.getName(), "process", t);
        }
    }

    private void setForwardReference(int gcid, String line) {
        if (gcid != -1) {
            forwardReference = collectionsUnderway.computeIfAbsent(gcid, k -> new G1GCForwardReference(new Decorators(line), gcid));
            forwardReference.setHeapRegionSize(regionSize);
            forwardReference.setMaxHeapSize(maxHeapSize);
            forwardReference.setMinHeapSize(minHeapSize);
            forwardReference.setInitialHeapSize(initialHeapSize);
        }
    }

    private void removeForwardReference(G1GCForwardReference forwardReference) {
        collectionsUnderway.remove(forwardReference.getGcID());
    }

    private void noop(GCLogTrace trace, String line) {}

    /*************
     *
     * Data Extraction methods
     */

    private void cpuBreakout(GCLogTrace trace, String line) {
        CPUSummary cpuSummary = new CPUSummary(trace.getDoubleGroup(1), trace.getDoubleGroup(2), trace.getDoubleGroup(3));
        forwardReference.setCPUSummary(cpuSummary);
        try {
            publishPauseEvent(forwardReference.buildEvent());
        } catch (MalformedEvent malformedEvent) {
            LOGGER.warning(malformedEvent.getMessage());
        }
    }

    // todo: need to drain the queues before terminating...
    // Just in case there isn't a JVM termination event in the log.
    public void endOfFile(GCLogTrace trace, String line) {
        publish(new JVMTermination((jvmTerminationEventTime.hasTimeStamp()) ? jvmTerminationEventTime : getClock(),diary.getTimeOfFirstEvent()));
    }


    /**
     * following records describe heap before the collection
     *
     * @param trace
     * @param line
     */
    private void heapBeforeAfterGCInvocationCount(GCLogTrace trace, String line) {
        if ("before".equals(trace.getGroup(1))) {
            before = true;
        } else if ("after".equals(trace.getGroup(1))) {
            before = false;
        }
        gcInvocations = trace.getIntegerGroup(2);
        fullGCInvocations = trace.getIntegerGroup(3);
    }

    //Ignore these log messages for now.
    private void heapSummary(GCLogTrace trace, String line) {
//        if ( before) {
//            forwardReference.setHeapOccupancyBeforeCollection(trace.getLongGroup(2));
//            forwardReference.setHeapSizeBeforeCollection(trace.getLongGroup(1));
//        } else {
//            forwardReference.setHeapOccupancyAfterCollection(trace.getLongGroup(2));
//            forwardReference.setHeapSizeAfterCollection(trace.getLongGroup(1));
//        }
    }

    //Minimum heap 8388608  Initial heap 268435456  Maximum heap 268435456
    //these values go back to the JavaVirtualMachine..
    public void heapSize(GCLogTrace trace, String line) {
        this.minHeapSize = trace.getLongGroup(1);
        this.initialHeapSize = trace.getLongGroup(2);
        this.maxHeapSize = trace.getLongGroup(3);
    }

    //return to JVM
    private int regionSize = 0; //region size in Gigabytes
    private long minHeapSize = 0;
    private long initialHeapSize = 0;
    private long maxHeapSize = 0;

    public void heapRegionSize(GCLogTrace trace, String line) {
        regionSize = trace.getIntegerGroup(1);
    }

    //[15.316s][debug][gc,heap      ] GC(0)   region size 1024K, 24 young (24576K), 0 survivors (0K)
    //ignore this logging for now
    private void youngRegionAllotment(GCLogTrace trace, String line) {
        forwardReference.setHeapRegionSize(trace.getIntegerGroup(1) / 1024);
        if (before) {
            forwardReference.setYoungOccupancyBeforeCollection(trace.getLongGroup(3));
            forwardReference.setSurvivorOccupancyBeforeCollection(trace.getLongGroup(5));
            forwardReference.setEdenOccupancyBeforeCollection(trace.getLongGroup(3)-trace.getLongGroup(5));
            forwardReference.setYoungSizeBeforeCollection(trace.getLongGroup(3));
        }
        else {
            forwardReference.setYoungOccupancyAfterCollection(trace.getLongGroup(5));
            forwardReference.setSurvivorOccupancyAfterCollection(trace.getLongGroup(5));
            forwardReference.setEdenOccupancyAfterCollection(0L);
            forwardReference.setYoungSizeAfterCollection(trace.getLongGroup(3));
        }
    }

    /**
     * @param trace
     * @param line
     */
    private void metaClassSpace(GCLogTrace trace, String line) {
        if (before) {
            if (trace.getGroup(1).equals("Metaspace")) {
                forwardReference.setMetaspaceOccupancyBeforeCollection(trace.getLongGroup(2));
                forwardReference.setMetaspaceSizeBeforeCollection(trace.getLongGroup(3));
                forwardReference.setMetaspaceCommittedBeforeCollection(trace.getLongGroup(4));
                forwardReference.setMetaspaceReservedBeforeCollection(trace.getLongGroup(5));
            } else if (trace.getGroup(1).equals("class space")) {
                forwardReference.setClassspaceOccupancyBeforeCollection(trace.getLongGroup(2));
                forwardReference.setClassspaceSizeBeforeCollection(trace.getLongGroup(3));
                forwardReference.setClassspaceCommittedBeforeCollection(trace.getLongGroup(4));
                forwardReference.setClassspaceReservedBeforeCollection(trace.getLongGroup(5));
            } else
                trace.notYetImplemented();
        } else {
            if (trace.getGroup(1).equals("Metaspace")) {
                forwardReference.setMetaspaceOccupancyAfterCollection(trace.getLongGroup(2));
                forwardReference.setMetaspaceSizeAfterCollection(trace.getLongGroup(3));
                forwardReference.setMetaspaceCommittedAfterCollection(trace.getLongGroup(4));
                forwardReference.setMetaspaceReservedAfterCollection(trace.getLongGroup(5));
            } else if (trace.getGroup(1).equals("class space")) {
                forwardReference.setClassspaceOccupancyAfterCollection(trace.getLongGroup(2));
                forwardReference.setClassspaceSizeAfterCollection(trace.getLongGroup(3));
                forwardReference.setClassspaceCommittedAfterCollection(trace.getLongGroup(4));
                forwardReference.setClassspaceReservedAfterCollection(trace.getLongGroup(5));
            } else
                trace.notYetImplemented();
        }

    }

    private void g1Collection(GCLogTrace trace, String line) {
        GarbageCollectionTypes gcType;
        String gcSubtype = trace.getGroup(3);
        if (gcSubtype == null)
            gcType = fromLabel(trace.getGroup(1));
        else {
            switch (gcSubtype) {
                default:
                    LOGGER.warning("GC Type not recognized: " + line);
                case "Prepare Mixed":
                case "Normal":
                    gcType = fromLabel(trace.getGroup(1));
                    break;
                case "Mixed":
                    gcType = fromLabel(trace.getGroup(3));
                    break;
                case "Concurrent End":
                case "Concurrent Start":
                    gcType = GarbageCollectionTypes.Initial_Mark;
                    break;
            }
        }
        forwardReference.setGcType(gcType);
        forwardReference.setGCCause(trace.gcCause(1));
        forwardReference.setStartTime(getClock());
    }

    private void workSummary(GCLogTrace trace, String line) {
        forwardReference.evacuationWorkers(trace.getIntegerGroup(1), trace.getIntegerGroup(2));
    }

    private void references(GCLogTrace trace, String line) {
        /**
         * todo: capture preclean phases
         * Not recording preclean phases for the moment. If the preclean capture groups is not null, then
         * it's a preclean phase so noop it.
         */
        if ( trace.getGroup(1) != null) return;
        switch (trace.getGroup(2)) {
            case "SoftReference":
                forwardReference.setSoftReferenceProcessingDuation(trace.getDurationInSeconds());
                break;
            case "WeakReference":
                forwardReference.setWeakReferenceProcessingDuration(trace.getDurationInSeconds());
                break;
            case "FinalReference":
                forwardReference.setFinalReferenceProcessingDuration(trace.getDurationInSeconds());
                break;
            case "PhantomReference":
                forwardReference.setPhantomReferenceProcessingDuration(trace.getDurationInSeconds());
                break;
            case "JNI Weak Reference":
                forwardReference.setJniWeakReferenceProcessingDuration(trace.getDurationInSeconds());
                break;
            default:
                trace.notYetImplemented();
        }
    }

    //todo: magic numbers
    //[15.322s][debug][gc,ref       ] GC(0) Ref Counts: Soft: 0 Weak: 994 Final: 1074 Phantom: 211
    private void referenceCounts(GCLogTrace trace, String line) {
        forwardReference.setReferenceCounts(trace.getIntegerGroup(1),
                trace.getIntegerGroup(2),
                trace.getIntegerGroup(3),
                trace.getIntegerGroup(4));
    }

    //Phases
    /*
        [15.322s][info ][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms
        [15.322s][info ][gc,phases    ] GC(0)   Evacuate Collection Set: 4.9ms
        [15.322s][info ][gc,phases    ] GC(0)   Post Evacuate Collection Set: 1.0ms
        [15.322s][info ][gc,phases    ] GC(0)   Other: 0.2ms
     */

    private void evacuateCollectionSetTime(GCLogTrace trace, String line) {
        if (trace.getGroup(1) == null) {
            forwardReference.setEvacuationCSetDuration(trace.getDurationInSeconds());
        } else if ("Pre".equals(trace.getGroup(1))) {
            forwardReference.setPreEvacuateCSetDuration(trace.getDurationInSeconds());
        } else if ("Post".equals(trace.getGroup(1)))
            forwardReference.setPostEvacuateCSetDuration(trace.getDurationInSeconds());
        else
            LOGGER.warning("Not recognized: " + line);
    }

    public void other(GCLogTrace trace, String line) {
        forwardReference.setOtherDuration(trace.getDurationInSeconds());
    }

    private void preEvacuateCSetPhaseDuration(GCLogTrace trace, String line) {
        forwardReference.postPreEvacuateCSetPhaseDuration(trace.getGroup(1), trace.getDurationInSeconds());
    }

    public void evacuateCSetPhase(GCLogTrace trace, String line) {
        forwardReference.postEvacuateCSetPhaseDuration(trace.getGroup(1), trace.getUnifiedStatisticalSummary());
    }

    public void postEvacuatePhaseDuration(GCLogTrace trace, String line) {
        forwardReference.postPostEvacuateCSetPhaseDuration(trace.getGroup(1), trace.getDurationInSeconds());
    }

    public void toSpaceExhausted(GCLogTrace trace, String line) {
        forwardReference.toSpaceExhausted();
    }

    public void parallelCount(GCLogTrace trace, String line) {
        switch (trace.getGroup(1)) {
            case "Processed Buffers":
                forwardReference.setProcessedBuffersSummary(trace.countSummary());
                break;
            case "Termination Attempts":
                forwardReference.setTerminationAttempts(trace.countSummary());
                break;
            default:
                trace.notYetImplemented();
        }
    }

    /**
     * The trace indicates number of active regions before and after the collection. This is then used to provide an
     * extremely coarse estimate of the amount of live data.
     * @param trace A chunk of GC log that we are attempting to match to a known GC log pattern
     * @param line The log line corresponding to the trace
     */
    public void regionSummary(GCLogTrace trace, String line) {
        RegionSummary summary = trace.regionSummary();
        switch (trace.getGroup(1)) {
            case "Eden":
                forwardReference.setEdenRegionSummary(summary);
                break;
            case "Survivor":
                forwardReference.setSurvivorRegionSummary(summary);
                break;
            case "Old":
                forwardReference.setOldRegionSummary(summary);
                break;
            case "Humongous":
                forwardReference.setHumongousRegionSummary(summary);
                break;
            case "Archive":
                forwardReference.setArchiveRegionSummary(summary);
                break;
            default:
                notYetImplemented(trace, line);
        }
    }

    public void unifiedMetaData(GCLogTrace trace, String line) {
        if (forwardReference.setMetaspaceOccupancyBeforeCollection(trace.toKBytes(1))) {
            forwardReference.setMetaspaceOccupancyAfterCollection(trace.toKBytes(3));
            forwardReference.setMetaspaceSizeAfterCollection(trace.toKBytes(5));
        }
    }

    public void youngDetails(GCLogTrace trace, String line) {
        forwardReference.setHeapOccupancyBeforeCollection(trace.toKBytes(5));
        forwardReference.setHeapOccupancyAfterCollection(trace.toKBytes(7));
        forwardReference.setHeapSizeAfterCollection(trace.toKBytes(9));
        forwardReference.setDuration(trace.getDurationInSeconds());
    }

    /**
     * Record contains Metaspace broken out to class and non-class space. Since
     * Metaspace = class space + non-class space, we can ignore the non-class space information (for now)
     * The space size before the collection can be determined by inspecting the previous record (ignore for now)
     * @param trace A chunk of GC log that we are attempting to match to a known GC log pattern
     * @param line The log line corresponding to the trace
     */
    public void metaNonClassClassSpace(GCLogTrace trace, String line) {
        MemoryPoolSummary metaspace = trace.getEnlargedMetaSpaceRecord(1);
        forwardReference.setMetaspaceOccupancyBeforeCollection(metaspace.getOccupancyBeforeCollection());
        forwardReference.setMetaspaceOccupancyAfterCollection(metaspace.getOccupancyAfterCollection());
        forwardReference.setMetaspaceSizeAfterCollection(metaspace.getSizeAfterCollection());
        MemoryPoolSummary classSpace = trace.getEnlargedMetaSpaceRecord(17);
        forwardReference.setClassspaceOccupancyBeforeCollection(classSpace.getOccupancyBeforeCollection());
        forwardReference.setClassspaceCommittedAfterCollection(classSpace.getOccupancyAfterCollection());
        forwardReference.setClassspaceSizeAfterCollection(classSpace.getSizeAfterCollection());
    }

    //Concurrent Mark

    /**
     * Start of concurrent phases which can be ignored (for now??)
     *
     * @param trace
     * @param line
     */

    private void concurrentCycleStart(GCLogTrace trace, String line) {
        forwardReference.setConcurrentCycleStartTime(getClock());
        forwardReference.setGcType(GarbageCollectionTypes.Concurrent_Cycle);
    }

    private void concurrentCycleEnd(GCLogTrace trace, String line) {
        removeForwardReference(forwardReference);
    }

    //todo: ????? this is a different type of concurrent cycle
    private void concurrentUndoCycleStart(GCLogTrace trace, String line) {
        forwardReference.setConcurrentCycleStartTime(getClock());
        forwardReference.setGcType(GarbageCollectionTypes.G1GCConcurrentUndoCycle);
    }

    //todo: need support for JDK 17 undo concurrent cycle event.. started here, commented out for a future PR.
    private void concurrentUndoCycleEnd(GCLogTrace trace, String line) {
        forwardReference.setDuration(trace.getDurationInSeconds());
        publishUndoCycle((G1ConcurrentUndoCycle) forwardReference.buildConcurrentUndoCycleEvent());
        removeForwardReference(forwardReference);
    }

    private void concurrentPhase(GCLogTrace trace, String line) {
        forwardReference.setConcurrentPhase(GarbageCollectionTypes.fromLabel(trace.getGroup(1)));
        forwardReference.setStartTime(getClock());
        concurrentPhaseActive = true;
    }

    private void concurrentMarkEnd(GCLogTrace trace, String line) {
        forwardReference.setDuration(trace.getDurationInSeconds());
        publishConcurrentEvent(forwardReference.buildConcurrentPhaseEvent());
    }

    private void concurrentPhaseDuration(GCLogTrace trace, String line) {
        forwardReference.setDuration(trace.getDurationInSeconds());
        publishConcurrentEvent(forwardReference.buildConcurrentPhaseEvent());
    }

    /**
     * this is the start of the records, nothing to be captured.
     * @param trace
     * @param line
     */
    private void concurrentMarkInternalPhases(GCLogTrace trace, String line) {
    }

    private void concurrentMarkInternalPhaseDuration(GCLogTrace trace, String line) {
        switch (trace.getGroup(1)) {
            case "Mark From Roots":
                forwardReference.setMarkFromRootsDuration(trace.getDurationInSeconds());
                break;
            case "Preclean":
                forwardReference.setPrecleanDuration(trace.getDurationInSeconds());
                break;
            default:
                LOGGER.warning("unknown Concurrent Mark phase : " + line);

        }
    }

    private void concurrentMarkWorkers(GCLogTrace trace, String line) {
        forwardReference.concurrentMarkWorkers(trace.getIntegerGroup(1), trace.getIntegerGroup(2));
    }

    private void concurrentMarkAborted(GCLogTrace trace, String line) {
        forwardReference.abortConcurrentMark();
        publishConcurrentEvent(forwardReference.buildConcurrentPhaseEvent());
    }

    private void remarkStart(GCLogTrace trace, String line) {
        forwardReference.pausePhaseDuringConcurrentCycle(GarbageCollectionTypes.G1GCRemark);
        forwardReference.pausePhaseDuringConcurrentCycleStart(getClock());
    }

    private void cleanupStart(GCLogTrace trace, String line) {
        forwardReference.pausePhaseDuringConcurrentCycle(GarbageCollectionTypes.G1GCCleanup);
        forwardReference.pausePhaseDuringConcurrentCycleStart(getClock());
    }

    private void finalizeMarking(GCLogTrace trace, String line) {
        forwardReference.finalizeMarkingDuration(trace.getDurationInSeconds());
    }

    private void systemDictionaryUnloading(GCLogTrace trace, String line) {
        forwardReference.systemDictionaryUnloadingDuration(trace.getDurationInSeconds());
    }

    private void stringSymbolTableCleaning(GCLogTrace trace, String s) {
        forwardReference.stringTableProcessedAndRemoved(trace.getIntegerGroup(1), trace.getIntegerGroup(2));
        forwardReference.symbolTableProcessedAndRemoved(trace.getIntegerGroup(3), trace.getIntegerGroup(4));
    }

    private void parallelUnloading(GCLogTrace trace, String line) {
        forwardReference.parallelUnloadingDuration(trace.getDurationInSeconds());
    }

    private void pausePhaseDuringConcurrentCycleDurationEnd(GCLogTrace trace, String line) {
        forwardReference.pausePhaseDuringConcurrentCycleDuration(trace.getDurationInSeconds());
        forwardReference.setHeapOccupancyBeforeCollection(trace.toKBytes(1));
        forwardReference.setHeapSizeBeforeCollection(trace.toKBytes(5));
        forwardReference.setHeapOccupancyAfterCollection(trace.toKBytes(3));
        forwardReference.setHeapSizeAfterCollection(trace.toKBytes(5));
    }

    //Full

    private void fullPhase(GCLogTrace trace, String line) {
        if (trace.getGroup(3) != null)
            forwardReference.fullPhase(trace.getIntegerGroup(1), trace.getGroup(2), trace.getDurationInSeconds());
    }

    /**
     * todo: need to process and view this captured data
     * @param trace
     * @param line
     */
    private void fullClassUnloading(GCLogTrace trace, String line) {
        //forwardReference.fullClassUnloadingDuration(trace.getMillisecondDurationInSeconds());
    }

    /**
     * todo: need to capture StringTable data (part of remark at debug level)
     * @param trace
     * @param line
     */
    private void fullStringSymbolTable(GCLogTrace trace, String line) {
//        forwardReference.scrubStringSymbolTableDuration(trace.getMillisecondDurationInSeconds());
    }

    /**
     * records a concurrent phase of a concurrent cycle. After the event has been recorded, all other events
     * that occurred during the concurrent event will be recorded.
     * The exception is the Concurrent Undo cycle which causes all concurrent phases to be queued until the
     * undo cycle ends.
     * @param event
     */
    private void publishConcurrentEvent(G1GCConcurrentEvent event) {
        if ( event == null) return;

        if ( forwardReference.getGcType() != GarbageCollectionTypes.G1GCConcurrentUndoCycle) {
            publish(event);
            concurrentPhaseActive = false;
            eventQueue.stream().forEach(this::publish);
            eventQueue.clear();
        } else {
            eventQueue.add(event);
        }
    }

    private void publishUndoCycle(G1ConcurrentUndoCycle cycle) {
        concurrentPhaseActive = false;
        publish(cycle);
        eventQueue.stream().forEach(this::publish);
        eventQueue.clear();
    }

    private final Queue<G1GCEvent> eventQueue = new LinkedList<>();

    /**
     * Events are published in the start time order. If a concurrent cycle has started and it's in a concurrent
     * phase, the pause event is queued. It will be published when the concurrent phase completes. As each event is
     * published, it corresponding forward reference is released.
     * @param event
     */
    private void publishPauseEvent(G1GCPauseEvent event) {
        if (event == null) return;
        if ( concurrentPhaseActive) {
            eventQueue.add(event);
            removeForwardReference(forwardReference);
        } else {
            publish(event);
            if ( ! forwardReference.isConcurrentCycle())
                removeForwardReference(forwardReference);
        }
    }


    // Let the EOF event send the exit event. Easier in cases where JVM termination isn't in the log
    private void jvmExit(GCLogTrace trace, String s) {
        jvmTerminationEventTime = this.getClock();
    }

    private boolean ignoreFrequentlySeenButUnwantedLines(String line) {
        if (line.contains("Desired survivor size")) return true;
        if (line.contains("Age table with threshold")) return true;
        if (line.contains("safepoint")) return true;
        if (line.contains(") Skipped phase ")) return true;
        if (line.contains(" Total                          Min: ")) return true;
        if (line.contains(" Dead                           Min: ")) return true;
        if (line.contains(" VM Weak                        Min")) return true;
        if (line.contains(" ObjectSynchronizer Weak        Min:")) return true;
        if (line.contains(" JVMTI Tag Weak OopStorage      Min:")) return true;
        if (line.contains(" StringTable Weak               Min:")) return true;
        if (line.contains(" ResolvedMethodTable Weak       Min:")) return true;
        if (line.contains(" JNI Weak                       Min:")) return true;
        return line.contains(" - age ");
    }

    /**
     * Ignore rules we don't need to process.
     *
     * @param trace GCLogTrace that hits this log line
     * @param line  Log line.
     */
    private void ignore(GCLogTrace trace, String line) {
        return;
    }

    private void log(String line) {
        if ( ! ignoreFrequentlySeenButUnwantedLines(line)) {

            GCToolKit.LOG_DEBUG_MESSAGE(() -> "Missed: " + line);
            LOGGER.log(Level.FINE, "Missed: {0}", line);
        }
    }

    @Override
    public boolean accepts(Diary diary) {
        return diary.isG1GC() && diary.isUnifiedLogging();
    }

    @Override
    public void publishTo(JVMEventChannel bus) {
        super.publishTo(bus);
    }

    private void publish(JVMEvent event) {
        super.publish(ChannelName.G1GC_PARSER_OUTBOX,event);
    }
}
