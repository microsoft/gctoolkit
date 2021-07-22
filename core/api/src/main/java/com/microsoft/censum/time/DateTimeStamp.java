// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.time;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

/**
 * A date and time. Both date and time come from reading the GC log. In cases where
 * the GC log has no date or time stamp, a DateTimeStamp is synthesized from the information
 * available in the log (event durations, for example).
 * <p>
 * Instance of DateTimeStamp are created by the parser. The constructors match what might be
 * found for dates and time stamps in a GC log file.
 */
public class DateTimeStamp {
    // Represents the time from Epoch
    // In the case where we have timestamps, the epoch is start of JVM
    // In the case where we only have date stamps, the epoch is 1970:01:01:00:00:00.000::UTC+0
    // All calculations in Censum make use of the double, timeStamp.
    // Calculations are based on an startup Epoch of 0.000 seconds. This isn't always the case and
    // certainly isn't the case when only date stamp is present. In these cases, start time is estimated.
    // This is surprisingly difficult to do thus use of timestamp is highly recommended.

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

    /**
     * Create a DateTimeStamp by parsing an ISO 8601 date/time string.
     * @param iso8601DateTime A String in ISO 8601 format.
     */
    public DateTimeStamp(String iso8601DateTime) {
        this(fromString(iso8601DateTime));
    }

    /**
     * Create a DateTimeStamp from a ZonedDateTime.
     * @param dateTime A ZonedDateTime
     */
    public DateTimeStamp(ZonedDateTime dateTime) {
        this(dateTime,Double.NaN);
    }

    /**
     * Create a DateTimeStamp from an IOS 8601 date/time string and
     * a time stamp. The time stamp represents decimal seconds.
     * @param iso8601DateTime A String in ISO 8601 format.
     * @param timeStamp A time stamp in decimal seconds.
     */
    public DateTimeStamp(String iso8601DateTime, double timeStamp) {
        this(fromString(iso8601DateTime), timeStamp);
    }

    /**
     * Create a DateTimeStamp from a time stamp.
     * @param timeStamp A time stamp in decimal seconds.
     */
    public DateTimeStamp(double timeStamp) {
        this((ZonedDateTime) null,timeStamp);
    }

    /**
     * Create a DateTimeStamp from a ZonedDateTime and a timestamp.
     * All other constructors end up here. If timeStamp is
     * {@code NaN} or less than zero, then the time stamp is extracted
     * from the ZonedDateTime.
     * @param dateTime A ZonedDateTime, which may be {@code null}.
     * @param timeStamp A time stamp in decimal seconds,
     *                  which should be greater than or equal to zero.
     */
    public DateTimeStamp(ZonedDateTime dateTime, double timeStamp) {
        this.dateTime = dateTime;
        //NaN is our agreed upon not set but less than 0 makes no sense either.
        if ( dateTime != null && (Double.isNaN(timeStamp) || timeStamp < 0.0d))
            this.timeStamp = (double)dateTime.toEpochSecond() + (double) dateTime.getNano() / 1_000_000_000d;
        else
            this.timeStamp = timeStamp;
    }

    /**
     * Return the time stamp value. All calculations in Censum use the time stamp value.
     * @return The time stamp value, in decimal seconds.
     */
    public double getTimeStamp() {
        return timeStamp;
    }

    /**
     * Return the date stamp.
     * @return The date stamp, which may be {@code null}
     */
    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Return {@code true} if the date stamp is not {@code null}.
     * @return {@code true} if the the date stamp is not {@code null}.
     */
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

    @Override
    public String toString() {
        if (dateTime == null)
            return "@" + String.format("%.3f", timeStamp);
        else
            return dateTime + "@" + String.format("%.3f", timeStamp);
    }

    /**
     * Return {@code true} if this time stamp is less than the other.
     * @param other The other time stamp, in decimal seconds.
     * @return {@code true} if this time stamp is less than the other.
     */
    public boolean before(double other) {
        return getTimeStamp() < other;
    }

    /**
     * Return {@code true} if this time stamp is greater than the other.
     * @param other The other time stamp, in decimal seconds.
     * @return {@code true} if this time stamp is greater than the other.
     */
    public boolean after(double other) {
        return getTimeStamp() > other;
    }

    /**
     * Return {@code true} if this DateTimeStamp comes before the other.
     * @param other The other DateTimeStamp.
     * @return {@code true} if this time stamp is less than the other.
     */
    public boolean before(DateTimeStamp other) {
        return !after(other);
    }

    /**
     * Return {@code true} if this DateTimeStamp comes after the other.
     * @param other The other DateTimeStamp.
     * @return {@code true} if this time stamp is less than the other.
     */
    public boolean after(DateTimeStamp other) {
        if ((other.hasDateStamp()) && (this.hasDateStamp())) {
            int comparison = other.compare(getDateTime());
            if (comparison < 0)
                return true;
            else if (comparison > 0)
                return false;
        }
        return after(other.getTimeStamp());
    }

    /**
     * Return {@code 1} if this date is after than the other,
     * {@code -1} if this date is before the other,
     * or {@code 0} if this date is the same as the other.
     * @param otherDate The other date.
     * @return {@code 1}, {@code 0}, {@code -1} if this date is
     * after, the same as, or before the other.
     */
    public int compare(ZonedDateTime otherDate) {
        if (hasDateStamp() && otherDate != null) {
            if (getDateTime().isAfter(otherDate)) return 1;
            if (getDateTime().isBefore(otherDate)) return -1;
            return 0;
        } else if (hasDateStamp()) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Return a new {@code DateTimeStamp} resulting from adding the
     * decimal second offset to this.
     * @param offsetInDecimalSeconds An offset, in decimal seconds.
     * @return A new {@code DateTimeStamp}, {@code offsetInDecimalSeconds} from this.
     */
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

    /**
     * Return a new {@code DateTimeStamp} resulting from subtracting the
     * decimal second offset from this.
     * @param offsetInDecimalSeconds An offset, in decimal seconds.
     * @return A new {@code DateTimeStamp}, {@code offsetInDecimalSeconds} from this.
     */
    public DateTimeStamp minus(double offsetInDecimalSeconds) {
        return add(-offsetInDecimalSeconds);
    }

    /**
     * Return the difference between this time stamp, and the time stamp of
     * the other DateTimeStamp.
     * @param dateTimeStamp The other {@code DateTimeStamp}
     * @return The difference between this time stamp, and the time stamp
     * of the other DateTimeStamp.
     */
    public double minus(DateTimeStamp dateTimeStamp) {
        return timeStamp - dateTimeStamp.timeStamp;
    }

    /**
     * Return the difference between time stamps, converted to minutes.
     * This is a convenience method for {@code this.minus(dateTimeStamp) / 60.0}.
     * @param dateTimeStamp The other {@code DateTimeStamp}
     * @return The difference between time stamps, converted to minutes.
     */
    public double timeSpanInMinutes(DateTimeStamp dateTimeStamp) {
        return this.minus(dateTimeStamp) / 60.0d;
    }

}
