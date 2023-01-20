// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

import com.microsoft.gctoolkit.jvm.PreUnifiedJavaVirtualMachine;
import com.microsoft.gctoolkit.jvm.UnifiedJavaVirtualMachine;

/*
 * Contains the core API for the Microsoft, Java Garbage Collection Toolkit.
 * The toolkit is a GC log parser and a framework for consuming and extracting data from
 * GC log events.
 * <p>
 * The main entry points are:
 * <dl>
 * <dt>{@link com.microsoft.gctoolkit.GCToolKit}</dt>
 * <dd>This is the main API that an application will use.</dd>
 * <dt>{@link com.microsoft.gctoolkit.io.GCLogFile}</dt>
 * <dd>A GCLogFile is passed to GCToolKit for analysis.</dd>
 * <dt>{@link com.microsoft.gctoolkit.jvm.JavaVirtualMachine}</dt>
 * <dd>This contains the results from running an analysis on a GC log.</dd>
 * <dt>{@link com.microsoft.gctoolkit.event.jvm.JVMEvent}</dt>
 * <dd>The parser generates JVMEvents.</dd>
 * <dt>{@link com.microsoft.gctoolkit.aggregator.Aggregator}</dt>
 * <dd>An Aggregator captures JVMEvents for analysis.</dd>
 * <dt>{@link com.microsoft.gctoolkit.aggregator.Aggregation}</dt>
 * <dd>An Aggregation works with an Aggregator to collect and analyze data from JVMEvents.</dd>
 * </dl>
 */
 /**
 * @uses com.microsoft.gctoolkit.jvm.JavaVirtualMachine
 * @uses com.microsoft.gctoolkit.aggregator.Aggregator
 */
module com.microsoft.gctoolkit.api {
    requires java.logging;

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
    exports com.microsoft.gctoolkit.message;

    uses com.microsoft.gctoolkit.aggregator.Aggregation;
    uses com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
    uses com.microsoft.gctoolkit.jvm.Diarizer;
    uses com.microsoft.gctoolkit.message.DataSourceParser;
    uses com.microsoft.gctoolkit.message.DataSourceChannel;
    uses com.microsoft.gctoolkit.message.DataSourceChannelListener;
    uses com.microsoft.gctoolkit.message.JVMEventChannel;
    uses com.microsoft.gctoolkit.message.JVMEventChannelListener;

    // todo: no need to load with SPI
    provides com.microsoft.gctoolkit.jvm.JavaVirtualMachine with
            PreUnifiedJavaVirtualMachine,
            UnifiedJavaVirtualMachine;
}
