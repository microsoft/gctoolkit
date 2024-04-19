// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.generational.CMSRemark;

import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class PreUnifiedGCLogParser extends GCLogParser {

    private static final Logger LOGGER = Logger.getLogger(PreUnifiedGCLogParser.class.getName());
    private final GCParseRule TIMESTAMP_BLOCK = new GCParseRule("TIMESTAMP_BLOCK", "^" + DATE_TIMESTAMP);

    public PreUnifiedGCLogParser() {}

    void advanceClock(String record) {
        try {
            GCLogTrace trace = TIMESTAMP_BLOCK.parse(record);
            if (trace == null) {
                trace = CMSPatterns.ABORT_PRECLEAN_DUE_TO_TIME_CLAUSE.parse(record);
                if (trace == null)
                    return;
            }
            super.advanceClock(trace.getDateTimeStamp());
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, "[PARSING ERROR] " + record, t);
        }
    }

    void recordRescanStepTimes(CMSRemark collection, String line) {
        GCLogTrace clause;
        double unloading = 0.0d, symbolTable = 0.0d, stringTable = 0.0d, stringAndSymbolTable = 0.0d;

        if ((clause = CLASS_UNLOADING.parse(line)) != null)
            unloading = clause.getDoubleGroup(6);
        if ((clause = SYMBOL_TABLE_SCRUB.parse(line)) != null)
            symbolTable = clause.getDoubleGroup(6);
        if ((clause = STRING_TABLE_SCRUB.parse(line)) != null)
            stringTable = clause.getDoubleGroup(6);
        if ((clause = STRING_AND_SYMBOL_SCRUB.parse(line)) != null)
            stringAndSymbolTable = clause.getDoubleGroup(6);

        collection.addClassUnloadingAndStringTableProcessingDurations(unloading, symbolTable, stringTable, stringAndSymbolTable);
    }
}
