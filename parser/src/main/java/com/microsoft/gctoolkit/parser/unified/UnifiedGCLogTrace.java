// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unified;


import java.util.regex.Matcher;

public class UnifiedGCLogTrace {

    private final Matcher matcher;

    public UnifiedGCLogTrace(Matcher matcher) {
        this.matcher = matcher;
    }

    public String get(int index) {
        return matcher.group(index);
    }

    public int getInteger(int index) {
        return 0;
    }

    public double getDouble(int index) {
        return 0.0d;
    }
}
