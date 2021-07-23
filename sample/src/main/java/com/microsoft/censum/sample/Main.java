package com.microsoft.censum.sample;

import com.microsoft.censum.Censum;
import com.microsoft.censum.io.GCLogFile;
import com.microsoft.censum.io.SingleGCLogFile;
import com.microsoft.censum.jvm.JavaVirtualMachine;
import com.microsoft.censum.sample.aggregation.HeapOccupancyAfterCollectionAggregation;
import com.microsoft.censum.sample.aggregation.HeapOccupancyAfterCollectionSummary;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.execute();
    }

    public void execute() {
        GCLogFile logFile = new SingleGCLogFile(Path.of("./gclogs/preunified/cms/defnew/details/defnew.log"));
        Censum censum = new Censum();
        JavaVirtualMachine machine = censum.analyze(logFile);
        HeapOccupancyAfterCollectionSummary results = machine.getAggregation(HeapOccupancyAfterCollectionSummary.class);
        results.get().forEach((k,v) -> System.out.println(k + " : " + v.getItems().size()));
        //do something with results
    }
}
