package com.microsoft.gctoolkit.message;

public interface Channel<M,L extends ChannelListener<M>> {
    void setChannel(Channels channel);
    void registerListener(L listener);
    void publish(M message);
}
