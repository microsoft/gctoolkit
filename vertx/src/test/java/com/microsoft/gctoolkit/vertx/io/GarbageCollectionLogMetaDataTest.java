// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.io;

import com.microsoft.gctoolkit.io.RotatingLogFileMetadata;
import com.microsoft.gctoolkit.io.SingleLogFileMetadata;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class GarbageCollectionLogMetaDataTest {
    @Test
    public void testPlainTextFile() {
        Path path = new TestLogFile("gc.log").getFile().toPath();
        SingleLogFileMetadata metaData = null;
        try {
            metaData = new SingleLogFileMetadata(path);
        } catch (IOException e) {
            fail(e);
        }
        assertEquals(1, metaData.getNumberOfFiles());
        assertFalse(metaData.isGZip());
        assertFalse(metaData.isZip());
        assertFalse(metaData.isDirectory());
        assertTrue(metaData.isPlainText());
    }

    @Test
    public void testSingleLogInZip() {
        try {
            Path path = new TestLogFile("gc.log.zip").getFile().toPath();
            SingleLogFileMetadata metaData = new SingleLogFileMetadata(path);
            assertEquals(1, metaData.getNumberOfFiles());
            assertFalse(metaData.isGZip());
            assertTrue(metaData.isZip());
            assertFalse(metaData.isDirectory());
            assertFalse(metaData.isPlainText());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test   //todo: make sure the order is correct
    public void testRotatingLogs() {
        try {
            Path path = new TestLogFile("rotating.zip").getFile().toPath();
            RotatingLogFileMetadata metaData = new RotatingLogFileMetadata(path);
            assertEquals(2, metaData.getNumberOfFiles());
            assertFalse(metaData.isGZip());
            assertTrue(metaData.isZip());
            assertFalse(metaData.isDirectory());
            assertFalse(metaData.isPlainText());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void testZippedDirectoryWithRotatingLog() {
        try {
            Path path = new TestLogFile("rotating_directory.zip").getFile().toPath();
            RotatingLogFileMetadata metaData = new RotatingLogFileMetadata(path);
            assertEquals(2, metaData.getNumberOfFiles());
            assertFalse(metaData.isGZip());
            assertTrue(metaData.isZip());
            assertFalse(metaData.isDirectory());
            assertFalse(metaData.isPlainText());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void testGZipFile() {
        try {
            Path path = new TestLogFile("gc.log.gz").getFile().toPath();
            SingleLogFileMetadata metaData = new SingleLogFileMetadata(path);
            assertEquals(1, metaData.getNumberOfFiles());
            assertTrue(metaData.isGZip());
            assertFalse(metaData.isZip());
            assertFalse(metaData.isDirectory());
            assertFalse(metaData.isPlainText());
        } catch (IOException e) {
            fail(e);
        }
    }

    /*
    todo: not yet implemented so test should "fail"
     */
    @Test
    public void testGZipTarFile() {
        try {
            Path path = new TestLogFile("gc.log.tar.gz").getFile().toPath();
            SingleLogFileMetadata metaData = new SingleLogFileMetadata(path);
            assertEquals(1, metaData.getNumberOfFiles());
            assertTrue(metaData.isGZip());
            assertFalse(metaData.isZip());
            assertFalse(metaData.isDirectory());
            assertFalse(metaData.isPlainText());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void testDirectory() {
        try {
            Path path = new TestLogFile("rotating_directory").getFile().toPath();
            RotatingLogFileMetadata metaData = new RotatingLogFileMetadata(path);
            assertEquals(2, metaData.getNumberOfFiles());
            assertFalse(metaData.isGZip());
            assertFalse(metaData.isZip());
            assertTrue(metaData.isDirectory());
            assertFalse(metaData.isPlainText());
        } catch (IOException e) {
            fail(e);
        }
    }
}
