package com.microsoft.gctoolkit.event.zgc;

import java.util.Arrays;
import java.util.Objects;

public enum ZGCCycleType {
    FULL("Garbage"), // Legacy ZGC
    MINOR("Minor"),
    MAJOR("Major");

    private final String cycleLabel;

    ZGCCycleType(String cycleLabel) {
        this.cycleLabel = cycleLabel;
    }

    public static ZGCCycleType get(String label) {
        return Arrays.stream(ZGCCycleType.class.getEnumConstants())
                .filter(collectionType -> Objects.equals(collectionType.cycleLabel, label))
                .findFirst()
                .orElse(null);
    }

    public static ZGCCycleType fromPhase(ZGCPhase phase){
        if(phase == ZGCPhase.FULL){
            return ZGCCycleType.FULL;
        } else if (phase == ZGCPhase.MAJOR_YOUNG || phase == ZGCPhase.MAJOR_OLD){
            return ZGCCycleType.MAJOR;
        } else if (phase == ZGCPhase.MINOR_YOUNG){
            return ZGCCycleType.MINOR;
        } else {
            throw new IllegalArgumentException(String.format("Unknown ZGCPhase: %s", phase));
        }
    }
}
