// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.io;

import java.io.IOException;
import java.util.stream.Stream;

public interface DataSource<T> {

    Stream<T> stream() throws IOException;

    T endOfData();
}
