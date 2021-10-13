// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;

import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.event.jvm.Safepoint;
import com.microsoft.gctoolkit.parser.JVMEventConsumer;
import com.microsoft.gctoolkit.parser.PreUnifiedGCLogParser;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;


public class SafepointParser extends PreUnifiedGCLogParser implements SafepointPatterns {

    public SafepointParser(LoggingDiary diary, JVMEventConsumer consumer) {
        super(diary, consumer);
    }

    public String getName() {
        return "SafepointParser";
    }

    protected void process(String line) {
        SafepointTrace trace;
        if ((trace = TRACE.parse(line)) != null) {
            Safepoint safepoint = trace.toSafepoint();
            consumer.record(safepoint);
        } else if (line.equals(END_OF_DATA_SENTINEL))
            consumer.record(new JVMTermination(getClock()));
    }
}

