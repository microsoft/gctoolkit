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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JVMEventParser extends PreUnifiedGCLogParser implements JVMPatterns {

    private static final double GCPAUSE_TIME_NOT_SET = -1.0; // a value that doesn't make sense
    private static final Logger LOGGER = Logger.getLogger(JVMEventParser.class.getName());
    private final Collection<SafePointData> safePoints = new ArrayList<>();
    private DateTimeStamp timeStamp = new DateTimeStamp(0.0d);
    private boolean lastEventWasGC = false;
    private double gcPauseTime = GCPAUSE_TIME_NOT_SET;

    public JVMEventParser() {}

    @Override
    public Set<EventSource> eventsProduced() {
        return Set.of(EventSource.JVM);
    }

    public String getName() {
        return "JVMEventParser";
    }

    /**
     * Application stopped time records prior to 7.0 are not timestamped.
     * In those cases one can use Application Run time to reconstruct the timings
     * If Application run time is missing, collect records and estimate run times
     * based on a change in the value of getClock();
     */
    @Override
    protected void process(String line) {

        GCLogTrace trace = null;

        try {
            //todo: unified safepointing here???
            if ((trace = APPLICATION_STOP_TIME.parse(line)) != null) {
                if (lastEventWasGC) {
                    // can estimate TTSP
                    double duration = trace.getDoubleGroup(3);
                    publish(new ApplicationStoppedTime(trace.getDateTimeStamp(), duration, duration - gcPauseTime, lastEventWasGC));
                    lastEventWasGC = false;
                    gcPauseTime = GCPAUSE_TIME_NOT_SET;
                } else {
                    publish(new ApplicationStoppedTime(trace.getDateTimeStamp(), trace.getDoubleGroup(3), lastEventWasGC));
                }
            } else if ((trace = APPLICATION_STOP_TIME_WITH_STOPPING_TIME.parse(line)) != null) {
                publish(new ApplicationStoppedTime(trace.getDateTimeStamp(), trace.getDoubleGroup(3), trace.getDoubleGroup(4), lastEventWasGC));
                lastEventWasGC = false;
                gcPauseTime = GCPAUSE_TIME_NOT_SET;
            } else if ((trace = APPLICATION_TIME.parse(line)) != null) {
                publish(new ApplicationConcurrentTime(trace.getDateTimeStamp(), trace.getDoubleGroup(3)));
                lastEventWasGC = false;
            } else if ((trace = SIMPLE_APPLICATION_STOP_TIME.parse(line)) != null) {
                safePoints.add(new StoppedTime(trace.getDoubleGroup(1), safePoints.isEmpty()));
            } else if ((trace = SIMPLE_APPLICATION_TIME.parse(line)) != null) {
                safePoints.add(new ConcurrentTime(trace.getDoubleGroup(3)));
            } else if ((trace = GC_PAUSE_CLAUSE.parse(line)) != null) {
                gcPauseTime = trace.getPauseTime();
                lastEventWasGC = true;
            } else if ((trace = TLAB_START.parse(line)) != null) {
                extractTLAB(trace, 2);
            } else if ((trace = TLAB_CONT.parse(line)) != null) {
                extractTLAB(trace, 0);
            } else if ((trace = TLAB_TOTALS.parse(line)) != null) {
                extractTLABSummary(trace);
            } else if (line.equals(END_OF_DATA_SENTINEL)) {
                // TODO: #154  else if (line.equals(END_OF_DATA_SENTINEL)|| (JVM_EXIT.parse(line) != null)) {
                // if we see "^heap" then we're at the end of the log
                // at issue is if logs have been concatenated then we're not at the end and we
                // shouldn't release the
                drainSafePoints();
                publish(new JVMTermination(getClock(),diary.getTimeOfFirstEvent()));
            } else if (getClock().getTimeStamp() > timeStamp.getTimeStamp()) {
                drainSafePoints();
                timeStamp = getClock();
            }

        } catch (Throwable t) {
            LOGGER.log(Level.FINE, "Missed: {0}", line);
        }
    }

    private void extractTLABSummary(GCLogTrace trace) {
        trace.notYetImplemented();
    }

    @SuppressWarnings("unused")
    private void extractTLAB(GCLogTrace trace, int offset) {
        String gcThreadId = trace.getGroup(1 + offset);
        int id = trace.getIntegerGroup(2 + offset);
        int desiredSize = trace.getIntegerGroup(3 + offset);
        int slowAllocs = trace.getIntegerGroup(4 + offset);
        int refillWaste = trace.getIntegerGroup(5 + offset);
        double allocFraction = trace.getDoubleGroup(6 + offset);
        int unknownKBField = trace.getIntegerGroup(7 + offset);
        int refills = trace.getIntegerGroup(8 + offset);
        double wastePercent = trace.getDoubleGroup(9 + offset);
        int gcUknownField = trace.getIntegerGroup(10 + offset);
        int slowUnknown = trace.getIntegerGroup(11 + offset);
        int fastUnknown = trace.getIntegerGroup(12 + offset);
    }

    //todo: should actually use the timings in the log but this is ok for now.
    private void drainSafePoints() {
        double interval = (getClock().getTimeStamp() - (timeStamp.getTimeStamp())) / (safePoints.size() + 1);
        double timeValue = getClock().getTimeStamp() + interval;
        for (SafePointData safePointData : safePoints) {
            publish(safePointData.complete(new DateTimeStamp(timeValue)));
            timeValue += interval;
        }
        safePoints.clear();
    }

    private abstract static class SafePointData {
        double duration;

        abstract JVMEvent complete(DateTimeStamp timeStamp1);

    }

    private static class StoppedTime extends SafePointData {

        boolean gcInduced;

        StoppedTime(double timing, boolean gc) {
            duration = timing;
            gcInduced = gc;
        }

        JVMEvent complete(DateTimeStamp dateTimeStamp) {
            return new ApplicationStoppedTime(dateTimeStamp, duration, gcInduced);
        }
    }

    private static class ConcurrentTime extends SafePointData {

        ConcurrentTime(double timing) {
            duration = timing;
        }

        JVMEvent complete(DateTimeStamp dateTimeStamp) {
            return new ApplicationConcurrentTime(dateTimeStamp, duration);
        }
    }

    @Override
    public boolean accepts(Diary diary) {
        return (diary.isTLABData() || diary.isApplicationStoppedTime() || diary.isApplicationRunningTime()) && ! diary.isUnifiedLogging();
    }

    @Override
    public void publishTo(JVMEventChannel bus) {
        super.publishTo(bus);
    }

    private void publish(JVMEvent event) {
        super.publish(ChannelName.JVM_EVENT_PARSER_OUTBOX, event);
    }
}
