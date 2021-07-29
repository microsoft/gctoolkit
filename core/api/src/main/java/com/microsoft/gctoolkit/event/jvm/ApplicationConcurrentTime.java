// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ApplicationConcurrentTime extends JVMEvent {

    public ApplicationConcurrentTime(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

}
