// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.io;

import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.io.FileDataSourceMetaData;
import com.microsoft.gctoolkit.io.GCLogFile;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SafepointLogFile implements DataSource<String> {

    private final FileDataSourceMetaData metadata;

    private final Path path;

    public SafepointLogFile(Path path) {
        this.path = path;
        this.metadata = new FileDataSourceMetaData(path);
    }

    @Override
    public String endOfData() {
        return GCLogFile.END_OF_DATA_SENTINAL;
    }

    public Path getPath() { return path; }

    public Stream<String> stream() throws IOException {
        if (metadata.isFile()) {
            return Files.lines(path);
        } else if (metadata.isZip()) {
            return streamZipFile();
        } else if (metadata.isGZip()) {
            return streamGZipFile();
        }
        throw new IOException("Unable to read " + path.toString());
    }

    Stream<String> streamZipFile() throws IOException {
        ZipInputStream zipStream = new ZipInputStream(Files.newInputStream(path));
        ZipEntry entry;
        do {
            entry = zipStream.getNextEntry();
        } while (entry.isDirectory());
        return new BufferedReader(new InputStreamReader(new BufferedInputStream(zipStream))).lines();
    }

    Stream<String> streamGZipFile() throws IOException {
        GZIPInputStream gzipStream = new GZIPInputStream(Files.newInputStream(path));
        return new BufferedReader(new InputStreamReader(new BufferedInputStream(gzipStream))).lines();
    }

}
