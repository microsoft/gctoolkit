package com.microsoft.gctoolkit.sample;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestMain {

    //@Test
    public void testMain() {
        try {
            String gcLogFile = System.getProperty("gcLogFile");

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
