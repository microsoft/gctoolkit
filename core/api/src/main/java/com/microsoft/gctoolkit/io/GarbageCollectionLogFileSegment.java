// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.io.IOException;
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

/**
 * A {@link RotatingGCLogFile} is made up of {@code GarbageCollectionLogFileSegment}s. Creating
 * a {@code GarbageCollectionLogFileSegment} is not necessary when the
 * {@link RotatingGCLogFile#RotatingGCLogFile(Path)} constructor is used.
 * The {@link RotatingGCLogFile#RotatingGCLogFile(Path, List)} constructor allows the user to
 * provide a list of discrete {@code GarbageCollectionLogFileSegement}s for a {@code RotatingGCLogFile}.
 */
public class GarbageCollectionLogFileSegment {

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
    private final int segmentIndex;
    private final boolean current;
    private DateTimeStamp endTime = null;
    private DateTimeStamp startTime = null;

    /**
     * The constructor attempts to extract the segment index from the file name.
     * @param path The path to the file.
     * @see #segmentIndex
     */
    public GarbageCollectionLogFileSegment(Path path) {
        this.path = path;

        final String filename = path.getFileName().toString();
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
        try {
            return Files.lines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return {@code true} if the log file segment was the file being written to.
     * @return {@code true} if the log file segment was the current file.
     */
    public boolean isCurrent() {
        return current;
    }

    private DateTimeStamp ageOfJVMAtLogStart() throws IOException {
        if (startTime == null)
            startTime = scanForTimeOfLogStart(path);
        return startTime;
    }

    private DateTimeStamp ageOfJVMAtLogEnd() throws IOException {
        if (endTime == null)
            endTime = scanForTimeOfLogEnd(path);
        return endTime;
    }

    // Define the maximum time (in milliseconds) between two rotating logs for them to be considered contiguous.
    private static final long MAX_INTERSITCE = Long.getLong("max-rotating-log-interstice", 30 * 1000);

    // does this log begin after otherSegment ends
    // log roll over has no JVM age.. can we estimate this...

    /**
     * Return true if this segment is a rollover from the other segment. If this segment starts
     * after the other segment ends, and the time difference between them is small, the segments are
     * considered contiguous.
     * @param otherSegment The log file segment being compared to this.
     * @return {@code true} if this segment is a rollover from the other segment.
     */
    public boolean isContiguousWith(GarbageCollectionLogFileSegment otherSegment) {
        // Compare by calendar date, if possible.
        double delta = rolloverDelta(this, otherSegment);
        if (Double.isNaN(delta)) {
            return false;
        }
        long intersticialDelta = (long)(delta * 1000d);
        return (0 <= intersticialDelta) && (intersticialDelta < MAX_INTERSITCE);
    }

    // calculate the delta between the start of newSegment and the end of oldSegment.
    /* package scope for testing */ static double rolloverDelta(GarbageCollectionLogFileSegment newSegment, GarbageCollectionLogFileSegment oldSegment) {
        DateTimeStamp startAge = null;
        DateTimeStamp endAge = null;
        try {
            startAge = newSegment.ageOfJVMAtLogStart();
            endAge = oldSegment.ageOfJVMAtLogEnd();
        } catch (IOException e) {
            // TODO: no access to logger here...
            return Double.NaN;
        }

        if (startAge == null || endAge == null) {
            return Double.NaN;
        }

        // Compare by calendar date, not uptime, if possible.
        final double startTime;
        final double endTime;
        if (startAge.hasDateStamp() && endAge.hasDateStamp()) {
            ZonedDateTime startDate = startAge.getDateTime();
            startTime = (double)startDate.toEpochSecond() + (double)startDate.getNano() / 1_000_000_000d;

            ZonedDateTime endDate   = endAge.getDateTime();
            endTime = (double)endDate.toEpochSecond() + (double)endDate.getNano() / 1_000_000_000d;

        } else {
            startTime = startAge.getTimeStamp();
            endTime   = endAge.getTimeStamp();
        }
        return startTime - endTime;
    }

    /**
     * {@inheritDoc}
     * @return Returns {@code this.getPath().toString(); }
     */
    @Override
    public String toString() {
        return path.toString();
    }

    private static Matcher matcher(String line) {
        if (line.startsWith("[")) {
            return UNIFIED_DATE_TIMESTAMP.matcher(line);
        } else {
            return PREUNIFIED_DATE_TIMESTAMP.matcher(line);
        }
    }

    private static DateTimeStamp calculateDateTimeStamp(Matcher matcher) {
        if (matcher.pattern() == UNIFIED_DATE_TIMESTAMP) {
            return calclateUnifiedDateTimeStamp(matcher);
        } else {
            return calculatePreUnifiedDateTimeStamp(matcher);
        }
    }

    // [2020-04-30T12:01:11.231-0400][2020-04-30T16:01:11.231+0000][0.009s][1588262471231ms][9ms][270937237146899ns][93577ns]
    private static DateTimeStamp calclateUnifiedDateTimeStamp(Matcher matcher) {
        String t = matcher.group(1);
        String time = t != null ? t.substring(1, t.length()-1) : null;
        String u = matcher.group(2);
        double uptime = u != null ? parseDouble(u.substring(1, u.length()-2)) : -1.0d;
        return new DateTimeStamp(time, uptime);
    }

    // 2017-09-07T09:00:12.795+0200: 0.716
    // group(1) = 2017-09-07T09:00:12.795+0200, group(2) = 0, group(3) = 716
    private static DateTimeStamp calculatePreUnifiedDateTimeStamp(Matcher matcher) {
        String dateStamp = matcher.group(1);
        double timeStamp = matcher.group(2) != null ? parseDouble(matcher.group(2)) : -1.0d;
        return new DateTimeStamp(dateStamp, timeStamp);
    }

    private static double parseDouble(String string) {
        if (string == null) return Double.NaN;
        return Double.parseDouble(string.replace(',','.'));
    }

    private static DateTimeStamp scanForTimeOfLogStart(Path path) throws IOException {
        return Files.lines(path)
                .map(GarbageCollectionLogFileSegment::matcher)
                .filter(Matcher::find)
                .findFirst()
                .map(GarbageCollectionLogFileSegment::calculateDateTimeStamp)
                .orElse(null);
    }

    private static DateTimeStamp scanForTimeOfLogEnd(Path path) throws IOException {
        return tail(path,100).stream()
                .map(GarbageCollectionLogFileSegment::matcher)
                .filter(Matcher::find)
                .map(GarbageCollectionLogFileSegment::calculateDateTimeStamp)
                .max(Comparator.comparing(dateTimeStamp -> dateTimeStamp != null ? dateTimeStamp.getTimeStamp() : 0))
                .orElse(null);
    }


     // todo: implementation may be a bit ugly...
     // https://codereview.stackexchange.com/questions/79039/get-the-tail-of-a-file-the-last-10-lines
     // Tail is not a class, it's a method so the solution in stackoverflow isn't correct but the core
     // could be used here as it's cleaner
    private static ArrayList<String> tail(Path path, int numberOfLines) throws IOException {

        char LF = '\n';
        char CR = '\r';
        boolean foundEOL = false;
        char eol = 0;
        RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "r");
        long currentPosition = randomAccessFile.length() - 1;
        int linesFound = 0;

        while (currentPosition > 0 && !foundEOL) {
            randomAccessFile.seek(currentPosition);
            char character = (char) randomAccessFile.readByte();
            if (character == LF) {
                eol = LF;
                randomAccessFile.seek(currentPosition - 1);
                character = (char) randomAccessFile.readByte();
                if (character == CR)
                    eol = CR;
                foundEOL = true;
            } else if (character == CR && !foundEOL) {
                eol = CR;
                foundEOL = true;
            } else
                currentPosition--;
        }

        currentPosition = randomAccessFile.length() - 1;

        while (currentPosition > 0 && linesFound < numberOfLines) {
            randomAccessFile.seek(--currentPosition);
            char character = (char) randomAccessFile.readByte();
            if (eol == character)
                linesFound++;
        }

        ArrayList<String> lines = new ArrayList<>();
        if (linesFound > 0) {
            String line;
            while ((line = randomAccessFile.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
