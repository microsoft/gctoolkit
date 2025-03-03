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
}
