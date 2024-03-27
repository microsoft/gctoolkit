// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unittests;

import com.microsoft.gctoolkit.parser.diary.TestLogFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

public class CMSParNewParserTest extends ParserTest {

    @Test
    public void testForSimpleLogs() {
        int i = 0;
        for (String name : simple) {
            try {
                Path path = new TestLogFile("cms/" + name).getFile().toPath();
                TestResults testResults = testGenerationalSingleLogFile(path);
                analyzeResults(name, testResults, simpleCountsNumberOfDifferentCollectors[i], simpleCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] simple = {
            "530_gc.log",
            "713_gc.log"
    };

    private static final int[] simpleCountsNumberOfDifferentCollectors = {
            4,
            4
    };

    private static final int[][] simpleCounts = {
            //   0,    1,    2,    3,    4,    5,    6,    7,    8,    9,   10,   11,   12
            {30, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 4, 3},
            {79, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 24, 24}
    };

    @Test
    public void testForDetails() {
        int i = 0;
        for (String name : details) {
            try {
                Path path = new TestLogFile("cms/parnew/details/" + name).getFile().toPath();
                TestResults testResults = testGenerationalSingleLogFile(path);
                analyzeResults(name, testResults, detailsCountsNumberOfDifferentCollectors[i], detailsCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] details = {
            "1073_1.log",
            "apptio_gc.log",
            "gc_www1_2013_10_19.log",
            "gc.2011-09-19.141115.log",
            "GCLog_CMSCollection.log",
            "lead_up_to_problem_gc_www1_2013_10_19.log", // todo: audit
            "LS_gc.log.20121116_193624",
            "sample.leak.workaround.log",
            "TC_oy579c2n5.gc.out",
            "tidied_gc_www1_2013_10_19.log"
    };

    private static final int[] detailsCountsNumberOfDifferentCollectors = {
            11,
            10,
             4,
             9,
             8,
             4,
             7,
            10,
             8,
             4
    };

    private static final int[][] detailsCounts = {
          // 0, 1,     2,  3, 4,  5, 6, 7, 8,  9, 10,  11,  12,  13,  14,  15,  16,  17
            {0, 0,  3668, 12, 0, 96, 0, 0, 22, 0,  0, 156,  68, 156,  75,  71,  68,  66},   //todo: these numbers don't seem to add up
            {0, 0,  2021,  1, 0,  8, 0, 0,  0, 0,  0,  71,  70,  71,  71,  70,  70,  63},
            {0, 0, 24981,  0, 0,  0, 0, 0,  0, 0,  2, 334, 333,   0,   0,   0,   0,   0},
            {0, 0,  2762,  0, 0,  0, 0, 0,  3, 0,  0,  50,  49,  50,  49,  49,  49,  49},
            {0, 0,   713,  0, 0,  0, 0, 0,  0, 0,  0,   1,   1,   1,   1,   1,   1,   1},
            {0, 0,  2160,  0, 0,  0, 0, 0,  0, 0,  1, 228, 228,   0,   0,   0,   0,   0},
            {0, 0,  1969,  0, 0,  0, 0, 0,  0, 0,  0,   1,   1,   1,   1,   0,   1,   1},
            {0, 0, 57762,  0, 0,  0, 0, 0,  1, 0,  3, 371, 371, 371, 371, 369, 371, 371},
            {0, 0, 20958,  0, 0,  0, 0, 0,  0, 0,  0,   4,   4,  4,    4,   4,   4,   4},
            {0, 0, 22821,  0, 0,  0, 0, 0,  0, 0,  1, 106, 105,  0,    0,   0,   0,   0}
    };


    @Test
    public void testForDetailsGCCause170() {

        int i = 0;
        for (String name : detailsGCCause170) {
            try {
                Path path = new TestLogFile("cms/parnew/details/gccause/170/" + name).getFile().toPath();
                TestResults testResults = testGenerationalSingleLogFile(path);
                analyzeResults(name, testResults, detailsGCCause170CountsNumberOfDifferentCollectors[i], detailsGCCause170Counts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] detailsGCCause170 = {
            "1.7.0_cms_mixed.log",
            "1.7.0_cms.log",
    };

    private static final int[] detailsGCCause170CountsNumberOfDifferentCollectors = {
            8,
            8,

    };

    private static final int[][] detailsGCCause170Counts = {
          // 0, 1,   2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17
            {0, 0, 603, 0, 0, 0, 0, 0, 0, 0,  0, 13, 13, 13, 13, 13, 13, 13},
            {0, 0, 603, 0, 0, 0, 0, 0, 0, 0, 0, 13, 13, 13, 13, 13, 13, 13},
    };

    @Test
    public void testForDetailsTenuring() {
        int i = 0;
        for (String name : detailsTenuring) {
            try {
                Path path = new TestLogFile("cms/parnew/details/tenuring/" + name).getFile().toPath();
                TestResults testResults = testGenerationalSingleLogFile(path);
                analyzeResults(name, testResults, detailsTenuringCountsNumberOfDifferentCollectors[i], detailsTenuringCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] detailsTenuring = {
            "9f1dfc0c-4dab-4e41-892c-7f4c01c91e70-1396054737854.log",
            "36bbe73f-181c-485b-b162-de2731668227-1390297131935.log",
            "239_1280857178635par.cms.wd.wt.log",
            "705_pa1.log",
            "706_pa2.gc",
            "707_pa3.gc",
            "860_gc.log",
            "863_gc.log",
            "929_def.cms.wd.wt.log",
            "BF653AA1-F897-4279-9570-2E4837D311E8-1405428823225.log.gz", //todo: missing many statements that should be filtered out
            "cms-gc-06102014.log.zip",
            "gc.log",
            "node1.gc",
            "node2.gc",
            "node3.gc",
            "par.cms.wd.wt.log",
            "par.cms.wd.wt.nopp.log",
            "par.cms.wd.wt.webapp.log",
            "web1-gc.log"
    };

    private static final int[] detailsTenuringCountsNumberOfDifferentCollectors = {
             1,
             8,
            11,
             8,
             8,
             8,
            10,
            13,
            11,
             8,
             8,
             8,
             9,
             9,
             9,
            11,
             7,
            10,
             8
    };

    private static final int[][] detailsTenuringCounts = {
           // 0, 1,     2, 3, 4,     5,  6, 7,  8, 9,   10,    11,    12,    13,    14,    15,    16,    17
            { 0, 0,    12, 0, 0,     0,  0, 0,  0, 0,    0,     0,     0,     0,     0,     0,     0,     0},
            { 0, 0, 14816, 0, 0,     0,  0, 0,  0, 0,    0,    30,    30,    30,    30,    30,    30,    30 },
            { 0, 0,  3370, 4, 0,     5,  0, 0,  0, 0,    3,    16,    12,    16,    16,    13,    12,    11},
            { 0, 0, 35995, 0, 0,    15,  0, 0,  0, 0,    0, 13676, 13673, 13673, 13673,     0, 13672, 13672},
            { 0, 0, 30334, 0, 0,    12,  0, 0,  0, 0,    0, 12339, 12336, 12336, 12336,     0, 12336, 12336},
            { 0, 0, 34285, 0, 0,    14,  0, 0,  0, 0,    0, 13354, 13350, 13350, 13350,     0, 13349, 13349},  // todo: audit cmf count feels wrong
            { 0, 0,  4592, 0, 0,     5,  0, 0,  0, 0,    3,    45,    43,    45,    45,    17,    43,    40},
            { 0, 0,    47, 1, 0,    22, 17, 0, 19, 0,    2,   210,   172,   210,   200,   178,   172,   172},  // todo: audit expected 210 - 22 = 188, not 172
            { 0, 0,  3369, 6, 0,     6,  0, 0,  0, 0,    3,    17,    11,    17,    17,    15,    11,    11},
            { 0, 0,  5305, 0, 0,     0,  0, 0,  0, 0,    0,   918,   917,   917,   917,   915,   917,   917},
            { 0, 0,   146, 0, 0,     0,  0, 0,  0, 0,    0,  1402,  1402,  1402,  1402,  1375,  1402,  1402},
            { 0, 0,  4046, 0, 0,     0,  0, 0,  0, 0,    0,    15,    15,    15,    15,     2,    15,    15},
            { 0, 0, 46228, 0, 0,     0,  0, 0,  0, 0,   17,     1,     1,     1,     1,     1,     1,     1},
            { 0, 0, 47000, 0, 0,     0,  0, 0,  0, 0,   15,     2,     2,     2,     2,     2,     2,     2},
            { 0, 0, 48222, 0, 0,     0,  0, 0,  0, 0,   17,     1,     1,     1,     1,     1,     1,     1},
            { 0, 0,  3370, 4, 0,     5,  0, 0,  0, 0,    3,    16,    12,    16,    16,    13,    12,    11},
            { 0, 0,  1333, 0, 0,     0,  0, 0,  0, 0,    0,     1,     1,     1,     1,     0,     1,     1},
            { 0, 0,   508, 0, 0,     0,  1, 0,  0, 0, 1395,     5,     4,     5,     5,     5,     4,     4},  // todo: audit, expected 5 at the end
            { 0, 0, 23829, 0, 0,     0,  0, 0,  0, 0,    0,   978,   977,   977,   977,   959,   975,   975}   // todo: audit, how were 2 concurrent cycles lost
    };

    @Test
    public void testForDefNewDetails() {

        int i = 0;
        for (String name : defNewDetailsLog) {
            try {
                Path path = new TestLogFile("cms/defnew/details/" + name).getFile().toPath();
                TestResults testResults = testGenerationalSingleLogFile(path);
                analyzeResults(name, testResults, defNewDetailsNumberOfDifferentCollectors[i], defnewDetailsCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] defNewDetailsLog = {
            "defnew.log"
    };

    private static final int[] defNewDetailsNumberOfDifferentCollectors = {
            8,

    };

    private static final int[][] defnewDetailsCounts = {
          // 0,     1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17
            {0, 19114, 0, 0, 0, 0, 0, 0, 0, 0,  0, 26, 26, 26, 26,  2, 26, 26}
    };

    @Test
    public void testForParNewDetailsTenuringReference80() {
        int i = 0;
        for (String name : parNewDetailsTenuringReference180) {
            try {
                Path path = new TestLogFile("cms/parnew/details/tenuring/180/" + name).getFile().toPath();
                TestResults testResults = testGenerationalSingleLogFile(path);
                analyzeResults(name, testResults, parNewDetailsTenuringReference180NumberOfDifferentCollectors[i], parNewDetailsTenuringReference180Counts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] parNewDetailsTenuringReference180 = {
            "parnew-reference.log"
    };

    private static final int[] parNewDetailsTenuringReference180NumberOfDifferentCollectors = {
            11

    };

    private static final int[][] parNewDetailsTenuringReference180Counts = {
          // 0, 1,     2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17
            {0, 0, 15338, 0, 0, 3, 0, 0, 3, 0,  7,  7,  1,  7,  1,  1,  1,  1}
    };


    @Test
    public void testForBrokenGCLockerWithTLAB() {
        int i = 0;
        for (String name : parNewDetailsTenuringTLAB) {
            try {
                Path path = new TestLogFile("cms/parnew/details/tenuring/tlab/" + name).getFile().toPath();
                TestResults testResults = testGenerationalSingleLogFile(path);
                analyzeResults(name, testResults, parNewDetailsTenuringTLABNumberOfDifferentCollectors[i], parNewDetailsTenuringTLABCounts[i++]);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private static final String[] parNewDetailsTenuringTLAB = {
            "tlab_broken_gc_locker.log"
            //"tlab_broken_gc_locker _fragment.txt"
    };

    private static final int[] parNewDetailsTenuringTLABNumberOfDifferentCollectors = {
            8,

    };

    private static final int[][] parNewDetailsTenuringTLABCounts = {
          // 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17
            {0, 0, 9, 0, 0, 0, 0, 0, 0, 0,  0,  1,  1,  1,  1,  1,  1,  1}
    };
}
