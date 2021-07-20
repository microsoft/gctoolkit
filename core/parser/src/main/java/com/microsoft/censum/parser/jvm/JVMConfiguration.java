// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.parser.jvm;

import com.microsoft.censum.time.DateTimeStamp;

public interface JVMConfiguration {

    String getCommandLine();

    DateTimeStamp getTimeOfFirstEvent();

    int getMaxTenuringThreshold();

    LoggingDiary getDiary();

    boolean hasJVMEvents();

    boolean completed();

    void fillInKnowns();

    boolean diarize(String line);

}