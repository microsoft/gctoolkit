// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.ReferenceGCSummary;
import com.microsoft.gctoolkit.event.StatisticalSummary;
import com.microsoft.gctoolkit.event.SurvivorMemoryPoolSummary;
import com.microsoft.gctoolkit.event.g1gc.ConcurrentScanRootRegion;
import com.microsoft.gctoolkit.event.g1gc.G1Cleanup;
import com.microsoft.gctoolkit.event.g1gc.G1ConcurrentCleanup;
import com.microsoft.gctoolkit.event.g1gc.G1ConcurrentMark;
import com.microsoft.gctoolkit.event.g1gc.G1ConcurrentMarkResetForOverflow;
import com.microsoft.gctoolkit.event.g1gc.G1ConcurrentStringDeduplication;
import com.microsoft.gctoolkit.event.g1gc.G1FullGCNES;
import com.microsoft.gctoolkit.event.g1gc.G1GCConcurrentEvent;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.g1gc.G1Mixed;
import com.microsoft.gctoolkit.event.g1gc.G1Remark;
import com.microsoft.gctoolkit.event.g1gc.G1SystemGC;
import com.microsoft.gctoolkit.event.g1gc.G1Trap;
import com.microsoft.gctoolkit.event.g1gc.G1Young;
import com.microsoft.gctoolkit.event.g1gc.G1YoungInitialMark;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.parser.collection.MRUQueue;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;

import java.util.AbstractMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO No reports or views generated from this data yet.
 * <p>
 * Result
 * -0 on when GC started
 * - type of GC triggered
 * - from, to, configured
 * - pause time if it is reported or can be calculated
 */
public class PreUnifiedG1GCParser extends PreUnifiedGCLogParser implements G1GCPatterns {

    private static final Logger LOGGER = Logger.getLogger(PreUnifiedG1GCParser.class.getName());
    private boolean debugging = Boolean.getBoolean("microsoft.debug");

    //values show up at the end of GC log file from a normally terminated JVM
    private long heapTotal, heapUsed;
    private long regionSize;
    private long metaSpaceUsed, metaCapacity, metaCommitted, metaReserved;
    private long classSpaceUsed, classSpaceCapacity, classSpaceCommitted, classSpaceReserved;

    private final G1GCPauseEvent trap = new G1Trap();
    private G1GCPauseEvent forwardReference = trap;

    private DateTimeStamp timeStampForwardReference;
    private GCCause gcCauseForwardReference;
    private GarbageCollectionTypes collectionTypeForwardReference;
    private ReferenceGCSummary referenceGCForwardReferenceSummary;

    private DateTimeStamp concurrentPhaseStartTimeStamp;
    private GarbageCollectionTypes concurrentCollectionTypeForwardReference;
    private final ConcurrentLinkedQueue<JVMEvent> backlog = new ConcurrentLinkedQueue<>();

    private final MRUQueue<GCParseRule, BiConsumer<GCLogTrace, String>> parseRules;

    {
        parseRules = new MRUQueue<>();
        parseRules.put(CPU_BREAKDOWN, this::recordCPUSummary);
        parseRules.put(G1_DETAILS, this::processYoungGenCollection);
        parseRules.put(YOUNG, this::processYoung);
        parseRules.put(FULL_GC, this::g1gcFullGC); //113.325: [Full GC (System.gc()) 37M->32M(96M), 0.0943030 secs]
        parseRules.put(G1_MEMORY_SUMMARY, this::processMemorySummary);
        parseRules.put(G1_NO_DETAILS_MEMORY_SUMMARY, this::processNoDetailsMemorySummary);
        parseRules.put(G1_PARALLEL_PHASE_SUMMARY, this::processParallelPhaseSummary);
        parseRules.put(G1_SOLARIS_PARALLEL_PHASE, this::processParallelPhase);
        parseRules.put(TERMINATION_ATTEMPTS, this::ignore);
        parseRules.put(PROCESSED_BUFFERS, this::processedBuffers);
        parseRules.put(PROCESSED_BUFFER, this::processedBuffer);
        parseRules.put(SOLARIS_WORKER_PARALLEL_BLOCK, this::solarisWorkerParallelBlock);
        parseRules.put(SOLARIS_WORKER_PARALLEL_ACTIVITY, this::ignore);
        parseRules.put(G1GC_PHASE_DETAIL_CLAUSE, this::processPhaseDetailClause);
        parseRules.put(G1GC_PHASE, this::processG1GCPhase);
        parseRules.put(CONCURRENT_STRING_DEDUP, this::concurrentStringDedup);
        parseRules.put(STRING_DEDUP_FIXUP, this::stringDedupFixup);
        parseRules.put(QUEUE_FIXUP, this::recordQueueFixup);
        parseRules.put(TABLE_FIXUP, this::recordTableFixup);
        parseRules.put(WORKER_ACTIVITY, this::workerActivity);
        parseRules.put(PARALLEL_TIME, this::parallelTime);
        parseRules.put(WORKER_PARALLEL_BLOCK, this::workerParallelBlock);
        parseRules.put(G1_FUll, this::g1gcDetailedFullGC);
        parseRules.put(G1_FULL_INTERRUPTS_CONCURRENT_CYCLE, this::g1FullInterruptsConcurrentCycle);
        parseRules.put(FULL_WITH_CONCURRENT_PHASE_START, this::g1FullWithConcurrentPhaseStart);
        parseRules.put(FULL_WITH_CONCURRENT_PHASE_CORRUPTED, this::g1FullWithConcurrentPhaseCorrupted);
        parseRules.put(FULL_MISSING_TIMESTAMP_CONCURRENT_START, this::fullMissingTimeStampWithConcurrentStart);
        parseRules.put(FULL_WITH_CONCURRENT_PHASE_INTERLEAVED, this::g1FullWIthConcurrentPhaseInterleaved);
        parseRules.put(FULL_WITH_CONCURRENT_END, this::fullWithConcurrentEnd);

        parseRules.put(FULL_GC_FRAGMENT, this::fullGCFragment);
        parseRules.put(CONCURRENT_START_V3, this::concurrentStartV3);
        parseRules.put(CONCURRENT_START_V4, this::concurrentStartV4);
        parseRules.put(CONCURRENT_START_V5, this::concurrentStartV3);

        parseRules.put(CORRUPTED_CONCURRENT_START, this::corruptedConcurrentStart);
        parseRules.put(CORRUPTED_CONCURRENT_START_V2, this::corruptedConcurrentStartV2);
        parseRules.put(CORRUPTED_CONCURRENT_START_V3, this::corruptedConcurrentStartV3);
        parseRules.put(CORRUPTED_CONCURRENT_START_V4, this::corruptedConcurrentStartV4);
        parseRules.put(CORRUPTED_CONCURRENT_START_V5, this::corruptedConcurrentStartV5);
        parseRules.put(CORRUPTED_CONCURRENT_START_V7, this::corruptedConcurrentStartV7);
        parseRules.put(CORRUPTED_CONCURRENT_START_V8, this::corruptedConcurrentStartV8);
        parseRules.put(CORRUPTED_CONCURRENT_START_V9, this::corruptedConcurrentStartV9);
        parseRules.put(YOUNG_WITH_CONCURRENT_END, this::youngWithConcurrentEnd);
        parseRules.put(YOUNG_SPLIT_AT_DATESTAMP, this::youngSplitAtDatestamp);
        parseRules.put(YOUNG_SPLIT_AT_TIMESTAMP, this::youngSplitAtTimestamp);
        parseRules.put(FREE_FLOATING_YOUNG_BLOCK, this::freeFloatingYoungBlock);
        parseRules.put(FULLGC_WITH_CONCURRENT_PHASE, this::fullGCWithConcurrentPhase);

        parseRules.put(G1_YOUNG_SPLIT_START, this::g1YoungSplitStart);
        parseRules.put(G1_YOUNG_SPLIT_END, this::g1YoungSplitEnd);
        parseRules.put(G1_INITIAL_MARK, this::g1InitialMark);
        parseRules.put(FREE_FLOATING_OCCUPANCY_SUMMARY, this::freeFloatingOccupancySummary);

        parseRules.put(G1_CONCURRENT_START, this::g1ConcurrentStart);
        parseRules.put(G1_CONCURRENT_START_WITHOUT_PREFIX, this::g1ConcurrentStartWithoutPrefix);
        parseRules.put(G1_CONCURRENT_END, this::g1ConcurrentEnd);
        parseRules.put(G1_REMARK, this::g1Remark);
        parseRules.put(G1_180_REMARK, this::g1180Remark);
        parseRules.put(G1_180_REMARK_REF_DETAILS, this::g1180RemarkRefDetails);
        parseRules.put(G1_CLEANUP, this::g1Cleanup);
        parseRules.put(G1_CLEANUP_NO_MEMORY, this::g1CleanupNoMemory);
        parseRules.put(G1_CONCURRENT_ABORT, this::g1ConcurrentAbort);
        parseRules.put(CONCURRENT_MARK_OVERFLOW, this::concurrentMarkOverflow);
        parseRules.put(G1_CORRUPTED_CONCURRENT_END, this::g1CorruptedConcurrentEnd);
        parseRules.put(G1_CORRUPTED_CONCURRENT_ROOT_REGION_SCAN_END, this::g1ConcurrentEndCorruptedByApplicationTime);
        parseRules.put(G1_FLOATING_CONCURRENT_PHASE_START, this::g1FloatingConcurrentPhaseStart);
        parseRules.put(SPLIT_CLEANUP, this::splitCleanup);

        //adaptive sizing
        parseRules.put(YOUNG_SPLIT_BY_G1ERGONOMICS, this::youngSplitByG1Ergonomics);
        parseRules.put(G1_INITIAL_MARK_ERGONOMICS, this::g1InitialMarkErgonomics);

        //RSet rules
        parseRules.put(G1_YOUNG_RS_SUMMARY, this::ignore);
        parseRules.put(RSET_HEADER, this::ignore);
        parseRules.put(RSET_CONCONCURRENT_HEADER, this::ignore);
        parseRules.put(RSET_CONCURRENT_RS_Threads, this::ignore);
        parseRules.put(RSET_CONCURRENT_MUTATOR, this::ignore);
        parseRules.put(RSET_CONCURRENT_TIMES_HEADER, this::ignore);
        parseRules.put(RSET_RS_SIZE, this::ignore);
        parseRules.put(RSET_THREAD_TIMES, this::ignore);
        parseRules.put(RSET_RS_STATIC_STRUCTURES, this::ignore);
        parseRules.put(RSET_RS_OCCUPIED_CARDS, this::ignore);
        parseRules.put(RSET_MAX_REGION_SIZE, this::ignore);
        parseRules.put(RSET_COARSENINGS, this::ignore);

        //heap summary
        parseRules.put(new GCParseRule("Heap tag", "^Heap$"), this::ignore);
        parseRules.put(GARBAGE_FIRST_HEAP, this::garbageFirstHeap);
        parseRules.put(REGION_SIZE, this::regionSize);
        parseRules.put(METASPACE_FINAL, this::metaspaceFinal);
        parseRules.put(CLASSPACE_FINAL, this::classspaceFinal);

        //Reference GC
        parseRules.put(G1_DETAILS_REFERENCE_GC, this::g1DetailsReferenceGC);
        parseRules.put(G1_REMARK_REFERENCE_GC, this::g1RemarkReferenceGC);
        parseRules.put(G1_FULL_DETAILS_REFERENCE_GC, this::g1FullDetailsReferenceGC);
        parseRules.put(G1_DETAILS_REFERENCE_INITIAL_MARK, this::g1DetailsReferenceInitialMark);
        parseRules.put(FREE_FLOATING_REFERENCE_RECORDS, this::freeFloatingReferenceRecords);
        parseRules.put(FLOATING_REFERENCE_WITH_ADAPTIVE_SIZING, this::floatingReferenceWithAdaptiveSizing);
        parseRules.put(YOUNG_REFERENCE_WITH_CONCURRENT_END, this::youngReferenceWIthConcurrentEnd);
        parseRules.put(G1_FULL_INTERRUPTS_CONCURRENT_WITH_REFERENCES, this::g1FullInterruptsConcurrentWithReferences);
        parseRules.put(G1_FULL_MEMORY_SPLIT_BY_CONCURRENT, this::g1FullMemorySplitByConcurrent);

        //Ergonomic rules
        parseRules.put(G1_FULL_ADAPTIVE_SIZING, this::g1FullAdaptiveSizing);

        //18.298: [GC pause (G1 Evacuation Pause) (young) 18.298: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 0, predicted base time: 10.00 ms, remaining time: 190.00 ms, target pause time: 200.00 ms]
        parseRules.put(G1_YOUNG_WITH_CSET_CONSTRUCTION_START, this::notYetDefined);

        // 7.793: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 1395, predicted base time: 14.19 ms, remaining time: 185.81 ms, target pause time: 200.00 ms]
        parseRules.put(G1_CSET_CONSTRUCTION_START, this::notYetDefined);

        parseRules.put(CSET_CONSTRUCTION, this::csetConstruction);

        //18.298: [G1Ergonomics (CSet Construction) finish choosing CSet, eden: 512 regions, survivors: 0 regions, old: 0 regions, predicted pause time: 7843.33 ms, target pause time: 200.00 ms]
        parseRules.put(CSET_CONSTRUCTION_END, this::notYetDefined);

        //0.772: [G1Ergonomics (Heap Sizing) shrink the heap, requested shrinking amount: 255605857 bytes, aligned shrinking amount: 254803968 bytes, attempted shrinking amount: 254803968 bytes]
        parseRules.put(HEAP_SHRINK, this::notYetDefined);

        //18.303: [G1Ergonomics (Heap Sizing) expand the heap, requested expansion amount: 845600000 bytes, attempted expansion amount: 1048576 bytes]
        parseRules.put(HEAP_EXPAND, this::notYetDefined);

        //18.303: [G1Ergonomics (Heap Sizing) attempt heap expansion, reason: region allocation request failed, allocation request: 8456 bytes]
        parseRules.put(ATTEMPT_HEAP_EXPANSION_ALLOC_FAILURE, this::notYetDefined);

        //7.942: [G1Ergonomics (Heap Sizing) attempt heap expansion, reason: recent GC overhead higher than threshold after GC, recent GC overhead: 22.02 %, threshold: 10.00 %, uncommitted: 1060110336 bytes, calculated expansion amount: 212022067 bytes (20.00 %)]
        parseRules.put(ATTEMPT_HEAP_EXPANSION_OVERHEAD, this::notYetDefined);

        //0.785: [G1Ergonomics (Heap Sizing) did not shrink the heap, reason: heap shrinking operation failed]
        parseRules.put(HEAP_SHRINKING_FAILED, this::notYetDefined);

        //7.835: [G1Ergonomics (Concurrent Cycles) request concurrent cycle initiation, reason: occupancy higher than threshold, occupancy: 7340032 bytes, allocation request: 0 bytes, threshold: 6134130 bytes (45.00 %), source: end of GC]
        parseRules.put(HIGH_OCCUPANCY_TRIGGERS_CONC, this::notYetDefined);

        //7.842: [G1Ergonomics (Concurrent Cycles) initiate concurrent cycle, reason: concurrent cycle initiation requested]
        parseRules.put(INITIATE_CONC_CYCLE, this::notYetDefined);

        //7.916: [G1Ergonomics (Concurrent Cycles) do not request concurrent cycle initiation, reason: still doing mixed collections, occupancy: 9437184 bytes, allocation request: 0 bytes, threshold: 6134130 bytes (45.00 %), source: end of GC]
        parseRules.put(DO_NOT_REQUEST_CONC_CYCLE, this::notYetDefined);

        //7.859: [G1Ergonomics (Mixed GCs) start mixed GCs, reason: candidate old regions available, candidate old regions: 4 regions, reclaimable: 2406696 bytes (17.66 %), threshold: 10.00 %]
        //7.869: [G1Ergonomics (Mixed GCs) do not continue mixed GCs, reason: reclaimable percentage not over threshold, candidate old regions: 2 regions, reclaimable: 1031104 bytes (7.56 %), threshold: 10.00 %]
        //7.986: [G1Ergonomics (Mixed GCs) do not start mixed GCs, reason: reclaimable percentage not over threshold, candidate old regions: 3 regions, reclaimable: 1380376 bytes (2.53 %), threshold: 10.00 %]
        parseRules.put(START_MIXED_GC, this::notYetDefined);
        parseRules.put(DELAY_MIXED_GC, this::notYetDefined);

        //7.866: [G1Ergonomics (CSet Construction) finish adding old regions to CSet, reason: old CSet region num reached max, old: 2 regions, max: 2 regions]
        //7.866: [G1Ergonomics (CSet Construction) finish add ing old regions to CSet, reason: old CSet region num reached max, old: 2 regions, max: 2 regions]
        parseRules.put(CSET_FINISH, this::notYetDefined);
        parseRules.put(CSET_ADDING, this::notYetDefined);

        parseRules.put(new GCParseRule("END_OF_DATA_SENTINAL", END_OF_DATA_SENTINAL), this::endOfFile);
    }

    public PreUnifiedG1GCParser(LoggingDiary diary, JVMEventConsumer consumer) {
        super(diary, consumer);
        forwardReference = trap;
    }

    public String getName() {
        return "PreUnifiedG1GCParser";
    }

    @Override
    protected void process(String line) {

        if (ignoreFrequentlySeenButUnwantedLines(line)) return;

        try {
            parseRules.keys()
                    .stream()
                    .map(rule -> new AbstractMap.SimpleEntry<>(rule, rule.parse(line)))
                    .filter(tuple -> tuple.getValue() != null)
                    .findFirst()
                    .ifPresentOrElse(
                            rulesAndTrace -> parseRules.get(rulesAndTrace.getKey()).accept(rulesAndTrace.getValue(), line),
                            () -> log(line));
        } catch (Throwable t) {
            LOGGER.throwing(this.getName(), "deriveConfiguration", t);
        }
    }

    void notYetDefined(GCLogTrace trace, String line) {
        if (System.getProperty("microsoft.develop.ergonomics") != null)
            trace.notYetImplemented();
    }

    public void endOfFile(GCLogTrace trace, String line) {
        consumer.record(new JVMTermination(getClock()));
    }

    //18.298: [G1Ergonomics (CSet Construction) add young regions to CSet, eden: 512 regions, survivors: 0 regions, predicted young region time: 7833.33 ms]
    void csetConstruction(GCLogTrace trace, String line) {
        if (System.getProperty("microsoft.develop.ergonomics") != null)
            trace.notYetImplemented();
    }

    //112.230: [Full GC (System.gc()) 112.327: [G1Ergonomics (Heap Sizing) attempt heap shrinking, reason: capacity higher than max desired capacity after Full GC, capacity: 128974848 bytes, occupancy: 29902320 bytes, max desired capacity: 99674399 bytes (70.00 %)]
    void g1FullAdaptiveSizing(GCLogTrace trace, String line) {
        timeStampForwardReference = getClock();
        collectionTypeForwardReference = GarbageCollectionTypes.Full;
    }

    /**
     * Convert if-else blocks to method calls to model GenerationalHeapParser implementation
     */

    private void g1YoungSplitStart(GCLogTrace trace, String line) {
        timeStampForwardReference = getClock();
        gcCauseForwardReference = trace.gcCause(3, 0);
        if ("(young)".equals(trace.getGroup(4)))
            collectionTypeForwardReference = GarbageCollectionTypes.Young;
        else
            collectionTypeForwardReference = GarbageCollectionTypes.Mixed;
    }

    private void stringDedupFixup(GCLogTrace trace, String line) {
        ((G1Young) forwardReference).setStringDedupingDuration(trace.getDoubleGroup(1), trace.getIntegerGroup(2));
    }

    private void recordQueueFixup(GCLogTrace trace, String line) {
        StatisticalSummary summary = extractCounterSummary(trace, 0);
        ((G1Young) forwardReference).queueFixupStatistics(summary);
    }

    private void recordTableFixup(GCLogTrace trace, String line) {
        StatisticalSummary summary = extractCounterSummary(trace, 0);
        ((G1Young) forwardReference).tableFixupStatistics(summary);
    }

    /**
     * Quick failure for lines that commonly appear that aren't interesting for this
     * parser.
     *
     * @param line log line
     * @return boolean true is the line should be ignored.
     */
    private final GCParseRule corruptedApplicationTime = new GCParseRule("corruptedApplicationTime", DATE_TIMESTAMP + TIMESTAMP);

    private boolean ignoreFrequentlySeenButUnwantedLines(String line) {
        if (line.contains("- age ")) return true;
        if (line.contains("Total time for which application threads were stopped")) return true;
        if (line.contains("Application time: ")) {
            if (corruptedApplicationTime.parse(line) != null)
                concurrentPhaseStartTimeStamp = getClock();
            return true;
        }
        if (line.contains("Termination Attempts:")) return true;
        if (line.contains("Desired survivor size")) return true;
        if (line.startsWith("{Heap before GC invocations")) return true;
        if (line.startsWith("region size ")) return true;
        if (line.startsWith("compacting perm gen  total ")) return true;
        if (line.startsWith("the space ")) return true;
        if (line.startsWith("No shared spaces configured.")) return true;
        if (line.startsWith("Heap after GC invocations=")) return true;
        if (line.startsWith("OpenJDK")) return true;
        if (line.equals("}")) return true;
        return line.contains("Allocation failed. Thread");
    }

    /**
     * Ignore rules we don't need to deriveConfiguration.
     *
     * @param trace GCLogTrace that hits this log line
     * @param line  Log line.
     */
    private void ignore(GCLogTrace trace, String line) {
        return;
    }


    //2014-02-22T10:49:26.487-0100: 7.477: [GC pause (G1 Evacuation Pause) (young) (initial-mark), 0.0042120 secs]
    //2014-02-22T10:49:26.501-0100: 7.490: [GC pause (G1 Evacuation Pause) (young), 0.0025420 secs]
    //2014-02-22T10:49:26.508-0100: 7.498: [GC pause (G1 Evacuation Pause) (mixed), 0.0026410 secs]
    //26.893: [GC pause (G1 Evacuation Pause) (young) (to-space exhausted), 0.1709670 secs]
    //115.421: [GC pause (G1 Evacuation Pause) (young) (initial-mark) (to-space exhausted), 0.0476190 secs]
    private void processYoungGenCollection(GCLogTrace trace, String line) {

        boolean initialMark = trace.contains(5, "initial-mark");
        boolean tospaceExhausted = trace.contains(trace.groupCount() - 2, "to-space");

        if (trace.contains(4, "young")) {
            if (initialMark)
                forwardReference = new G1YoungInitialMark(trace.getDateTimeStamp(), trace.gcCause(3, 0), trace.getDoubleGroup(trace.groupCount()));
            else
                forwardReference = new G1Young(trace.getDateTimeStamp(), trace.gcCause(3, 0), trace.getDoubleGroup(trace.groupCount()));
            if (tospaceExhausted)
                ((G1Young) forwardReference).toSpaceExhausted();
        } else if (trace.contains(4, "mixed")) {
            forwardReference = new G1Mixed(trace.getDateTimeStamp(), trace.gcCause(3, 0), trace.getDoubleGroup(trace.groupCount()));
            if (tospaceExhausted)
                ((G1Mixed) forwardReference).toSpaceExhausted();
        } else
            trace.notYetImplemented();
    }

    //Simple records
    //5.478: [GC pause (young) 8878K->5601K(13M), 0.0027650 secs]
    //1566.108: [GC pause (mixed) 7521K->5701K(13M), 0.0030090 secs]
    //549.243: [GC pause (young) (initial-mark) 9521K->7824K(13M), 0.0021590 secs]
    private void processYoung(GCLogTrace trace, String line) {
        if (trace.getGroup(7) != null)
            trace.notYetImplemented();
        if (trace.getGroup(8) != null)
            trace.notYetImplemented();
        MemoryPoolSummary summary = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(9);
        if ("young".equals(trace.getGroup(4))) {
            G1Young collection = null;
            if (trace.getGroup(6) == null)
                collection = new G1Young(getClock(), trace.gcCause(), trace.getPauseTime());
            else {
                trace.notYetImplemented();
                return;
            }
            collection.addMemorySummary(summary);
            record(collection);
        } else if ("mixed".equals(trace.getGroup(4))) {
            G1Young collection = new G1Mixed(getClock(), trace.gcCause(), trace.getPauseTime());
            collection.addMemorySummary(summary);
            record(collection);
        } else
            trace.notYetImplemented();
    }

    //[Eden: 512.0M(512.0M)->0.0B(505.0M) Survivors: 0.0B->7168.0K Heap: 512.0M(518.0M)->6418.4K(519.0M)]
    private void processMemorySummary(GCLogTrace trace, String line) {
        MemoryPoolSummary edenSummary = extractPoolSummary(trace, 1);
        MemoryPoolSummary heap = extractPoolSummary(trace, 13);
        SurvivorMemoryPoolSummary survivor = extractSurvivorPoolSummary(trace, 9);
        forwardReference.addMemorySummary(edenSummary, survivor, heap);
        forwardReference.addPermOrMetaSpaceRecord(extractPermOrMetaspaceRecord(line));
        if ((diary != null) && (!diary.isPrintGCDetails()))
            record(forwardReference);
    }

    private void processNoDetailsMemorySummary(GCLogTrace trace, String line) {
        MemoryPoolSummary summary = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);

        if (collectionTypeForwardReference == GarbageCollectionTypes.Young) {
            forwardReference = new G1Young(timeStampForwardReference, gcCauseForwardReference, trace.getPauseTime());
            forwardReference.addMemorySummary(summary);
            record(forwardReference);
        } else if (collectionTypeForwardReference == GarbageCollectionTypes.Mixed) {
            forwardReference = new G1Mixed(timeStampForwardReference, gcCauseForwardReference, trace.getPauseTime());
            forwardReference.addMemorySummary(summary);
            record(forwardReference);
        } else if (collectionTypeForwardReference == GarbageCollectionTypes.G1GCYoungInitialMark) {
            forwardReference = new G1YoungInitialMark(timeStampForwardReference, gcCauseForwardReference, trace.getPauseTime());
            forwardReference.addMemorySummary(summary);
            record(forwardReference);
        } else if (collectionTypeForwardReference == GarbageCollectionTypes.Full) {
            forwardReference = new G1FullGCNES(timeStampForwardReference, gcCauseForwardReference, trace.getPauseTime());
            if (!hasPrintGCDetails()) {
                forwardReference.addMemorySummary(summary);
                record(forwardReference);
            }
        } else if (collectionTypeForwardReference == GarbageCollectionTypes.G1GCCleanup) {
            G1Cleanup cleanup = new G1Cleanup(timeStampForwardReference, trace.getDuration());
            cleanup.addMemorySummary(summary);
            record(cleanup);
        } else
            trace.notYetImplemented();
    }

    private void processParallelPhaseSummary(GCLogTrace trace, String line) {
        ((G1Young) forwardReference).addParallelPhaseSummary(trace.getGroup(1), extractStatisticalSummaryWithSum(trace, 1));
    }

    private void processParallelPhase(GCLogTrace trace, String line) {
        double count = trace.getDoubleGroup(2);
        ((G1Young) forwardReference).addParallelPhaseSummary(trace.getGroup(1), new StatisticalSummary(count, count, count, 0.0d, count));
    }

    private void processPhaseDetailClause(GCLogTrace trace, String line) {
        ((G1Young) forwardReference).addPhaseDuration(trace.getGroup(1), trace.getDoubleGroup(2));
    }

    private void processG1GCPhase(GCLogTrace trace, String line) {
        String phase = trace.getGroup(1);
        if ("Code Root Fixup".equals(phase))
            ((G1Young) forwardReference).setCodeRootFixupDuration(trace.getDoubleGroup(2));
        else if ("Code Root Migration".equals(phase))
            ((G1Young) forwardReference).setCodeRootMigrationDuration(trace.getDoubleGroup(2));
        else if ("Code Root Purge".equals(phase))
            ((G1Young) forwardReference).setCodeRootPurgeDuration(trace.getDoubleGroup(2));
        else if ("Clear CT".equals(phase))
            ((G1Young) forwardReference).setClearCTDuration(trace.getDoubleGroup(2));
        else if ("Other".equals(phase))
            ((G1Young) forwardReference).setOtherPhaseDurations(trace.getDoubleGroup(2));
        else if ("Expand Heap".equals(phase)) {
            ((G1Young) forwardReference).setExpandHeapDuration(trace.getDoubleGroup(2));
        } else
            trace.notYetImplemented();
    }

    private void workerActivity(GCLogTrace trace, String line) {
        ((G1Young) forwardReference).addWorkerActivity(trace.getGroup(1), extractStatisticalSummaryWithSum(trace, 1));

    }

    private void parallelTime(GCLogTrace trace, String line) {
        ((G1Young) forwardReference).setParallelPhaseDuration(trace.getDoubleGroup(1));
        ((G1Young) forwardReference).setGcWorkers(trace.getIntegerGroup(2));
    }

    private void workerParallelBlock(GCLogTrace trace, String line) {
        if (line.contains("Start"))
            ((G1Young) forwardReference).setWorkersStart(extractStatisticalSummary(trace, 1));
        else if (line.contains("End"))
            ((G1Young) forwardReference).setWorkersEnd(extractStatisticalSummary(trace, 1));
    }

    private void solarisWorkerParallelBlock(GCLogTrace trace, String line) {
        double time = trace.getDoubleGroup(2);
        StatisticalSummary summary = new StatisticalSummary(time, time, time, 0.0d, time);
        if (line.contains("Start"))
            ((G1Young) forwardReference).setWorkersStart(summary);
        else if (line.contains("End"))
            ((G1Young) forwardReference).setWorkersEnd(summary);
    }

    private void processedBuffers(GCLogTrace trace, String line) {
        ((G1Young) forwardReference).addProcessedBuffersSummary(extractCounterSummary(trace, 1));
    }

    private void processedBuffer(GCLogTrace trace, String line) {
        int count = trace.getIntegerGroup(1);
        ((G1Young) forwardReference).addProcessedBuffersSummary(new StatisticalSummary(count, count, count, 0.0d, count));
    }

    private void g1gcDetailedFullGC(GCLogTrace trace, String line) {
        forwardReference = new G1FullGCNES(trace.getDateTimeStamp(), trace.gcCause(), trace.getPauseTime());
    }

    private void g1FullInterruptsConcurrentCycle(GCLogTrace trace, String line) {
        // forward reference the Full GC (System.gc())
        if (trace.gcCause() == GCCause.JAVA_LANG_SYSTEM)
            collectionTypeForwardReference = GarbageCollectionTypes.SystemGC;
        else
            collectionTypeForwardReference = GarbageCollectionTypes.Full;
        timeStampForwardReference = getClock();
        gcCauseForwardReference = trace.gcCause();

        // close the concurrent phase
        if ("root-region-scan".equals(trace.getGroup(7))) {
            record(new ConcurrentScanRootRegion(concurrentPhaseStartTimeStamp, trace.getPauseTime()));
        } else if ("mark".equals(trace.getGroup(7))) {
            record(new G1ConcurrentMark(concurrentPhaseStartTimeStamp, trace.getPauseTime()));
        } else if ("cleanup".equals(trace.getGroup(7))) {
            record(new G1ConcurrentCleanup(concurrentPhaseStartTimeStamp, trace.getPauseTime()));
        } else
            trace.notYetImplemented();
    }

    private boolean setGarbageCollectionTypeForwardReference(String gcType) {
        if ("root-region-scan".equals(gcType)) {
            concurrentCollectionTypeForwardReference = GarbageCollectionTypes.ConcurrentRootRegionScan;
        } else if ("mark".equals(gcType)) {
            concurrentCollectionTypeForwardReference = GarbageCollectionTypes.G1GCConcurrentMark;
        } else if ("cleanup".equals(gcType)) {
            concurrentCollectionTypeForwardReference = GarbageCollectionTypes.G1GCCleanup;
        } else
            concurrentCollectionTypeForwardReference = null;
        return concurrentCollectionTypeForwardReference != null;
    }

    private void g1FullWithConcurrentPhaseStart(GCLogTrace trace, String line) {
        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        timeStampForwardReference = getClock();
        gcCauseForwardReference = trace.gcCause();
        concurrentPhaseStartTimeStamp = trace.getDateTimeStamp(2);
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(7)))
            trace.notYetImplemented();
    }

    private void g1FullWithConcurrentPhaseCorrupted(GCLogTrace trace, String line) {
        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        timeStampForwardReference = trace.getDateTimeStamp(1);
        gcCauseForwardReference = trace.gcCause(2);
        concurrentPhaseStartTimeStamp = timeStampForwardReference;
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(6)))
            trace.notYetImplemented();
    }

    private void g1FullWIthConcurrentPhaseInterleaved(GCLogTrace trace, String line) {
        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        timeStampForwardReference = getClock();
        gcCauseForwardReference = trace.gcCause(1);
        concurrentPhaseStartTimeStamp = timeStampForwardReference;
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(6)))
            trace.notYetImplemented();
    }

    private void fullMissingTimeStampWithConcurrentStart(GCLogTrace trace, String line) {
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(4)))
            trace.notYetImplemented();
        timeStampForwardReference = trace.getDateTimeStamp(1);
        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        gcCauseForwardReference = trace.gcCause(0, 1);
    }

    private void fullWithConcurrentEnd(GCLogTrace trace, String line) {
        if (!recordConcurrentPhase(trace.getGroup(5), trace.getDuration()))
            trace.notYetImplemented();

        timeStampForwardReference = new DateTimeStamp(trace.getGroup(2), trace.getDoubleGroup(3));
        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        gcCauseForwardReference = trace.gcCause(0, 1);
    }

    private void fullGCFragment(GCLogTrace trace, String line) {
        timeStampForwardReference = trace.getDateTimeStamp();
        gcCauseForwardReference = trace.gcCause();
        collectionTypeForwardReference = GarbageCollectionTypes.Full;
    }

    private void concurrentStartV3(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = new DateTimeStamp(trace.getGroup(1), trace.getDoubleGroup(3));
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(4)))
            trace.notYetImplemented();
    }

    private void concurrentStartV4(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = new DateTimeStamp(trace.getGroup(1), trace.getDoubleGroup(2));
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(3)))
            trace.notYetImplemented();
    }

    private void corruptedConcurrentStart(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = new DateTimeStamp(trace.getDateStamp(), trace.getDoubleGroup(3));
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(4)))
            trace.notYetImplemented();
    }

    private void corruptedConcurrentStartV2(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = new DateTimeStamp(trace.getGroup(1), trace.getDoubleGroup(2));
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(5)))
            trace.notYetImplemented();
    }

    private void corruptedConcurrentStartV3(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = new DateTimeStamp(trace.getDateStamp(), trace.getDoubleGroup(2));
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(6)))
            trace.notYetImplemented();

        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        gcCauseForwardReference = trace.gcCause(1);
        timeStampForwardReference = new DateTimeStamp(trace.getDateStamp(), trace.getDoubleGroup(2));
    }

    private void corruptedConcurrentStartV4(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = trace.getDateTimeStamp();
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(8)))
            trace.notYetImplemented();

        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        gcCauseForwardReference = trace.gcCause(1);
        timeStampForwardReference = trace.getDateTimeStamp();
    }

    private void corruptedConcurrentStartV5(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = new DateTimeStamp(trace.getDateStamp(), trace.getDoubleGroup(3));
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(8)))
            trace.notYetImplemented();

        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        gcCauseForwardReference = trace.gcCause(1);
        timeStampForwardReference = trace.getDateTimeStamp();
    }

    private void corruptedConcurrentStartV7(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = new DateTimeStamp(trace.getGroup(1), trace.getDoubleGroup(3));
        setGarbageCollectionTypeForwardReference(trace.getGroup(5));
        if (concurrentCollectionTypeForwardReference == null)
            trace.notYetImplemented();

        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        gcCauseForwardReference = trace.gcCause(4);
        timeStampForwardReference = concurrentPhaseStartTimeStamp;
    }

    private void corruptedConcurrentStartV8(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = new DateTimeStamp(trace.getGroup(1), trace.getDoubleGroup(2));
        setGarbageCollectionTypeForwardReference(trace.getGroup(5));
        if (concurrentCollectionTypeForwardReference == null)
            trace.notYetImplemented();

        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        gcCauseForwardReference = trace.gcCause(4);
        timeStampForwardReference = concurrentPhaseStartTimeStamp;
    }

    private void corruptedConcurrentStartV9(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = new DateTimeStamp(trace.getGroup(1), trace.getDoubleGroup(3));
        if (!setGarbageCollectionTypeForwardReference(trace.getGroup(6)))
            trace.notYetImplemented();

        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        gcCauseForwardReference = trace.gcCause(2);
        timeStampForwardReference = concurrentPhaseStartTimeStamp;
    }

    //113.325: [Full GC (System.gc()) 37M->32M(96M), 0.0943030 secs]
    private void g1gcFullGC(GCLogTrace trace, String line) {
        if (trace.gcCause() == GCCause.JAVA_LANG_SYSTEM)
            forwardReference = new G1SystemGC(trace.getDateTimeStamp(), trace.getPauseTime());
        else
            forwardReference = new G1FullGCNES(trace.getDateTimeStamp(), trace.gcCause(), trace.getPauseTime());
        forwardReference.addMemorySummary(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(4));
        // not thrilled about this "hack".... but currently no way to differentiate between a full with details and a full withoutgit c
        if ((diary != null) && (!diary.isPrintGCDetails()))
            record(forwardReference);
    }

    private boolean recordConcurrentPhase(String phaseName, double duration) {
        //Order sensitive operations. First completed the concurrent phase.
        switch (phaseName) {
            default:
                return false;
            case "root-region-scan":
                record(new ConcurrentScanRootRegion(concurrentPhaseStartTimeStamp, duration));
            case "mark":
                record(new G1ConcurrentMark(concurrentPhaseStartTimeStamp, duration));
            case "cleanup":
                record(new G1ConcurrentCleanup(concurrentPhaseStartTimeStamp, duration));
        }
        return true;
    }

    private void youngWithConcurrentEnd(GCLogTrace trace, String line) {
        if (!recordConcurrentPhase(trace.getGroup(8), trace.getDuration()))
            trace.notYetImplemented();
        //recording has cleaned up all the forward references
        gcCauseForwardReference = trace.gcCause(3, 0);
        timeStampForwardReference = getClock();
        if ("young".equals(trace.getGroup(4)))
            collectionTypeForwardReference = GarbageCollectionTypes.Young;
        else if ("mixed".equals(trace.getGroup(4)))
            collectionTypeForwardReference = GarbageCollectionTypes.Mixed;
        else
            trace.notYetImplemented();
    }

    private void youngSplitAtDatestamp(GCLogTrace trace, String line) {
        timeStampForwardReference = new DateTimeStamp(trace.getGroup(1), trace.getDoubleGroup(3));
    }

    private void youngSplitAtTimestamp(GCLogTrace trace, String line) {
        timeStampForwardReference = new DateTimeStamp(trace.getGroup(1));
        if (!recordConcurrentPhase(trace.getGroup(8), trace.getDuration()))
            trace.notYetImplemented();
    }

    private void freeFloatingYoungBlock(GCLogTrace trace, String line) {
        boolean initialMark = false;
        if (trace.getGroup(3) != null)
            initialMark = trace.contains(3, "initial-mark");
        if (trace.contains(2, "young"))
            if (initialMark)
                forwardReference = new G1YoungInitialMark(trace.getDateTimeStamp(), trace.gcCause(3, -2), trace.getPauseTime());
            else
                forwardReference = new G1Young(trace.getDateTimeStamp(), trace.gcCause(3, -2), trace.getPauseTime());
        else if (trace.getGroup(2).contains("mixed")) {
            forwardReference = new G1Mixed(trace.getDateTimeStamp(), trace.gcCause(3, -2), trace.getPauseTime());
        }
    }

    private void fullGCWithConcurrentPhase(GCLogTrace trace, String line) {
        timeStampForwardReference = getClock();
        gcCauseForwardReference = trace.gcCause();
        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        concurrentPhaseStartTimeStamp = trace.getDateTimeStamp(2);
    }

    private void g1YoungSplitEnd(GCLogTrace trace, String line) {
        if (collectionTypeForwardReference == GarbageCollectionTypes.Young) {
            forwardReference = new G1Young(timeStampForwardReference, gcCauseForwardReference, trace.getPauseTime());
        } else if (collectionTypeForwardReference == GarbageCollectionTypes.Mixed) {
            forwardReference = new G1Mixed(timeStampForwardReference, gcCauseForwardReference, trace.getPauseTime());
        } else if (collectionTypeForwardReference == GarbageCollectionTypes.G1GCYoungInitialMark) {
            forwardReference = new G1YoungInitialMark(timeStampForwardReference, gcCauseForwardReference, trace.getDoubleGroup(trace.groupCount()));
        } else {
            LOGGER.log(Level.WARNING, trace.toString());
            return;
        }

        if (referenceGCForwardReferenceSummary != null) {
            forwardReference.add(referenceGCForwardReferenceSummary);
        }

        if (trace.contains(1, "to-space")) {
            ((G1Young) forwardReference).toSpaceExhausted();
        }
    }

    private void g1InitialMark(GCLogTrace trace, String line) {
        timeStampForwardReference = getClock();
        gcCauseForwardReference = trace.gcCause();
        if ("young".equals(trace.getGroup(4)))
            collectionTypeForwardReference = GarbageCollectionTypes.G1GCYoungInitialMark;
        else if (trace.contains(4, "mixed"))
            collectionTypeForwardReference = GarbageCollectionTypes.G1GCMixedInitialMark;
        else
            trace.notYetImplemented();
    }

    //17M->3758K(13M), 0.0235300 secs]
    //todo: this corrects a corrupted Full GC (System.gc()) record but it also can be found with other
    //record interruptions
    private void freeFloatingOccupancySummary(GCLogTrace trace, String line) {
        if (collectionTypeForwardReference == GarbageCollectionTypes.Full) {
            forwardReference = new G1FullGCNES(timeStampForwardReference, gcCauseForwardReference, trace.getPauseTime());
            record(forwardReference);
            collectionTypeForwardReference = GarbageCollectionTypes.Unknown;
            timeStampForwardReference = null;
            gcCauseForwardReference = GCCause.UNKNOWN_GCCAUSE;
        } else if (collectionTypeForwardReference == GarbageCollectionTypes.SystemGC) {
            forwardReference = new G1SystemGC(timeStampForwardReference, trace.getPauseTime());
            collectionTypeForwardReference = GarbageCollectionTypes.Unknown;
            timeStampForwardReference = null;
            gcCauseForwardReference = GCCause.UNKNOWN_GCCAUSE;
        } else
            trace.notYetImplemented();
    }

    //549.246: [GC concurrent-root-region-scan-start]
    //549.246: [GC concurrent-mark-start]
    private void g1ConcurrentStartWithoutPrefix(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = getClock();
    }

    private void g1ConcurrentStart(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = trace.getDateTimeStamp();
    }

    private void concurrentStringDedup(GCLogTrace trace, String line) {
        double startingStringVolume = trace.toKBytes(trace.getDoubleGroup(4), trace.getGroup(5));
        double endingStringValue = trace.toKBytes(trace.getDoubleGroup(6), trace.getGroup(7));
        double reduction = trace.toKBytes(trace.getDoubleGroup(8), trace.getGroup(9));
        double percentReduction = trace.getDoubleGroup(10);
        record(new G1ConcurrentStringDeduplication(getClock(), trace.gcCause(), startingStringVolume, endingStringValue, reduction, percentReduction, trace.getDoubleGroup(trace.groupCount())));
    }

    //9.251: [GC remark, 0.0012190 secs]
    //6.298: [GC remark 6.298: [GC ref-proc, 0.0000570 secs], 0.0010940 secs]
    //2014-02-21T16:04:24.321-0100: 7.852: [GC remark 2014-02-21T16:04:24.322-0100: 7.853: [GC ref-proc, 0.0000640 secs], 0.0013310 secs]
    private void g1Remark(GCLogTrace trace, String line) {
        G1Remark remark = new G1Remark(trace.getDateTimeStamp(), (trace.getGroup(7) != null) ? trace.getDoubleGroup(7) : 0.0d, trace.getDoubleGroup(trace.groupCount()));
        record(remark);
    }

    //2015-04-09T14:28:44.235+0100: 6.597: [GC remark 6.597: [Finalize Marking, 0.0091510 secs] 6.606: [GC ref-proc, 0.0014102 secs] 6.608: [Unloading, 0.0044869 secs], 0.0153351 secs]
    //public G1Remark( DateTimeStamp timeStamp, double referenceProcessingTimes, double finalizeMarking, double unloading, double duration)
    private void g1180Remark(GCLogTrace trace, String line) {
        G1Remark remark = new G1Remark(trace.getDateTimeStamp(), trace.getDoubleGroup(8), trace.getDoubleGroup(5), trace.getDoubleGroup(11), trace.getDoubleGroup(trace.groupCount()));
        record(remark);
    }

    private void g1180RemarkRefDetails(GCLogTrace trace, String line) {
        G1Remark remark = new G1Remark(trace.getDateTimeStamp(), trace.getDoubleGroup(32), trace.getDoubleGroup(5), trace.getDoubleGroup(trace.groupCount() - 1), trace.getDuration());
        ReferenceGCSummary summary = extractPrintReferenceGC(line);
        remark.add(summary);
        record(remark);
    }

    //549.253: [GC cleanup 7826K->7826K(13M), 0.0004750 secs]
    //todo: capture memory summary
    private void g1Cleanup(GCLogTrace trace, String line) {
        G1Cleanup cleanup = new G1Cleanup(trace.getDateTimeStamp(), trace.getPauseTime());
        cleanup.addMemorySummary(getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(trace, 4));
        record(cleanup);
    }

    private void g1CleanupNoMemory(GCLogTrace trace, String line) {
        G1Cleanup cleanup = new G1Cleanup(trace.getDateTimeStamp(), trace.getPauseTime());
        record(cleanup);
    }

    private void splitCleanup(GCLogTrace trace, String line) {
        timeStampForwardReference = getClock();
        collectionTypeForwardReference = GarbageCollectionTypes.G1GCCleanup;
    }

    //511.628: [GC concurrent-mark-abort]
    private void g1ConcurrentAbort(GCLogTrace trace, String line) {
        if (concurrentPhaseStartTimeStamp != null) {
            G1ConcurrentMark concurrentMark = new G1ConcurrentMark(concurrentPhaseStartTimeStamp, trace.gcCause(), trace.getTimeStamp() - concurrentPhaseStartTimeStamp.getTimeStamp());
            concurrentMark.abort();
            record(concurrentMark);
        }
    }

    //604.395: [GC concurrent-mark-reset-for-overflow]
    private void concurrentMarkOverflow(GCLogTrace trace, String line) {
        record(new G1ConcurrentMarkResetForOverflow(getClock()));
    }

    private void g1CorruptedConcurrentEnd(GCLogTrace trace, String line) {
        //In this case a corruption stripped the timestamp away from the actual record
        //Fortunately it's a rare event.
        if (concurrentPhaseStartTimeStamp == null)
            concurrentPhaseStartTimeStamp = getClock();

        if ("root-region-scan".equals(trace.getGroup(1))) {
            record(new ConcurrentScanRootRegion(concurrentPhaseStartTimeStamp, GCCause.UNKNOWN_GCCAUSE, trace.getDoubleGroup(trace.groupCount())));
        } else if ("mark".equals(trace.getGroup(1))) {
            record(new G1ConcurrentMark(concurrentPhaseStartTimeStamp, GCCause.UNKNOWN_GCCAUSE, trace.getDoubleGroup(trace.groupCount())));
        } else if ("cleanup".equals(trace.getGroup(1))) {
            record(new G1ConcurrentCleanup(concurrentPhaseStartTimeStamp, GCCause.UNKNOWN_GCCAUSE, trace.getDoubleGroup(trace.groupCount())));
        } else
            trace.notYetImplemented();
    }

    public void g1ConcurrentEndCorruptedByApplicationTime(GCLogTrace trace, String line) {
        if (concurrentPhaseStartTimeStamp == null)
            concurrentPhaseStartTimeStamp = getClock();
        if ("root-region-scan".equals(trace.getGroup(4)))
            record(new ConcurrentScanRootRegion(concurrentPhaseStartTimeStamp, GCCause.UNKNOWN_GCCAUSE, trace.getPauseTime()));
        else if ("mark".equals(trace.getGroup(4)))
            record(new G1ConcurrentMark(concurrentPhaseStartTimeStamp, GCCause.UNKNOWN_GCCAUSE, trace.getPauseTime()));
        else if ("cleanup".equals(trace.getGroup(4)))
            record(new G1ConcurrentCleanup(concurrentPhaseStartTimeStamp, GCCause.UNKNOWN_GCCAUSE, trace.getPauseTime()));
    }

    public void g1FloatingConcurrentPhaseStart(GCLogTrace trace, String line) {
        concurrentPhaseStartTimeStamp = getClock();
    }

    /***********************************/
    /* Reference processing            */

    /***********************************/


    private void g1DetailsReferenceGC(GCLogTrace trace, String line) {
        processYoungGenCollection(trace, line);
        forwardReference.add(extractPrintReferenceGC(line));
        if (trace.contains(trace.groupCount() - 2, "to-space"))
            ((G1Young) forwardReference).toSpaceExhausted();
    }

    private void g1RemarkReferenceGC(GCLogTrace trace, String line) {
        ReferenceGCSummary summary = extractPrintReferenceGC(line);
        double totalReferenceProcessingTime = summary.getSoftReferencePauseTime() + summary.getWeakReferencePauseTime() + summary.getFinalReferencePauseTime() + summary.getPhantomReferencePauseTime() + summary.getJniWeakReferencePauseTime();
        G1Remark remark = new G1Remark(getClock(), totalReferenceProcessingTime, trace.getPauseTime());
        remark.add(summary);
        record(remark);
    }


    //11907.092: [Full GC11907.961: [SoftReference, 1403 refs, 0.0002110 secs]11907.962: [WeakReference, 31981 refs, 0.0026320 secs]11907.964: [FinalReference, 411 refs, 0.0000720 secs]11907.964: [PhantomReference, 491 refs, 0.0000400 secs]11907.964: [JNI Weak Reference, 0.0001960 secs] 11208M->1149M(12000M), 4.7476800 secs]
    private void g1FullDetailsReferenceGC(GCLogTrace trace, String line) {
        forwardReference = new G1FullGCNES(getClock(), trace.gcCause(), trace.getPauseTime());
        forwardReference.add(extractPrintReferenceGC(line));
        if ((diary != null) && (isPreJDK17040())) {
            MemoryPoolSummary heap = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(27);
            forwardReference.addMemorySummary(heap);
            record(forwardReference);
        }
    }


    //2014-10-21T11:47:46.879-0500: 11971.476: [GC pause (young)11972.105: [SoftReference, 0 refs, 0.0000050 secs]11972.105: [WeakReference, 1 refs, 0.0000020 secs]11972.105: [FinalReference, 94 refs, 0.0000360 secs]11972.105: [PhantomReference, 5 refs, 0.0000030 secs]11972.105: [JNI Weak Reference, 0.0002010 secs] (initial-mark), 0.63448400 secs]
    private void g1DetailsReferenceInitialMark(GCLogTrace trace, String line) {
        forwardReference = new G1YoungInitialMark(getClock(), trace.gcCause(), trace.getPauseTime());
        ReferenceGCSummary summary = extractPrintReferenceGC(line);
        forwardReference.add(summary);
        if (trace.contains(24, "to-space "))
            ((G1YoungInitialMark) forwardReference).toSpaceExhausted();
    }

    private void freeFloatingReferenceRecords(GCLogTrace trace, String line) {
        if (collectionTypeForwardReference == GarbageCollectionTypes.Young) {
            forwardReference = new G1Young(timeStampForwardReference, gcCauseForwardReference, trace.getPauseTime());
        } else if (collectionTypeForwardReference == GarbageCollectionTypes.Mixed) {
            forwardReference = new G1Mixed(timeStampForwardReference, gcCauseForwardReference, trace.getPauseTime());
        } else if (collectionTypeForwardReference == GarbageCollectionTypes.G1GCYoungInitialMark) {
            forwardReference = new G1YoungInitialMark(timeStampForwardReference, gcCauseForwardReference, trace.getPauseTime());
        }
        forwardReference.add(extractPrintReferenceGC(line));
        if (trace.contains("to-space")) { //todo: where this shows up in this record is current unknown.
            ((G1Young) forwardReference).toSpaceExhausted();
        }
    }

    private void floatingReferenceWithAdaptiveSizing(GCLogTrace trace, String line) {
        referenceGCForwardReferenceSummary = extractPrintReferenceGC(line);
    }

    //todo: phase durations are all wrong.... I don't have a good feeling about editing this method...
    private void youngReferenceWIthConcurrentEnd(GCLogTrace trace, String line) {
        //Order sensitive operations. First completed the concurrent phase.
        String concurrentPhase = trace.getGroup(32);
        if ("root-region-scan".equals(concurrentPhase)) {
            record(new ConcurrentScanRootRegion(concurrentPhaseStartTimeStamp, GCCause.UNKNOWN_GCCAUSE, trace.getPauseTime()));
        } else if ("mark".equals(concurrentPhase)) {
            record(new G1ConcurrentMark(concurrentPhaseStartTimeStamp, GCCause.UNKNOWN_GCCAUSE, trace.getPauseTime()));
        } else if ("cleanup".equals(concurrentPhase)) {
            record(new G1ConcurrentCleanup(concurrentPhaseStartTimeStamp, GCCause.UNKNOWN_GCCAUSE, trace.getPauseTime()));
        } else
            trace.notYetImplemented();

        //recording has cleaned up all the forward references but the object can be created here as the pause time is known.
        timeStampForwardReference = trace.getDateTimeStamp();
        gcCauseForwardReference = trace.gcCause();
        if ("young".equals(trace.getGroup(4)))
            collectionTypeForwardReference = GarbageCollectionTypes.Young;
        else if ("mixed".equals(trace.getGroup(4)))
            collectionTypeForwardReference = GarbageCollectionTypes.Mixed;
        else
            trace.notYetImplemented();
        referenceGCForwardReferenceSummary = extractPrintReferenceGC(line);
    }

    private void g1FullInterruptsConcurrentWithReferences(GCLogTrace trace, String line) {
        String concurrentPhase = trace.getGroup(30);  //25 was here before now with Atlassian it wants to be 30.
        if ("root-region-scan".equals(concurrentPhase)) {
            record(new ConcurrentScanRootRegion(concurrentPhaseStartTimeStamp, trace.gcCause(22), trace.getPauseTime()));
        } else if ("mark".equals(concurrentPhase)) {
            record(new G1ConcurrentMark(concurrentPhaseStartTimeStamp, trace.gcCause(22), trace.getPauseTime()));
        } else if ("cleanup".equals(concurrentPhase)) {
            record(new G1ConcurrentCleanup(concurrentPhaseStartTimeStamp, trace.gcCause(22), trace.getPauseTime()));
        } else
            trace.notYetImplemented();

        collectionTypeForwardReference = GarbageCollectionTypes.Full;
        gcCauseForwardReference = trace.gcCause();
        timeStampForwardReference = trace.getDateTimeStamp();
        referenceGCForwardReferenceSummary = extractPrintReferenceGC(line);
    }

    private void g1FullMemorySplitByConcurrent(GCLogTrace trace, String line) {
        if (collectionTypeForwardReference == GarbageCollectionTypes.Full) {
            G1FullGCNES full = new G1FullGCNES(timeStampForwardReference, gcCauseForwardReference, trace.getPauseTime());
            full.addMemorySummary(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1));
            record(full);
        } else
            trace.notYetImplemented();
    }

    /***********************************/
    /* Adaptive Sizing                 */

    /***********************************/

    private void youngSplitByG1Ergonomics(GCLogTrace trace, String line) {
        timeStampForwardReference = getClock();
        gcCauseForwardReference = trace.gcCause(3, 0);
        if ("young".equals(trace.getGroup(4)))
            collectionTypeForwardReference = GarbageCollectionTypes.Young;
        else if ("mixed".equals(trace.getGroup(4)))
            collectionTypeForwardReference = GarbageCollectionTypes.Mixed;
        else
            trace.notYetImplemented();
    }

    private void g1InitialMarkErgonomics(GCLogTrace trace, String line) {
        timeStampForwardReference = getClock();
        gcCauseForwardReference = trace.gcCause(3, 0);
        if ("young".equals(trace.getGroup(4)))
            collectionTypeForwardReference = GarbageCollectionTypes.G1GCYoungInitialMark;
        else if ("mixed".equals(trace.getGroup(4)))
            collectionTypeForwardReference = GarbageCollectionTypes.G1GCYoungInitialMark;
        else
            trace.notYetImplemented();
    }

    /***********************************/
    /* Heap Summary                    */

    /***********************************/

    //We have found this section. If PrintHeapAtGC does not interfere
    // this method should consume all remaining lines
    private void garbageFirstHeap(GCLogTrace trace, String line) {
        //Swallow log header that we don't do anything with
    }

    private void regionSize(GCLogTrace trace, String line) {
        regionSize = trace.getLongGroup(1);
    }

    private void metaspaceFinal(GCLogTrace trace, String line) {
        metaSpaceUsed = trace.getMemoryInKBytes(1);
        metaCapacity = trace.getMemoryInKBytes(3);
        metaCommitted = trace.getMemoryInKBytes(5);
        metaReserved = trace.getMemoryInKBytes(7);
    }

    private void classspaceFinal(GCLogTrace trace, String line) {
        classSpaceUsed = trace.getMemoryInKBytes(1);
        classSpaceCapacity = trace.getMemoryInKBytes(3);
        classSpaceCommitted = trace.getMemoryInKBytes(5);
        classSpaceReserved = trace.getMemoryInKBytes(7);
    }

    /***********************************/
    /* Pre 170_40 rules                */
    /* Very old and should not be used */

    /***********************************/

    private void g1Pre17040Summary(GCLogTrace trace, String line) {

        MemoryPoolSummary edenSummary = extractPoolSummary(trace, 1);
        MemoryPoolSummary heap = extractPoolSummary(trace, 13);
        SurvivorMemoryPoolSummary survivor = extractSurvivorPoolSummary(trace, 9);
        forwardReference.addMemorySummary(edenSummary, survivor, heap);
        record(forwardReference);
    }


    /*************************/
    /* Support Methods       */

    /*************************/

    private void g1ConcurrentEnd(GCLogTrace trace, String line) {
        //If concurrentPhaseStartTimeStamp is null, something went wrong in the parsing.
        // Use an estimate of the start time of the phase or suffer
        // a worse fate later on. Generally the cause is log file corruption.
        if (concurrentPhaseStartTimeStamp == null)
            concurrentPhaseStartTimeStamp = getClock();

        if ("root-region-scan".equals(trace.getGroup(4))) {
            record(new ConcurrentScanRootRegion(concurrentPhaseStartTimeStamp, trace.getDuration()));
        } else if ("mark".equals(trace.getGroup(4))) {
            record(new G1ConcurrentMark(concurrentPhaseStartTimeStamp, trace.getDuration()));
        } else if ("cleanup".equals(trace.getGroup(4))) {
            record(new G1ConcurrentCleanup(concurrentPhaseStartTimeStamp, trace.getDuration()));
        } else
            trace.notYetImplemented();
    }


    //Min: " + TIME + ", Avg: " + TIME + ", Max: " + TIME + ", Diff: " + TIME
    private StatisticalSummary extractStatisticalSummary(GCLogTrace trace, int offset) {
        return new StatisticalSummary(trace.getDoubleGroup(1 + offset),
                trace.getDoubleGroup(2 + offset),
                trace.getDoubleGroup(3 + offset),
                trace.getDoubleGroup(4 + offset),
                StatisticalSummary.UNDEFINED);
    }

    //Min: " + TIME + ", Avg: " + TIME + ", Max: " + TIME + ", Diff: " + TIME + ", Sum: " + REAL_NUMBER;
    private StatisticalSummary extractStatisticalSummaryWithSum(GCLogTrace trace, int offset) {
        return new StatisticalSummary(trace.getDoubleGroup(1 + offset),
                trace.getDoubleGroup(2 + offset),
                trace.getDoubleGroup(3 + offset),
                trace.getDoubleGroup(4 + offset),
                trace.getDoubleGroup(5 + offset));
    }

    private StatisticalSummary extractCounterSummary(GCLogTrace trace, int offset) {
        return new StatisticalSummary(trace.getIntegerGroup(offset),
                trace.getDoubleGroup(1 + offset),
                trace.getIntegerGroup(2 + offset),
                trace.getIntegerGroup(3 + offset),
                trace.getIntegerGroup(4 + offset));
    }

    private MemoryPoolSummary extractPoolSummary(GCLogTrace trace, int offset) {
        long occupancyBefore = Math.round(toKBytes(trace.getDoubleGroup(offset), trace.getGroup(1 + offset)));
        long sizeBefore = Math.round(toKBytes(trace.getDoubleGroup(2 + offset), trace.getGroup(3 + offset)));
        long occupancyAfter = Math.round(toKBytes(trace.getDoubleGroup(4 + offset), trace.getGroup(5 + offset)));
        long size = Math.round(toKBytes(trace.getDoubleGroup(6 + offset), trace.getGroup(7 + offset)));
        return new MemoryPoolSummary(occupancyBefore, sizeBefore, occupancyAfter, size);
    }

    private SurvivorMemoryPoolSummary extractSurvivorPoolSummary(GCLogTrace trace, int offset) {
        long occupancyBefore = Math.round(toKBytes(trace.getDoubleGroup(offset), trace.getGroup(1 + offset)));
        long occupancyAfter = Math.round(toKBytes(trace.getDoubleGroup(2 + offset), trace.getGroup(3 + offset)));
        return new SurvivorMemoryPoolSummary(occupancyBefore, occupancyAfter);
    }

    /**
     * Maintain time ordering of records. Concurrent phases are recorded at start time and thus
     * other phases may mix in.
     *
     * @throws InterruptedException
     */
    private void drainBacklog() throws InterruptedException {
        if (backlog.size() > 0)
            for (JVMEvent event : backlog)
                consumer.record(event);
    }

    public void record(G1GCConcurrentEvent concurrentEvent) {
        try {
            consumer.record(concurrentEvent);
            drainBacklog();
        } catch (InterruptedException e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }

    private void recordCPUSummary(GCLogTrace trace, String line) {
        forwardReference.addCPUSummary(new CPUSummary(trace.getDoubleGroup(1), trace.getDoubleGroup(2), trace.getDoubleGroup(3)));
        record(forwardReference);
    }

    public void record(G1GCPauseEvent collection) {

        if (collection == trap) {
            LOGGER.warning("Parsing Error: Attempt to record Trap @" + getClock().getTimeStamp());
            return;
        }

        // wait for the CPU summary
        if ((collection.getCpuSummary() == null) && (diary == null || diary.isPrintGCDetails())) {
            forwardReference = collection;
        } else {
            consumer.record(collection);
            forwardReference = trap;
            collectionTypeForwardReference = null;
            referenceGCForwardReferenceSummary = null;
        }
    }

    private void log(String line) {

        if (line.startsWith("Java HotSpot(TM)")) return;
        if (line.startsWith("Memory: ")) return;
        if (line.startsWith("CommandLine flags: ")) return;

        if (debugging)
            LOGGER.fine("Missed: " + line);

        LOGGER.log(Level.FINE, "Missed: {0}", line);

    }
}
