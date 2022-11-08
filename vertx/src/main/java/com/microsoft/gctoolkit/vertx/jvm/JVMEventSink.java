// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.message.JVMEventBus;
import io.vertx.core.Vertx;

public interface JVMEventSink extends JVMEventBus {

    void start(Vertx vertx, String lineSource, String eventSource);

}
