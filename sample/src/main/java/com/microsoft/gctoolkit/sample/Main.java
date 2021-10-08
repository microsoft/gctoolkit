package com.microsoft.gctoolkit.sample;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.sample.aggregation.HeapOccupancyAfterCollectionSummary;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
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
        JavaVirtualMachine machine = gcToolKit.analyze(logFile);

        // Retrieves the Aggregation for HeapOccupancyAfterCollectionSummary. This is a time-series aggregation.
        Optional<HeapOccupancyAfterCollectionSummary> results = machine.getAggregation(HeapOccupancyAfterCollectionSummary.class);

        if (results.isPresent()) {
            // Simple treatment of results
            HeapOccupancyAfterCollectionSummary aggregationResult = results.get();
            String message = "The XYDataSet for %s contains %s items.\n";
            aggregationResult.get().forEach((gcType, dataSet) -> {
                System.out.printf(message, gcType, dataSet.size());

                String gcTypeName = gcType.toString();
                if (gcTypeName.equals("InitialMark")) {
                    initialMarkCount = dataSet.size();
                } else if (gcTypeName.equals("Remark")) {
                    remarkCount = dataSet.size();
                } else if (gcTypeName.equals("DefNew")) {
                    defNewCount = dataSet.size();
                }
            });
        } else {
            System.out.println("No aggregation found.");
        }
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
