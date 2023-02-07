package com.microsoft.gctoolkit.integration;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.integration.aggregation.CollectionCycleCountsSummary;
import com.microsoft.gctoolkit.integration.aggregation.HeapOccupancyAfterCollectionSummary;
import com.microsoft.gctoolkit.integration.io.TestLogFile;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

@Tag("modulePath")
public class ZeroAggregationTest {

    @Test
    public void testNoAggregationRegistered() {
        Path path = new TestLogFile("cms/defnew/details/defnew.log").getFile().toPath();
        /*
         * GC log files can come in  one of two types: single or series of rolling logs.
         * In this sample, we load a single log file.
         * The log files can be either in text, zip, or gzip format.
         */
        GCLogFile logFile = new SingleGCLogFile(path);
        GCToolKit gcToolKit = new GCToolKit();
        // Do not call GCToolKit::loadAggregationsFromServiceLoader
        JavaVirtualMachine machine = null;
        try {
            machine = gcToolKit.analyze(logFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        // Retrieves the Aggregation for HeapOccupancyAfterCollectionSummary. This is a time-series aggregation.
        Assertions.assertTrue(machine.getAggregation(HeapOccupancyAfterCollectionSummary.class).isEmpty());
        Assertions.assertTrue(machine.getAggregation(CollectionCycleCountsSummary.class).isEmpty());
    }

    @Tag("modulePath")
    @Test
    public void testSuppliedAggregation() {
        Path path = new TestLogFile("cms/defnew/details/defnew.log").getFile().toPath();
        GCLogFile logFile = new SingleGCLogFile(path);
        GCToolKit gcToolKit = new GCToolKit();
        // Load our local Aggregation that will not be registered for the given log file
        gcToolKit.loadAggregation(new ZeroAggregationTest.TestAggregation());
        JavaVirtualMachine machine = null;
        try {
            machine = gcToolKit.analyze(logFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        // Retrieves the Aggregation for HeapOccupancyAfterCollectionSummary. This is a time-series aggregation.
        Assertions.assertTrue(machine.getAggregation(ZeroAggregationTest.TestAggregation.class).isEmpty());

    }

    @Collates(ZeroAggregationTest.TestAggregator.class)
    public static class TestAggregation extends Aggregation {

        @Override
        public boolean hasWarning() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    @Aggregates(EventSource.G1GC)
    public static class TestAggregator extends Aggregator<TestAggregation> {

        /**
         * Subclass only.
         *
         * @param aggregation The Aggregation that {@literal @}Collates this Aggregator
         * @see Collates
         * @see Aggregation
         */
        protected TestAggregator(TestAggregation aggregation) {
            super(aggregation);
        }
    }
}
