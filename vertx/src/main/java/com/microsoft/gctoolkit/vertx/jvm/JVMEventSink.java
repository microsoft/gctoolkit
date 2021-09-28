// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import io.vertx.core.Vertx;

public interface JVMEventSink {

    void start(Vertx vertx, String lineSource, String eventSource);

}
