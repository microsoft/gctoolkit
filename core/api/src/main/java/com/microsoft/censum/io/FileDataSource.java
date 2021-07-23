// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.io;

import java.nio.file.Path;

/**
 * A DataSource rooted in a file system.
 * @param <T> The type of data returned from the DataSource
 */
public abstract class FileDataSource<T> implements DataSource<T> {

    protected final Path path;
    protected final FileDataSourceMetaData metaData;

    /**
     * Subclass only.
     * @param path The path to the file in the file system.
     */
    protected FileDataSource(Path path) {
        this.path = path;
        this.metaData = new FileDataSourceMetaData(path);
    }

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
     */
    public FileDataSourceMetaData getMetaData() {
        return metaData;
    }

    /**
     * {@inheritDoc}
     * @return Returns {@code this.getPath().toString();}
     */
    @Override
    public String toString() {
        return path.toString();
    }
}