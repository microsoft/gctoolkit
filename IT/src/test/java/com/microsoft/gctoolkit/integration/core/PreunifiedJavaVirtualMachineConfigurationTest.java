package com.microsoft.gctoolkit.integration.core;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.integration.io.TestLogFile;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class PreunifiedJavaVirtualMachineConfigurationTest {

    private String logFile = "preunified/g1gc/details/tenuring/180/g1gc.log";
    private int[] times = { 0, 1028, 945481, 944453};

    @Tag("modulePath")
    @Test
    public void testSingle() {
        TestLogFile log = new TestLogFile(logFile);
        smokeTest(new SingleGCLogFile(log.getFile().toPath()), times);
    }

    private void smokeTest(GCLogFile log, int[] endStartTimes ) {
        GCToolKit gcToolKit = new GCToolKit();
        gcToolKit.loadAggregationsFromServiceLoader();
        TestTimeAggregation aggregation = new TestTimeAggregation();
        gcToolKit.loadAggregation(aggregation);
        JavaVirtualMachine machine = null;
        try {
            machine = gcToolKit.analyze(log);
            aggregation = machine.getAggregation(PreunifiedJavaVirtualMachineConfigurationTest.TestTimeAggregation.class).get();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            machine.getEstimatedJVMStartTime();
            machine.getTimeOfFirstEvent().getTimeStamp();
            aggregation.timeOfTerminationEvent().getTimeStamp();
            aggregation.estimatedRuntime();
        } catch(Throwable t) {
            fail("Failed to extract runtime timing information",t);
        }

        Assertions.assertEquals( endStartTimes[0], (int)(machine.getEstimatedJVMStartTime().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[1], (int)(machine.getTimeOfFirstEvent().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[2], (int)(aggregation.timeOfTerminationEvent().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[3], (int)(aggregation.estimatedRuntime() * 1000.0d));
    }

    @Aggregates({EventSource.G1GC,EventSource.GENERATIONAL,EventSource.ZGC,EventSource.SHENANDOAH})
    public static class TestTimeAggregator extends Aggregator<TestTimeAggregation> {

        /**
         * Subclass only.
         *
         * @param aggregation The Aggregation that {@literal @}Collates this Aggregator
         * @see Collates
         * @see Aggregation
         */
        public TestTimeAggregator(TestTimeAggregation aggregation) {
            super(aggregation);
        }
    }

    @Collates(TestTimeAggregator.class)
    public static class TestTimeAggregation extends Aggregation {

        public TestTimeAggregation() {}

        @Override
        public boolean hasWarning() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

    }
}
