package com.microsoft.gctoolkit.message;

/**
 * A list of all of the channel names that are available for publishing on.
 */
public enum ChannelName {

    DATA_SOURCE("DataSource"),
    PARSER_INBOX("PARSER"),
    JVM_EVENT_PARSER_OUTBOX("JVMEventParser"),
    SURVIVOR_MEMORY_POOL_PARSER_OUTBOX("SurvivorMemoryPoolParser"),
    GENERATIONAL_HEAP_PARSER_OUTBOX("GenerationalHeapParser"),
    CMS_TENURED_POOL_PARSER_OUTBOX("CMSTenuredPoolParser"),
    G1GC_PARSER_OUTBOX("G1GCParser"),
    ZGC_PARSER_OUTBOX("ZGCParser"),
    SHENANDOAH_PARSER_OUTBOX("ShenandoahParser");

    private final String name;

    ChannelName(String channelName) {
        this.name = channelName;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }
}
