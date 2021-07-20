// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class G1ConcurrentMark extends G1GCConcurrentEvent {

    private double markFromRootsDuration = -1.0d;
    private int activeWorkerThreads = -1;
    private int availableWorkerThreads = -1;
    private double precleanDuration = -1.0d;
    private boolean aborted = false;

    public G1ConcurrentMark(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentMark, GCCause.GCCAUSE_NOT_SET, duration);
    }

    public G1ConcurrentMark(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.G1GCConcurrentMark, cause, duration);
    }

    public void setMarkFromRootsDuration(double duration) {
        this.markFromRootsDuration = duration;
    }

    public double getMarkFromRootsDuration() {
        return markFromRootsDuration;
    }

    public int getActiveWorkerThreads() {
        return activeWorkerThreads;
    }

    public void setActiveWorkerThreads(int activeWorkerThreads) {
        this.activeWorkerThreads = activeWorkerThreads;
    }

    public int getAvailableWorkerThreads() {
        return availableWorkerThreads;
    }

    public void setAvailableWorkerThreads(int availableWorkerThreads) {
        this.availableWorkerThreads = availableWorkerThreads;
    }

    public double getPrecleanDuration() {
        return precleanDuration;
    }

    public void setPrecleanDuration(double duration) {
        this.precleanDuration = duration;
    }

    public void abort() {
        this.aborted = true;
    }

    public boolean isAborted() {
        return this.aborted;
    }
}
