// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.io;

import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.RotatingGCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class StreamGarbageCollectionLogFileTest {
    
    private GCLogFile createLogFile(Path path, boolean rotating) throws IOException {
        return rotating ? new RotatingGCLogFile(path) : new SingleGCLogFile(path);
    }

    @Test
    public void testPlainGCLogLineCount() {
        try {
            Path path = new TestLogFile("streaming/gc.log").getFile().toPath();
            GCLogFile logFile = createLogFile(path,false);
            assertEquals(1,logFile.getMetaData().getNumberOfFiles(),"file count mismatch");
            assertExpectedLineCount(431604, logFile);
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    @Test
    public void testGZipGCLogLineCount() {
        try {
            Path path = new TestLogFile("streaming/gc.log.gz").getFile().toPath();
            GCLogFile logFile = createLogFile(path,false);
            assertEquals(1,logFile.getMetaData().getNumberOfFiles(),"file count mismatch");
            assertExpectedLineCount(18305, logFile);
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    @Test
    public void testGZipTarGCLogLineCount() {
        try {
            Path path = new TestLogFile("streaming/gc.log.tar.gz").getFile().toPath();
            GCLogFile logFile = createLogFile(path,false);
            assertEquals(1,logFile.getMetaData().getNumberOfFiles(),"file count mismatch");
            assertExpectedLineCount(410055, logFile);
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    @Test
    public void testZipGCLogLineCount() {
        try {
            Path path = new TestLogFile("streaming/gc.log.zip").getFile().toPath();
            GCLogFile logFile = createLogFile(path,false);
            assertEquals(1,logFile.getMetaData().getNumberOfFiles(),"file count mismatch");
            assertExpectedLineCount(431604, logFile);
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    @Test
    public void testZipRotatingGCLogLineCount() {
        try {
            Path path = new TestLogFile("rotating.zip").getFile().toPath();
            GCLogFile logFile = createLogFile(path,true);
            assertEquals(2,logFile.getMetaData().getNumberOfFiles(),"file count mismatch");
            assertExpectedLineCount(72210, logFile);
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    @Test
    public void testDirectoryRotatingGCLogLineCount() {
        try {
            Path path = new TestLogFile("rotating_directory").getFile().toPath();
            GCLogFile logFile = createLogFile(path, true);
            assertEquals(2, logFile.getMetaData().getNumberOfFiles(), "file count mismatch");
            assertExpectedLineCount(72210, logFile);
        } catch (IOException ioe) {
            assertEquals(ioe.getClass(), IOException.class);
        }
    }

    @Test
    public void testZippedDirectoryRotatingLogLineCount() {
        try {
            Path path = new TestLogFile("rotating_directory.zip").getFile().toPath();
            GCLogFile logFile = createLogFile(path, true);
            assertEquals(2, logFile.getMetaData().getNumberOfFiles(), "file count mismatch");
            assertExpectedLineCount(72210, logFile);
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    private void assertExpectedLineCount(long expectedCount, GCLogFile logFile) {
        try {
            Stream<String> logStream = logFile.stream();
            assertEquals(expectedCount,logStream.count());
            logStream.close();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
    }
}
