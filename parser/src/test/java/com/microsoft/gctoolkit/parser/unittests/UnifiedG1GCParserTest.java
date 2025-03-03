// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unittests;

import com.microsoft.gctoolkit.parser.diary.TestLogFile;
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
                TestResults testResults = testUnifiedG1GCSingleFile(path);
                analyzeResults(name, testResults, detailsNumberOfDifferentCollectors[i], detailsCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] details = {
            "jvm-gc-10Nov2022.log",
            "details_reference.log",
            "jdk11_details.log.zip"
    };

    private static final int[] detailsNumberOfDifferentCollectors = {
            9,
            12,
            11
    };
    private static final int[][] detailsCounts = {
            //  0,   1,   2, 3,  4,   5, 6,   7,   8,   9, 10, 11,  12,  13,  14,  15,  16,  17,  18,  19
            {   4,   0,   2, 0,  0,   2, 0,   2,   2,   2,  0,  0,   2,   2,   0,   0,   0,   0,   2,   0},
            {1130, 141, 146, 0,  1, 146, 0, 146,   0, 146,  0,  0, 146, 146,   0,   0,  16, 146, 146,   0},
            {9969,  40, 194, 0, 42, 198, 0, 156, 152, 152,  0,  0, 194, 194,   0,   0,   0,   0, 152,   0}
    };
}
