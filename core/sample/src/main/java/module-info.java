// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains an Aggregator and an Aggregation
 */
module censum.sample {

    exports com.microsoft.censum.sample.aggregation to
            censum.api;

    requires censum.api;
    requires java.logging;


}
