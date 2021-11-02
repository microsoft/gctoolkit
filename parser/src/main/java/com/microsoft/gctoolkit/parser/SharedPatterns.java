// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

/**
 * TODO: #157 There needs to be a clearer separation of what is a Token vs what is a SharedPattern
 * <p>
 * Shared generational rules
 */
public interface SharedPatterns extends PreUnifiedTokens {

    String WEAK_REF_BLOCK = DATE_TIMESTAMP + "\\[weak refs processing, " + PAUSE_TIME + "\\]";
    String CLASS_UNLOADING_BLOCK = DATE_TIMESTAMP + "\\[class unloading, " + PAUSE_TIME + "\\]";
    String STRING_TABLE_SCRUB_BLOCK = DATE_TIMESTAMP + "\\[scrub string table, " + PAUSE_TIME + "\\]";
    String SYMBOL_TABLE_SCRUB_BLOCK = DATE_TIMESTAMP + "\\[scrub symbol table, " + PAUSE_TIME + "\\]";
    String STRING_AND_SYMBOL_SCRUB_BLOCK = DATE_TIMESTAMP + "\\[scrub symbol & string tables, " + PAUSE_TIME + "\\]";

    GCParseRule OCCUPANCY_CONFIGURED_RULE = new GCParseRule("OCCUPANCY_CONFIGURED_RULE", OCCUPANCY_CONFIGURED);
    GCParseRule MEMORY_SUMMARY_RULE = new GCParseRule("MEMORY_SUMMARY_RULE", BEFORE_AFTER_CONFIGURED);
    GCParseRule BEFORE_AFTER_CONFIGURED_PAUSE_RULE = new GCParseRule("BEFORE_AFTER_CONFIGURED_PAUSE_RULE", BEFORE_AFTER_CONFIGURED_PAUSE);
    GCParseRule WEAK_REF = new GCParseRule("WEAK_REF", WEAK_REF_BLOCK);
    GCParseRule CLASS_UNLOADING = new GCParseRule("CLASS_UNLOADING", CLASS_UNLOADING_BLOCK);
    GCParseRule STRING_TABLE_SCRUB = new GCParseRule("STRING_TABLE_SCRUB", STRING_TABLE_SCRUB_BLOCK);
    GCParseRule SYMBOL_TABLE_SCRUB = new GCParseRule("SYMBOL_TABLE_SCRUB", SYMBOL_TABLE_SCRUB_BLOCK);
    GCParseRule STRING_AND_SYMBOL_SCRUB = new GCParseRule("STRING_AND_SYMBOL_SCRUB", STRING_AND_SYMBOL_SCRUB_BLOCK);

    // Tenuring Details
    //: 17337K->347K(18624K), 0.0007142 secs] 23862K->6872K(81280K), 0.0008003 secs]
    GCParseRule TENURING_DETAILS = new GCParseRule("TENURING_DETAILS", "^: " + BEFORE_AFTER_CONFIGURED + ", " + PAUSE_TIME + "\\] " + BEFORE_AFTER_CONFIGURED + ", " + PAUSE_TIME + "\\]");

    // Perm space (pre 1.8.0) record
    GCParseRule PERM_SPACE_RECORD = new GCParseRule("PERM_SPACE_RECORD", PERM_RECORD);

    // Metaspace record (1.8.0+)
    GCParseRule META_SPACE_RECORD = new GCParseRule("META_SPACE_RECORD", META_RECORD);
}
