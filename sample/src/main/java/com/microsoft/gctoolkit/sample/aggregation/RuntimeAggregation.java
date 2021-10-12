package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * An Aggregation that collates runtime data. This class is meant to be extended by other
 * implementations that need access to runtime data to perform calculations.
 * <p>
 * The following code shows recommended practice for using this Aggregation. The example
 * uses {@code RuntimeAggregation} to calculate the ratio of pause time to runtime duration
 * for a G1GC log.
 * <pre><code>
 * {@literal @}Collates(G1PauseTimeAggregator.class)
 * public abstract class G1PauseTimeAggregation extends RuntimeAggregation {
 *      public abstract void recordPause(double pauseTime);
 * }
 *
 * {@literal @}Aggregates({EventSource.G1GC)
 * public class G1PauseTimeAggregator extends RuntimeAggregator{@literal <}G1PauseTimeAggregation{@literal >} {
 *
 *     public G1PauseTimeAggregator(G1PauseTimeAggregation aggregation) {
 *         super(aggregation);
 *         register(G1RealPause.class, this::process);
 *     }
 *
 *      private void process(G1RealPause event) {
 *          aggregation().recordPause(event.getDuration());
 *      }
 * }
 *
 * public class G1PauseTimeRatio extends G1PauseTimeAggregation {
 *
 *     long totalPauseTime;
 *
 *     public MaxFullGCPauseTime() {}
 *
 *     {@literal @}Override
 *     public void recordPause(double pauseTime) {
 *         totalPauseTime += pauseTime;
 *     }
 *
 *     public double getPauseTimeRatio() {
 *         return getRuntimeDuration() > 0.0 ? totalPauseTime / getRuntimeDuration() : 0.0;
 *     }
 *
 *     {@literal @}Override
 *     public boolean hasWarning() { return false; }
 *
 *     {@literal @}Override
 *     public boolean isEmpty() { return getRuntimeDuration() <= 0.0; }
 * }
 * </code></pre>

 */
public abstract class RuntimeAggregation  implements Aggregation {

    private volatile DateTimeStamp timeOfFirstEvent = null;
    private volatile DateTimeStamp timeOfLastEvent = new DateTimeStamp(0d);

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

        if (timeOfFirstEvent == null && eventTime != null) {
            timeOfFirstEvent = eventTime;
        }

        double eventDuration = !Double.isNaN(duration) ? duration : 0d;

        DateTimeStamp now = eventTime != null
                ? eventTime.add(duration)
                : timeOfLastEvent.add(duration);

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
     * @return The duration of the JVM runtime represented by the log.
     */
    public double getRuntimeDuration() {
        return getTimeOfLastEvent().minus(getTimeOfFirstEvent());
    }
}
