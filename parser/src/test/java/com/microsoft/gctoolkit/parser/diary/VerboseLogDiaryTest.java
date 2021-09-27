// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.diary;

import com.microsoft.gctoolkit.parser.TestLogFile;
import org.junit.jupiter.api.Test;

public class VerboseLogDiaryTest extends LogDiaryTest {

    @Test
    public void testForVerbose() {
        int i = 0;
        for (String name : verbose) {
            testWith(new TestLogFile("verbose/" + name).getFile(), name, verboseDiary[i], verboseUnknown[i], verboseKnown[i++]);
        }
    }

    @Test
    public void testForVerboseTenuring() {
        int i = 0;
        for (String name : verboseTenuring) {
            testWith(new TestLogFile("verbose/tenuring/" + name).getFile(), name, verboseTenuringDiary[i], verboseTenuringUnknown[i], verboseTenuringKnown[i++]);
        }
    }


    private static final String[] verbose = {
            "282_sun_gc.log",
            "600_gc.log",
            "755_gc.log",
            "777_gc_flat.log",
            "814_gc_interesting.log"
    };

    private static final boolean[][] verboseDiary = {
            //   0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18     19     20     21     22     23     24     25     26     27
            {false, false, false, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private static final int[][] verboseUnknown = {
            {-1},
            {-1},
            {-1},
            {-1},
            {-1}
    };

    private static final int[][] verboseKnown = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
    };

    private static final String[] verboseTenuring = {
            "931_def.par.nd.wt.log",
            "943_par.def.nd.wt.log",
            "947_par.par.nd.wt.log"
    };

    private static final boolean[][] verboseTenuringDiary = {
            //   0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18     19     20     21     22     23     24     25     26     27
            {false, false, false, false, false, false,  true,  true, false, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true, false, false, false, false,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false,  true,  true, false, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private static final int[][] verboseTenuringUnknown = {
            {-1},
            {-1},
            {-1}
    };

    private static final int[][] verboseTenuringKnown = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27}
    };
}
