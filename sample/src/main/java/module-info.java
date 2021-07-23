// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains an Aggregator and an Aggregation
 */
module censum.sample {


    requires censum.api;
    requires censum.vertx;
    requires java.logging;

    exports com.microsoft.censum.sample.aggregation to censum.vertx;

    provides com.microsoft.censum.aggregator.Aggregation with com.microsoft.censum.sample.aggregation.HeapOccupancyAfterCollectionSummary;
}
