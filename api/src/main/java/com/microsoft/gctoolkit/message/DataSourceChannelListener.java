package com.microsoft.gctoolkit.message;

import com.microsoft.gctoolkit.jvm.Diary;

public interface DataSourceChannelListener extends ChannelListener<String> {
    boolean accepts(Diary diary);
    void forward(JVMEventChannel channel);
}
