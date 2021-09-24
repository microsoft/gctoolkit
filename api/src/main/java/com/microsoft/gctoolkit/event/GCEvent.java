// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.Objects;

/**
 * A GCEvent is something that happens in the GC sub system that is captured in
 * the log, e.g. a Full GC
 * <p>
 * A GCEvent has meta data which it expects to be overridden, e.g. whether or
 * not it was a young collection
 */
public abstract class GCEvent extends JVMEvent {

    static final double TIMESTAMP_THRESHOLD = 1.0E-6;
    private final GarbageCollectionTypes gcType;
    private GCCause cause;

    protected GCEvent(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, duration);
        this.gcType = gcType;
        this.cause = cause;
    }

    protected GCEvent(DateTimeStamp timeStamp, double duration) {
        this(timeStamp, GarbageCollectionTypes.Unknown, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    protected GCEvent(DateTimeStamp timeStamp, GCCause cause, double duration) {
        this(timeStamp, GarbageCollectionTypes.Unknown, cause, duration);
    }

    protected GCEvent(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, double duration) {
        this(timeStamp, gcType, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    public void setGCCause(GCCause cause) {
        this.cause = cause;
    }

    public GCCause getGCCause() {
        return this.cause;
    }

    public GarbageCollectionTypes getGarbageCollectionType() {
        return gcType;
    }

    public boolean isZGC() { return false; }

    public boolean isYoung() {
        return false;
    }

    public boolean isFull() {
        return false;
    }

    public boolean isConcurrent() {
        return false;
    }

    public boolean isG1Young() {
        return false;
    }

    public boolean isG1Mixed() {
        return false;
    }

    public boolean isG1Concurrent() {
        return false;
    }

    public boolean isSystemGC() {
        return false;
    }

    public boolean isConcurrentModeFailure() {
        return false;
    }

    public boolean isConcurrentModeInterrupted() {
        return false;
    }

    private static boolean withinThreshold(final double x, final double y) {
        return TIMESTAMP_THRESHOLD > Math.abs(x - y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gcType.getLabel(),getDateTimeStamp().getTimeStamp(),getDuration());
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GCEvent gcEvent = (GCEvent) o;
        return gcType.getLabel().equals(gcEvent.gcType.getLabel())
                && withinThreshold(getDateTimeStamp().getTimeStamp(), gcEvent.getDateTimeStamp().getTimeStamp())
                && withinThreshold(getDuration(), gcEvent.getDuration());
    }
}
