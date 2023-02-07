// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

public class UnifiedCountSummary {

    //example: Min: 1, Avg: 17.4, Max: 34, Diff: 33, Sum: 139, Workers: 8

    private final int min;
    private final double average;
    private final int max;
    private final int diff;
    private final int sum;
    private final int threads;

    public UnifiedCountSummary(int min, double average, int max, int diff, int sum, int threads) {
        this.min = min;
        this.average = average;
        this.max = max;
        this.diff = diff;
        this.sum = sum;
        this.threads = threads;
    }

    public int getMin() {
        return min;
    }

    public double getAverage() {
        return average;
    }

    public int getMax() {
        return max;
    }

    public int getDiff() {
        return diff;
    }

    public int getSum() {
        return sum;
    }

    public int getThreads() {
        return this.threads;
    }

}
