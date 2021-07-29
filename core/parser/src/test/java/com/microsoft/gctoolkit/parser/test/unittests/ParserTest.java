// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.test.unittests;

import com.microsoft.gctoolkit.event.GCEvent;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.RotatingGCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.parser.GenerationalHeapParser;
import com.microsoft.gctoolkit.parser.JVMEventConsumer;
import com.microsoft.gctoolkit.parser.PreUnifiedG1GCParser;
import com.microsoft.gctoolkit.parser.UnifiedG1GCParser;
import com.microsoft.gctoolkit.parser.jvm.JVMConfiguration;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedJVMConfiguration;
import com.microsoft.gctoolkit.parser.jvm.UnifiedJVMConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class ParserTest {

    private static final Logger LOGGER = Logger.getLogger(ParserTest.class.getName());

    private HashMap<GarbageCollectionTypes, Integer> collectorNameMapping;

    {
        collectorNameMapping = new HashMap<>();
        collectorNameMapping.put(GarbageCollectionTypes.Young, 0);
        collectorNameMapping.put(GarbageCollectionTypes.DefNew, 1);
        collectorNameMapping.put(GarbageCollectionTypes.ParNew, 2);
        collectorNameMapping.put(GarbageCollectionTypes.ParNewPromotionFailed, 3);
        collectorNameMapping.put(GarbageCollectionTypes.ConcurrentModeFailure, 4);   // delete this line  todo:
        collectorNameMapping.put(GarbageCollectionTypes.ConcurrentModeFailure, 5);
        collectorNameMapping.put(GarbageCollectionTypes.ConcurrentModeInterrupted, 6);
        collectorNameMapping.put(GarbageCollectionTypes.PSYoungGen, 7);
        collectorNameMapping.put(GarbageCollectionTypes.FullGC, 8);
        collectorNameMapping.put(GarbageCollectionTypes.SystemGC, 10);
        collectorNameMapping.put(GarbageCollectionTypes.InitialMark, 11);
        collectorNameMapping.put(GarbageCollectionTypes.Remark, 12);
        collectorNameMapping.put(GarbageCollectionTypes.PSFull, 8);  // bit of a hack to account that the parser is now differentiating between Full and PSFull. (kcp 11/8/15)
        collectorNameMapping.put(GarbageCollectionTypes.Young, 0);                  // delete this line todo:
        collectorNameMapping.put(GarbageCollectionTypes.Mixed, 1);
        collectorNameMapping.put(GarbageCollectionTypes.G1GCYoungInitialMark, 2);
        collectorNameMapping.put(GarbageCollectionTypes.G1GCMixedInitialMark, 3);
        collectorNameMapping.put(GarbageCollectionTypes.Full, 4);
        collectorNameMapping.put(GarbageCollectionTypes.ConcurrentMark, 5);
        collectorNameMapping.put(GarbageCollectionTypes.G1GCConcurrentMark, 5);
        collectorNameMapping.put(GarbageCollectionTypes.G1GCRemark, 7);

        collectorNameMapping.put(GarbageCollectionTypes.G1GCConcurrentCleanup, 8);
        collectorNameMapping.put(GarbageCollectionTypes.G1GCCleanup, 9);
        collectorNameMapping.put(GarbageCollectionTypes.G1ConcurrentMarkResetForOverflow, 11);
        collectorNameMapping.put(GarbageCollectionTypes.ConcurrentRootRegionScan, 12);
    }

    private List<GarbageCollectionTypes> findGarbageCollector(final int index) {
        return collectorNameMapping
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == index)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void analyzeResults(String gcLogName, TestResults testResults, int numberOfDifferentPhases, int[] invocationCounts) {
        assertEquals(numberOfDifferentPhases, testResults.numberOfDifferentPhases());
        for (int i = 0; i < invocationCounts.length; i++) {
            assertEquals(invocationCounts[i], testResults.getCount(i), "Phase Count Differs @ " + i + " for " + findGarbageCollector(i) + " in " + gcLogName);
        }

    }

    private GCLogFile loadLogFile(Path path, boolean rotating) {
        return rotating ? new RotatingGCLogFile(path) : new SingleGCLogFile(path);
    }
    
    private JVMConfiguration getJVMConfiguration(GCLogFile gcLogFile) {
        try {
            final JVMConfiguration jvmConfiguration = gcLogFile.isUnifiedFormat()
                    ? new UnifiedJVMConfiguration()
                    : new PreUnifiedJVMConfiguration();

            gcLogFile.stream().
                    filter(Objects::nonNull).
                    map(String::trim).
                    filter(s -> s.length() > 0).
                    map(jvmConfiguration::diarize).
                    filter(completed -> completed).
                    findFirst();

            jvmConfiguration.fillInKnowns();
            return jvmConfiguration;
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return null;
    }
    
    TestResults testGenerationalRotatingLogFile(Path path) throws IOException {
        TestResults testResults = new TestResults();
        GCLogFile logfile = loadLogFile(path, true);
        JVMConfiguration jvmConfiguration = getJVMConfiguration(logfile);
        GenerationalHeapParser generationalHeapParser = new GenerationalHeapParser(jvmConfiguration.getDiary(), testResults);
        logfile.stream().map(String::trim).forEach(generationalHeapParser::receive);
        return testResults;
    }

    TestResults testGenerationalSingleLogFile(Path path) throws IOException {
        TestResults testResults = new TestResults();
        GCLogFile logfile = loadLogFile(path, false);
        JVMConfiguration jvmConfiguration = getJVMConfiguration(logfile);
        GenerationalHeapParser generationalHeapParser = new GenerationalHeapParser(jvmConfiguration.getDiary(), testResults);
        logfile.stream().map(String::trim).forEach(generationalHeapParser::receive);
        return testResults;
    }

    TestResults testUnifiedSingleFile(Path path) throws IOException {
        TestResults testResults = new TestResults();
        SingleGCLogFile logfile = new SingleGCLogFile(path);
        UnifiedJVMConfiguration unifiedJVMConfiguration = new UnifiedJVMConfiguration();
        UnifiedG1GCParser parser = new UnifiedG1GCParser(unifiedJVMConfiguration.getDiary(), testResults);
        logfile.stream().map(String::trim).forEach(parser::receive);
        return testResults;
    }

    TestResults testRegionalRotatingLogFile(Path path) throws IOException {
        TestResults testResults = new TestResults();
        GCLogFile logfile = loadLogFile(path, true);
        JVMConfiguration jvmConfiguration = getJVMConfiguration(logfile);
        PreUnifiedG1GCParser parser = new PreUnifiedG1GCParser(jvmConfiguration.getDiary(), testResults);
        logfile.stream().map(String::trim).forEach(parser::receive);
        return testResults;
    }

    TestResults testRegionalSingleLogFile(Path path) throws IOException {
        TestResults testResults = new TestResults();
        GCLogFile logfile = loadLogFile(path, false);
        JVMConfiguration jvmConfiguration = getJVMConfiguration(logfile);
        PreUnifiedG1GCParser parser = new PreUnifiedG1GCParser(jvmConfiguration.getDiary(), testResults);
        logfile.stream().map(String::trim).forEach(parser::receive);
        return testResults;
    }

    class TestResults implements JVMEventConsumer {

        private int[] counts = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        private int metaSpaceRecordCount = 0;

        public int getCount(int index) {
            return counts[index];
        }

        public int numberOfDifferentPhases() {
            int count = 0;
            for (int i = 0; i < counts.length; i++)
                if (counts[i] > 0)
                    count++;
            return count;
        }

        public int getMetaSpaceRecordCount() {
            return metaSpaceRecordCount;
        }

        @Override
        public void record(JVMEvent event) {
            if (!(event instanceof JVMTermination)) {
                GCEvent gcEvent = (GCEvent) event;
                int index = collectorNameMapping.get(gcEvent.getGarbageCollectionType());
                counts[index] = counts[index] + 1;
                if (event instanceof G1GCPauseEvent) {
                    if (((G1GCPauseEvent) event).getPermOrMetaspace() != null)
                        metaSpaceRecordCount++;
                }
            }
        }
    }

//    class TestResults extends AbstractVerticle {

    //private CountDownLatch latch = new CountDownLatch(1);
    //private CountDownLatch deployedLatch = new CountDownLatch(1);
//        private int[] counts = { 0 , 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//        private int metaSpaceRecordCount = 0;
//
//        public int getCount( int index) {
//            return counts[index];
//        }
//
//        public int numberOfDifferentPhases() {
//            int count = 0;
//            for ( int i = 0; i < counts.length; i++)
//                if ( counts[i] > 0)
//                    count++;
//            return count;
//        }
//
//        public int getMetaSpaceRecordCount() {
//            return metaSpaceRecordCount;
//        }
//
//        public boolean record(JVMEvent event) {
//            if (event instanceof JVMTermination) {
//                latch.countDown();
//            } else {
//                GCEvent gcEvent = (GCEvent) event;
//                int index = collectorNameMapping.get(gcEvent.getGarbageCollectionType());
//                counts[index] = counts[index] + 1;
//                if ( event instanceof G1GCPauseEvent) {
//                    if ( ((G1GCPauseEvent)event).getPermOrMetaspace() != null)
//                        metaSpaceRecordCount++;
//                }
//            }
//        }

//        public void start() {
//            try {
//                vertx.eventBus().consumer("ParserTest", (Handler<Message<JVMEvent>>) message -> {
//                    record(message.body());
//                });
//                deployedLatch.countDown();
//            } catch (Throwable t) {
//                fail( "Processing events failed: " + t.getMessage());
//            }
//        }

//        public void awaitDeployment() {
//            try {
//                deployedLatch.await();
//            } catch (InterruptedException ie) {
//                fail("block on deployment latch interrupted");
//            }
//        }


//        public void awaitJVMTermination() {
//            try {
//                latch.await();
//            } catch (InterruptedException e) {
//                fail("block on latch interrupted: " + e.getMessage());
//            }
//        }
//    }
}
