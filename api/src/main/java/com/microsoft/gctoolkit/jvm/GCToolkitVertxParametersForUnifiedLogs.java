// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.message.DataSourceParser;

import java.util.Set;

class GCToolkitVertxParametersForUnifiedLogs extends GCToolkitVertxParameters {

    private final Set<DataSourceParser> logFileParsers;
    //private final Set<JVMEventConsumer> aggregatorVerticles;
    private final String mailBox;

    /* package */ GCToolkitVertxParametersForUnifiedLogs(
            Set<Class<? extends Aggregation>> registeredAggregations,
            Diary diary) {
        logFileParsers = initLogFileParsers(diary);
        //aggregatorVerticles = initAggregatorVerticles(registeredAggregations, diary);
        mailBox = initMailBox(diary);
    }

//    @Override
//    public Set<DataSourceParser> logFileParsers() {
////      return logFileParsers;
//        return null;
//    }

//    @Override
//    public Set<DataSourceParser> aggregatorVerticles() {
////      return aggregatorVerticles;
//        return null;
//    }

//    @Override
//    public String mailBox() {
//        return mailBox;
//    }


    private  Set<DataSourceParser> initLogFileParsers(Diary diary) {
//        Set<JVMEventConsumer> logFileParsers = new HashSet<>();

//        if (diary.isApplicationStoppedTime() || diary.isApplicationRunningTime()) {
//            logFileParsers.add(new DataSourceConsumer<String>(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.JVM_EVENT_PARSER_OUTBOX, consumer -> new UnifiedJVMEventParser(diary, consumer)));
//        }
//
//        if (diary.isTenuringDistribution()) {
//            logFileParsers.add(new DataSourceConsumer<String>(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX, consumer -> new UnifiedSurvivorMemoryPoolParser(diary, consumer)));
//        }
//
//        if (diary.isGenerational()) {
//            logFileParsers.add(new DataSourceConsumer<String>(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.GENERATIONAL_HEAP_PARSER_OUTBOX, consumer -> new UnifiedGenerationalParser(diary, consumer)));
//        }
//
//        if (diary.isG1GC()) {
//            logFileParsers.add(new DataSourceConsumer<String>(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.G1GC_PARSER_OUTBOX, consumer -> new UnifiedG1GCParser(diary, consumer)));
//        }
//
//        if (diary.isZGC()) {
//            logFileParsers.add(new DataSourceConsumer<String>(GCToolkitVertx.PARSER_INBOX, GCToolkitVertx.ZGC_PARSER_OUTBOX, consumer -> new ZGCParser(diary, consumer)));
//        }
//
//        if (diary.isShenandoah()) {
//            throw new UnsupportedOperationException("Not yet implemented");
//        }

        return logFileParsers;
    }

//    private Set<AggregatorVerticle> initAggregatorVerticles(
//            Set<Class<? extends Aggregation>> registeredAggregations,
//            Diary diary) {
////        final Set<AggregatorVerticle> aggregatorVerticles = new HashSet<>();
//
//        if (diary.isG1GC()) {
//            Set<Aggregator<?>> aggregators = getAggregators(EventSource.G1GC, registeredAggregations);
////            if (aggregators != null && !aggregators.isEmpty()) {
////                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.G1GC_PARSER_OUTBOX);
////                aggregators.forEach(aggregatorVerticle::registerAggregator);
////                aggregatorVerticles.add(aggregatorVerticle);
////            }
//        }

//        if (diary.isGenerational()) {
//            Set<Aggregator<?>> aggregators = getAggregators(EventSource.GENERATIONAL, registeredAggregations);
//            if (aggregators != null && !aggregators.isEmpty()) {
//                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.GENERATIONAL_HEAP_PARSER_OUTBOX);
//                aggregators.forEach(aggregatorVerticle::registerAggregator);
//                aggregatorVerticles.add(aggregatorVerticle);
//            }
//        }

//        if (diary.isShenandoah()) {
//            Set<Aggregator<?>> aggregators = getAggregators(EventSource.SHENANDOAH, registeredAggregations);
//            if (aggregators != null && !aggregators.isEmpty()) {
//                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.SHENANDOAH_PARSER_OUTBOX);
//                aggregators.forEach(aggregatorVerticle::registerAggregator);
//                aggregatorVerticles.add(aggregatorVerticle);
//            }
//        }

//        if (diary.isZGC()) {
//            Set<Aggregator<?>> aggregators = getAggregators(EventSource.ZGC, registeredAggregations);
//            if (aggregators != null && !aggregators.isEmpty()) {
//                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.ZGC_PARSER_OUTBOX);
//                aggregators.forEach(aggregatorVerticle::registerAggregator);
//                aggregatorVerticles.add(aggregatorVerticle);
//            }
//        }

//        if (diary.isTenuringDistribution()) {
//            Set<Aggregator<?>> aggregators = getAggregators(EventSource.SURVIVOR, registeredAggregations);
//            if (aggregators != null && !aggregators.isEmpty()) {
//                AggregatorVerticle aggregatorVerticle = new AggregatorVerticle(GCToolkitVertx.SURVIVOR_MEMORY_POOL_PARSER_OUTBOX);
//                aggregators.forEach(aggregatorVerticle::registerAggregator);
//                aggregatorVerticles.add(aggregatorVerticle);
//            }
//        }

//        return aggregatorVerticles;
//    }

    private String initMailBox(Diary diary) {
//        if ( diary.isG1GC()) return GCToolkitVertx.G1GC_PARSER_OUTBOX;
//        if ( diary.isZGC()) return GCToolkitVertx.ZGC_PARSER_OUTBOX;
//        if ( diary.isShenandoah()) return GCToolkitVertx.SHENANDOAH_PARSER_OUTBOX;
//        return GCToolkitVertx.GENERATIONAL_HEAP_PARSER_OUTBOX;
        return ""; //todo: me
    }

}
