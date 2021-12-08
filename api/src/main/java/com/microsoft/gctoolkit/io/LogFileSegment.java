package com.microsoft.gctoolkit.io;

import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public interface LogFileSegment {

    String ROTATING_LOG_SUFFIX = ".*\\.(\\d+)(\\.current)?$";
    Pattern ROTATING_LOG_PATTERN = Pattern.compile(ROTATING_LOG_SUFFIX);

    Path getPath();
    String getSegmentName();
    double getStartTime();
    double getEndTime();
    Stream<String> stream();
}
