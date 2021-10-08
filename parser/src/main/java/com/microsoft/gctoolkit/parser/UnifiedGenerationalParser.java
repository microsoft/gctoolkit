// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.generational.AbortablePreClean;
import com.microsoft.gctoolkit.event.generational.CMSConcurrentEvent;
import com.microsoft.gctoolkit.event.generational.CMSRemark;
import com.microsoft.gctoolkit.event.generational.ConcurrentMark;
import com.microsoft.gctoolkit.event.generational.ConcurrentModeFailure;
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
import com.microsoft.gctoolkit.event.generational.YoungGC;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.parser.collection.RuleSet;
import com.microsoft.gctoolkit.parser.jvm.Decorators;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;
import com.microsoft.gctoolkit.parser.unified.UnifiedGenerationalPatterns;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.Abortable_Preclean;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.ConcurrentModeFailure;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.Concurrent_Mark;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.Concurrent_Preclean;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.Concurrent_Reset;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.Concurrent_Sweep;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.DefNew;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.FullGC;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.InitialMark;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.PSFull;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.PSYoungGen;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.ParNew;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.Remark;

/**
 * TODO No reports or views generated from this data yet.
 * <p>
 * Result on
 * - when GC started
 * - type of GC triggered
 * - from, to, configured
 * - pause time if it is reported or can be calculated
 */
public class UnifiedGenerationalParser extends UnifiedGCLogParser implements UnifiedGenerationalPatterns {

    private static final Logger LOGGER = Logger.getLogger(UnifiedGenerationalParser.class.getName());
    private boolean debugging = Boolean.getBoolean("microsoft.debug");

    private final RuleSet<GCParseRule, BiConsumer<GCLogTrace, String>> parseRules;

    {
        parseRules = new RuleSet<>();
        parseRules.put(CMS_TAG, this::tag);
        parseRules.put(PARALLEL_TAG, this::tag);
        parseRules.put(SERIAL_TAG, this::tag);
        parseRules.put(YOUNG_HEADER, this::youngHeader);
        parseRules.put(GENERATIONAL_MEMORY_SUMMARY, this::generationalMemorySummary);
        parseRules.put(GENERATIONAL_MEMORY_SUMMARY_EXTENDED, this::extendedGenerationalMemorySummary);
        parseRules.put(UNIFIED_META_DATA, this::metaSpaceSummary);
        parseRules.put(YOUNG_DETAILS, this::youngDetails);
        parseRules.put(CPU_BREAKOUT, this::cpuBreakout);
        parseRules.put(INITIAL_MARK, this::initialMark);
        parseRules.put(INITIAL_MARK_SUMMARY, this::initialMarkSummary);
        parseRules.put(CONCURRENT_PHASE_START, this::concurrentPhaseStart);
        parseRules.put(CONCURRENT_PHASE_END, this::concurrentPhaseEnd);
        parseRules.put(WORKER_THREADS, this::workerThreads);
        parseRules.put(REMARK, this::remark);
        parseRules.put(REMARK_SUMMARY, this::remarkSummary);
        parseRules.put(GC_PHASE, this::remarkPhase);
        parseRules.put(OLD_SUMMARY, this::oldSummary);
        parseRules.put(PROMOTION_FAILED, this::promotionFailed);
        parseRules.put(FULL_GC, this::fullGC);
        parseRules.put(FULL_GC_SUMMARY, this::fullGCSummary);
        parseRules.put(FULL_GC_PHASE_START, this::fullGCPhase);
        parseRules.put(FULL_GC_PHASE_END, this::fullGCPhaseEnd);
        parseRules.put(PRE_COMPACT, this::preCompact);
        parseRules.put(PARALLEL_PHASE, this::parallelPhase);
        parseRules.put(PARALLEL_PHASE_SUMMARY, this::parallelPhaseSummary);
        parseRules.put(JVM_EXIT, this::jvmExit);
        parseRules.put(END_OF_FILE, this::jvmExit);
        parseRules.put(METASPACE_DETAILED, this::metaSpaceDetails);

    }

    private final Map<String, GarbageCollectionTypes> concurrentPhases = Map.of(
            "Mark", Concurrent_Mark,
            "Preclean", Concurrent_Preclean,
            "Abortable Preclean", Abortable_Preclean,
            "Sweep", Concurrent_Sweep,
            "Reset", Concurrent_Reset
    );

    public UnifiedGenerationalParser(LoggingDiary diary, JVMEventConsumer consumer) {
        super(diary, consumer);
    }

    public String getName() {
        return "UnifiedG1GCParser";
    }

    @Override
    protected void process(String line) {

        if (ignoreFrequentlySeenButUnwantedLines(line)) return;

        Optional<AbstractMap.SimpleEntry<GCParseRule, GCLogTrace>> ruleToApply = parseRules.keys().stream()
                .map(rule -> new AbstractMap.SimpleEntry<>(rule, rule.parse(line)))
                .filter(tuple -> tuple.getValue() != null)
                .findFirst();
        if (!ruleToApply.isPresent()) {
            log(line);
            return;
        }

        try {
            parseRules.get(ruleToApply.get().getKey()).accept(ruleToApply.get().getValue(), line);
        } catch (Throwable t) {
            LOGGER.throwing(this.getName(), "process", t);
        }
        log(line);
    }

    /*************
     *
     * Data Extraction methods
     */
    private GenerationalForwardReference pauseEvent = null;
    private GenerationalForwardReference concurrentCyclePauseEvent = null;
    private GenerationalForwardReference concurrentEvent = null;
    private boolean inConcurrentPhase = false;

//    private boolean isCMS;
//    private boolean isParallel;
//    private boolean isSerial;

    private void tag(GCLogTrace trace, String line) {
        noop();
    }

    private void youngHeader(GCLogTrace trace, String line) {
        if (pauseEvent != null)
            LOGGER.warning("Pause event not recorded: " + pauseEvent.getGcID());
        if (diary.isCMS())
            pauseEvent = new GenerationalForwardReference(ParNew, new Decorators(line), super.GCID_COUNTER.parse(line).getIntegerGroup(1));
        else if (diary.isPSYoung())
            pauseEvent = new GenerationalForwardReference(PSYoungGen, new Decorators(line), super.GCID_COUNTER.parse(line).getIntegerGroup(1));
        else if (diary.isSerialFull())
            pauseEvent = new GenerationalForwardReference(DefNew, new Decorators(line), super.GCID_COUNTER.parse(line).getIntegerGroup(1));
        else {
            LOGGER.warning("Unrecognized collection phase -> " + line);
            return;
        }
        pauseEvent.setStartTime(getClock());
        pauseEvent.setGCCause(trace.gcCause(1, 0));
    }

    private void generationalMemorySummary(GCLogTrace trace, String line) {
        switch (trace.getGroup(1)) {
            case "ParNew":
            case "PSYoungGen":
            case "DefNew":
                pauseEvent.setYoung(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(2));
                break;
            case "CMS":
            case "ParOldGen":
            case "Tenured":
                pauseEvent.setTenured(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(2));
                break;
            default:
                trace.notYetImplemented();
        }
    }

    private void extendedGenerationalMemorySummary(GCLogTrace trace, String line) {
        switch (trace.getGroup(1)) {
            case "ParNew":
            case "PSYoungGen":
            case "DefNew":
                pauseEvent.setYoung(trace.getEnlargedMemoryPoolRecord(2));
                break;
            case "CMS":
            case "ParOldGen":
            case "Tenured":
                pauseEvent.setTenured(trace.getEnlargedMemoryPoolRecord(2));
                break;
            default:
                trace.notYetImplemented();
        }
    }

    private void metaSpaceSummary(GCLogTrace trace, String line) {
        pauseEvent.setMetaspace(trace.getMetaSpaceRecord(1));
    }

    private void metaSpaceDetails(GCLogTrace trace, String line) {
        pauseEvent.setMetaspace(trace.getEnlargedMetaSpaceRecord(1));
        pauseEvent.setNonClassspace(trace.getEnlargedMetaSpaceRecord(9));
        pauseEvent.setClassspace(trace.getEnlargedMetaSpaceRecord(17));
    }

    private void youngDetails(GCLogTrace trace, String line) {
        pauseEvent.setDuration(trace.getDuration() / 1000.d);
        pauseEvent.setHeap(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(2));
    }

    /**
     * If the concurrentCyclePauseEvent has not been recorded, something has gone wrong and it's likely
     * that it doesn't have a consistent state. The default action is to lose it.
     * @param trace
     * @param line
     */
    private void initialMark(GCLogTrace trace, String line) {
        if (concurrentCyclePauseEvent != null)
            LOGGER.warning("Pause event not completely recorded: " + pauseEvent.getGcID());
        concurrentCyclePauseEvent = new GenerationalForwardReference(InitialMark, new Decorators(line), GCID_COUNTER.parse(line).getIntegerGroup(1));
        concurrentCyclePauseEvent.setStartTime(getClock());
    }

    private void initialMarkSummary(GCLogTrace trace, String line) {
        concurrentCyclePauseEvent.setHeap(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1));
        concurrentCyclePauseEvent .setDuration(trace.getDuration() / 1000.0d);
    }

    private void concurrentPhaseStart(GCLogTrace trace, String line) {
        if (concurrentEvent != null)
            LOGGER.warning("Concurrent phase not completely recorded: " + concurrentEvent.getGcID());
        GarbageCollectionTypes gcType = concurrentPhases.get(trace.getGroup(1));
        if (gcType == null) {
            LOGGER.warning("Unknown concurrent phase: " + line);
            return;
        }
        concurrentEvent = new GenerationalForwardReference(gcType, new Decorators(line), GCID_COUNTER.parse(line).getIntegerGroup(1));
        concurrentEvent.setStartTime(getClock());
        inConcurrentPhase = true;
    }

    private void concurrentPhaseEnd(GCLogTrace trace, String line) {
        concurrentEvent.setDuration(trace.getDuration() / 1000.0d);
    }

    private void workerThreads(GCLogTrace trace, String line) {
        notYetImplemented(trace, line);
    }

    /**
     * If the forward reference has not been recorded, assume it's state is corrupted so over-write it.
     * @param trace
     * @param line
     */
    private void remark(GCLogTrace trace, String line) {
        if (concurrentCyclePauseEvent != null)
            LOGGER.warning("Pause event not recorded and is about to be lost: " + pauseEvent.getGcID());
        concurrentCyclePauseEvent = new GenerationalForwardReference(Remark, new Decorators(line), GCID_COUNTER.parse(line).getIntegerGroup(1));
        concurrentCyclePauseEvent.setStartTime(getClock());
    }

    private void remarkSummary(GCLogTrace trace, String line) {
        concurrentCyclePauseEvent.setHeap(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1));
        concurrentCyclePauseEvent.setDuration(trace.getDuration() / 1000.0d);
    }

    //Rescan \\(parallel\\)|Reference Processing|Weak Processing|ClassLoaderData|ProtectionDomainCacheTable|ResolvedMethodTable|Class Unloading|Scrub Symbol Table|Scrub String Table
    private void remarkPhase(GCLogTrace trace, String line) {
        concurrentCyclePauseEvent.addCMSRemarkPhase(trace.getGroup(1), trace.getDuration() / 1000.0d);
    }

    private void oldSummary(GCLogTrace trace, String line) {
        //the problem is all of the phases have been reported on
        //this data is floating outside events in our current model
        //ignore for now....
    }

    private void promotionFailed(GCLogTrace trace, String line) {
        if (pauseEvent != null) {
            pauseEvent.convertToConcurrentModeFailure();
        }
    }

    private void fullGC(GCLogTrace trace, String line) {
        if (pauseEvent == null) {
            if (diary.isPSOldGen())
                pauseEvent = new GenerationalForwardReference(PSFull, new Decorators(line), super.GCID_COUNTER.parse(line).getIntegerGroup(1));
            else
                pauseEvent = new GenerationalForwardReference(FullGC, new Decorators(line), super.GCID_COUNTER.parse(line).getIntegerGroup(1));
            pauseEvent.setStartTime(getClock());
        } else if (pauseEvent.getGarbageCollectionType() == ParNew) {
            pauseEvent.convertToConcurrentModeFailure();
        } else if (pauseEvent.getGarbageCollectionType() == DefNew) {
            pauseEvent.convertToSerialFull();
        } else if (pauseEvent.getGarbageCollectionType() != ConcurrentModeFailure) {
            LOGGER.warning("Pause event not recorded: " + pauseEvent.getGcID()); //todo: difficult to know if this is a full or a CMF
            pauseEvent = new GenerationalForwardReference(FullGC, new Decorators(line), super.GCID_COUNTER.parse(line).getIntegerGroup(1));
            pauseEvent.setStartTime(getClock());
        }
        pauseEvent.setGCCause(trace.gcCause(1, 0));
    }

    private void fullGCSummary(GCLogTrace trace, String line) {
        pauseEvent.setHeap(trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(2));
        pauseEvent.setDuration(trace.getDuration() / 1000.0d);
    }

    private void fullGCPhase(GCLogTrace trace, String line) {
        //todo: noop();
    }

    private void fullGCPhaseEnd(GCLogTrace trace, String line) {
        pauseEvent.addFullGCPhase(trace.getGroup(2), trace.getDuration() / 1000.0d);
    }

    private double preCompactPhaseDuration = -1.0d;

    private void preCompact(GCLogTrace trace, String line) {
        preCompactPhaseDuration = trace.getDuration() / 1000.0d;
    }

    private void parallelPhase(GCLogTrace trace, String line) {
        //todo: noop()
    }

    private void parallelPhaseSummary(GCLogTrace trace, String line) {
        pauseEvent.addFullGCPhase(trace.getGroup(1), trace.getDuration() / 1000.0d);
    }

    private void jvmExit(GCLogTrace trace, String line) {
        consumer.record(new JVMTermination(getClock()));
    }

    /**
     *
     * By convention, events are emitted iini the order that they started. For CMS, it's possible to have a ParNew
     * intermixed with a concurrent cycle. To cover these cases, the young gen collection is cached, the concurrent
     * event is completed and then the cached event is emitted.
     */
    private ArrayList<GenerationalGCPauseEvent> cache = new ArrayList<>();

    private void cpuBreakout(GCLogTrace trace, String line) {
        GCLogTrace gcidTrace = GCID_COUNTER.parse(line);
        if (gcidTrace != null) {
            CPUSummary cpuSummary = new CPUSummary(trace.getDoubleGroup(1), trace.getDoubleGroup(2), trace.getDoubleGroup(3));
            int gcid = gcidTrace.getIntegerGroup(1);
            // There are 3 cases to consider.
            // - pause event outside of a concurrent cycle
            // - pause event that is part of the concurrent cycle
            // - a non-concurrent cycle pause event during a concurrent cycle
            // In all cases, the goal is to preserve the time ordering of the events. This translates to, if there
            // is an external pause event during a concurrent cycle and we're in a concurrent phase, then the
            // external pause event needs to be cached. When the concurrent phase ends, it should ben published
            // and then all of the external pause events should be published
            if (pauseEvent != null && pauseEvent.getGcID() == gcid) {
                pauseEvent.add(cpuSummary);
                if (inConcurrentPhase) {
                    cache.add(buildPauseEvent((pauseEvent)));
                } else {
                    consumer.record(buildPauseEvent(pauseEvent));
                }
                pauseEvent = null;
            } else if (concurrentCyclePauseEvent != null && concurrentCyclePauseEvent.getGcID() == gcid) {
                concurrentCyclePauseEvent.add(cpuSummary);
                consumer.record(buildPauseEvent(concurrentCyclePauseEvent));
                concurrentCyclePauseEvent = null;
            } else if ((concurrentEvent != null) && (concurrentEvent.getGcID() == gcid)) {
                concurrentEvent.add(cpuSummary);
                consumer.record(buildConcurrentPhase(concurrentEvent));
                concurrentEvent = null;
                inConcurrentPhase = false;
                cache.forEach(consumer::record);
                cache.clear();
            }
        }
    }

    private void fillOutMetaspaceData(GenerationalGCPauseEvent event, GenerationalForwardReference forwardReference) {
        if (forwardReference.getMetaspace() != null)
            event.addPermOrMetaSpaceRecord(forwardReference.getMetaspace());
        if (forwardReference.getClassspace() != null)
            event.addClassspace(forwardReference.getClassspace());
        if (forwardReference.getNonClassspace() != null)
            event.addNonClassspace(forwardReference.getNonClassspace());
    }

    private void fillOutMemoryPoolData(GenerationalGCPauseEvent event, GenerationalForwardReference values) {
        int map = 0;
        map |= (values.getHeap() != null) ? 1 : 0;
        map |= (values.getYoung() != null) ? 2 : 0;
        map |= (values.getTenured() != null) ? 4 : 0;
        switch (map) {
            default:
            case 0:  // none, error
            case 2: // young
            case 4: // tenured
                break;
            case 1: // heap
                event.add(values.getHeap());
                break;
            case 3: // young, heap
                event.add(values.getYoung(), values.getHeap());
                break;
            case 5: // tenured, heap
                event.add(values.getHeap().minus(values.getTenured()), values.getTenured(), values.getHeap());
                break;
            case 6: // tenured, young
                event.add(values.getYoung(), values.getTenured(), values.getYoung().add(values.getTenured()));
                break;
            case 7: // tenured, young, heap
                event.add(values.getYoung(), values.getTenured(), values.getHeap());
        }
    }

    private GenerationalGCPauseEvent buildYoungEvent(GenerationalForwardReference forwardReference) {
        GenerationalGCPauseEvent youngCollection = null;
        switch (forwardReference.getGarbageCollectionType()) {
            case DefNew:
                youngCollection = new DefNew(forwardReference.getStartTime(), forwardReference.getGCCause(), forwardReference.getDuration());
                break;
            case ParNew:
                youngCollection = new ParNew(forwardReference.getStartTime(), forwardReference.getGCCause(), forwardReference.getDuration());
                break;
            case Young:
                youngCollection = new YoungGC(forwardReference.getStartTime(), forwardReference.getGCCause(), forwardReference.getDuration());
                break;
            case PSYoungGen:
                youngCollection = new PSYoungGen(forwardReference.getStartTime(), forwardReference.getGCCause(), forwardReference.getDuration());
                break;
            default:
                LOGGER.warning(forwardReference.getGarbageCollectionType() + " not recognized");
        }

        fillOutMemoryPoolData(youngCollection, forwardReference);
        fillOutMetaspaceData(youngCollection, forwardReference);
        youngCollection.add(forwardReference.getCPUSummary());
        // add in reference processing
        return youngCollection;
    }

    public InitialMark buildInitialMark(GenerationalForwardReference values) {
        InitialMark collection = new InitialMark(values.getStartTime(), values.getGCCause(), values.getDuration());
        collection.add(values.getHeap());
        collection.add(values.getCPUSummary());
        return collection;
    }

    private CMSRemark buildRemark(GenerationalForwardReference values) {
        CMSRemark remark = new CMSRemark(values.getStartTime(), values.getGCCause(), values.getDuration());
        remark.add(values.getHeap());
        //add in all the other work
        remark.add(values.getCPUSummary());
        return remark;
    }

    private FullGC fillOutFullGC(FullGC event, GenerationalForwardReference values) {
        fillOutMemoryPoolData(event, values);
        // add in reference processing
        fillOutMetaspaceData(event,values);
        event.add(values.getCPUSummary());
        return event;
    }

    private FullGC buildFullGC(GenerationalForwardReference forwardReference) {
        switch (forwardReference.getGarbageCollectionType()) {
            case PSFull:
                return fillOutFullGC(new PSFullGC(forwardReference.getStartTime(), forwardReference.getGCCause(), forwardReference.getDuration()), forwardReference);
            case FullGC:
            case Full:
                return fillOutFullGC(new FullGC(forwardReference.getStartTime(), forwardReference.getGCCause(), forwardReference.getDuration()), forwardReference);
            default:
                LOGGER.warning(forwardReference.getGarbageCollectionType() + " is unrecognized");
        }
        return null;
    }

    private ConcurrentModeFailure buildConcurrentModeFailure(GenerationalForwardReference forwardReference) {
        return (ConcurrentModeFailure) fillOutFullGC(new ConcurrentModeFailure(forwardReference.getStartTime(), forwardReference.getGCCause(), forwardReference.getDuration()), forwardReference);
    }

    private GenerationalGCPauseEvent buildPauseEvent(GenerationalForwardReference forwardReference) {
        switch (forwardReference.getGarbageCollectionType()) {
            case DefNew:
            case PSYoungGen:
            case ParNew:
            case Young:
                return buildYoungEvent(forwardReference);
            case InitialMark:
                return buildInitialMark(forwardReference);
            case Remark:
                return buildRemark(forwardReference);
            case PSFull: //todo:
            case FullGC:
            case Full:
                return buildFullGC(forwardReference);
            case ConcurrentModeFailure:
                return buildConcurrentModeFailure(forwardReference);
            default:
                LOGGER.warning(forwardReference.getGarbageCollectionType() + " is unrecognized");
        }
        notYetImplemented();
        return null;
    }

    private CMSConcurrentEvent buildConcurrentPhase(GenerationalForwardReference values) {
        switch (values.getGarbageCollectionType()) {
            case Concurrent_Mark:
                return new ConcurrentMark(values.getStartTime(), values.getDuration(), values.getCPUSummary().getKernel() + values.getCPUSummary().getUser(), values.getCPUSummary().getWallClock());
            case Concurrent_Preclean:
                return new ConcurrentPreClean(values.getStartTime(), values.getDuration(), values.getCPUSummary().getKernel() + values.getCPUSummary().getUser(), values.getCPUSummary().getWallClock());
            case Abortable_Preclean:
                return new AbortablePreClean(values.getStartTime(), values.getDuration(), values.getCPUSummary().getKernel() + values.getCPUSummary().getUser(), values.getCPUSummary().getWallClock(), false); //todo: if duration is ? 2 minutes, this should be true
            case Concurrent_Sweep:
                return new ConcurrentSweep(values.getStartTime(), values.getDuration(), values.getCPUSummary().getKernel() + values.getCPUSummary().getUser(), values.getCPUSummary().getWallClock());
            case Concurrent_Reset:
                return new ConcurrentReset(values.getStartTime(), values.getDuration(), values.getCPUSummary().getKernel() + values.getCPUSummary().getUser(), values.getCPUSummary().getWallClock());
            default:
                LOGGER.warning(values.getGarbageCollectionType() + " is unrecognized");
        }
        return null;
    }

    // diagnostics

    private void notYetImplemented() {
    }

    private void notImplemented(GCLogTrace trace, String line) {
        trace.notYetImplemented();
    }

    private boolean ignoreFrequentlySeenButUnwantedLines(String line) {
        if (line.contains("application threads")) return true;
        if (line.contains("Application time")) return true;
        if (line.contains("exit"))
            if (line.contains("used")) return true;
        if (line.contains("workers")) return true;
        if (line.contains("Heap address")) return true;
        return line.contains("Desired") || line.contains("Age table") || line.contains("- age ");
    }


    private void log(String line) {
        if (debugging)
            LOGGER.fine("Missed: " + line);

        LOGGER.log(Level.FINE, "Missed: {0}", line);

    }
}
