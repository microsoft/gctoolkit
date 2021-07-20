// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.io;

import java.nio.file.Path;

public abstract class FileDataSource<T> implements DataSource<T> {

    protected final Path path;
    protected final FileDataSourceMetaData metaData;

    protected FileDataSource(Path path) {
        this.path = path;
        this.metaData = new FileDataSourceMetaData(path);
    }

    public Path getPath() {
        return path;
    }

    public FileDataSourceMetaData getMetaData() {
        return metaData;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}