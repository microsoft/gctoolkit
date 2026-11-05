package com.microsoft.gctoolkit.integration;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.jvm.Safepoint;
import com.microsoft.gctoolkit.integration.io.TestLogFile;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * End to end coverage for the single safepoint line aggregation.
 */
@Tag("modulePath")
public class UnifiedSafepointAggregationTest {

    private SafepointSummary analyze(String logName) {
        GCLogFile logFile = new SingleGCLogFile(Path.of(new TestLogFile(logName).getFile().getPath()));
        GCToolKit gcToolKit = new GCToolKit();
        gcToolKit.loadAggregation(new SafepointSummary());
        JavaVirtualMachine machine = null;
        try {
            machine = gcToolKit.analyze(logFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return machine.getAggregation(SafepointSummary.class).orElseGet(() -> {
            fail("SAFEPOINT aggregation was not run for " + logName);
            return null;
        });
    }

    @Test
    public void testZGCSafepoints() {
        SafepointSummary summary = analyze("zgc/zgc.log");
        assertEquals(3478, summary.count(), "safepoint lines in zgc.log");
        assertTrue(summary.totalTimeToSafepoint() > 0.0d, "ZGC log should report time to safepoint");
        assertTrue(summary.reasons().contains("ZMarkStart"), "expected ZGC VM operations to be reported");
    }

    @Test
    public void testG1Safepoints() {
        SafepointSummary summary = analyze("g1gc/G1-80-16gbps2.log.0");
        assertEquals(2158, summary.count(), "safepoint lines in G1-80-16gbps2.log.0");
        assertTrue(summary.reasons().contains("G1CollectForAllocation"));
    }

    @Test
    public void testSerialSafepoints() {
        SafepointSummary summary = analyze("serial/factorization-serialgc-tip.log");
        assertEquals(17, summary.count(), "safepoint lines in factorization-serialgc-tip.log");
        assertTrue(summary.reasons().contains("SerialGCCollect"));
    }

    @Aggregates(EventSource.SAFEPOINT)
    public static class SafepointAggregator extends Aggregator<SafepointSummary> {

        public SafepointAggregator(SafepointSummary aggregation) {
            super(aggregation);
            register(Safepoint.class, this::process);
        }

        private void process(Safepoint event) {
            aggregation().record(event);
        }
    }

    @Collates(SafepointAggregator.class)
    public static class SafepointSummary extends Aggregation {

        private final List<String> reasons = new ArrayList<>();
        private double totalTimeToSafepoint = 0.0d;

        public void record(Safepoint event) {
            reasons.add(event.getVmOperation());
            if (event.hasReachingSafepointDuration())
                totalTimeToSafepoint += event.getReachingSafepointDuration();
        }

        public int count() {
            return reasons.size();
        }

        public List<String> reasons() {
            return reasons;
        }

        public double totalTimeToSafepoint() {
            return totalTimeToSafepoint;
        }

        @Override
        public boolean hasWarning() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return reasons.isEmpty();
        }
    }
}
