// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.sample.aggregation.HeapOccupancyAfterCollectionSummary;

/**
 * Contains an Aggregator and an Aggregation
 */
module com.microsoft.gctoolkit.sample {
    requires com.microsoft.gctoolkit.api;
    requires com.microsoft.gctoolkit.parser;
    requires com.microsoft.gctoolkit.vertx;
    requires java.logging;

    exports com.microsoft.gctoolkit.sample.aggregation to com.microsoft.gctoolkit.vertx;
    exports com.microsoft.gctoolkit.sample.collections to com.microsoft.gctoolkit.sample.test;

    provides Aggregation with HeapOccupancyAfterCollectionSummary;
}
