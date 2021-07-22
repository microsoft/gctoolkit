// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.vertx.jvm;

import com.microsoft.censum.aggregator.Aggregation;
import com.microsoft.censum.aggregator.Aggregator;
import com.microsoft.censum.aggregator.EventSource;
import com.microsoft.censum.parser.CMSTenuredPoolParser;
import com.microsoft.censum.parser.GenerationalHeapParser;
import com.microsoft.censum.parser.JVMEventParser;
import com.microsoft.censum.parser.PreUnifiedG1GCParser;
import com.microsoft.censum.parser.SurvivorMemoryPoolParser;
import com.microsoft.censum.parser.jvm.JVMConfiguration;
import com.microsoft.censum.parser.jvm.LoggingDiary;
import com.microsoft.censum.vertx.CensumVertx;
import com.microsoft.censum.vertx.aggregator.AggregatorVerticle;

import java.util.HashSet;
import java.util.Set;

/* package */ class CensumVertxParametersForPreUnifiedLogs extends CensumVertxParameters {

    public static final String SAFEPOINT_OUTBOX = "SafepointParser";

    private final Set<LogFileParser> logFileParsers;
    private final Set<AggregatorVerticle> aggregatorVerticles;
    private final String mailBox;

    /* package */ CensumVertxParametersForPreUnifiedLogs(
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
            logFileParsers.add(new LogFileParser(CensumVertx.PARSER_INBOX, CensumVertx.JVM_EVENT_PARSER_OUTBOX, consumer -> new JVMEventParser(diary, consumer)));
        }

        if (diary.isTenuringDistribution()) {
            logFileParsers.add(new LogFileParser(CensumVertx.PARSER_INBOX, CensumVertx.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX, consumer -> new SurvivorMemoryPoolParser(diary, consumer)));
        }

        if (diary.isGenerational()) {
            logFileParsers.add(new LogFileParser(CensumVertx.PARSER_INBOX, CensumVertx.GENERATIONAL_HEAP_PARSER_OUTBOX, consumer -> new GenerationalHeapParser(diary, consumer)));
            if (diary.isCMS()) {
                logFileParsers.add(new LogFileParser(CensumVertx.PARSER_INBOX, CensumVertx.CMS_TENURED_POOL_PARSER_OUTBOX, consumer -> new CMSTenuredPoolParser(diary, consumer)));
            }
        }

        if (diary.isG1GC()) {
            logFileParsers.add(new LogFileParser(CensumVertx.PARSER_INBOX, CensumVertx.G1GC_PARSER_OUTBOX, consumer -> new PreUnifiedG1GCParser(diary, consumer)));
        }

        if (diary.isZGC()) {
            throw new UnsupportedOperationException("Internal Censum Error: " + getClass().getName() + " cannot process ZGC");
        }

        if (diary.isShenandoah()) {
            throw new UnsupportedOperationException("Internal Censum Error: " + getClass().getName() + " cannot process Shenandoah");
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
                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(CensumVertx.G1GC_PARSER_OUTBOX);
                aggregators.forEach(aggregatorVerticle::registerAggregator);
                aggregatorVerticles.add(aggregatorVerticle);
            }
        }

        if (diary.isGenerational()) {
            Set<Aggregator<?>> aggregators = getAggregators(EventSource.GENERATIONAL, registeredAggregations);
            if (aggregators != null && !aggregators.isEmpty()) {
                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(CensumVertx.GENERATIONAL_HEAP_PARSER_OUTBOX);
                aggregators.forEach(aggregatorVerticle::registerAggregator);
                aggregatorVerticles.add(aggregatorVerticle);
            }
            if (diary.isCMS()) {
                Set<Aggregator<?>> cmsAggregators = getAggregators(EventSource.TENURED, registeredAggregations);
                if (cmsAggregators != null && !cmsAggregators.isEmpty()) {
                    AggregatorVerticle cmsAggregatorVerticle = new AggregatorVerticle(CensumVertx.CMS_TENURED_POOL_PARSER_OUTBOX);
                    cmsAggregators.forEach(cmsAggregatorVerticle::registerAggregator);
                    aggregatorVerticles.add(cmsAggregatorVerticle);
                }
            }
        }

        if (diary.isZGC()) {
            throw new IllegalStateException("Internal Censum Error: " + getClass().getName() + " cannot process ZGC");
        }

        if (diary.isShenandoah()) {
            throw new IllegalStateException("Internal Censum Error: " + getClass().getName() + " cannot process Shenandoah");
        }

        if (diary.isTenuringDistribution()) {
            Set<Aggregator<?>> aggregators = getAggregators(EventSource.SURVIVOR, registeredAggregations);
            if (aggregators != null && !aggregators.isEmpty()) {
                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(CensumVertx.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX);
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
        if ( diary.isG1GC()) return CensumVertx.G1GC_PARSER_OUTBOX;
        if ( diary.isGenerational())
            return (diary.isCMS()) ? CensumVertx.CMS_TENURED_POOL_PARSER_OUTBOX : CensumVertx.GENERATIONAL_HEAP_PARSER_OUTBOX;
        if ( diary.isZGC()) return CensumVertx.ZGC_PARSER_OUTBOX;
        if ( diary.isShenandoah()) return CensumVertx.SHENANDOAH_PARSER_OUTBOX;
        return CensumVertx.GENERATIONAL_HEAP_PARSER_OUTBOX;

    }

}
