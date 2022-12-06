package com.microsoft.gctoolkit.message;

import com.microsoft.gctoolkit.jvm.Diary;

public interface DataSourceParser extends DataSourceChannelListener {
    void publishTo(JVMEventChannel channel);
    void diary(Diary diary);
    boolean accepts(Diary diary);
}
