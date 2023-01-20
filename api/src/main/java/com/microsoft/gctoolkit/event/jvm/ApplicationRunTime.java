// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * Application run time between safepoint events.
 */
public class ApplicationRunTime extends JVMEvent {

    /**
     * @param timeStamp start of event
     * @param duration duration of the event
     */
    public ApplicationRunTime(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

}
