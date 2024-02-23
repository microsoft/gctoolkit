package com.microsoft.gctoolkit.integration.query;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.integration.aggregation.PauseTimeSummary;
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
public class MissingTimeStampTest {
    @Test
    public void testMain() {
        Path path = new TestLogFile("unified/g1gc/gc-no-age-timestamp.log").getFile().toPath();
        analyze(path.toString());
    }

    public void analyze(String gcLogFile) {
        /*
         * GC log files can come in  one of two types: single or series of rolling logs.
         * In this sample, we load a single log file.
         * The log files can be either in text, zip, or gzip format.
         */
        GCLogFile logFile = new SingleGCLogFile(Path.of(gcLogFile));
        GCToolKit gcToolKit = new GCToolKit();

        /*
         * This call will load all implementations of Aggregator that have been declared in module-info.java.
         * This mechanism makes use of Module SPI.
         */
        gcToolKit.loadAggregationsFromServiceLoader();

        /*
         * The JavaVirtualMachine contains the aggregations as filled out by the Aggregators.
         * It also contains configuration information about how the JVM was configured for the runtime.
         */
        JavaVirtualMachine machine = null;
        try {
            machine = gcToolKit.analyze(logFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        // Look at getPercentSpentInGc()
        double pausePercent;
        try {
            pausePercent = machine.getAggregation(PauseTimeSummary.class).get().getPercentPaused();
        } catch(Throwable t) {
            fail("getPercentPaused failed", t);
        }

        double pauseTimePercentage = 0.0d;

        try {
            pauseTimePercentage = machine.getAggregation(PauseTimeSummary.class).get().getPercentPaused();
        } catch (Throwable t) {
            fail("pauseTimePercentage failed", t);
        }
        Assertions.assertTrue(pauseTimePercentage > 0.0d);

    }
}
