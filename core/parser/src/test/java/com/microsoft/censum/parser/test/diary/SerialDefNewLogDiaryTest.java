// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.parser.test.diary;

import com.microsoft.censum.parser.test.TestLogFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SerialDefNewLogDiaryTest extends LogDiaryTest {

    @Test
    public void testForSerialDefNewDetails() {
        int i = 0;
        for (String name : serialDefNewDetails) {
            testWith(new TestLogFile(name).getFile(), name, serialDefNewDetailsDiary[i], serialDefNewDetailsUnknown[i], serialDefNewDetailsKnown[i++]);
        }
    }

    @Test
    public void testForSerialDefNewDetailsTenuring() {
        int i = 0;
        for (String name : serialDefNewDetailsTenuring) {
            testWith(new TestLogFile(name).getFile(), name, serialDefNewDetailsTenuringDiary[i], serialDefNewDetailsTenuringUnknown[i], serialDefNewDetailsTenuringKnown[i++]);
        }
    }

    @Test
    public void testForSerialDefNewDetailsTenuring180() {
        int i = 0;
        for (String name : serialDefNewDetailsTenuring180) {
            testWith(new TestLogFile(name).getFile(), name, serialDefNewDetailsTenuring180Diary[i], serialDefNewDetailsTenuring180Unknown[i], serialDefNewDetailsTenuring180Known[i++]);
        }
    }


    private static String[] serialDefNewDetails = {
            "serial/defnew/details/server_app2_1_gc_2012-10-31_18h07min24s.log"
    };

    private static boolean[][] serialDefNewDetailsDiary = {
            //   0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18     19     20     21     22     23     24     25     26     27
            {false, false,  true, false, false, false, false, false,  true, false, false, false,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false, false, false}
    };

    private static int[][] serialDefNewDetailsUnknown = {
            {18}
    };

    private static int[][] serialDefNewDetailsKnown = {
            {-1}
    };

    private static String[] serialDefNewDetailsTenuring = {
            "serial/defnew/details/tenuring/amit.barcap.log"
    };

    private static boolean[][] serialDefNewDetailsTenuringDiary = {
            //   0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18     19     20     21     22     23     24     25     26     27
            {false, false,  true, false, false, false, false, false,  true, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false}
    };

    private static int[][] serialDefNewDetailsTenuringUnknown = {
            {-1}
    };

    private static int[][] serialDefNewDetailsTenuringKnown = {
            {18}
    };

    private static String[] serialDefNewDetailsTenuring180 = {
            "serial/defnew/details/tenuring/180/serial180.log"
    };

    private static boolean[][] serialDefNewDetailsTenuring180Diary = {
            //   0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18     19     20     21     22     23     24     25     26     27
            {false, false,  true, false, false, false, false, false,  true, false, false, false,  true,  true,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false}
    };

    private static int[][] serialDefNewDetailsTenuring180Unknown = {
            {}
    };

    private static int[][] serialDefNewDetailsTenuring180Known = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 , 21, 22, 23, 24, 25, 26, 27}
    };
}
