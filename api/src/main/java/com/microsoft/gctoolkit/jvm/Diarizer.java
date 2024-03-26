// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

public interface Diarizer {

    int MAXIMUM_LINES_TO_EXAMINE = 10_000;

    String getCommandLine();

    DateTimeStamp getTimeOfFirstEvent();

    int getMaxTenuringThreshold();

    boolean isUnified();

    Diary getDiary();

    boolean hasJVMEvents();

    boolean completed();

    boolean diarize(String line);

}
