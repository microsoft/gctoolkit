// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains an Aggregator and an Aggregation
 */
module com.microsoft.gctoolkit.sample {
    requires com.microsoft.gctoolkit.api;
    requires com.microsoft.gctoolkit.parser;
    requires com.microsoft.gctoolkit.vertx;
    requires java.logging;

    exports com.microsoft.gctoolkit.sample;

    exports com.microsoft.gctoolkit.sample.aggregation to
            com.microsoft.gctoolkit.vertx;

    provides com.microsoft.gctoolkit.aggregator.Aggregation with
             com.microsoft.gctoolkit.sample.aggregation.HeapOccupancyAfterCollectionSummary,
             com.microsoft.gctoolkit.sample.aggregation.PauseTimeSummary,
             com.microsoft.gctoolkit.sample.aggregation.CollectionCycleCountsSummary;
}

/*
/Library/Java/JavaVirtualMachines/jdk-13.jdk/Contents/Home/bin/java -ea
-DxgcLogFile=../gclogs/preunified/cms/defnew/details/defnew.log
-DgcLogFile=../gclogs/unified/g1gc/G1-80-16gbps2.log.0
-Didea.test.cyclic.buffer.size=1048576

"-javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=49740:/Applications/IntelliJ IDEA.app/Contents/bin"
-Dfile.encoding=UTF-8

-classpath
"/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar:
/Applications/IntelliJ IDEA.app/Contents/plugins/junit/lib/junit-rt.jar:
/Applications/IntelliJ IDEA.app/Contents/plugins/junit/lib/junit5-rt.jar:
/Users/chpepper/.m2/repository/org/junit/platform/junit-platform-launcher/1.8.1/junit-platform-launcher-1.8.1.jar:
/Users/chpepper/.m2/repository/org/junit/platform/junit-platform-engine/1.8.1/junit-platform-engine-1.8.1.jar:
/Users/chpepper/.m2/repository/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar:
/Users/chpepper/.m2/repository/org/junit/platform/junit-platform-commons/1.8.1/junit-platform-commons-1.8.1.jar:
/Users/chpepper/.m2/repository/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar:
/Users/chpepper/Projects/github/gctoolkit/sample/target/test-classes:
/Users/chpepper/Projects/github/gctoolkit/sample/target/classes:
/Users/chpepper/Projects/github/gctoolkit/api/target/classes:
/Users/chpepper/Projects/github/gctoolkit/vertx/target/classes:
/Users/chpepper/Projects/github/gctoolkit/parser/target/classes:
/Users/chpepper/.m2/repository/io/vertx/vertx-core/4.1.5/vertx-core-4.1.5.jar:
/Users/chpepper/.m2/repository/io/netty/netty-common/4.1.68.Final/netty-common-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-buffer/4.1.68.Final/netty-buffer-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-transport/4.1.68.Final/netty-transport-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-handler/4.1.68.Final/netty-handler-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-codec/4.1.68.Final/netty-codec-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-handler-proxy/4.1.68.Final/netty-handler-proxy-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-codec-socks/4.1.68.Final/netty-codec-socks-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-codec-http/4.1.68.Final/netty-codec-http-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-codec-http2/4.1.68.Final/netty-codec-http2-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-resolver/4.1.68.Final/netty-resolver-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-resolver-dns/4.1.68.Final/netty-resolver-dns-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-codec-dns/4.1.68.Final/netty-codec-dns-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.11.4/jackson-core-2.11.4.jar:
/Users/chpepper/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.8.1/junit-jupiter-api-5.8.1.jar:
/Users/chpepper/.m2/repository/org/junit/jupiter/junit-jupiter-engine/5.8.1/junit-jupiter-engine-5.8.1.jar"

com.intellij.rt.execution.junit.JUnitStarter -ideVersion5 -junit5 com.microsoft.gctoolkit.sample.TestMain


/Library/Java/JavaVirtualMachines/jdk-13.jdk/Contents/Home/bin/java
"-javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=49851:/Applications/IntelliJ IDEA.app/Contents/bin"
-Dfile.encoding=UTF-8

-p
/Users/chpepper/Projects/github/gctoolkit/sample/target/classes:
/Users/chpepper/Projects/github/gctoolkit/api/target/classes:
/Users/chpepper/Projects/github/gctoolkit/vertx/target/classes:
/Users/chpepper/Projects/github/gctoolkit/parser/target/classes:
/Users/chpepper/.m2/repository/io/vertx/vertx-core/4.1.5/vertx-core-4.1.5.jar:
/Users/chpepper/.m2/repository/io/netty/netty-common/4.1.68.Final/netty-common-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-buffer/4.1.68.Final/netty-buffer-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-transport/4.1.68.Final/netty-transport-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-handler/4.1.68.Final/netty-handler-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-codec/4.1.68.Final/netty-codec-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-handler-proxy/4.1.68.Final/netty-handler-proxy-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-codec-socks/4.1.68.Final/netty-codec-socks-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-codec-http/4.1.68.Final/netty-codec-http-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-codec-http2/4.1.68.Final/netty-codec-http2-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-resolver/4.1.68.Final/netty-resolver-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-resolver-dns/4.1.68.Final/netty-resolver-dns-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/io/netty/netty-codec-dns/4.1.68.Final/netty-codec-dns-4.1.68.Final.jar:
/Users/chpepper/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.11.4/jackson-core-2.11.4.jar

-m
com.microsoft.gctoolkit.sample/com.microsoft.gctoolkit.sample.Main gclogs/preunified/cms/defnew/details/defnew.log gclogs/unified/g1gc/G1-80-16gbps2.log.0 gclogs/unified/g1gc/jdk11_details.log
 */
