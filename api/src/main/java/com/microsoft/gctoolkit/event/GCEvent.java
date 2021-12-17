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

    /**
     *
     * @param timeStamp
     * @param gcType
     * @param cause
     * @param duration
     */
    protected GCEvent(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, duration);
        this.gcType = gcType;
        this.cause = cause;
    }

    /**
     *
     * @param timeStamp
     * @param duration
     */
    protected GCEvent(DateTimeStamp timeStamp, double duration) {
        this(timeStamp, GarbageCollectionTypes.Unknown, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    /**
     *
     * @param timeStamp
     * @param cause
     * @param duration
     */
    protected GCEvent(DateTimeStamp timeStamp, GCCause cause, double duration) {
        this(timeStamp, GarbageCollectionTypes.Unknown, cause, duration);
    }

    /**
     *
     * @param timeStamp
     * @param gcType
     * @param duration
     */
    protected GCEvent(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, double duration) {
        this(timeStamp, gcType, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    /**
     *
     * @param cause
     */
    public void setGCCause(GCCause cause) {
        this.cause = cause;
    }

    /**
     *
     * @return the cause of the event.
     */
    public GCCause getGCCause() {
        return this.cause;
    }

    /**
     *
     * @return the type of collection this event represents
     */
    public GarbageCollectionTypes getGarbageCollectionType() {
        return gcType;
    }

    /**
     *
     * @return is a Z cycle
     */
    public boolean isZGC() { return false; }

    /**
     *
     * @return is a young collection
     */
    public boolean isYoung() {
        return false;
    }

    /**
     *
     * @return is a full collection
     */
    public boolean isFull() {
        return false;
    }

    /**
     *
     * @return is a concurrent phase
     */
    public boolean isConcurrent() {
        return false;
    }

    /**
     *
     * @return is a G1 young collection
     */
    public boolean isG1Young() {
        return false;
    }

    /**
     *
     * @return is a G1 mixed collection
     */
    public boolean isG1Mixed() {
        return false;
    }

    /**
     *
     * @return is a G1 concurrent phase
     */
    public boolean isG1Concurrent() {
        return false;
    }

    /**
     *
     * @return triggered by System.gc() or Runtime.gc().
     */
    public boolean isSystemGC() {
        return false;
    }

    /**
     *
     * @return the type is a CMF
     */
    public boolean isConcurrentModeFailure() {
        return false;
    }

    /**
     *
     * @return the type is a CMI
     */
    public boolean isConcurrentModeInterrupted() {
        return false;
    }

    /**
     *
     * Is the distance between x and y larger than the provided threshold.
     * @param x
     * @param y
     * @return
     */
    private static boolean withinThreshold(final double x, final double y) {
        return TIMESTAMP_THRESHOLD > Math.abs(x - y);
    }

    /**
     *
     * @return hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(gcType.getLabel(),getDateTimeStamp().getTimeStamp(),getDuration());
    }

    /**
     *
     * @param o the object to compare to.
     * @return true is this equals o.
     */
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GCEvent gcEvent = (GCEvent) o;
        return gcType.getLabel().equals(gcEvent.gcType.getLabel())
                && withinThreshold(getDateTimeStamp().getTimeStamp(), gcEvent.getDateTimeStamp().getTimeStamp())
                && withinThreshold(getDuration(), gcEvent.getDuration());
    }
}
