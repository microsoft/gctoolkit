// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains an Aggregator and an Aggregation
 */
module censum.sample {

    provides com.microsoft.censum.aggregator.Aggregation with com.microsoft.censum.sample.aggregation.HeapOccupancyAfterCollectionSummary;

    requires censum.api;
    requires censum.vertx;
    requires java.logging;

}
