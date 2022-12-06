package com.microsoft.gctoolkit.message;

public interface Channel<M,L extends ChannelListener<M>> {
    void registerListener(L listener);
    void publish(Channels channel, M message);
}
