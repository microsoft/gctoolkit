// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
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
    private final int segmentIndex;
    private final boolean current;
    private DateTimeStamp endTime = null;
    private DateTimeStamp startTime = null;

    /**
     * The constructor attempts to extract the segment index from the file name.
     * @param path The path to the file.
     */
    public GCLogFileSegment(Path path) {
        this.path = path;

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
        try {
            ageOfJVMAtLogStart();
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
        try {
            ageOfJVMAtLogEnd();
            return endTime.getTimeStamp();
        } catch (NullPointerException|IOException ex) {
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

    private DateTimeStamp ageOfJVMAtLogStart() {
        if (startTime == null) {
            startTime = stream()
                    .map(DateTimeStamp::fromGCLogLine)
                    .filter(dateTimeStamp -> dateTimeStamp.hasTimeStamp() || dateTimeStamp.hasDateStamp())
                    .findFirst()
                    .orElse(new DateTimeStamp(-1.0d));
        }
        return startTime;
    }

    private DateTimeStamp ageOfJVMAtLogEnd() throws IOException {
        if (endTime == null) {
            endTime = tail(100).stream()
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


     // todo: implementation may be a bit ugly...
     // https://codereview.stackexchange.com/questions/79039/get-the-tail-of-a-file-the-last-10-lines
     // Tail is not a class, it's a method so the solution in stackoverflow isn't correct but the core
     // could be used here as it's cleaner
    private ArrayList<String> tail(int numberOfLines) throws IOException {

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
