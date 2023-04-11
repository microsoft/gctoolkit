package com.microsoft.gctoolkit.message;

import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.jvm.Diary;

import java.util.Set;

public interface DataSourceParser extends DataSourceChannelListener {
    void publishTo(JVMEventChannel channel);
    void diary(Diary diary);
    boolean accepts(Diary diary);
    Set<EventSource> eventsProduced();
}
