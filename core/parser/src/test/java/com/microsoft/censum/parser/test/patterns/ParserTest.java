// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.parser.test.patterns;


import com.microsoft.censum.event.MemoryPoolSummary;
import com.microsoft.censum.parser.GCLogParser;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserTest {


    /**
     * Parser runs in it's own thread so start one for it and then feed it the lines to be parsed
     *
     * @param parser
     * @param lines
     */
    protected void feedParser(GCLogParser parser, String[] lines) {
        Arrays.stream(lines).map(String::trim).forEach(parser::receive);
    }

    /**
     * Common check for all GC events that report on memory.
     *
     * @param summary
     * @param occupancyAtStartOfCollection
     * @param sizeAtStartOfCollection
     * @param occupancyAfterCollection
     * @param sizeAfterCollection
     */
    protected void assertMemoryPoolValues(MemoryPoolSummary summary, long occupancyAtStartOfCollection, long sizeAtStartOfCollection, long occupancyAfterCollection, long sizeAfterCollection) {
        assertTrue(summary.getOccupancyBeforeCollection() == occupancyAtStartOfCollection);
        assertTrue(summary.getSizeBeforeCollection() == sizeAtStartOfCollection);
        assertTrue(summary.getOccupancyAfterCollection() == occupancyAfterCollection);
        assertTrue(summary.getSizeAfterCollection() == sizeAfterCollection);
    }
}
