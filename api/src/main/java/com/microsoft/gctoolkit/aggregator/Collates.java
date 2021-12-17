// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.aggregator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An implementation of an Aggregation collates data from an Aggregator.
 * This annotation is used on an Aggregation to tell GCToolKit which
 * Aggregator produces the data the Aggregation collates.
 * <p>
 * In this example, there is a PauseTimeAggegator which receives pause time events
 * from GCToolKit. The PauseTimeAggregator extracts the cause, the time the event
 * occurred, and the duration of the event and calls the PauseTimeAggregation method.
 * <p>
 * An implementation of PauseTimeAggregation is able to collate the data in a way
 * that makes sense for how the data is to be viewed. For example: one may want to
 * summarize the data as a histogram, or one may want to collect a series of data
 * for plotting.
 * <pre><code>
 * {@literal @}Aggregates({EventSource.G1GC. EventSource.Generational, EventSource.ZGC, EventSource.Shenandoah})
 * public class PauseTimeAggregator extends Aggregator{@literal <}PauseTimeAggregation{@literal >} {
 *     ...
 * }
 * {@literal @}Collates(PauseTimeAggregator.class)
 * public interface PauseTimeAggregation extends Aggregation {
 *     ...
 * }
 *
 * public class PauseTimeSummary implements PauseTimeAggregation {
 *     ...
 * }
 *
 * public class PauseTimeGraph implements PauseTimeAggregation {
 *     ...
 * }
 * </code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Collates {
    /**
     *
     * @return the Aggregator that collates.
     */
    Class<? extends Aggregator<?>> value();
}
