// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of rotating GC log files. The collection will contain only those files that can be
 * considered contiguous. The log file segments are ordered, with the current or newest file first.
 */
public class RotatingGCLogFile extends GCLogFile {

    /**
     * Use the given path to find rotating log files. If the path is a file, the file name is used to match
     * other files in the directory. If the path is a directory, all files in the directory are considered.
     * @param path the path to a rotating log file, or to a directory containing rotating log files.
     */
    public RotatingGCLogFile(Path path) {
        this(path, LogFileReadLimits.defaults());
    }

    /**
     * Use the given path to find rotating log files with explicit resource limits.
     *
     * @param path the path to a rotating log file, or to a directory containing rotating log files
     * @param readLimits resource limits applied while inspecting and streaming the logs
     */
    public RotatingGCLogFile(Path path, LogFileReadLimits readLimits) {
        super(path, readLimits);
    }

    private RotatingLogFileMetadata metaData;

    public LogFileMetadata getMetaData() throws IOException {
        if ( metaData == null)
            metaData =  new RotatingLogFileMetadata(getPath(), getReadLimits());
        return metaData;
    }

    @Override
    public Stream<String> stream() throws IOException {
        LogFileReadBudget budget = new LogFileReadBudget(getReadLimits().getMaxExpandedBytes());
        LogFileMetadata metadata = getMetaData();
        if (metadata.isDirectory() || metadata.isPlainText() || metadata.isZip()) {
            Stream<LogFileSegment> segments = metaData.logFiles(budget);
            return Stream.concat(
                    segments
                            .flatMap(segment -> LogFileStreams.segment(segment, getReadLimits(), budget))
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> s.length() > 0),
                    Stream.of(endOfData()));
        } else { // yes, this is returning an empty stream.
            return Stream.of(endOfData());
        }
    }

    /**
     * The {@link GCLogFileSegment}s in rotating order. Note that only the contiguous
     * log file segments are included. Therefore, the number of log file segments may be less than
     * the files that match the rotating pattern.
     * @return The log file segments in rotating order.
     * @throws IOException when there is an IO exception
     */
    public List<LogFileSegment> getOrderedGarbageCollectionLogFiles() throws IOException {
        return getMetaData().logFiles().collect(Collectors.toList());
    }
}
