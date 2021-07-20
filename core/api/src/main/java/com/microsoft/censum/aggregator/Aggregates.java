package com.microsoft.censum.aggregator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by implementations of Aggregator to indicate
 * to the AggregationRegistry the source of the events being aggregated.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * This annotation is used by implementations of Aggregator to indicate
 * to the AggregatorRegistry the source of the events being aggregated.
 * @see AggregatorRegistry#getAggregators(EventSource)
 */
public @interface Aggregates {
    /**
     * Indicate the source of events being aggregated by an Aggregator.
     * @return An array of EventSource
     */
    EventSource[] value();
}
