// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

public enum ZGCConcurrentPhases {

    MARK("Concurrent Mark"),
    REFERENCE_PROCESSING( "Reference Processing"),
    RELOCATION_SET_SELECTION( "Relocation Set Selection"),
    RELOCATE( "Relocate");

    private final String label;

    ZGCConcurrentPhases(String label) {
        this.label = label;
    }

    public static ZGCConcurrentPhases fromLabel(String label) {
        for (ZGCConcurrentPhases gcType : ZGCConcurrentPhases.values()) {
            if (gcType.label.equals(label))
                return gcType;
        }
        return null;
    }

    public String getLabel() {
        return label;
    }
}
