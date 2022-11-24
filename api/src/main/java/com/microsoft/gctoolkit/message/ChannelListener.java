package com.microsoft.gctoolkit.message;

public interface ChannelListener<M> {

    void receive(M payload);

}
