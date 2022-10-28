package com.microsoft.gctoolkit.message;

public interface DataSourceBus<S> {

    void register(DataSourceConsumer parser);
    void start();
    void stop();
    void publish(S data);
}
