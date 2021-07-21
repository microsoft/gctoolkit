// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains the core of the Censum API.
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
    uses com.microsoft.censum.aggregator.Aggregation;
}
