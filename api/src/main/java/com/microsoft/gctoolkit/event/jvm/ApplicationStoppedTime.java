// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ApplicationStoppedTime extends JVMEvent {

    private static final double NO_TTSP = -1.0d; // negative times.. don't make sense
    private final double timeToStopThreads;
    private final VMOperations safePointReason;
    private final boolean gcPause;

    public ApplicationStoppedTime(DateTimeStamp timeStamp, double duration,
                                  double timeToStopThreads, VMOperations safePointReason) {
        this(timeStamp, duration, timeToStopThreads, safePointReason, safePointReason.isCollection());
    }

    public ApplicationStoppedTime(DateTimeStamp timeStamp, double duration, boolean gcPause) {
        this(timeStamp, duration, NO_TTSP, gcPause);
    }

    public ApplicationStoppedTime(DateTimeStamp timeStamp, double duration,
                                  double timeToStopThreads, boolean gcPause) {
        this(timeStamp, duration, timeToStopThreads, null, gcPause);
    }

    private ApplicationStoppedTime(DateTimeStamp timeStamp, double duration,
                                  double timeToStopThreads,
                                  VMOperations safePointReason,
                                  boolean gcPause) {
        super(timeStamp, duration);
        this.timeToStopThreads = timeToStopThreads;
        this.safePointReason = safePointReason;
        this.gcPause = gcPause;
    }

    public double getTimeToStopThreads() {
        return this.timeToStopThreads;
    }

    public boolean hasTTSP() {
        return timeToStopThreads != NO_TTSP;
    }

    public VMOperations getSafePointReason() {
        return safePointReason;
    }

    public boolean isGCPause() {
        if (safePointReason != null)
            return safePointReason.isCollection();
        return this.gcPause;
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
