// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.parser.GCLogParser;
import com.microsoft.gctoolkit.parser.JVMEventConsumer;

public interface ParserFactory {
    GCLogParser get(JVMEventConsumer consumer);
}
