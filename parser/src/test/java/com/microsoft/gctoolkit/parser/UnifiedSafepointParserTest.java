// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.jvm.ApplicationStoppedTime;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JDK 14 consolidated safepoint logging onto a single line (JDK-8221507). These tests cover that
 * form, which is what every collector emits under -Xlog:safepoint on JDK 14 and later.
 */
public class UnifiedSafepointParserTest extends ParserTest {

    @Override
    protected Diarizer diarizer() {
        return new UnifiedDiarizer();
    }

    @Override
    protected GCLogParser parser() {
        return new UnifiedJVMEventParser();
    }

    private ApplicationStoppedTime parseSingleSafepoint(String line) {
        List<JVMEvent> events = feedParser(new String[]{line});
        List<ApplicationStoppedTime> stops = events.stream()
                .filter(ApplicationStoppedTime.class::isInstance)
                .map(ApplicationStoppedTime.class::cast)
                .collect(Collectors.toList());
        assertEquals(1, stops.size(), "expected exactly one ApplicationStoppedTime from: " + line);
        return stops.get(0);
    }

    @Test
    public void testG1Safepoint() {
        ApplicationStoppedTime stop = parseSingleSafepoint(
                "[1.361s][info][safepoint    ] Safepoint \"G1CollectForAllocation\", Time since last: 295590960 ns, Reaching safepoint: 238882 ns, At safepoint: 23888872 ns, Total: 24127754 ns");

        assertDoubleEquals(0.024127754d, stop.getDuration());
        assertDoubleEquals(0.000238882d, stop.getTimeToStopThreads());
        assertTrue(stop.hasTTSP());
        assertEquals(ApplicationStoppedTime.VMOperations.G1CollectForAllocation, stop.getSafePointReason());
        assertTrue(stop.isGCPause());
        // The line is written when the safepoint ends, so the event starts total seconds earlier.
        // DateTimeStamp rounds to milliseconds, so 1.361 - 0.024127754 lands on 1.337.
        assertDoubleEquals(1.337d, stop.getDateTimeStamp().getTimeStamp());

        assertDoubleEquals(0.295590960d, stop.getTimeSinceLastSafepoint());
        assertDoubleEquals(0.023888872d, stop.getAtSafepointTime());
        // JDK 14 - 17 report neither of these
        assertFalse(stop.hasCleanupTime());
        assertFalse(stop.hasLeavingSafepointTime());
        assertFalse(stop.hasThreadCounts());
    }

    @Test
    public void testSerialSafepoint() {
        ApplicationStoppedTime stop = parseSingleSafepoint(
                "[0.773s][info][safepoint    ] Safepoint \"SerialGCCollect\", Time since last: 477036488 ns, Reaching safepoint: 34173 ns, At safepoint: 130196669 ns, Total: 130230842 ns");

        assertDoubleEquals(0.130230842d, stop.getDuration());
        assertDoubleEquals(0.000034173d, stop.getTimeToStopThreads());
        assertEquals(ApplicationStoppedTime.VMOperations.SerialGCCollect, stop.getSafePointReason());
        assertTrue(stop.isGCPause());
    }

    @Test
    public void testZGCSafepoint() {
        ApplicationStoppedTime stop = parseSingleSafepoint(
                "[3.114s][info][safepoint    ] Safepoint \"ZMarkStart\", Time since last: 1050386 ns, Reaching safepoint: 197300 ns, At safepoint: 1248300 ns, Total: 1445600 ns");

        assertDoubleEquals(0.0014456d, stop.getDuration());
        assertDoubleEquals(0.0001973d, stop.getTimeToStopThreads());
        assertEquals(ApplicationStoppedTime.VMOperations.ZMarkStart, stop.getSafePointReason());
        assertTrue(stop.isGCPause());
    }

    /**
     * JDK 21 reports a Cleanup phase between "Reaching safepoint" and "At safepoint".
     */
    @Test
    public void testSafepointWithCleanupPhase() {
        ApplicationStoppedTime stop = parseSingleSafepoint(
                "[1.136s][info][safepoint   ] Safepoint \"XMarkStart\", Time since last: 190106589 ns, Reaching safepoint: 120017 ns, Cleanup: 62407 ns, At safepoint: 121268 ns, Total: 303692 ns");

        assertDoubleEquals(0.000303692d, stop.getDuration());
        assertDoubleEquals(0.000120017d, stop.getTimeToStopThreads());
        assertEquals(ApplicationStoppedTime.VMOperations.XMarkStart, stop.getSafePointReason());
        assertTrue(stop.isGCPause());

        assertDoubleEquals(0.190106589d, stop.getTimeSinceLastSafepoint());
        assertDoubleEquals(0.000062407d, stop.getCleanupTime());
        assertDoubleEquals(0.000121268d, stop.getAtSafepointTime());
        assertFalse(stop.hasLeavingSafepointTime());
    }

    /**
     * Later JDK 21 builds add a "Leaving safepoint" phase as well.
     */
    @Test
    public void testSafepointWithCleanupAndLeavingPhases() {
        ApplicationStoppedTime stop = parseSingleSafepoint(
                "[0.557s][info][safepoint] Safepoint \"ICBufferFull\", Time since last: 328272185 ns, Reaching safepoint: 4929 ns, Cleanup: 156005 ns, At safepoint: 852 ns, Leaving safepoint: 811 ns, Total: 162597 ns");

        assertDoubleEquals(0.000162597d, stop.getDuration());
        assertDoubleEquals(0.000004929d, stop.getTimeToStopThreads());
        assertEquals(ApplicationStoppedTime.VMOperations.ICBufferFull, stop.getSafePointReason());
        assertFalse(stop.isGCPause());

        assertDoubleEquals(0.328272185d, stop.getTimeSinceLastSafepoint());
        assertDoubleEquals(0.000156005d, stop.getCleanupTime());
        assertDoubleEquals(0.000000852d, stop.getAtSafepointTime());
        assertDoubleEquals(0.000000811d, stop.getLeavingSafepointTime());
        assertFalse(stop.hasThreadCounts());
    }

    /**
     * JDK 25 drops Cleanup, keeps Leaving safepoint, and appends a thread count after Total.
     */
    @Test
    public void testGenerationalZGCSafepointWithTrailingThreadCounts() {
        ApplicationStoppedTime stop = parseSingleSafepoint(
                "[2.803s][info][safepoint] Safepoint \"ZMarkStartYoungAndOld\", Time since last: 1367178708 ns, Reaching safepoint: 91483 ns, At safepoint: 33824 ns, Leaving safepoint: 38612 ns, Total: 163919 ns, Threads: 3 runnable, 23 total");

        assertDoubleEquals(0.000163919d, stop.getDuration());
        assertDoubleEquals(0.000091483d, stop.getTimeToStopThreads());
        assertEquals(ApplicationStoppedTime.VMOperations.ZMarkStartYoungAndOld, stop.getSafePointReason());
        assertTrue(stop.isGCPause());

        assertDoubleEquals(1.367178708d, stop.getTimeSinceLastSafepoint());
        assertFalse(stop.hasCleanupTime());
        assertDoubleEquals(0.000033824d, stop.getAtSafepointTime());
        assertDoubleEquals(0.000038612d, stop.getLeavingSafepointTime());
        assertTrue(stop.hasThreadCounts());
        assertEquals(3, stop.getRunnableThreads());
        assertEquals(23, stop.getTotalThreads());
    }

    /**
     * HotSpot writes the phases such that they account for the whole safepoint. Verified against
     * every safepoint line in the logs under gclogs, so it is a cheap check that the capture groups
     * are aligned with the fields they are named for.
     */
    @Test
    public void testPhasesSumToTotal() {
        List<JVMEvent> events = feedParser(new String[]{
                "[0.557s][info][safepoint] Safepoint \"ICBufferFull\", Time since last: 328272185 ns, Reaching safepoint: 4929 ns, Cleanup: 156005 ns, At safepoint: 852 ns, Leaving safepoint: 811 ns, Total: 162597 ns",
                "[1.136s][info][safepoint] Safepoint \"XMarkStart\", Time since last: 190106589 ns, Reaching safepoint: 120017 ns, Cleanup: 62407 ns, At safepoint: 121268 ns, Total: 303692 ns",
                "[1.361s][info][safepoint] Safepoint \"G1CollectForAllocation\", Time since last: 295590960 ns, Reaching safepoint: 238882 ns, At safepoint: 23888872 ns, Total: 24127754 ns",
                "[2.803s][info][safepoint] Safepoint \"ZMarkStartYoungAndOld\", Time since last: 1367178708 ns, Reaching safepoint: 91483 ns, At safepoint: 33824 ns, Leaving safepoint: 38612 ns, Total: 163919 ns, Threads: 3 runnable, 23 total"
        });

        List<ApplicationStoppedTime> stops = events.stream()
                .filter(ApplicationStoppedTime.class::isInstance)
                .map(ApplicationStoppedTime.class::cast)
                .collect(Collectors.toList());
        assertEquals(4, stops.size());

        for (ApplicationStoppedTime stop : stops) {
            double sum = stop.getTimeToStopThreads() + stop.getAtSafepointTime()
                    + (stop.hasCleanupTime() ? stop.getCleanupTime() : 0.0d)
                    + (stop.hasLeavingSafepointTime() ? stop.getLeavingSafepointTime() : 0.0d);
            assertEquals(stop.getDuration(), sum, 1.0e-9d,
                    "phases should account for the total for " + stop.getSafePointReason());
        }
    }

    /**
     * Time since last exceeds Integer.MAX_VALUE nanoseconds, so the values must be read as longs.
     */
    @Test
    public void testLargeNanosecondValues() {
        ApplicationStoppedTime stop = parseSingleSafepoint(
                "[20.947s][info][safepoint    ] Safepoint \"SerialCollectForAllocation\", Time since last: 19109967274 ns, Reaching safepoint: 38663 ns, At safepoint: 264693508 ns, Total: 264732171 ns");

        assertDoubleEquals(0.264732171d, stop.getDuration());
        assertDoubleEquals(0.000038663d, stop.getTimeToStopThreads());
    }

    /**
     * HotSpot adds and removes VM operations every release, so an unknown name must still yield an
     * event carrying the timings. Only the reason is lost.
     */
    @Test
    public void testUnknownVMOperationStillPublishesTimings() {
        ApplicationStoppedTime stop = parseSingleSafepoint(
                "[1.234s][info][safepoint] Safepoint \"SomeFutureVMOperation\", Time since last: 1000000 ns, Reaching safepoint: 5000 ns, At safepoint: 20000 ns, Total: 25000 ns");

        assertDoubleEquals(0.000025d, stop.getDuration());
        assertDoubleEquals(0.000005d, stop.getTimeToStopThreads());
        assertNull(stop.getSafePointReason());
        assertFalse(stop.isGCPause());
    }

    /**
     * Parallel and the JDK 21 G1 remark and cleanup pauses are collections, so they must be
     * attributed to a GC rather than counted as an unrelated stop.
     */
    @Test
    public void testCollectionOperationsAreAttributedToAGC() {
        String[] operations = {"ParallelGCFailedAllocation", "ParallelGCSystemGC",
                "ParallelCollectForAllocation", "ParallelGCCollect", "G1PauseRemark", "G1PauseCleanup",
                "CollectForMetadataAllocation"};

        String[] lines = new String[operations.length];
        for (int i = 0; i < operations.length; i++)
            lines[i] = "[1.234s][info][safepoint] Safepoint \"" + operations[i]
                    + "\", Time since last: 1000000 ns, Reaching safepoint: 5000 ns, At safepoint: 20000 ns, Total: 25000 ns";

        List<ApplicationStoppedTime> stops = feedParser(lines).stream()
                .filter(ApplicationStoppedTime.class::isInstance)
                .map(ApplicationStoppedTime.class::cast)
                .collect(Collectors.toList());
        assertEquals(operations.length, stops.size());

        for (int i = 0; i < operations.length; i++) {
            assertEquals(ApplicationStoppedTime.VMOperations.valueOf(operations[i]), stops.get(i).getSafePointReason());
            assertTrue(stops.get(i).isGCPause(), operations[i] + " should be attributed to a collection");
        }
    }

    /**
     * The JDK 9 - 13 form must keep working.
     */
    @Test
    public void testLegacySafepointRegionStillParses() {
        List<JVMEvent> events = feedParser(new String[]{
                "[0.648s][info][safepoint    ] Entering safepoint region: RevokeBias",
                "[0.648s][info][safepoint    ] Leaving safepoint region",
                "[0.648s][info][safepoint    ] Total time for which application threads were stopped: 0.0006115 seconds, Stopping threads took: 0.0003832 seconds"
        });

        ApplicationStoppedTime stop = events.stream()
                .filter(ApplicationStoppedTime.class::isInstance)
                .map(ApplicationStoppedTime.class::cast)
                .findFirst()
                .orElse(null);

        assertNotNull(stop);
        assertDoubleEquals(0.0006115d, stop.getDuration());
        assertDoubleEquals(0.0003832d, stop.getTimeToStopThreads());
        assertEquals(ApplicationStoppedTime.VMOperations.RevokeBias, stop.getSafePointReason());
        // The JDK 9 - 13 form carries no phase breakdown
        assertFalse(stop.hasAtSafepointTime());
        assertFalse(stop.hasTimeSinceLastSafepoint());
    }

    /**
     * An unrecognised operation must not cost us the timings on the JDK 9 - 13 form either.
     */
    @Test
    public void testLegacyUnknownVMOperationStillPublishesTimings() {
        List<JVMEvent> events = feedParser(new String[]{
                "[0.648s][info][safepoint    ] Entering safepoint region: SomeFutureVMOperation",
                "[0.648s][info][safepoint    ] Leaving safepoint region",
                "[0.648s][info][safepoint    ] Total time for which application threads were stopped: 0.0006115 seconds, Stopping threads took: 0.0003832 seconds"
        });

        ApplicationStoppedTime stop = events.stream()
                .filter(ApplicationStoppedTime.class::isInstance)
                .map(ApplicationStoppedTime.class::cast)
                .findFirst()
                .orElse(null);

        assertNotNull(stop);
        assertDoubleEquals(0.0006115d, stop.getDuration());
        assertDoubleEquals(0.0003832d, stop.getTimeToStopThreads());
        assertNull(stop.getSafePointReason());
    }
}
