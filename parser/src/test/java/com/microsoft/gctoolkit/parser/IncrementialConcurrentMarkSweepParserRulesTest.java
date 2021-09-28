// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IncrementialConcurrentMarkSweepParserRulesTest implements ICMSPatterns {

    private static final Logger LOGGER = Logger.getLogger(IncrementialConcurrentMarkSweepParserRulesTest.class.getName());

    private final GCParseRule[] rules = {
            iCMS_FULL,
            iCMS_PARNEW,
            iCMS_PARNEW_PROMOTION_FAILURE_RECORD,
            iCMS_PARNEW_PROMOTION_FAILURE
    };

    private String[][] lines = {
            {
                    "2015-09-02T12:08:51.106+0200: 36341.777: [Full GC (System) 2015-09-02T12:08:51.106+0200: 36341.778: [CMS: 3067214K->899382K(3397600K), 5.5353585 secs] 3234199K->899382K(4160928K), [CMS Perm : 160338K->160194K(262144K)] icms_dc=3 , 5.5414287 secs]"
            },
            {
                    "2015-06-26T17:40:28.063+0100: 268.102: [GC2015-06-26T17:40:28.064+0100: 268.102: [ParNew: 547627K->28112K(613440K), 0.0768200 secs] 547627K->28112K(16709120K) icms_dc=5 , 0.0771220 secs] [Times: user=0.40 sys=0.07, real=0.08 secs]"
            },
            {
                    "2015-08-14T12:57:03.064+0100: 445909.040: [GC 445909.040: [ParNew (1: promotion failure size = 4629668)  (promotion failed): 460096K->460096K(460096K), 0.8467130 secs]445909.887: [CMS: 6252252K->2709964K(7877440K), 23.9213270 secs] 6637399K->2709964K(8337536K), [CMS Perm : 1201406K->68157K(2097152K)] icms_dc=0 , 24.7685320 secs] [Times: user=26.72 sys=0.17, real=24.77 secs]"
            },
            {
                    "2016-08-17T10:41:41.088-0500: 76388.020: [GC (Allocation Failure) 2016-08-17T10:41:41.088-0500: 76388.020: [ParNew (promotion failed): 428321K->431492K(471872K), 0.1049894 secs]2016-08-17T10:41:41.193-0500: 76388.125: [CMS: 466697K->186596K(524288K), 0.7357085 secs] 893715K->186596K(996160K), [Metaspace: 46524K->46524K(1097728K)] icms_dc=0 , 0.8408171 secs] [Times: user=0.91 sys=0.00, real=0.84 secs]",
                    "263204.684: [GC 263204.684: [ParNew (promotion failed): 153342K->153343K(153344K), 0.0910130 secs]263204.776: [CMS: 1752518K->1636004K(1926784K), 8.9443330 secs] 1889511K->1636004K(2080128K), [CMS Perm : 137004K->135442K(233916K)] icms_dc=75 , 9.0355990 secs] [Times: user=9.31 sys=0.00, real=9.04 secs]"
            }
    };

    @Test
    public void testiCMSParseRules() {
        for (int i = 0; i < rules.length; i++)
            for (int j = 0; j < lines.length; j++) {
                for (int k = 0; k < lines[j].length; k++) {
                    GCLogTrace trace = rules[i].parse(lines[j][k]);
                    if (trace != null) {
                        assertTrue(i == j, "rule @" + i + " matched record @" + j + ":" + k);
                    } else {
                        assertTrue(i != j, "Rule missed @" + i + ":" + k);
                    }

                }
            }
    }

    private void evaluate(GCParseRule rule, String string, boolean debugging) {
        GCLogTrace trace = rule.parse(string);
        assertTrue(trace != null);
        if (debugging) {
            LOGGER.fine("matches groups " + trace.groupCount());
            for (int i = 0; i <= trace.groupCount(); i++) {
                LOGGER.fine(i + ": " + trace.getGroup(i));
            }
        }
    }

}
