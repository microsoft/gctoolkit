// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

import java.util.Arrays;

/**
 * Representation of GC Collection Events
 */
public interface LabelledGCEventType {
    static <E extends Enum<E> & LabelledGCEventType> E fromLabel(Class<E> type,
                                                                 String label) {
        return Arrays.stream(type.getEnumConstants())
                .filter(gcType -> gcType.getLabel().equals(label))
                .findFirst()
                .orElse(null);
    }

    String getLabel();
}
