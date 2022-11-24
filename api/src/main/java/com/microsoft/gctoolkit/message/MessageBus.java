package com.microsoft.gctoolkit.message;

import java.util.stream.Stream;

public interface MessageBus<M> {
    void register(String channel, MessageConsumer consumer);
    void close();
    void publish(M payload);
    void publish(Stream<M> dataSource);
}
