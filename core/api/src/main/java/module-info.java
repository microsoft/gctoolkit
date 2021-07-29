// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;

/**
 * Contains the core API for the Microsoft, Java Garbage Collection Toolkit.
 * The toolkit is a GC log parser and a framework for consuming and extracting data from
 * GC log events.
 * <p>
 * The main entry points are:
 * <dl>
 * <dt>{@link GCToolKit}</dt>
 * <dd>This is the main API that an application will use.</dd>
 * <dt>{@link GCLogFile}</dt>
 * <dd>A GCLogFile is passed to GCToolKit for analysis.</dd>
 * <dt>{@link JavaVirtualMachine}</dt>
 * <dd>This contains the results from running an analysis on a GC log.</dd>
 * <dt>{@link JVMEvent}</dt>
 * <dd>The parser generates JVMEvents.</dd>
 * <dt>{@link Aggregator}</dt>
 * <dd>An Aggregator captures JVMEvents for analysis.</dd>
 * <dt>{@link Aggregation}</dt>
 * <dd>An Aggregation works with an Aggregator to collect and analyze data from JVMEvents.</dd>
 * </dl>
 * @uses JavaVirtualMachine
 * @uses Aggregator
 */
module gctoolkit.api {

    exports com.microsoft.gctoolkit;
    exports com.microsoft.gctoolkit.aggregator;
    exports com.microsoft.gctoolkit.event;
    exports com.microsoft.gctoolkit.event.g1gc;
    exports com.microsoft.gctoolkit.event.generational;
    exports com.microsoft.gctoolkit.event.jvm;
    exports com.microsoft.gctoolkit.event.shenandoah;
    exports com.microsoft.gctoolkit.event.zgc;
    exports com.microsoft.gctoolkit.io;
    exports com.microsoft.gctoolkit.jvm;
    exports com.microsoft.gctoolkit.time;
    exports com.microsoft.gctoolkit.collections;
    requires java.logging;

    uses JavaVirtualMachine;
    uses Aggregation;
}
