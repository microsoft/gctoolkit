// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unittests;

import com.microsoft.gctoolkit.event.GCEvent;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.RotatingGCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import com.microsoft.gctoolkit.parser.GCLogParser;
import com.microsoft.gctoolkit.parser.GenerationalHeapParser;
import com.microsoft.gctoolkit.parser.PreUnifiedG1GCParser;
import com.microsoft.gctoolkit.parser.UnifiedG1GCParser;
import com.microsoft.gctoolkit.parser.UnifiedGenerationalParser;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedDiarizer;
import com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class ParserTest {

    /**
     * A mapping of GC event types to an index. This supports the counting of events. The tests will compare the
     * expected counts against the counts that are captured here.
     */
    private final Map<GarbageCollectionTypes, Integer> collectorNameMapping = Map.ofEntries(
            Map.entry(GarbageCollectionTypes.Young, 0),
            Map.entry(GarbageCollectionTypes.DefNew, 1),
            Map.entry(GarbageCollectionTypes.ParNew, 2),
            Map.entry(GarbageCollectionTypes.ParNewPromotionFailed, 3),
            Map.entry(GarbageCollectionTypes.ConcurrentModeFailure, 5),
            Map.entry(GarbageCollectionTypes.ConcurrentModeInterrupted, 6),
            Map.entry(GarbageCollectionTypes.PSYoungGen, 7),
            Map.entry(GarbageCollectionTypes.FullGC, 8),
            Map.entry(GarbageCollectionTypes.SystemGC, 10),
            Map.entry(GarbageCollectionTypes.InitialMark, 11),
            Map.entry(GarbageCollectionTypes.Remark, 12),
            Map.entry(GarbageCollectionTypes.PSFull, 8),  // bit of a hack to account that the parser is now differentiating between Full and PSFull. (kcp 11/8/15)
            Map.entry(GarbageCollectionTypes.ConcurrentMark, 13),

            Map.entry(GarbageCollectionTypes.Concurrent_Preclean, 14),
            Map.entry(GarbageCollectionTypes.Abortable_Preclean,15),
            Map.entry(GarbageCollectionTypes.Concurrent_Sweep, 16),
            Map.entry(GarbageCollectionTypes.Concurrent_Reset, 17),

            Map.entry(GarbageCollectionTypes.Mixed, 1),
            Map.entry(GarbageCollectionTypes.G1GCYoungInitialMark, 2),
            Map.entry(GarbageCollectionTypes.G1GCMixedInitialMark, 3),
            Map.entry(GarbageCollectionTypes.Full, 4),
            Map.entry(GarbageCollectionTypes.G1GCConcurrentMark, 5),
            Map.entry(GarbageCollectionTypes.G1GCRemark, 7),

            Map.entry(GarbageCollectionTypes.G1GCConcurrentCleanup, 8),
            Map.entry(GarbageCollectionTypes.G1GCCleanup, 9),
            Map.entry(GarbageCollectionTypes.G1ConcurrentMarkResetForOverflow, 11),
            Map.entry(GarbageCollectionTypes.ConcurrentRootRegionScan, 12),
            Map.entry(GarbageCollectionTypes.ConcurrentClearClaimedMarks, 13),
            Map.entry(GarbageCollectionTypes.ConcurrentScanRootRegions, 14),
            Map.entry(GarbageCollectionTypes.Concurrent_Mark, 15),
            Map.entry(GarbageCollectionTypes.ConcurrentCompleteCleanup, 16),
            Map.entry(GarbageCollectionTypes.ConcurrentCreateLiveData, 17),
            Map.entry(GarbageCollectionTypes.ConcurrentCleanupForNextMark, 18),
            Map.entry(GarbageCollectionTypes.G1ConcurrentRebuildRememberedSets, 19)
    );

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

    private GCLogFile loadLogFile(Path path, boolean rotating) throws IOException {
        return rotating ? new RotatingGCLogFile(path) : new SingleGCLogFile(path);
    }
    
    private Diarizer getJVMConfiguration(GCLogFile gcLogFile) {
        try {
            final Diarizer jvmConfiguration = gcLogFile.isUnified()
                    ? new UnifiedDiarizer()
                    : new PreUnifiedDiarizer();

            gcLogFile.stream().
                    filter(Objects::nonNull).
                    map(String::trim).
                    filter(s -> s.length() > 0).
                    map(jvmConfiguration::diarize).
                    filter(completed -> completed).
                    findFirst();
            return jvmConfiguration;
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return null;
    }

    TestResults executeParsing(GCLogFile logfile) {
        return null;
    }
    
    TestResults testGenerationalRotatingLogFile(Path path) throws IOException {
        GCLogFile logfile = loadLogFile(path, true);
        Diarizer jvmConfiguration = getJVMConfiguration(logfile);
        GenerationalHeapParser generationalHeapParser = new GenerationalHeapParser();
        TestResults testResults = new TestResults();
        generationalHeapParser.publishTo(testResults);
        generationalHeapParser.diary(jvmConfiguration.getDiary());
        logfile.stream().map(String::trim).forEach(generationalHeapParser::receive);
        return testResults;
    }

    TestResults testGenerationalSingleLogFile(Path path) throws IOException {
        GCLogFile logfile = loadLogFile(path, false);
        Diarizer jvmConfiguration = getJVMConfiguration(logfile);
        GCLogParser generationalHeapParser = (jvmConfiguration.getDiary().isUnifiedLogging()) ? new UnifiedGenerationalParser() : new GenerationalHeapParser();
        TestResults testResults = new TestResults();
        generationalHeapParser.publishTo(testResults);
        generationalHeapParser.diary(jvmConfiguration.getDiary());
        logfile.stream().map(String::trim).forEach(generationalHeapParser::receive);
        return testResults;
    }

    TestResults testUnifiedG1GCSingleFile(Path path) throws IOException {
        SingleGCLogFile logfile = new SingleGCLogFile(path);
        Diarizer jvmConfiguration = getJVMConfiguration(logfile);
        UnifiedG1GCParser parser = new UnifiedG1GCParser();
        TestResults testResults = new TestResults();
        parser.publishTo(testResults);
        parser.diary(jvmConfiguration.getDiary());
        logfile.stream().map(String::trim).forEach(parser::receive);
        return testResults;
    }

    TestResults testRegionalRotatingLogFile(Path path) throws IOException {
        GCLogFile logfile = loadLogFile(path, true);
        Diarizer jvmConfiguration = getJVMConfiguration(logfile);
        logfile.stream().map(String::trim).forEach(jvmConfiguration::diarize);
        PreUnifiedG1GCParser parser = new PreUnifiedG1GCParser();
        TestResults testResults = new TestResults();
        parser.publishTo(testResults);
        parser.diary(jvmConfiguration.getDiary());
        logfile.stream().map(String::trim).forEach(parser::receive);
        return testResults;
    }

    TestResults testRegionalSingleLogFile(Path path) throws IOException {
        GCLogFile logfile = loadLogFile(path, false);
        Diarizer jvmConfiguration = getJVMConfiguration(logfile);
        PreUnifiedG1GCParser parser = new PreUnifiedG1GCParser();
        TestResults testResults = new TestResults();
        parser.publishTo(testResults);
        parser.diary(jvmConfiguration.getDiary());
        logfile.stream().map(String::trim).forEach(parser::receive);
        return testResults;
    }

    /**
     * Setups an array of counts that is indexed by the type of GC event.
     */
    class TestResults implements JVMEventChannel {

        private final int[] counts = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 , 0, 0, 0, 0, 0};
        private int metaSpaceRecordCount = 0;

        public int getCount(int index) {
            return counts[index];
        }

        public int numberOfDifferentPhases() {
            int count = 0;
            for (int j : counts)
                if (j > 0)
                    count++;
            return count;
        }

        public int getMetaSpaceRecordCount() {
            return metaSpaceRecordCount;
        }

        @Override
        public void registerListener(JVMEventChannelListener listener) {
            throw new IllegalStateException("Listener not used for testing");
        }

        /**
         * Counts by the type of the incoming event.
         * @param event
         */
        @Override
        public void publish(ChannelName channel, JVMEvent event) {
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

        @Override
        public void close() {

        }
    }
}
