// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * Event to report on time application is running with the collector
 */
public class ApplicationConcurrentTime extends JVMEvent {

    /**
     * @param timeStamp start of event
     * @param duration duration of the event
     */
    public ApplicationConcurrentTime(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

}
