// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event;

public class UnifiedStatisticalSummary extends StatisticalSummary {

    private int threads;

    public UnifiedStatisticalSummary(double min, double average, double max, double diff, double sum, int threads) {
        super(min, average, max, diff, sum);
        this.threads = threads;
    }

    public int getThreads() {
        return this.threads;
    }
}
