// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.diary;

import com.microsoft.gctoolkit.parser.TestLogFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ShenandoahLogDiaryTest extends LogDiaryTest {


    @Test
    public void testForShenandoahLogDiaryPickups() {
        int i = 0;
        for (String name : shenandoahLogs) {
            testWith(new TestLogFile("shenandoah/" + name).getFile(), name, shenandoahDiary[i], shenandoahUnknown[i], shenandoahKnown[i++]);
        }
    }


    private static final String[] shenandoahLogs = {
            "shenandoah.log.0"
    };


    private static final boolean[][] shenandoahDiary = {
            //    0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18     19     20     21     22     23     24     25     26     27
            { false, false, false, false, false, false, false, false, false, false, false,  true,  true, false,  true, false,  true, false, false, false,  true, false, false, false, false, false, false, false}
    };

    // todo: The Diary process for Shenandoah is immature leaving these attributes undiscovered. This should change as the diary matures for Shenandoah
    private static final int[][] shenandoahUnknown = {
            {-1}
    };

    private static final int[][] shenandoahKnown = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27}
    };
}
