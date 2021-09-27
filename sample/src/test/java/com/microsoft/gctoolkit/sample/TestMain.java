package com.microsoft.gctoolkit.sample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.microsoft.gctoolkit.sample.Main;

public class TestMain {

    @Test
    public void testMain() {
        String gcLogFile = System.getProperty("gcLogFile");

        Main main = new Main();
        main.analyze(gcLogFile);

        assertEquals(26, main.getInitialMarkCount());
        assertEquals(26, main.getRemarkCount());
        assertEquals(19114, main.getDefNewCount());
    }

}
