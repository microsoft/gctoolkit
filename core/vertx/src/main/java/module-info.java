// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.vertx.jvm.DefaultJavaVirtualMachine;

/**
 * Contains a vertx based implementation of GCToolKit. The vertx implementation is an internal module.
 * @provides com.microsoft.gctoolkit.jvm.JavaVirtualMachine
 */
module gctoolkit.vertx {
    exports com.microsoft.gctoolkit.vertx to
            gctoolkit.vertx.test;

    exports com.microsoft.gctoolkit.vertx.aggregator to
            gctoolkit.vertx.test;

    exports com.microsoft.gctoolkit.vertx.io to
            gctoolkit.vertx.test;

    exports com.microsoft.gctoolkit.vertx.jvm to
            gctoolkit.api,
            gctoolkit.vertx.test;

    provides JavaVirtualMachine with DefaultJavaVirtualMachine;

    requires gctoolkit.api;
    requires gctoolkit.parser;
    requires java.logging;
    requires io.vertx.core;
}