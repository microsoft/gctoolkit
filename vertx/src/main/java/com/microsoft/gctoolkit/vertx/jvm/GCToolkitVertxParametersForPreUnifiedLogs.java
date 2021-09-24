// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.parser.CMSTenuredPoolParser;
import com.microsoft.gctoolkit.parser.GenerationalHeapParser;
import com.microsoft.gctoolkit.parser.JVMEventParser;
import com.microsoft.gctoolkit.parser.PreUnifiedG1GCParser;
import com.microsoft.gctoolkit.parser.SurvivorMemoryPoolParser;
import com.microsoft.gctoolkit.parser.jvm.JVMConfiguration;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;
import com.microsoft.gctoolkit.vertx.GCToolkitVertx;
import com.microsoft.gctoolkit.vertx.aggregator.AggregatorVerticle;

import java.util.HashSet;
import java.util.Set;

/* package */ class GCToolkitVertxParametersForPreUnifiedLogs extends GCToolkitVertxParameters {

    public static final String SAFEPOINT_OUTBOX = "SafepointParser";

    private final Set<LogFileParser> logFileParsers;
    private final Set<AggregatorVerticle> aggregatorVerticles;
    private final String mailBox;

    /* package */ GCToolkitVertxParametersForPreUnifiedLogs(
            Set<Class<? extends Aggregation>> registeredAggregations,
            JVMConfiguration jvmConfiguration)
    {
        logFileParsers = initLogFileParsers(jvmConfiguration);
        aggregatorVerticles = initAggregatorVerticles(registeredAggregations, jvmConfiguration);
        mailBox = initMailBox(jvmConfiguration);
    }

    @Override
    public Set<LogFileParser> logFileParsers() {
        return logFileParsers;
    }

    @Override
    public Set<AggregatorVerticle> aggregatorVerticles() {
        return aggregatorVerticles;
    }

    @Override
    public String mailBox() {
        return mailBox;
    }

    private  Set<LogFileParser> initLogFileParsers(JVMConfiguration jvmConfiguration) {
        Set<LogFileParser> logFileParsers = new HashSet<>();
        final LoggingDiary diary = jvmConfiguration.getDiary();

        if (jvmConfiguration.hasJVMEvents()) {
            logFileParsers.add(new LogFileParser(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.JVM_EVENT_PARSER_OUTBOX, consumer -> new JVMEventParser(diary, consumer)));
        }

        if (diary.isTenuringDistribution()) {
            logFileParsers.add(new LogFileParser(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX, consumer -> new SurvivorMemoryPoolParser(diary, consumer)));
        }

        if (diary.isGenerational()) {
            logFileParsers.add(new LogFileParser(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.GENERATIONAL_HEAP_PARSER_OUTBOX, consumer -> new GenerationalHeapParser(diary, consumer)));
            if (diary.isCMS()) {
                logFileParsers.add(new LogFileParser(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.CMS_TENURED_POOL_PARSER_OUTBOX, consumer -> new CMSTenuredPoolParser(diary, consumer)));
            }
        }

        if (diary.isG1GC()) {
            logFileParsers.add(new LogFileParser(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.G1GC_PARSER_OUTBOX, consumer -> new PreUnifiedG1GCParser(diary, consumer)));
        }

        if (diary.isZGC()) {
            throw new UnsupportedOperationException("Internal GCToolKit Error: " + getClass().getName() + " cannot process ZGC");
        }

        if (diary.isShenandoah()) {
            throw new UnsupportedOperationException("Internal GCToolKit Error: " + getClass().getName() + " cannot process Shenandoah");
        }

        return logFileParsers;

    }

    private Set<AggregatorVerticle> initAggregatorVerticles(
            Set<Class<? extends Aggregation>> registeredAggregations,
            JVMConfiguration jvmConfiguration) 
    {
        final Set<AggregatorVerticle> aggregatorVerticles = new HashSet<>();
        final LoggingDiary diary = jvmConfiguration.getDiary();

        if (diary.isG1GC()) {
            Set<Aggregator<?>> aggregators = getAggregators(EventSource.G1GC, registeredAggregations);
            if (aggregators != null && !aggregators.isEmpty()) {
                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.G1GC_PARSER_OUTBOX);
                aggregators.forEach(aggregatorVerticle::registerAggregator);
                aggregatorVerticles.add(aggregatorVerticle);
            }
        }

        if (diary.isGenerational()) {
            Set<Aggregator<?>> aggregators = getAggregators(EventSource.GENERATIONAL, registeredAggregations);
            if (aggregators != null && !aggregators.isEmpty()) {
                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.GENERATIONAL_HEAP_PARSER_OUTBOX);
                aggregators.forEach(aggregatorVerticle::registerAggregator);
                aggregatorVerticles.add(aggregatorVerticle);
            }
            if (diary.isCMS()) {
                Set<Aggregator<?>> cmsAggregators = getAggregators(EventSource.TENURED, registeredAggregations);
                if (cmsAggregators != null && !cmsAggregators.isEmpty()) {
                    AggregatorVerticle cmsAggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.CMS_TENURED_POOL_PARSER_OUTBOX);
                    cmsAggregators.forEach(cmsAggregatorVerticle::registerAggregator);
                    aggregatorVerticles.add(cmsAggregatorVerticle);
                }
            }
        }

        if (diary.isZGC()) {
            throw new IllegalStateException("Internal GCToolKit Error: " + getClass().getName() + " cannot process ZGC");
        }

        if (diary.isShenandoah()) {
            throw new IllegalStateException("Internal GCToolKit Error: " + getClass().getName() + " cannot process Shenandoah");
        }

        if (diary.isTenuringDistribution()) {
            Set<Aggregator<?>> aggregators = getAggregators(EventSource.SURVIVOR, registeredAggregations);
            if (aggregators != null && !aggregators.isEmpty()) {
                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX);
                aggregators.forEach(aggregatorVerticle::registerAggregator);
                aggregatorVerticles.add(aggregatorVerticle);
            }
        }

        Set<Aggregator<?>> safepointAggregators = getAggregators(EventSource.SAFEPOINT, registeredAggregations);
        if (safepointAggregators != null && !safepointAggregators.isEmpty()) {
            AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(SAFEPOINT_OUTBOX);
            safepointAggregators.forEach(aggregatorVerticle::registerAggregator);
            aggregatorVerticles.add(aggregatorVerticle);
        }

        return aggregatorVerticles;
    }

    private String initMailBox(JVMConfiguration jvmConfiguration) {
        final LoggingDiary diary = jvmConfiguration.getDiary();
        if ( diary.isG1GC()) return GCToolkitVertx.G1GC_PARSER_OUTBOX;
        if ( diary.isGenerational())
            return (diary.isCMS()) ? GCToolkitVertx.CMS_TENURED_POOL_PARSER_OUTBOX : GCToolkitVertx.GENERATIONAL_HEAP_PARSER_OUTBOX;
        if ( diary.isZGC()) return GCToolkitVertx.ZGC_PARSER_OUTBOX;
        if ( diary.isShenandoah()) return GCToolkitVertx.SHENANDOAH_PARSER_OUTBOX;
        return GCToolkitVertx.GENERATIONAL_HEAP_PARSER_OUTBOX;

    }

}
