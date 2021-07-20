// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.vertx.jvm;

import com.microsoft.censum.parser.GCLogParser;
import com.microsoft.censum.parser.JVMEventConsumer;

public interface ParserFactory {
    GCLogParser get(JVMEventConsumer consumer);
}
