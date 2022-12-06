package com.microsoft.gctoolkit.message;

public interface ChannelListener<M> {
    Channels channel();
    void receive(M payload);
}
