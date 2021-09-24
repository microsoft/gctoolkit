// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

public class StatisticalSummary {

    public static final double UNDEFINED = -1.0d;

    private final double min;
    private final double average;
    private final double max;
    private final double diff;
    private final double sum;

    public StatisticalSummary(double min, double average, double max, double diff, double sum) {
        this.min = min;
        this.average = average;
        this.max = max;
        this.diff = diff;
        this.sum = sum;
    }

    public double getMin() {
        return min;
    }

    public double getAverage() {
        return average;
    }

    public double getMax() {
        return max;
    }

    public double getDiff() {
        return diff;
    }

    public double getSum() {
        return sum;
    }
}
