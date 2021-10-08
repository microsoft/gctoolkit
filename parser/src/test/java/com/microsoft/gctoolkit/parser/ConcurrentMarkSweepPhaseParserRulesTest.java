// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrentMarkSweepPhaseParserRulesTest implements CMSPatterns {

    private static final Logger LOGGER = Logger.getLogger(ConcurrentMarkSweepPhaseParserRulesTest.class.getName());

    @Test
    public void testCMSParseRules() {
        for (int i = 0; i < rules.length; i++)
            for (int j = 0; j < lines.length; j++) {
                GCLogTrace trace = rules[i].parse(lines[j]);
                if (trace != null) {
                    assertTrue(i == j, i + " captured " + j);
                } else {
                    assertTrue(i != j, i + " captured " + j);
                }
            }

        assertTrue(true);
    }


    /* Code that is useful when testing individual records */

    private final boolean debugging = Boolean.getBoolean("microsoft.debug");

    @Test
    public void testDebugCMSParseRules() {
        int index = 0;
        GCParseRule rule = rules[index];
        evaluate(rule, lines[index], debugging);
    }


    private void evaluate(GCParseRule rule, String string, boolean dump) {

        GCLogTrace trace = rule.parse(string);
        assertTrue(trace != null);
        if (dump) {
            LOGGER.fine("matches groups " + trace.groupCount());
            for (int i = 0; i <= trace.groupCount(); i++) {
                LOGGER.fine(i + ": " + trace.getGroup(i));
            }
        }
    }

    //2015-08-04T19:50:56.691-0400: 168027.353: [GC[YG occupancy: 64589 K (720896 K)]2015-08-04T19:50:56.693-0400: 168027.355: [GC 168027.355: [ParNew
    //109.194: [weak refs processing, 0.0000112 secs]109.194: [class unloading, 0.0018913 secs]109.196: [scrub symbol & string tables, 0.0026648 secs] [1 CMS-remark: 3327853K(4194304K)] 3892284K(5872064K), 0.0626457 secs]
    //168027.437: [Rescan (parallel) , 0.0387024 secs]168027.478: [weak refs processing, 0.0700188 secs] [1 CMS-remark: 1396579K(2293760K)] 1403108K(3014656K), 0.2027973 secs] [Times: user=1.25 sys=0.06, real=0.20 secs]
    private GCParseRule[] rules = {
            SPLIT_REMARK,
            PRECLEAN_REFERENCE
    };

    private String[] lines = {
            "2015-08-04T19:50:56.691-0400: 168027.353: [GC[YG occupancy: 64589 K (720896 K)]2015-08-04T19:50:56.693-0400: 168027.355: [GC 168027.355: [ParNew",
            //"168027.437: [Rescan (parallel) , 0.0387024 secs]168027.478: [weak refs processing, 0.0700188 secs] [1 CMS-remark: 1396579K(2293760K)] 1403108K(3014656K), 0.2027973 secs] [Times: user=1.25 sys=0.06, real=0.20 secs]"
            "2016-10-06T08:48:07.320+0200: 2002,085: [Preclean SoftReferences, 0,0000050 secs]2016-10-06T08:48:07.320+0200: 2002,085: [Preclean WeakReferences, 0,0000283 secs]2016-10-06T08:48:07.320+0200: 2002,085: [Preclean FinalReferences, 0,0000036 secs]2016-10-06T08:48:07.320+0200: 2002,085: [Preclean PhantomReferences, 0,0000354 secs]2016-10-06T08:48:07.330+0200: 2002,095: [CMS-concurrent-preclean: 0,010/0,011 secs] [Times: user=0,01 sys=0,00, real=0,01 secs]"
    };
}
