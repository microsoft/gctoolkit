// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ApplicationRunTime extends JVMEvent {

    public ApplicationRunTime(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

}
