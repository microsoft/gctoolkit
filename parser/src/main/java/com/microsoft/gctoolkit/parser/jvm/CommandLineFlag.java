// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.jvm;

public enum CommandLineFlag {

    PrintGCApplicationStoppedTime("PrintGCApplicationStoppedTime"),
    PrintGCApplicationConcurrentTime("PrintGCApplicationConcurrentTime"),
    PrintGCTimeStamps("PrintGCTimeStamps"),
    PrintGCDetails("PrintGCDetails"),
    PrintGCCause("PrintGCCause"),
    PrintTenuringDistribution("PrintTenuringDistribution"),
    PrintAdaptiveSizePolicy("PrintAdaptiveSizePolicy"),
    CMSScavengeBeforeRemark("CMSScavengeBeforeRemark"),
    PrintHeapAtGC("PrintHeapAtGC"),
    PrintReferenceGC("PrintReferenceGC"),
    G1LogLevel("G1LogLevel"),
    G1PrintRegionLivenessInfo("G1PrintRegionLivenessInfo"),
    G1SummarizeRSetStats("G1SummarizeRSetStats"),
    PrintPromotionFailure("PrintPromotionFailure"),
    PrintFLSStatistics("PrintFLSStatistics");

    public static CommandLineFlag getEnumFromString(String string) {
        try {
            return Enum.valueOf(CommandLineFlag.class, string.trim());
        } catch (IllegalArgumentException ex) {
        }
        return null;
    }

    public static CommandLineFlag fromString(String name) {
        return getEnumFromString(name);
    }

    private final String label;

    CommandLineFlag(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
