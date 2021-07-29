// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.aggregator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by implementations of {@link Aggregator} to indicate
 * the source of the events being aggregated. An {@code Aggregator} must include
 * this annotation in order to receive JVM events from the parser.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * This annotation is used by implementations of Aggregator to indicate
 * the source of the events being aggregated.
 */
public @interface Aggregates {
    /**
     * Indicate the source of events being aggregated by an Aggregator.
     * @return An array of EventSource
     */
    EventSource[] value();
}
