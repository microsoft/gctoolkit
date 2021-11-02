// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.parser.vmops.SafepointTrace;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that tracks whether a log entry was parsed successfully (hit), or not
 * (miss) and captures the origin of that hit or miss.
 */
public class SafepointParseRule {

    private static final ConcurrentMap<SafepointParseRule, AtomicInteger> hits = new ConcurrentHashMap<>();
    private static final ConcurrentMap<SafepointParseRule, AtomicInteger> misses = new ConcurrentHashMap<>();
    private static final ConcurrentMap<SafepointParseRule, Throwable> origin = new ConcurrentHashMap<>();

    private final Pattern pattern;

    public SafepointParseRule(String pattern) {
        this.pattern = Pattern.compile(pattern);
        Throwable throwable = new Throwable();
        throwable = throwable.fillInStackTrace();
        origin.put(this, throwable);
    }

    /**
     * TODO #155 This painful pattern of returning a null which gets checked by
     * the caller could be replaced by use of Optional
     *
     * @param trace The trace to match against the pattern
     * @return A trace with a valid matcher or null
     */
    public SafepointTrace parse(String trace) {
        Matcher matcher = pattern.matcher(trace);
        if (matcher.find()) {
            hits();
            return new SafepointTrace(matcher);
        } else {
            misses();
            return null;
        }
    }

    private void hits() {
        AtomicInteger count = hits.get(this);
        if (count == null) {
            count = new AtomicInteger(0);
            hits.put(this, count);
        }
        count.getAndIncrement();
    }

    private void misses() {
        AtomicInteger count = misses.get(this);
        if (count == null) {
            count = new AtomicInteger(0);
            misses.put(this, count);
        }
        count.getAndIncrement();
    }

    public static ConcurrentMap<SafepointParseRule, AtomicInteger> getHits() {
        return hits;
    }

    public static ConcurrentMap<SafepointParseRule, AtomicInteger> getMisses() {
        return misses;
    }

    public static ConcurrentMap<SafepointParseRule, Throwable> getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return pattern.toString();
    }
}
