// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.parser.test.diary;

import com.microsoft.censum.parser.test.TestLogFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ShenandoahLogDiaryTest extends LogDiaryTest {


    @Test  @Disabled
    public void testForShenandoahLogDiaryPickups() {
        int i = 0;
        for (String name : shenandoahLogs) {
            testWith(new TestLogFile("shenandoah/" + name).getFile(), name, shenandoahDiary[i], shenandoahUnknown[i], shenandoahKnown[i++]);
        }
    }


    private static String[] shenandoahLogs = {
            "shenandoah.log.0"
    };


    private static boolean[][] shenandoahDiary = {
            //    0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18     19     20     21     22     23     24     25     26     27
            { false, false, false, false, false, false, false, false, false, false, false,  true,  true, false,  true, false,  true, false, false, false,  true, false, false, false, false, false, false, false}
    };

    // todo: The Diary process for Shenandoah is immature leaving these attributes undiscovered. This should change as the diary matures for Shenandoah
    private static int[][] shenandoahUnknown = {
            {21, 23, 25}
    };

    private static int[][] shenandoahKnown = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 24, 26, 27}
    };
}
