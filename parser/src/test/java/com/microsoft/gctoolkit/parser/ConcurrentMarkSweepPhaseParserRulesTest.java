// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.GCToolKit;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static com.microsoft.gctoolkit.parser.CommonTestHelper.captureTest;
import static com.microsoft.gctoolkit.parser.ICMSPatterns.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrentMarkSweepPhaseParserRulesTest implements CMSPatterns {

    private static final Logger LOGGER = Logger.getLogger(ConcurrentMarkSweepPhaseParserRulesTest.class.getName());

    @Test
    public void testCMSParseRules() {
        assertEquals(rules.length,lines.length, "rules length != # of groups of lines to be test");
        for (int i = 0; i < rules.length; i++) {
            for (int j = 0; j < lines.length; j++) {
                int captured = captureTest(rules[i], lines[j]);
                if (i == j) {
                    assertEquals(captured, lines[j].length, i + " failed to captured it's lines");
                } else {
                    assertEquals(0, captured, i + " captured " + j);
                }
            }
        }

        assertTrue(true);
    }


    /* Code that is useful when testing individual records */

    //@Test
    public void testDebugCMSParseRules() {
        int index = 8;
        for (String line : lines[index])
            evaluate(rules[index], line);
    }


    private void evaluate(GCParseRule rule, String string) {

        GCLogTrace trace = rule.parse(string);
        assertNotNull(trace);
        trace.notYetImplemented();
        // Enable debugging by setting gctoolkit.debug to true
        GCToolKit.LOG_DEBUG_MESSAGE(() -> {
            StringBuilder sb = new StringBuilder("matches groups " + trace.groupCount());
            for (int i = 0; i <= trace.groupCount(); i++) {
                sb.append(String.format("%n%d : %s", i, trace.getGroup(i))) ;
            }
            return sb.toString();
        });
    }

    //2015-08-04T19:50:56.691-0400: 168027.353: [GC[YG occupancy: 64589 K (720896 K)]2015-08-04T19:50:56.693-0400: 168027.355: [GC 168027.355: [ParNew
    //109.194: [weak refs processing, 0.0000112 secs]109.194: [class unloading, 0.0018913 secs]109.196: [scrub symbol & string tables, 0.0026648 secs] [1 CMS-remark: 3327853K(4194304K)] 3892284K(5872064K), 0.0626457 secs]
    //168027.437: [Rescan (parallel) , 0.0387024 secs]168027.478: [weak refs processing, 0.0700188 secs] [1 CMS-remark: 1396579K(2293760K)] 1403108K(3014656K), 0.2027973 secs] [Times: user=1.25 sys=0.06, real=0.20 secs]
    private GCParseRule[] rules = {
            SPLIT_REMARK,
            PRECLEAN_REFERENCE,
            CORRUPTED_PARNEW_BODY,
            PARNEW_DETAILS_WITH_CONCURRENT_MODE_FAILURE,
            iCMS_PARNEW_PROMOTION_FAILURE_RECORD, //todo: iCMS test???
            iCMS_FULL,
            iCMS_PARNEW_PROMOTION_FAILURE,
            PARNEW_CONCURRENT_MODE_FAILURE_PERM,
    };

    private String[][] lines = {
            {   //  0
                "2015-08-04T19:50:56.691-0400: 168027.353: [GC[YG occupancy: 64589 K (720896 K)]2015-08-04T19:50:56.693-0400: 168027.355: [GC 168027.355: [ParNew"
            },
            {   //  1
                //168027.437: [Rescan (parallel) , 0.0387024 secs]168027.478: [weak refs processing, 0.0700188 secs] [1 CMS-remark: 1396579K(2293760K)] 1403108K(3014656K), 0.2027973 secs] [Times: user=1.25 sys=0.06, real=0.20 secs]
                "2016-10-06T08:48:07.320+0200: 2002.085: [Preclean SoftReferences, 0.0000050 secs]2016-10-06T08:48:07.320+0200: 2002.085: [Preclean WeakReferences, 0.0000283 secs]2016-10-06T08:48:07.320+0200: 2002.085: [Preclean FinalReferences, 0.0000036 secs]2016-10-06T08:48:07.320+0200: 2002.085: [Preclean PhantomReferences, 0.0000354 secs]2016-10-06T08:48:07.330+0200: 2002.095: [CMS-concurrent-preclean: 0.010/0.011 secs] [Times: user=0.01 sys=0.00, real=0.01 secs]",
                //"2016-10-06T08:48:07.320+0200: 2002,085: [Preclean SoftReferences, 0,0000050 secs]2016-10-06T08:48:07.320+0200: 2002,085: [Preclean WeakReferences, 0,0000283 secs]2016-10-06T08:48:07.320+0200: 2002,085: [Preclean FinalReferences, 0,0000036 secs]2016-10-06T08:48:07.320+0200: 2002,085: [Preclean PhantomReferences, 0,0000354 secs]2016-10-06T08:48:07.330+0200: 2002,095: [CMS-concurrent-preclean: 0,010/0,011 secs] [Times: user=0,01 sys=0,00, real=0,01 secs]"
            },
            {   //  2
                "(promotion failed): 118016K->118016K(118016K), 0.0288030 secs]17740.440: [CMS (concurrent mode failure): 914159K->311550K(917504K), 0.5495730 secs] 985384K->311550K(1035520K), [CMS Perm : 65977K->65950K(131072K)], 0.5785090 secs] [Times: user=0.67 sys=0.01, real=0.58 secs]"
            },
            {   //  3
                ": 19134K->19136K(19136K), 0.0493809 secs]236.955: [CMS: 107351K->79265K(107776K), 0.2540576 secs] 119733K->79265K(126912K), [CMS Perm : 14256K->14256K(24092K)], 0.3036551 secs]",
                ": 1069879K->1069879K(1090560K), 0.3135220 secs]2014-09-19T06:07:23.135+0200: 73512.294: [CMS: 1613084K->823344K(2423488K), 3.5186340 secs] 2639961K->823344K(3514048K), [CMS Perm : 205976K->205949K(343356K)], 3.8323790 secs] [Times: user=4.44 sys=0.00, real=3.83 secs]"
            },
            {   //  4
                "445909.040: [GC 445909.040: [ParNew (1: promotion failure size = 4629668)  (promotion failed): 460096K->460096K(460096K), 0.8467130 secs]445909.887: [CMS: 6252252K->2709964K(7877440K), 23.9213270 secs] 6637399K->2709964K(8337536K), [CMS Perm : 1201406K->68157K(2097152K)] icms_dc=0 , 24.7685320 secs]"
            },
            {   //  5
                "2013-06-06T14:12:49.554+0200: 534744,148: [Full GC (System) 534744,148: [CMS: 1513767K->410320K(3512768K), 2,1361260 secs] 1598422K->410320K(4126208K), [CMS Perm : 120074K->119963K(200424K)] icms_dc=0 , 2,1363550 secs]",
                "619930,816: [Full GC 619930,816: [CMS: 3512768K->3512767K(3512768K), 17,8327610 secs] 4126207K->4073974K(4126208K), [CMS Perm : 120062K->120053K(201192K)] icms_dc=100 , 17,8329750 secs]"
            },
            {   //  6
                "2016-08-17T10:41:41.088-0500: 76388.020: [GC (Allocation Failure) 2016-08-17T10:41:41.088-0500: 76388.020: [ParNew (promotion failed): 428321K->431492K(471872K), 0.1049894 secs]2016-08-17T10:41:41.193-0500: 76388.125: [CMS: 466697K->186596K(524288K), 0.7357085 secs] 893715K->186596K(996160K), [Metaspace: 46524K->46524K(1097728K)] icms_dc=0 , 0.8408171 secs] [Times: user=0.91 sys=0.00, real=0.84 secs]",
                "72825.712: [GC72825.712: [ParNew (promotion failed): 153344K->153344K(153344K), 0.3895590 secs]72826.102: [CMS: 1831960K->1554399K(1926784K), 8.3796720 secs] 1914705K->1554399K(2080128K), [Metaspace: 131704K->130876K(222768K)] icms_dc=42 , 8.7694530 secs]"
            },
            {   //  7
                "2015-02-04T17:36:07.103-0500: 199626.435: [GC 199626.435: [ParNew: 812672K->812672K(914240K), 0.0000400 secs]199626.435: [CMS (concurrent mode failure): 1071394K->1081343K(1081344K), 6.8504740 secs] 1884066K->1092775K(1995584K), [CMS Perm : 99417K->99411K(524288K)], 6.8510440 secs] [Times: user=6.63 sys=0.02, real=6.85 secs]",
                "199626.435: [GC 199626.435: [ParNew: 812672K->812672K(914240K), 0.0000400 secs]199626.435: [CMS (concurrent mode failure): 1071394K->1081343K(1081344K), 6.8504740 secs] 1884066K->1092775K(1995584K), [CMS Perm : 99417K->99411K(524288K)], 6.8510440 secs] [Times: user=6.63 sys=0.02, real=6.85 secs]",
                "199626.435: [GC 199626.435: [ParNew: 812672K->812672K(914240K), 0.0000400 secs]199626.435: [CMS (concurrent mode failure): 1071394K->1081343K(1081344K), 6.8504740 secs] 1884066K->1092775K(1995584K), [Metaspace: 99417K->99411K(524288K)], 6.8510440 secs] [Times: user=6.63 sys=0.02, real=6.85 secs]"
            }
    };
}
