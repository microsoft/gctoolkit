// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.parser.jvm.Decorators;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base class for parsers that process GC logs produced by the
 * {@code -Xlog:gc*} unified logging framework introduced with JEP 158.
 * <p>
 * This parser provides common functionality shared by all unified GC log
 * parsers, such as advancing the internal {@link com.microsoft.gctoolkit.time.DateTimeStamp}
 * based on the decorators present in a unified log record.
 * </p>
 * <p>
 * Third-party extensions should subclass {@code UnifiedGCLogParser} when
 * implementing parsers for new or customized unified GC log formats. In most
 * cases, subclasses are expected to:
 * <ul>
 *     <li>Override the parsing and event-dispatch methods defined in
 *     {@link GCLogParser} to interpret individual log lines and emit
 *     the appropriate GC events.</li>
 *     <li>Use {@link #advanceClock(String)} to keep the parser's notion of
 *     time in sync with the timestamps or decorators found in each log record.</li>
 *     <li>Optionally use {@link #notYetImplemented(GCLogTrace, String)} and
 *     {@link #noop()} as helpers when handling log entries that are not yet
 *     supported or require no action.</li>
 * </ul>
 * This class itself does not implement concrete parsing logic; that
 * responsibility is delegated to subclasses tailored to specific unified
 * GC log variants.
 */
public abstract class UnifiedGCLogParser extends GCLogParser {

    private static final Logger LOGGER = Logger.getLogger(UnifiedGCLogParser.class.getName());
    private static final boolean DEBUG = Boolean.getBoolean("microsoft.debug");

    public UnifiedGCLogParser() {}

    protected void advanceClock(String record) {
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
