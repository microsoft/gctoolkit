// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
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

    private final static Logger LOGGER = Logger.getLogger(SingleGCLogFile.class.getName());

    private static boolean isUnifiedLogging(Path path) {
        try {
            FileDataSourceMetaData metadata = new FileDataSourceMetaData(path);
            Stream<String> stream = SingleGCLogFile.stream(path, metadata);
            boolean isUnifiedLogging = isUnifiedLogging(stream);
            // TODO: if isUnifiedLogging is false, assert that the file is pre-unified.
            //       if file is neither unified nor pre-unified, then we're dealing with
            //       something we can't handle.
            return isUnifiedLogging;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "cannot determine whether " + path + " is a unified log format");
            return false;
        }
    }

    /**
     * Constructor for a single, GC log file.
     * @param path The path to the log file.
     */
    public SingleGCLogFile(Path path) {
        super(path, SingleGCLogFile.isUnifiedLogging(path));
    }

    @Override
    public Stream<String> stream() throws IOException {
        return stream(path, getMetaData());
    }

    private static Stream<String> stream(Path path, FileDataSourceMetaData metadata) throws IOException {
        if (metadata.isFile()) {
            return Files.lines(path);
        } else if (metadata.isZip()) {
            return streamZipFile(path);
        } else if (metadata.isGZip()) {
            return streamGZipFile(path);
        }
        throw new IOException("Unable to read " + path.toString());
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
