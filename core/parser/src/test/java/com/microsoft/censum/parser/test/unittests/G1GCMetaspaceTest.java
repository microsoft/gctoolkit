// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.parser.test.unittests;


import com.microsoft.censum.parser.test.TestLogFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class G1GCMetaspaceTest extends ParserTest {

    @Test
    public void countMetaspaceRecordsTest() {
        int i = 0;
        for (String name : details) {
            try {
                Path path = new TestLogFile("g1gc/details/" + name).getFile().toPath();
                TestResults testResults = testRegionalSingleLogFile(path);
                assertEquals(permGenMetaspaceCounts[i++], testResults.getMetaSpaceRecordCount(), "Meta or Perm space record count mismatch: ");
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }

    }

    String[] details = {
            "gc.log.4.31_1_aug_sep_current.zip"
    };

    int[] permGenMetaspaceCounts = {
            15
    };
}
