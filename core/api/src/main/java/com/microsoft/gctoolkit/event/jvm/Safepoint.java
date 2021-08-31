// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;


public class Safepoint extends JVMEvent {

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

    @Override
    public String toString() {
        return this.getVmOperation();
    }

}
