// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;

import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.event.jvm.Safepoint;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.parser.PreUnifiedGCLogParser;

import java.util.Set;

public class SafepointParser extends PreUnifiedGCLogParser implements SafepointPatterns {

    public SafepointParser() {}

    @Override
    public Set<EventSource> eventsProduced() {
        return Set.of(EventSource.SAFEPOINT);
    }

    public String getName() {
        return "SafepointParser";
    }

    protected void process(String line) {
        SafepointTrace trace;
        if ((trace = TRACE.parse(line)) != null) {
            Safepoint safepoint = trace.toSafepoint();
            super.publish(ChannelName.JVM_EVENT_PARSER_OUTBOX, safepoint);
        } else if (line.equals(END_OF_DATA_SENTINEL))
            super.publish( ChannelName.JVM_EVENT_PARSER_OUTBOX, new JVMTermination(getClock(),diary.getTimeOfFirstEvent()));
    }

    @Override
    public boolean accepts(Diary diary) {
        return (diary.isTLABData() || diary.isApplicationStoppedTime() || diary.isApplicationRunningTime()) && ! diary.isUnifiedLogging();
    }

    @Override
    public void publishTo(JVMEventChannel bus) {
        super.publishTo(bus);
    }
}

