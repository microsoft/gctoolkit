// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.RegionSummary;
import com.microsoft.gctoolkit.event.g1gc.G1GCConcurrentEvent;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.parser.collection.RuleSet;
import com.microsoft.gctoolkit.parser.jvm.Decorators;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;
import com.microsoft.gctoolkit.parser.unified.UnifiedG1GCPatterns;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.fromLabel;

/**
 * TODO No reports or views generated from this data yet.
 * <p>
 * Result on
 * - when GC started
 * - type of GC triggered
 * - from, to, configured
 * - pause time if it is reported or can be calculated
 */
public class UnifiedG1GCParser extends UnifiedGCLogParser implements UnifiedG1GCPatterns {

    private static final Logger LOGGER = Logger.getLogger(UnifiedG1GCParser.class.getName());
    private boolean debugging = Boolean.getBoolean("microsoft.debug");

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
        parseRules.put(HEAP_REGION_SIZE, this::heapRegionSize);
        parseRules.put(HEAP_SIZE, this::heapSize);
        parseRules.put(G1_TAG, this::ignore);

        parseRules.put(CONCURRENT_CYCLE_START, this::concurrentCycleStart);
        parseRules.put(CONCURRENT_CYCLE_END, this::concurrentCycleEnd);
        parseRules.put(CONCURRENT_PHASE, this::concurrentPhase);
        parseRules.put(CONCURRENT_PHASE_DURATION, this::concurrentPhaseDuration);
        parseRules.put(CONCURRENT_MARK_PHASE, this::concurrentMarkInternalPhases);
        parseRules.put(CONCURRENT_MARK_PHASE_DURATION, this::concurrentMarkInternalPhaseDuration);
        parseRules.put(CONCURRENT_MARK_START, this::concurrentPhase);
        parseRules.put(CONCURRENT_MARK_WORKERS, this::concurrentMarkWorkers);
        parseRules.put(CONCURRENT_MARK_ABORTED, this::concurrentMarkAborted);
        parseRules.put(CONCURRENT_MARK_END, this::concurrentMarkEnd);
        parseRules.put(PAUSE_REMARK_START, this::remarkStart);
        parseRules.put(FINIALIZE_MARKING, this::finalizeMarking);
        parseRules.put(SYSTEM_DICTIONARY_UNLOADING, this::systemDictionaryUnloading);
        parseRules.put(STRING_SYMBOL_TABLE, this::stringSymbolTableCleaning);
        parseRules.put(PARALLEL_UNLOADING, this::parallelUnloading);
        parseRules.put(PAUSE_REMARK_END, this::pausePhaseDuringConcurrentCycleDurationEnd);
        parseRules.put(CLEANUP_START, this::cleanupStart);
        parseRules.put(CLEANUP_END, this::pausePhaseDuringConcurrentCycleDurationEnd);
        parseRules.put(FULL_PHASE, this::fullPhase);
        parseRules.put(FULL_CLASS_UNLOADING, this::fullClassUnloading);
        parseRules.put(FULL_STRING_SYMBOL_TABLE, this::fullStringSymbolTable);
        parseRules.put(JVM_EXIT, this::jvmExit);
        parseRules.put(new GCParseRule("END_OF_DATA_SENTINAL", END_OF_DATA_SENTINAL), this::endOfFile);
    }

    public UnifiedG1GCParser(LoggingDiary diary, JVMEventConsumer consumer) {
        super(diary, consumer);
    }

    public String getName() {
        return "UnifiedG1GCParser";
    }

    @Override
    protected void process(String line) {
        if (ignoreFrequentlySeenButUnwantedLines(line)) return;
        parse(line);
    }

    private void parse(String line) {
        Optional<AbstractMap.SimpleEntry<GCParseRule, GCLogTrace>> ruleToApply = parseRules.keys().stream()
                .map(rule -> new AbstractMap.SimpleEntry<>(rule, rule.parse(line)))
                .filter(tuple -> tuple.getValue() != null)
                .findFirst();
        if (!ruleToApply.isPresent()) {
            log(line);
            return;
        }

        try {
            setForwardReference(line);
            parseRules.get(ruleToApply.get().getKey()).accept(ruleToApply.get().getValue(), line);
        } catch (Throwable t) {
            LOGGER.throwing(this.getName(), "process", t);
        }
    }

    private void setForwardReference(String line) {
        GCLogTrace trace = GCID_COUNTER.parse(line);
        if (trace != null) {
            int gcid = trace.getIntegerGroup(1);
            forwardReference = collectionsUnderway.get(gcid);
            if (forwardReference == null) {
                forwardReference = new G1GCForwardReference(new Decorators(line), trace.getIntegerGroup(1));
                collectionsUnderway.put(forwardReference.getGcID(), forwardReference);
            } else if (gcid != forwardReference.getGcID())
                forwardReference = collectionsUnderway.get(gcid);
        }
    }

    private void removeForwardReference(G1GCForwardReference forwardReference) {
        collectionsUnderway.remove(forwardReference.getGcID());
    }

    /*************
     *
     * Data Extraction methods
     */

    private void cpuBreakout(GCLogTrace trace, String line) {
        CPUSummary cpuSummary = new CPUSummary(trace.getDoubleGroup(1), trace.getDoubleGroup(2), trace.getDoubleGroup(3));
        forwardReference.setCPUSummary(cpuSummary);
        record(forwardReference.buildEvent());
    }

    // Just in case there isn't a JVM termination event in the log.
    public void endOfFile(GCLogTrace trace, String line) {
        consumer.record(new JVMTermination((jvmTerminationEventTime.getTimeStamp() < 0.0d)
                ? getClock() : jvmTerminationEventTime));
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
        G1GCForwardReference.setMinHeapSize(trace.getLongGroup(1));
        G1GCForwardReference.setInitialHeapSize(trace.getLongGroup(2));
        G1GCForwardReference.setMaxHeapSize(trace.getLongGroup(3));
    }

    //return to JVM
    private int regionSize = 0; //region size in Gigabytes

    public void heapRegionSize(GCLogTrace trace, String line) {
        regionSize = trace.getIntegerGroup(1);
        G1GCForwardReference.setHeapRegionSize(regionSize);
    }

    //[15.316s][debug][gc,heap      ] GC(0)   region size 1024K, 24 young (24576K), 0 survivors (0K)
    //ignore this logging for now
    private void youngRegionAllotment(GCLogTrace trace, String line) {
//        if (before) {
//            forwardReference.setYoungOccupancyBeforeCollection(trace.getLongGroup(3));
//            forwardReference.setSurvivorOccupancyBeforeCollection(trace.getLongGroup(5));
//            forwardReference.setEdenOccupancyBeforeCollection(trace.getLongGroup(3)-trace.getLongGroup(5));
//            forwardReference.setYoungSizeBeforeCollection(trace.getLongGroup(3));
//        }
//        else {
//            forwardReference.setYoungOccupancyAfterCollection(trace.getLongGroup(5));
//            forwardReference.setSurvivorOccupancyAfterCollection(trace.getLongGroup(5));
//            forwardReference.setEdenOccupancyAfterCollection(0L);
//            forwardReference.setYoungSizeAfterCollection(trace.getLongGroup(3));
//        }
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
        switch (trace.getGroup(1)) {
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

    /*
        [15.322s][info ][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms
        [15.322s][debug][gc,phases    ] GC(0)     Choose Collection Set: 0.0ms
        [15.322s][debug][gc,phases    ] GC(0)     Humongous Register: 0.0ms
     */

    private void preEvacuateCSetPhaseDuration(GCLogTrace trace, String line) {
        forwardReference.recordPreEvacuateCSetPhaseDuration(trace.getGroup(1), trace.getDurationInSeconds());
    }

    public void evacuateCSetPhase(GCLogTrace trace, String line) {
        forwardReference.recordEvacuateCSetPhaseDuration(trace.getGroup(1), trace.getUnifiedStatisticalSummary());
    }

    public void postEvacuatePhaseDuration(GCLogTrace trace, String line) {
        forwardReference.recordPostEvacuateCSetPhaseDuration(trace.getGroup(1), trace.getDurationInSeconds());
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

    public void regionSummary(GCLogTrace trace, String line) {
        RegionSummary summary = trace.regionSummary();
        switch (trace.getGroup(1)) {
            case "Eden":
                forwardReference.setEdenOccupancyBeforeCollection(summary.getBefore() * regionSize * 1024);
                forwardReference.setEdenOccupancyAfterCollection(summary.getAfter() * regionSize * 1024);
                forwardReference.setEdenSizeBeforeCollection(summary.getBefore() * regionSize * 1024);
                forwardReference.setEdenSizeAfterCollection(summary.getAssigned() * regionSize * 1024);
                break;
            case "Survivor":
                forwardReference.setSurvivorOccupancyBeforeCollection(summary.getBefore() * regionSize * 1024);
                forwardReference.setSurvivorOccupancyAfterCollection(summary.getAfter() * regionSize * 1024);
                forwardReference.setSurvivorSizeBeforeCollection(summary.getBefore() * regionSize * 1024);
                forwardReference.setSurvivorSizeAfterCollection(summary.getAssigned() * regionSize * 1024);
                break;
            case "Old":
                forwardReference.setOldOccupancyBeforeCollection(summary.getBefore() * regionSize * 1024);
                forwardReference.setOldOccupancyAfterCollection(summary.getAfter() * regionSize * 1024);
                forwardReference.setOldSizeBeforeCollection(summary.getBefore() * regionSize * 1024);
                forwardReference.setOldSizeAfterCollection(summary.getAfter() * regionSize * 1024);
                break;
            case "Humongous":
                forwardReference.setHumongousOccupancyBeforeCollection(summary.getBefore() * regionSize * 1024);
                forwardReference.setHumongousOccupancyAfterCollection(summary.getAfter() * regionSize * 1024);
                forwardReference.setHumongousSizeBeforeCollection(summary.getBefore() * regionSize * 1024);
                forwardReference.setHumongousSizeAfterCollection(summary.getAfter() * regionSize * 1024);
                break;
            default:
                notYetImplemented(trace, line);
        }
    }

    public void unifiedMetaData(GCLogTrace trace, String line) {
        if (forwardReference.setMetaspaceOccupancyBeforeCollection(trace.getMemoryInKBytes(1))) {
            forwardReference.setMetaspaceOccupancyAfterCollection(trace.getMemoryInKBytes(3));
            forwardReference.setMetaspaceSizeAfterCollection(trace.getMemoryInKBytes(5));
        }
    }

    public void youngDetails(GCLogTrace trace, String line) {
        forwardReference.setHeapOccupancyBeforeCollection(trace.getMemoryInKBytes(5));
        forwardReference.setHeapOccupancyAfterCollection(trace.getMemoryInKBytes(7));
        forwardReference.setHeapSizeAfterCollection(trace.getMemoryInKBytes(9));
        forwardReference.setDuration(trace.getDurationInSeconds());
    }

    //Concurrent Mark

    /**
     * Start of concurrent phases which can be ignored (for now??)
     *
     * @param trace
     * @param line
     */

    private void concurrentCycleStart(GCLogTrace trace, String line) {
        forwardReference.setGcType(GarbageCollectionTypes.Concurrent_Cycle);
    }

    private void concurrentCycleEnd(GCLogTrace trace, String line) {
        removeForwardReference(forwardReference);
    }

    private void concurrentPhase(GCLogTrace trace, String line) {
        forwardReference.setConcurrentPhase(GarbageCollectionTypes.fromLabel(trace.getGroup(1)));
        forwardReference.setStartTime(getClock());
        concurrentPhaseActive = true;
    }

    private void concurrentMarkEnd(GCLogTrace trace, String line) {
        forwardReference.setDuration(trace.getDurationInSeconds());
        record(forwardReference.buildConcurrentEvent());
    }

    private void concurrentPhaseDuration(GCLogTrace trace, String line) {
        forwardReference.setDuration(trace.getDurationInSeconds());
        record(forwardReference.buildConcurrentEvent());
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
        record(forwardReference.buildConcurrentEvent());
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
        forwardReference.setHeapOccupancyBeforeCollection(trace.getMemoryInKBytes(1));
        forwardReference.setHeapSizeBeforeCollection(trace.getMemoryInKBytes(5));
        forwardReference.setHeapOccupancyAfterCollection(trace.getMemoryInKBytes(3));
        forwardReference.setHeapSizeAfterCollection(trace.getMemoryInKBytes(5));
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

    private void record(G1GCConcurrentEvent event) {
        if ( event == null) return;
        consumer.record(event);
        concurrentPhaseActive = false;
        eventQueue.stream().forEach(consumer::record);
        eventQueue.clear();
    }

    private final Queue<G1GCPauseEvent> eventQueue = new LinkedList<>();
    private void record(G1GCPauseEvent event) {
        if (event == null) return;
        if ( concurrentPhaseActive)
            eventQueue.add(event);
        else
            consumer.record(event);
        if (!forwardReference.isConcurrentCycle())
            removeForwardReference(forwardReference);
    }


    // Let the EOF event send the exit event. Easier in cases where JVM termination isn't in the log
    private void jvmExit(GCLogTrace trace, String s) {
        jvmTerminationEventTime = this.getClock();
    }

    private boolean ignoreFrequentlySeenButUnwantedLines(String line) {
        if (line.contains("Desired survivor size")) return true;
        if (line.contains("Age table with threshold")) return true;
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
        if (line.contains("Desired survivor size")) return;
        if (line.contains("Age table with threshold")) return;
        if (line.contains("- age")) return;

        if (debugging)
            LOGGER.fine("Missed: " + line);

        LOGGER.log(Level.FINE, "Missed: {0}", line);
    }
}
