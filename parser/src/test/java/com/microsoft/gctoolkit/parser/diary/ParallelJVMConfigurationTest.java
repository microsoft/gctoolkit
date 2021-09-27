// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.diary;

import org.junit.jupiter.api.Test;

public class ParallelJVMConfigurationTest extends LogDiaryTest {

    @Test
    public void testForDetails() {
        int i = 0;
        for (String name : details) {
            testWith(name, detailsDiary[i], detailsUnknown[i], detailsKnown[i++]);
        }
    }

    @Test
    public void testFor170GCCause() {
        int i = 0;
        for (String name : a170gcCause) {
            testWith("ps/details/gccause/170/" + name, a170gcCauseDiary[i], a170gcCauseUnknown[i], a170CauseKnown[i++]);
        }
    }

    @Test
    public void testFor180GCCause() {
        int i = 0;
        for (String name : a180gcCause) {
            testWith("ps/details/gccause/180/" + name, a180gcCauseDiary[i], a180gcCauseUnknown[i], a180gcCauseKnown[i++]);
        }
    }

    @Test
    public void testForDetailsTenuring() {
        int i = 0;
        for (String name : detailsTenuring) {
            testWith(name, detailsTenuringDiary[i], detailsTenuringUnknown[i], detailsTenuringKnown[i++]);
        }
    }

    @Test
    public void testForDetailsTenuring80() {
        int i = 0;
        for (String name : detailsTenuring80) {
            testWith("ps/details/tenuring/gccause/180/" + name, detailsTenuring80Diary[i], detailsTenuring80Unknown[i], detailsTenuring80Known[i++]);
        }
    }

    private static final String[] details = {
            "ps/details/1074_2.log",
            "ps/details/854_gcstats_rdms3_20110124.txt",
            "ps/details/932_def.par.wd.nt.log",
            "ps/details/951_par.par.wd.nt.log",
            "ps/details/959_gc.log.lasttest",
            "ps/details/960_old.log",
            "ps/details/gc_23am_18users_ok.log",
            "ps/details/par.oldpar.wd.nt.ast.log",
            "ps/details/par.par.wd.nt.log",
            "ps/details/premature2.log",
            "ps/details/premature3.log",
            "ps/details/psyg.cms.wd.nt.log",
            "ps/details/gc_ccpcc01_late.log",
            "ps/details/memoryleak1.log",
            "ps/details/memoryleak2.log"
    };

    private static final boolean[][] detailsDiary = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12,    13,    14,    15,    16,    17,    18,    19,    20,    21,    22,    23,    24,    25,    26,    27
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},  //18 is unknown
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},  //18 is unknown
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},  //18 is unknown
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true,  true, false, false,  true, false, false, false, false, false, false},
             { true,  true, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},  //18 is unknown
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},  //18 is unknown
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},  //18 is unknown
             { true,  true, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true, false, false, false,  true, false, false, false, false, false, false},  //18 is unknown
             { true,  true, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},  //18 is unknown
             { true,  true, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true, false, false, false,  true, false, false, false, false, false, false},  //18 is unknown
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false}
    };

    private static final int[][] detailsUnknown = {
            {18}, {-1}, {-1}, {-1},
            {18}, {13, 18}, {-1}, {18},
            {-1}, {18}, {18}, {18},
            {18}, {18}, {-1}
    };

    private static final int[][] detailsKnown = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
    };

    private static final String[] a170gcCause = {
            "1.7.0.parallel.log",
            "psyoung.log",
            "psyoung.nt.wd.log"
    };

    private static final boolean[][] a170gcCauseDiary = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12,    13,    14,    15,    16,    17,    18,    19,    20,    21,    22,    23,    24,    25,    26,    27
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false,  true, false, false,  true, false, false, false, false, false, false, false},
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true,  true,  true, false, false,  true, false, false, false, false, false, false, false},
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true, false,  true, false, false,  true, false, false, false, false, false, false, false},
    };

    private static final int[][] a170gcCauseUnknown = {
            {-1},
            {-1},
            {-1}
    };

    private static final int[][] a170CauseKnown = {
            {-1},
            {-1},
            {-1}
    };

    private static final String[] a180gcCause = {
            "ps.negative.recovery80.log"
    };

    //PrintTenuringDistribution is set but it cannot be seen in the log as there are only Full collections.
    private static final boolean[][] a180gcCauseDiary = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12,    13,    14,    15,    16,    17,    18,    19,    20,    21,    22,    23,    24,    25,    26,    27
             { true, false, false, false, false, false,  true,  true, false, false, false, false,  true,  true,  true, false, false, false, false,  true, false, false, false, false, false},
    };

    private static final int[][] a180gcCauseKnown = {
            {-1}
    };

    private static final int[][] a180gcCauseUnknown = {
            {-1}
    };

    private static final String[] detailsTenuring = {
            "ps/details/tenuring/853_gc.log",
            "ps/details/tenuring/949_par.par.wd.wt.log",
            "ps/details/tenuring/933_def.par.wd.wt.log",
            "ps/details/tenuring/fib_billion_6.vgc",
            "ps/details/tenuring/gc-Instance31-20121207-1645.log",
            "ps/details/tenuring/p_app_01_gc.log.zip",
            "ps/details/tenuring/p_app_02_gc.log.zip",
            "ps/details/tenuring/p_util_01_gc.log.zip",
            "ps/details/tenuring/premature1.vgc",
            "ps/details/tenuring/psy.par.wd.wt.log"
    };

    private static final boolean[][] detailsTenuringDiary = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12,    13,    14,    15,    16,    17,    18,    19,    20,    21,    22,    23,    24,    25,    26,    27
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false},
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false},
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false},
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false},
             { true, false, false, false, false, false,  true,  true, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false},
             { true,  true, false, false, false, false,  true,  true, false, false, false, false,  true,  true, false, false, false,  true, false, false, false,  true, false, false, false},
             { true,  true, false, false, false, false,  true,  true, false, false, false, false,  true,  true, false, false, false,  true, false, false, false,  true, false, false, false},
             { true,  true, false, false, false, false,  true,  true, false, false, false, false,  true,  true, false, false, false,  true, false, false, false,  true, false, false, false},
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false},
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false}
    };

    private static final int[][] detailsTenuringUnknown = {
            {-1},
            {-1}, {-1}, {-1}, {-1},
            {-1},
            {-1}, {-1}, {-1},
            {-1}
    };

    private static final int[][] detailsTenuringKnown = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25}
    };

    private static final String[] detailsTenuring80 = {
            "1398331617.vgc"
    };

    private static final boolean[][] detailsTenuring80Diary = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12,    13,    14,    15,    16,    17,    18,    19,    20,    21,    22,    23,    24,    25,    26,    27
             {false, false, false, false, false, false,  true,  true, false, false, false, false,  true,  true,  true, false, false, false, false,  true, false, false, false, false, false}
    };

    private static final int[][] detailsTenuring80Unknown = {
            {-1}
    };

    private static final int[][] detailsTenuring80Known = {
            {-1}
    };

    @Test
    public void testForDetailsTenuringAdaptiveSizing() {
        int i = 0;
        for (String name : detailsTenuringAdpativeSizing) {
            testWith(name, detailsTenuringAdaptiveSizingDiary[i], detailsTenuringAdaptiveSizingDiaryUnknown[i], detailsTenuringAdaptiveSizingDiaryKnown[i++]);
        }
    }

    private static final String[] detailsTenuringAdpativeSizing = {
            "ps/details/tenuring/adaptive_sizing/icap.log"
    };

    private static final boolean[][] detailsTenuringAdaptiveSizingDiary = {
            //    0,     1,     2,     3,     4,     5,     6,     7,     8,     9,    10,    11,    12,    13,    14,    15,    16,    17,    18,    19,    20,    21,    22,    23,    24,    25,    26,    27
             { true, false, false, false, false, false,  true,  true, false, false,  false, false, true,  true,  true, false,  true, false, false,  true, false, false, false, false, false}
    };

    private static final int[][] detailsTenuringAdaptiveSizingDiaryUnknown = {
            {-1}
    };

    private static final int[][] detailsTenuringAdaptiveSizingDiaryKnown = {
            {-1}
    };
}
