// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unittests;

import com.microsoft.gctoolkit.parser.diary.TestLogFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.fail;

public class ICMSParNewParserTest extends ParserTest {

    /*
    guide
    0,          1,      2,                         3,                                4,                       5,                           6,          7,              8,                                 9,          10,           11,     12
    Young, DefNew, ParNew, ParNew (promotion failed), ParNew (concurrent mode failure), concurrent-mode-failure, concurrent mode interrupted, PSYoungGen, Full GC/PSFull, Full GC (concurrent mode failure), System.gc(), Initial-mark, Remark
    */

    private static final Logger LOGGER = Logger.getLogger(ICMSParNewParserTest.class.getName());

    @Test
    public void testForSimpleLogs() {
        int i = 0;
        for (String name : details) {
            try {
                Path path = new TestLogFile("icms/details/" + name).getFile().toPath();
                TestResults testResults = testGenerationalSingleLogFile(path);
                analyzeResults(name,testResults, detailsCountsNumberOfDifferentCollectors[i], detailsCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] details = {
            "ossm-gc.2012-02-01.142455.log",
            "par.icms.wd.nt.ast.2.log",
            "par.icms.wd.nt.ast.log",
            "par.icms.wd.nt.log",
            "production-1.gc.log",
            "production-2.gc.log",
    };

    private static final int[] detailsCountsNumberOfDifferentCollectors = {
             9,
             8,
             8,
             9,
            11,
            11
    };

    private static final int[][] detailsCounts = {
            //   0,      1,      2,      3,      4,      5,      6,      7,      8,      9,     10,     11      12
            {0, 0,  12359, 41, 0, 219, 0, 0, 0, 0, 0, 179, 166},
            {0, 0,   2564,  0, 0, 13, 0, 0, 0, 0, 0, 13, 13},
            {0, 0,   2564,  0, 0, 11, 0, 0, 0, 0, 0, 11, 11},
            {0, 0,   1374,  0, 0, 2, 1, 0, 0, 0, 0, 3, 2},
            {0, 0, 143543, 75, 0, 10434, 0, 0, 7, 0, 1, 10272, 10015},
            {0, 0,  72395, 43, 0, 5254, 0, 0, 4, 0, 1, 5137, 4985}
    };


    @Test
    public void testForDetailsTenuring() {
        int i = 0;
        for (String name : detailsTenuring) {
            try {
                Path path = new TestLogFile("icms/details/tenuring/" + name).getFile().toPath();
                TestResults testResults = testGenerationalSingleLogFile(path);
                analyzeResults(name, testResults, detailsTenuringCountsNumberOfDifferentCollectors[i], detailsTenuringCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] detailsTenuring = {
            "gc-master.log",
            "gigaspace_content_gsc13.gc.out",
            "par.cms.wt.wd.ast.log.zip",
            "par.icms.wd.wt.ast.log",
            "parnew.icms.wd.wt.datestamp.log"
    };

    private static final int[] detailsTenuringCountsNumberOfDifferentCollectors = {
            9,
            9,
            10,
            8,
            9

    };

    private static final int[][] detailsTenuringCounts = {
            //    0,    1,     2,     3,     4,     5,     6,      7,     8,     9,    10,    11,    12
            {0, 0,     67,  0, 0, 26, 0, 0, 1, 0, 0, 25, 24},
            {0, 0,   6240,  0, 0, 2087, 0, 0, 0, 0, 1, 2087, 2087},
            {0, 0, 417811, 83, 0, 7908, 0, 0, 76, 0, 0, 7840, 7809},
            {0, 0,  32439,  0, 0, 103, 0, 0, 0, 0, 0, 103, 103},
            {0, 0,  18507,  0, 0, 5, 1, 0, 0, 0, 0, 6, 5},
    };


    @Test
    public void testForDetailsDebug() {
        int i = 0;
        for (String name : detailsDebug) {
            try {
                Path path = new TestLogFile("icms/details/debug/" + name).getFile().toPath();
                TestResults testResults = testGenerationalSingleLogFile(path);
                analyzeResults(name, testResults, detailsDebugCountsNumberOfDifferentCollectors[i], detailsDebugCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] detailsDebug = {
            "gc_mmtreeservernode8_20121120005124.log"
    };

    private static final int[] detailsDebugCountsNumberOfDifferentCollectors = {
            8

    };

    private static final int[][] detailsDebugCounts = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12
            {0, 0, 3089, 0, 0, 532, 0, 0, 0, 0, 0, 533, 532}
    };
}
