package com.microsoft.censum.sample;

import com.microsoft.censum.Censum;
import com.microsoft.censum.aggregator.Aggregation;
import com.microsoft.censum.aggregator.Aggregator;
import com.microsoft.censum.io.GCLogFile;
import com.microsoft.censum.io.SingleGCLogFile;
import com.microsoft.censum.jvm.JavaVirtualMachine;
import com.microsoft.censum.sample.aggregation.HeapOccupancyAfterCollection;
import com.microsoft.censum.sample.aggregation.HeapOccupancyAfterCollectionAggregation;
import com.microsoft.censum.sample.aggregation.HeapOccupancyAfterCollectionSummary;

import java.nio.file.Path;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.execute();
    }

    public void execute() {
        GCLogFile logFile = new SingleGCLogFile(Path.of(""));
        Censum censum = new Censum();
        JavaVirtualMachine machine = censum.analyze(logFile);
        Map<Class<? extends Aggregator<?>>, Aggregation> aggregations = machine.getAggregations();
        HeapOccupancyAfterCollectionAggregation results = (HeapOccupancyAfterCollectionAggregation) aggregations.get(HeapOccupancyAfterCollectionAggregation.class);
        //do something with results
    }
}
