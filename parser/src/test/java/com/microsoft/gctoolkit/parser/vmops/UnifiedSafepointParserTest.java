// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.vmops;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.Safepoint;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.GCLogParser;
import com.microsoft.gctoolkit.parser.ParserTest;
import com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        return new UnifiedSafepointParser();
    }

    private List<Safepoint> safepoints(String... lines) {
        List<JVMEvent> events = feedParser(lines);
        return events.stream()
                .filter(Safepoint.class::isInstance)
                .map(Safepoint.class::cast)
                .collect(Collectors.toList());
    }

    private Safepoint parseSingleSafepoint(String line) {
        List<Safepoint> safepoints = safepoints(line);
        assertEquals(1, safepoints.size(), "expected exactly one Safepoint from: " + line);
        return safepoints.get(0);
    }

    @Test
    public void testG1Safepoint() {
        Safepoint safepoint = parseSingleSafepoint(
                "[1.361s][info][safepoint    ] Safepoint \"G1CollectForAllocation\", Time since last: 295590960 ns, Reaching safepoint: 238882 ns, At safepoint: 23888872 ns, Total: 24127754 ns");

        assertEquals("G1CollectForAllocation", safepoint.getVmOperation());
        assertDoubleEquals(0.024127754d, safepoint.getDuration());
        assertDoubleEquals(0.000238882d, safepoint.getReachingSafepointDuration());
        // The line is written when the safepoint ends, so the event starts total seconds earlier.
        // DateTimeStamp rounds to milliseconds, so 1.361 - 0.024127754 lands on 1.337.
        assertDoubleEquals(1.337d, safepoint.getDateTimeStamp().getTimeStamp());

        assertDoubleEquals(0.295590960d, safepoint.getTimeSinceLastSafepoint());
        assertDoubleEquals(0.023888872d, safepoint.getAtSafepointDuration());
        // JDK 14 - 17 report neither of these
        assertFalse(safepoint.hasCleanupPhaseDuration());
        assertFalse(safepoint.hasLeavingSafepointDuration());
        assertFalse(safepoint.hasTotalThreads());
    }

    @Test
    public void testSerialSafepoint() {
        Safepoint safepoint = parseSingleSafepoint(
                "[0.773s][info][safepoint    ] Safepoint \"SerialGCCollect\", Time since last: 477036488 ns, Reaching safepoint: 34173 ns, At safepoint: 130196669 ns, Total: 130230842 ns");

        assertDoubleEquals(0.130230842d, safepoint.getDuration());
        assertDoubleEquals(0.000034173d, safepoint.getReachingSafepointDuration());
        assertEquals("SerialGCCollect", safepoint.getVmOperation());
    }

    @Test
    public void testZGCSafepoint() {
        Safepoint safepoint = parseSingleSafepoint(
                "[3.114s][info][safepoint    ] Safepoint \"ZMarkStart\", Time since last: 1050386 ns, Reaching safepoint: 197300 ns, At safepoint: 1248300 ns, Total: 1445600 ns");

        assertDoubleEquals(0.0014456d, safepoint.getDuration());
        assertDoubleEquals(0.0001973d, safepoint.getReachingSafepointDuration());
        assertEquals("ZMarkStart", safepoint.getVmOperation());
    }

    /**
     * JDK 21 reports a Cleanup phase between "Reaching safepoint" and "At safepoint".
     */
    @Test
    public void testSafepointWithCleanupPhase() {
        Safepoint safepoint = parseSingleSafepoint(
                "[1.136s][info][safepoint   ] Safepoint \"XMarkStart\", Time since last: 190106589 ns, Reaching safepoint: 120017 ns, Cleanup: 62407 ns, At safepoint: 121268 ns, Total: 303692 ns");

        assertDoubleEquals(0.000303692d, safepoint.getDuration());
        assertDoubleEquals(0.000120017d, safepoint.getReachingSafepointDuration());
        assertEquals("XMarkStart", safepoint.getVmOperation());

        assertDoubleEquals(0.190106589d, safepoint.getTimeSinceLastSafepoint());
        assertDoubleEquals(0.000062407d, safepoint.getCleanupPhaseDuration());
        assertDoubleEquals(0.000121268d, safepoint.getAtSafepointDuration());
        assertFalse(safepoint.hasLeavingSafepointDuration());
    }

    /**
     * Later JDK 21 builds add a "Leaving safepoint" phase as well.
     */
    @Test
    public void testSafepointWithCleanupAndLeavingPhases() {
        Safepoint safepoint = parseSingleSafepoint(
                "[0.557s][info][safepoint] Safepoint \"ICBufferFull\", Time since last: 328272185 ns, Reaching safepoint: 4929 ns, Cleanup: 156005 ns, At safepoint: 852 ns, Leaving safepoint: 811 ns, Total: 162597 ns");

        assertDoubleEquals(0.000162597d, safepoint.getDuration());
        assertDoubleEquals(0.000004929d, safepoint.getReachingSafepointDuration());
        assertEquals("ICBufferFull", safepoint.getVmOperation());

        assertDoubleEquals(0.328272185d, safepoint.getTimeSinceLastSafepoint());
        assertDoubleEquals(0.000156005d, safepoint.getCleanupPhaseDuration());
        assertDoubleEquals(0.000000852d, safepoint.getAtSafepointDuration());
        assertDoubleEquals(0.000000811d, safepoint.getLeavingSafepointDuration());
        assertFalse(safepoint.hasTotalThreads());
    }

    /**
     * JDK 25 drops Cleanup, keeps Leaving safepoint, and appends thread counts after Total.
     */
    @Test
    public void testGenerationalZGCSafepointWithTrailingThreadCounts() {
        Safepoint safepoint = parseSingleSafepoint(
                "[2.803s][info][safepoint] Safepoint \"ZMarkStartYoungAndOld\", Time since last: 1367178708 ns, Reaching safepoint: 91483 ns, At safepoint: 33824 ns, Leaving safepoint: 38612 ns, Total: 163919 ns, Threads: 3 runnable, 23 total");

        assertDoubleEquals(0.000163919d, safepoint.getDuration());
        assertDoubleEquals(0.000091483d, safepoint.getReachingSafepointDuration());
        assertEquals("ZMarkStartYoungAndOld", safepoint.getVmOperation());

        assertDoubleEquals(1.367178708d, safepoint.getTimeSinceLastSafepoint());
        assertFalse(safepoint.hasCleanupPhaseDuration());
        assertDoubleEquals(0.000033824d, safepoint.getAtSafepointDuration());
        assertDoubleEquals(0.000038612d, safepoint.getLeavingSafepointDuration());
        assertTrue(safepoint.hasTotalThreads());
        assertEquals(3, safepoint.getRunnableThreads());
        assertEquals(23, safepoint.getTotalThreads());
    }

    /**
     * HotSpot writes the phases such that they account for the whole safepoint. Verified against
     * every safepoint line in the logs under gclogs, so it is a cheap check that the capture groups
     * are aligned with the fields they are named for.
     */
    @Test
    public void testPhasesSumToTotal() {
        List<Safepoint> safepoints = safepoints(
                "[0.557s][info][safepoint] Safepoint \"ICBufferFull\", Time since last: 328272185 ns, Reaching safepoint: 4929 ns, Cleanup: 156005 ns, At safepoint: 852 ns, Leaving safepoint: 811 ns, Total: 162597 ns",
                "[1.136s][info][safepoint] Safepoint \"XMarkStart\", Time since last: 190106589 ns, Reaching safepoint: 120017 ns, Cleanup: 62407 ns, At safepoint: 121268 ns, Total: 303692 ns",
                "[1.361s][info][safepoint] Safepoint \"G1CollectForAllocation\", Time since last: 295590960 ns, Reaching safepoint: 238882 ns, At safepoint: 23888872 ns, Total: 24127754 ns",
                "[2.803s][info][safepoint] Safepoint \"ZMarkStartYoungAndOld\", Time since last: 1367178708 ns, Reaching safepoint: 91483 ns, At safepoint: 33824 ns, Leaving safepoint: 38612 ns, Total: 163919 ns, Threads: 3 runnable, 23 total");

        assertEquals(4, safepoints.size());

        for (Safepoint safepoint : safepoints) {
            double sum = safepoint.getReachingSafepointDuration() + safepoint.getAtSafepointDuration()
                    + (safepoint.hasCleanupPhaseDuration() ? safepoint.getCleanupPhaseDuration() : 0.0d)
                    + (safepoint.hasLeavingSafepointDuration() ? safepoint.getLeavingSafepointDuration() : 0.0d);
            assertEquals(safepoint.getDuration(), sum, 1.0e-9d,
                    "phases should account for the total for " + safepoint.getVmOperation());
        }
    }

    /**
     * Time since last exceeds Integer.MAX_VALUE nanoseconds, so the values must be read as longs.
     */
    @Test
    public void testLargeNanosecondValues() {
        Safepoint safepoint = parseSingleSafepoint(
                "[20.947s][info][safepoint    ] Safepoint \"SerialCollectForAllocation\", Time since last: 19109967274 ns, Reaching safepoint: 38663 ns, At safepoint: 264693508 ns, Total: 264732171 ns");

        assertDoubleEquals(19.109967274d, safepoint.getTimeSinceLastSafepoint());
        assertDoubleEquals(0.264732171d, safepoint.getDuration());
        assertDoubleEquals(0.000038663d, safepoint.getReachingSafepointDuration());
    }

    /**
     * HotSpot adds and removes VM operations every release, so an unknown name must still yield an
     * event carrying the timings. Only the reason is lost.
     */
    @Test
    public void testUnknownVMOperationStillPublishesTimings() {
        Safepoint safepoint = parseSingleSafepoint(
                "[1.234s][info][safepoint] Safepoint \"SomeFutureVMOperation\", Time since last: 1000000 ns, Reaching safepoint: 5000 ns, At safepoint: 20000 ns, Total: 25000 ns");

        assertEquals("SomeFutureVMOperation", safepoint.getVmOperation());
        assertDoubleEquals(0.000025d, safepoint.getDuration());
        assertDoubleEquals(0.000005d, safepoint.getReachingSafepointDuration());
    }

    /**
     * Operation names vary across releases and collectors; each must be reported verbatim.
     */
    @Test
    public void testVMOperationNamesAreReportedVerbatim() {
        String[] operations = {"ParallelGCFailedAllocation", "ParallelGCSystemGC",
                "ParallelCollectForAllocation", "ParallelGCCollect", "G1PauseRemark", "G1PauseCleanup",
                "CollectForMetadataAllocation"};

        String[] lines = new String[operations.length];
        for (int i = 0; i < operations.length; i++)
            lines[i] = "[1.234s][info][safepoint] Safepoint \"" + operations[i]
                    + "\", Time since last: 1000000 ns, Reaching safepoint: 5000 ns, At safepoint: 20000 ns, Total: 25000 ns";

        List<Safepoint> safepoints = safepoints(lines);
        assertEquals(operations.length, safepoints.size());

        for (int i = 0; i < operations.length; i++) {
            assertEquals(operations[i], safepoints.get(i).getVmOperation());
        }
    }

    /**
     * The JDK 9 - 13 form is handled by UnifiedJVMEventParser, so this parser must leave it alone
     * rather than half matching it.
     */
    @Test
    public void testLegacySafepointFormIsIgnored() {
        assertTrue(safepoints(
                "[0.648s][info][safepoint    ] Entering safepoint region: RevokeBias",
                "[0.648s][info][safepoint    ] Leaving safepoint region",
                "[0.648s][info][safepoint    ] Total time for which application threads were stopped: 0.0006115 seconds, Stopping threads took: 0.0003832 seconds"
        ).isEmpty());
    }
}
