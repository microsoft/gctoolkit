package com.microsoft.gctoolkit.message;

import java.util.stream.Stream;

public interface DataSourceBus {
    void register(DataSourceParser consumer);
    void close();
    void publish(String channel, String payload);
    void publish(Channels channel, Stream<String> dataSource);
}
