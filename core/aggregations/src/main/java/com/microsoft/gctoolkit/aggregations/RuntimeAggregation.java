package com.microsoft.gctoolkit.aggregations;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * An Aggregation that collates runtime data. This class is meant to be extended by other
 * implementations that need access to runtime data to perform calculations. For example,
 * to calculate the ratio of pause time to runtime duration.
 */
public abstract class RuntimeAggregation  implements Aggregation {

    private volatile DateTimeStamp timeOfFirstEvent = null;
    private volatile DateTimeStamp timeOfLastEvent = new DateTimeStamp(0d);

    /**
     * Runtime duration (in decimal seconds) after which we no longer consider the GC log to be a fragment
     */
    private static final double LOG_FRAGMENT_THRESHOLD = 18d;

    /**
     * This class is meant to be extended.
     */
    protected RuntimeAggregation() {}

    /**
     * RuntimeAggregation collates the time of an event and the duration of the event.
     * @param eventTime The time a JVMEvent occurred.
     * @param duration The duration of the JVMEvent.
     */
    public void record(DateTimeStamp eventTime, double duration) {
        duration = !Double.isNaN(duration) ? duration : 0d;

        if (timeOfFirstEvent == null || (eventTime != null && eventTime.before(timeOfFirstEvent))) {
            timeOfFirstEvent = eventTime != null ? eventTime : new DateTimeStamp(0d);
        }

        final DateTimeStamp now =
                eventTime != null ? eventTime.add(duration) : timeOfLastEvent.add(duration);
        if (now.after(timeOfLastEvent)) {
            timeOfLastEvent = now;
        }
    }

    /**
     * Return the time of the first event of the GC log.
     * @return The time of the first event.
     */
    public DateTimeStamp getTimeOfFirstEvent() {
        return timeOfFirstEvent != null ? timeOfFirstEvent : new DateTimeStamp(0.0);
    }

    /**
     * Return the time of the last event of the GC log.
     * Note well! The time of the last event is not the start time of the event, but is the
     * time the event ended (event start time plus the event duration).
     * @return The time of the last event.
     */
    public DateTimeStamp getTimeOfLastEvent() {
        return timeOfLastEvent != null ? timeOfLastEvent : getTimeOfFirstEvent();
    }

    /**
     * Return the duration of the GC log. Fundamentally, this is the difference between the
     * time of the last event and the time of the first event.
     * @return The time of the first event.
     */
    public double getRuntimeDuration() {
        double duration = getTimeOfLastEvent().minus(getTimeOfFirstEvent());
        boolean isLogFragment = duration < LOG_FRAGMENT_THRESHOLD;
        return !isLogFragment
                ? duration
                : getTimeOfLastEvent().getTimeStamp();
    }
}
