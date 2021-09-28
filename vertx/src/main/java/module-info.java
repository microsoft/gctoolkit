// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains a vertx based implementation of GCToolKit. The vertx implementation is an internal module.
 * @provides com.microsoft.gctoolkit.jvm.JavaVirtualMachine
 */
module com.microsoft.gctoolkit.vertx {
    exports com.microsoft.gctoolkit.vertx to
            gctoolkit.vertx.test;

    exports com.microsoft.gctoolkit.vertx.aggregator to
            gctoolkit.vertx.test;

    exports com.microsoft.gctoolkit.vertx.io to
            gctoolkit.vertx.test;

    exports com.microsoft.gctoolkit.vertx.jvm to
            com.microsoft.gctoolkit.api,
            gctoolkit.vertx.test;

    provides com.microsoft.gctoolkit.jvm.JavaVirtualMachine
            with com.microsoft.gctoolkit.vertx.jvm.DefaultJavaVirtualMachine;

    requires com.microsoft.gctoolkit.api;
    requires com.microsoft.gctoolkit.parser;
    requires java.logging;
    requires io.vertx.core;
}