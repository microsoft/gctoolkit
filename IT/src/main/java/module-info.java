// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/*
 Module for the purposes of housing tests that need to be in a module in order to run.
 */
open module com.microsoft.gctoolkit.integration {

    requires com.microsoft.gctoolkit.api;
    requires java.logging;

    exports com.microsoft.gctoolkit.integration.aggregation to
            com.microsoft.gctoolkit.api;

    provides com.microsoft.gctoolkit.aggregator.Aggregation with
            com.microsoft.gctoolkit.integration.aggregation.HeapOccupancyAfterCollectionSummary,
            com.microsoft.gctoolkit.integration.aggregation.PauseTimeSummary,
            com.microsoft.gctoolkit.integration.aggregation.CollectionCycleCountsSummary,
            com.microsoft.gctoolkit.integration.shared.OneRuntimeReport,
            com.microsoft.gctoolkit.integration.shared.TwoRuntimeReport,
            com.microsoft.gctoolkit.integration.aggregation.CMSCycleAggregation;
}