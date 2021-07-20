package com.microsoft.censum.parser.datatype;

import java.util.Locale;

/**
 * When a boolean just won't do. Parsing a log file is a process of discovery. Nothing is known until it is known.
 */
public enum TripleState {
    UNKNOWN,
    TRUE,
    FALSE;

    /**
     * @return {@code true} if {@code this != TripleState.UNKOWN}
     */

    public boolean isKnown() {
        return this != UNKNOWN;
    }

    /**
     * @return {@code true} if {@code this == TripleState.TRUE}
     */
    public boolean isTrue() {
        return this == TRUE;
    }

    /**
     * @return {@code true} if {@code this == TripleState.FALSE}
     */
    public boolean isFalse() {
        return this == FALSE;
    }

    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
