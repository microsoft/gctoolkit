package com.microsoft.gctoolkit.message;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;

public interface JVMEventBus {
    void registerJVMEventListener(JVMEventListener listener);
    void close();
    void publish(JVMEvent event);
}
