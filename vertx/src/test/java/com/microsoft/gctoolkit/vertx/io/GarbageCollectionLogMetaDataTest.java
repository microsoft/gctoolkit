// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.io;

import com.microsoft.gctoolkit.io.FileDataSourceMetaData;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class GarbageCollectionLogMetaDataTest {
    @Test
    public void testPlainTextFile() {
        Path path = new TestLogFile("gc.log").getFile().toPath();
        FileDataSourceMetaData metaData = new FileDataSourceMetaData(path);
        assertEquals(1, metaData.getNumberOfFiles());
        assertFalse(metaData.isGZip());
        assertFalse(metaData.isZip());
        assertFalse(metaData.isDirectory());
        assertTrue(metaData.isFile());
    }

    @Test
    public void testSingleLogInZip() {
        Path path = new TestLogFile("gc.log.zip").getFile().toPath();
        FileDataSourceMetaData metaData = new FileDataSourceMetaData(path);
        assertEquals(2, metaData.getNumberOfFiles());
        assertFalse(metaData.isGZip());
        assertTrue(metaData.isZip());
        assertFalse(metaData.isDirectory());
        assertFalse(metaData.isFile());
    }

    @Test
    public void testRotatingLogs() {
        Path path = new TestLogFile("rotating.zip").getFile().toPath();
        FileDataSourceMetaData metaData = new FileDataSourceMetaData(path);
        assertEquals(2, metaData.getNumberOfFiles());
        assertFalse(metaData.isGZip());
        assertTrue(metaData.isZip());
        assertFalse(metaData.isDirectory());
        assertFalse(metaData.isFile());
    }

    @Test
    public void testZippedDirectoryWithRotatingLog() {
        Path path = new TestLogFile("rotating_directory.zip").getFile().toPath();
        FileDataSourceMetaData metaData = new FileDataSourceMetaData(path);
        assertEquals(5, metaData.getNumberOfFiles());
        assertFalse(metaData.isGZip());
        assertTrue(metaData.isZip());
        assertFalse(metaData.isDirectory());
        assertFalse(metaData.isFile());
    }

    @Test
    public void testGZipFile() {
        Path path = new TestLogFile("gc.log.gz").getFile().toPath();
        FileDataSourceMetaData metaData = new FileDataSourceMetaData(path);
        assertEquals(1, metaData.getNumberOfFiles());
        assertTrue(metaData.isGZip());
        assertFalse(metaData.isZip());
        assertFalse(metaData.isDirectory());
        assertFalse(metaData.isFile());
    }

    /*
    todo: not yet implemented so test should "fail"
     */
    @Test
    public void testGZipTarFile() {
        Path path = new TestLogFile("gc.log.tar.gz").getFile().toPath();
        FileDataSourceMetaData metaData = new FileDataSourceMetaData(path);
        assertEquals(1, metaData.getNumberOfFiles());
        assertTrue(metaData.isGZip());
        assertFalse(metaData.isZip());
        assertFalse(metaData.isDirectory());
        assertFalse(metaData.isFile());
    }

    @Test
    public void testDirectory() {
        Path path = new TestLogFile("rotating_directory").getFile().toPath();
        FileDataSourceMetaData metaData = new FileDataSourceMetaData(path);
        assertEquals(2, metaData.getNumberOfFiles());
        assertFalse(metaData.isGZip());
        assertFalse(metaData.isZip());
        assertTrue(metaData.isDirectory());
        assertFalse(metaData.isFile());
    }
}
