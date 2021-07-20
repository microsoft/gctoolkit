// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.parser;

import com.microsoft.censum.parser.vmops.SafepointTrace;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that tracks whether a log entry was parsed successfully (hit), or not
 * (miss) and captures the origin of that hit or miss.
 */
public class SafepointParseRule {

    private static final ConcurrentHashMap<SafepointParseRule, AtomicInteger> hits = new ConcurrentHashMap<SafepointParseRule, AtomicInteger>();
    private static final ConcurrentHashMap<SafepointParseRule, AtomicInteger> misses = new ConcurrentHashMap<SafepointParseRule, AtomicInteger>();
    private static final ConcurrentHashMap<SafepointParseRule, Throwable> origin = new ConcurrentHashMap<SafepointParseRule, Throwable>();

    final private Pattern pattern;

    public SafepointParseRule(String pattern) {
        this.pattern = Pattern.compile(pattern);
        Throwable throwable = new Throwable();
        throwable = throwable.fillInStackTrace();
        origin.put(this, throwable);
    }

    /**
     * TODO This painful pattern of returning a null which gets checked by
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

    public static ConcurrentHashMap<SafepointParseRule, AtomicInteger> getHits() {
        return hits;
    }

    public static ConcurrentHashMap<SafepointParseRule, AtomicInteger> getMisses() {
        return misses;
    }

    public static ConcurrentHashMap<SafepointParseRule, Throwable> getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return pattern.toString();
    }
}
