// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.aggregator;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.online.statistics.WelfordVarianceCalculator;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/// An `Aggregation` collates data from an [Aggregator] and may be thought of as a view
/// of the data. An `Aggregation` might collate data into a time series for plotting, or it might
/// summarize the data. Separating the capture of the data, which is the job of the `Aggregator`
/// from how the data is aggregated, which is the job of the `Aggregation`, allows for multiple
/// views of the data to be built for the same `Aggregator`.
///
/// The [Collates] annotation is used to indicate which `Aggregator` an `Aggregation`
/// works with. If an `Aggregation` does not have the `Collates` annotation, the `Aggregation`
/// will not be used.
///
/// An implementation of Aggregation must have a public, no-arg constructor.
///
/// Best practice for creating an `Aggregation` is to create an interface for the methods the
/// `Aggregator` will call.  When a GC log is analyzed, `JVMEvents` are captured by the
/// `Aggregators`. An `Aggregator` extracts data from the JVMEvent and calls the
/// `Aggregation` API to collate the data.
///
/// As an example, say one wants to record the pause times of full GCs. An `Aggregation`
/// could present an API that takes the date/time of the event, the cause of the full GC, and
/// the duration of the full GC. The `Aggregator` could capture G1FullGC events and
/// FullGC events. Which event is actually sent to the `Aggregator` depends on the
/// what kind of GC log file is being parsed.
///
/// The example FullGCAggregator is annotated with the `@Aggregates` annotation, giving
/// the G1GC and Generational as the event source. This lets GCToolKit know that this Aggregator
/// is capturing events from those sources. Notice also that the constructor of FullGCAggregator
/// registers the JVMEvent types that it is interested in, and gives the method to call for that
/// event type. Lastly, the process method extracts the data from the event and calls the
/// FullGCAggregation API.
///
/// The implementation of FullGCAggregation can collate the data however desired. MaxFullGCPauseTime is
/// just one example. Notice that the method to get the maximum pause time is defined in MaxFullGCPauseTime,
/// not in FullGCAggregation. This keeps the FullGCAggregation interface from imposing API that some
/// other view (some other Aggregation) of the data might not want or need.
///
/// ```
///
/// `@`Collates(FullGCAggregator.class)
/// public interface FullGCAggregation extends Aggregation {
///      void recordFullGC(DateTimeStamp timeStamp, GCCause cause, double pauseTime);
/// }
///
/// `@`Aggregates({EventSource.G1GC, EventSource.Generational})
/// public class FullGCAggregator implements Aggregator`<`FullGCAggregation`>` {
///
///     public FullGCAggregator(FullGCAggregation aggregation) {
///         super(aggregation);
///         register(G1FullGC.class, this::process);
///         register(FullGC.class, this::process);
///     }
///
///      private void process(GCEvent event) {
///          aggregation().recordFullGC(event.getDateTimeStamp(), event.getGCCause(), event.getDuration());
///      }
/// }
///
/// public class MaxFullGCPauseTime implements FullGCAggregation {
///     Map`<`GCCause, Double`>` maxPauseTime = new HashMap();
///
///     public MaxFullGCPauseTime() {}
///
///     `@`Override
///     public void recordFullGC(DateTimeStamp timeStamp, GCCause cause, double pauseTime) {
///         maxPauseTime.compute(cause, (k, v) -`>` (v == null) ? pauseTime : Math.max(v, pauseTime));
///     }
///
///     public double getMaxPauseTime(GCCause cause) {
///         return maxPauseTime.get(cause);
///     }
///
///     `@`Override
///     public boolean hasWarning() { return false; }
///
///     `@`Override
///     public boolean isEmpty() { return maxPauseTime.isEmpty(); }
/// }
/// ```
///
/// @see JavaVirtualMachine#getAggregation(Class)
/// @see Collates
public abstract class Aggregation {

    private DateTimeStamp timeOfFirstEvent = null;
    private DateTimeStamp timeOfTermination = DateTimeStamp.baseDate();
    private final WelfordVarianceCalculator varianceCalculator = new WelfordVarianceCalculator();
    private DateTimeStamp timeOfLastSeenEvent = null;

    /// Constructor for the module SPI
    protected Aggregation() {}

    /// @param eventTime of first event seen
    public void timeOfFirstEvent(DateTimeStamp eventTime) {
        this.timeOfFirstEvent = eventTime;
    }

    /// @return time of first event seen
    public DateTimeStamp timeOfFirstEvent() {
        return this.timeOfFirstEvent;
    }

    /// Interface to record the time span of the log
    /// Estimate based on information carried in the JVMTermination event.
    /// @param eventTime - estimate start time of the log.
    public void timeOfTerminationEvent(DateTimeStamp eventTime) {
        this.timeOfTermination = eventTime;
    }

    /// @return the timestamp reported by the JVM termination record if present otherwise the end of the last event.
    public DateTimeStamp timeOfTerminationEvent() {
        return this.timeOfTermination;
    }

    /// Estimates the start time of the log based on the available data.
    ///
    /// If the first event does not have a timestamp, the method returns the time of the first event minus the variance of GC frequency.
    ///
    /// If the timestamp is present and the timestamp of the first event is greater than the variance, the method returns the timestamp minus the variance.
    /// However, if the resulting timestamp is negative, the method returns the time of the first event instead, since a negative timestamp is not possible.
    ///
    /// @return The estimated start time of the log based on the available data
    public DateTimeStamp estimatedStartTime() {
        var sd = Math.sqrt(varianceCalculator.getValue());
        if (!timeOfFirstEvent.hasTimeStamp()) {
            return timeOfFirstEvent.minus(sd);
        }

        final DateTimeStamp estimatedStartTime = timeOfFirstEvent.minus(sd);
        if (!estimatedStartTime.hasTimeStamp()) {
            return timeOfFirstEvent;
        } else {
            return estimatedStartTime;
        }
    }

    /// @return estimate time span represented by the data presented.
    public double estimatedRuntime() {
        return timeOfTermination.minus(estimatedStartTime());
    }

    /// Return true if the Aggregation contains a warning. For example, an Aggregation that
    /// looks at GC Cause might return `true` if it finds a System.gc() call.
    /// @return `true` if the Aggregation contains a warning.
    abstract public boolean hasWarning();

    /// Return `true` if there is no data in the Aggregation.
    /// @return `true` if there is no data in the Aggregation.
    abstract public boolean isEmpty();

    /// Sort if a given Aggregator collates for this aggregation.
    /// @return aggregator
    public Class<? extends Aggregator<?>> collates() {
        return collates(getClass());
    }

    public void updateEventFrequency(JVMEvent event) {
        final DateTimeStamp dateTimeStamp = event.getDateTimeStamp();
        if (timeOfLastSeenEvent == null) {
            timeOfLastSeenEvent = dateTimeStamp;
            return;
        }
        var timeSpan = dateTimeStamp.minus(timeOfLastSeenEvent);
        varianceCalculator.update(timeSpan);
    }

    /// Calculates the aggregator for this aggregation.
    /// @param clazz this Aggregation
    /// @return the Aggregator
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
