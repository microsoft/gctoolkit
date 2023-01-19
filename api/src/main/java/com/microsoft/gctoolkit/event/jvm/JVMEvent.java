// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * This is the base class for all JVM events created by the parser.
 */
public abstract class JVMEvent {

    private final DateTimeStamp timeStamp;
    private final double duration;

    /**
     * All events have a date/time stamp of when the event occurred, and a duration.
     * @param timeStamp The date/time stamp of when the event occurred.
     * @param duration The duration of the event in decimal seconds.
     */
    public JVMEvent(DateTimeStamp timeStamp, double duration) {
        this.timeStamp = timeStamp;
        this.duration = duration;
    }

    /**
     * Get the date/time stamp of when the event occurred.
     * @return The date/time stamp of when the event occurred.
     */
    public DateTimeStamp getDateTimeStamp() {
        return timeStamp;
    }

    /**
     * Get the duration of the event.
     * @return The duration of the event in decimal seconds.
     */
    public double getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(getClass().getSimpleName()).append("@");
        if (timeStamp != null)
            string.append(timeStamp.toString());
        else
            string.append("unknown");
        return string.toString();
    }

}
