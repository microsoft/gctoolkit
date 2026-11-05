// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;

import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.event.jvm.Safepoint;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.parser.GCLogTrace;
import com.microsoft.gctoolkit.parser.UnifiedGCLogParser;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnifiedSafepointParser extends UnifiedGCLogParser implements UnifiedSafepointPatterns {

    private static final Logger LOGGER = Logger.getLogger(UnifiedSafepointParser.class.getName());
    private static final double NANOS_PER_SECOND = 1_000_000_000.0d;

    private static final int VM_OPERATION_GROUP = 1;
    private static final int TIME_SINCE_LAST_GROUP = 2;
    private static final int REACHING_SAFEPOINT_GROUP = 3;
    private static final int CLEANUP_GROUP = 4;
    private static final int AT_SAFEPOINT_GROUP = 5;
    private static final int LEAVING_SAFEPOINT_GROUP = 6;
    private static final int TOTAL_GROUP = 7;
    private static final int RUNNABLE_THREADS_GROUP = 8;
    private static final int TOTAL_THREADS_GROUP = 9;

    public UnifiedSafepointParser() {}

    @Override
    public Set<EventSource> eventsProduced() {
        return Set.of(EventSource.SAFEPOINT);
    }

    public String getName() {
        return "UnifiedSafepointParser";
    }

    @Override
    protected void process(String line) {
        try {
            GCLogTrace trace;
            if ((trace = SAFEPOINT.parse(line)) != null) {
                super.publish(ChannelName.JVM_EVENT_PARSER_OUTBOX, extractSafepoint(trace));
            } else if (line.equals(END_OF_DATA_SENTINEL)) {
                super.publish(ChannelName.JVM_EVENT_PARSER_OUTBOX, new JVMTermination(getClock(), diary.getTimeOfFirstEvent()));
            }
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, "Missed: {0}", line);
        }
    }

    private Safepoint extractSafepoint(GCLogTrace trace) {
        double total = nanosToSeconds(trace, TOTAL_GROUP);
        Safepoint safepoint = new Safepoint(trace.getGroup(VM_OPERATION_GROUP), getClock().minus(total), total);
        safepoint.recordPhases(nanosToSeconds(trace, TIME_SINCE_LAST_GROUP),
                nanosToSeconds(trace, REACHING_SAFEPOINT_GROUP),
                nanosToSeconds(trace, AT_SAFEPOINT_GROUP));
        if (trace.groupNotNull(CLEANUP_GROUP))
            safepoint.recordCleanupPhaseDuration(nanosToSeconds(trace, CLEANUP_GROUP));
        if (trace.groupNotNull(LEAVING_SAFEPOINT_GROUP))
            safepoint.recordLeavingSafepointDuration(nanosToSeconds(trace, LEAVING_SAFEPOINT_GROUP));
        if (trace.groupNotNull(RUNNABLE_THREADS_GROUP))
            safepoint.recordThreadsAtSafepoint(trace.getIntegerGroup(RUNNABLE_THREADS_GROUP), trace.getIntegerGroup(TOTAL_THREADS_GROUP));
        return safepoint;
    }

    private static double nanosToSeconds(GCLogTrace trace, int group) {
        return trace.getLongGroup(group) / NANOS_PER_SECOND;
    }

    @Override
    public boolean accepts(Diary diary) {
        return (diary.isApplicationStoppedTime() || diary.isApplicationRunningTime()) && diary.isUnifiedLogging();
    }

    @Override
    public void publishTo(JVMEventChannel bus) {
        super.publishTo(bus);
    }
}
