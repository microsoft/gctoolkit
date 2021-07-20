// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.jvm;

import com.microsoft.censum.time.DateTimeStamp;

public abstract class JVMEvent {

    private final DateTimeStamp timeStamp;
    private final double duration;

    public JVMEvent(DateTimeStamp timeStamp, double duration) {
        this.timeStamp = timeStamp;
        this.duration = duration;
    }

    public DateTimeStamp getDateTimeStamp() {
        return timeStamp;
    }

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
