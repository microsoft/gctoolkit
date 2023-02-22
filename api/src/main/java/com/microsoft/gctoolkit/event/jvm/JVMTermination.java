// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

public class JVMTermination extends JVMEvent {

    private DateTimeStamp timeOfFirstEvent;

    /**
     * estimatedTimeOfJVMTermination is the time JVM terminated or the last JVMEvent was seen. For the last
     * JVMEvent, any pause time will have been added to timeStamp. The duration normally means the duration
     * of the event. In this case duration doesn't refer to the time the JVM took to shutdown but instead
     * refers to the total running time of the JVM. This is a recognized change in the definition of duration.
     *
     * The start variable is the timeOfFirstEvent on the first event seen in the data source.
     * Not starting from 0.000 secs should not add much noise to any of the subsequent calculations.
     *
     * @param estimatedTimeOfJVMTermination time of JVM termination message or end of last event seen
     * @param timeOfFirstEvent time of first message in the GC log.
     */
    public JVMTermination(DateTimeStamp estimatedTimeOfJVMTermination, DateTimeStamp timeOfFirstEvent) {
        super(estimatedTimeOfJVMTermination, 0.0d);
        this.timeOfFirstEvent = timeOfFirstEvent;
    }

    public DateTimeStamp getTimeOfTerminationEvent() {
        return super.getDateTimeStamp();
    }

    public DateTimeStamp getTimeOfFirstEvent() {
        return this.timeOfFirstEvent;
    }

}
