package com.microsoft.gctoolkit.integration;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.integration.io.TestLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

@Tag("modulePath")
public class MissingAnnotationTest {

    @SuppressWarnings("unchecked")
    private void workFlow(Aggregation aggregation, Class clazz) {
        GCToolKit gcToolKit = new GCToolKit();
        // Load our test aggregation instead of calling GCToolKit::loadAggregationsFromServiceLoader
        gcToolKit.loadAggregation(aggregation);
        JavaVirtualMachine machine = null;
        try {
            machine = gcToolKit.analyze(new SingleGCLogFile(new TestLogFile("cms/defnew/details/defnew.log").getFile().toPath()));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        Assertions.assertTrue(machine.getAggregation(clazz).isEmpty());
    }

    @Tag("modulePath")
    @Test
    void testNoAggregationRegistered() {
        workFlow(new MissingAnnotationAggregation(), MissingAnnotationTest.MissingAnnotationAggregation.class);
    }

    @Tag("modulePath")
    @Test
    void testSuppliedAggregation() {
        workFlow(new MissingEventSource(), MissingAnnotationTest.MissingEventSource.class);
    }

    /************* Aggregator/Aggregation with missing Collates annotation */
    public static class MissingAnnotationAggregation extends Aggregation {

        @Override
        public boolean hasWarning() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    @SuppressWarnings("unused")
    @Aggregates(EventSource.G1GC)
    public static class TestAggregator extends Aggregator<MissingAnnotationAggregation> {
        protected TestAggregator(MissingAnnotationAggregation aggregation) {
            super(aggregation);
        }
    }

    /************* Aggregator/Aggregation with missing Aggregates annotation */
    @Collates(MissingAnnotationAggregator.class)
    public static class MissingEventSource extends Aggregation {
        @Override
        public boolean hasWarning() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    public static class MissingAnnotationAggregator extends Aggregator<MissingEventSource> {
        protected MissingAnnotationAggregator(MissingEventSource aggregation) {
            super(aggregation);
        }
    }
}
