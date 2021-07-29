// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.sample.aggregation.HeapOccupancyAfterCollectionSummary;

/**
 * Contains an Aggregator and an Aggregation
 */
module gctoolkit.sample {

    requires gctoolkit.api;
    requires gctoolkit.vertx;
    requires java.logging;

    exports com.microsoft.gctoolkit.sample.aggregation to gctoolkit.vertx;

    provides Aggregation with HeapOccupancyAfterCollectionSummary;
}
