// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.generational.AbortablePreClean;
import com.microsoft.gctoolkit.event.generational.CMSRemark;
import com.microsoft.gctoolkit.event.generational.ConcurrentMark;
import com.microsoft.gctoolkit.event.generational.ConcurrentPreClean;
import com.microsoft.gctoolkit.event.generational.ConcurrentReset;
import com.microsoft.gctoolkit.event.generational.ConcurrentSweep;
import com.microsoft.gctoolkit.event.generational.InitialMark;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;

import java.util.logging.Logger;

public class CMSTenuredPoolParser extends PreUnifiedGCLogParser implements SimplePatterns, ICMSPatterns {

    private static final Logger LOG = Logger.getLogger(CMSTenuredPoolParser.class.getName());
    private DateTimeStamp startOfPhase = null;
    private GCParseRule EndOfFile = new GCParseRule("END_OF_DATA_SENTINAL", END_OF_DATA_SENTINAL);

    public CMSTenuredPoolParser(LoggingDiary diary, JVMEventConsumer eventConsumer) {
        super(diary, eventConsumer);
    }

    public String getName() {
        return "CMSTenuredPoolParser";
    }

    @Override
    protected void process(String line) {

        GCLogTrace trace;

        //this rule must be evaluated before CONCURRENT_PHASE_END_BLOCK
        if ((trace = ABORT_PRECLEAN_DUE_TO_TIME_CLAUSE.parse(line)) != null)
            abortPrecleanDueToTime(trace);
        else if ((trace = CONCURRENT_PHASE_START_BLOCK.parse(line)) != null)
            startOfConcurrentPhase(trace);
        else if ((trace = CONCURRENT_PHASE_END_BLOCK.parse(line)) != null)
            endOfConcurrentPhase(trace);
        else if ((trace = PRECLEAN_REFERENCE.parse(line)) != null)
            endConcurrentPrecleanWithReferenceProcessing(trace);
        else if ((trace = INITIAL_MARK.parse(line)) != null)
            initialMark(trace);
        else if ((trace = REMARK_CLAUSE.parse(line)) != null)
            remark(trace, line);
        else if ((trace = REMARK_REFERENCE_PROCESSING.parse(line)) != null)
            remarkWithReferenceProcessing(trace, line);
        else if ((trace = SPLIT_REMARK.parse(line)) != null)
            startOfPhase = getClock();
        else if ((trace = EndOfFile.parse(line)) != null) {
            consumer.record(new JVMTermination(getClock()));
        }

    }

    /**
     * 12.986: [GC[1 CMS-initial-mark: 33532K(62656K)] 49652K(81280K), 0.0014191 secs]
     * null,12.986,null,null,null,null,33532,K,62656,K,null,null,null,49652,K,81280,K,0.0014191
     * first 6 is the date.
     */
    private void initialMark(GCLogTrace trace) {
        InitialMark initialMark = new InitialMark(trace.getDateTimeStamp(), GCCause.UNKNOWN_GCCAUSE, trace.getDoubleGroup(trace.groupCount()));
        MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(4);
        MemoryPoolSummary heap = trace.getOccupancyWithMemoryPoolSizeSummary(8);
        initialMark.add(heap.minus(tenured), tenured, heap);
        consumer.record(initialMark);
    }

    //12.987: [CMS-concurrent-mark-start]
    private void startOfConcurrentPhase(GCLogTrace trace) {
        startOfPhase = trace.getDateTimeStamp();
    }

    private void endOfConcurrentPhase(GCLogTrace trace) {
        DateTimeStamp endOfPhase = trace.getDateTimeStamp();
        endOfConcurrentPhase(trace, endOfPhase);
    }

    private void endConcurrentPrecleanWithReferenceProcessing(GCLogTrace trace) {
        try {
            consumer.record(new ConcurrentPreClean(startOfPhase, trace.getDoubleGroup(14) - startOfPhase.getTimeStamp(), trace.getDoubleGroup(16), trace.getDoubleGroup(17)));
        } catch (Throwable t) {
            LOG.warning("concurrent phase choked on " + trace.toString());
        }
    }

    private void endOfConcurrentPhase(GCLogTrace trace, DateTimeStamp timeStamp) {
        String phase = trace.getGroup(3);
        double cpuTime = trace.getDoubleGroup(4);
        double wallTime = trace.getDoubleGroup(5);
        double duration = timeStamp.getTimeStamp() - startOfPhase.getTimeStamp();
        if ("mark".equals(phase))
            consumer.record(new ConcurrentMark(startOfPhase, duration, cpuTime, wallTime));
        else if ("preclean".equals(phase))
            consumer.record(new ConcurrentPreClean(startOfPhase, duration, cpuTime, wallTime));
        else if ("abortable-preclean".equals(phase))
            consumer.record(new AbortablePreClean(startOfPhase, duration, cpuTime, wallTime, false));
        else if ("sweep".equals(phase))
            consumer.record(new ConcurrentSweep(startOfPhase, duration, cpuTime, wallTime));
        else if ("reset".equals(phase))
            consumer.record(new ConcurrentReset(startOfPhase, duration, cpuTime, wallTime));
        else
            LOG.warning("concurrent phase choked on " + trace.toString());
    }

    private void abortPrecleanDueToTime(GCLogTrace trace) {
        try {
            double cpuTime = trace.getDoubleGroup(4);
            double wallClock = trace.getDoubleGroup(5);
            consumer.record(new AbortablePreClean(startOfPhase, trace.getDateTimeStamp().getTimeStamp() - startOfPhase.getTimeStamp(), cpuTime, wallClock, true));
        } catch (Exception e) {
            LOG.warning("concurrent phase end choked on " + trace);
        }
    }

    private void remark(GCLogTrace trace, String line) {
        consumer.record(extractRemark(trace, line));
    }

    private void remarkWithReferenceProcessing(GCLogTrace trace, String line) {
        CMSRemark remark = extractRemark(trace, line);
        remark.addReferenceGCSummary(extractPrintReferenceGC(line));
        consumer.record(remark);
    }

    private CMSRemark extractRemark(GCLogTrace trace, String line) {
        // [1 CMS-remark: 30259925K(60327552K)] 30338613K(60768448K), 0.0528880 secs]
        GCLogTrace prefix = GC_PREFIX_RULE.parse(line);
        GCCause gcCause = GCCause.CMS_FINAL_REMARK;
        if (prefix != null) {
            startOfPhase = getClock();
            gcCause = prefix.gcCause();
            if (gcCause == GCCause.UNKNOWN_GCCAUSE)
                gcCause = GCCause.CMS_FINAL_REMARK;
        }

        CMSRemark remark = new CMSRemark(startOfPhase, gcCause, trace.getDoubleGroup(trace.groupCount()));

        try {
            MemoryPoolSummary tenured = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(1);
            MemoryPoolSummary heap = trace.getOccupancyWithMemoryPoolSizeSummary(5);
            remark.add(heap.minus(tenured), tenured, heap);
            recordRescanStepTimes(remark, line);
            remark.add(extractPrintReferenceGC(line));
        } catch (Exception e) {
            LOG.warning("Unable to properly extract data from " + trace);
        }
        return remark;
    }
}

