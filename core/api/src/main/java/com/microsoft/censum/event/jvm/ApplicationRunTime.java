// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.jvm;

import com.microsoft.censum.time.DateTimeStamp;

public class ApplicationRunTime extends JVMEvent {

    public ApplicationRunTime(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

}
