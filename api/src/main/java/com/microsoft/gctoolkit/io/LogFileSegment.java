package com.microsoft.gctoolkit.io;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface LogFileSegment {

    Path getPath();
    String getSegmentName();
    double getStartTime();
    double getEndTime();
    Stream<String> stream();
}
