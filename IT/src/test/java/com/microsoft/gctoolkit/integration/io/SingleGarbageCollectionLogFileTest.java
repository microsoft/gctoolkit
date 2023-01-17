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

    private String[] unifiedLogs = { "unified/cms/gc.log", "unified/g1gc/G1-80-16gbps2.log.0", "icms/details/tenuring/par.cms.wt.wd.ast.log.zip", "streaming/gc.log.gz"};
    private boolean[] logIsUnified = { true, true, false, false};
    private boolean[] logIsZipped = { false, false, true, false};
    private boolean[] logIsGZipped = { false, false, false, true};

    @Test
    public void unifiedLog() {
        for (int index = 0; index < unifiedLogs.length; index++)
            logStreamingTest(unifiedLogs[index], logIsUnified[index], logIsZipped[index], logIsGZipped[index]);
    }

    private void logStreamingTest(String log, boolean unified, boolean zipped, boolean gzipped) {
        Path path = new TestLogFile(log).getFile().toPath();
        SingleGCLogFile gcLogFile = new SingleGCLogFile(path);
        assertEquals(unified, gcLogFile.isUnified(), "Expected unified but failed");
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
            assertEquals(unified, diary.isUnifiedLogging());
        } catch (IOException ioe) {
            fail(ioe);
        }

        try {
            LogFileMetadata metadata = gcLogFile.getMetaData();
            assertEquals( !(zipped || gzipped), metadata.isPlainText());
            assertEquals(zipped, metadata.isZip());
            assertEquals(gzipped, metadata.isGZip());
            assertFalse(metadata.isDirectory());
            assertEquals(1, metadata.getNumberOfFiles(), "Expected 1 file but " + metadata.getNumberOfFiles() + " found.");
        } catch (IOException ioe) {
            fail(ioe);
        }
    }
}
