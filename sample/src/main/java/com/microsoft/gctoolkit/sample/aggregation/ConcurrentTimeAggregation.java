package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;

/**
 * API for an Aggregation that records pause time duration. A
 * PauseTimeAggregation gets its data from a PauseTimeAggregator.
 */
@Collates(ConcurrentTimeAggregator.class)
public class ConcurrentTimeAggregation extends Aggregation {
    /**
     * Record the duration of a pause event. This method is called from PauseTimeAggregator.
     * @param duration The duration (in decimal seconds) of a GC pause.
     */
    public void recordDuration(double duration) {};

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
