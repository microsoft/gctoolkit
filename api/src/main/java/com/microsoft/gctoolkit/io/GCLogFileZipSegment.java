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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A {@link RotatingGCLogFile} is made up of {@code GarbageCollectionLogFileSegment}s. Creating
 * a {@code GarbageCollectionLogFileSegment} is not necessary when the
 * {@link RotatingGCLogFile#RotatingGCLogFile(Path)} constructor is used.
 * The {@link RotatingGCLogFile#RotatingGCLogFile(Path, List)} constructor allows the user to
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
    //private final int segmentIndex;
    //private final boolean current;
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

//        String filename = path.getFileName().toString();
//        Matcher matcher = ROTATING_LOG_PATTERN.matcher(filename);
//        if (matcher.matches()) {
//            segmentIndex = Integer.parseInt(matcher.group(1));
//            current = ".current".equals(matcher.group(2));
//        } else {
//            // unified log with no number is the current file
//            segmentIndex = Integer.MAX_VALUE;
//            current = true;
//        }
    }

    /**
     * Return the path to the file.
     * @return The path to the file.
     */
    public Path getPath() {
        return path;
    }

    public String getSegmentName() { return this.segmentName; }

    @Override
    public double getStartTime() {
        return 0;
    }

    @Override
    public double getEndTime() {
        return 0;
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

    // Define the maximum time (in milliseconds) between two rotating logs for them to be considered contiguous.
    //private static final long MAX_INTERSITCE = Long.getLong("max-rotating-log-interstice", 30 * 1000);

    // does this log begin after otherSegment ends
    // log roll over has no JVM age.. can we estimate this...

    /**
     * Return true if this segment is a rollover from the other segment. If this segment starts
     * after the other segment ends, and the time difference between them is small, the segments are
     * considered contiguous.
     * @param otherSegment The log file segment being compared to this.
     * @return {@code true} if this segment is a rollover from the other segment.
     */
//    public boolean isContiguousWith(GCLogFileZipSegment otherSegment) {
//        // Compare by calendar date, if possible.
//        double delta = rolloverDelta(this, otherSegment);
//        if (Double.isNaN(delta)) {
//            return false;
//        }
//        long intersticialDelta = (long)(delta * 1000d);
//        return (0 <= intersticialDelta) && (intersticialDelta < MAX_INTERSITCE);
//    }

    // calculate the delta between the start of newSegment and the end of oldSegment.
//    /* package scope for testing */ static double rolloverDelta(GCLogFileZipSegment newSegment, GCLogFileZipSegment oldSegment) {
//        DateTimeStamp startAge;
//        DateTimeStamp endAge;
//        try {
//            startAge = newSegment.ageOfJVMAtLogStart();
//            endAge = oldSegment.ageOfJVMAtLogEnd();
//        } catch (IOException e) {
//            // TODO: no access to logger here...
//            return Double.NaN;
//        }
//
//        if (startAge == null || endAge == null) {
//            return Double.NaN;
//        }
//
//        // Compare by calendar date, not uptime, if possible.
//        double startTime;
//        double endTime;
//        if (startAge.hasDateStamp() && endAge.hasDateStamp()) {
//            ZonedDateTime startDate = startAge.getDateTime();
//            startTime = (double)startDate.toEpochSecond() + (double)startDate.getNano() / 1_000_000_000d;
//
//            ZonedDateTime endDate   = endAge.getDateTime();
//            endTime = (double)endDate.toEpochSecond() + (double)endDate.getNano() / 1_000_000_000d;
//
//        } else {
//            startTime = startAge.getTimeStamp();
//            endTime   = endAge.getTimeStamp();
//        }
//        return startTime - endTime;
//    }

    /**
     * {@inheritDoc}
     * @return Returns {@code this.getPath().toString(); }
     */
//    @Override
//    public String toString() {
//        return path.toString();
//    }
//
//    private static Matcher matcher(String line) {
//        if (line.startsWith("[")) {
//            return UNIFIED_DATE_TIMESTAMP.matcher(line);
//        } else {
//            return PREUNIFIED_DATE_TIMESTAMP.matcher(line);
//        }
//    }

//    private static DateTimeStamp calculateDateTimeStamp(Matcher matcher) {
//        if (matcher.pattern() == UNIFIED_DATE_TIMESTAMP) {
//            return calclateUnifiedDateTimeStamp(matcher);
//        } else {
//            return calculatePreUnifiedDateTimeStamp(matcher);
//        }
//    }

    // [2020-04-30T12:01:11.231-0400][2020-04-30T16:01:11.231+0000][0.009s][1588262471231ms][9ms][270937237146899ns][93577ns]
//    private static DateTimeStamp calclateUnifiedDateTimeStamp(Matcher matcher) {
//        String t = matcher.group(1);
//        String time = t != null ? t.substring(1, t.length()-1) : null;
//        String u = matcher.group(2);
//        double uptime = u != null ? parseDouble(u.substring(1, u.length()-2)) : -1.0d;
//        return new DateTimeStamp(time, uptime);
//    }

    // 2017-09-07T09:00:12.795+0200: 0.716
    // group(1) = 2017-09-07T09:00:12.795+0200, group(2) = 0, group(3) = 716
//    private static DateTimeStamp calculatePreUnifiedDateTimeStamp(Matcher matcher) {
//        String dateStamp = matcher.group(1);
//        double timeStamp = matcher.group(2) != null ? parseDouble(matcher.group(2)) : -1.0d;
//        return new DateTimeStamp(dateStamp, timeStamp);
//    }

//    private static double parseDouble(String string) {
//        if (string == null) return Double.NaN;
//        return Double.parseDouble(string.replace(',','.'));
//    }
//
//    private static DateTimeStamp scanForTimeOfLogStart(Path path) throws IOException {
//        return Files.lines(path)
//                .map(GCLogFileZipSegment::matcher)
//                .filter(Matcher::find)
//                .findFirst()
//                .map(GCLogFileZipSegment::calculateDateTimeStamp)
//                .orElse(null);
//    }

//    private static DateTimeStamp scanForTimeOfLogEnd(Path path) throws IOException {
//        return tail(path,100).stream()
//                .map(GCLogFileZipSegment::matcher)
//                .filter(Matcher::find)
//                .map(GCLogFileZipSegment::calculateDateTimeStamp)
//                .max(Comparator.comparing(dateTimeStamp -> dateTimeStamp != null ? dateTimeStamp.getTimeStamp() : 0))
//                .orElse(null);
//    }
}
