// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.ReferenceGCSummary;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.MetaspaceRecord;
import com.microsoft.gctoolkit.event.jvm.PermGenSummary;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.DataSourceParser;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GCLogParser implements DataSourceParser, SharedPatterns {

    private static final Logger LOGGER = Logger.getLogger(GCLogParser.class.getName());
    public static final String END_OF_DATA_SENTINEL = GCLogFile.END_OF_DATA_SENTINEL;

    public static final GCParseRule GCID_COUNTER = new GCParseRule("GCID_COUNTER", " GC\\((\\d+)\\) ");
    private JVMEventChannel consumer;
    protected Diary diary;
    private DateTimeStamp clock = new DateTimeStamp(DateTimeStamp.EPOC, 0.0d);
    private double lastDuration = 0.0d;


    public GCLogParser() {}

    /**
     * Setting the diary will set the current clock time to the time of the first event
     * in the GC log. This is a better choice than EPOC though EPOC is better than null.
     * @param diary summary the GC log.
     */
    @Override
    public void diary(Diary diary) {
        this.diary = diary;
        this.clock = diary.getTimeOfFirstEvent();
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
            LOGGER.log(Level.WARNING, "Log File may be Corrupted: Time traveled backwards from {0} to {1}", new Object[]{getClock().toString(), now.toString()});
        }
        //todo: should preserve date in cases where statements do not respect PrintDateStamp (eg. ergonomics)
        setClock(now);
    }

    /**
     * The clock is advanced to the time at the end of the event.
     * The times reported in the GCLog file are "noisy" at best. This
     * hyperactives the clock rollback detection which doesn't allow
     * for slop. To minimize this, the clock only records when a time
     * stamp is presented and isn't advanced to the end of the event
     * except in the case of EOF where the time of JVM termination has
     * not been recorded. In this case, JVMTermination time should advanced
     * to after the previous event has ended.
     *
     * @param channel to publish to
     * @param event   to be published
     */
    public void publish(ChannelName channel, JVMEvent event) {
        lastDuration = event.getDuration();
        consumer.publish(channel,event);
    }

    public void receive(String trace) {
        if (!trace.equals(END_OF_DATA_SENTINEL))
            advanceClock(trace);
        else
            advanceClock(getClock().add(lastDuration));
        process(trace);
    }

    boolean isPreJDK17040() {
        return diary.isPre70_40();
    }

    boolean hasPrintGCDetails() {
        return diary.isPrintGCDetails();
    }

    // todo: mixes aggregator with parsing. premature optimization...
    MemoryPoolSummary getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(GCLogTrace trace, int offset) {
        return trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(offset);
    }

    MemoryPoolSummary getTotalOccupancyWithTotalHeapSizeSummary(GCLogTrace trace, int offset) {
        return trace.getOccupancyWithMemoryPoolSizeSummary(offset);
    }

    GCLogTrace extractReferenceBlock(String line, GCParseRule rule) {
        return rule.parse(line);
    }

    ReferenceGCSummary extractPrintReferenceGC(String line) {

        ReferenceGCSummary summary = new ReferenceGCSummary();
        GCLogTrace trace;
        if ((trace = extractReferenceBlock(line, SOFT_REFERENCE)) != null)
            summary.addSoftReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(6), trace.getDuration());
        if ((trace = extractReferenceBlock(line, WEAK_REFERENCE)) != null)
            summary.addWeakReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(6), trace.getDuration());

        if ((trace = extractReferenceBlock(line, FINAL_REFERENCE)) != null)
            summary.addFinalReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(6), trace.getDuration());

        if ((trace = extractReferenceBlock(line, PHANTOM_REFERENCE)) != null) {
            if (trace.groupNotNull(7)) {
                summary.addPhantomReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(6), trace.getIntegerGroup(7), trace.getDuration());
            }
            else
                summary.addPhantomReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(6), trace.getDuration());
        }

        if ((trace = extractReferenceBlock(line, JNI_REFERENCE)) != null) {
            if (trace.groupNotNull(6))
                summary.addJNIWeakReferences(trace.getDateTimeStamp(), trace.getIntegerGroup(6), trace.getDuration());
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
            String recordType = trace.getGroup(1).trim();
            switch (recordType) {
                case "CMS Perm":
                case "PS Perm":
                case "PSPermGen":
                case "Perm":
                    metaDataPool = extractPermGenRecord(trace);
                    break;
                case "Metaspace":
                    if (trace.getGroup(2) != null) {
                        metaDataPool = new MetaspaceRecord(trace.toKBytes(2), trace.toKBytes(4), trace.toKBytes(6));
                    } else {
                        metaDataPool = new MetaspaceRecord(trace.toKBytes(4), trace.toKBytes(4), trace.toKBytes(6));
                    }
                    break;
            }
        } else if ((trace = META_SPACE_RECORD.parse(line)) != null) {
            int index = (trace.getGroup(1) == null) ? 1 : 3;
            metaDataPool = new MetaspaceRecord(trace.toKBytes(index), trace.toKBytes(3), trace.toKBytes(5));
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

    @Override
    public void publishTo(JVMEventChannel channel) {
        this.consumer = channel;
    }

    @Override
    public ChannelName channel() {
        return ChannelName.DATA_SOURCE;
    }
}
