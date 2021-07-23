// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains the Censum GC log parser. The parser is an internal module.
 */
module censum.parser {

    exports com.microsoft.censum.parser to
            censum.parser.test,
            censum.vertx;

    exports com.microsoft.censum.parser.collection to
            censum.parser.test;

    exports com.microsoft.censum.parser.io to
            censum.parser.test,
            censum.vertx;

    exports com.microsoft.censum.parser.jvm to
            censum.parser.test,
            censum.vertx;

    exports com.microsoft.censum.parser.unified to
            censum.parser.test,
            censum.vertx;

    exports com.microsoft.censum.parser.vmops to
            censum.parser.test,
            censum.vertx;
    exports com.microsoft.censum.parser.datatype to censum.parser.test, censum.vertx;

    requires censum.api;
    requires java.logging;

}