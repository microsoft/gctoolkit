// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * Concurrent phase
 *
 */

public class G1ConcurrentMark extends G1GCConcurrentEvent {

    private double markFromRootsDuration = -1.0d;
    private int activeWorkerThreads = -1;
    private int availableWorkerThreads = -1;
    private double precleanDuration = -1.0d;
    private boolean aborted = false;

    /**
     * @param timeStamp time of the event
     * @param duration duration of the event
     */
    public G1ConcurrentMark(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.G1GCConcurrentMark, GCCause.GCCAUSE_NOT_SET, duration);
    }

    /**
     * @param timeStamp time of the event
     * @param cause reason to trigger the event
     * @param duration duration of the event
     */
    public G1ConcurrentMark(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.G1GCConcurrentMark, cause, duration);
    }

    /**
     * @param duration for the mark from roots step
     */
    public void setMarkFromRootsDuration(double duration) {
        this.markFromRootsDuration = duration;
    }

    /**
     * @return mark from roots duration
     */
    public double getMarkFromRootsDuration() {
        return markFromRootsDuration;
    }

    /**
     * @return number of active workers
     */
    public int getActiveWorkerThreads() {
        return activeWorkerThreads;
    }

    /**
     * @param activeWorkerThreads number of active workers
     */
    public void setActiveWorkerThreads(int activeWorkerThreads) {
        this.activeWorkerThreads = activeWorkerThreads;
    }

    /**
     * @return size of worker pool
     */
    public int getAvailableWorkerThreads() {
        return availableWorkerThreads;
    }

    /**
     * @param availableWorkerThreads set the work pool size
     */
    public void setAvailableWorkerThreads(int availableWorkerThreads) {
        this.availableWorkerThreads = availableWorkerThreads;
    }

    /**
     * @return preclean duration
     */
    public double getPrecleanDuration() {
        return precleanDuration;
    }

    /**
     * @param duration set preclean duration
     */
    public void setPrecleanDuration(double duration) {
        this.precleanDuration = duration;
    }

    /**
     * was preclean aborted due to occupancy threshold
     */
    public void abort() {
        this.aborted = true;
    }

    /**
     * @return if preclean was aborted due to occupancy threshold
     */
    public boolean isAborted() {
        return this.aborted;
    }
}
