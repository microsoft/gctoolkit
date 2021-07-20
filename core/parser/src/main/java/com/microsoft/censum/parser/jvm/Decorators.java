// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.parser.jvm;

import com.microsoft.censum.time.DateTimeStamp;
import com.microsoft.censum.parser.unified.UnifiedLoggingLevel;
import com.microsoft.censum.parser.unified.UnifiedLoggingTokens;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class Decorators {

    private final static Logger LOGGER = Logger.getLogger(Decorators.class.getName());

    private final static long TWENTY_YEARS_IN_MILLIS = 731L * 24L * 60L * 60L * 1000L;
    private final static long TWENTY_YEARS_IN_NANO = 731L * 24L * 60L * 60L * 1000L;

    private Matcher decorators = null;
    private List<String> tags = new ArrayList<>();


    public Decorators(String line) {
        extractValues(line);
    }

    private void extractValues(String line) {

        if (!line.startsWith("["))
            return;

        decorators = UnifiedLoggingTokens.DECORATORS.matcher(line);
        if (!decorators.find()) {
            decorators = null;
        }

        Matcher tagMatcher = UnifiedLoggingTokens.TAGS.matcher(line);
        if (tagMatcher.find()) {
            tags.addAll(Arrays.asList(tagMatcher.group(0).trim().split("[\\[\\]]")));
            tags = tags.stream()
                    .filter(decorator -> !decorator.isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
        }
    }

    public boolean found() {
        return !tags.isEmpty();
    }

    // For some reason, ISO_DATE_TIME doesn't like that time-zone is -0100. It wants -01:00.
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public ZonedDateTime getDateStamp() {
        try {
            String value = decorators.group(1);
            if (value != null) {
                TemporalAccessor temporalAccessor = formatter.parse(value.substring(1, value.length()-1));
                return ZonedDateTime.from(temporalAccessor);            }
        } catch (NullPointerException npe) {
            LOGGER.log(Level.SEVERE, npe.getMessage(), npe);
        }
        return null;
    }

    public double getUpTime() {
        String value = decorators.group(2);
        if (value != null) {
            value = value.replace(",", ".");
            return Double.parseDouble(unboxValue(value, 1));
        }
        return -1.0d;
    }

    private long extractClock(int groupIndex, long threshold) {
        long clockReading = -1L;
        String stringValue = decorators.group(groupIndex);
        if (stringValue != null) {
            clockReading = Long.parseLong(unboxValue(stringValue, 2));
            if (decorators.group(groupIndex + 1) == null)
                if (clockReading < threshold)
                    clockReading = -1L;
        }
        return clockReading;
    }

    public long getTimeMillis() {
        return extractClock(3, TWENTY_YEARS_IN_MILLIS);
    }

    public long getUptimeMillis() {
        String value = decorators.group(4);
        if (value == null) {
            value = decorators.group(3);
        }
        if (value != null) {
            long longValue = Long.parseLong(unboxValue(value, 2));
            if (longValue < TWENTY_YEARS_IN_MILLIS)
                return longValue;
        }
        return -1L;
    }

    public long getTimeNano() {
        return extractClock(5, TWENTY_YEARS_IN_NANO);
    }

    public long getUptimeNano() {
        String value = decorators.group(6);
        if (value == null) {
            value = decorators.group(5);
        }
        if (value != null) {
            long longValue = Long.parseLong(unboxValue(value, 2));
            if (longValue < TWENTY_YEARS_IN_NANO)
                return longValue;
        }
        return -1L;
    }

    public int getPid() {
        String value = decorators.group(7);
        if (value != null) {
            return Integer.parseInt(unboxValue(value));
        }
        return -1;
    }

    public int getTid() {
        String value = decorators.group(8);
        if (value != null) {
            return Integer.parseInt(unboxValue(value));
        }
        return -1;
    }

    public Optional<UnifiedLoggingLevel> getLogLevel() {
        String level = decorators.group(9);
        if (level != null)
            try {
                return Optional.of(UnifiedLoggingLevel.valueOf(unboxValue(level)));
            } catch (IllegalArgumentException e) {
                LOGGER.fine("No such debug level: " + level);
                LOGGER.fine(e.getMessage());
                return Optional.empty();
            }
        return Optional.empty();
    }

    public DateTimeStamp getDateTimeStamp() {
        return new DateTimeStamp(getDateStamp(), getUpTime());
    }

    public int getNumberOfDecorators() {
        return tags.size();
    }

    private String unboxValue(String boxedValue, int postFix) {
        return boxedValue.substring(1, boxedValue.length() - (1 + postFix));
    }

    private String unboxValue(String boxedValue) {
        return unboxValue(boxedValue, 0);
    }

    public boolean tagsContain(String tagList) {
        return tags.contains(tagList);
    }

    public List<String> getTags() {
        return tags;
    }
}