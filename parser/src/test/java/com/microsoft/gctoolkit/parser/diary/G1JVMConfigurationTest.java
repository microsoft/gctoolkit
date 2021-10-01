// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.diary;

import com.microsoft.gctoolkit.parser.TestLogFile;
import org.junit.jupiter.api.Test;

public class G1JVMConfigurationTest extends LogDiaryTest {


    // Details
    private static final String[] details = {
            "170_51mastermind.log",
            "g1.details.cause.log"
    };

    private static final boolean[][] detailsDiary = {
            //    0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18,    19,    20,    21,    22,    23,    24,    25,    26,    27,
            { false, false, false, false, false, false, false, false, false,  true, false, false,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},
            { false, false, false, false, false, false, false, false, false,  true, false, false,  true, false,  true, false, false,  true, false, false, false, false, false, false, false, false, false, false}
    };

    private static final int[][] detailsUnknown = {
            {-1},
            {-1}
    };

    private static final int[][] detailsKnown = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27}
    };

    @Test
    public void testForDetails() {
        int i = 0;
        for (String name : details) {
            testWith(new TestLogFile("g1gc/details/" + name).getFile(), name, detailsDiary[i], detailsUnknown[i], detailsKnown[i++]);
        }
    }


    // Details Tenuring
    private static final String[] detailsTenuring = {
            "server1-gc.log",
            "server2-gc.log",
            "server3-gc.log",
            "170_45/g1_details_tenuring_cause.log"
    };

    private static final boolean[][] detailsTenuringDiary = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12,    13,    14,    15,    16,    17,    18,    19,    20,    21,    22,    23,    24,    25,    26,    27
            { false, false, false, false, false, false, false, false, false,  true, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false,  true, false, false, false},
            { false, false, false, false, false, false, false, false, false,  true, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false,  true, false, false, false},
            { false, false, false, false, false, false, false, false, false,  true, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false,  true, false, false, false},
            {  true, false, false, false, false, false, false, false, false,  true, false, false,  true,  true,  true, false, false,  true, false, false, false, false, false, false, false, false, false, false}
    };

    private static final int[][] detailsTenuringUnknown = {
            {-1},
            {-1},
            {-1},
            {-1},
    };

    private static final int[][] detailsTenuringKnown = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27}
    };


    @Test
    public void testForDetailsTenuring() {
        int i = 0;
        for (String name : detailsTenuring) {
            testWith(new TestLogFile("g1gc/details/tenuring/" + name).getFile(), name, detailsTenuringDiary[i], detailsTenuringUnknown[i], detailsTenuringKnown[i++]);
        }
    }


    // Details, Tenuring, Rolling, pre 170_40
    private static final String[] detailsTenuringRolling17040 = {
            "ahjapp02a_gclog/ahj_prod_gc.log.0"
    };

    //this is a 7.0_45 log
    private static final boolean[][] detailsTenuringRolling17040Diary = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12,    13,    14,    15,    16,    17,    18,    19,    20,    21,    22,    23,    24,    25,    26,    27
             { true,  true, false, false, false, false, false, false, false,  true, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false, false, false, false}
    };

    private static final int[][] detailsTenuringRolling17040Unknown = {
            {-1}
    };

    private static final int[][] detailsTenuringRolling17040Known = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27}
    };

    @Test
    public void testForDetailsTenuringRolling17040() {
        int i = 0;
        for (String name : detailsTenuringRolling17040) {
            testWith(new TestLogFile("g1gc/details/tenuring/170_45/rolling/" + name).getFile(), name, detailsTenuringRolling17040Diary[i], detailsTenuringRolling17040Unknown[i], detailsTenuringRolling17040Known[i++]);
        }
    }


    //Details Tenuring 1.8.0
    private static final String[] detailsTenuring80 = {
            "neo4j-gc-fragment.log",
            "neo4j-gc.log.20140625"
    };

    private static final boolean[][] detailsTenuring80Diary = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12,    13,    14,    15,    16,    17,    18,    19,    20,    21,    22,    23,    24,    25,    26,    27
            {  true, false, false, false, false, false, false, false, false,  true, false, false,  true,  true,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false},
            {  true, false, false, false, false, false, false, false, false,  true, false, false,  true,  true,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false}
    };

    private static final int[][] detailsTenuring80Unknown = {
            {-1},
            {-1}
    };

    private static final int[][] detailsTenuring80Known = {
            {1, 2, 3, 4, 5, 6, 7, 8, 13, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {1, 2, 3, 4, 5, 6, 7, 8, 13, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27}
    };


    @Test
    public void testForDetailsTenuring80() {
        int i = 0;
        for (String name : detailsTenuring80) {
            testWith(new TestLogFile("g1gc/details/tenuring/180/" + name).getFile(), name, detailsTenuring80Diary[i], detailsTenuring80Unknown[i], detailsTenuring80Known[i++]);
        }
    }


    // Details, Print Reference GC
    private static final String[] detailsPrintReferenceGC = {
            "post17040/170_51_mastermind.log"
    };

    private static final boolean[][] detailsPrintReferenceGCDiary = {
            //    0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18,    19,    20    21,     22,    23,    24     25,    26,    27
            { false, false, false, false, false, false, false, false, false,  true, false, false,  true, false, false, false, false,  true, false, false, false, false, false,  true, false, false, false, false}
    };

    private static final int[][] detailsPrintReferenceGCUnknown = {
            {-1}
    };

    private static final int[][] detailsPrintReferenceGCKnown = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 12, 13, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27}
    };


    @Test
    public void testForDetailsPrintReferenceGC() {
        int i = 0;
        for (String name : detailsPrintReferenceGC) {
            testWith(new TestLogFile("g1gc/details/reference/" + name).getFile(), name, detailsPrintReferenceGCDiary[i], detailsPrintReferenceGCUnknown[i], detailsPrintReferenceGCKnown[i++]);
        }
    }

    // Details, Print Reference GC, is post 1.7.0_40
    private static final String[] detailsAdaptiveSizeRSet = {
            "gc1gc_details_adaptivesizing_rset.log"
    };

    private static final boolean[][] detailsAdaptiveSizeRSetDiary = {
            //   0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18,    19,    20,    21,    22,    23,    24,    25,    26,    27
            {false, false, false, false, false, false, false, false, false,  true, false, false,  true, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false}
    };

    private static final int[][] detailsAdaptiveSizeRSetUnknown = {
            {-1}
    };

    private static final int[][] detailsAdaptiveSizeRSetKnown = {
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27}
    };


    @Test
    public void testForDetailsAdaptiveSizeRSet() {
        int i = 0;
        for (String name : detailsAdaptiveSizeRSet) {
            testWith(new TestLogFile("g1gc/details/adaptivesize/rset/" + name).getFile(), name, detailsAdaptiveSizeRSetDiary[i], detailsAdaptiveSizeRSetUnknown[i], detailsAdaptiveSizeRSetKnown[i++]);
        }
    }

    @Test
    public void testUnifiedG1GC() {
        int i = 0;
        for (String name : unifiedLogs) {
            testWith(new TestLogFile("unified/g1gc/" + name).getFile(), name, unifiedDiary[i], unifiedUnknown[i], unifiedKnown[i++]);
        }
    }

    private static final String[] unifiedLogs = {
            "G1-80-16gbps2.log.0",  // initial log segment
            "G1-80-16gbps2.log"     // log segment
    };

    private static final boolean[][] unifiedDiary = {
            //   0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18,    19,    20,    21,    22,    23,    24,    25,    26,    27
            { true,  true, false, false, false, false, false, false, false,  true, false, false,  true,  true, false, false, false, false, false, false,  true, false, false,  true, false, false, false, false},
            { true,  true, false, false, false, false, false, false, false,  true, false, false,  true,  true, false, false, false, false, false, false,  true, false, false,  true, false, false, false, false}
    };

    private static final int[][] unifiedUnknown = {
            {14,22},
            {14,22}
    };

    private static final int[][] unifiedKnown = {
            {1,2,3,4,5,6,7,8,9,10,11,12,13,15,16,17,18,19,20,21,23,24,25,26,27},
            {1,2,3,4,5,6,7,8,9,10,11,12,13,15,16,17,18,19,20,21,23,24,25,26,27}
    };

}
