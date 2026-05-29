// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.sample.aggregation;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.generational.DefNew;
import com.microsoft.gctoolkit.event.zgc.ZGCYoungCollection;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeapOccupancyAfterCollectionTest {

    @Test
    void ignoresGenerationalEventsWithoutHeapSummary() {
        HeapOccupancyAfterCollectionSummary summary = new HeapOccupancyAfterCollectionSummary();
        HeapOccupancyAfterCollection aggregator = new HeapOccupancyAfterCollection(summary);
        DefNew event = new DefNew(new DateTimeStamp(1.0d), GCCause.ALLOCATION_FAILURE, 1.0d);

        assertDoesNotThrow(() -> aggregator.receive(event));

        assertTrue(summary.isEmpty());
    }

    @Test
    void ignoresZgcEventsWithoutMemorySummary() {
        HeapOccupancyAfterCollectionSummary summary = new HeapOccupancyAfterCollectionSummary();
        HeapOccupancyAfterCollection aggregator = new HeapOccupancyAfterCollection(summary);
        ZGCYoungCollection event = new ZGCYoungCollection(
                new DateTimeStamp(1.0d),
                GarbageCollectionTypes.ZGCMinorYoung,
                GCCause.ALLOCATION_FAILURE,
                1.0d
        );

        assertDoesNotThrow(() -> aggregator.receive(event));

        assertTrue(summary.isEmpty());
    }
}
