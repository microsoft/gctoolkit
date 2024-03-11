// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.time;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Comparator.*;


/**
 * A date and time. Both date and time come from reading the GC log. In cases where
 * the GC log has no date or time stamp, a DateTimeStamp is synthesized from the information
 * available in the log (event durations, for example).
 * <p>
 * Instance of DateTimeStamp are created by the parser. The constructors match what might be
 * found for dates and time stamps in a GC log file.
 */

public class DateTimeStamp implements Comparable<DateTimeStamp> {
    // Represents the time from Epoch
    // In the case where we have timestamps, the epoch is start of JVM
    // In the case where we only have date stamps, the epoch is 1970:01:01:00:00:00.000::UTC+0
    // All calculations in GCToolKit make use of the double, timeStamp.
    // Calculations are based on the startup Epoch of 0.000 seconds. This isn't always the case and
    // certainly isn't the case when only date stamp is present. In these cases, start time is estimated.
    // This is surprisingly difficult to do thus use of timestamp is highly recommended.

    // Requirements
    // Timestamp can never be less than 0
    //      - use NaN to say it's not set
    public final static double TIMESTAMP_NOT_SET = Double.NaN;
    public final static ZonedDateTime EPOC = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("GMT"));
    private final ZonedDateTime dateTime;
    private final double timeStamp;
    public static final Comparator<DateTimeStamp> comparator = getComparator();

    // For some reason, ISO_DATE_TIME doesn't like that time-zone is -0100. It wants -01:00.
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static ZonedDateTime dateFromString(String iso8601DateTime) {
        if (iso8601DateTime == null)
            return null;
        return ZonedDateTime.from(formatter.parse(iso8601DateTime));
    }

    private static double ageFromString(String doubleFormat) {
        if ( doubleFormat == null) return TIMESTAMP_NOT_SET;
        return Double.parseDouble(doubleFormat.replace(",","."));
    }

    // Patterns needed to support conversion of a log line to a DateTimeStamp

    private static final String DECIMAL_POINT = "(?:\\.|,)";
    private static final String INTEGER = "\\d+";
    private static final String DATE = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+|-]\\d{4}";
    private static final String TIME = INTEGER + DECIMAL_POINT + "\\d{3}";

    // Unified Tokens
    private static final String DATE_TAG = "\\[" + DATE + "]";
    private static final String UPTIME_TAG = "\\[(" + TIME + ")s]";

    // Pre-unified tokens
    private static final String TIMESTAMP = "(" + TIME + "): ";
    private static final String DATE_STAMP = "(" + DATE + "): ";
    private static final String DATE_TIMESTAMP = "^(?:" + DATE_STAMP + ")?" + TIMESTAMP;

    //  2017-09-07T09:00:12.795+0200: 0.716:
    private static final Pattern PREUNIFIED_DATE_TIMESTAMP = Pattern.compile(DATE_TIMESTAMP);
    // JEP 158 has ISO-8601 time and uptime in seconds and milliseconds as the first two decorators.
    private static final Pattern UNIFIED_DATE_TIMESTAMP = Pattern.compile("^(" + DATE_TAG + ")?(" + UPTIME_TAG + ")?");
    public static final DateTimeStamp EMPTY_DATE = new DateTimeStamp(EPOC, TIMESTAMP_NOT_SET);

    public static DateTimeStamp fromGCLogLine(String line) {
        Matcher matcher;
        int captureGroup = 2;
        if ( line.startsWith("[")) {
            matcher = UNIFIED_DATE_TIMESTAMP.matcher(line);
            captureGroup = 3;
        } else
            matcher = PREUNIFIED_DATE_TIMESTAMP.matcher(line);

        if ( matcher.find())
            return new DateTimeStamp(dateFromString(matcher.group(1)), ageFromString(matcher.group(captureGroup)));
        else
            return EMPTY_DATE;
    }

    /**
     * Provides a minimal date.
     * @return a minimal date
     */
    public static DateTimeStamp baseDate() {
        return new DateTimeStamp(EPOC, 0.0d);
    }

    /**
     * Create a DateTimeStamp by parsing an ISO 8601 date/time string.
     * @param iso8601DateTime A String in ISO 8601 format.
     */
    public DateTimeStamp(String iso8601DateTime) {
        this(dateFromString(iso8601DateTime));
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
        this(dateFromString(iso8601DateTime), timeStamp);
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
        if ( (timeStamp < 0.00d) || Double.isNaN(timeStamp))
            this.timeStamp = TIMESTAMP_NOT_SET;
        else
            // the time stamps in the log have 3 significant digits after the decimal. This corrects for that.
            this.timeStamp = Math.round(timeStamp * 1000.0d) / 1000.0d;
    }

    /**
     * Return the time stamp value. Allows a consistent time stamp be available to all calculations.
     * @return The time stamp value, in decimal seconds.
     */
    @Deprecated
    public double getTimeStamp() {
        if (!hasTimeStamp())
            return toEpochInMillis();
        return timeStamp;
    }

    public double toMilliseconds() {
        if (!hasTimeStamp())
            return toEpochInMillis();
        return timeStamp * 1000.0d;
    }

    public double toSeconds() {
        return toMilliseconds() / 1000.0d;
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
     * It is possible to have two DateTimeStamps from the same GC log, one with a DateStamp and one without.
     * @return {@code true} if the date stamp is not {@code null}.
     */
    public boolean hasDateStamp() {
        return ! (getDateTime() == null || EPOC.equals(getDateTime()));
    }

    public boolean hasTimeStamp() {
        return ! Double.isNaN(timeStamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DateTimeStamp) {
            DateTimeStamp other = (DateTimeStamp) obj;
            if (this.hasDateStamp())
                return this.getDateTime().equals(other.getDateTime()) &&
                        (this.getTimeStamp() == other.getTimeStamp());
            else
                return getTimeStamp() == other.getTimeStamp();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDateTime(), getTimeStamp());
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (hasDateStamp())
            buffer.append(getDateTime().toString());
        if (hasTimeStamp())
            buffer.append("@").append(String.format(Locale.US,"%.3f", getTimeStamp()));
        return buffer.toString();
    }

    /**
     * Return {@code true} if this DateTimeStamp comes before the other.
     * @param other The other DateTimeStamp.
     * @return {@code true} if this time stamp is less than the other.
     */
    public boolean before(DateTimeStamp other) {
        return compareTo(other) < 0;
    }

    /**
     * Return {@code true} if this DateTimeStamp comes after the other.
     * @param other The other DateTimeStamp.
     * @return {@code true} if this time stamp is less than the other.
     */
    public boolean after(DateTimeStamp other) {
        return compareTo(other) > 0;
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
        } else {
            throw new IllegalStateException("One or more DateStamp is missing");
        }
    }

    /**
     * Return a new {@code DateTimeStamp} resulting from adding the
     * decimal second offset to this.
     * @param offsetInDecimalSeconds An offset, in decimal seconds.
     * @return A new {@code DateTimeStamp}, {@code offsetInDecimalSeconds} from this.
     */
    public DateTimeStamp add(double offsetInDecimalSeconds) {
        if (Double.isNaN(offsetInDecimalSeconds))
            throw new IllegalArgumentException("Cannot add " + Double.NaN);

        double adjustedTimeStamp = Double.NaN;
        ZonedDateTime adjustedDateStamp = null;
        if ( hasTimeStamp()) {
            adjustedTimeStamp = getTimeStamp() + offsetInDecimalSeconds;
        }

        if (hasDateStamp()) {
            double offset = (Double.isNaN(offsetInDecimalSeconds)) ? 0.000d : offsetInDecimalSeconds;
            int seconds = (int) offset;
            long nanos = ((long) ((offset % 1) * 1_000L)) * 1_000_000L;
            adjustedDateStamp = dateTime.plusSeconds(seconds).plusNanos(nanos);
        }

        return new DateTimeStamp(adjustedDateStamp, adjustedTimeStamp);
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
     * @param other The other {@code DateTimeStamp}
     * @return The difference between this time stamp, and the time stamp
     * of the other DateTimeStamp.
     */
    public double minus(DateTimeStamp other) {
        if (hasTimeStamp() && other.hasTimeStamp())
            return getTimeStamp() - other.getTimeStamp();
        if (hasDateStamp() && other.hasDateStamp()) {
            double thisInSeconds = (double)getDateTime().toEpochSecond() + ((double)(getDateTime().getNano() / 1_000_000)) / 1000.0d;
            double otherInSeconds = (double)other.getDateTime().toEpochSecond() + ((double)(other.getDateTime().getNano() / 1_000_000)) / 1000.0d;
            return thisInSeconds - otherInSeconds;
        }
        return Double.NaN;
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

    /**
     * It will compare date time first, if both are equals then compare timestamp value,
     * For Null date time  considered to be last entry.
     * @param dateTimeStamp - other object to compared
     * @return  a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
     */
    @Override
    public int compareTo(DateTimeStamp dateTimeStamp) {
        return  comparator.compare(this,dateTimeStamp);
    }

    private static  Comparator<DateTimeStamp> getComparator(){
        // compare with dateTime field, if null then it will go to last
        // need a check to make sure these are comparable
        return nullsLast((o1, o2) -> {
            Comparator<DateTimeStamp> dateTimeStampComparator = compareDateTimeStamp(o1, o2);
            return dateTimeStampComparator.compare(o1,o2);
        });
    }

    private static Comparator<DateTimeStamp> compareDateTimeStamp(DateTimeStamp o1, DateTimeStamp o2) {
        if (o1.hasTimeStamp() && o2.hasTimeStamp())
            return comparingDouble(DateTimeStamp::getTimeStamp);
        else if (o1.hasDateStamp() && o2.hasDateStamp())
            return comparing(DateTimeStamp::getDateTime, ChronoZonedDateTime::compareTo);
        else
            throw new IllegalStateException("DateTimeStamp parameters cannot be compared as either timestamp or datestamp must be set in both instances.");
    }

    public double toEpochInMillis() {
        if ( dateTime != null) {
            return (double)(dateTime.toEpochSecond() * 1000) + (((double)dateTime.getNano()) / 1_000_000.0d);
        }
        return Double.NaN;
    }
}
