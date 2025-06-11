package com.microsoft.gctoolkit.event.zgc;

import java.util.Arrays;
import java.util.Objects;

public enum ZGCCollectionType {
    FULL("Garbage"), // Legacy ZGC
    MINOR("Minor"),
    MAJOR("Major");

    private final String collectionLabel;

    ZGCCollectionType(String collectionLabel) {
        this.collectionLabel = collectionLabel;
    }

    public static ZGCCollectionType get(String label) {
        return Arrays.stream(ZGCCollectionType.class.getEnumConstants())
                .filter(collectionType -> Objects.equals(collectionType.collectionLabel, label))
                .findFirst()
                .orElse(null);
    }

    public static ZGCCollectionType fromPhase(ZGCPhase phase){
        if(phase == ZGCPhase.FULL){
            return ZGCCollectionType.FULL;
        } else if (phase == ZGCPhase.MAJOR_YOUNG || phase == ZGCPhase.MAJOR_OLD){
            return ZGCCollectionType.MAJOR;
        } else if (phase == ZGCPhase.MINOR_YOUNG){
            return ZGCCollectionType.MINOR;
        } else {
            throw new IllegalArgumentException(String.format("Unknown ZGCPhase: %s", phase));
        }
    }
}
