// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.message.DataSourceParser;

import java.util.HashSet;
import java.util.Set;

class GCToolkitVertxParametersForPreUnifiedLogs extends GCToolkitVertxParameters {

    public static final String SAFEPOINT_OUTBOX = "SafepointParser";

//    private final Set<DataSourceBus> parsers;
//    private final Set<JVMEventConsumer> aggregators;
    private final String mailBox;

    /* package */ GCToolkitVertxParametersForPreUnifiedLogs( Set<Class<? extends Aggregation>> registeredAggregations, Diary diary) {
//        parsers = initLogFileParsers(diary);
//        aggregatorVerticles = initAggregatorVerticles(registeredAggregations, diary);
        mailBox = initMailBox(diary);
    }

    private  Set<DataSourceParser> initLogFileParsers(Diary diary) {
        Set<DataSourceParser> logFileParsers = new HashSet<>();

//        if (diary.isApplicationRunningTime() || diary.isApplicationStoppedTime()) {
//            logFileParsers.add(new LogFileParser(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.JVM_EVENT_PARSER_OUTBOX, consumer -> new JVMEventParser(diary, consumer)));
//            logFileParsers.add(new LogFileParser(GCToolkitVertx.PARSER_INBOX, SAFEPOINT_OUTBOX, consumer -> new JVMEventParser(diary, consumer)));
//        }
//
//        if (diary.isTenuringDistribution()) {
//            logFileParsers.add(new LogFileParser(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX, consumer -> new SurvivorMemoryPoolParser(diary, consumer)));
//        }
//
//        if (diary.isGenerational()) {
//            logFileParsers.add(new LogFileParser(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.GENERATIONAL_HEAP_PARSER_OUTBOX, consumer -> new GenerationalHeapParser(diary, consumer)));
//            if (diary.isCMS()) {
//                logFileParsers.add(new LogFileParser(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.CMS_TENURED_POOL_PARSER_OUTBOX, consumer -> new CMSTenuredPoolParser(diary, consumer)));
//            }
//        }
//
//        if (diary.isG1GC()) {
//            logFileParsers.add(new LogFileParser(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.G1GC_PARSER_OUTBOX, consumer -> new PreUnifiedG1GCParser(diary, consumer)));
//        }
//
//        if (diary.isZGC()) {
//            throw new UnsupportedOperationException("Internal GCToolKit Error: " + getClass().getName() + " cannot process ZGC");
//        }
//
//        if (diary.isShenandoah()) {
//            throw new UnsupportedOperationException("Internal GCToolKit Error: " + getClass().getName() + " cannot process Shenandoah");
//        }

        return logFileParsers;

    }
//
//    private Set<AggregatorVerticle> initAggregatorVerticles(
//            Set<Class<? extends Aggregation>> registeredAggregations,
//            Diary diary)
//    {
//        final Set<AggregatorVerticle> aggregatorVerticles = new HashSet<>();
//
//        if (diary.isG1GC()) {
//            Set<Aggregator<?>> aggregators = getAggregators(EventSource.G1GC, registeredAggregations);
//            if (aggregators != null && !aggregators.isEmpty()) {
//                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.G1GC_PARSER_OUTBOX);
//                aggregators.forEach(aggregatorVerticle::registerAggregator);
//                aggregatorVerticles.add(aggregatorVerticle);
//            }
//        }
//
//        if (diary.isGenerational()) {
//            Set<Aggregator<?>> aggregators = getAggregators(EventSource.GENERATIONAL, registeredAggregations);
//            if (aggregators != null && !aggregators.isEmpty()) {
//                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.GENERATIONAL_HEAP_PARSER_OUTBOX);
//                aggregators.forEach(aggregatorVerticle::registerAggregator);
//                aggregatorVerticles.add(aggregatorVerticle);
//            }
//            if (diary.isCMS()) {
//                Set<Aggregator<?>> cmsAggregators = getAggregators(EventSource.TENURED, registeredAggregations);
//                if (cmsAggregators != null && !cmsAggregators.isEmpty()) {
//                    AggregatorVerticle cmsAggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.CMS_TENURED_POOL_PARSER_OUTBOX);
//                    cmsAggregators.forEach(cmsAggregatorVerticle::registerAggregator);
//                    aggregatorVerticles.add(cmsAggregatorVerticle);
//                }
//            }
//        }
//
//        if (diary.isZGC()) {
//            throw new IllegalStateException("Internal GCToolKit Error: " + getClass().getName() + " cannot process ZGC");
//        }
//
//        if (diary.isShenandoah()) {
//            throw new IllegalStateException("Internal GCToolKit Error: " + getClass().getName() + " cannot process Shenandoah");
//        }
//
//        if (diary.isTenuringDistribution()) {
//            Set<Aggregator<?>> aggregators = getAggregators(EventSource.SURVIVOR, registeredAggregations);
//            if (aggregators != null && !aggregators.isEmpty()) {
//                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX);
//                aggregators.forEach(aggregatorVerticle::registerAggregator);
//                aggregatorVerticles.add(aggregatorVerticle);
//            }
//        }
//
//        if (diary.isApplicationRunningTime() || diary.isApplicationStoppedTime()) {
//            Set<Aggregator<?>> safepointAggregators = getAggregators(EventSource.SAFEPOINT, registeredAggregations);
//	        if (safepointAggregators != null && !safepointAggregators.isEmpty()) {
//	            AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(SAFEPOINT_OUTBOX);
//	            safepointAggregators.forEach(aggregatorVerticle::registerAggregator);
//	            aggregatorVerticles.add(aggregatorVerticle);
//	        }
//        }
//
//        return aggregatorVerticles;
//    }

    private String initMailBox(Diary diary) {
//        if ( diary.isG1GC()) return GCToolkitVertx.G1GC_PARSER_OUTBOX;
//        if ( diary.isGenerational())
//            return (diary.isCMS()) ? GCToolkitVertx.CMS_TENURED_POOL_PARSER_OUTBOX : GCToolkitVertx.GENERATIONAL_HEAP_PARSER_OUTBOX;
//        if ( diary.isZGC()) return GCToolkitVertx.ZGC_PARSER_OUTBOX;
//        if ( diary.isShenandoah()) return GCToolkitVertx.SHENANDOAH_PARSER_OUTBOX;
//        return GCToolkitVertx.GENERATIONAL_HEAP_PARSER_OUTBOX;
        return "";

    }

}
