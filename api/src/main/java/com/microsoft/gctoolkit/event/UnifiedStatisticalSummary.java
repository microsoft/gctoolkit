// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

public class UnifiedStatisticalSummary extends StatisticalSummary {

    private final int threads;

    public UnifiedStatisticalSummary(double min, double average, double max, double diff, double sum, int threads) {
        super(min, average, max, diff, sum);
        this.threads = threads;
    }

    public int getThreads() {
        return this.threads;
    }
}
