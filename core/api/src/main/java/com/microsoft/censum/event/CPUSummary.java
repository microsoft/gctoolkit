// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event;

public class CPUSummary {

    private final double user;
    private final double kernel;
    private final double wallClock;

    public CPUSummary(double user, double kernel, double wallClock) {
        this.user = user;
        this.kernel = kernel;
        this.wallClock = wallClock;
    }

    public double getUser() {
        return this.user;
    }

    public double getKernel() {
        return this.kernel;
    }

    public double getWallClock() {
        return this.wallClock;
    }

    public String toString() {
        return "[Times: user=" + user + " sys=" + kernel + ", real=" + wallClock + " secs]";
    }

}
