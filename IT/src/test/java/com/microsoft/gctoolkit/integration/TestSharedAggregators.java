package com.microsoft.gctoolkit.integration;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.integration.io.TestLogFile;
import com.microsoft.gctoolkit.integration.shared.OneRuntimeReport;
import com.microsoft.gctoolkit.integration.shared.TwoRuntimeReport;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

@Tag("modulePath")
public class TestSharedAggregators {

    private String testLog = "unified/cms/gc.log";

    @Test
    public void compareRuntimeDurations() {
        TestLogFile logFile = new TestLogFile(testLog);
        Path gcLogFile = logFile.getFile().toPath();
        GCLogFile log = new SingleGCLogFile(gcLogFile);
        GCToolKit toolKit = new GCToolKit();
        toolKit.loadAggregationsFromServiceLoader();
        JavaVirtualMachine jvm = null;
        try {
            jvm = toolKit.analyze(log);
        } catch (IOException e) {
            Assertions.fail(e);
        }

        jvm.getAggregation(OneRuntimeReport.class).ifPresentOrElse(
                oneRuntimeReport -> Assertions.assertEquals(8.772d, oneRuntimeReport.getRuntimeDuration()),
                () -> Assertions.fail("1 report missing"));

        jvm.getAggregation(TwoRuntimeReport.class).ifPresentOrElse(
                twoRuntimeReport -> Assertions.assertEquals(8.772d, twoRuntimeReport.getRuntimeDuration()),
                () -> Assertions.fail("2 report missing"));
    }
}