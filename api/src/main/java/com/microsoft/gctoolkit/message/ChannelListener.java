package com.microsoft.gctoolkit.message;

public interface ChannelListener<M> {
    ChannelName channel();
    void receive(M payload);
}
