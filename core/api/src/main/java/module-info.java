// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains the core API for the Microsoft, Java Garbage Collection Toolkit.
 * The toolkit is a GC log parser and a framework for consuming and extracting data from
 * GC log events.
 * <p>
 * The main entry points are:
 * <dl>
 * <dt>{@link com.microsoft.censum.Censum}</dt>
 * <dd>This is the main API that an application will use.</dd>
 * <dt>{@link com.microsoft.censum.io.GCLogFile}</dt>
 * <dd>A GCLogFile is passed to Censum for analysis.</dd>
 * <dt>{@link com.microsoft.censum.jvm.JavaVirtualMachine}</dt>
 * <dd>This contains the results from running an analysis on a GC log.</dd>
 * <dt>{@link com.microsoft.censum.event.jvm.JVMEvent}</dt>
 * <dd>The parser generates JVMEvents.</dd>
 * <dt>{@link com.microsoft.censum.aggregator.Aggregator}</dt>
 * <dd>An Aggregator captures JVMEvents for analysis.</dd>
 * <dt>{@link com.microsoft.censum.aggregator.Aggregation}</dt>
 * <dd>An Aggregation works with an Aggregator to collect and analyze data from JVMEvents.</dd>
 * </dl>
 * @uses com.microsoft.censum.jvm.JavaVirtualMachine
 * @uses com.microsoft.censum.aggregator.Aggregator
 */
module censum.api {

    exports com.microsoft.censum;
    exports com.microsoft.censum.aggregator;
    exports com.microsoft.censum.event;
    exports com.microsoft.censum.event.g1gc;
    exports com.microsoft.censum.event.generational;
    exports com.microsoft.censum.event.jvm;
    exports com.microsoft.censum.event.shenandoah;
    exports com.microsoft.censum.event.zgc;
    exports com.microsoft.censum.io;
    exports com.microsoft.censum.jvm;
    exports com.microsoft.censum.time;
    exports com.microsoft.censum.collections;
    requires java.logging;

    uses com.microsoft.censum.jvm.JavaVirtualMachine;
    uses com.microsoft.censum.aggregator.Aggregator;
}
