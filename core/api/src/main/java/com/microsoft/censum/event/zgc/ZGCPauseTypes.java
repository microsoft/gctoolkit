// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.zgc;

public enum ZGCPauseTypes {

    MARK_START("Mark Start"),
    MARK_END( "Mark End"),
    RELOCATE_START( "Relocate Start");

    private final String label;

    ZGCPauseTypes(String label) {
        this.label = label;
    }

    public static ZGCPauseTypes fromLabel(String label) {
        for (ZGCPauseTypes gcType : ZGCPauseTypes.values()) {
            if (gcType.label.equals(label))
                return gcType;
        }
        return null;
    }

    public String getLabel() {
        return label;
    }
}
