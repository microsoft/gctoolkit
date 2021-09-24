// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * A source of data which may be streamed.
 * @param <T> The type of data returned in the stream.
 */
public interface DataSource<T> {

    /**
     * Return a stream of the data.
     * @return A stream of the data.
     * @throws IOException Thrown if the data cannot be streamed,
     * or an IOException is raised while streaming.
     */
    Stream<T> stream() throws IOException;

    /**
     * Return a sentinel value marking the end of the data.
     * @return A value used as a sentinel to mark the end of data.
     */
    T endOfData();
}
