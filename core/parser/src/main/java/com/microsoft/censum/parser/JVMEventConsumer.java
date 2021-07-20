// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.parser;

import com.microsoft.censum.event.jvm.JVMEvent;

public interface JVMEventConsumer {

    void record(JVMEvent event);
}
