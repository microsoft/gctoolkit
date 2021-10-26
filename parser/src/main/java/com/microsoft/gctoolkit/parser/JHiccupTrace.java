// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that represents a chunk of a jHiccup log that we are attempting to match to a
 */
public class JHiccupTrace {

    //0.124,1.004,2.408,HIST
    private static final String TIME = "(\\d+(?:,|.)\\d+)";
    public static final Pattern JHICCUP_LOG_ENTRY = Pattern.compile(TIME + "," + TIME + "," + TIME + ",HIST");
    private static final String US_FORMAT_SEPARATOR = ".";
    private static final String EUROPEAN_FORMAT_SEPARATOR = ",";

    // TODO #150 Why -1.0d as the default?
    // private static final double DEFAULT_TIMESTAMP = -1.0d;

    private final Matcher trace;

    public static JHiccupTrace toTrace(String line) {
        Matcher m = JHICCUP_LOG_ENTRY.matcher(line);
        return (m == null) ? null : new JHiccupTrace(m);
    }

    protected JHiccupTrace(Matcher matcher) {
        this.trace = matcher;
    }

    public double getTimeStamp() {
        return getDoubleGroup(1);
    }

    public double getInterval() {
        return getDoubleGroup(2);
    }

    public double getDuration() {
        return getDoubleGroup(3);
    }

    public double getDoubleGroup(int index) {
        return convertToDouble(trace.group(index));
    }

    private double convertToDouble(String value) {
        return Double.parseDouble(value.replaceAll(EUROPEAN_FORMAT_SEPARATOR, US_FORMAT_SEPARATOR));
    }

    public String getGroup(int index) {
        return trace.group(index);
    }
}
