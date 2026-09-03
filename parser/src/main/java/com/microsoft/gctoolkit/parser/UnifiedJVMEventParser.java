// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.jvm.ApplicationConcurrentTime;
import com.microsoft.gctoolkit.event.jvm.ApplicationStoppedTime;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.microsoft.gctoolkit.parser.unified.UnifiedPatterns.JVM_EXIT;

public class UnifiedJVMEventParser extends UnifiedGCLogParser implements JVMPatterns {

    private static final Logger LOGGER = Logger.getLogger(UnifiedJVMEventParser.class.getName());
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

    private DateTimeStamp timeStamp = new DateTimeStamp(0.0d);
    private ApplicationStoppedTime.VMOperations safePointReason = null;
    private boolean gcPause = false;

    public UnifiedJVMEventParser() {}

    @Override
    public Set<EventSource> eventsProduced() {
        return Set.of(EventSource.JVM, EventSource.SAFEPOINT);
    }

    public String getName() {
        return "JavaEventParser";
    }

    @Override
    protected void process(String line) {

        GCLogTrace trace = null;

        try {

            if ((trace = UNIFIED_LOGGING_SAFEPOINT.parse(line)) != null) {
                double total = nanosToSeconds(trace, TOTAL_GROUP);
                ApplicationStoppedTime safepoint = new ApplicationStoppedTime(getClock().minus(total), total,
                        nanosToSeconds(trace, REACHING_SAFEPOINT_GROUP),
                        ApplicationStoppedTime.VMOperations.fromName(trace.getGroup(VM_OPERATION_GROUP)));
                safepoint.recordPhases(nanosToSeconds(trace, TIME_SINCE_LAST_GROUP), nanosToSeconds(trace, AT_SAFEPOINT_GROUP));
                if (trace.groupNotNull(CLEANUP_GROUP))
                    safepoint.recordCleanupTime(nanosToSeconds(trace, CLEANUP_GROUP));
                if (trace.groupNotNull(LEAVING_SAFEPOINT_GROUP))
                    safepoint.recordLeavingSafepointTime(nanosToSeconds(trace, LEAVING_SAFEPOINT_GROUP));
                if (trace.groupNotNull(RUNNABLE_THREADS_GROUP))
                    safepoint.recordThreadCounts(trace.getIntegerGroup(RUNNABLE_THREADS_GROUP), trace.getIntegerGroup(TOTAL_THREADS_GROUP));
                publish(safepoint);
                safePointReason = null;
                gcPause = false;
            } else if ((trace = UNIFIED_LOGGING_APPLICATION_STOP_TIME_WITH_STOPPING_TIME.parse(line)) != null) {
                if (safePointReason != null)
                    publish(new ApplicationStoppedTime(timeStamp, trace.getDoubleGroup(1), trace.getDoubleGroup(2), safePointReason));
                else
                    publish(new ApplicationStoppedTime(timeStamp, trace.getDoubleGroup(1), trace.getDoubleGroup(2), gcPause));
                safePointReason = null;
                gcPause = false;
            } else if (GC_PAUSE_CLAUSE.parse(line) != null) {
                gcPause = true;
            } else if ((trace = SAFEPOINT_REGION.parse(line)) != null) {
                timeStamp = getClock();
                safePointReason = ApplicationStoppedTime.VMOperations.fromName(trace.getGroup(1));
            } else if ((trace = LEAVING_SAFEPOINT.parse(line)) != null) {
            } //noop this one.

            else if ((trace = UNIFIED_LOGGING_APPLICATION_TIME.parse(line)) != null) {
                publish(new ApplicationConcurrentTime(getClock(), trace.getDoubleGroup(1)));
            } else if (line.equals(END_OF_DATA_SENTINEL) || (JVM_EXIT.parse(line) != null)) {
                publish(new JVMTermination(getClock(),diary.getTimeOfFirstEvent()));
            } else if (getClock().getTimeStamp() > timeStamp.getTimeStamp()) {
                if (isGCPause(line)) gcPause = true;
                timeStamp = getClock();
            }

        } catch (Throwable t) {
            LOGGER.log(Level.FINE, "Missed: {0}", line);
        }
    }

    /**
     * Converts values in nanoseconds to seconds
     */
    private static double nanosToSeconds(GCLogTrace trace, int group) {
        return trace.getLongGroup(group) / NANOS_PER_SECOND;
    }

    private boolean isGCPause(String line) {
        return ((line.contains(" Pause Initial Mark")) ||
                (line.contains(" Remark ")) ||
                (line.contains(" Pause Young ")) ||
                (line.contains(" Full ")));
    }

    @Override
    public boolean accepts(Diary diary) {
        return (diary.isApplicationStoppedTime() || diary.isApplicationRunningTime()) && diary.isUnifiedLogging();
    }

    @Override
    public void publishTo(JVMEventChannel bus) {
        super.publishTo(bus);
    }

    private void publish(JVMEvent event) {
        super.publish(ChannelName.JVM_EVENT_PARSER_OUTBOX, event);
    }
}
