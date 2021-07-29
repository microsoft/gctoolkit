// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

/**
 * Meta-data about a {@link FileDataSource}.
 */
public class FileDataSourceMetaData {

    private final static Logger LOG = Logger.getLogger(FileDataSourceMetaData.class.getName());

    private static final int GZIP_MAGIC1 = 0x1F;
    private static final int GZIP_MAGIC2 = 0x8b;

    private static final int ZIP_MAGIC1 = 0x50;
    private static final int ZIP_MAGIC2 = 0x4b;

    private boolean zip = false;
    private boolean gzip = false;
    private int numberOfFiles = -1;

    private final ArrayList<Path> logFiles = new ArrayList<>();
    private final Path path;

    public FileDataSourceMetaData(Path path) {
        this.path = path;
        magic(path);
    }

    private boolean magic(Path path, int field1, int field2) {
        try (FileInputStream magicByteReader = new FileInputStream(path.toFile())) {
            int magicByte1 = magicByteReader.read();
            int magicByte2 = magicByteReader.read();
            return magicByte1 == field1 && magicByte2 == field2;
        } catch (IOException ioe) {
            LOG.warning(ioe.getMessage());
        }
        return false;
    }

    private void magic(Path path) {

        try {
            if (Files.isDirectory(path)) {
                Files.list(path).forEach(child -> logFiles.add(child));
                this.numberOfFiles = logFiles.size();
            } else if (Files.isRegularFile(path)) {
                if (magic(path, ZIP_MAGIC1, ZIP_MAGIC2)) {
                    this.zip = true;
                    this.numberOfFiles = countNumberOfEntries();
                } else if (magic(path, GZIP_MAGIC1, GZIP_MAGIC2)) {
                    this.gzip = true;
                    this.numberOfFiles = 1; //todo, is this a tar entry???
                } else {
                    this.numberOfFiles = 1;
                }
            }
        } catch (IOException ioe) {
            LOG.warning(ioe.getMessage());
        }
    }


    private int countNumberOfEntries() {
        long count = -1;
        try {
            count = new ZipFile(path.toFile()).stream().filter(zipEntry -> !zipEntry.isDirectory()).count();
        } catch (IOException ioe) {
            LOG.warning(ioe.getMessage());
        }
        return (int) count;
    }


    /**
     * Return the number of files. Useful if the file is a compressed file which may
     * contain multiple entries.
     * @return The number of files in the file.
     */
    public int getNumberOfFiles() {
        return this.numberOfFiles;
    }

    /**
     * {@code true} if the file is a Zip compressed file. 
     * @return {@code true} if the file is a Zip compressed file.
     */
    public boolean isZip() {
        return this.zip;
    }

    /**
     * {@code true} if the file is a GZip compressed file. 
     * @return {@code true} if the file is a GZip compressed file.
     */
    public boolean isGZip() {
        return this.gzip;
    }

    /**
     * {@code true} if the file is a regular file. 
     * @return {@code true} if the file is a regular file.
     */
    public boolean isFile() {
        return !(isGZip() || isZip() || isDirectory());
    }

    /**
     * {@code true} if the file is a directory. 
     * @return {@code true} if the file is a directory.
     */
    public boolean isDirectory() {
        return path.toFile().isDirectory();
    }

}
