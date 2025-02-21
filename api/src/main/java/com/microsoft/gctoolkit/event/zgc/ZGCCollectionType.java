package com.microsoft.gctoolkit.event.zgc;

public enum ZGCCollectionType {
    FULL("Garbage"), // Legacy ZGC
    MINOR("Minor"),
    MAJOR("Major");

    private final String collectionLabel;

    ZGCCollectionType(String collectionLabel) {
        this.collectionLabel = collectionLabel;
    }

    public static ZGCCollectionType get(String label) {
        for (ZGCCollectionType status : ZGCCollectionType.values()) {
            if (status.collectionLabel.equalsIgnoreCase(label.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("No matching ZGCCollectionType found for: " + label);
    }
}
