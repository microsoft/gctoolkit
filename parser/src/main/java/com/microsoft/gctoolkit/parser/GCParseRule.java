// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that tracks whether a log entry was parsed successfully (hit), or not
 * (miss) and captures the origin of that hit or miss.
 */
public class GCParseRule {

    private final String name;
    private final Pattern pattern;

    public GCParseRule(String name, String pattern) {
        this.name = name;
        this.pattern = Pattern.compile(pattern);
    }

    /**
     * TODO This painful pattern of returning a null which gets checked by
     * the caller could be replaced by use of Optional
     * todo: for some reason the matcher is getting corrupted, synchronized helps! Need to sort out corruption
     *
     * @param trace The trace to match against the pattern
     * @return A trace with a valid matcher or null
     */
    public GCLogTrace parse(String trace) {
        Matcher matcher = pattern.matcher(trace);
        if (matcher.find()) {
            return new GCLogTrace(matcher);
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name + " -> " + pattern.toString();
    }

    public Pattern pattern() { return pattern; }
}
