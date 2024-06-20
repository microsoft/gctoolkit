package com.microsoft.gctoolkit.integration.core;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.integration.io.TestLogFile;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.RotatingGCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class UnifiedJavaVirtualMachineConfigurationTest {

    private String logFile = "rolling/jdk14/rollinglogs/long_restart.log";
    private int[][] times = { { 0, 13, 262172, 262159}, { 259077, 259077, 262172, 3992}};

    @Tag("modulePath")
    @Test
    public void testRotating() {
        TestLogFile log = new TestLogFile(logFile);
        test(new RotatingGCLogFile(log.getFile().toPath()), times[0]);
    }

    @Tag("modulePath")
    @Test
    public void testSingle() {
        TestLogFile log = new TestLogFile(logFile);
        test(new SingleGCLogFile(log.getFile().toPath()), times[1]);
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
        Assertions.assertEquals( endStartTimes[0], (int)(machine.getEstimatedJVMStartTime().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[1], (int)(machine.getTimeOfFirstEvent().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[2], (int)(machine.getJVMTerminationTime().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[3], (int)(machine.getRuntimeDuration() * 1000.0d));
    }
}
