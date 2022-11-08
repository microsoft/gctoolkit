// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.event.jvm.SurvivorRecord;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.JVMEventBus;

import static com.microsoft.gctoolkit.parser.unified.UnifiedPatterns.JVM_EXIT;

public class SurvivorMemoryPoolParser extends PreUnifiedGCLogParser implements TenuredPatterns {

    private SurvivorRecord forwardReference = null;

    public SurvivorMemoryPoolParser(Diary diary) {
        super(diary);
    }

    public String getName() {
        return "SurvivorMemoryPoolParser";
    }

    /**
     * 61.572: [GC 61.572: [ParNew
     * Desired survivor size 1343488 bytes, new threshold 4 (max 4)
     * - age   1:      25176 bytes,      25176 total
     *
     * @param entry : GC log entry to deriveConfiguration
     */
    @Override
    protected void process(String entry) {
        GCLogTrace trace;

        if ((trace = TENURING_SUMMARY.parse(entry)) != null) {
            forwardReference = new SurvivorRecord(getClock(), trace.getLongGroup(1), trace.getIntegerGroup(2), trace.getIntegerGroup(3));
        } else if ((trace = TENURING_AGE_BREAKDOWN.parse(entry)) != null) {
            forwardReference.add(trace.getIntegerGroup(1), trace.getLongGroup(2));
        } else if (entry.equals(END_OF_DATA_SENTINEL) || (JVM_EXIT.parse(entry) != null)) {
            if (forwardReference != null)
                consumer.publish(forwardReference);
            consumer.publish(new JVMTermination(getClock(),diary.getTimeOfFirstEvent()));
        } else if (forwardReference != null) {
            consumer.publish(forwardReference);
            forwardReference = null;
        }
    }

    @Override
    public boolean accepts(Diary diary) {
        return diary.isTenuringDistribution();
    }

    @Override
    public void publishTo(JVMEventBus bus) {
        super.publishTo(bus, Channels.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX.getChannel());
    }
}
