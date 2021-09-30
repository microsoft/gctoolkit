// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unittests;


public class ParallelCollectorParserTest extends ParserTest {
    /*
        @Test
        public void testForDetailsTenuringGCCause170() {
            int i = 0;
            for ( String name : detailsTenuringGCCause170) {
                try {
                    Path path = new TestLogFile("ps/details/tenuring/gccause/170/" + name).getFile().toPath();
                    TestResults testResults = testGenerationalSingleLogFile( path);
                    analyzeResults(testResults, detailsTenuringGCCause170CountsNumberOfDifferentCollectors[i], detailsTenuringGCCause170Counts[i++]);
                } catch (IOException ioe) {
                    fail(ioe.getMessage());
                }
            }
        }
    */
    private static final String[] detailsTenuringGCCause170 = {
            "ps.cause.tenuring.details.v17051.log",
            "ps.dates.cause.tenuring.details.v17051.log",
    };

    private static final int[] detailsTenuringGCCause170CountsNumberOfDifferentCollectors = {
            2,
            2

    };

    private static final int[][] detailsTenuringGCCause170Counts = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12
            {0, 0, 0, 0, 0, 0, 0, 90, 4, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 95, 3, 0, 0, 0, 0}
    };
    /*
        @Test
        public void testForDetails() {
            int i = 0;
            for ( String name : details) {
                try {
                    Path path = new TestLogFile("ps/details/" + name).getFile().toPath();
                    TestResults testResults = testGenerationalSingleLogFile( path);
                    analyzeResults(testResults, detailsNumberOfDifferentCollectors[i], detailsCounts[i++]);
                } catch (IOException ioe) {
                    fail(ioe.getMessage());
                }
            }
        }
    */
    private static final String[] details = {
            "long_pause.log"
    };

    private static final int[] detailsNumberOfDifferentCollectors = {
            2

    };

    private static final int[][] detailsCounts = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12
            {0, 0, 0, 0, 0, 0, 0, 21, 1, 0, 0, 0, 0}
    };
}
