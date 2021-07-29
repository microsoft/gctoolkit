// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ApplicationStoppedTime extends JVMEvent {

    private static final double NO_TTSP = -1.0d; // negative times.. don't make sense
    private boolean gcPause;
    private double timeToStopThreads;
    private VMOperations safePointReason = null;

    public ApplicationStoppedTime(DateTimeStamp timeStamp, double duration, double timeToStopThreads, VMOperations safePointReason) {
        this(timeStamp, duration, timeToStopThreads, safePointReason.isCollection());
        this.safePointReason = safePointReason;
    }

    public ApplicationStoppedTime(DateTimeStamp timeStamp, double duration, boolean gcPause) {
        this(timeStamp, duration, NO_TTSP, gcPause);
    }

    public ApplicationStoppedTime(DateTimeStamp timeStamp, double duration, double timeToStopThreads, boolean gcPause) {
        super(timeStamp, duration);
        this.timeToStopThreads = timeToStopThreads;
        this.gcPause = gcPause;
    }

    public VMOperations getSafePointReason() {
        return safePointReason;
    }

    public double getTimeToStopThreads() {
        return this.timeToStopThreads;
    }

    public boolean isGCPause() {
        if (safePointReason != null)
            return safePointReason.isCollection();
        return this.gcPause;
    }

    public boolean hasTTSP() {
        return timeToStopThreads != NO_TTSP;
    }

    public enum VMOperations {
        BulkRevokeBias(false), CGC_Operation(true), Cleanup(false),
        Deoptimize(true), EnableBiasedLocking(false), Exit(false),
        G1CollectForAllocation(true), RevokeBias(false);

        private final boolean collection;

        public boolean isCollection() {
            return collection;
        }

        VMOperations(boolean collection) {
            this.collection = collection;
        }
    }
}
