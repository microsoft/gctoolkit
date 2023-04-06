package com.microsoft.gctoolkit.io;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RotatingGCLogTest {

    @Test
    void orderRotatingLogsTest() {
        Path path = new TestLogFile("G1-80-16gbps2.log").getFile().toPath();
        try {
            RotatingGCLogFile file = new RotatingGCLogFile(path);
            assertEquals(2, file.getMetaData().getNumberOfFiles());
            assertEquals(2, file.getMetaData().logFiles().map(LogFileSegment::getPath).map(Path::toFile).map(File::getName).filter(s -> s.startsWith("G1-80-16gbps2")).count());
            file.getMetaData().logFiles().map(LogFileSegment::getEndTime).forEach(System.out::println);
        } catch (IOException ioe) {
            fail(ioe);
        }
    }
}
