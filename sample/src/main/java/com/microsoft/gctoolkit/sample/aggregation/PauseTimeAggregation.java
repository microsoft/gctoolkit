package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;

/**
 * API for an Aggregation that records pause time duration. A
 * PauseTimeAggregation gets its data from a PauseTimeAggregator.
 */
@Collates(PauseTimeAggregator.class)
public abstract class PauseTimeAggregation extends Aggregation {
    /**
     * Record the duration of a pause event. This method is called from PauseTimeAggregator.
     * @param duration The duration (in decimal seconds) of a GC pause.
     */
    public abstract void recordPauseDuration(double duration);
}
