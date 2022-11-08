package com.microsoft.gctoolkit.message;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;

public interface JVMEventListener {

    void receive(JVMEvent event);
}
