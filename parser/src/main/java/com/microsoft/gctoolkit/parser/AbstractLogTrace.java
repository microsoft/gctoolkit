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
        return convertToDouble(matcher.group(1));
    }

    public String getDateStamp() {
        if (trace.find()) {
            return trace.group(4);
        } else {
            return null;
        }
    }

    /**
     *
     * @return the date timestamp paring found at the beginning of the GC log line.
     */
    public DateTimeStamp getDateTimeStamp() {
        return getDateTimeStamp(1);
    }

    /**
     * If a line contains multiple date timestamp group pairing then return the nth pair.
     * The following example contains 3 different date timestamps. index of 1 yields 57724.218
     * whereas index 3 yields 2010-04-21T10:45:33.367+0100@57724.319
     * <code>
     *     57724.218: [Full GC 57724.218: [CMS2010-04-21T10:45:33.367+0100: 57724.319: [CMS-concurrent-mark: 2.519/2.587 secs]
     * <code/>
     * one time stamp or a date timestamp pairing
     * @param nth is date timestamp field pair to be returned
     * @return the nth date timestamp pairing
     */
    public DateTimeStamp getDateTimeStamp(int nth) {
        Matcher matcher;
        if ( nth > 1) {
            matcher = DATE_TIME_STAMP_RULE.matcher(trace.group(0));
            for (int i = 0; i < nth; i++)
                if (!matcher.find())
                    break;
        } else
            matcher = trace;

        String timeStamp = ( matcher.group(3) == null) ? matcher.group(4) : matcher.group(3);
        String dateStamp = ( matcher.group(2) == null) ? matcher.group(5) : matcher.group(2);
        if (timeStamp != null) {
            return new DateTimeStamp(dateStamp, convertToDouble(timeStamp));
        } else if ( dateStamp != null)
            return new DateTimeStamp(dateStamp);
        return new DateTimeStamp(MISSING_TIMESTAMP_SENTINEL);
    }

    public GCLogTrace next() {
        if (trace.find())
            return new GCLogTrace(trace);
        return null;
    }
}
