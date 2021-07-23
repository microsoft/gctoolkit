// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.aggregator;

/**
 * EventSource indicates to the source of the GC events
 * that an Aggregator is meant to process.
 * @see Aggregates
 */
public enum EventSource {
    GENERATIONAL,
    G1GC,
    SHENANDOAH,
    ZGC,
    SAFEPOINT,
    SURVIVOR,
    TENURED;
}
