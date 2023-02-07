// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains a vertx based implementation of GCToolKit. The vertx implementation is an internal module.
 * @provides com.microsoft.gctoolkit.jvm.JavaVirtualMachine
 */
module com.microsoft.gctoolkit.vertx {
    requires com.microsoft.gctoolkit.api;
    requires io.vertx.core;
    requires java.logging;

    provides com.microsoft.gctoolkit.message.DataSourceChannel with com.microsoft.gctoolkit.vertx.VertxDataSourceChannel;
    provides com.microsoft.gctoolkit.message.JVMEventChannel with com.microsoft.gctoolkit.vertx.VertxJVMEventChannel;


}
