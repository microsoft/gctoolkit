// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;

import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.event.jvm.Safepoint;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.JVMEventBus;
import com.microsoft.gctoolkit.parser.PreUnifiedGCLogParser;


public class SafepointParser extends PreUnifiedGCLogParser implements SafepointPatterns {

    public SafepointParser(Diary diary) {
        super(diary);
    }

    public String getName() {
        return "SafepointParser";
    }

    protected void process(String line) {
        SafepointTrace trace;
        if ((trace = TRACE.parse(line)) != null) {
            Safepoint safepoint = trace.toSafepoint();
            consumer.publish(safepoint);
        } else if (line.equals(END_OF_DATA_SENTINEL))
            consumer.publish(new JVMTermination(getClock(),diary.getTimeOfFirstEvent()));
    }

    @Override
    public boolean accepts(Diary diary) {
        return (diary.isTLABData() || diary.isApplicationStoppedTime() || diary.isApplicationRunningTime()) && ! diary.isUnifiedLogging();
    }

    @Override
    public void publishTo(JVMEventBus bus) {
        super.publishTo(bus, Channels.JVM_EVENT_PARSER_OUTBOX.getName());
    }
}

