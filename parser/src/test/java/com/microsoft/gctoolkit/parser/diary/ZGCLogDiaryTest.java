// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.diary;

import com.microsoft.gctoolkit.parser.TestLogFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ZGCLogDiaryTest extends LogDiaryTest {


    @Test
    public void testForZGCLogDiaryPickups() {
        int i = 0;
        for (String name : zgcLogs) {
            testWith(new TestLogFile("zgc/" + name).getFile(), name, zgcDiary[i], zgcUnknown[i], zgcKnown[i++]);
        }
    }


    private static final String[] zgcLogs = {
            "zgc.log"
    };

    private static final boolean[][] zgcDiary = {
            //    0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18     19     20     21     22     23     24     25     26     27
            {  true,  true, false, false, false, false, false, false, false, false,  true, false,  true, false,  true, false, false, false, false, false,  true,  true, false,  true, false, false, false, false}
    };

    private static final int[][] zgcUnknown = {
            {-1}
    };

    private static final int[][] zgcKnown = {
            {0, 1, 2,3,4,5,6,7,8,9,10,11, 12, 13, 14, 15,16, 17,18,19,20,21, 22, 23, 24, 25, 26,27}
    };
}
