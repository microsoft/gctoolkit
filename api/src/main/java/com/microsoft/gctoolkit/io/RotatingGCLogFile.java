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

    /**
     * Use the given path to find rotating log files. If the path is a file, the file name is used to match
     * other files in the directory. If the path is a directory, all files in the directory are considered.
     * @param path the path to a rotating log file, or to a directory containing rotating log files.
     */
    public RotatingGCLogFile(Path path) {
        super(path);

//        if (getMetaData().isZip() || getMetaData().isGZip()) {
//            //TODO: add code to ensure correct order to stream files in zip and gzip files
//            this.orderedGarbageCollectionLogFiles = new LinkedList<>();
//        } else {
//            LinkedList<GCLogFileSegment> orderedSegments = null;
//            try {
//                List<GCLogFileSegment> segments = findGCLogSegments();
//                orderedSegments = orderSegments(segments);
//            } catch (IOException e) {
//                LOGGER.log(Level.WARNING, "Cannot find and order GC log file segments for: " + path);
//            } finally {
//                this.orderedGarbageCollectionLogFiles = orderedSegments;
//            }
//        }
    }

    /**
     * Create a RotatingGCLogFile with the given log file segments.
     * @param parentDirectory The directory that contains the log file segments.
     */
    public RotatingGCLogFile(Path parentDirectory, List<GCLogFileSegment> segments) throws IOException {
        super(parentDirectory); //, new SingleLogFileMetadata(parentDirectory));
        //this.orderedGarbageCollectionLogFiles = orderSegments(segments);
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

    //private final LinkedList<GCLogFileSegment> orderedGarbageCollectionLogFiles;
    private RotatingLogFileMetadata metaData;

    public LogFileMetadata getMetaData() throws IOException {
        if ( metaData == null)
            metaData =  new RotatingLogFileMetadata(getPath());
        return metaData;
    }

    @Override
    public Stream<String> stream() throws IOException {
        return stream(getMetaData(), null); // orderedGarbageCollectionLogFiles);
    }

    private Stream<String> stream(
            LogFileMetadata metadata,
            LinkedList<GCLogFileSegment> segments)
            throws IOException {
        //todo: find rolling files....
        if (metadata.isPlainText() || metadata.isDirectory()) {
            switch (segments.size()) {
                case 0:
                    String[] empty = new String[0];
                    return Arrays.stream(empty);
                case 1:
                    return segments.getFirst().stream();
                default:
                    // This code removes elements from the list of segments, so work on a copy.
                    LinkedList<GCLogFileSegment> copySegments = new LinkedList<>(segments);
                    Stream<String> allSegments = Stream.concat(copySegments.removeFirst().stream(), copySegments.removeFirst().stream());
                    while (!copySegments.isEmpty())
                        allSegments = Stream.concat(allSegments, copySegments.removeFirst().stream());
                    return allSegments;
            }
        } else if (metadata.isZip()) {
            return streamZipFile();
        } else if (metadata.isGZip()) {
            throw new IOException("Unable to stream GZip files. Please unzip and retry");
        }
        throw new IOException("Unrecognised file type");
    }

    @SuppressWarnings("resource")
    private Stream<String> streamZipFile() throws IOException {
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
     * The {@link GCLogFileSegment}s in rotating order. Note that only the contiguous
     * log file segments are included. Therefore, the number of log file segments may be less than
     * the files that match the rotating pattern.
     * @return The log file segments in rotating order.
     */
    public List<GCLogFileSegment> getOrderedGarbageCollectionLogFiles() {
        return null; //Collections.unmodifiableList(orderedGarbageCollectionLogFiles);
    }

    //assume directory but then allow for a file.
    private List<GCLogFileSegment> findGCLogSegments() throws IOException {

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
                    map(GCLogFileSegment::new).
                    collect(Collectors.toList());
         } else if (Files.isDirectory(path)) {
            return Files.list(path).
                    filter(Files::isRegularFile).
                    filter(file -> !file.toFile().isHidden()).
                    map(GCLogFileSegment::new).
                    collect(Collectors.toList());
        }

        throw new IllegalArgumentException("path is not a file or directory: " + path);
    }

    private LinkedList<GCLogFileSegment> orderSegments(List<GCLogFileSegment> gcLogSegments) {

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

        LinkedList<GCLogFileSegment> segments = new LinkedList<>();
        GCLogFileSegment[] orderedSegments = gcLogSegments.toArray(new GCLogFileSegment[0]);
        Arrays.sort(orderedSegments, Comparator.comparingInt(GCLogFileSegment::getSegmentIndex));

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
            double delta = GCLogFileSegment.rolloverDelta(orderedSegments[current], orderedSegments[index]);
            if (!Double.isNaN(delta) && 0 <= delta && delta < closestTime) {
                GCLogFileSegment temp = orderedSegments[current];
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
