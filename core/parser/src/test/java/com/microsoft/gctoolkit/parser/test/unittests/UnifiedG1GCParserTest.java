// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.test.unittests;

import com.microsoft.gctoolkit.parser.test.TestLogFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

public class UnifiedG1GCParserTest extends ParserTest {

    @Test
    public void testForDetailsLogs() {
        int i = 0;
        for (String name : details) {
            try {
                Path path = new TestLogFile("g1gc/" + name).getFile().toPath();
                TestResults testResults = testUnifiedSingleFile(path);
                analyzeResults(name, testResults, detailsNumberOfDifferentCollectors[i], detailsCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static String[] details = {
            "details_reference.log",
            "jdk11_details.log",
    };

    private static int[] detailsNumberOfDifferentCollectors = {
            8,
            9
    };
    private static int[][] detailsCounts = {
            //  0,   1,   2, 3,  4,   5, 6,   7,   8,   9, 10, 11,  12
            {1129, 141, 146, 0,  0, 146, 0, 146,   0, 145,  1,  0, 146},
            {9944,  40, 194, 0, 42, 198, 0, 156, 152, 152,  0,  0, 194}
    };
}
