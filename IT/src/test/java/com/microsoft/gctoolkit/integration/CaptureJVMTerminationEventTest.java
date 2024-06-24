package com.microsoft.gctoolkit.integration;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.integration.aggregation.HeapOccupancyAfterCollectionSummary;
import com.microsoft.gctoolkit.integration.io.TestLogFile;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

@Tag("modulePath")
public class CaptureJVMTerminationEventTest {

    @Test
    public void testMain() {
        Path path = new TestLogFile("cms/defnew/details/defnew.log").getFile().toPath();
        analyze(path.toString());
    }

    public void analyze(String gcLogFile) {
        /**
         * GC log files can come in  one of two types: single or series of rolling logs.
         * In this sample, we load a single log file.
         * The log files can be either in text, zip, or gzip format.
         */
        GCLogFile logFile = new SingleGCLogFile(Path.of(gcLogFile));
        GCToolKit gcToolKit = new GCToolKit();

        /**
         * This call will load all implementations of Aggregator that have been declared in module-info.java.
         * This mechanism makes use of Module SPI.
         */
        gcToolKit.loadAggregationsFromServiceLoader();

        /**
         * The JavaVirtualMachine contains the aggregations as filled out by the Aggregators.
         * It also contains configuration information about how the JVM was configured for the runtime.
         */
        JavaVirtualMachine machine = null;
        JVMTerminationEventAggregation terminationAggregation = null;
        HeapOccupancyAfterCollectionSummary heapOccupancyAfterCollectionSummary = null;
        try {
            gcToolKit.loadAggregation(new JVMTerminationEventAggregation());
            machine = gcToolKit.analyze(logFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        try {
            terminationAggregation = machine.getAggregation(JVMTerminationEventAggregation.class).get();
            heapOccupancyAfterCollectionSummary = machine.getAggregation(HeapOccupancyAfterCollectionSummary.class).get();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        Assertions.assertEquals( 2.193d, terminationAggregation.getStartTime().getTimeStamp(),0.001d, "Time of first event");
        Assertions.assertEquals( 608797.895d, heapOccupancyAfterCollectionSummary.estimatedRuntime(),0.001d, "Runtime duration");

    }
   
   @Aggregates({EventSource.GENERATIONAL,EventSource.CMS_UNIFIED,EventSource.G1GC,EventSource.GENERATIONAL,EventSource.JVM,EventSource.SHENANDOAH, EventSource.TENURED,EventSource.ZGC})
    public static class JVMEventTerminationAggregator extends Aggregator<JVMTerminationEventAggregation> {

        public JVMEventTerminationAggregator(JVMTerminationEventAggregation aggregation) {
            super(aggregation);
            register(JVMTermination.class, this::process);
        }

        private void process(JVMTermination event) {
            aggregation().recordStartTime(event.getTimeOfFirstEvent());
        }
    }

    @Collates(JVMEventTerminationAggregator.class)
    public static class JVMTerminationEventAggregation extends Aggregation {

        DateTimeStamp startTime;

        public DateTimeStamp getStartTime() {
            return startTime;
        }

        public void recordStartTime(DateTimeStamp time) {
            startTime = time;
        }

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
