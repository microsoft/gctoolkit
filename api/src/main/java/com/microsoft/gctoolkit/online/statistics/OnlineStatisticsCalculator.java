package com.microsoft.gctoolkit.online.statistics;

public interface OnlineStatisticsCalculator {


    /**
     * Updates the statistics calculation with the given value.
     * <p>
     * For example, if the statistics calculation is a mean, this method would update the mean with the given value.
     *
     * @param sampleValue the value to be added to the statistics calculation
     */
    void update(double sampleValue);

    /**
     * @return the value of the statistics calculation
     */
    double getValue();
}
