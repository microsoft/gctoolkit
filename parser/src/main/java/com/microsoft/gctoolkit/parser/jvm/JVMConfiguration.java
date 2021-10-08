// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

public interface JVMConfiguration {

    int MAXIMUM_LINES_TO_EXAMINE = 10_000;

    String getCommandLine();

    DateTimeStamp getTimeOfFirstEvent();

    int getMaxTenuringThreshold();

    LoggingDiary getDiary();

    boolean hasJVMEvents();

    boolean completed();

    void fillInKnowns();

    boolean diarize(String line);

}
