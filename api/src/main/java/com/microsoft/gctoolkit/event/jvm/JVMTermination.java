// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;


import com.microsoft.gctoolkit.time.DateTimeStamp;

public class JVMTermination extends JVMEvent {

    private DateTimeStamp startTime;

    public JVMTermination(DateTimeStamp timeStamp, DateTimeStamp startTime) {
        super(timeStamp, 0.0d);
        this.startTime = startTime;
    }

    public double getEstimatedRuntimeDuration() {
        return super.getDateTimeStamp().minus(startTime);
    }

}
