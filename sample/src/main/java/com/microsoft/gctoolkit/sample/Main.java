package com.microsoft.gctoolkit.sample;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.sample.aggregation.HeapOccupancyAfterCollectionSummary;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("This sample requires a path to a GC log file as an argument.");
        }
        var gcLogFilePath = args[0];

        var logFile = new SingleGCLogFile(Path.of(gcLogFilePath));
        
        var gcToolKit = new GCToolKit();
        gcToolKit.loadAggregationsFromServiceLoader();

        var machine = gcToolKit.analyze(logFile);
        var results = machine.getAggregation(HeapOccupancyAfterCollectionSummary.class);

        if (results.isPresent()) {
            var aggregationResult = results.get();
            aggregationResult.get().forEach((gcType, dataSet) -> System.out
                    .println("The XYDataSet for " + gcType + " contains " + dataSet.size() + " items."));
        } else {
            System.out.println("No aggregation found.");
        }
    }
}
