package com.microsoft.gctoolkit.io;

import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A segment of a garbage collection log, including its location and time range.
 *
 * <p>A segment may be a standalone file or an entry within an archive. Implementations stream the
 * segment contents one line at a time.
 */
public interface LogFileSegment {

    /** The suffix used by rotating garbage collection log file names. */
    String ROTATING_LOG_SUFFIX = ".*\\.(\\d+)(\\.current)?$";

    /** The compiled pattern used to identify rotating garbage collection log file names. */
    Pattern ROTATING_LOG_PATTERN = Pattern.compile(ROTATING_LOG_SUFFIX);

    /**
     * Returns the path containing this segment.
     *
     * @return the segment file path, or the archive path for an archived segment
     */
    Path getPath();

    /**
     * Returns the name that identifies this segment within its containing path.
     *
     * @return the segment name
     */
    String getSegmentName();

    /**
     * Returns the earliest timestamp represented by this segment.
     *
     * @return the segment start time as either JVM uptime or epoch time
     */
    double getStartTime();

    /**
     * Returns the latest timestamp represented by this segment.
     *
     * @return the segment end time as either JVM uptime or epoch time
     */
    double getEndTime();

    /**
     * Streams the segment contents one line at a time.
     *
     * @return a stream of log lines
     */
    Stream<String> stream();
}
