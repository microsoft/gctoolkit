// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.jvm;


import com.microsoft.censum.time.DateTimeStamp;

public class JVMTermination extends JVMEvent {

    public JVMTermination(DateTimeStamp timeStamp) {
        super(timeStamp, 0.0d);
    }

}
