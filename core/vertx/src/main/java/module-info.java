// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

import com.microsoft.censum.jvm.JavaVirtualMachine;
import com.microsoft.censum.vertx.jvm.DefaultJavaVirtualMachine;

/**
 * Contains a vertx based implementation of Censum. The vertx implementation is an internal module.
 * @provides com.microsoft.censum.jvm.JavaVirtualMachine
 */
module censum.vertx {
    exports com.microsoft.censum.vertx to
            censum.vertx.test;

    exports com.microsoft.censum.vertx.aggregator to
            censum.vertx.test;

    exports com.microsoft.censum.vertx.io to
            censum.vertx.test;

    exports com.microsoft.censum.vertx.jvm to
            censum.api,
            censum.vertx.test;

    provides JavaVirtualMachine with DefaultJavaVirtualMachine;

    requires censum.api;
    requires censum.parser;
    requires java.logging;
    requires io.vertx.core;
}