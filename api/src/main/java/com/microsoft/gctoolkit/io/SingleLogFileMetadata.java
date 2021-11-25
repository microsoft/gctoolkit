// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Meta-data about a {@link FileDataSource}.
 */
public class SingleLogFileMetadata extends LogFileMetadata {

    private static final Logger LOG = Logger.getLogger(SingleLogFileMetadata.class.getName());

    private GCLogFileSegment logFile;

    public SingleLogFileMetadata(Path path) throws IOException {
        super(path);
    }

    public Stream<GCLogFileSegment> logFiles() {
        return List.of(logFile).stream();
    }

    void magic(Path path) throws IOException {
        if (path.toFile().isDirectory()) {
            logFile = new GCLogFileSegment(Files.list(path).findFirst().orElseThrow());
        } else if (path.toFile().isFile()) {
            discoverFormat();
            logFile = new GCLogFileSegment(path);
        } else
            throw new IOException(path + " is neither a directory or a regular file.");
    }

    public int getNumberOfFiles() {
        return ( logFile != null) ? 1 : 0;
    }
}
