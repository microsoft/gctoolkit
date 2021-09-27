package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.parser.GCParseRule;

import java.util.Arrays;
import java.util.Objects;

public final class CommonTestHelper {
    private CommonTestHelper() {
    }

    public static int captureTest(GCParseRule rule, String[] lines) {
        return (int) Arrays.stream(lines)
                .map(rule::parse)
                .filter(Objects::nonNull)
                .count();
    }
}
