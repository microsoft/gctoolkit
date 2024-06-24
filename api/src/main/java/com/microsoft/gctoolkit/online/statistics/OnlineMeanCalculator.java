package com.microsoft.gctoolkit.online.statistics;

public class OnlineMeanCalculator implements OnlineStatisticsCalculator {
    private int numSamples = 0;
    private double mean = 0.0;

    public void update(double sampleValue) {
        numSamples++;
        mean += (sampleValue - mean) / numSamples;
    }

    @Override
    public double getValue() {
        return mean;
    }
}
