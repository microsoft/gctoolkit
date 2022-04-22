package com.microsoft.gctoolkit.integration.core;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.integration.io.TestLogFile;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class PreunifiedJavaVirtualMachineConfigurationTest {

    private String logFile = "preunified/g1gc/details/tenuring/180/g1gc.log";
    private int[] times = { 0, 1028, 945481, 945481};

    @Test
    public void testSingle() {
        TestLogFile log = new TestLogFile(logFile);
        test(new SingleGCLogFile(log.getFile().toPath()), times);
    }

    private void test(GCLogFile log, int[] endStartTimes ) {
        GCToolKit gcToolKit = new GCToolKit();
        gcToolKit.loadAggregationsFromServiceLoader();
        JavaVirtualMachine machine = null;
        try {
            machine = gcToolKit.analyze(log);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        System.out.println("************************************************************");
        Assertions.assertEquals( endStartTimes[0], (int)(machine.getEstimatedJVMStartTime().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[1], (int)(machine.getTimeOfFirstEvent().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[2], (int)(machine.getJVMTerminationTime().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[3], (int)(machine.getRuntimeDuration() * 1000.0d));
        System.out.println("************************************************************");
    }
}
