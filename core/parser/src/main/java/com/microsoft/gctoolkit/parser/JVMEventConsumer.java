// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;

public interface JVMEventConsumer {

    void record(JVMEvent event);
}
