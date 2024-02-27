// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.aggregator;

import com.microsoft.gctoolkit.message.ChannelName;

import static com.microsoft.gctoolkit.message.ChannelName.*;

/**
 * EventSource indicates to the source of the GC events
 * that an Aggregator is meant to process.
 * @see Aggregates
 */
public enum EventSource {
    /**
     * Event come from CMS, Parallel or Serial collectors.
     */
    GENERATIONAL(GENERATIONAL_HEAP_PARSER_OUTBOX),
    CMS_UNIFIED(GENERATIONAL_HEAP_PARSER_OUTBOX),

    /**
     * Events that come from the CMS collection cycles
     * @deprecated use the GENERATIONAL event source instead.
     */
    @Deprecated(since="3.0.8", forRemoval=true)
    CMS_PREUNIFIED(CMS_TENURED_POOL_PARSER_OUTBOX),
    /**
     * Events come from the G1 collector.
     */
    G1GC(G1GC_PARSER_OUTBOX),
    /**
     * Events come from the Shenandoah collector.
     */
    SHENANDOAH(SHENANDOAH_PARSER_OUTBOX),
    /**
     * Events come from the ZGC collector.
     */
    ZGC(ZGC_PARSER_OUTBOX),
    /**
     * Events come from the safe points in the GC log, or from a separate safepoint log.
     */
    SAFEPOINT(JVM_EVENT_PARSER_OUTBOX),
    /**
     * Events come from the survivor space.
     */
    SURVIVOR(SURVIVOR_MEMORY_POOL_PARSER_OUTBOX),
    /**
     * Events come from the tenured space.
     */
    TENURED(GENERATIONAL_HEAP_PARSER_OUTBOX),
    /**
     * Events that come from all sources
     */
    JVM(JVM_EVENT_PARSER_OUTBOX);

    ChannelName channel;

    EventSource(ChannelName channel) {
        this.channel = channel;
    }

    public ChannelName toChannel() {
        return channel;
    }
}
