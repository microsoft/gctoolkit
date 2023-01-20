// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import com.microsoft.gctoolkit.jvm.Diary;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A DataSource rooted in a file system.
 * @param <T> The type of data returned from the DataSource
 */
public abstract class FileDataSource<T> implements DataSource<T> {

    protected final Path path;

    /**
     * Subclass only.
     * @param path The path to the file in the file system.
     */
    protected FileDataSource(Path path) {
        this.path = path;
    }

    /**
     * The Diary contains a summary of important properties of the log that will be used in orchestrating the
     * setup and configuration of the internal components of GCToolkit.
     * @return a diary.
     */
    abstract public Diary diary() throws IOException;

    /**
     * Return the path to the file in the file system.
     * @return The path to the file in the file system.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Return meta data about the file.
     * @return Meta data about the file.
     * @throws IOException when there is an issue generating the diary
     */
    public abstract LogFileMetadata getMetaData() throws IOException;

    /**
     * {@inheritDoc}
     * @return Returns {@code this.getPath().toString();}
     */
    @Override
    public String toString() {
        return path.toString();
    }
}