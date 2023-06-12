// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.event.jvm.SurvivorRecord;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.parser.jvm.Decorators;

import java.util.Set;

import static com.microsoft.gctoolkit.parser.unified.UnifiedPatterns.CPU_BREAKOUT;
import static com.microsoft.gctoolkit.parser.unified.UnifiedPatterns.JVM_EXIT;

public class UnifiedSurvivorMemoryPoolParser extends UnifiedGCLogParser implements TenuredPatterns {

    /**
     * [16.962s][debug][gc,age       ] GC(14) Desired survivor size 10485760 bytes, new threshold 15 (max threshold 15)
     * [16.973s][trace][gc,age       ] GC(14) Age table with threshold 15 (max threshold 15)
     * [16.973s][trace][gc,age       ] GC(14) - age   1:     768744 bytes,     768744 total
     * ...
     * [16.974s][trace][gc,age       ] GC(14) - age  14:     542328 bytes,    8307008 total
     */
    private GCParseRule DESIRED_SURVIVOR_SIZE = new GCParseRule("DESIRED_SURVIVOR_SIZE", "Desired survivor size " + COUNTER + " bytes, new threshold " + COUNTER + " \\(max threshold " + COUNTER + "\\)");
    private GCParseRule AGE_TABLE_HEADER = new GCParseRule("AGE_TABLE_HEADER", "Age table with threshold " + COUNTER + " \\(max threshold " + COUNTER + "\\)");
    private GCParseRule AGE_RECORD = new GCParseRule("AGE_RECORD", "- age\\s+" + COUNTER + ":\\s+" + COUNTER + " bytes,\\s+" + COUNTER + " total");

    private SurvivorRecord forwardReference = null;
    private boolean ageDataCollected = false;

    public UnifiedSurvivorMemoryPoolParser() {}

    @Override
    public Set<EventSource> eventsProduced() {
        return Set.of(EventSource.SURVIVOR);
    }

    public String getName() {
        return "SurvivorMemoryPoolParser";
    }

    @Override
    protected void process(String entry) {
        GCLogTrace trace;

        if ((trace = DESIRED_SURVIVOR_SIZE.parse(entry)) != null) {
            forwardReference = new SurvivorRecord(new Decorators(entry).getDateTimeStamp(), trace.getLongGroup(1), trace.getIntegerGroup(2), trace.getIntegerGroup(3));
        } else if ((trace = AGE_TABLE_HEADER.parse(entry)) != null) {
            //we've collected this data so.. eat it...
        } else if ((trace = AGE_RECORD.parse(entry)) != null) {
            if (forwardReference != null) {
                forwardReference.add(trace.getIntegerGroup(1), trace.getLongGroup(2));
                ageDataCollected = true;
            }
        } else if (entry.equals(END_OF_DATA_SENTINEL) || (JVM_EXIT.parse(entry) != null)) {
            if (forwardReference != null)
                publish(forwardReference);
            publish(new JVMTermination(getClock(),diary.getTimeOfFirstEvent()));
        } else if (forwardReference != null && ageDataCollected) {
            publish(forwardReference);
            forwardReference = null;
            ageDataCollected = false;
        } else if (CPU_BREAKOUT.parse(entry) != null) {
            if (forwardReference != null) {
                publish(forwardReference);
                forwardReference = null;
                ageDataCollected = false;
            }
        }
    }

    @Override
    public boolean accepts(Diary diary) {
        return diary.isTenuringDistribution() && diary.isUnifiedLogging();
    }

    @Override
    public void publishTo(JVMEventChannel bus) {
        super.publishTo(bus);
    }

    private void publish(JVMEvent event) {
        super.publish(ChannelName.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX, event);
    }
}
