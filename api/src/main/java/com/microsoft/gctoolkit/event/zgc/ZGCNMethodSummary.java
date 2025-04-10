package com.microsoft.gctoolkit.event.zgc;

public class ZGCNMethodSummary {
    private final long registered;
    private final long unregistered;

    public ZGCNMethodSummary(long registered, long unregistered) {
        this.registered = registered;
        this.unregistered = unregistered;
    }

    public long getUnregistered() {
        return unregistered;
    }

    public long getRegistered() {
        return registered;
    }
}
