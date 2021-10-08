// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.event.zgc.OccupancySummary;
import com.microsoft.gctoolkit.event.zgc.ReclaimSummary;
import com.microsoft.gctoolkit.event.zgc.ZGCCycle;
import com.microsoft.gctoolkit.event.zgc.ZGCMemoryPoolSummary;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.parser.collection.MRUQueue;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;
import com.microsoft.gctoolkit.parser.unified.ZGCPatterns;

import java.util.AbstractMap;
import java.util.Optional;
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

    private final MRUQueue<GCParseRule, BiConsumer<GCLogTrace, String>> parseRules;

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
        parseRules.put(END_OF_FILE,this::endOfFile);
    }

    public ZGCParser(LoggingDiary diary, JVMEventConsumer consumer) {
        super(diary, consumer);
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

    // TODO populate with lines that should be ignored
    private final boolean inPrintHeapAtGC = false;

    private boolean ignoreFrequentButUnwantedEntries(String line) {
        if ( MEMORY_TABLE_HEADER.parse(line) != null) return true;
        return false;
    }

    public void endOfFile(GCLogTrace trace, String line) {
        record(new JVMTermination(getClock()));
    }

    //Implement all capture methods

    private ZGCCycle cycle;
    private ZGCForwardReference forwardReference;

    private void cycleStart(GCLogTrace trace, String s) {
        forwardReference = new ZGCForwardReference(getClock(), trace.gcCause(0,1));
    }

    private void pausePhase(GCLogTrace trace, String s) {
        DateTimeStamp startTime = getClock().minus( trace.getDuration() / 1000.00d);
        if ( "Mark Start".equals(trace.getGroup(1))) {
            forwardReference.setPauseMarkStartDuration(trace.getDuration());
            forwardReference.setPauseMarkStart(startTime);
        } else if ("Mark End".equals(trace.getGroup(1))) {
            forwardReference.setPauseMarkEndDuration(trace.getDuration());
            forwardReference.setPauseMarkEndStart(startTime);
        } else if ( "Relocate Start".equals(trace.getGroup(1))) {
            forwardReference.setPauseRelocateStartDuration(trace.getDuration());
            forwardReference.setPauseRelocateStart(startTime);
        } else
            trace.notYetImplemented();
    }

    private void concurrentPhase(GCLogTrace trace, String s) {
        DateTimeStamp startTime = getClock().minus( trace.getDuration() / 1000.0d);
        if ( "Mark".equals(trace.getGroup(1))) {
            forwardReference.setConcurrentMarkDuration(trace.getDuration());
            forwardReference.setConcurrentMarkStart(startTime);
        } else if ( "Process Non-Strong References".equals(trace.getGroup(1))) {
            forwardReference.setConcurrentProcessNonStrongReferencesDuration(trace.getDuration());
            forwardReference.setConcurrentProcessNonStringReferencesStart(startTime);
        } else if ( "Reset Relocation Set".equals(trace.getGroup(1))) {
            forwardReference.setConcurrentResetRelocationSetDuration(trace.getDuration());
            forwardReference.setConcurrentResetRelocationSetStart(startTime);
        } else if ( "Select Relocation Set".equals(trace.getGroup(1))) {
            forwardReference.setConcurrentSelectRelocationSetDuration(trace.getDuration());
            forwardReference.setConcurrentSelectRelocationSetStart(startTime);
        } else if ( "Relocate".equals(trace.getGroup(1))) {
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
        ZGCMemoryPoolSummary summary = new ZGCMemoryPoolSummary(trace.getLongGroup(3),trace.getLongGroup(5),0L,trace.getLongGroup(1));
        forwardReference.setMetaspace(summary);
    }

    private void referenceProcessing(GCLogTrace trace, String s) {
        //trace.notYetImplemented();
    }

    private void capacity(GCLogTrace trace, String s) {
        //trace.notYetImplemented();
    }

    private long[] markStart = new long[3];
    private long[] markEnd = new long[3];
    private long[] relocateStart = new long[3];
    private long[] relocateEnd = new long[3];

    private void captureAtIndex(GCLogTrace trace, int index) {
        markStart[index] = trace.getLongGroup(2);
        markEnd[index] =  trace.getLongGroup(5);
        relocateStart[index] = trace.getLongGroup(8);
        relocateEnd[index] = trace.getLongGroup(11);
    }

    private void sizeEntry(GCLogTrace trace, String s) {
        switch(trace.getGroup(1)) {
            case "Capacity" :
                captureAtIndex( trace,0);
                break;
            case "Reserve"  :
                captureAtIndex( trace,1);
                break;
            case "Free"     :
                captureAtIndex( trace,2);
                break;
            case "Used"     :
                forwardReference.setMarkStart(new ZGCMemoryPoolSummary( markStart[0], markStart[1], markStart[2], trace.getLongGroup(2)));
                forwardReference.setMarkEnd(new ZGCMemoryPoolSummary( markEnd[0], markEnd[1], markEnd[2], trace.getLongGroup(5)));
                forwardReference.setRelocateStart(new ZGCMemoryPoolSummary( relocateStart[0], relocateStart[1], relocateStart[2], trace.getLongGroup(8)));
                forwardReference.setRelocateEnd(new ZGCMemoryPoolSummary( relocateEnd[0], relocateEnd[1], relocateEnd[2], trace.getLongGroup(11)));
                break;
            default         :
                LOGGER.warning(trace.getGroup(1) + "not recognized, Heap Occupancy/size is is ignored. Please report this with the GC log");
        }
    }

    private void occupancyEntry(GCLogTrace trace, String s) {
        OccupancySummary summary = new OccupancySummary(trace.getLongGroup(2), trace.getLongGroup(5), trace.getLongGroup(8));
        if ( "Live".equals(trace.getGroup(1))) {
            forwardReference.setMarkedLive(summary);
        } else if ( "Allocated".equals(trace.getGroup(1))) {
            forwardReference.setAllocated(summary);
        } else if ( "Garbage".equals(trace.getGroup(1))) {
            forwardReference.setGarbage(summary);
        } else
            trace.notYetImplemented();
    }

    private void reclaimed(GCLogTrace trace, String s) {
        forwardReference.setReclaimed(new ReclaimSummary(trace.getLongGroup(1),trace.getLongGroup(4)));
    }

    private void memorySummary(GCLogTrace trace, String s) {
        forwardReference.setMemorySummary(new ReclaimSummary(trace.getLongGroup(2), trace.getLongGroup(5)));
        record();
    }

    private void log(String line) {
        if (debugging)
            LOGGER.log(Level.FINE,"ZGCHeapParser missed: {0}", line);
        LOGGER.log(Level.WARNING, "Missed: {0}", line);

    }

    public void logMissedFirstRecordForEvent(String line) {
        LOGGER.log(Level.WARNING, "Missing initial record for: {0}", line);
    }

    public void record() {
        record(forwardReference.toZGCCycle(getClock()));
    }

    public void record(JVMEvent event) {
        consumer.record(event);
        forwardReference = null;
    }

    private class ZGCForwardReference {
        private final DateTimeStamp startTimeStamp;
        private final GCCause gcCause;

        // Timing
        private DateTimeStamp pauseMarkStart;
        private double pauseMarkStartDuration;
        private DateTimeStamp pauseMarkEndStart;
        private double pauseMarkEndDuration;
        private DateTimeStamp pauseRelocateStart;
        private double pauseRelocateStartDuration;
        private DateTimeStamp concurrentMarkStart;
        private double concurrentMarkDuration;
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
        private ZGCMemoryPoolSummary metaspace;

        //Load
        private double[] load = new double[3];
        private double[] mmu = new double[6];


        public ZGCForwardReference(DateTimeStamp dateTimeStamp, GCCause cause) {
            this.startTimeStamp = dateTimeStamp;
            gcCause = cause;
        }

        ZGCCycle toZGCCycle(DateTimeStamp endTime) {
            ZGCCycle cycle = new ZGCCycle(startTimeStamp,gcCause, endTime.minus(startTimeStamp));
            cycle.setPauseMarkStart(pauseMarkStart,pauseMarkStartDuration);
            cycle.setConcurrentMark(concurrentMarkStart,concurrentMarkDuration);
            cycle.setPauseMarkEnd( pauseMarkEndStart, pauseMarkEndDuration);
            cycle.setConcurrentProcessNonStrongReferences(concurrentProcessNonStringReferencesStart, concurrentProcessNonStrongReferencesDuration);
            cycle.setConcurrentResetRelocationSet(concurrentResetRelocationSetStart,concurrentResetRelocationSetDuration);
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

        public void setMetaspace(ZGCMemoryPoolSummary summary) { this.metaspace = summary; }

        public void setLoad(double[] load) {
            this.load = load;
        }

        public void setMMU(double[] mmu) {
            this.mmu = mmu;
        }
    }
}
