// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.stream.Stream;

/// Meta-data about a [FileDataSource].
public abstract class LogFileMetadata {

    private static final Logger LOG = Logger.getLogger(LogFileMetadata.class.getName());

    static final int GZIP_MAGIC1 = 0x1F;
    static final int GZIP_MAGIC2 = 0x8b;

    static final int ZIP_MAGIC1 = 0x50;
    static final int ZIP_MAGIC2 = 0x4b;

    private FileFormat fileFormat = FileFormat.UNKNOWN;
    private final Path path;

    public LogFileMetadata(Path path) throws IOException {
        this.path = path;
        magic();
    }

    public Path getPath() {
        return path;
    }

    boolean magic(int field1, int field2) {
        try (var magicByteReader = new FileInputStream(path.toFile())) {
            var magicByte1 = magicByteReader.read();
            var magicByte2 = magicByteReader.read();
            return magicByte1 == field1 && magicByte2 == field2;
        } catch (IOException ioe) {
            LOG.warning(ioe.getMessage());
        }
        return false;
    }

    public abstract Stream<LogFileSegment> logFiles();

    private void magic() {
        if (getPath().toFile().isDirectory())
            fileFormat = FileFormat.DIRECTORY;
        else if ( magic(GZIP_MAGIC1, GZIP_MAGIC2))
            fileFormat = FileFormat.GZIP;
        else if ( magic(ZIP_MAGIC1, ZIP_MAGIC2))
            fileFormat = FileFormat.ZIP;
        else
            fileFormat = FileFormat.PLAINTEXT;
    }

    /// Return the number of files. Useful if the file is a compressed file which may
    /// contain multiple entries.
    /// @return The number of files in the file.
    public abstract int getNumberOfFiles();

    /// `true` if the file is a Zip compressed file.
    /// @return `true` if the file is a Zip compressed file.
    public boolean isZip()  {
        return fileFormat == FileFormat.ZIP;
    }

    /// `true` if the file is a GZip compressed file.
    /// @return `true` if the file is a GZip compressed file.
    public boolean isGZip() {
        return fileFormat == FileFormat.GZIP;
    }

    /// `true` if the file is a regular file.
    /// @return `true` if the file is a regular file.
    public boolean isPlainText() {
        return fileFormat == FileFormat.PLAINTEXT;
    }

    /// `true` if the file is a directory.
    /// @return `true` if the file is a directory.
    public boolean isDirectory() {
        return fileFormat == FileFormat.DIRECTORY;
    }

    enum FileFormat {
        ZIP,
        GZIP,
        PLAINTEXT,
        DIRECTORY,
        UNKNOWN
    }

}
