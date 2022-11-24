package com.microsoft.gctoolkit.message;

public interface MessageConsumer<M> {

    void receive(M message);

}
