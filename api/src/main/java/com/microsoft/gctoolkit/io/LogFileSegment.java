package com.microsoft.gctoolkit.io;

import java.nio.file.Path;

public interface LogFileSegment {

    Path getPath();
    double getStartTime();
    double getEndTime();
}
