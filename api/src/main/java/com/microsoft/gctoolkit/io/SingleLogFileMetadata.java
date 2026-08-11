// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Meta-data about a {@link FileDataSource}.
 */
public class SingleLogFileMetadata extends LogFileMetadata {

    private static final Logger LOG = Logger.getLogger(SingleLogFileMetadata.class.getName());

    private LogFileSegment logFile;

    /**
     * Creates metadata for a single garbage collection log file.
     *
     * @param path path to the log file
     * @throws IOException if the path cannot be inspected
     */
    public SingleLogFileMetadata(Path path) throws IOException {
        super(path);
        this.logFile = new GCLogFileSegment(path);
    }

    /**
     * Returns a stream containing the single log segment.
     *
     * @return a stream containing the log segment
     */
    public Stream<LogFileSegment> logFiles() {
        return List.of(logFile).stream();
    }

    /**
     * Returns the number of log segments represented by this metadata.
     *
     * @return {@code 1} when the segment is present; otherwise {@code 0}
     */
    public int getNumberOfFiles() {
        return ( logFile != null) ? 1 : 0;
    }

}
