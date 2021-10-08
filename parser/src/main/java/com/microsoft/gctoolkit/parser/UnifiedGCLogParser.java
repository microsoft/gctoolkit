// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.parser.jvm.Decorators;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;

import java.util.logging.Level;
import java.util.logging.Logger;

abstract class UnifiedGCLogParser extends GCLogParser {

    private static final Logger LOGGER = Logger.getLogger(UnifiedGCLogParser.class.getName());
    private static final boolean DEBUG = Boolean.getBoolean("microsoft.debug");

    UnifiedGCLogParser(LoggingDiary diary, JVMEventConsumer consumer) {
        super(diary, consumer);
    }

    void advanceClock(String record) {
        try {
            DateTimeStamp now = new Decorators(record).getDateTimeStamp();
            super.advanceClock(now);
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, "[PARSING ERROR] " + record, t);
        }
    }

    void notYetImplemented(GCLogTrace trace, String line) {
        trace.notYetImplemented();
    }

    /**
     * Some log entries require no actions
     */
    void noop() {
        if (DEBUG)
            System.out.println("noop");
    }
}
