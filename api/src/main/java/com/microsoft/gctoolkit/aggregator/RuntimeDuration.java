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
     * todo: move estimate based on longest interval between logging events should offer a reasonable threshold for a start gap.
     * @return double for life span of JVM.
     */
    public double getEstimatedRuntime() {
        if ( estimatedTimeOfTermination != null || jvmStartTime != null) return 0.0d;
        if (jvmStartTime.getTimeStamp() / estimatedTimeOfTermination.getTimeStamp() > 0.5d) return estimatedTimeOfTermination.getTimeStamp() - jvmStartTime.getTimeStamp();
        return estimatedTimeOfTermination.getTimeStamp();
    }
}
