// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.ReferenceGCSummary;
import com.microsoft.gctoolkit.event.jvm.MetaspaceRecord;
import com.microsoft.gctoolkit.event.jvm.PermGenSummary;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;

import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class GCLogParser implements SharedPatterns {

    private static final Logger LOGGER = Logger.getLogger(GCLogParser.class.getName());
    public static final String END_OF_DATA_SENTINAL = GCLogFile.END_OF_DATA_SENTINAL;

    public static final GCParseRule GCID_COUNTER = new GCParseRule("GCID_COUNTER", " GC\\((\\d+)\\) ");
    protected final JVMEventConsumer consumer;
    protected LoggingDiary diary;
    private DateTimeStamp clock = new DateTimeStamp(0.0d);


    public GCLogParser(LoggingDiary diary, JVMEventConsumer consumer) {
        this.diary = diary;
        this.consumer = consumer;
    }

    public DateTimeStamp getClock() {
        return clock;
    }

    public void setClock(DateTimeStamp newValue) {
        this.clock = newValue;
    }

    public abstract String getName();

    protected abstract void process(String trace);

    abstract void advanceClock(String record);

    /**
     * The assumption is, this method will manage the global clock and thus should never be over ridden
     * @param now - DateTimeStamp from the current GC log record
     */
    protected final void advanceClock(DateTimeStamp now) {
        if (now == null)
            return;
            // now can be the same but it can't be less than
        else if (now.before(getClock())) {
            if ( ! now.equals(getClock()))
                LOGGER.log(Level.WARNING, "Thread: {0}, abort GC log parsing. Time traveled backwards from {1} to {2}", new Object[]{Thread.currentThread().getName(), getClock().toString(), now.toString()});
        }
        //todo: should preserve date in cases where statements do not respect PrintDateStamp (eg. ergonomics)
        setClock(now);
    }

    public void receive(String trace) {
        if (!trace.equals(END_OF_DATA_SENTINAL))
            advanceClock(trace);
        process(trace);
    }

    boolean isPreJDK17040() {
        return diary.isPre70_40();
    }

    boolean hasPrintGCDetails() {
        return diary.isPrintGCDetails();
    }

    //todo: remove heapSize as it's not used
    private long heapSize;

    void setHeapSize(long heapSize) {
        this.heapSize = heapSize;
    }

    // todo: mixes aggregator with parsing. premature optimization...
    MemoryPoolSummary getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(GCLogTrace trace, int offset) {
        MemoryPoolSummary summary = trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(offset);
        setHeapSize(summary.getSizeAfterCollection());
        return summary;
    }

    MemoryPoolSummary getTotalOccupancyWithTotalHeapSizeSummary(GCLogTrace trace, int offset) {
        MemoryPoolSummary summary = trace.getOccupancyWithMemoryPoolSizeSummary(offset);
        setHeapSize(summary.getSizeAfterCollection());
        return summary;
    }

    GCLogTrace extractReferenceBlock(String line, GCParseRule rule) {
        return rule.parse(line);
    }

    ReferenceGCSummary extractPrintReferenceGC(String line) {

        ReferenceGCSummary summary = new ReferenceGCSummary();
        GCLogTrace trace;
        if ((trace = extractReferenceBlock(line, SOFT_REFERENCE)) != null)
            summary.addSoftReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(3), trace.getDuration());

        if ((trace = extractReferenceBlock(line, WEAK_REFERENCE)) != null)
            summary.addWeakReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(3), trace.getDuration());

        if ((trace = extractReferenceBlock(line, FINAL_REFERENCE)) != null)
            summary.addFinalReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(3), trace.getDuration());

        if ((trace = extractReferenceBlock(line, PHANTOM_REFERENCE)) != null) {
            if (trace.groupNotNull(4))
                summary.addPhantomReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(3), trace.getIntegerGroup(4), trace.getDuration());
            else
                summary.addPhantomReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(3), trace.getDuration());
        }

        if ((trace = extractReferenceBlock(line, JNI_REFERENCE)) != null) {
            if (trace.groupNotNull(3))
                summary.addJNIWeakReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(3), trace.getDuration());
            else
                summary.addJNIWeakReferences(trace.getDateTimeStamp(), trace.getDuration());
        }

        return summary;

    }

    /*
     * [PSPermGen: 7034K->7034K(21248K)]
     * [CMS Perm : 10696K->10696K(21248K)]
     * [Perm : 16382K->16382K(16384K)]
     * [Perm : 16382K(16384K)]
     *
     * @param line from GC log file
     */
    MemoryPoolSummary extractPermOrMetaspaceRecord(String line) {
        GCLogTrace trace;
        MemoryPoolSummary metaDataPool = null;
        if ((trace = PERM_SPACE_RECORD.parse(line)) != null) {
            String type = trace.getGroup(1).trim();
            if ("CMS Perm".equals(type))
                metaDataPool = extractPermGenRecord(trace);
            else if ("PS Perm".equals(type))
                metaDataPool = extractPermGenRecord(trace);
            else if ("PSPermGen".equals(type))
                metaDataPool = extractPermGenRecord(trace);
            else if ("Perm".equals(type))
                metaDataPool = extractPermGenRecord(trace);
            else if ("Metaspace".equals(type)) {
                if (trace.getGroup(2) != null) {
                    metaDataPool = new MetaspaceRecord(trace.getMemoryInKBytes(2), trace.getMemoryInKBytes(4), trace.getMemoryInKBytes(6));
                } else {
                    metaDataPool = new MetaspaceRecord(trace.getMemoryInKBytes(4), trace.getMemoryInKBytes(4), trace.getMemoryInKBytes(6));
                }
            }
        } else if ((trace = META_SPACE_RECORD.parse(line)) != null) {
            int index = (trace.getGroup(1) == null) ? 1 : 3;
            metaDataPool = new MetaspaceRecord(trace.getMemoryInKBytes(index), trace.getMemoryInKBytes(3), trace.getMemoryInKBytes(5));
        }

        return metaDataPool;
    }

    MemoryPoolSummary extractPermGenRecord(GCLogTrace trace) {
        int index = (trace.getGroup(2) == null) ? 2 : 4;
        return new PermGenSummary(trace.getLongGroup(index), trace.getLongGroup(4), trace.getLongGroup(6));
    }

    int extractGCID(String line) {
        GCLogTrace trace = GCID_COUNTER.parse(line);
        return (trace != null) ? trace.getIntegerGroup(1) : -1;
    }

    CPUSummary extractCPUSummary(String line) {
        GCLogTrace trace;
        if ((trace = CPU_BREAKDOWN.parse(line)) != null) {
            return new CPUSummary(trace.getDoubleGroup(1), trace.getDoubleGroup(2), trace.getDoubleGroup(3));
        }
        return null;
    }

    double toKBytes(double value, String units) {
        switch (units.charAt(0)) {
            case 'G':
            case 'g':
                value *= 1024L;
            case 'M':
            case 'm':
                value *= 1024L;
            case 'K':
            case 'k':
                break;
            case 'B':
            case 'b':
                value /= 1024;
                break;
            default:
        }

        return value;
    }
}
