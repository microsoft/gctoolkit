package com.microsoft.gctoolkit.online.statistics;

public class WelfordVarianceCalculator implements OnlineStatisticsCalculator {
    private int numSamples = 0;
    private double m2 = 0.0;
    private final OnlineMeanCalculator onlineMeanCalculator = new OnlineMeanCalculator();

    @Override
    public void update(double sampleValue) {
        double oldMean = onlineMeanCalculator.getValue();

        onlineMeanCalculator.update(sampleValue);
        numSamples++;

        double newMean = onlineMeanCalculator.getValue();

        m2 += (sampleValue - oldMean) * (sampleValue - newMean);
    }

    @Override
    public double getValue() throws NotEnoughSampleException {
        if (numSamples < 2) {
            throw new NotEnoughSampleException("Variance requires at least 2 samples.");
        }
        return m2 / (numSamples - 1);
    }
}
