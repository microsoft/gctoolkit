// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.parser.jvm.PreUnifiedDiarizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreUnifiedDiarizerTest {

    @Test
    void earlyTimestampLookupDoesNotPreventRecordingFirstEvent() {
        PreUnifiedDiarizer diarizer = new PreUnifiedDiarizer();
        assertEquals(0.0, diarizer.getTimeOfFirstEvent().toSeconds());

        diarizer.diarize("1.250: [GC (Allocation Failure) 100K->50K(200K), 0.001 secs]");
        assertEquals(1.250, diarizer.getTimeOfFirstEvent().toSeconds());
        assertEquals(1.250, diarizer.getDiary().getTimeOfFirstEvent().toSeconds());

        diarizer.diarize("2.500: [GC (Allocation Failure) 100K->50K(200K), 0.001 secs]");
        assertEquals(1.250, diarizer.getTimeOfFirstEvent().toSeconds());
        assertEquals(1.250, diarizer.getDiary().getTimeOfFirstEvent().toSeconds());
    }
}
