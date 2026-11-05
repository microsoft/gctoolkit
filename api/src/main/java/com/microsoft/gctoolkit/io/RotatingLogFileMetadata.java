// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Meta-data about a {@link FileDataSource}.
 */
public class RotatingLogFileMetadata extends LogFileMetadata {

    private static final Logger LOG = Logger.getLogger(RotatingLogFileMetadata.class.getName());

    private List<LogFileSegment> segments;
    private final LogFileReadLimits readLimits;

    /**
     * Creates metadata for a rotating garbage collection log source.
     *
     * @param path path to a rotating log file, archive, or directory
     * @throws IOException if the path cannot be inspected
     */
    public RotatingLogFileMetadata(Path path) throws IOException {
        this(path, LogFileReadLimits.defaults());
    }

    /**
     * Creates metadata for a rotating garbage collection log source with explicit resource limits.
     *
     * @param path path to a rotating log file, archive, or directory
     * @param readLimits resource limits applied while inspecting log segments
     * @throws IOException if the path cannot be inspected
     */
    public RotatingLogFileMetadata(Path path, LogFileReadLimits readLimits) throws IOException {
        super(path);
        this.readLimits = Objects.requireNonNull(readLimits, "readLimits");
    }

    /**
     * Streams the contiguous log segments in chronological order.
     *
     * @return a stream of ordered log segments
     */
    public Stream<LogFileSegment> logFiles() {
        return logFiles(new LogFileReadBudget(readLimits.getMaxExpandedBytes()));
    }

    Stream<LogFileSegment> logFiles(LogFileReadBudget inspectionBudget) {
        if ( segments == null) {
            if ( isPlainText() || isDirectory())
                findSegments(inspectionBudget);
            else if ( isZip())
                findZIPSegments(inspectionBudget);
            else {
                LOG.warning("unknown log file format");
                segments = new ArrayList<>();
            }
        }
        return segments.stream();
    }

    private void findZIPSegments(LogFileReadBudget inspectionBudget) {
        List<LogFileStreams.ZipEntryReference> entries;
        try {
            entries = LogFileStreams.zipEntries(getPath(), readLimits);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        segments = entries.stream()
                .filter(entry -> !entry.isDirectory())
                .map(entry -> new GCLogFileZipSegment(
                        getPath(),
                        entry.getName(),
                        readLimits,
                        entry))
                .collect(toList());
        orderSegments(inspectionBudget);
    }

    /**
     * Return the number of files. Useful if the file is a compressed file which may
     * contain multiple entries.
     * @return The number of files in the file.
     */
    public int getNumberOfFiles() {
        if ( this.segments == null)
            if ( isZip())
                findZIPSegments(new LogFileReadBudget(readLimits.getMaxExpandedBytes()));
            else
                findSegments(new LogFileReadBudget(readLimits.getMaxExpandedBytes()));
            return this.segments.size();
    }

    /**
     * Root for the pattern for the file currently being written to... has
     * a .<number> suffix for unified
     * a .current suffix for pre-unified.
     *
     * The possible parameters here along with the actions
     * 1) directory
     * 2) the file currently being written to
     * 3) a file not currently being written to.
     *
     * In all cases we want to find the file currently being written to and
     * use that to reverse engineer the root.
     *
     * @return String representing the pattern for the root of the rotating log name
     */
    private String getRootPattern() {

        // at this point we only have the path, not a segment... it maybe that we have to save the chosen segment
        // so  that we can normalize the code path for zip and file based logs????
        String[] bits;
        if (isDirectory()) {
            // if base is gc.log, filter out gc.log.<number>
            bits = segments.stream()
                    .filter(segment -> !segment.getSegmentName().matches(".+\\.\\d+$"))
                    .findFirst()
                    .get()
                    .getSegmentName().split("\\.");
        } else if ( isZip()) {
            bits = segments.get(0).getSegmentName().split("\\.");
        } else {
            bits = getPath().getFileName().toString().split("\\.");
        }

        int baseLength = 0;
        if ( "current".equals(bits[bits.length - 1]))
            baseLength = bits.length - 2;
        else if ( bits[bits.length - 1].matches("\\d+$"))
            baseLength = bits.length - 1;
        else
            baseLength = bits.length;

        StringBuilder base = new StringBuilder(bits[0]);
        for ( int i = 1; i < baseLength; i++)
            base.append(".").append(bits[i]);
        return base.toString();
    }

    private void findSegments(LogFileReadBudget inspectionBudget) {
        try (Stream<Path> paths = Files.list(isDirectory() ? getPath() : getPath().getParent())) {
            Stream<Path> matchingPaths = paths;
            if (isDirectory()) {
                matchingPaths = paths;
            } else {
                matchingPaths = paths.filter(
                        file -> file.getFileName().toString().startsWith(getRootPattern()));
            }
            List<Path> segmentPaths = matchingPaths
                    .limit((long) readLimits.getMaxArchiveEntries() + 1L)
                    .collect(toList());
            if (segmentPaths.size() > readLimits.getMaxArchiveEntries()) {
                throw new LogFileReadLimitExceededException(
                        LogFileReadLimitExceededException.LimitType.ARCHIVE_ENTRIES,
                        getPath(),
                        null,
                        Integer.toString(readLimits.getMaxArchiveEntries()),
                        Integer.toString(segmentPaths.size()));
            }
            segments = segmentPaths.stream()
                    .map(path -> new GCLogFileSegment(path, readLimits))
                    .collect(toList());
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        orderSegments(inspectionBudget);
    }

    private void orderSegments(LogFileReadBudget inspectionBudget) {

        if (segments.size() < 2) return;

        String basePattern = getRootPattern();
        LogFileSegment current = segments.stream()
                .filter( segment -> segment.getSegmentName().endsWith(basePattern) || segment.getSegmentName().endsWith(".current"))
                .findFirst().get();

        LinkedList<LogFileSegment> orderedList = new LinkedList<>();
        orderedList.addLast(current);
        double nextStartTime = getStartTime(current, inspectionBudget);
        List<LogFileSegment> candidates = segments.stream()
                .filter(segment -> segment != current)
                .sorted(Comparator.comparing(
                        (LogFileSegment segment) -> getEndTime(segment, inspectionBudget))
                        .reversed())
                .collect(toList());
        for (LogFileSegment candidate : candidates) {
            if (getEndTime(candidate, inspectionBudget) <= nextStartTime) {
                orderedList.addFirst(candidate);
                nextStartTime = getStartTime(candidate, inspectionBudget);
            }
        }
        segments = orderedList;
    }

    private static double getStartTime(
            LogFileSegment segment,
            LogFileReadBudget inspectionBudget) {
        if (segment instanceof GCLogFileZipSegment) {
            return ((GCLogFileZipSegment) segment).getStartTime(inspectionBudget);
        }
        return ((GCLogFileSegment) segment).getStartTime(inspectionBudget);
    }

    private static double getEndTime(
            LogFileSegment segment,
            LogFileReadBudget inspectionBudget) {
        if (segment instanceof GCLogFileZipSegment) {
            return ((GCLogFileZipSegment) segment).getEndTime(inspectionBudget);
        }
        return ((GCLogFileSegment) segment).getEndTime(inspectionBudget);
    }
}
