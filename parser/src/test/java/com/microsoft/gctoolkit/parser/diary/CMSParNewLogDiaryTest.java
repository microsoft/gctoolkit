// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.diary;

import com.microsoft.gctoolkit.parser.TestLogFile;
import org.junit.jupiter.api.Test;

public class CMSParNewLogDiaryTest extends LogDiaryTest {


    @Test
    public void testForCMSParNewDetails() {
        int i = 0;
        for (String name : cmsParNewDetails) {
            testWith(new TestLogFile("cms/parnew/details/" + name).getFile(), name, cmsParNewDetailsDiary[i], cmsParNewDetailsUnknown[i], cmsParNewDetailsKnown[i++]);
        }
    }

    @Test
    public void testForCMSParNewDetailsTenuring() {
        int i = 0;
        for (String name : cmsParNewDetailsTenuring) {
            testWith(new TestLogFile("cms/parnew/details/tenuring/" + name).getFile(), name, cmsParNewDetailsTenuringDiary[i], cmsParNewDetailsTenuringUnknown[i], cmsParNewDetailsTenuringKnown[i++]);
        }
    }

    @Test
    public void testForCMS() {
        int i = 0;
        for (String name : cms) {
            testWith(new TestLogFile("cms/" + name).getFile(), name, cmsDiary[i], cmsUnknown[i], cmsKnown[i++]);
        }
    }

    @Test
    public void testForCMSParNewDetailsGCCause170() {
        int i = 0;
        for (String name : cmsParNewDetailsGCCause170) {
            testWith(new TestLogFile("cms/parnew/details/gccause/170/" + name).getFile(), name, cmsParNewDetailsGCCause170Diary[i], cmsParNewDetailsGCCause170Unknown[i], cmsParNewDetailsGCCause170Known[i++]);
        }
    }


    private static final String[] cmsParNewDetails = {
            "1073_1.log",
            "LS_gc.log.20121116_193624",
            "TC_oy579c2n5.gc.out",
            "apptio_gc.log",
            "gc.2011-09-19.141115.log",
            "GCLog_CMSCollection.log",
            "gc_www1_2013_10_19.log",
            "lead_up_to_problem_gc_www1_2013_10_19.log",
            "sample.leak.workaround.log",
            "tidied_gc_www1_2013_10_19.log"
    };

    private static final boolean[][] cmsParNewDetailsDiary = {
            //    0      1     2     3     4      5      6      7      8      9    10     11     12     13     14    15     16     17     18     19     20     21     22     23     24     25     26     27
            {false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, true,  true, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, true,  true, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, true,  true, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, true, false, false, false, false, false, false, false, true, false, false, false, false, true,  true, false, false, false, false, false, false, false, false, false},
    };

    private static final int[][] cmsParNewDetailsUnknown = {
            {18}, {18}, {18}, {18}, {18}, {18}, {-1}, {-1}, {-1}, {-1}
    };

    private static final int[][] cmsParNewDetailsKnown = {
            {-1}, {-1}, {-1}, {-1}, {-1}, {-1}, {-1}, {-1}, {-1}, {-1}
    };

    private static final String[] cmsParNewDetailsTenuring = {
            "239_1280857178635par.cms.wd.wt.log",
            "36bbe73f-181c-485b-b162-de2731668227-1390297131935.log",
            "705_pa1.log",
            "706_pa2.gc",
            "707_pa3.gc",
            "860_gc.log",
            "863_gc.log",
            "929_def.cms.wd.wt.log",
            "9f1dfc0c-4dab-4e41-892c-7f4c01c91e70-1396054737854.log",
            "gc.log",
            "node1.gc",
            "node2.gc",
            "node3.gc",
            "par.cms.wd.wt.log",
            "par.cms.wd.wt.nopp.log",
            "par.cms.wd.wt.webapp.log",
            "web1-gc.log",
            "cms-gc-06102014.log.zip",
            "BF653AA1-F897-4279-9570-2E4837D311E8-1405428823225.log.gz",
            "parnew.details.tenuring.cause.18020.log"
    };

    private static final boolean[][] cmsParNewDetailsTenuringDiary = {
            //   0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18     19     20     21     22     23     24     25     26     27
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},
            { true,  true, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},
            { true,  true, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},
            { true,  true, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
            { true,  true, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true,  true, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true, false, false, false, false, false, false, false, false, false, false},
            { true, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true, false, false, false,  true, false, false, false,  true, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true,  true,  true, false, false, false, false,  true, false, false, false, false, false, false, false, false}
    };

    private static final int[][] cmsParNewDetailsTenuringUnknown = {
            {-1}, {-1},
            {5, 18}, {-1}, {-1},
            {-1}, {-1}, {-1},
            {5, 18},
            {-1},
            {-1}, {-1}, {-1}, {-1}, {-1},
            {-1}, {-1}, {18}, {18}, {-1}

    };

    private static final int[][] cmsParNewDetailsTenuringKnown = {
            {-1}, {-1},
            {0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {-1}, {-1}, {-1},
            {-1}, {-1},
            {0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {-1}, {-1},
            {-1}, {-1}, {-1}, {-1}, {-1},
            {-1}, {-1}, {0, 1, 2, 6, 7, 8, 9, 10, 11, 15, 19, 20, 21},
            {1, 2, 6, 7, 8, 9, 10, 11, 14, 15, 16, 19},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21}
    };

    private static final String[] cms = {
            "530_gc.log",
            "718_SERVER.gc_100315_163149.log",
            "815_gc.log",
            "862_gc.log",
            "919_gc13.112.txt",
            "938_par.cms.nd.nt.log",
            "parnew.cms.nd.log"
    };

    private static final boolean[][] cmsDiary = {
            //    0     1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18     19     20     21     22     23     24     25     26     27
            {false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private static final int[][] cmsUnknown = {
            {-1},
            {-1},
            {-1},
            {-1},
            {-1},
            {-1},
            {-1}
    };

    //known but are not true
    private static final int[][] cmsKnown = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27}
    };

    private static final String[] cmsParNewDetailsGCCause170 = {
            "1.7.0_cms_mixed.log",
            "1.7.0_cms.log"
    };

    private static final boolean[][] cmsParNewDetailsGCCause170Diary = {
            //   0      1      2      3      4      5      6      7      8      9     10     11     12     13     14     15     16     17     18     19     20     21     22     23     24     25     26     27
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true, false,  true, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false,  true,  true, false, false, false, false, false, false, false,  true, false,  true, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private static final int[][] cmsParNewDetailsGCCause170Unknown = {
            {17, 19},
            {17, 19}
    };

    //known but are not true
    private static final int[][] cmsParNewDetailsGCCause170Known = {
            {-1},
            {-1}
    };
}
