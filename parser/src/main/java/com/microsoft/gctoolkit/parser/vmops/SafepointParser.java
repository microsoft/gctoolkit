// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;

import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.event.jvm.Safepoint;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.parser.JVMEventConsumer;
import com.microsoft.gctoolkit.parser.PreUnifiedGCLogParser;
import com.microsoft.gctoolkit.parser.UnifiedGCLogParser;

import java.util.function.Supplier;

public class SafepointParser {

    public static class PreUnified extends PreUnifiedGCLogParser implements SafepointPatterns.PreUnified {
        public PreUnified(Diary diary, JVMEventConsumer consumer) {
            super(diary, consumer);
        }

        @Override
        public String getName() {
            return "SafepointParser.PreUnified";
        }

        // TODO - refactor to remove duplicate code (with SafepointParser.Unified)
        @Override
        protected void process(String line) {
            SafepointTrace trace;
            if ((trace = TRACE.parse(line, this.getClass())) != null) {
                Safepoint safepoint = trace.toSafepoint();
                consumer.record(safepoint);
            } else if (line.equals(END_OF_DATA_SENTINEL))
                consumer.record(new JVMTermination(getClock(), diary.getTimeOfFirstEvent()));
        }
    }

    public static class Unified extends UnifiedGCLogParser implements SafepointPatterns.Unified {
        public Unified(Diary diary, JVMEventConsumer consumer) {
            super(diary, consumer);
        }

        @Override
        public String getName() {
            return "SafepointParser.Unified";
        }

        // TODO - refactor to remove duplicate code (with SafepointParser.PreUnified)
        @Override
        protected void process(String line) {
            SafepointTrace trace;
            if ((trace = TRACE.parse(line, this.getClass())) != null) {
                Safepoint safepoint = trace.toSafepoint();
                consumer.record(safepoint);
            } else if (line.equals(END_OF_DATA_SENTINEL))
                consumer.record(new JVMTermination(getClock(), diary.getTimeOfFirstEvent()));
        }
    }

    private SafepointParser() {}

}

