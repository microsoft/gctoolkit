// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;


public class Safepoint extends JVMEvent {

    private static final double NOT_REPORTED = -1.0d; // negative times.. don't make sense
    private static final int NO_THREAD_COUNT = -1;

    private final String vmOperation;
    private int totalNumberOfApplicationThreads;
    private int initiallyRunning;
    private int waitingToBlock;

    private int spinDuration;
    private int blockDuration;
    private int syncDuration;
    private int cleanupDuration;
    private int vmopDuration;

    private int pageTrapCount;

    //unified log fields
    private double timeSinceLastSafepoint = NOT_REPORTED;
    private double cleanupPhaseDuration = NOT_REPORTED;
    private double reachingSafepointDuration = NOT_REPORTED;
    private double atSafepointDuration = NOT_REPORTED;
    private double leavingSafepointDuration = NOT_REPORTED;
    private int runnableThreads = NO_THREAD_COUNT;
    private int totalThreads = NO_THREAD_COUNT;

    public Safepoint(String vmOperationName, DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
        this.vmOperation = vmOperationName;
    }

    //[threads: total initially_running wait_to_block]
    public void recordThreadCounts(int totalThreads, int initiallyRunning, int waitingToBlock) {
        this.totalNumberOfApplicationThreads = totalThreads;
        this.initiallyRunning = initiallyRunning;
        this.waitingToBlock = waitingToBlock;
    }

    //[time: spin block sync cleanup vmop]
    public void recordDurations(int spinDuration, int blockDuration, int syncDuration, int cleanupDuration, int vmopDuration) {
        this.spinDuration = spinDuration;
        this.blockDuration = blockDuration;
        this.syncDuration = syncDuration;
        this.cleanupDuration = cleanupDuration;
        this.vmopDuration = vmopDuration;
    }

    // page_trap_count
    public void recordPageTrapCount(int pageTrapCount) {
        this.pageTrapCount = pageTrapCount;
    }

    /**
     * Records the phases that every safepoint line carries.
     */
    public void recordPhases(double timeSinceLast, double reachingSafepoint, double atSafepoint) {
        this.timeSinceLastSafepoint = timeSinceLast;
        this.reachingSafepointDuration = reachingSafepoint;
        this.atSafepointDuration = atSafepoint;
    }

    public void recordCleanupPhaseDuration(double cleanup) {
        this.cleanupPhaseDuration = cleanup;
    }

    public void recordLeavingSafepointDuration(double leavingSafepoint) {
        this.leavingSafepointDuration = leavingSafepoint;
    }

    public void recordThreadsAtSafepoint(int runnable, int total) {
        this.runnableThreads = runnable;
        this.totalThreads = total;
    }

    public String getVmOperation() {
        return vmOperation;
    }

    public int getTotalNumberOfApplicationThreads() {
        return totalNumberOfApplicationThreads;
    }

    public int getInitiallyRunning() {
        return initiallyRunning;
    }

    public int getWaitingToBlock() {
        return waitingToBlock;
    }

    public int getSpinDuration() {
        return spinDuration;
    }

    public int getBlockDuration() {
        return blockDuration;
    }

    public int getSyncDuration() {
        return syncDuration;
    }

    public int getCleanupDuration() {
        return cleanupDuration;
    }

    public int getVmopDuration() {
        return vmopDuration;
    }

    public int getPageTrapCount() {
        return pageTrapCount;
    }

    public double getTimeSinceLastSafepoint() {
        return timeSinceLastSafepoint;
    }

    public double getReachingSafepointDuration() {
        return reachingSafepointDuration;
    }

    public double getCleanupPhaseDuration() {
        return cleanupPhaseDuration;
    }

    public double getAtSafepointDuration() {
        return atSafepointDuration;
    }

    public double getLeavingSafepointDuration() {
        return leavingSafepointDuration;
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

    public boolean hasReachingSafepointDuration() {
        return reachingSafepointDuration != NOT_REPORTED;
    }

    public boolean hasCleanupPhaseDuration() {
        return cleanupPhaseDuration != NOT_REPORTED;
    }

    public boolean hasAtSafepointDuration() {
        return atSafepointDuration != NOT_REPORTED;
    }

    public boolean hasLeavingSafepointDuration() {
        return leavingSafepointDuration != NOT_REPORTED;
    }

    public boolean hasRunnableThreads() {
        return runnableThreads != NO_THREAD_COUNT;
    }

    public boolean hasTotalThreads() {
        return totalThreads != NO_THREAD_COUNT;
    }

    @Override
    public String toString() {
        return this.getVmOperation();
    }

}
