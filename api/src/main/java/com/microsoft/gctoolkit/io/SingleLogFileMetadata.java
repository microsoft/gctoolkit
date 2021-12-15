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

    public SingleLogFileMetadata(Path path) throws IOException {
        super(path);
        this.logFile = new GCLogFileSegment(path);
    }

    public Stream<LogFileSegment> logFiles() {
        return List.of(logFile).stream();
    }

    public int getNumberOfFiles() {
        return ( logFile != null) ? 1 : 0;
    }

}
