package com.microsoft.gctoolkit.integration;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.integration.aggregation.CollectionCycleCountsSummary;
import com.microsoft.gctoolkit.integration.aggregation.HeapOccupancyAfterCollectionSummary;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;

@Tag("modulePath")
public class EndToEndIntegrationTest {

    @Test
    public void testMain() {
        Path path = new TestLogFile("cms/defnew/details/defnew.log").getFile().toPath();
        analyze(path.toString());
        Assertions.assertEquals(26, getInitialMarkCount());
        Assertions.assertEquals(26, getRemarkCount());
        Assertions.assertEquals(19114, getDefNewCount());
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
        try {
            machine = gcToolKit.analyze(logFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        // Retrieves the Aggregation for HeapOccupancyAfterCollectionSummary. This is a time-series aggregation.
        String message = "The XYDataSet for %s contains %s items.\n";
        machine.getAggregation(HeapOccupancyAfterCollectionSummary.class)
                .map(HeapOccupancyAfterCollectionSummary::get)
                .ifPresent(summary -> {
                    summary.forEach((gcType, dataSet) -> {
                        switch (gcType) {
                            case DefNew:
                                defNewCount = dataSet.size();
                                Assertions.assertEquals(19114,defNewCount, "DefNew count");
                                break;
                            case InitialMark:
                                initialMarkCount = dataSet.size();
                                Assertions.assertEquals(26,initialMarkCount,"Initial-Mark count");
                                break;
                            case Remark:
                                remarkCount = dataSet.size();
                                Assertions.assertEquals(26,remarkCount,"Remark count");
                                break;
                            default:
                                System.out.println(gcType + " not managed");
                                break;
                        }
                    });
                });

        Optional<CollectionCycleCountsSummary> summary = machine.getAggregation(CollectionCycleCountsSummary.class);
        // Retrieves the Aggregation for PauseTimeSummary. This is a com.microsoft.gctoolkit.sample.aggregation.RuntimeAggregation.
        machine.getAggregation(PauseTimeSummary.class).ifPresent(pauseTimeSummary -> {
            Assertions.assertEquals( 208.922, pauseTimeSummary.getTotalPauseTime(), 0.001d, "Total Pause Time");
            Assertions.assertEquals( 608800.087, pauseTimeSummary.estimatedRuntime(),0.001d, "Runtime duration");
            Assertions.assertEquals( 34, (int)(pauseTimeSummary.getPercentPaused() * 1000d), "percent paused");
        });

    }

    private int initialMarkCount = 0;
    private int remarkCount = 0;
    private int defNewCount = 0;

    public int getInitialMarkCount() {
        return initialMarkCount;
    }

    public int getRemarkCount() {
        return remarkCount;
    }

    public int getDefNewCount() {
        return defNewCount;
    }

}
