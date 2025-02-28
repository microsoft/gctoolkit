package com.microsoft.gctoolkit.event.zgc;

import java.util.Arrays;
import java.util.Objects;

public enum ZGCPhase {
    FULL(null),
    MAJOR_YOUNG("Y"),
    MAJOR_OLD("O"),
    MINOR_YOUNG("y");

    private final String phase;

    ZGCPhase(String s) {
        this.phase = s;
    }

    public String getPhase() {
        return phase;
    }

    public static ZGCPhase get(String label) {
        return Arrays.stream(ZGCPhase.class.getEnumConstants())
                .filter(phase -> Objects.equals(phase.getPhase(), label))
                .findFirst()
                .orElse(null);
    }
}
