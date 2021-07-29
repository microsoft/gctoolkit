// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class AbstractLogTrace {

    private static final String US_FORMAT_SEPARATOR = ".";
    private static final String EUROPEAN_FORMAT_SEPARATOR = ",";
    private static final double MISSING_TIMESTAMP_SENTINEL = -1.0d;

    private static final Pattern TIMES_STAMP_RULE = Pattern.compile(PreUnifiedTokens.TIMESTAMP);
    private static final Pattern DATE_TIME_STAMP_RULE = Pattern.compile(PreUnifiedTokens.DATE_TIMESTAMP);
    protected final Matcher trace;

    public AbstractLogTrace(Matcher matcher) {
        this.trace = matcher;
    }

    public int length() {
        return trace.group(0).length();
    }

    public int groupCount() {
        return trace.groupCount();
    }

    public boolean groupNotNull(int index) {
        return getGroup(index) != null;
    }

    public long getLongGroup(int index) {
        return Long.parseLong(trace.group(index));
    }

    public int getIntegerGroup(int index) {
        return Integer.parseInt(trace.group(index));
    }

    public String getGroup(int index) {
        return trace.group(index);
    }

    public double getDoubleGroup(int index) {
        return convertToDouble(trace.group(index));
    }

    protected double convertToDouble(String value) {
        return Double.parseDouble(value.replaceAll(EUROPEAN_FORMAT_SEPARATOR, US_FORMAT_SEPARATOR));
    }

    public double getPercentageGroup(int i) {
        String value = getGroup(i);
        return convertToDouble(value.substring(0, value.length() - 1));
    }

    public double getTimeStamp() {
        Matcher matcher = TIMES_STAMP_RULE.matcher(trace.group(0));
        if (!matcher.find()) {
            return MISSING_TIMESTAMP_SENTINEL;
        }
        return getDoubleGroup(2);
    }

    public String getDateStamp() {
        Matcher matcher = DATE_TIME_STAMP_RULE.matcher(trace.group(0));
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public DateTimeStamp getDateTimeStamp() {
        return getDateTimeStamp(1);
    }

    public DateTimeStamp getDateTimeStamp(int index) {
        Matcher matcher = DATE_TIME_STAMP_RULE.matcher(trace.group(0));
        for (int i = 0; i < index - 1; i++)
            if (!matcher.find())
                break;
        if (matcher.find()) {
            if (matcher.group(2) == null) {
                return new DateTimeStamp(matcher.group(1));
            }
            return new DateTimeStamp(matcher.group(1), convertToDouble(matcher.group(2)));
        }
        return new DateTimeStamp(MISSING_TIMESTAMP_SENTINEL);
    }

    public GCLogTrace next() {
        if (trace.find())
            return new GCLogTrace(trace);
        return null;
    }
}
