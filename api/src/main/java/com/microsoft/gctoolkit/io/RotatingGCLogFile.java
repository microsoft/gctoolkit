// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A collection of rotating GC log files. The collection will contain only those files that can be
 * considered contiguous. The log file segments are ordered, with the current or newest file first.
 */
public class RotatingGCLogFile extends GCLogFile {

    private static final Logger LOGGER = Logger.getLogger(RotatingGCLogFile.class.getName());

    private static boolean isUnifiedLogging(Path path) {
        try {
            FileDataSourceMetaData metadata = new FileDataSourceMetaData(path);

            List<GarbageCollectionLogFileSegment> segments;
            if (metadata.isZip() || metadata.isGZip()) {
                //TODO: add code to ensure correct order to stream files in zip and gzip files
                segments = List.of();
            } else {
                segments = findGCLogSegments(path);
            }
            return isUnifiedLogging(path, segments);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "cannot determine whether " + path + " is a unified log format");
            return false;
        }
    }

    private static boolean isUnifiedLogging(Path path, List<GarbageCollectionLogFileSegment> segments) {
        // TODO: if isUnifiedLogging is false, assert that the file is pre-unified.
        //       if file is neither unified nor pre-unified, then we're dealing with
        //       something we can't handle.
        return segments.stream()
                .map(GarbageCollectionLogFileSegment::stream)
                .anyMatch(s -> {
                    try {
                        return isUnifiedLogging(s);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "cannot determine whether '" + path + "' is a unified log format");
                        return false;
                    }
                });
    }

    /**
     * Use the given path to find rotating log files. If the path is a file, the file name is used to match
     * other files in the directory. If the path is a directory, all files in the directory are considered.
     * @param path the path to a rotating log file, or to a directory containing rotating log files.
     */
    public RotatingGCLogFile(Path path) {
        super(path, isUnifiedLogging(path));

        if (getMetaData().isZip() || getMetaData().isGZip()) {
            //TODO: add code to ensure correct order to stream files in zip and gzip files
            this.orderedGarbageCollectionLogFiles = new LinkedList<>();
        } else {
            LinkedList<GarbageCollectionLogFileSegment> orderedSegments = null;
            try {
                List<GarbageCollectionLogFileSegment> segments = findGCLogSegments(path);
                orderedSegments = orderSegments(segments);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Cannot find and order GC log file segments for: " + path);
            } finally {
                this.orderedGarbageCollectionLogFiles = orderedSegments;
            }
        }
    }

    /**
     * Create a RotatingGCLogFile with the given log file segments.
     * @param parentDirectory The directory that contains the log file segments.
     * @param segments The log file segments.
     */
    public RotatingGCLogFile(Path parentDirectory, List<GarbageCollectionLogFileSegment> segments) {
        super(parentDirectory, isUnifiedLogging(parentDirectory, segments));
        this.orderedGarbageCollectionLogFiles = orderSegments(segments);
    }

    /**
     * A regular expression for matching a file name suffix such as '.0' or '.5.current'
     */
    private static final String ROTATING_LOG_SUFFIX = ".*(\\.\\d+(?:\\.current)?)$";

    /**
     * A pattern for matching the suffix of a rotating log file, such as '.0' or '.5.current'. Given
     * Path.getFileName().toString(), group(1) is the suffix of the rotating log. The dot is not
     * captured in the first group.
     */
    public static final Pattern ROTATING_LOG_PATTERN = Pattern.compile(ROTATING_LOG_SUFFIX);

    private final LinkedList<GarbageCollectionLogFileSegment> orderedGarbageCollectionLogFiles;

    @Override
    public Stream<String> stream() throws IOException {
        return stream(path, getMetaData(), orderedGarbageCollectionLogFiles);
    }

    private static Stream<String> stream(
            Path path,
            FileDataSourceMetaData metadata,
            LinkedList<GarbageCollectionLogFileSegment> segments)
            throws IOException {
        //todo: find rolling files....
        if (metadata.isFile() || metadata.isDirectory()) {
            switch (segments.size()) {
                case 0:
                    String[] empty = new String[0];
                    return Arrays.stream(empty);
                case 1:
                    return segments.getFirst().stream();
                default:
                    // This code removes elements from the list of segments, so work on a copy.
                    LinkedList<GarbageCollectionLogFileSegment> copySegments = new LinkedList<>(segments);
                    Stream<String> allSegments = Stream.concat(copySegments.removeFirst().stream(), copySegments.removeFirst().stream());
                    while (!copySegments.isEmpty())
                        allSegments = Stream.concat(allSegments, copySegments.removeFirst().stream());
                    return allSegments;
            }
        } else if (metadata.isZip()) {
            return streamZipFile(path);
        } else if (metadata.isGZip()) {
            throw new IOException("Unable to stream GZip files. Please unzip and retry");
        }
        throw new IOException("Unrecognised file type");
    }

    @SuppressWarnings("resource")
    private static Stream<String> streamZipFile(Path path) throws IOException {
        ZipFile zipFile = new ZipFile(path.toFile());
        List<ZipEntry> entries = zipFile.stream().filter(entry -> !entry.isDirectory()).collect(Collectors.toList());
        Vector<InputStream> streams = new Vector<>();

        try {
            entries
                    .stream()
                    .map(entry -> {
                        try {
                            return zipFile.getInputStream(entry);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .forEach(streams::add);
        } catch (UncheckedIOException uioe) {
            throw uioe.getCause();
        }

        SequenceInputStream sequenceInputStream = new SequenceInputStream(streams.elements());
        
        return new BufferedReader(new InputStreamReader(sequenceInputStream)).lines();
    }

    /**
     * The {@link GarbageCollectionLogFileSegment}s in rotating order. Note that only the contiguous
     * log file segments are included. Therefore, the number of log file segments may be less than
     * the files that match the rotating pattern.
     * @return The log file segments in rotating order.
     */
    public List<GarbageCollectionLogFileSegment> getOrderedGarbageCollectionLogFiles() {
        return Collections.unmodifiableList(orderedGarbageCollectionLogFiles);
    }

    //assume directory but then allow for a file.
    private static List<GarbageCollectionLogFileSegment> findGCLogSegments(Path path) throws IOException {

        if (Files.isRegularFile(path)) {
            String filename = path.getFileName().toString();
            Matcher matcher = ROTATING_LOG_PATTERN.matcher(filename);
            String rotatingLogBaseName;
            if (matcher.matches()) {
                String suffix = matcher.group(1);
                rotatingLogBaseName = filename.substring(0, filename.length()-suffix.length());
            } else {
                rotatingLogBaseName = filename;
            }
            Pattern rotatingLogBaseNamePattern = Pattern.compile(rotatingLogBaseName + "(?:" + ROTATING_LOG_SUFFIX + ")?");
            Predicate<Path> gcLogFragmentFinder = file -> rotatingLogBaseNamePattern.matcher(file.getFileName().toString()).matches();
            return Files.list(Paths.get(path.getParent().toString())).
                    filter(Files::isRegularFile).
                    filter(file -> !file.toFile().isHidden()).
                    filter(gcLogFragmentFinder).
                    map(GarbageCollectionLogFileSegment::new).
                    collect(Collectors.toList());
         } else if (Files.isDirectory(path)) {
            return Files.list(path).
                    filter(Files::isRegularFile).
                    filter(file -> !file.toFile().isHidden()).
                    map(GarbageCollectionLogFileSegment::new).
                    collect(Collectors.toList());
        }

        throw new IllegalArgumentException("path is not a file or directory: " + path);
    }

    private static LinkedList<GarbageCollectionLogFileSegment> orderSegments(List<GarbageCollectionLogFileSegment> gcLogSegments) {

        // Unified rotation: jdk11/src/hotspot/share/logging/logFileOutput.cpp
        //     Output is always to named file, e.g. 'gc.log' if given -Xlog:gc*:file=gc.log::filecount=5
        //     When gc.log is full, archive as gc.log.<_current_file>. Before the of gc.log to gc.log.<_current_file>,
        //     an attempt is made to delete an existing gc.log.<_current_file>
        //     Increment _current_file. if _current_file == filecount, set _current_file to zero
        //         - JVM always begins archiving at gc.log.0
        //         - gc.log is always the current log
        //         - Once filecount files have been archived, archive begins at zero again
        //
        // Pre-unified rotation: jdk8/hotspot/src/share/vm/utilities/ostream.cpp
        //     rotate file in names extended_filename.0, extended_filename.1, ...,
        //     extended_filename.<NumberOfGCLogFiles - 1>. Current rotation file name will
        //     have a form of extended_filename.<i>.current where i is the current rotation
        //     file number. After it reaches max file size, the file will be saved and renamed
        //     with .current removed from its tail.

        if (gcLogSegments.size() < 2) {
            return new LinkedList<>(gcLogSegments);
        }

        LinkedList<GarbageCollectionLogFileSegment> segments = new LinkedList<>();
        GarbageCollectionLogFileSegment[] orderedSegments = gcLogSegments.toArray(new GarbageCollectionLogFileSegment[0]);
        Arrays.sort(orderedSegments, Comparator.comparingInt(GarbageCollectionLogFileSegment::getSegmentIndex));

        int current = orderedSegments.length;
        while (0 <= --current) {
            if (orderedSegments[current].isCurrent()) {
                break;
            }
        }

        // if current == -1, then there is no current! What to do?
        if (current == -1) {
            Collections.addAll(segments, orderedSegments);
            return segments;
        }

        double closestTime = Double.MAX_VALUE;
        // Find where current belongs.
        for (int index = current-1; 0 <= index; --index) {
            double delta = GarbageCollectionLogFileSegment.rolloverDelta(orderedSegments[current], orderedSegments[index]);
            if (!Double.isNaN(delta) && 0 <= delta && delta < closestTime) {
                GarbageCollectionLogFileSegment temp = orderedSegments[current];
                for(int n=current; n > index+1; --n) {
                    orderedSegments[n] = orderedSegments[n-1];
                }
                orderedSegments[current=index+1] = temp;
                closestTime = delta;
            }
        }

        segments.addLast(orderedSegments[current]);

        int index = ((current-1) + orderedSegments.length) % orderedSegments.length;
        while (index != current) {
            if (orderedSegments[current].isContiguousWith(orderedSegments[index])) {
                segments.addFirst(orderedSegments[index]);
                current = index;
                index = ((current - 1) + orderedSegments.length) % orderedSegments.length;
            } else {
                break;
            }
        }

        return segments;
    }
}
