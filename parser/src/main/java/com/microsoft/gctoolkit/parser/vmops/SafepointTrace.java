// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;


import com.microsoft.gctoolkit.event.jvm.Safepoint;
import com.microsoft.gctoolkit.parser.AbstractLogTrace;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.regex.Matcher;

public class SafepointTrace extends AbstractLogTrace {

    public SafepointTrace(Matcher matcher) {
        super(matcher);
    }

    public Safepoint toSafepoint() {
        Safepoint safepoint = new Safepoint(getVMOP(), getDateTimeStamp(), getDuration());
        safepoint.recordThreadCounts(totalThreads(), initiallyRunningThreads(), waitingToBlockThreads());
        safepoint.recordDurations(spinTime(), blockTime(), syncTime(), cleanupTime(), vmopTime());
        safepoint.recordPageTrapCount(getTrapCount());
        return safepoint;
    }

    public String getVMOP() {
        return super.getGroup(VMOP);
    }

    public DateTimeStamp getDateTimeStamp() {
        return new DateTimeStamp(getTimeStampGroup());
    }

    public double getDuration() {
        return (spinTime() + blockTime() + syncTime() + cleanupTime() + vmopTime()) / 1000.0d;
    }

    public int totalThreads() {
        return getIntegerGroup(TOTAL_THREADS);
    }

    public int initiallyRunningThreads() {
        return getIntegerGroup(INITIALLY_RUNNING_THREADS);
    }

    public int waitingToBlockThreads() {
        return getIntegerGroup(WAITING_TO_BLOCK);
    }

    public int spinTime() {
        return getIntegerGroup(SPIN_TIME);
    }

    public int blockTime() {
        return getIntegerGroup(BLOCK_TIME);
    }

    public int syncTime() {
        return getIntegerGroup(SYNC_TIME);
    }

    public int cleanupTime() {
        return getIntegerGroup(CLEANUP_TIME);
    }

    public int vmopTime() {
        return getIntegerGroup(VMOP_TIME);
    }

    public int getTrapCount() {
        return getIntegerGroup(TRAP_COUNT);
    }

    private double getTimeStampGroup() {
        return getDoubleGroup(TIME_STAMP);
    }

    private final int TIME_STAMP = 1;
    private final int VMOP = 2;
    private final int TOTAL_THREADS = 3;
    private final int INITIALLY_RUNNING_THREADS = 4;
    private final int WAITING_TO_BLOCK = 5;
    private final int SPIN_TIME = 6;
    private final int BLOCK_TIME = 7;
    private final int SYNC_TIME = 8;
    private final int CLEANUP_TIME = 9;
    private final int VMOP_TIME = 10;
    private final int TRAP_COUNT = 11;

}

/*
         vmop                    [threads: total initially_running wait_to_block]    [time: spin block sync cleanup vmop] page_trap_count
0.099: Deoptimize                       [       9          0              0    ]      [     0     0     0     0     0    ]  0
1.474: no vm operation                  [      13          0              0    ]      [     0     0     0     0     0    ]  0
4.088: EnableBiasedLocking              [      13          0              0    ]      [     0     0     0     0     0    ]  0
19.135: GenCollectForAllocation          [      14          0              0    ]      [     0     0     0     0     5    ]  0
19.142: RevokeBias                       [      14          0              1    ]      [     0     0     0     0     0    ]  0
36.464: BulkRevokeBias                   [      20          1              1    ]      [     0     0     0     0     0    ]  1
37.557: ThreadDump                       [      22          0              1    ]      [     0     0     0     0     0    ]  0
37.566: FindDeadlocks                    [      22          0              0    ]      [     0     0     0     0     0    ]  0
Polling page always armed
ThreadDump                       159
FindDeadlocks                     16
Deoptimize                        16
GenCollectForAllocation          202
EnableBiasedLocking                1
RevokeBias                        87
BulkRevokeBias                     7
Exit                               1
    0 VM operations coalesced during safepoint
Maximum sync time      0 ms
 */
