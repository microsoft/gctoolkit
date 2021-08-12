// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains the GCToolKit GC log parser. The parser is an internal module.
 */
module com.microsoft.gctoolkit.parser {

    exports com.microsoft.gctoolkit.parser to
            gctoolkit.parser.test,
            com.microsoft.gctoolkit.vertx;

    exports com.microsoft.gctoolkit.parser.collection to
            gctoolkit.parser.test;

    exports com.microsoft.gctoolkit.parser.io to
            gctoolkit.parser.test,
            com.microsoft.gctoolkit.vertx;

    exports com.microsoft.gctoolkit.parser.jvm to
            gctoolkit.parser.test,
            com.microsoft.gctoolkit.vertx;

    exports com.microsoft.gctoolkit.parser.unified to
            gctoolkit.parser.test,
            com.microsoft.gctoolkit.vertx;

    exports com.microsoft.gctoolkit.parser.vmops to
            gctoolkit.parser.test,
            com.microsoft.gctoolkit.vertx;

    exports com.microsoft.gctoolkit.parser.datatype to
            gctoolkit.parser.test,
            com.microsoft.gctoolkit.vertx;

    requires com.microsoft.gctoolkit.api;
    requires java.logging;

}