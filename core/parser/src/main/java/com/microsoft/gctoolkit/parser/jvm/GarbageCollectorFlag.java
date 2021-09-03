// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.jvm;


public enum GarbageCollectorFlag {

    UseG1GC("UseG1GC"),
    UseParNewGC("UseParNewGC "),
    UseConcMarkSweepGC("UseConcMarkSweepGC"),
    UseParallelGC("UseParallelGC"),
    UseParallelOldGC("UseParallelOldGC"),
    UseSerialGC("UseSerialGC"),
    CMSIncrementialMode("CMSIncrementalMode");

    public static GarbageCollectorFlag getEnumFromString(String string) {
        try {
            return Enum.valueOf(GarbageCollectorFlag.class, string.trim());
        } catch (IllegalArgumentException ex) {
        }
        return null;
    }

    public static GarbageCollectorFlag fromString(String name) {
        return getEnumFromString(name);
    }

    private final String label;

    GarbageCollectorFlag(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
