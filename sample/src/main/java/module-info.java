// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains an Aggregator and an Aggregation
 */
module com.microsoft.gctoolkit.sample {
    requires com.microsoft.gctoolkit.api;
    requires java.logging;

    exports com.microsoft.gctoolkit.sample;

    exports com.microsoft.gctoolkit.sample.aggregation to
            com.microsoft.gctoolkit.api;

    provides com.microsoft.gctoolkit.aggregator.Aggregation with
             com.microsoft.gctoolkit.sample.aggregation.HeapOccupancyAfterCollectionSummary,
             com.microsoft.gctoolkit.sample.aggregation.PauseTimeSummary,
             com.microsoft.gctoolkit.sample.aggregation.CollectionCycleCountsSummary;
}
