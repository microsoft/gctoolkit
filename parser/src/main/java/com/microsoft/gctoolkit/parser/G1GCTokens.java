// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

public interface G1GCTokens extends SharedPatterns {


    String G1GC_PREFIX = "^" + DATE_TIMESTAMP + "\\[GC pause " + GC_CAUSE;

    String G1_SURVIVOR_FROM_TO = REAL_VALUE + "([B,K,M,G])->" + REAL_VALUE + "([B,K,M,G])";
    String G1_OCCUPANCY_CONFIGURED = REAL_VALUE + "([B,K,M,G])\\(" + REAL_VALUE + "([B,K,M,G])\\)";
    String G1_FROM_TO = G1_OCCUPANCY_CONFIGURED + "->" + G1_OCCUPANCY_CONFIGURED;

    String G1_PHASE_TIME_SUMMARY = "Min: " + TIME + ", Avg: " + TIME + ", Max: " + TIME + ", Diff: " + TIME;
    String G1_PHASE_TIME_SUMMARY_SUM = G1_PHASE_TIME_SUMMARY + ", Sum: " + TIME;
    String G1_PHASE_COUNTER_SUMMARY = "Min: (" + INTEGER + "), Avg: " + REAL_VALUE + ", Max: (" + INTEGER + "), Diff: (" + INTEGER + "), Sum: (" + INTEGER + ")";

    String G1_REF_PROC_BLOCK = DATE_TIMESTAMP + "\\[GC ref-proc, " + PAUSE_TIME + "\\]";

    /*
    Pre 1.7.0_40 formatted rules
     */

    String PRE17040_G1_SURVIVOR_FROM_TO = COUNTER + UNITS + "->" + COUNTER + UNITS;
    String PRE17040_G1_OCCUPANCY_CONFIGURED = COUNTER + UNITS + "\\(" + COUNTER + UNITS + "\\)";
    String PRE17040_G1_FROM_TO = PRE17040_G1_OCCUPANCY_CONFIGURED + "->" + PRE17040_G1_OCCUPANCY_CONFIGURED;

}
