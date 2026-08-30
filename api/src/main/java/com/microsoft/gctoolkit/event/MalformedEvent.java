// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

/**
 * Thrown when a GC log fragment cannot be parsed into an event.
 */
public class MalformedEvent extends Exception {
    public MalformedEvent(String message) {
        super(message);
    }
}
