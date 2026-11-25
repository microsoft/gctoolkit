// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * A {@link RotatingGCLogFile} is made up of {@code GarbageCollectionLogFileSegment}s. Creating
 * a {@code GarbageCollectionLogFileSegment} is not necessary when the
 * {@link RotatingGCLogFile#RotatingGCLogFile(Path)} constructor is used.
 * The { @ link RotatingGCLogFile # RotatingGCLogFile(Path, List) } constructor allows the user to
 * provide a list of discrete {@code GarbageCollectionLogFileSegement}s for a {@code RotatingGCLogFile}.
 */
public class GCLogFileSegment implements LogFileSegment {

    private final Path path;
    private final LogFileReadLimits readLimits;
    private final int segmentIndex;
    private final boolean current;
    private DateTimeStamp endTime = null;
    private DateTimeStamp startTime = null;

    /**
     * The constructor attempts to extract the segment index from the file name.
     * @param path The path to the file.
     */
    public GCLogFileSegment(Path path) {
        this(path, LogFileReadLimits.defaults());
    }

    /**
     * Creates a log segment with explicit resource limits.
     *
     * @param path the path to the file
     * @param readLimits resource limits applied while streaming the segment
     */
    public GCLogFileSegment(Path path, LogFileReadLimits readLimits) {
        this.path = path;
        this.readLimits = Objects.requireNonNull(readLimits, "readLimits");

        String filename = path.getFileName().toString();
        Matcher matcher = ROTATING_LOG_PATTERN.matcher(filename);
        if (matcher.matches()) {
            segmentIndex = Integer.parseInt(matcher.group(1));
            current = ".current".equals(matcher.group(2));
        } else {
            // unified log with no number is the current file
            segmentIndex = Integer.MAX_VALUE;
            current = true;
        }
    }

    /**
     * Return the path to the file.
     * @return The path to the file.
     */
    public Path getPath() {
        return path;
    }

    public String getSegmentName() {
        return getPath().toFile().getName();
    }

    /**
     * return some comparable value for the first time found in the log.
     * If isn't found, then return min value. This combined with the end
     * time being a max value implies the log covers an impossible amount
     * of time. The sorting logic in the Metadata classes should filter
     * out these types of segments.
     * @return double representing either the age of the JVM or time
     * from epoch if only a date stamp is found at the beginning of the log file
     */
    @Override
    public double getStartTime() {
        return getStartTime(new LogFileReadBudget(readLimits.getMaxExpandedBytes()));
    }

    double getStartTime(LogFileReadBudget readBudget) {
        try {
            ageOfJVMAtLogStart(readBudget);
            return startTime.getTimeStamp();
        } catch (NullPointerException ex) {
            return Double.MAX_VALUE;
        }
    }

    /**
     * return some comparable value for the last time found in the log.
     * If isn't found, then return max value. This combined with the start
     * time implies the log covers an impossible amount of time. The
     * sorting logic in the Metadata classes should filter out these
     * types of segments.
     * @return double representing either the age of the JVM or time
     * from epoch if only a date stamp is found at the end of the log file
     */
    @Override
    public double getEndTime() {
        return getEndTime(new LogFileReadBudget(readLimits.getMaxExpandedBytes()));
    }

    double getEndTime(LogFileReadBudget readBudget) {
        try {
            ageOfJVMAtLogEnd(readBudget);
            return endTime.getTimeStamp();
        } catch (NullPointerException ex) {
            return Double.MIN_VALUE;
        }
    }

    /**
     * The segment index is the integer appended to the file name. If the file name does not
     * have a segment index, then {@code Integer.MAX_VALUE} is returned.
     * @return The segment index, or {@code Integer.MAX_VALUE} if the file does not have a segment index.
     */
    public int getSegmentIndex() {
        return segmentIndex;
    }

    /**
     * Stream the file, one line at a time.
     * @return A stream of lines from the file.
     */
    public Stream<String> stream() {
        return stream(new LogFileReadBudget(readLimits.getMaxExpandedBytes()));
    }

    Stream<String> stream(LogFileReadBudget readBudget) {
        try {
            return LogFileStreams.plainText(
                    path,
                    readLimits,
                    readBudget);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Return {@code true} if the log file segment was the file being written to.
     * @return {@code true} if the log file segment was the current file.
     */
    public boolean isCurrent() {
        return current;
    }

    private DateTimeStamp ageOfJVMAtLogStart(LogFileReadBudget readBudget) {
        if (startTime == null) {
            try (Stream<String> lines = stream(readBudget)) {
                startTime = lines
                        .map(DateTimeStamp::fromGCLogLine)
                        .filter(dateTimeStamp -> dateTimeStamp.hasTimeStamp() || dateTimeStamp.hasDateStamp())
                        .findFirst()
                        .orElse(new DateTimeStamp(-1.0d));
            }
        }
        return startTime;
    }

    private DateTimeStamp ageOfJVMAtLogEnd(LogFileReadBudget readBudget) {
        if (endTime == null) {
            endTime = tail(100, readBudget).stream()
                    .map(DateTimeStamp::fromGCLogLine)
                    .filter(dateTimeStamp -> dateTimeStamp.hasTimeStamp() || dateTimeStamp.hasDateStamp())
                    .max(Comparator.comparing(dateTimeStamp -> dateTimeStamp != null ? dateTimeStamp.getTimeStamp() : 0))
                    .orElse(new DateTimeStamp(-1.0d));
        }
        return endTime;
    }

    /**
     * {@inheritDoc}
     * @return Returns {@code this.getName(); }
     */
    @Override
    public String toString() {
        return getSegmentName();
    }

    private List<String> tail(int numberOfLines, LogFileReadBudget readBudget) {
        try (Stream<String> lines = stream(readBudget)) {
            return lines.collect(tailCollector(numberOfLines));
        }
    }

    private static <T> Collector<T, ?, List<T>> tailCollector(int count) {
        return Collector.<T, Deque<T>, List<T>>of(ArrayDeque::new, (buffer, line) -> {
            if (buffer.size() == count) {
                buffer.pollFirst();
            }
            buffer.add(line);
        }, (buffer, list) -> {
            while (list.size() < count && !buffer.isEmpty()) {
                list.addFirst(buffer.pollLast());
            }
            return list;
        }, ArrayList::new);
    }
}
