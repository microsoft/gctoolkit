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

/**
 * Abstract class representing a parser for GC log files.
 * Implements the DataSourceParser and SharedPatterns interfaces.
 */
public abstract class GCLogParser implements DataSourceParser, SharedPatterns {

    private static final Logger LOGGER = Logger.getLogger(GCLogParser.class.getName());
    /**
     * Special string to indicate the end of the data in the GC log.
     */
    public static final String END_OF_DATA_SENTINEL = GCLogFile.END_OF_DATA_SENTINEL;

    // TODO: GCID_COUNTER should be in SharedPatterns, not here.
    /**
     * Rule for parsing the GCID counter.
     */
    public static final GCParseRule GCID_COUNTER = new GCParseRule("GCID_COUNTER", " GC\\((\\d+)\\) ");
    private JVMEventChannel consumer;
    protected Diary diary;
    private DateTimeStamp clock = new DateTimeStamp(DateTimeStamp.EPOC, 0.0d);
    private double lastDuration = 0.0d;

    /**
     * Default constructor.
     */
    public GCLogParser() {}

    /**
     * Sets the diary and initializes the clock to the time of the first event in the GC log.
     * @param diary summary of the GC log.
     */
    @Override
    public void diary(Diary diary) {
        this.diary = diary;
        this.clock = diary.getTimeOfFirstEvent();
    }

    /**
     * Gets the current clock time.
     * @return the current DateTimeStamp.
     */
    public DateTimeStamp getClock() {
        return clock;
    }

    /**
     * Sets the clock to a new value.
     * @param newValue the new DateTimeStamp value.
     */
    public void setClock(DateTimeStamp newValue) {
        this.clock = newValue;
    }

    /**
     * Abstract method to get the name of the parser.
     * @return the name of the parser.
     */
    public abstract String getName();

    /**
     * Abstract method to process a trace line from the GC log.
     * @param trace the trace line to process.
     */
    protected abstract void process(String trace);

    /**
     * Abstract method to advance the clock based on a record.
     * @param record the record to use for advancing the clock.
     */
    abstract void advanceClock(String record);

    /**
     * Advances the clock to the specified time.
     * @param now the new DateTimeStamp.
     */
    protected final void advanceClock(DateTimeStamp now) {
        if (now == null)
            return;
        else if (now.before(getClock())) {
            LOGGER.log(Level.WARNING, "Log File may be Corrupted: Time traveled backwards from {0} to {1}", new Object[]{getClock().toString(), now.toString()});
        }
        setClock(now);
    }

    /**
     * Publishes a JVM event to the specified channel.
     * @param channel the channel to publish to.
     * @param event the event to be published.
     */
    public void publish(ChannelName channel, JVMEvent event) {
        lastDuration = event.getDuration();
        consumer.publish(channel, event);
    }

    /**
     * Receives a trace line and processes it.
     * @param trace the trace line to process.
     */
    public void receive(String trace) {
        if (!trace.equals(END_OF_DATA_SENTINEL))
            advanceClock(trace);
        else
            advanceClock(getClock().add(lastDuration));
        process(trace);
    }

    /**
     * Checks if the diary indicates a pre-JDK 1.7.0_40 version.
     * @return true if pre-JDK 1.7.0_40, false otherwise.
     */
    boolean isPreJDK17040() {
        return diary.isPre70_40();
    }

    /**
     * Checks if the diary has PrintGCDetails enabled.
     * @return true if PrintGCDetails is enabled, false otherwise.
     */
    boolean hasPrintGCDetails() {
        return diary.isPrintGCDetails();
    }

    /**
     * Extracts a MemoryPoolSummary from a GCLogTrace.
     * @param trace the GCLogTrace to extract from.
     * @param offset the offset to use.
     * @return the extracted MemoryPoolSummary.
     */
    MemoryPoolSummary getTotalOccupancyBeforeAfterWithTotalHeapPoolSizeSummary(GCLogTrace trace, int offset) {
        return trace.getOccupancyBeforeAfterWithMemoryPoolSizeSummary(offset);
    }

    /**
     * Extracts a MemoryPoolSummary from a GCLogTrace.
     * @param trace the GCLogTrace to extract from.
     * @param offset the offset to use.
     * @return the extracted MemoryPoolSummary.
     */
    MemoryPoolSummary getTotalOccupancyWithTotalHeapSizeSummary(GCLogTrace trace, int offset) {
        return trace.getOccupancyWithMemoryPoolSizeSummary(offset);
    }

    /**
     * Extracts a GCLogTrace from a line using a specified GCParseRule.
     * @param line the line to parse.
     * @param rule the GCParseRule to use.
     * @return the extracted GCLogTrace.
     */
    GCLogTrace extractReferenceBlock(String line, GCParseRule rule) {
        return rule.parse(line);
    }

    /**
     * Extracts a ReferenceGCSummary from a line.
     * @param line the line to parse.
     * @return the extracted ReferenceGCSummary.
     */
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
            } else
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

    /**
     * Extracts a MemoryPoolSummary from a line representing PermGen or Metaspace records.
     * @param line the line to parse.
     * @return the extracted MemoryPoolSummary.
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

    /**
     * Extracts a PermGenSummary from a GCLogTrace.
     * @param trace the GCLogTrace to extract from.
     * @return the extracted PermGenSummary.
     */
    MemoryPoolSummary extractPermGenRecord(GCLogTrace trace) {
        int index = (trace.getGroup(2) == null) ? 2 : 4;
        return new PermGenSummary(trace.getLongGroup(index), trace.getLongGroup(4), trace.getLongGroup(6));
    }

    /**
     * Extracts the GCID from a line.
     * @param line the line to parse.
     * @return the extracted GCID, or -1 if not found.
     */
    int extractGCID(String line) {
        GCLogTrace trace = GCID_COUNTER.parse(line);
        return (trace != null) ? trace.getIntegerGroup(1) : -1;
    }

    /**
     * Extracts a CPUSummary from a line.
     * @param line the line to parse.
     * @return the extracted CPUSummary, or null if not found.
     */
    CPUSummary extractCPUSummary(String line) {
        GCLogTrace trace;
        if ((trace = CPU_BREAKDOWN.parse(line)) != null) {
            return new CPUSummary(trace.getDoubleGroup(1), trace.getDoubleGroup(2), trace.getDoubleGroup(3));
        }
        return null;
    }

    /**
     * Sets the JVMEventChannel to publish to.
     * @param channel the channel to publish to.
     */
    @Override
    public void publishTo(JVMEventChannel channel) {
        this.consumer = channel;
    }

    /**
     * Gets the channel name.
     * @return the channel name.
     */
    @Override
    public ChannelName channel() {
        return ChannelName.DATA_SOURCE;
    }
}