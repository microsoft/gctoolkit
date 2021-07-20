// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event;

public class UnifiedCountSummary {

    //Min: 1, Avg: 17.4, Max: 34, Diff: 33, Sum: 139, Workers: 8

    private int min;
    private double average;
    private int max;
    private int diff;
    private int sum;
    private int threads;

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
