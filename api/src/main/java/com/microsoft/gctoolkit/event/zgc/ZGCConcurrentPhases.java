// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

import com.microsoft.gctoolkit.event.LabelledGCEventType;

public enum ZGCConcurrentPhases implements LabelledGCEventType {

    MARK("Concurrent Mark"),
    REFERENCE_PROCESSING( "Reference Processing"),
    RELOCATION_SET_SELECTION( "Relocation Set Selection"),
    RELOCATE( "Relocate");

    private final String label;

    ZGCConcurrentPhases(String label) {
        this.label = label;
    }

    public static ZGCConcurrentPhases fromLabel(String label) {
        return LabelledGCEventType.fromLabel(ZGCConcurrentPhases.class, label);
    }

    public String getLabel() {
        return label;
    }
}