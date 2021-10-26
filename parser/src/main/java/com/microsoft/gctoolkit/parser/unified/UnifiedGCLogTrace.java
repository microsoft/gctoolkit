// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unified;


import java.util.regex.Matcher;

public class UnifiedGCLogTrace {

    private final Matcher matcher;

    public UnifiedGCLogTrace(final Matcher matcher) {
        this.matcher = matcher;
    }

    public String get(final int index) {
        return matcher.group(index);
    }

    public int getInteger(final int index) {
        return 0;
    }

    public double getDouble(final int index) {
        return 0.0d;
    }
}
