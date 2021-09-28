// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unittests;


import com.microsoft.gctoolkit.parser.TestLogFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

public class PreunifiedG1GCParserTest extends ParserTest {

    @Test
    public void testForSolarisLogs() {
        int i = 0;
        for (String name : solaris) {
            try {
                Path path = new TestLogFile("g1gc/details/solaris/" + name).getFile().toPath();
                TestResults testResults = testRegionalSingleLogFile(path);
                analyzeResults(name, testResults, solarisNumberOfDifferentCollectors[i], solarisCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] solaris = {
            "rightmove_parse_errors.log"
    };

    private static final int[] solarisNumberOfDifferentCollectors = {
            9
    };

    private static final int[][] solarisCounts = {
            //   0,    1,    2,    3,    4,    5,    6,    7,    8,    9,   10,   11,   12
            {945, 54, 57, 0, 4, 57, 0, 57, 57, 57, 0, 0, 57}
    };

    @Test
    public void testForDetailsLogs() {
        int i = 0;
        for (String name : details) {
            try {
                Path path = new TestLogFile("g1gc/details/" + name).getFile().toPath();
                TestResults testResults = testRegionalSingleLogFile(path);
                analyzeResults(name, testResults, detailsNumberOfDifferentCollectors[i], detailsCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] details = {
            "170_51mastermind.log",
            "g1.details.cause.log",
            "gc.log.4.31_1_aug_sep_current.zip"
    };

    private static final int[] detailsNumberOfDifferentCollectors = {
            1,
            10,
            9
    };

    private static final int[][] detailsCounts = {
            //   0,    1,    2,    3,    4,    5,    6,    7,    8,    9,   10,   11,   12
            {480, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {130, 2, 33, 0, 19, 33, 0, 17, 5, 17, 3, 0, 33},
            {11116, 1485, 320, 0, 15, 320, 0, 305, 303, 305, 0, 0, 319},
    };


    @Test
    public void testForDetailsTenuringLogs() {
        int i = 0;
        for (String name : detailsTenuring) {
            try {
                Path path = new TestLogFile("g1gc/details/tenuring/" + name).getFile().toPath();
                TestResults testResults = testRegionalSingleLogFile(path);
                analyzeResults(name, testResults, detailsTenuringNumberOfDifferentCollectors[i], detailsTenuringCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] detailsTenuring = {
            "server1-gc.log",
            "server2-gc.log",
            "server3-gc.log",
            "180/neo4j-gc.log.20140625",
            "170_45/rolling/ahjapp02a_gclog/ahj_prod_gc.log.0",   //this file has been corrupted and hence missed an entire record
            "170_45/g1_details_tenuring_cause.log",
            "180/seeker.log"
    };

    private static final int[] detailsTenuringNumberOfDifferentCollectors = {
            8,
            9,
            1,
            9,
            9,
            8,
            8
    };

    private static final int[][] detailsTenuringCounts = {
            //   0,    1,    2,    3,    4,    5,    6,    7,    8,    9,   10,   11,   12
            {794, 413, 157, 0, 0, 157, 0, 157, 157, 157, 0, 0, 157},
            {2194, 1308, 553, 0, 39, 553, 0, 523, 521, 523, 0, 0, 553},
            {16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1432, 9, 4, 0, 3, 4, 0, 4, 4, 4, 0, 0, 4},
            {3019, 7, 3, 0, 4, 3, 0, 3, 3, 3, 0, 0, 3},
            {544, 100, 52, 0, 0, 52, 0, 52, 36, 52, 0, 0, 52},
            {1581, 708, 246, 0, 0, 246, 0, 246, 246, 246, 0, 0, 246}
    };


    @Test
    public void testForDetailsReferenceLogs() {

        int i = 0;
        for (String name : detailsReference) {
            try {
                Path path = new TestLogFile("g1gc/details/reference/" + name).getFile().toPath();
                TestResults testResults = testRegionalSingleLogFile(path);
                analyzeResults(name, testResults, detailsReferenceNumberOfDifferentCollectors[i], detailsReferenceCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] detailsReference = {
            "post17040/170_51_mastermind.log",
            "post17040/gc_with_overflow.log"
    };

    private static final int[] detailsReferenceNumberOfDifferentCollectors = {
            1,
            7
    };

    private static final int[][] detailsReferenceCounts = {
            //    0,      1,      2,      3,      4,      5,      6,      7,      8,      9,    10,     11,     12
            {468, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {158, 0, 40, 0, 401, 40, 0, 3, 0, 3, 0, 0, 40}
    };
}
