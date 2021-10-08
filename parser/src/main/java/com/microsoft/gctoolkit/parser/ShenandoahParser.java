// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.parser.collection.MRUQueue;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;
import com.microsoft.gctoolkit.parser.unified.ShenandoahPatterns;

import java.util.AbstractMap;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Time of GC
 * GCType
 * Collect total heap values
 * Heap before collection
 * Heap after collection
 * Heap configured size
 * total pause time
 * CMS failures
 * System.gc() calls
 */

public class ShenandoahParser extends UnifiedGCLogParser implements ShenandoahPatterns {

    private static final Logger LOGGER = Logger.getLogger(ShenandoahParser.class.getName());

    private final boolean debugging = Boolean.getBoolean("microsoft.debug");
    private final boolean develop = Boolean.getBoolean("microsoft.develop");

    private final MRUQueue<GCParseRule, BiConsumer<GCLogTrace, String>> parseRules;

    {
        parseRules = new MRUQueue<>();
        parseRules.put(END_OF_FILE,this::endOfFile);
    }

    public ShenandoahParser(LoggingDiary diary, JVMEventConsumer consumer) {
        super(diary, consumer);
    }

    @Override
    public String getName() {
        return "Shenandoah Parser";
    }

    @Override
    protected void process(String line) {

        if (ignoreFrequentButUnwantedEntries(line)) return;

        try {
            Optional<AbstractMap.SimpleEntry<GCParseRule, GCLogTrace>> optional = parseRules.keys()
                    .stream()
                    .map(rule -> new AbstractMap.SimpleEntry<>(rule, rule.parse(line)))
                    .filter(tuple -> tuple.getValue() != null)
                    .findFirst();
            if ( optional.isPresent()) {
                AbstractMap.SimpleEntry<GCParseRule, GCLogTrace> ruleAndTrace = optional.get();
                parseRules.get(ruleAndTrace.getKey()).accept(ruleAndTrace.getValue(), line);
                return;
            }
        } catch (Throwable t) {
            LOGGER.throwing(this.getName(), "process", t);
        }

        log(line);
    }

    // TODO populate with lines that should be ignored
    private final boolean inPrintHeapAtGC = false;

    private boolean ignoreFrequentButUnwantedEntries(String line) {
        return false;
    }

    public void endOfFile(GCLogTrace trace, String line) {
        record(new JVMTermination(getClock()));
    }

    //Implement all capture methods

    private void log(String line) {
        if (debugging)
            LOGGER.log(Level.FINE,"ZGCHeapParser missed: {0}", line);
        LOGGER.log(Level.WARNING, "Missed: {0}", line);

    }

    public void record() {
        record(forwardReference.toShenandoahCycle());
    }

    public void record(JVMEvent event) {
        consumer.record(event);
        forwardReference = null;
    }

    private ShenandoahForwardReference forwardReference;

    private class ShenandoahForwardReference {

        JVMEvent toShenandoahCycle() {
            return null;
        }
    }


}
