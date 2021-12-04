// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A {@link RotatingGCLogFile} is made up of {@code GarbageCollectionLogFileSegment}s. Creating
 * a {@code GarbageCollectionLogFileSegment} is not necessary when the
 * {@link RotatingGCLogFile#RotatingGCLogFile(Path)} constructor is used.
 * The {@link RotatingGCLogFile#RotatingGCLogFile(Path)} constructor allows the user to
 * provide a list of discrete {@code GarbageCollectionLogFileSegement}s for a {@code RotatingGCLogFile}.
 */
public class GCLogFileZipSegment implements LogFileSegment {

    private static final String ROTATING_LOG_SUFFIX = ".*\\.(\\d+)(\\.current)?$";
    private static final Pattern ROTATING_LOG_PATTERN = Pattern.compile(ROTATING_LOG_SUFFIX);

    // Generic tokens
    private static final String DECIMAL_POINT = "(?:\\.|,)";
    private static final String INTEGER = "\\d+";
    private static final String DATE = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[\\+|\\-]\\d{4}";
    private static final String TIME = INTEGER + DECIMAL_POINT + "\\d{3}";

    // Pre-unified tokens
    private static final String TIMESTAMP = "(" + TIME + "): ";
    private static final String DATE_STAMP = "(" + DATE + "): ";
    private static final String DATE_TIMESTAMP = "^(?:" + DATE_STAMP + ")?" + TIMESTAMP;

    //  2017-09-07T09:00:12.795+0200: 0.716:
    private static final Pattern PREUNIFIED_DATE_TIMESTAMP = Pattern.compile(DATE_TIMESTAMP);

    // Unified Tokens
    private static final String DATE_TAG = "\\[" + DATE + "\\]";
    private static final String UPTIME_TAG = "\\[" + TIME + "s\\]";

    // JEP 158 has ISO-8601 time and uptime in seconds and milliseconds as the first two decorators.
    private static final Pattern UNIFIED_DATE_TIMESTAMP= Pattern.compile("^(" + DATE_TAG + ")?(" + UPTIME_TAG + ")?");

    private final Path path;
    private final String segmentName;
    private DateTimeStamp endTime = null;
    private DateTimeStamp startTime = null;

    /**
     * The constructor attempts to extract the segment index from the file name.
     * @param path The path to the file.
     * @ see # segmentIndex
     */
    public GCLogFileZipSegment(Path path, String segmentName) {
        this.path = path;
        this.segmentName = segmentName;
    }

    /**
     * Return the path to the file.
     * @return The path to the file.
     */
    public Path getPath() {
        return path;
    }

    public String getSegmentName() { return this.segmentName; }

    private DateTimeStamp ageOfJVMAtLogStart() throws IOException {
        if (startTime == null) {
            startTime = stream()
                    .map(this::matcher)
                    .filter(Matcher::find)
                    .findFirst()
                    .map(this::calculateDateTimeStamp)
                    .orElse(new DateTimeStamp(-1.0d));
        }
        return startTime;
    }

    private DateTimeStamp ageOfJVMAtLogEnd() throws IOException {
        if (endTime == null) {
            List<String> tail = stream().
                    collect(tail(100));

            endTime = tail.stream()
                    .filter(line -> ! line.contains("Saved as"))
                    .map(this::matcher)
                    .filter(Matcher::find)
                    .map(this::calculateDateTimeStampDebug)
                    .max(Comparator.comparing(dateTimeStamp -> dateTimeStamp != null ? dateTimeStamp.getTimeStamp() : 0))
                    .orElse(new DateTimeStamp(-1.0d));
        }
        return endTime;
    }

    public <T> Collector<T, ?, List<T>> tail(int n) {
        return Collector.<T, Deque<T>, List<T>>of(ArrayDeque::new, (buffer, line) -> {
            if(buffer.size() == n)
                buffer.pollFirst();
            buffer.add(line);
        }, (buffer, list) -> {
            while(list.size() < n && !buffer.isEmpty()) {
                list.addFirst(buffer.pollLast());
            }
            return list;
        }, ArrayList::new);
    }

    @Override
    public double getStartTime() {
        try {
            ageOfJVMAtLogStart();
            if ( startTime.hasTimeStamp())
                return startTime.getTimeStamp();
            else if ( startTime.hasDateTime())
                return startTime.toEpochInMillis();
            else
                return Double.MAX_VALUE;
        } catch (NullPointerException|IOException ex) {
            return Double.MIN_VALUE;
        }
    }

    @Override
    public double getEndTime() {
        try {
            ageOfJVMAtLogEnd();
            if ( endTime.hasTimeStamp())
                return endTime.getTimeStamp();
            else if ( endTime.hasDateTime())
                return endTime.toEpochInMillis();
            else
                return Double.MAX_VALUE;
        } catch (NullPointerException|IOException ex) {
            return Double.MIN_VALUE;
        }
    }

    /**
     * Stream the file, one line at a time.
     * @return A stream of lines from the file.
     */
    public Stream<String> stream() {
        try {
            ZipFile file = new ZipFile(path.toFile());
            ZipEntry entry = file.getEntry(this.segmentName);
            return new BufferedReader(new InputStreamReader(file.getInputStream(entry))).lines();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<String>().stream();
    }

    /**
     * {@inheritDoc}
     * @return Returns {@code this.getPath().toString(); }
     */
    @Override
    public String toString() {
        return getSegmentName();
    }

    private Matcher matcher(String line) {
        if (line.startsWith("[")) {
            return UNIFIED_DATE_TIMESTAMP.matcher(line);
        } else {
            return PREUNIFIED_DATE_TIMESTAMP.matcher(line);
        }
    }

    private DateTimeStamp calculateDateTimeStampDebug(Matcher matcher) {
        if (matcher.pattern() == UNIFIED_DATE_TIMESTAMP) {
            return calclateUnifiedDateTimeStamp(matcher);
        } else {
            return calculatePreUnifiedDateTimeStamp(matcher);
        }
    }

    private DateTimeStamp calculateDateTimeStamp(Matcher matcher) {
        if (matcher.pattern() == UNIFIED_DATE_TIMESTAMP) {
            return calclateUnifiedDateTimeStamp(matcher);
        } else {
            return calculatePreUnifiedDateTimeStamp(matcher);
        }
    }

    // [2020-04-30T12:01:11.231-0400][2020-04-30T16:01:11.231+0000][0.009s][1588262471231ms][9ms][270937237146899ns][93577ns]
    private DateTimeStamp calclateUnifiedDateTimeStamp(Matcher matcher) {
        String t = matcher.group(1);
        String time = t != null ? t.substring(1, t.length()-1) : null;
        String u = matcher.group(2);
        double uptime = u != null ? parseDouble(u.substring(1, u.length()-2)) : -1.0d;
        return new DateTimeStamp(time, uptime);
    }

    // 2017-09-07T09:00:12.795+0200: 0.716
    // group(1) = 2017-09-07T09:00:12.795+0200, group(2) = 0, group(3) = 716
    private DateTimeStamp calculatePreUnifiedDateTimeStamp(Matcher matcher) {
        String dateStamp = matcher.group(1);
        double timeStamp = matcher.group(2) != null ? parseDouble(matcher.group(2)) : -1.0d;
        return new DateTimeStamp(dateStamp, timeStamp);
    }

    private static double parseDouble(String string) {
        if (string == null) return Double.NaN;
        return Double.parseDouble(string.replace(',','.'));
    }
}
