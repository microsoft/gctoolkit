// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.event.zgc.OccupancySummary;
import com.microsoft.gctoolkit.event.zgc.ReclaimSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCCycle;
import com.microsoft.gctoolkit.event.zgc.ZGCMemoryPoolSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCMetaspaceSummary;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.parser.collection.MRUQueue;
import com.microsoft.gctoolkit.parser.unified.ZGCPatterns;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.AbstractMap;
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

    private ZGCForwardReference forwardReference;

    private final long[] markStart = new long[3];
    private final long[] markEnd = new long[3];
    private final long[] relocateStart = new long[3];
    private final long[] relocateEnd = new long[3];

    private final MRUQueue<GCParseRule, BiConsumer<GCLogTrace, String>> parseRules;

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
        parseRules.put(MEMORY_TABLE_ENTRY_RECLAIMED, this::reclaimed);
        parseRules.put(MEMORY_SUMMARY, this::memorySummary);
        parseRules.put(END_OF_FILE, this::endOfFile);
    }


    public ZGCParser() {}

    @Override
    public Set<EventSource> eventsProduced() {
        return Set.of(EventSource.ZGC);
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

    private void cycleStart(GCLogTrace trace, String s) {
        forwardReference = new ZGCForwardReference(getClock(), trace.getLongGroup(1), trace.gcCause(1, 1));
    }

    private void pausePhase(GCLogTrace trace, String s) {
        DateTimeStamp startTime = getClock().minus(Math.round(trace.getDuration()) / 1000.00d);
        if ("Mark Start".equals(trace.getGroup(1))) {
            forwardReference.setPauseMarkStartDuration(trace.getDuration());
            forwardReference.setPauseMarkStart(startTime);
        } else if ("Mark End".equals(trace.getGroup(1))) {
            forwardReference.setPauseMarkEndDuration(trace.getDuration());
            forwardReference.setPauseMarkEndStart(startTime);
        } else if ("Relocate Start".equals(trace.getGroup(1))) {
            forwardReference.setPauseRelocateStartDuration(trace.getDuration());
            forwardReference.setPauseRelocateStart(startTime);
        } else
            trace.notYetImplemented();
    }

    private void concurrentPhase(GCLogTrace trace, String s) {
        DateTimeStamp startTime = getClock().minus(Math.round(trace.getDuration()) / 1000.0d);
        if ("Mark".equals(trace.getGroup(1))) {
            forwardReference.setConcurrentMarkDuration(trace.getDuration());
            forwardReference.setConcurrentMarkStart(startTime);
        } else if ("Mark Free".equals(trace.getGroup(1))) {
            forwardReference.setConcurrentMarkFreeDuration(trace.getDuration());
            forwardReference.setConcurrentMarkFreeStart(startTime);
        } else if ("Process Non-Strong References".equals(trace.getGroup(1))) {
            forwardReference.setConcurrentProcessNonStrongReferencesDuration(trace.getDuration());
            forwardReference.setConcurrentProcessNonStringReferencesStart(startTime);
        } else if ("Reset Relocation Set".equals(trace.getGroup(1))) {
            forwardReference.setConcurrentResetRelocationSetDuration(trace.getDuration());
            forwardReference.setConcurrentResetRelocationSetStart(startTime);
        } else if ("Select Relocation Set".equals(trace.getGroup(1))) {
            forwardReference.setConcurrentSelectRelocationSetDuration(trace.getDuration());
            forwardReference.setConcurrentSelectRelocationSetStart(startTime);
        } else if ("Relocate".equals(trace.getGroup(1))) {
            forwardReference.setConcurrentSelectRelocateStart(startTime);
            forwardReference.setConcurrentSelectRelocateDuration(trace.getDuration());
        } else
            trace.notYetImplemented();
    }

    private void load(GCLogTrace trace, String s) {
        double[] load = new double[3];
        load[0] = trace.getDoubleGroup(1);
        load[1] = trace.getDoubleGroup(2);
        load[2] = trace.getDoubleGroup(3);
        forwardReference.setLoad(load);
    }

    private void mmu(GCLogTrace trace, String s) {
        double[] mmu = new double[6];
        mmu[0] = trace.getDoubleGroup(1);
        mmu[1] = trace.getDoubleGroup(2);
        mmu[2] = trace.getDoubleGroup(3);
        mmu[3] = trace.getDoubleGroup(4);
        mmu[4] = trace.getDoubleGroup(5);
        mmu[5] = trace.getDoubleGroup(6);
        forwardReference.setMMU(mmu);
    }

    private void markSummary(GCLogTrace trace, String s) {
        //trace.notYetImplemented();
    }

    private void relocationSummary(GCLogTrace trace, String s) {
        //trace.notYetImplemented();
    }

    private void nMethods(GCLogTrace trace, String s) {
        //trace.notYetImplemented();
    }

    private void metaspace(GCLogTrace trace, String s) {
        ZGCMetaspaceSummary summary = new ZGCMetaspaceSummary(
                trace.toKBytes(1),
                trace.toKBytes(3),
                trace.toKBytes(5));
        forwardReference.setMetaspace(summary);
    }

    private void referenceProcessing(GCLogTrace trace, String s) {
        //trace.notYetImplemented();
    }

    private void capacity(GCLogTrace trace, String s) {
        //trace.notYetImplemented();
    }

    private void captureAtIndex(GCLogTrace trace, int index) {
        markStart[index] = trace.toKBytes(2);
        markEnd[index] = trace.toKBytes(5);
        relocateStart[index] = trace.toKBytes(8);
        relocateEnd[index] = trace.toKBytes(11);
    }

    private void sizeEntry(GCLogTrace trace, String s) {
        switch (trace.getGroup(1)) {
            case "Capacity":
                captureAtIndex(trace, 0);
                break;
            case "Free":
                captureAtIndex(trace, 1);
                break;
            case "Used":
                forwardReference.setMarkStart(new ZGCMemoryPoolSummary(markStart[0], markStart[1], trace.toKBytes(2)));
                forwardReference.setMarkEnd(new ZGCMemoryPoolSummary(markEnd[0], markEnd[1], trace.toKBytes(5)));
                forwardReference.setRelocateStart(new ZGCMemoryPoolSummary(relocateStart[0], relocateStart[1], trace.toKBytes(8)));
                forwardReference.setRelocateEnd(new ZGCMemoryPoolSummary(relocateEnd[0], relocateEnd[1], trace.toKBytes(11)));
                break;
            default:
                LOGGER.warning(trace.getGroup(1) + "not recognized, Heap Occupancy/size is is ignored. Please report this with the GC log");
        }
    }

    private void occupancyEntry(GCLogTrace trace, String s) {
        OccupancySummary summary = new OccupancySummary(
                trace.toKBytes(2),
                trace.toKBytes(5),
                trace.toKBytes(8));
        if ("Live".equals(trace.getGroup(1))) {
            forwardReference.setMarkedLive(summary);
        } else if ("Allocated".equals(trace.getGroup(1))) {
            forwardReference.setAllocated(summary);
        } else if ("Garbage".equals(trace.getGroup(1))) {
            forwardReference.setGarbage(summary);
        } else
            trace.notYetImplemented();
    }

    private void reclaimed(GCLogTrace trace, String s) {
        forwardReference.setReclaimed(
                new ReclaimSummary(
                        trace.toKBytes(1),
                        trace.toKBytes(4)
                )
        );
    }

    private void memorySummary(GCLogTrace trace, String s) {
        forwardReference.setMemorySummary(
                new ReclaimSummary(
                        trace.toKBytes(2),
                        trace.toKBytes(5)
                )
        );
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
        publish(forwardReference.toZGCCycle(getClock()));
    }

    public void publish(JVMEvent event) {
        super.publish(ChannelName.ZGC_PARSER_OUTBOX, event);
        forwardReference = null;
    }

    private class ZGCForwardReference {
        private final DateTimeStamp startTimeStamp;
        private final GCCause gcCause;
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

        // Memory
        private ZGCMemoryPoolSummary markStart;
        private ZGCMemoryPoolSummary markEnd;
        private ZGCMemoryPoolSummary relocatedStart;
        private ZGCMemoryPoolSummary relocateEnd;
        private OccupancySummary markedLive;
        private OccupancySummary allocated;
        private OccupancySummary garbage;
        private ReclaimSummary reclaimed;
        private ReclaimSummary memorySummary;
        private ZGCMetaspaceSummary metaspace;

        //Load
        private double[] load = new double[3];
        private double[] mmu = new double[6];

        public ZGCForwardReference(DateTimeStamp dateTimeStamp, long gcId, GCCause cause) {
            this.startTimeStamp = dateTimeStamp;
            this.gcId = gcId;
            gcCause = cause;
        }

        ZGCCycle toZGCCycle(DateTimeStamp endTime) {
            ZGCCycle cycle = new ZGCCycle(startTimeStamp, GarbageCollectionTypes.ZGCCycle, gcCause, endTime.minus(startTimeStamp));
            cycle.setGcId(gcId);
            cycle.setPauseMarkStart(pauseMarkStart, pauseMarkStartDuration);
            cycle.setConcurrentMark(concurrentMarkStart, concurrentMarkDuration);
            cycle.setConcurrentMarkFree(concurrentMarkFreeStart, concurrentMarkFreeDuration);
            cycle.setPauseMarkEnd(pauseMarkEndStart, pauseMarkEndDuration);
            cycle.setConcurrentProcessNonStrongReferences(concurrentProcessNonStringReferencesStart, concurrentProcessNonStrongReferencesDuration);
            cycle.setConcurrentResetRelocationSet(concurrentResetRelocationSetStart, concurrentResetRelocationSetDuration);
            cycle.setConcurrentSelectRelocationSet(concurrentSelectRelocationSetStart, concurrentSelectRelocationSetDuration);
            cycle.setPauseRelocateStart(pauseRelocateStart, pauseRelocateStartDuration);
            cycle.setConcurrentRelocate(concurrentSelectRelocateStart, concurrentSelectRelocateDuration);
            //Memory
            cycle.setMarkStart(markStart);
            cycle.setMarkEnd(markEnd);
            cycle.setRelocateStart(relocatedStart);
            cycle.setRelocateEnd(relocateEnd);
            cycle.setLive(markedLive);
            cycle.setAllocated(allocated);
            cycle.setGarbage(garbage);
            cycle.setReclaimed(reclaimed);
            cycle.setMemorySummary(memorySummary);
            cycle.setMetaspace(metaspace);
            cycle.setLoadAverages(load);
            cycle.setMMU(mmu);
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

        public void setMarkedLive(OccupancySummary summary) {
            markedLive = summary;
        }

        public void setAllocated(OccupancySummary summary) {
            this.allocated = summary;
        }

        public void setGarbage(OccupancySummary summary) {
            this.garbage = summary;
        }

        public void setReclaimed(ReclaimSummary summary) {
            reclaimed = summary;
        }

        public void setMemorySummary(ReclaimSummary summary) {
            this.memorySummary = summary;
        }

        public void setMetaspace(ZGCMetaspaceSummary summary) {
            this.metaspace = summary;
        }

        public void setLoad(double[] load) {
            this.load = load;
        }

        public void setMMU(double[] mmu) {
            this.mmu = mmu;
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
