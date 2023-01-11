// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.integration.io;

import com.microsoft.gctoolkit.io.LogFileSegment;
import com.microsoft.gctoolkit.io.RotatingGCLogFile;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RotatingGarbageCollectionLogFileTest {

    private String[][] expected = new String[][] {
            new String[] {
                    "rolling/jdk14/rollinglogs/long_restart.log",
                    "long_restart.log.3", "long_restart.log.4", "long_restart.log.0", "long_restart.log.1", "long_restart.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/restart_after_rollover.log",
                    "restart_after_rollover.log.4", "restart_after_rollover.log.0", "restart_after_rollover.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/rollover.log",
                    "rollover.log.3", "rollover.log.4", "rollover.log.0", "rollover.log.1", "rollover.log.2", "rollover.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/short_restart_2.log",
                    "short_restart_2.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/short_restart_3.log",
                    "short_restart_3.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/short_restart_4.log",
                    "short_restart_4.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/singleseries.log",
                    "singleseries.log.1", "singleseries.log.2", "singleseries.log"
            }
    };

    @Test
    public void getOrderedGarbageCollectionLogFiles() {
        for(String[] data : expected) {
            try {
                TestLogFile logFile = new TestLogFile(data[0]);
                RotatingGCLogFile garbageCollectionLogFile = new RotatingGCLogFile(logFile.getFile().toPath());
                List<LogFileSegment> segments = garbageCollectionLogFile.getOrderedGarbageCollectionLogFiles();
                assertEquals(data.length - 1, segments.size());
                for (int n = 1; n < data.length; n++) {
                    assertEquals(data[n], segments.get(n - 1).getPath().getFileName().toString());
                }
            } catch (Throwable e) {
                fail(e);
            }
        }
    }

    private void runRollingLogOrderingTest(Path path, List<String> expectedOrdering, long lineCount) {
        try {
            RotatingGCLogFile rotatingGCLogFile = new RotatingGCLogFile(path);
            List<String> actual =
                    rotatingGCLogFile
                            .getOrderedGarbageCollectionLogFiles()
                            .stream()
                            .map(segment -> segment.getSegmentName())
                            .collect(Collectors.toList());
            assertEquals(expectedOrdering, actual);
            long count = rotatingGCLogFile.stream().count();
            assertEquals(246733,count,"Unequal line counts");
        } catch (Exception badTestData) {
            fail(badTestData);
        }
    }

    @Test
    public void testRollingLogOrderUsage() {
        Path path = new TestLogFile("rolling/jdk14/rollinglogs/rollover.log").getFile().toPath();
        List<String> expected = Arrays.asList(
                "rollover.log.3", "rollover.log.4", "rollover.log.0", "rollover.log.1", "rollover.log.2", "rollover.log"
        );
        runRollingLogOrderingTest(path, expected, 246732);
    }

    @Test
    public void testRollingInZip() {
        Path path = new TestLogFile("rolling/jdk14/rollinglogs/zip/rollover.zip").getFile().toPath();
        List<String> expected = Arrays.asList(
                "rollover.log.3", "rollover.log.4", "rollover.log.0", "rollover.log.1", "rollover.log.2", "rollover.log"
        );
        runRollingLogOrderingTest(path,expected, 246733);
    }

    @Test
    public void testRollingInDirInZip() {
        Path path = new TestLogFile("rolling/jdk14/rollinglogs/zip/rolloverdir.zip").getFile().toPath();
        List<String> expected = Arrays.asList(
                "rollover/rollover.log.3", "rollover/rollover.log.4", "rollover/rollover.log.0", "rollover/rollover.log.1", "rollover/rollover.log.2", "rollover/rollover.log"
        );
        runRollingLogOrderingTest(path,expected, 246733);
    }
}
