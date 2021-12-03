package com.microsoft.gctoolkit.integration;

import com.microsoft.gctoolkit.sample.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestMain {

    //@Test
    public void testMain() {
        try {
            String gcLogFile = System.getProperty("gcLogFile");

            Main main = new Main();
            main.analyze(gcLogFile);

            Assertions.assertEquals(26, main.getInitialMarkCount());
            Assertions.assertEquals(26, main.getRemarkCount());
            Assertions.assertEquals(19114, main.getDefNewCount());
        } catch (IOException ioe) {
            Assertions.fail(ioe);
        }
    }

}
