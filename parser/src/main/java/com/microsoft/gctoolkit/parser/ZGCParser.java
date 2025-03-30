// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;
import com.microsoft.gctoolkit.GCToolKit;

import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GCEvent;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.event.zgc.ZGCPageAgeSummary;
import com.microsoft.gctoolkit.event.zgc.MinorZGCCycle;
import com.microsoft.gctoolkit.event.zgc.ZGCAllocatedSummary;
import com.microsoft.gctoolkit.event.zgc.FullZGCCycle;
import com.microsoft.gctoolkit.event.zgc.ZGCGarbageSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCHeapCapacitySummary;
import com.microsoft.gctoolkit.event.zgc.ZGCLiveSummary;
import com.microsoft.gctoolkit.event.zgc.MajorZGCCycle;
import com.microsoft.gctoolkit.event.zgc.OccupancySummary;
import com.microsoft.gctoolkit.event.zgc.ZGCCompactedSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCNMethodSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCPageSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCPhase;
import com.microsoft.gctoolkit.event.zgc.ZGCPromotedSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCReclaimSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCCollectionType;
import com.microsoft.gctoolkit.event.zgc.ZGCCycle;
import com.microsoft.gctoolkit.event.zgc.ZGCMarkSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMemoryPoolSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMetaspaceSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMemorySummary;
import com.microsoft.gctoolkit.event.zgc.ZGCReferenceSummary;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.parser.collection.MRUQueue;
import com.microsoft.gctoolkit.parser.unified.ZGCPatterns;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class ZGCParser extends UnifiedGCLogParser implements ZGCPatterns {

    private static final Logger LOGGER = Logger.getLogger(ZGCParser.class.getName());

    private final boolean debugging = Boolean.getBoolean("microsoft.debug");
    private final boolean develop = Boolean.getBoolean("microsoft.develop");

    private ZForwardReference forwardReference;

    private final long[] markStart = new long[3];
    private final long[] markEnd = new long[3];
    private final long[] relocateStart = new long[3];
    private final long[] relocateEnd = new long[3];
    private final long[] heapCapacity = new long[3];

    private final MRUQueue<GCParseRule, BiConsumer<GCLogTrace, String>> parseRules;
    private boolean oldGenHeapStats = false;
    private boolean youngGenHeapStats = false;

    //Implement all capture methods
    {
        parseRules = new MRUQueue<>();
        parseRules.put(CYCLE_START, this::cycleStart);
        parseRules.put(PAUSE_PHASE, this::pausePhase);
        parseRules.put(CONCURRENT_PHASE, this::concurrentPhase);
        parseRules.put(LOAD, this::load);
        parseRules.put(MMU, this::mmu);
        parseRules.put(MARK_SUMMARY, this::markSummary);
        parseRules.put(RELOCATION_SUMMARY, this::relocationSummary);
        parseRules.put(NMETHODS, this::nMethods);
        parseRules.put(METASPACE, this::metaspace);
        parseRules.put(REFERENCE_PROCESSING, this::referenceProcessing);
        parseRules.put(CAPACITY, this::capacity);
        parseRules.put(MEMORY_TABLE_ENTRY_SIZE, this::sizeEntry);
        parseRules.put(MEMORY_TABLE_ENTRY_OCCUPANCY, this::occupancyEntry);
        parseRules.put(MEMORY_SUMMARY, this::memorySummary);
        parseRules.put(END_OF_FILE, this::endOfFile);
        parseRules.put(MEMORY_TABLE_ENTRY_RECLAIMED_PROMOTED, this::reclaimedPromoted);
        parseRules.put(MEMORY_TABLE_ENTRY_COMPACTED, this::compacted);

        // Generation ("gen") ZGC only options
        parseRules.put(LOAD_GEN, this::loadGen);
        parseRules.put(REFERENCE_PROCESSING_GEN, this::referenceProcessingGen);
        parseRules.put(END_OF_PHASE_SUMMARY_GEN, this::endOfPhaseMemorySummary);
        parseRules.put(PAGES_GEN, this::pageSummary);
        parseRules.put(FORWARDING_USAGE_GEN, this::forwardingUsage);
        parseRules.put(AGE_TABLE_GEN, this::ageTable);

        // Phase change rules
        parseRules.put(MARK_OLD_GEN_HEAP_STATS, this::markOldGenHeapStats);
        parseRules.put(MARK_YOUNG_GEN_HEAP_STATS, this::markYoungGenHeapStats);
    }

    public ZGCParser() {}

    @Override
    public Set<EventSource> eventsProduced() {
        return Set.of(EventSource.ZGC);
    }

    /**
     * This marks the phase we're in for memory stats. Generation ZGC will provide heap capacity
     * as well as old and young gen capacities. This enables the Young gen phase
     */
    private void markYoungGenHeapStats(GCLogTrace gcLogTrace, String s) {
        this.youngGenHeapStats = true;
        this.oldGenHeapStats = false;
    }

    /**
     * This marks the phase we're in for memory stats. Generation ZGC will provide heap capacity
     * as well as old and young gen capacities. This enables the Old gen phase
     */
    private void markOldGenHeapStats(GCLogTrace gcLogTrace, String s) {
        this.youngGenHeapStats = false;
        this.oldGenHeapStats = true;
    }


    @Override
    public String getName() {
        return "ZGC Parser";
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
            if (optional.isPresent()) {
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
        return MEMORY_TABLE_HEADER.parse(line) != null;
    }

    public void endOfFile(GCLogTrace trace, String line) {
        publish(new JVMTermination(getClock(), diary.getTimeOfFirstEvent()));
    }

    private ZGCForwardReference getForwardRefForPhase(ZGCPhase zgcPhase) {
        switch (zgcPhase) {
            // No phase information, legacy ZGC
            case FULL:
                return (ZGCForwardReference) forwardReference;
            case MAJOR_YOUNG:
                return ((ZGCMajorForwardReference)forwardReference).getYoungGeneration();
            case MAJOR_OLD:
                return ((ZGCMajorForwardReference)forwardReference).getOldGeneration();
            case MINOR_YOUNG:
                return ((ZGCMinorForwardReference)forwardReference).getYoungGeneration();
        }

        // Should never reach (should convert to switch expression)
        throw new RuntimeException("Unknown phase " + zgcPhase);
    }

    private void cycleStart(GCLogTrace trace, String s) {
        ZGCCollectionType type = ZGCCollectionType.get(trace.getGroup(2));

        switch (type) {
            case FULL:
                // FULL, Legacy ZGC
                forwardReference = new ZGCForwardReference(getClock(), trace.getLongGroup(1), trace.gcCause(3,0), type, ZGCPhase.FULL);
                break;
            case MINOR:
                // MINOR, young only collection
                forwardReference = new ZGCMinorForwardReference(getClock(), trace.getLongGroup(1), trace.gcCause(3,0), type);
                break;
            case MAJOR:
                // MAJOR, young and old gen collection
                forwardReference = new ZGCMajorForwardReference(getClock(), trace.getLongGroup(1), trace.gcCause(3,0), type);
                break;
        }
    }

    private void pausePhase(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        DateTimeStamp startTime = getClock().minus(trace.getDuration() / 1000.00d);
        if ("Mark Start".equals(trace.getGroup(2))) {
            ref.setPauseMarkStartDuration(trace.getDuration());
            ref.setPauseMarkStart(startTime);
        } if ("Mark Start (Major)".equals(trace.getGroup(2))) {
            ref.setPauseMarkStartDuration(trace.getDuration());
            ref.setPauseMarkStart(startTime);
        } else if ("Mark End".equals(trace.getGroup(2))) {
            ref.setPauseMarkEndDuration(trace.getDuration());
            ref.setPauseMarkEndStart(startTime);
        } else if ("Relocate Start".equals(trace.getGroup(2))) {
            ref.setPauseRelocateStartDuration(trace.getDuration());
            ref.setPauseRelocateStart(startTime);
        } else
            trace.notYetImplemented();
    }

    private void concurrentPhase(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        DateTimeStamp startTime = getClock().minus(trace.getDuration() / 1000.0d);
        if ("Mark".equals(trace.getGroup(2))) {
            ref.setConcurrentMarkDuration(trace.getDuration());
            ref.setConcurrentMarkStart(startTime);
        } else if ("Mark Continue".equals(trace.getGroup(2))) {
            ref.setConcurrentMarkContinueDuration(trace.getDuration());
            ref.setConcurrentMarkContinueStart(startTime);
        } else if ("Mark Free".equals(trace.getGroup(2))) {
            ref.setConcurrentMarkFreeDuration(trace.getDuration());
            ref.setConcurrentMarkFreeStart(startTime);
        } else if ("Process Non-Strong References".equals(trace.getGroup(2)) || "Process Non-Strong".equals(trace.getGroup(2)) ) {
            ref.setConcurrentProcessNonStrongReferencesDuration(trace.getDuration());
            ref.setConcurrentProcessNonStringReferencesStart(startTime);
        } else if ("Reset Relocation Set".equals(trace.getGroup(2))) {
            ref.setConcurrentResetRelocationSetDuration(trace.getDuration());
            ref.setConcurrentResetRelocationSetStart(startTime);
        } else if ("Select Relocation Set".equals(trace.getGroup(2))) {
            ref.setConcurrentSelectRelocationSetDuration(trace.getDuration());
            ref.setConcurrentSelectRelocationSetStart(startTime);
        } else if ("Relocate".equals(trace.getGroup(2))) {
            ref.setConcurrentSelectRelocateStart(startTime);
            ref.setConcurrentSelectRelocateDuration(trace.getDuration());
        } else if ("Remap Roots".equals(trace.getGroup(2))) {
            ref.setConcurrentRemapRootsStart(startTime);
            ref.setConcurrentRemapRootsDuration(trace.getDuration());
        } else if ("Mark Roots".equals(trace.getGroup(2))) {
            ref.setMarkRootsStart(startTime);
            ref.setMarkRootsDuration(trace.getDuration());
        } else if ("Mark Follow".equals(trace.getGroup(2))) {
            ref.setMarkFollowStart(startTime);
            ref.setMarkFollowDuration(trace.getDuration());
        } else if ("Remap Roots Colored".equals(trace.getGroup(2))) {
            ref.setRemapRootsColoredStart(startTime);
            ref.setRemapRootsColoredDuration(trace.getDuration());
        } else if ("Remap Roots Uncolored".equals(trace.getGroup(2))) {
            ref.setRemapRootsUncoloredStart(startTime);
            ref.setRemapRootsUncoloredDuration(trace.getDuration());
        } else if ("Remap Remembered".equals(trace.getGroup(2))) {
            ref.setRemapRememberedStart(startTime);
            ref.setRemapRememberedDuration(trace.getDuration());
        }
        else
            trace.notYetImplemented();
    }


    private void pageSummary(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        ZGCPageSummary summary = new ZGCPageSummary(
                trace.getLongGroup(3),
                trace.getLongGroup(4),
                trace.getLongGroup(5),
                trace.toKBytes(6),
                trace.toKBytes(8),
                trace.toKBytes(10));
        if ("Small".equals(trace.getGroup(2))){
            ref.setSmallPageSummary(summary);
        } else if ("Medium".equals(trace.getGroup(2))) {
            ref.setMediumPageSummary(summary);
        } else if ("Large".equals(trace.getGroup(2))) {
            ref.setLargePageSummary(summary);
        }
    }

    private void forwardingUsage(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        ref.setForwardingUsage(trace.toKBytes(2));
    }

    private void ageTable(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        ZGCPageAgeSummary summary = new ZGCPageAgeSummary(
                trace.getGroup(2),
                trace.toKBytes(3),
                trace.getIntegerGroup(5),
                trace.toKBytes(6),
                trace.getIntegerGroup(8),
                trace.getLongGroup(9),
                trace.getLongGroup(10),
                trace.getLongGroup(11),
                trace.getLongGroup(12),
                trace.getLongGroup(13),
                trace.getLongGroup(14));
        ref.addPageAgeSummary(summary);
    }

    private void load(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        double[] load = new double[3];
        load[0] = trace.getDoubleGroup(2);
        load[1] = trace.getDoubleGroup(3);
        load[2] = trace.getDoubleGroup(4);
        ref.setLoad(load);
    }

    private void loadGen(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        double[] load = new double[3];
        load[0] = trace.getDoubleGroup(2);
        load[1] = trace.getDoubleGroup(3);
        load[2] = trace.getDoubleGroup(4);
        ref.setLoad(load);
    }

    private void mmu(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        double[] mmu = new double[6];
        mmu[0] = trace.getDoubleGroup(2);
        mmu[1] = trace.getDoubleGroup(3);
        mmu[2] = trace.getDoubleGroup(4);
        mmu[3] = trace.getDoubleGroup(5);
        mmu[4] = trace.getDoubleGroup(6);
        mmu[5] = trace.getDoubleGroup(7);
        ref.setMMU(mmu);
    }

    private void markSummary(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        ref.setMarkSummary(new ZGCMarkSummary(
                trace.getIntegerGroup(2),
                trace.getIntegerGroup(3),
                trace.getIntegerGroup(4),
                trace.getIntegerGroup(5),
                trace.getIntegerGroup(6)));
    }

    private void relocationSummary(GCLogTrace trace, String s) {
        //trace.notYetImplemented();
    }

    private void nMethods(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        ZGCNMethodSummary summary = new ZGCNMethodSummary(
                trace.getLongGroup(2),
                trace.getLongGroup(3));
        ref.setNMethodSummary(summary);
    }

    private void metaspace(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        ZGCMetaspaceSummary summary = new ZGCMetaspaceSummary(
                trace.toKBytes(2),
                trace.toKBytes(4),
                trace.toKBytes(6));
        ref.setMetaspaceSummary(summary);
    }

    private void referenceProcessing(GCLogTrace trace, String s) {
        //trace.notYetImplemented();
    }

    private void referenceProcessingGen(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        ZGCReferenceSummary summary = new ZGCReferenceSummary(trace.getLongGroup(3), trace.getLongGroup(4), trace.getLongGroup(5));
        if ("Soft".equals(trace.getGroup(2))){
            ref.setSoftRefSummary(summary);
        } else if ("Weak".equals(trace.getGroup(2))) {
            ref.setWeakRefSummary(summary);
        } else if ("Final".equals(trace.getGroup(2))) {
            ref.setFinalRefSummary(summary);
        } else if ("Phantom".equals(trace.getGroup(2))) {
            ref.setPhantomRefSummary(summary);
        }
    }

    private void capacity(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        if ("Min Capacity".equals(trace.getGroup(2))){
            heapCapacity[0] = trace.toKBytes(3);
        } else if ("Max Capacity".equals(trace.getGroup(2))) {
            heapCapacity[1] = trace.toKBytes(3);
        } else if ("Soft Max Capacity".equals(trace.getGroup(2))) {
            heapCapacity[2] = trace.toKBytes(3);
            ref.setHeapCapacitySummary(new ZGCHeapCapacitySummary(heapCapacity[0], heapCapacity[1], heapCapacity[2]));
        }
    }

    private void captureAtIndex(GCLogTrace trace, int index) {
        // temporary holder of marking info to get set on fwdref
        markStart[index] = trace.toKBytes(3);
        markEnd[index] = trace.toKBytes(6);
        relocateStart[index] = trace.toKBytes(9);
        relocateEnd[index] = trace.toKBytes(12);
    }

    private void sizeEntry(GCLogTrace trace, String s) {
        ZGCPhase phase = trace.getZCollectionPhase();
        ZGCForwardReference ref = getForwardRefForPhase(phase);

        if (oldGenHeapStats || youngGenHeapStats) {
            if ("Used".equals(trace.getGroup(2))){
                OccupancySummary summary = new OccupancySummary(
                        trace.toKBytes(3),
                        trace.toKBytes(6),
                        trace.toKBytes(9),
                        trace.toKBytes(12));
                ref.setUsed(phase, summary);
            } else {
                trace.notYetImplemented();
            }
            return;
        }

        switch (trace.getGroup(2)) {
            case "Capacity":
                captureAtIndex(trace, 0);
                break;
            case "Free":
                captureAtIndex(trace, 1);
                break;
            case "Used":
                ref.setMarkStart(new ZGCMemoryPoolSummary(markStart[0], markStart[1], trace.toKBytes(3)));
                ref.setMarkEnd(new ZGCMemoryPoolSummary(markEnd[0], markEnd[1], trace.toKBytes(6)));
                ref.setRelocateStart(new ZGCMemoryPoolSummary(relocateStart[0], relocateStart[1], trace.toKBytes(9)));
                ref.setRelocateEnd(new ZGCMemoryPoolSummary(relocateEnd[0], relocateEnd[1], trace.toKBytes(12)));
                break;
            default:
                LOGGER.warning(trace.getGroup(2) + "not recognized, Heap Occupancy/size is is ignored. Please report this with the GC log");
        }
    }

    private void occupancyEntry(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        if ("Live".equals(trace.getGroup(2))) {
            ref.setMarkedLiveSummary(new ZGCLiveSummary(
                    trace.toKBytes(3),
                    trace.toKBytes(6),
                    trace.toKBytes(9)));
        } else if ("Allocated".equals(trace.getGroup(2))) {
            ref.setAllocatedSummary(new ZGCAllocatedSummary(
                    trace.toKBytes(3),
                    trace.toKBytes(6),
                    trace.toKBytes(9)));
        } else if ("Garbage".equals(trace.getGroup(2))) {
            ref.setGarbageSummary(new ZGCGarbageSummary(
                    trace.toKBytes(3),
                    trace.toKBytes(6),
                    trace.toKBytes(9)));
        } else
            trace.notYetImplemented();
    }

    private void reclaimedPromoted(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        if ("Reclaimed".equals(trace.getGroup(2))) {
            ref.setReclaimSummary(
                    new ZGCReclaimSummary(
                            trace.toKBytes(3),
                            trace.toKBytes(6)
                    )
            );
        } else if ("Promoted".equals(trace.getGroup(2))) {
            ref.setPromotedSummary(
                    new ZGCPromotedSummary(
                            trace.toKBytes(3),
                            trace.toKBytes(6)
                    )
            );
        }
    }

    private void compacted(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        // Exit both young and old gen stats, reset for next cycle
        this.oldGenHeapStats = false;
        this.youngGenHeapStats = false;
        ref.setCompactedSummary(
                new ZGCCompactedSummary(
                        trace.toKBytes(2)
                )
        );
    }

    private void endOfPhaseMemorySummary(GCLogTrace trace, String s) {
        ZGCForwardReference ref = getForwardRefForPhase(trace.getZCollectionPhase());

        ref.setMemorySummary(
                new ZGCMemorySummary(
                        trace.toKBytes(3),
                        trace.toKBytes(6)));

        if (trace.getGroup(9) != null) {
            ref.setGcDuration(trace.getSeconds(9));
        }
    }

    private void memorySummary(GCLogTrace trace, String s) {
        ZGCCollectionType type = ZGCCollectionType.get(trace.getGroup(1));
        switch (type) {
            case FULL:
                ((ZGCForwardReference)forwardReference).setMemorySummary(
                    new ZGCMemorySummary(
                            trace.toKBytes(3),
                            trace.toKBytes(6)));
                break;
            case MINOR:
                ((ZGCMinorForwardReference)forwardReference).setMemorySummary(
                        new ZGCMemorySummary(
                                trace.toKBytes(3),
                                trace.toKBytes(6)));
                break;
            case MAJOR:
                ((ZGCMajorForwardReference)forwardReference).setMemorySummary(
                        new ZGCMemorySummary(
                                trace.toKBytes(3),
                                trace.toKBytes(6)));
                break;
        }

        // Clean up / reset temp variables
        Arrays.fill(markStart, 0L);
        Arrays.fill(markEnd, 0L);
        Arrays.fill(relocateStart, 0L);
        Arrays.fill(relocateEnd, 0L);
        Arrays.fill(heapCapacity, 0L);
        oldGenHeapStats = false;
        youngGenHeapStats = false;

        // TODO: Consider using logging cycle duration instead of timestamps
        publish();
    }


    private void log(String line) {
        GCToolKit.LOG_DEBUG_MESSAGE(() -> "ZGCHeapParser missed: " + line);

        LOGGER.log(Level.WARNING, "Missed: {0}", line);
    }

    public void logMissedFirstRecordForEvent(String line) {
        LOGGER.log(Level.WARNING, "Missing initial record for: {0}", line);
    }

    public void publish() {
        publish(forwardReference.getGCEVent(getClock()));
    }

    public void publish(JVMEvent event) {
        super.publish(ChannelName.ZGC_PARSER_OUTBOX, event);
        forwardReference = null;
    }

    private interface ZForwardReference {
        GCEvent getGCEVent(DateTimeStamp endTime);
    }

    private static class ZGCMinorForwardReference implements ZForwardReference {
        private final DateTimeStamp startTimeStamp;
        private final GCCause gcCause;
        private final ZGCCollectionType type;
        private final long gcId;
        private ZGCMemorySummary cycleMemorySummary;

        // Holds single reference to minor collection stats
        private ZGCForwardReference youngGeneration;

        public ZGCMinorForwardReference(DateTimeStamp dateTimeStamp, long gcId, GCCause cause, ZGCCollectionType type) {
            this.startTimeStamp = dateTimeStamp;
            this.gcCause = cause;
            this.type = type;
            this.gcId = gcId;

            youngGeneration = new ZGCForwardReference(dateTimeStamp, gcId, cause, type, ZGCPhase.MINOR_YOUNG);
        }

        public ZGCForwardReference getYoungGeneration() {
            return youngGeneration;
        }

        @Override
        public GCEvent getGCEVent(DateTimeStamp endTime) {
            GarbageCollectionTypes gcType = GarbageCollectionTypes.Unknown;
            switch (type) {
                case FULL:
                    gcType = GarbageCollectionTypes.ZGCFull;
                    break;
                case MINOR:
                    gcType = GarbageCollectionTypes.ZGCMinor;
                    break;
                case MAJOR:
                    gcType = GarbageCollectionTypes.ZGCMajor;
                    break;
            }

            MinorZGCCycle cycle = new MinorZGCCycle(startTimeStamp, gcType, gcCause, endTime.minus(startTimeStamp));
            cycle.setYoungCycle(youngGeneration.getZGCCycle(endTime));
            cycle.setMemorySummary(cycleMemorySummary);
            cycle.setGcId(gcId);

            return cycle;
        }

        public void setMemorySummary(ZGCMemorySummary cycleMemorySummary) {
            this.cycleMemorySummary = cycleMemorySummary;
        }
    }

    private static class ZGCMajorForwardReference implements ZForwardReference {
        private final DateTimeStamp startTimeStamp;
        private final GCCause gcCause;
        private final ZGCCollectionType type;
        private final long gcId;
        private ZGCMemorySummary cycleMemorySummary;

        // Holds two references to minor and major collection stats
        private final ZGCForwardReference youngGeneration;
        private final ZGCForwardReference oldGeneration;

        public ZGCMajorForwardReference(DateTimeStamp dateTimeStamp, long gcId, GCCause cause, ZGCCollectionType type) {
            this.startTimeStamp = dateTimeStamp;
            this.gcCause = cause;
            this.type = type;
            this.gcId = gcId;

            youngGeneration = new ZGCForwardReference(dateTimeStamp, gcId, cause, type, ZGCPhase.MAJOR_YOUNG);
            oldGeneration = new ZGCForwardReference(dateTimeStamp, gcId, cause, type, ZGCPhase.MAJOR_OLD);
        }

        public ZGCForwardReference getYoungGeneration() {
            return youngGeneration;
        }

        public ZGCForwardReference getOldGeneration() {
            return oldGeneration;
        }

        public void setMemorySummary(ZGCMemorySummary cycleMemorySummary) {
            this.cycleMemorySummary = cycleMemorySummary;
        }

        @Override
        public GCEvent getGCEVent(DateTimeStamp endTime) {
            GarbageCollectionTypes gcType = GarbageCollectionTypes.Unknown;
            switch (type) {
                case FULL:
                    gcType = GarbageCollectionTypes.ZGCFull;
                    break;
                case MINOR:
                    gcType = GarbageCollectionTypes.ZGCMinor;
                    break;
                case MAJOR:
                    gcType = GarbageCollectionTypes.ZGCMajor;
                    break;
            }

            MajorZGCCycle cycle = new MajorZGCCycle(startTimeStamp, gcType, gcCause, endTime.minus(startTimeStamp));
            cycle.setYoungCycle(youngGeneration.getZGCCycle(endTime));
            cycle.setOldCycle(oldGeneration.getZGCCycle(endTime));
            cycle.setMemorySummary(cycleMemorySummary);
            cycle.setGcId(gcId);

            return cycle;
        }
    }

    private static class ZGCForwardReference implements ZForwardReference {
        private final DateTimeStamp startTimeStamp;
        private final GCCause gcCause;
        private final ZGCCollectionType type;
        private final ZGCPhase phase;
        private final long gcId;

        // Timing
        private DateTimeStamp pauseMarkStart;
        private double pauseMarkStartDuration;
        private DateTimeStamp pauseMarkEndStart;
        private double pauseMarkEndDuration;
        private DateTimeStamp pauseRelocateStart;
        private double pauseRelocateStartDuration;
        private DateTimeStamp concurrentMarkStart;
        private double concurrentMarkDuration;
        private double concurrentMarkFreeDuration;
        private DateTimeStamp concurrentMarkFreeStart;

        private DateTimeStamp concurrentProcessNonStringReferencesStart;
        private double concurrentProcessNonStrongReferencesDuration;
        private DateTimeStamp concurrentResetRelocationSetStart;
        private double concurrentResetRelocationSetDuration;
        private DateTimeStamp concurrentSelectRelocationSetStart;
        private double concurrentSelectRelocationSetDuration;
        private DateTimeStamp concurrentSelectRelocateStart;
        private double concurrentSelectRelocateDuration;
        private DateTimeStamp concurrentMarkContinueStart;
        private double concurrentMarkContinueDuration;


        // Memory
        private ZGCHeapCapacitySummary heapCapacitySummary;
        private ZGCMemoryPoolSummary markStart;
        private ZGCMemoryPoolSummary markEnd;
        private ZGCMemoryPoolSummary relocatedStart;
        private ZGCMemoryPoolSummary relocateEnd;
        private ZGCLiveSummary liveSummary;
        private ZGCAllocatedSummary allocatedSummary;
        private ZGCGarbageSummary garbageSummary;
        private ZGCReclaimSummary reclaimSummary;
        private ZGCMemorySummary memorySummary;
        private ZGCMetaspaceSummary metaspaceSummary;
        private ZGCMarkSummary markSummary;

        //Load
        private double[] load = new double[3];
        private double[] mmu = new double[6];
        private DateTimeStamp concurrentRemapRootsStart;
        private double concurrentRemapRootsDuration;
        private DateTimeStamp markRootsStart;
        private double markRootsDuration;
        private DateTimeStamp markFollowStart;
        private double markFollowDuration;
        private DateTimeStamp remapRootColoredStart;
        private double remapRootsColoredDuration;
        private DateTimeStamp remapRootsUncoloredStart;
        private double remapRootsUncoloredDuration;
        private DateTimeStamp remapRememberedStart;
        private double remapRememberedDuration;
        private ZGCPromotedSummary promotedSummary;
        private ZGCCompactedSummary compactedSummary;
        private Double gcDuration;
        private OccupancySummary usedOccupancySummary;
        private ZGCReferenceSummary softRefSummary;
        private ZGCReferenceSummary weakRefSummary;
        private ZGCReferenceSummary finalRefSummary;
        private ZGCReferenceSummary phantomRefSummary;
        private ZGCNMethodSummary nMethodSummary;
        private ZGCPageSummary smallPageSummary;
        private ZGCPageSummary mediumPageSummary;
        private ZGCPageSummary largePageSummary;
        private long forwardingUsage;
        private List<ZGCPageAgeSummary> ageTableSummary;

        public ZGCForwardReference(DateTimeStamp dateTimeStamp, long gcId, GCCause cause, ZGCCollectionType type, ZGCPhase phase) {
            this.startTimeStamp = dateTimeStamp;
            this.gcId = gcId;
            this.gcCause = cause;
            this.type = type;
            this.phase = phase;
        }

        @Override
        public GCEvent getGCEVent(DateTimeStamp endTime) {
            GarbageCollectionTypes gcType = GarbageCollectionTypes.Unknown;
            switch (type) {
                case FULL:
                    gcType = GarbageCollectionTypes.ZGCFull;
                    break;
                case MINOR:
                    gcType = GarbageCollectionTypes.ZGCMinor;
                    break;
                case MAJOR:
                    gcType = GarbageCollectionTypes.ZGCMajor;
                    break;
            }

            // Duration recorded by GC
            FullZGCCycle fullZGCCycle;
            ZGCCycle delegate = getZGCCycle(endTime);
            if (gcDuration != null){
                fullZGCCycle = new FullZGCCycle(startTimeStamp, gcType, gcCause, gcDuration, delegate);
            } else {
                fullZGCCycle = new FullZGCCycle(startTimeStamp, gcType, gcCause, endTime.minus(startTimeStamp), delegate);
            }

            return fullZGCCycle;
        }

        public ZGCCycle getZGCCycle(DateTimeStamp endTime) {
            ZGCCycle cycle = new ZGCCycle();
            cycle.setGcId(gcId);
            cycle.setType(type);
            cycle.setPhase(phase);
            cycle.setPauseMarkStart(pauseMarkStart, pauseMarkStartDuration);
            cycle.setConcurrentMark(concurrentMarkStart, concurrentMarkDuration);
            cycle.setConcurrentMarkContinue(concurrentMarkContinueStart, concurrentMarkContinueDuration);
            cycle.setConcurrentMarkFree(concurrentMarkFreeStart, concurrentMarkFreeDuration);
            cycle.setPauseMarkEnd(pauseMarkEndStart, pauseMarkEndDuration);
            cycle.setMarkRoots(markRootsStart, markRootsDuration);
            cycle.setMarkFollow(markFollowStart, markFollowDuration);
            cycle.setRemapRootsColored(remapRootColoredStart, remapRootsColoredDuration);
            cycle.setRemapRootsUncolored(remapRootsUncoloredStart, remapRootsUncoloredDuration);
            cycle.setRemapRemembered(remapRememberedStart, remapRememberedDuration);
            cycle.setConcurrentProcessNonStrongReferences(concurrentProcessNonStringReferencesStart, concurrentProcessNonStrongReferencesDuration);
            cycle.setConcurrentResetRelocationSet(concurrentResetRelocationSetStart, concurrentResetRelocationSetDuration);
            cycle.setConcurrentSelectRelocationSet(concurrentSelectRelocationSetStart, concurrentSelectRelocationSetDuration);
            cycle.setPauseRelocateStart(pauseRelocateStart, pauseRelocateStartDuration);
            cycle.setConcurrentRelocate(concurrentSelectRelocateStart, concurrentSelectRelocateDuration);
            cycle.setConcurrentRemapRoots(concurrentRemapRootsStart, concurrentRemapRootsDuration);
            cycle.setPromotedSummary(promotedSummary);
            cycle.setCompactedSummary(compactedSummary);
            cycle.setusedOccupancySummary(usedOccupancySummary);
            cycle.setSoftRefSummary(softRefSummary);
            cycle.setWeakRefSummary(weakRefSummary);
            cycle.setFinalRefSummary(finalRefSummary);
            cycle.setPhantomRefSummary(phantomRefSummary);

            //Memory
            cycle.setHeapCapacitySummary(heapCapacitySummary);
            cycle.setMarkStart(markStart);
            cycle.setMarkEnd(markEnd);
            cycle.setRelocateStart(relocatedStart);
            cycle.setRelocateEnd(relocateEnd);
            cycle.setLiveSummary(liveSummary);
            cycle.setAllocatedSummary(allocatedSummary);
            cycle.setGarbageSummary(garbageSummary);
            cycle.setReclaimSummary(reclaimSummary);
            cycle.setMemorySummary(memorySummary);
            cycle.setMetaspaceSummary(metaspaceSummary);
            cycle.setLoadAverages(load);
            cycle.setMMU(mmu);
            cycle.setMarkSummary(markSummary);
            cycle.setNMethodSummary(nMethodSummary);
            cycle.setSmallPageSummary(smallPageSummary);
            cycle.setMediumPageSummary(mediumPageSummary);
            cycle.setLargePageSummary(largePageSummary);
            cycle.setForwardingUsage(forwardingUsage);
            cycle.setAgeTableSummary(ageTableSummary);
            return cycle;
        }

        public void setPauseMarkStart(DateTimeStamp pauseMarkStart) {
            this.pauseMarkStart = pauseMarkStart;
        }

        public void setPauseMarkStartDuration(double pauseMarkStartDuration) {
            this.pauseMarkStartDuration = pauseMarkStartDuration;
        }

        public void setPauseMarkEndStart(DateTimeStamp pauseMarkEndStart) {
            this.pauseMarkEndStart = pauseMarkEndStart;
        }

        public void setPauseMarkEndDuration(double pauseMarkEndDuration) {
            this.pauseMarkEndDuration = pauseMarkEndDuration;
        }

        public void setPauseRelocateStart(DateTimeStamp pauseRelocateStart) {
            this.pauseRelocateStart = pauseRelocateStart;
        }

        public void setPauseRelocateStartDuration(double pauseRelocateStartDuration) {
            this.pauseRelocateStartDuration = pauseRelocateStartDuration;
        }

        public void setConcurrentMarkStart(DateTimeStamp concurrentMarkStart) {
            this.concurrentMarkStart = concurrentMarkStart;
        }

        public void setConcurrentMarkDuration(double concurrentMarkDuration) {
            this.concurrentMarkDuration = concurrentMarkDuration;
        }

        public void setConcurrentMarkFreeStart(DateTimeStamp concurrentMarkFreeStart) {
            this.concurrentMarkFreeStart = concurrentMarkFreeStart;
        }
        public void setConcurrentMarkFreeDuration(double concurrentMarkFreeDuration) {
            this.concurrentMarkFreeDuration = concurrentMarkFreeDuration;
        }

        public void setConcurrentProcessNonStringReferencesStart(DateTimeStamp concurrentProcessNonStringReferencesStart) {
            this.concurrentProcessNonStringReferencesStart = concurrentProcessNonStringReferencesStart;
        }

        public void setConcurrentProcessNonStrongReferencesDuration(double concurrentProcessNonStrongReferencesDuration) {
            this.concurrentProcessNonStrongReferencesDuration = concurrentProcessNonStrongReferencesDuration;
        }

        public void setConcurrentResetRelocationSetStart(DateTimeStamp concurrentResetRelocationSetStart) {
            this.concurrentResetRelocationSetStart = concurrentResetRelocationSetStart;
        }

        public void setConcurrentResetRelocationSetDuration(double concurrentResetRelocationSetDuration) {
            this.concurrentResetRelocationSetDuration = concurrentResetRelocationSetDuration;
        }

        public void setConcurrentSelectRelocationSetStart(DateTimeStamp concurrentSelectRelocationSetStart) {
            this.concurrentSelectRelocationSetStart = concurrentSelectRelocationSetStart;
        }

        public void setConcurrentSelectRelocationSetDuration(double concurrentSelectRelocationSetDuration) {
            this.concurrentSelectRelocationSetDuration = concurrentSelectRelocationSetDuration;
        }

        public void setConcurrentSelectRelocateStart(DateTimeStamp concurrentSelectRelocateStart) {
            this.concurrentSelectRelocateStart = concurrentSelectRelocateStart;
        }

        public void setConcurrentSelectRelocateDuration(double concurrentSelectRelocateDuration) {
            this.concurrentSelectRelocateDuration = concurrentSelectRelocateDuration;
        }

        //Memory
        public void setMarkStart(ZGCMemoryPoolSummary summary) {
            this.markStart = summary;
        }

        public void setMarkEnd(ZGCMemoryPoolSummary summary) {
            this.markEnd = summary;
        }

        public void setRelocateStart(ZGCMemoryPoolSummary summary) {
            this.relocatedStart = summary;
        }

        public void setRelocateEnd(ZGCMemoryPoolSummary summary) {
            this.relocateEnd = summary;
        }

        public void setMarkedLiveSummary(ZGCLiveSummary summary) {
            this.liveSummary = summary;
        }

        public void setAllocatedSummary(ZGCAllocatedSummary summary) {
            this.allocatedSummary = summary;
        }

        public void setGarbageSummary(ZGCGarbageSummary summary) {
            this.garbageSummary = summary;
        }

        public void setReclaimSummary(ZGCReclaimSummary summary) {
            reclaimSummary = summary;
        }

        public void setMemorySummary(ZGCMemorySummary summary) {
            this.memorySummary = summary;
        }

        public void setMetaspaceSummary(ZGCMetaspaceSummary summary) {
            this.metaspaceSummary = summary;
        }

        public void setLoad(double[] load) {
            this.load = load;
        }

        public void setMMU(double[] mmu) {
            this.mmu = mmu;
        }

        public void setConcurrentMarkContinueStart(DateTimeStamp startTime) {
            this.concurrentMarkContinueStart = startTime;
        }

        public void setConcurrentMarkContinueDuration(double duration) {
            this.concurrentMarkContinueDuration = duration;
        }

        public void setConcurrentRemapRootsStart(DateTimeStamp startTime) {
            this.concurrentRemapRootsStart = startTime;
        }

        public void setConcurrentRemapRootsDuration(double duration) {
            this.concurrentRemapRootsDuration = duration;
        }

        public void setMarkRootsStart(DateTimeStamp startTime) {

            this.markRootsStart = startTime;
        }

        public void setMarkRootsDuration(double duration) {

            this.markRootsDuration = duration;
        }

        public void setMarkFollowStart(DateTimeStamp markFollowStart) {

            this.markFollowStart = markFollowStart;
        }

        public void setMarkFollowDuration(double markFollowDuration) {

            this.markFollowDuration = markFollowDuration;
        }

        public void setRemapRootsColoredStart(DateTimeStamp remapRootColoredStart) {

            this.remapRootColoredStart = remapRootColoredStart;
        }

        public void setRemapRootsColoredDuration(double remapRootsColoredDuration) {

            this.remapRootsColoredDuration = remapRootsColoredDuration;
        }

        public void setRemapRootsUncoloredStart(DateTimeStamp remapRootsUncoloredStart) {

            this.remapRootsUncoloredStart = remapRootsUncoloredStart;
        }

        public void setRemapRootsUncoloredDuration(double remapRootsUncoloredDuration) {

            this.remapRootsUncoloredDuration = remapRootsUncoloredDuration;
        }

        public void setRemapRememberedStart(DateTimeStamp remapRememberedStart) {

            this.remapRememberedStart = remapRememberedStart;
        }

        public void setRemapRememberedDuration(double remapRememberedDuration) {

            this.remapRememberedDuration = remapRememberedDuration;
        }

        public void setMarkSummary(ZGCMarkSummary markSummary) {
            this.markSummary = markSummary;
        }

        public void setPromotedSummary(ZGCPromotedSummary promotedSummary) {
            this.promotedSummary = promotedSummary;
        }

        public void setCompactedSummary(ZGCCompactedSummary compactedSummary) {
            this.compactedSummary = compactedSummary;
        }

        public void setGcDuration(double gcDuration) {
            this.gcDuration = gcDuration;
        }

        public void setUsed(ZGCPhase phase, OccupancySummary summary) {
            switch (phase) {
                case FULL:
                    // does not apply to non generational GC
                    break;
                case MAJOR_YOUNG:
                case MAJOR_OLD:
                case MINOR_YOUNG:
                    this.usedOccupancySummary = summary;
                    break;
            }
        }

        public void setSoftRefSummary(ZGCReferenceSummary summary) {
            this.softRefSummary = summary;
        }

        public void setWeakRefSummary(ZGCReferenceSummary weakRefSummary) {
            this.weakRefSummary = weakRefSummary;
        }

        public void setFinalRefSummary(ZGCReferenceSummary finalRefSummary) {
            this.finalRefSummary = finalRefSummary;
        }

        public void setPhantomRefSummary(ZGCReferenceSummary phantomRefSummary) {
            this.phantomRefSummary = phantomRefSummary;
        }

        public void setHeapCapacitySummary(ZGCHeapCapacitySummary heapCapacitySummary) {
            this.heapCapacitySummary = heapCapacitySummary;
        }

        public void setNMethodSummary(ZGCNMethodSummary nMethodSummary) {
            this.nMethodSummary = nMethodSummary;
        }

        public void setSmallPageSummary(ZGCPageSummary smallPageSummary) {
            this.smallPageSummary = smallPageSummary;
        }

        public void setMediumPageSummary(ZGCPageSummary mediumPageSummary) {
            this.mediumPageSummary = mediumPageSummary;
        }

        public void setLargePageSummary(ZGCPageSummary largePageSummary) {
            this.largePageSummary = largePageSummary;
        }

        public void setForwardingUsage(long forwardingUsage) {
            this.forwardingUsage = forwardingUsage;
        }

        public void addPageAgeSummary(ZGCPageAgeSummary summary) {
            if (this.ageTableSummary == null) {
                this.ageTableSummary = new ArrayList<>();
            }

            ageTableSummary.add(summary);
        }
    }

    @Override
    public boolean accepts(Diary diary) {
        return diary.isZGC();
    }

    @Override
    public void publishTo(JVMEventChannel bus) {
        super.publishTo(bus);
    }
}
