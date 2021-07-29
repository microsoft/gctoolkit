// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.parser.UnifiedG1GCParser;
import com.microsoft.gctoolkit.parser.UnifiedGenerationalParser;
import com.microsoft.gctoolkit.parser.UnifiedJVMEventParser;
import com.microsoft.gctoolkit.parser.UnifiedSurvivorMemoryPoolParser;
import com.microsoft.gctoolkit.parser.ZGCParser;
import com.microsoft.gctoolkit.parser.jvm.JVMConfiguration;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;
import com.microsoft.gctoolkit.vertx.CensumVertx;
import com.microsoft.gctoolkit.vertx.aggregator.AggregatorVerticle;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/* package scope */ class CensumVertxParametersForUnifiedLogs extends CensumVertxParameters {

    private static final Logger LOGGER = Logger.getLogger(CensumVertxParametersForUnifiedLogs.class.getName());

    private final Set<LogFileParser> logFileParsers;
    private final Set<AggregatorVerticle> aggregatorVerticles;
    private final String mailBox;

    /* package */ CensumVertxParametersForUnifiedLogs(
            Set<Class<? extends Aggregation>> registeredAggregations,
            JVMConfiguration jvmConfiguration) {
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
            logFileParsers.add(new LogFileParser(CensumVertx.PARSER_INBOX, CensumVertx.JVM_EVENT_PARSER_OUTBOX, consumer -> new UnifiedJVMEventParser(diary, consumer)));
        }

        if (diary.isTenuringDistribution()) {
            logFileParsers.add(new LogFileParser(CensumVertx.PARSER_INBOX, CensumVertx.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX, consumer -> new UnifiedSurvivorMemoryPoolParser(diary, consumer)));
        }

        if (diary.isGenerational()) {
            logFileParsers.add(new LogFileParser(CensumVertx.PARSER_INBOX, CensumVertx.GENERATIONAL_HEAP_PARSER_OUTBOX, consumer -> new UnifiedGenerationalParser(diary, consumer)));
        }

        if (diary.isG1GC()) {
            logFileParsers.add(new LogFileParser(CensumVertx.PARSER_INBOX, CensumVertx.G1GC_PARSER_OUTBOX, consumer -> new UnifiedG1GCParser(diary, consumer)));
        }

        if (diary.isZGC()) {
            logFileParsers.add(new LogFileParser(CensumVertx.PARSER_INBOX, CensumVertx.ZGC_PARSER_OUTBOX, consumer -> new ZGCParser(diary, consumer)));
        }

        if (diary.isShenandoah()) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        return logFileParsers;
    }

    private Set<AggregatorVerticle> initAggregatorVerticles(
            Set<Class<? extends Aggregation>> registeredAggregations,
            JVMConfiguration jvmConfiguration) {
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
        }

        if (diary.isShenandoah()) {
            Set<Aggregator<?>> aggregators = getAggregators(EventSource.SHENANDOAH, registeredAggregations);
            if (aggregators != null && !aggregators.isEmpty()) {
                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(CensumVertx.SHENANDOAH_PARSER_OUTBOX);
                aggregators.forEach(aggregatorVerticle::registerAggregator);
                aggregatorVerticles.add(aggregatorVerticle);
            }
        }

        if (diary.isZGC()) {
            Set<Aggregator<?>> aggregators = getAggregators(EventSource.ZGC, registeredAggregations);
            if (aggregators != null && !aggregators.isEmpty()) {
                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(CensumVertx.ZGC_PARSER_OUTBOX);
                aggregators.forEach(aggregatorVerticle::registerAggregator);
                aggregatorVerticles.add(aggregatorVerticle);
            }
        }

        if (diary.isTenuringDistribution()) {
            Set<Aggregator<?>> aggregators = getAggregators(EventSource.SURVIVOR, registeredAggregations);
            if (aggregators != null && !aggregators.isEmpty()) {
                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(CensumVertx.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX);
                aggregators.forEach(aggregatorVerticle::registerAggregator);
                aggregatorVerticles.add(aggregatorVerticle);
            }
        }

        return aggregatorVerticles;
    }

    private String initMailBox(JVMConfiguration jvmConfiguration) {
        final LoggingDiary diary = jvmConfiguration.getDiary();
        if ( diary.isG1GC()) return CensumVertx.G1GC_PARSER_OUTBOX;
        if ( diary.isZGC()) return CensumVertx.ZGC_PARSER_OUTBOX;
        if ( diary.isShenandoah()) return CensumVertx.SHENANDOAH_PARSER_OUTBOX;
        return CensumVertx.GENERATIONAL_HEAP_PARSER_OUTBOX;
    }

}
