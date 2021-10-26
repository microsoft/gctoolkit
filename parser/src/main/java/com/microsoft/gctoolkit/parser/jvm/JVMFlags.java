// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.jvm;

import java.util.Map;

public enum JVMFlags {

    PrintGCApplicationConcurrentTime("PrintGCApplicationConcurrentTime"),
    PrintGCApplicationStoppedTime("PrintGCApplicationStoppedTime"),
    Unknown("unknown");

    private final String label;

    JVMFlags(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
    /*
        The following flags I found interesting but I would not recommend them for a normal production environment. However, this would, I think could generate great interest
        PrintClassHistogramBeforeFullGC
        PrintClassHistogramAfterFullGC

    */

    private final Map<String, Boolean> gcLoggingFlagSupport = Map.ofEntries(
            Map.entry("PrintGCApplicationConcurrentTime", true),
            Map.entry("PrintGCApplicationStoppedTime", true),
            Map.entry("PrintGCDetails", true),
            Map.entry("PrintGCDateStamps", true),
            Map.entry("PrintTenuringDistribution", true),
            Map.entry("PrintReferenceGC", true),
            Map.entry("PrintGCCause", true),
            Map.entry("PrintFLSStatistics", true),
            Map.entry("PrintFLSCensus", true),
            Map.entry("PrintPromotionFailure", true),
            Map.entry("PrintAdaptiveSizePolicy", true),
            Map.entry("PrintCMSInitiationStatistics", true),
            Map.entry("PrintTLAB", false),
            Map.entry("TLABStats", false),
            Map.entry("PrintPLAB", false),
            Map.entry("PrintOldPLAB", false)
    );

    public boolean supported(String flag) {
        return gcLoggingFlagSupport.get(flag);
    }
}
