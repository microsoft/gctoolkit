// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.time;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

public class DateTimeStamp {
    // Represents the time from Epoch
    // In the case where we have timestamps, the epoch is start of JVM
    // In the case where we only have date stamps, the epoch is 1970:01:01:00:00:00.000::UTC+0
    // All calculations in Censum make use of the double, timeStamp.
    // Calculations are based on an startup Epoch of 0.000 seconds. This isn't always the case and
    // certainly isn't the case when only date stamp is present. In these cases, start time is estimated.
    // This is surprisingly difficult to do thus use of timestamp is highly recommended.

    // todo: we need to remove knowledge of what an "EmptyDateTimeStamp".
//    final static private DateTimeStamp EmptyDateTimeStamp = new DateTimeStamp(Double.NaN);
//    public static DateTimeStamp emptyDateTimeStamp() { return emptyDateTimeStamp; }

    // Requirements
    // Timestamp can never be less than 0
    //      - use NaN to say it's not set
    final private ZonedDateTime dateTime;
    final private double timeStamp;

    // For some reason, ISO_DATE_TIME doesn't like that time-zone is -0100. It wants -01:00.
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static ZonedDateTime fromString(String iso8601DateTime) {
        if (iso8601DateTime != null) {
            TemporalAccessor temporalAccessor = formatter.parse(iso8601DateTime);
            return ZonedDateTime.from(temporalAccessor);
        }
        return null;
    }

    public DateTimeStamp(String iso8601DateTime) {
        this(fromString(iso8601DateTime));
    }

    public DateTimeStamp(ZonedDateTime dateTime) {
        this(dateTime,Double.NaN);
    }

    public DateTimeStamp(String iso8601DateTime, double timeStamp) {
        this(fromString(iso8601DateTime), timeStamp);
    }

    public DateTimeStamp(double timeStamp) {
        this((ZonedDateTime) null,timeStamp);
    }

    public DateTimeStamp(ZonedDateTime dateTime, double timeStamp) {
        this.dateTime = dateTime;
        //NaN is our agreed upon not set but less than 0 makes no sense either.
        if ( dateTime != null && (Double.isNaN(timeStamp) || timeStamp < 0.0d))
            this.timeStamp = (double)dateTime.toEpochSecond() + (double) dateTime.getNano() / 1_000_000_000d;
        else
            this.timeStamp = timeStamp;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public boolean hasDateStamp() {
        return getDateTime() != null;
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof  DateTimeStamp) {
            DateTimeStamp other = (DateTimeStamp)obj;
            boolean eq = getDateTime() == null ? other.getDateTime() == null : getDateTime().equals(other.getDateTime());
            return eq && getTimeStamp() == other.getTimeStamp();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDateTime(), getTimeStamp());
    }

    public String toString() {
        if (dateTime == null)
            return "@" + String.format("%.3f", timeStamp);
        else
            return dateTime + "@" + String.format("%.3f", timeStamp);
    }

    public boolean before(double timeStamp) {
        return getTimeStamp() < timeStamp;
    }

    public boolean after(double timeStamp) {
        return getTimeStamp() > timeStamp;
    }

    public boolean before(DateTimeStamp dateTimeStamp) {
        return !after(dateTimeStamp);
    }

    public boolean after(DateTimeStamp dateTimeStamp) {
        if ((dateTimeStamp.hasDateStamp()) && (this.hasDateStamp())) {
            int comparison = dateTimeStamp.compare(getDateTime());
            if (comparison < 0)
                return true;
            else if (comparison > 0)
                return false;
        }
        return after(dateTimeStamp.getTimeStamp());
    }

    public int compare(ZonedDateTime otherDate) {
        if (getDateTime().isAfter(otherDate)) return 1;
        if (getDateTime().isBefore(otherDate)) return -1;
        return 0;
    }

    public DateTimeStamp add(double offsetInDecimalSeconds) {
        DateTimeStamp now;
        if (dateTime != null) {
            int seconds = (int) offsetInDecimalSeconds;
            long nanos = (long) ((offsetInDecimalSeconds % 1) * 1_000_000_000L);
            now = new DateTimeStamp(dateTime.plusSeconds(seconds).plusNanos(nanos), timeStamp + offsetInDecimalSeconds);
        } else
            now = new DateTimeStamp(getTimeStamp() + offsetInDecimalSeconds);

        return now;
    }

    public DateTimeStamp minus(double offsetInDecimalSeconds) {
        return add(-offsetInDecimalSeconds);
    }

    public double minus(DateTimeStamp dateTimeStamp) {
        return timeStamp - dateTimeStamp.timeStamp;
    }

    public double timeSpanInMinutes(DateTimeStamp dateTimeStamp) {
        return this.minus(dateTimeStamp) / 60.0d;
    }

}
