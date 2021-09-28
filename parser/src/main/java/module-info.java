// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains the GCToolKit GC log parser. The parser is an internal module.
 */
module com.microsoft.gctoolkit.parser {
    requires com.microsoft.gctoolkit.api;
    requires java.logging;

    exports com.microsoft.gctoolkit.parser to
            com.microsoft.gctoolkit.vertx;

    exports com.microsoft.gctoolkit.parser.datatype to
            com.microsoft.gctoolkit.vertx;

    exports com.microsoft.gctoolkit.parser.io to
            com.microsoft.gctoolkit.vertx;

    exports com.microsoft.gctoolkit.parser.jvm to
            com.microsoft.gctoolkit.vertx;

    exports com.microsoft.gctoolkit.parser.unified to
            com.microsoft.gctoolkit.vertx;

    exports com.microsoft.gctoolkit.parser.vmops to
            com.microsoft.gctoolkit.vertx;
}