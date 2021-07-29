// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.test.diary;

import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.parser.jvm.JVMConfiguration;
import com.microsoft.gctoolkit.parser.test.TestLogFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class CMSDefNewLogDiaryTest extends LogDiaryTest {

    @Test
    public void testForCMSDefNewDetails() {
        int i = 0;
        for (String name : cmsDefNewDetails) {
            TestLogFile logFile = new TestLogFile(name);
            JVMConfiguration diary = null;
            try {
                diary = getJVMConfiguration(new SingleGCLogFile(logFile.getFile().toPath()));
            } catch (Exception e) {
                fail(e.getMessage());
            }
            interrogateDiary(diary, name, cmsDefNewDetailsDiary[i]);
            lookForUnknowns(diary, name, cmsDefNewDetailsUnknown[i]);
            lookForKnowns(diary, name, cmsDefNewDetailsKnown[i++]);
        }
    }


    private static String[] cmsDefNewDetails = {
            "cms/defnew/details/defnew.log"
    };

    private static boolean[][] cmsDefNewDetailsDiary = {
            //   0      1     2      3     4      5      6      7      8      9     10     11    12     13     14     15     16    17     18     19     20     21     22     23     24     25     26     27
            {false, false, true, false, true, false, false, false, false, false, false, false, true, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false}
    };

    private static int[][] cmsDefNewDetailsUnknown = {
            {18}
    };

    private static int[][] cmsDefNewDetailsKnown = {
            {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,19,20,21,22,23,24,25,26,27}
    };
}
