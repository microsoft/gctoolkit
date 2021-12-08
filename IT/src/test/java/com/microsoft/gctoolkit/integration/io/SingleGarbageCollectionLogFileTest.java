// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.integration.io;

import com.microsoft.gctoolkit.io.LogFileMetadata;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.Diary;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SingleGarbageCollectionLogFileTest {


    private String[] unifiedLogs = { "unified/cms/gc.log", "unified/g1gc/G1-80-16gbps2.log.0"};

    @Test
    public void unifiedLog() {
        for(String log : unifiedLogs) {
            Path path = new TestLogFile(log).getFile().toPath();
            SingleGCLogFile gcLogFile = new SingleGCLogFile(path);
            assertEquals(true, gcLogFile.isUnified(), "Expected unified but failed");
            try {
                assertEquals(1, gcLogFile.getMetaData().getNumberOfFiles(), "Expected 1 but found " + gcLogFile.getMetaData().getNumberOfFiles());
            } catch (IOException ioe) {
                fail(ioe);
            }

            try {
                gcLogFile.stream().findFirst();
                assertTrue(true); // that we made here means we were able to stream this file
            } catch(IOException ioe) {
                fail(ioe);
            }

            try {
                Diary diary = gcLogFile.diary();
                assertNotEquals(null, diary, "Unable to get the diary");
                assertTrue(diary.isUnifiedLogging());
            } catch (IOException ioe) {
                fail(ioe);
            }

            try {
                LogFileMetadata metadata = gcLogFile.getMetaData();
                assertTrue(metadata.isPlainText());
                assertFalse(metadata.isZip());
                assertFalse(metadata.isGZip());
                assertFalse(metadata.isDirectory());
                assertEquals(1, metadata.getNumberOfFiles(), "Expected 1 file but " + metadata.getNumberOfFiles() + " found.");
            } catch (IOException ioe) {
                fail(ioe);
            }
        }
    }
}
