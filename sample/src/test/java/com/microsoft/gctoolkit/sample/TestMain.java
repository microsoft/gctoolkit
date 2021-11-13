package com.microsoft.gctoolkit.sample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestMain {

    @Test
    public void testMain() {
        try {
            String gcLogFile = "gclogs/unified/g1gc/G1-80-16gbps2.log.0";

            Main main = new Main();
            main.analyze(gcLogFile);

            assertEquals(26, main.getInitialMarkCount());
            assertEquals(26, main.getRemarkCount());
            assertEquals(19114, main.getDefNewCount());
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

}
