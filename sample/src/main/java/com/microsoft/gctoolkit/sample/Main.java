package com.microsoft.gctoolkit.sample;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.sample.aggregation.HeapOccupancyAfterCollectionAggregation;
import com.microsoft.gctoolkit.sample.aggregation.HeapOccupancyAfterCollectionSummary;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.execute();
    }

    public void execute() {
        GCLogFile logFile = new SingleGCLogFile(Path.of("../gclogs/preunified/cms/defnew/details/defnew.log"));
        GCToolKit gcToolKit = new GCToolKit();
        JavaVirtualMachine machine = gcToolKit.analyze(logFile);
        HeapOccupancyAfterCollectionAggregation results = machine.getAggregation(HeapOccupancyAfterCollectionSummary.class);
        System.out.println(results.toString());
    }
}
