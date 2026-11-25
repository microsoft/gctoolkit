// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A single GC log file. If the file is a zip or gzip file,
 * then the first entry is the file of interest.
 */
public class SingleGCLogFile extends GCLogFile {

    private SingleLogFileMetadata metadata = null;

    /**
     * Constructor for a single, GC log file.
     * @param path The path to the log file.
     */
    public SingleGCLogFile(Path path) {
        this(path, LogFileReadLimits.defaults());
    }

    /**
     * Constructor for a single GC log file with explicit resource limits.
     *
     * @param path the path to the log file
     * @param readLimits resource limits applied while streaming the log
     */
    public SingleGCLogFile(Path path, LogFileReadLimits readLimits) {
        super(path, readLimits);
    }

    @Override
    public LogFileMetadata getMetaData() throws IOException {
        if (metadata == null) {
            metadata = new SingleLogFileMetadata(path, getReadLimits());
        }
        return metadata;
    }

    @Override
    public Stream<String> stream() throws IOException {
        return stream(getMetaData());
    }

    private Stream<String> stream(LogFileMetadata metadata) throws IOException {
        LogFileReadBudget budget = new LogFileReadBudget(getReadLimits().getMaxExpandedBytes());
        Stream<String> stream;
        if (metadata.isPlainText()) {
            stream = LogFileStreams.plainText(metadata.getPath(), getReadLimits(), budget);
        } else if (metadata.isZip()) {
            stream = LogFileStreams.firstZipEntry(metadata.getPath(), getReadLimits(), budget);
        } else if (metadata.isGZip()) {
            stream = LogFileStreams.gzip(metadata.getPath(), getReadLimits(), budget);
        } else {
            throw new IOException("Unable to read " + path);
        }
        return Stream.concat(stream
                .filter(Objects::nonNull)
                .filter(line -> ! line.isBlank())
                .map(String::trim)
                .filter(s -> s.length() > 0)
                ,Stream.of(endOfData()));

    }
}
