package com.microsoft.gctoolkit.aggregator;

import com.microsoft.gctoolkit.time.DateTimeStamp;


public class RuntimeDuration {

    private DateTimeStamp jvmStartTime  = null;
    private DateTimeStamp estimatedTimeOfTermination;

    public RuntimeDuration() {}

    public void update(DateTimeStamp timeStamp, double duration) {
        if ( jvmStartTime == null)
            jvmStartTime = timeStamp;
        estimatedTimeOfTermination = timeStamp.add(duration);
    }

    public DateTimeStamp getJVMStartTime() {
        return jvmStartTime;
    }

    public DateTimeStamp getEstimatedTimeOfTermination() {
        return this.estimatedTimeOfTermination;
    }

    /**
     * Add more logic here to use 0.000 when start time is very close to 0.000
     * @return double for life span of JVM.
     */
    public double getEstimatedRuntime() {
        if ( estimatedTimeOfTermination != null && jvmStartTime != null)
            return estimatedTimeOfTermination.getTimeStamp() - jvmStartTime.getTimeStamp();
        return -1.0d;
    }
}
