// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.zgc;

import com.microsoft.gctoolkit.event.LabelledGCEventType;

public enum ZGCPauseTypes implements LabelledGCEventType {

    MARK_START("Mark Start"),
    MARK_END( "Mark End"),
    RELOCATE_START( "Relocate Start");

    private final String label;

    ZGCPauseTypes(String label) {
        this.label = label;
    }

    public static ZGCPauseTypes fromLabel(String label) {
        return LabelledGCEventType.fromLabel(ZGCPauseTypes.class, label);
    }

    public String getLabel() {
        return label;
    }
}
