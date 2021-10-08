// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unified;

public enum UnifiedLoggingLevel {

    trace("trace"),
    debug("debug"),
    info("info"),
    warning("warning"),
    error("error");

    private final String label;

    UnifiedLoggingLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * @param other logging level to compare this one to
     * @return whether this logging level is lesser or equal to the other one
     */
    public boolean isLessThanOrEqualTo(UnifiedLoggingLevel other) {
        return this.compareTo(other) <= 0;
    }


    /**
     * @param other logging level to compare this one to
     * @return whether this logging level is greater or equal to the other one
     */
    public boolean isGreaterThanOrEqualTo(UnifiedLoggingLevel other) {
        return this.compareTo(other) >= 0;
    }

}
