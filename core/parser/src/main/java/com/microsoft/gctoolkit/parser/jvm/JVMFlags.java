// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.jvm;


import java.util.HashMap;

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

    private HashMap<String, Boolean> gcLoggingFlagSupport;

    {
        gcLoggingFlagSupport = new HashMap<String, Boolean>();
        gcLoggingFlagSupport.put("PrintGCApplicationConcurrentTime", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintGCApplicationStoppedTime", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintGCDetails", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintGCDateStamps", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintTenuringDistribution", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintReferenceGC", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintGCCause", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintFLSStatistics", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintFLSCensus", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintPromotionFailure", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintAdaptiveSizePolicy", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintCMSInitiationStatistics", Boolean.TRUE);
        gcLoggingFlagSupport.put("PrintTLAB", Boolean.FALSE);
        gcLoggingFlagSupport.put("TLABStats", Boolean.FALSE);
        gcLoggingFlagSupport.put("PrintPLAB", Boolean.FALSE);
        gcLoggingFlagSupport.put("PrintOldPLAB", Boolean.FALSE);
    }

    public boolean supported(String flag) {
        return gcLoggingFlagSupport.get(flag);
    }
}
