package com.microsoft.gctoolkit.message;

import com.microsoft.gctoolkit.jvm.Diary;

public interface DataSourceParser {

    boolean accepts(Diary diary);

}
