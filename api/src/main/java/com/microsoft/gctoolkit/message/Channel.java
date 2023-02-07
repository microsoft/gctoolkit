package com.microsoft.gctoolkit.message;

public interface Channel<M,L extends ChannelListener<M>> {
    void registerListener(L listener);
    void publish(ChannelName channel, M message);
    void close();
}
