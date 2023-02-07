package com.microsoft.gctoolkit.sample.aggregation;

/**
 * An implementation of PauseTimeAggregation which simply accumulates pause times, and
 * provides methods for getting the total pause time and the percentage of time the
 * application was paused. This is an instance of RuntimeAggregation, which gives us
 * the run time represented by the GC log.
 */
public class PauseTimeSummary extends PauseTimeAggregation {

    private double totalPauseTime;

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void recordPauseDuration(double duration) {
        totalPauseTime += duration;
    }

    /**
     * Get the total amount of time the application was paused for garbage collection.
     * @return The total pause time.
     */
    public double getTotalPauseTime() {
        return totalPauseTime;
    }

    /**
     * Get the amount of time the application was paused as a percentage of total runtime.
     * @return The percentage of time the application was paused.
     */
    public double getPercentPaused() {
        return (totalPauseTime / super.estimatedRuntime()) * 100.0D;
    }
}
