// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;


public interface GenericTokens {
    // Primitives
    String DECIMAL_POINT = "(?:\\.|,)";
    String INTEGER = "\\d+";
    String REAL_NUMBER = INTEGER + DECIMAL_POINT + INTEGER;
    String PERCENTAGE = "(" + REAL_NUMBER + ")" + "\\s?%";
    String INT_PERCENTAGE = "(" + INTEGER + ")" + "%";
    String HEX = "0x[0-9,a-f]{16}";
    String INT = "(" + INTEGER + ")";
    String COUNTER = INT;
    String BYTES = INT;
    String REAL_VALUE = "(" + REAL_NUMBER + ")";
    String UNITS = "([B,K,M,G])";

    //Time
    String TIME = "(-?" + REAL_NUMBER + ")";
    String DURATION_MS = TIME + "\\s?ms";
    String INT_DURATION_MS = INTEGER + "ms";
    //0.0700188
    String PAUSE_TIME = TIME + "\\s?(?:secs?|ms)";
    String CONCURRENT_TIME = PAUSE_TIME;

    // Date values
    String DATE = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+|-]\\d{4}";

    // Post 1.7.0_40 clauses
    //"(\\(.+?\\)\\s?)? ?"; this rule was too liberal
    //"(\\([a-zA-Z\\. 1]+?\\(?\\){1,2})?\\s*"; this rule works, but I prefer the one below
    String GC_CAUSE = "(\\([G1,A-Z,a-z, ,-,.gc\\(\\)]+\\))?\\s*";
    //  (Diagnostic Command) (System.gc())
    String SAFE_POINT_CAUSE = "\"(.+)\"";

    // CMS cycles and Perm Space report using this format
    String MEMORY_SIZE = "(" + INTEGER + ")" + UNITS;
    String CHURN = "(" + INTEGER + ") " + UNITS + "B/s";
    String OCCUPANCY_CONFIGURED = MEMORY_SIZE + "\\(" + MEMORY_SIZE + "\\)";
    String OCCUPANCY_CONFIGURED_PAUSE = OCCUPANCY_CONFIGURED + ", " + PAUSE_TIME;
    String BEFORE_CONFIGURED_AFTER_CONFIGURED = OCCUPANCY_CONFIGURED + "->" + OCCUPANCY_CONFIGURED;
    String BEFORE_AFTER_CONFIGURED = MEMORY_SIZE + "->" + MEMORY_SIZE + "\\(" + MEMORY_SIZE + "\\)";
    String BEFORE_AFTER_CONFIGURED_PAUSE = BEFORE_AFTER_CONFIGURED + ",? ?" + PAUSE_TIME;   //todo: ",? ?" is a hack but anything else will destabilize all rule harvesting

    // 1.8.0 tokens and rules
    String META_RECORD = "\\[Metaspace: " + BEFORE_AFTER_CONFIGURED + "\\]";
    String CPU_SUMMARY = "\\[Times: user=" + REAL_VALUE + " sys=" + REAL_VALUE + ", real=" + PAUSE_TIME + "\\]";
}
