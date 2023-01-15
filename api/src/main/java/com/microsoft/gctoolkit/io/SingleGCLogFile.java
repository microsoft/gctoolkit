// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A single GC log file. If the file is a zip or gzip file,
 * then the first entry is the file of interest.
 */
public class SingleGCLogFile extends GCLogFile {

    private static final Logger LOGGER = Logger.getLogger(SingleGCLogFile.class.getName());

    /**
     * Constructor for a single, GC log file.
     * @param path The path to the log file.
     */

    private SingleLogFileMetadata metadata = null;

    public SingleGCLogFile(Path path) {
        super(path);
    }

    @Override
    public LogFileMetadata getMetaData() throws IOException {
        if (metadata == null) {
            metadata = new SingleLogFileMetadata(path);
        }
        return metadata;
    }

    @Override
    public Stream<String> stream() throws IOException {
        return stream(getMetaData());
    }

    private Stream<String> stream(LogFileMetadata metadata) throws IOException {
        Stream<String> stream = null;
        if (metadata.isPlainText()) {
            stream = Files.lines(metadata.getPath());
        } else if (metadata.isZip()) {
            stream = streamZipFile(metadata.getPath());
        } else if (metadata.isGZip()) {
            stream = streamGZipFile(metadata.getPath());
        }
        if ( stream == null)
            throw new IOException("Unable to read " + path.toString());
        return Stream.concat(stream
                .filter(Objects::nonNull)
                .filter(line -> ! line.isBlank())
                .map(String::trim)
                .filter(s -> s.length() > 0)
                ,Stream.of(endOfData()));

    }

    private static Stream<String> streamZipFile(Path path) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(Files.newInputStream(path));
        ZipEntry entry;
        do {
            entry = zipStream.getNextEntry();
        } while (entry != null && entry.isDirectory());
        return new BufferedReader(new InputStreamReader(new BufferedInputStream(zipStream))).lines();
    }

    private static Stream<String> streamGZipFile(Path path) throws IOException {
        GZIPInputStream gzipStream = new GZIPInputStream(Files.newInputStream(path));
        return new BufferedReader(new InputStreamReader(new BufferedInputStream(gzipStream))).lines();
    }

}
