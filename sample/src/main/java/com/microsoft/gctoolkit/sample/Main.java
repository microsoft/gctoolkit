package com.microsoft.gctoolkit.sample;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.sample.aggregation.CollectionCycleCountsSummary;
import com.microsoft.gctoolkit.sample.aggregation.HeapOccupancyAfterCollectionSummary;
import com.microsoft.gctoolkit.sample.aggregation.PauseTimeSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Main {

    public static void main(String[] args) throws IOException {
        String userInput = args.length > 0 ? args[0] : "";
        String gcLogFile = System.getProperty("gcLogFile", userInput);

        if (gcLogFile.isBlank()) {
            throw new IllegalArgumentException("This sample requires a path to a GC log file.");
        }

        if (Files.notExists(Path.of(gcLogFile))) {
            throw new IllegalArgumentException(String.format("File %s not found.", gcLogFile));
        }
       
        Main main = new Main();
        main.analyze(gcLogFile);
    }

    public void analyze(String gcLogFile) throws IOException {
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
        JavaVirtualMachine machine = gcToolKit.analyze(logFile);

        // Retrieves the Aggregation for HeapOccupancyAfterCollectionSummary. This is a time-series aggregation.
        String message = "The XYDataSet for %s contains %s items.\n";
        machine.getAggregation(HeapOccupancyAfterCollectionSummary.class)
                .map(HeapOccupancyAfterCollectionSummary::get)
                .ifPresent(summary -> {
                    summary.forEach((gcType, dataSet) -> {
                        System.out.printf(message, gcType, dataSet.size());
                        switch (gcType) {
                            case DefNew:
                                defNewCount = dataSet.size();
                                break;
                            case InitialMark:
                                initialMarkCount = dataSet.size();
                                break;
                            case Remark:
                                remarkCount = dataSet.size();
                                break;
                            default:
                                System.out.println(gcType + " not managed");
                                break;
                        }
                    });
                });

        Optional<CollectionCycleCountsSummary> summary = machine.getAggregation(CollectionCycleCountsSummary.class);
        summary.ifPresent(s -> s.printOn(System.out));
        // Retrieves the Aggregation for PauseTimeSummary. This is a com.microsoft.gctoolkit.sample.aggregation.RuntimeAggregation.
        machine.getAggregation(PauseTimeSummary.class).ifPresent(pauseTimeSummary -> {
            System.out.printf("Total pause time  : %.4f\n", pauseTimeSummary.getTotalPauseTime());
            System.out.printf("Total run time    : %.4f\n", pauseTimeSummary.getTotalPauseTime());
            System.out.printf("Percent pause time: %.2f\n", pauseTimeSummary.getPercentPaused());
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
