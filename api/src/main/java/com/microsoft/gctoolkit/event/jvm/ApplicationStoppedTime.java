// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ApplicationStoppedTime extends JVMEvent {

    private static final double NOT_REPORTED = -1.0d; // negative times.. don't make sense
    private static final int NO_THREAD_COUNT = -1;

    private static final double NO_TTSP = NOT_REPORTED;
    private final double timeToStopThreads;
    private final VMOperations safePointReason;
    private final boolean gcPause;
    private double timeSinceLastSafepoint = NOT_REPORTED;
    private double cleanupTime = NOT_REPORTED;
    private double atSafepointTime = NOT_REPORTED;
    private double leavingSafepointTime = NOT_REPORTED;
    private int runnableThreads = NO_THREAD_COUNT;
    private int totalThreads = NO_THREAD_COUNT;

    public ApplicationStoppedTime(DateTimeStamp timeStamp, double duration,
                                  double timeToStopThreads, VMOperations safePointReason) {
        this(timeStamp, duration, timeToStopThreads, safePointReason, safePointReason != null && safePointReason.isCollection());
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

    /**
     * Records the two phases that every safepoint line carries.
     */
    public void recordPhases(double timeSinceLast, double atSafepoint) {
        this.timeSinceLastSafepoint = timeSinceLast;
        this.atSafepointTime = atSafepoint;
    }

    public void recordCleanupTime(double cleanup) {
        this.cleanupTime = cleanup;
    }

    public void recordLeavingSafepointTime(double leavingSafepoint) {
        this.leavingSafepointTime = leavingSafepoint;
    }

    public void recordThreadCounts(int runnable, int total) {
        this.runnableThreads = runnable;
        this.totalThreads = total;
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

    public double getTimeSinceLastSafepoint() {
        return timeSinceLastSafepoint;
    }

    public double getCleanupTime() {
        return cleanupTime;
    }

    public double getAtSafepointTime() {
        return atSafepointTime;
    }

    public double getLeavingSafepointTime() {
        return leavingSafepointTime;
    }

    public int getRunnableThreads() {
        return runnableThreads;
    }

    public int getTotalThreads() {
        return totalThreads;
    }

    public boolean hasTimeSinceLastSafepoint() {
        return timeSinceLastSafepoint != NOT_REPORTED;
    }

    public boolean hasCleanupTime() {
        return cleanupTime != NOT_REPORTED;
    }

    public boolean hasAtSafepointTime() {
        return atSafepointTime != NOT_REPORTED;
    }

    public boolean hasLeavingSafepointTime() {
        return leavingSafepointTime != NOT_REPORTED;
    }

    public boolean hasThreadCounts() {
        return totalThreads != NO_THREAD_COUNT;
    }

    public enum VMOperations {
        BulkRevokeBias(false), CleanClassLoaderDataMetaspaces(false), CGC_Operation(true),
        Cleanup(false), CollectForMetadataAllocation(true), Deoptimize(true),
        EnableBiasedLocking(false), Exit(false),
        G1CollectForAllocation(true), G1CollectFull(true), G1Concurrent(true),
        G1TryInitiateConcMark(true), GenCollectForAllocation(true), GenCollectFull(true),
        ICBufferFull(false), RevokeBias(false), SerialCollectForAllocation(true),
        SerialGCCollect(true),
        // The G1 concurrent cycle pauses, JDK 17 onwards
        G1PauseCleanup(true), G1PauseRemark(true),
        // Parallel. The first pair was renamed to the second between JDK 21 and JDK 25
        ParallelGCFailedAllocation(true), ParallelGCSystemGC(true),
        ParallelCollectForAllocation(true), ParallelGCCollect(true),
        // CMS, dropped after JDK 17
        GenCollectFullConcurrent(true),
        // JDK 21 onwards
        CollectForCodeCacheAllocation(true),
        // Non generational ZGC
        XMarkEnd(true), XMarkStart(true), XRelocateStart(true),
        ZMarkEnd(true), ZMarkStart(true), ZRelocateStart(true),
        // Generational ZGC
        ZMarkEndOld(true), ZMarkEndYoung(true), ZMarkStartYoung(true),
        ZMarkStartYoungAndOld(true), ZRelocateStartOld(true), ZRelocateStartYoung(true);

        public static VMOperations fromName(String name) {
            try {
                return valueOf(name);
            } catch (IllegalArgumentException unknownOperation) {
                return null;
            }
        }

        private final boolean collection;

        public boolean isCollection() {
            return collection;
        }

        VMOperations(boolean collection) {
            this.collection = collection;
        }
    }
}
