#!/bin/sh

#######
# Sample cmd line
#######

export API_VERSION=2.0.1-SNAPSHOT

export APP=target/sample-${API_VERSION}.jar
export MODULES=../core/api/target/api-${API_VERSION}.jar:../core/vertx/target/vertx-${API_VERSION}.jar:../core/parser/target/parser-${API_VERSION}.jar
export VERTX=$HOME/.m2/repository/io/vertx/vertx-core/4.1.2/vertx-core-4.1.2.jar:
export NETTY=$HOME/.m2/repository/io/netty/netty-common/4.1.65.Final/netty-common-4.1.65.Final.jar:$HOME/.m2/repository/io/netty/netty-buffer/4.1.65.Final/netty-buffer-4.1.65.Final.jar:$HOME/.m2/repository/io/netty/netty-transport/4.1.65.Final/netty-transport-4.1.65.Final.jar:$HOME/.m2/repository/io/netty/netty-handler/4.1.65.Final/netty-handler-4.1.65.Final.jar:$HOME/.m2/repository/io/netty/netty-codec/4.1.65.Final/netty-codec-4.1.65.Final.jar:$HOME/.m2/repository/io/netty/netty-handler-proxy/4.1.65.Final/netty-handler-proxy-4.1.65.Final.jar:$HOME/.m2/repository/io/netty/netty-codec-socks/4.1.65.Final/netty-codec-socks-4.1.65.Final.jar:$HOME/.m2/repository/io/netty/netty-codec-http/4.1.65.Final/netty-codec-http-4.1.65.Final.jar:$HOME/.m2/repository/io/netty/netty-codec-http2/4.1.65.Final/netty-codec-http2-4.1.65.Final.jar:$HOME/.m2/repository/io/netty/netty-resolver/4.1.65.Final/netty-resolver-4.1.65.Final.jar:$HOME/.m2/repository/io/netty/netty-resolver-dns/4.1.65.Final/netty-resolver-dns-4.1.65.Final.jar:$HOME/.m2/repository/io/netty/netty-codec-dns/4.1.65.Final/netty-codec-dns-4.1.65.Final.jar
export JACKSON=$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.11.4/jackson-core-2.11.4.jar 

java -p $APP:$MODULES:$VERTX:$NETTY:$JACKSON -m gctoolkit.sample/com.microsoft.gctoolkit.sample.Main ../gclogs/preunified/cms/defnew/details/defnew.log

