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
    
    private GCLogFile loadLogFile(Path path, boolean rotating) {
        return rotating ? new RotatingGCLogFile(path) : new SingleGCLogFile(path);
    }
    
    @Test
    public void testPlainTextFileLineCount() {
        Path path = new TestLogFile("streaming/gc.log").getFile().toPath();
        assertExpectedLineCount(431603, loadLogFile(path, false));
    }

    @Test
    public void testSingleLogInZipLineCount() {
        Path path = new TestLogFile("gc.log.zip").getFile().toPath();
        assertExpectedLineCount(431603, loadLogFile(path, false));
    }

    @Test
    public void testRotatingLogsLineCount() {
        Path path = new TestLogFile("rotating.zip").getFile().toPath();
        assertExpectedLineCount(52029, loadLogFile(path, false));
    }

    @Test
    public void testZippedDirectoryWithRotatingLogLineCount() {
        Path path = new TestLogFile("rotating_directory.zip").getFile().toPath();
        assertExpectedLineCount(52029, loadLogFile(path, false));
    }

    @Test
    public void testGZipFileLineCount() {
        Path path = new TestLogFile("gc.log.gz").getFile().toPath();
        assertExpectedLineCount(18304, loadLogFile(path, false));
    }

    /*
    todo: not yet implemented so test should "fail"
     */
    @Test
    public void testGZipTarFileLineCount() {
        Path path = new TestLogFile("gc.log.tar.gz").getFile().toPath();
        assertExpectedLineCount(410055, loadLogFile(path, false));
    }

    @Test
    public void testFilesInDirectoryTotalLineCount() {
        Path path = new TestLogFile("rotating_directory").getFile().toPath();
        try {
            GCLogFile logFile = loadLogFile(path, true);
            Stream<String> stream = logFile.stream();
            stream.close();
        } catch (IOException ioe) {
            assertEquals(ioe.getClass(), IOException.class);
        }
    }

    @Test
    public void testPlainTextFileRotatingLineCount() {
        Path path = new TestLogFile("gc.log").getFile().toPath();
        try {
            GCLogFile logFile = loadLogFile(path, true);
            Stream<String> stream = logFile.stream();
            stream.close();
        } catch (IOException ioe) {
            assertEquals(ioe.getClass(), IOException.class);
            assertEquals("Not a rotating GC log", ioe.getMessage());
        } catch (IllegalArgumentException iae) {
            assertEquals("Unable to read as rotating GC log", iae.getMessage());
        }
    }

    @Test
    public void testSingleLogInZipRotatingLineCount() {
        Path path = new TestLogFile("gc.log.zip").getFile().toPath();
        assertExpectedLineCount(431603, loadLogFile(path, true));
    }

    @Test
    public void testRotatingLogsRotatingLineCount() {
        Path path = new TestLogFile("rotating.zip").getFile().toPath();
        assertExpectedLineCount(72209, loadLogFile(path, true));
    }

    @Test
    public void testZippedDirectoryWithRotatingLogRotatingLineCount() {
        Path path = new TestLogFile("rotating_directory.zip").getFile().toPath();
        assertExpectedLineCount(72210, loadLogFile(path, true));
    }

    @Test
    public void testGZipFileRotatingLineCount() {
        Path path = new TestLogFile("gc.log.gz").getFile().toPath();
        try {
            GCLogFile logFile = new RotatingGCLogFile(path);
            Stream<String> stream = logFile.stream();
            stream.close();
        } catch (IOException ioe) {
            assertEquals(ioe.getClass(), IOException.class);
            assertEquals("Unable to stream GZip files. Please unzip and retry", ioe.getMessage());
        }
    }

    /*
    todo: not yet implemented so test should "fail"
     */
//    @Test
//    public void testGZipTarFileRotating() {
//        Path path = new TestLogFile("gc.log").getFile().toPath();
//        assertExpectedLineCount(410055, new RotatingGarbageCollectionLogFile( Paths.get("gc.log.tar.gz")));
//    }

    @Test
    public void testDirectoryRotating() {
        Path path = new TestLogFile("rotating_directory").getFile().toPath();
        assertExpectedLineCount(72209, loadLogFile(path, true));
    }

    private void assertExpectedLineCount(long expectedCount, GCLogFile directory) {
        try {
            Stream<String> directoryStream = directory.stream();
            assertEquals(expectedCount, directoryStream.count());
            directoryStream.close();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
    }
}
