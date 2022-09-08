package com.microsoft.gctoolkit.message;

import com.microsoft.gctoolkit.jvm.Diary;

public interface DataSourceConsumer {

    boolean accepts(Diary diary);

}
