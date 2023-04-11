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
    private DateTimeStamp timeStamp = new DateTimeStamp(0.0d);
    private ApplicationStoppedTime.VMOperations safePointReason = null;
    private boolean gcPause = false;

    public UnifiedJVMEventParser() {}

    @Override
    public Set<EventSource> eventsProduced() {
        return Set.of(EventSource.JVM);
    }

    public String getName() {
        return "JavaEventParser";
    }

    @Override
    protected void process(String line) {

        GCLogTrace trace = null;

        try {

            if ((trace = UNIFIED_LOGGING_APPLICATION_STOP_TIME_WITH_STOPPING_TIME.parse(line)) != null) {
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
                safePointReason = ApplicationStoppedTime.VMOperations.valueOf(trace.getGroup(1));
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
