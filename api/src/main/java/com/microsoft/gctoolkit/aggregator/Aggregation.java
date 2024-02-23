// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.aggregator;

import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * An {@code Aggregation} collates data from an {@link Aggregator} and may be thought of as a view
 * of the data. An {@code Aggregation} might collate data into a time series for plotting, or it might
 * summarize the data. Separating the capture of the data, which is the job of the {@code Aggregator}
 * from how the data is aggregated, which is the job of the {@code Aggregation}, allows for multiple
 * views of the data to be built for the same {@code Aggregator}.
 * <p>
 * The {@link Collates} annotation is used to indicate which {@code Aggregator} an {@code Aggregation}
 * works with. If an {@code Aggregation} does not have the {@code Collates} annotation, the {@code Aggregation}
 * will not be used.
 * <p>
 * An implementation of Aggregation must have a public, no-arg constructor.
 * <p>
 * Best practice for creating an {@code Aggregation} is to create an interface for the methods the
 * {@code Aggregator} will call.  When a GC log is analyzed, {@code JVMEvents} are captured by the
 * {@code Aggregators}. An {@code Aggregator} extracts data from the JVMEvent and calls the
 * {@code Aggregation} API to collate the data.
 * <p>
 * As an example, say one wants to record the pause times of full GCs. An {@code Aggregation}
 * could present an API that takes the date/time of the event, the cause of the full GC, and
 * the duration of the full GC. The {@code Aggregator} could capture G1FullGC events and
 * FullGC events. Which event is actually sent to the {@code Aggregator} depends on the
 * what kind of GC log file is being parsed.
 * <p>
 * The example FullGCAggregator is annotated with the {@code @Aggregates} annotation, giving
 * the G1GC and Generational as the event source. This lets GCToolKit know that this Aggregator
 * is capturing events from those sources. Notice also that the constructor of FullGCAggregator
 * registers the JVMEvent types that it is interested in, and gives the method to call for that
 * event type. Lastly, the process method extracts the data from the event and calls the
 * FullGCAggregation API.
 * <p>
 * The implementation of FullGCAggregation can collate the data however desired. MaxFullGCPauseTime is
 * just one example. Notice that the method to get the maximum pause time is defined in MaxFullGCPauseTime,
 * not in FullGCAggregation. This keeps the FullGCAggregation interface from imposing API that some
 * other view (some other Aggregation) of the data might not want or need.
 *
 * <pre><code>
 * {@literal @}Collates(FullGCAggregator.class)
 * public interface FullGCAggregation extends Aggregation {
 *      void recordFullGC(DateTimeStamp timeStamp, GCCause cause, double pauseTime);
 * }
 *
 * {@literal @}Aggregates({EventSource.G1GC, EventSource.Generational})
 * public class FullGCAggregator implements Aggregator{@literal <}FullGCAggregation{@literal >} {
 *
 *     public FullGCAggregator(FullGCAggregation aggregation) {
 *         super(aggregation);
 *         register(G1FullGC.class, this::process);
 *         register(FullGC.class, this::process);
 *     }
 *
 *      private void process(GCEvent event) {
 *          aggregation().recordFullGC(event.getDateTimeStamp(), event.getGCCause(), event.getDuration());
 *      }
 * }
 *
 * public class MaxFullGCPauseTime implements FullGCAggregation {
 *     Map{@literal <}GCCause, Double{@literal >} maxPauseTime = new HashMap();
 *
 *     public MaxFullGCPauseTime() {}
 *
 *     {@literal @}Override
 *     public void recordFullGC(DateTimeStamp timeStamp, GCCause cause, double pauseTime) {
 *         maxPauseTime.compute(cause, (k, v) -{@literal >} (v == null) ? pauseTime : Math.max(v, pauseTime));
 *     }
 *
 *     public double getMaxPauseTime(GCCause cause) {
 *         return maxPauseTime.get(cause);
 *     }
 *
 *     {@literal @}Override
 *     public boolean hasWarning() { return false; }
 *
 *     {@literal @}Override
 *     public boolean isEmpty() { return maxPauseTime.isEmpty(); }
 * }
 * </code></pre>
 *
 * @see JavaVirtualMachine#getAggregation(Class)
 * @see Collates
 */
public abstract class Aggregation {

    private DateTimeStamp timeOfFirstEvent = null;
    private DateTimeStamp timeOfTermination = DateTimeStamp.baseDate();

    /**
     * Constructor for the module SPI
     */
    protected Aggregation() {}

    /**
     * @param eventTime of first event seen
     */
    public void timeOfFirstEvent(DateTimeStamp eventTime) {
        this.timeOfFirstEvent = eventTime;
    }

    /**
     * @return time of first event seen
     */
    public DateTimeStamp timeOfFirstEvent() {
        return this.timeOfFirstEvent;
    }

    /**
     * Interface to record the time span of the log
     * Estimate based on information carried in the JVMTermination event.
     * @param eventTime - estimate start time of the log.
     */
    public void timeOfTerminationEvent(DateTimeStamp eventTime) {
        this.timeOfTermination = eventTime;
    }

    /**
     * @return the timestamp reported by the JVM termination record if present otherwise the end of the last event.
     */
    public DateTimeStamp timeOfTerminationEvent() {
        return this.timeOfTermination;
    }

    /**
     * the 0.25 is a guess as to how far the first event should be from 0.000 seconds before the time of the
     * first event will be considered to be the beginning of the log file.
     *
     * todo: The better way to do this is to calculate the variance in GC frequency and if the gap between 0.000 and
     * the first event exceeds the variance, then the first event would be considered the beginning of the log file.
     * otherwise, the time of the first event should be the time of the first event - the variance.
     *
     * @return estimate of the start of the log using the data presented (most likely is 0.000s)
     */
    public DateTimeStamp estimatedStartTime() {
        if (timeOfFirstEvent.getTimeStamp() / timeOfTermination.getTimeStamp() > 0.25d) {
            return timeOfFirstEvent;
        }

        if ( ! timeOfFirstEvent.hasDateStamp())
            return new DateTimeStamp(0.0d);
        else // this looks after adjusting the date stamp.
            return timeOfFirstEvent.minus(timeOfFirstEvent.getTimeStamp());
    }

    /**
     * @return estimate time span represented by the data presented.
     */
    public double estimatedRuntime() {
        return timeOfTermination.minus(estimatedStartTime());
    }

    /**
     * Return true if the Aggregation contains a warning. For example, an Aggregation that
     * looks at GC Cause might return {@code true} if it finds a System.gc() call.
     * @return {@code true} if the Aggregation contains a warning.
     */
    abstract public boolean hasWarning();

    /**
     * Return {@code true} if there is no data in the Aggregation.
     * @return {@code true} if there is no data in the Aggregation.
     */
    abstract public boolean isEmpty();

    /**
     * Sort if a given Aggregator collates for this aggregation.
      * @return aggregator
     */
    public Class<? extends Aggregator<?>> collates() {
        return collates(getClass());
    }

    /**
     * Calculates the aggregator for this aggregation.
     * @param clazz this Aggregation
     * @return the Aggregator
     */
    private Class<? extends Aggregator<?>> collates(Class<?> clazz) {
        Class<? extends Aggregator<?>> target;
        if (clazz != null && clazz != Aggregation.class) {

            if (clazz.isAnnotationPresent(Collates.class)) {
                Collates collates = clazz.getAnnotation(Collates.class);
                return collates.value();
            }

            Class<?> superClass = clazz.getSuperclass();
            target = collates(superClass);
            if ( target != null)
                return target;

            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> iface : interfaces) {
                target = collates(iface);
                if (target != null)
                    return target;
            }
        }
        return null;
    }
}
