package com.microsoft.gctoolkit.message;

public interface DataSourceBus<S> {

    void register(DataSourceParser parser);
    void start();
    void stop();
    void publish(S data);
}
