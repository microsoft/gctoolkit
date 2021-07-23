// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.aggregator;

/**
 * EventSource indicates to the source of the GC events
 * that an Aggregator is meant to process.
 * @see Aggregates
 */
public enum EventSource {
    /**
     * Event come from CMS, Parallel or Serial collectors.
     */
    GENERATIONAL,
    /**
     * Events come from the G1 collector.
     */
    G1GC,
    /**
     * Events come from the Shenandoah collector.
     */
    SHENANDOAH,
    /**
     * Events come from the ZGC collector.
     */
    ZGC,
    /**
     * Events come from the safe points in the GC log, or from a separate safepoint log.
     */
    SAFEPOINT,
    /**
     * Events come from the survivor space.
     */
    SURVIVOR,
    /**
     * Events come from the tenured space.
     */
    TENURED;
}
