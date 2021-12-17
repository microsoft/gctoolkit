// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

/**
 * Breakout of the CPU summary to contain gc thread time in the
 * kernel
 * total time for all threads executing GC code
 * wall clock time or pause time of GC cycle.
 */
public class CPUSummary {

    private final double user;
    private final double kernel;
    private final double wallClock;

    /**
     *
     * @param user time
     * @param kernel time
     * @param wallClock time
     */
    public CPUSummary(double user, double kernel, double wallClock) {
        this.user = user;
        this.kernel = kernel;
        this.wallClock = wallClock;
    }

    /**
     *
     * @return user time in seconds.
     */
    public double getUser() {
        return this.user;
    }

    /**qq
     *
     * @return kernel time in seconds.
     */
    public double getKernel() {
        return this.kernel;
    }

    /**
     *
     * @return real time in seconds.
     */
    public double getWallClock() {
        return this.wallClock;
    }

    /**
     *
     * @return string representation of the data.
     */
    public String toString() {
        return "[Times: user=" + user + " sys=" + kernel + ", real=" + wallClock + " secs]";
    }

}
