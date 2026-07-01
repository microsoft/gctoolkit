// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.time;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class DateTimeStampTest {

    // For some reason, ISO_DATE_TIME doesn't like that time-zone is -0100. It wants -01:00.
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Test
    void getTimeStamp() {
        var dateTimeStamp = new DateTimeStamp(.586);
        assertEquals(.586, dateTimeStamp.getTimeStamp(), 0.0001);

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertEquals(1.522836600586E12, dateTimeStamp.getTimeStamp(), 0.0001);

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertEquals(.18, dateTimeStamp.getTimeStamp(), 0.0001);
    }

    @Test
    void getDateStampAsString() {
        var dateTimeStamp = new DateTimeStamp(.586);
        assertEquals("@0.586", dateTimeStamp.toString());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertEquals("2018-04-04T09:10:00.586-01:00", dateTimeStamp.toString());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertEquals("2018-04-04T09:10:00.586-01:00@0.180", dateTimeStamp.toString());
    }

    @Test
    void getDateTime() {
        var dateTimeStamp = new DateTimeStamp(.586);
        assertNull(dateTimeStamp.getDateTime());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertEquals(ZonedDateTime.from(formatter.parse("2018-04-04T09:10:00.586-0100")), dateTimeStamp.getDateTime());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertEquals(ZonedDateTime.from(formatter.parse("2018-04-04T09:10:00.586-0100")), dateTimeStamp.getDateTime());
    }

    @Test
    void hasDateStamp() {
        var dateTimeStamp = new DateTimeStamp(.586);
        assertFalse(dateTimeStamp.hasDateStamp());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertTrue(dateTimeStamp.hasDateStamp());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertTrue(dateTimeStamp.hasDateStamp());
    }

    @Test
    void testHash() {
        var a = new DateTimeStamp(.586);
        var b = new DateTimeStamp(.587);
        assertNotEquals(a.hashCode(), b.hashCode());

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        assertNotEquals(a.hashCode(), b.hashCode());

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        assertNotEquals(a.hashCode(), b.hashCode());

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", 0.18);
        assertNotEquals(a.hashCode(), b.hashCode());

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0000", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertNotEquals(a.hashCode(), b.hashCode());

        a = new DateTimeStamp(.587);
        b = new DateTimeStamp(.587);
        assertEquals(a.hashCode(), b.hashCode());

        a = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        assertEquals(a.hashCode(), b.hashCode());

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        assertEquals(a.hashCode(), b.hashCode());

    }

    @Test
    void testEquals() {
        var a = new DateTimeStamp(0.586d);
        var b = new DateTimeStamp(0.586d);
        assertEquals(a,b);
        assertEquals(b,a);
        b = new DateTimeStamp(.587);
        assertNotEquals(a, b);
        assertNotEquals(b,a);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertEquals(a, b);
        assertEquals(b,a);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        assertNotEquals(a, b);
        assertNotEquals(b,a);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertEquals(a, b);
        assertEquals(b,a);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        assertNotEquals(a, b);
        assertNotEquals(b,a);

        b = new DateTimeStamp("2018-04-04T09:10:00.586-0200", 0.18);
        assertNotEquals(a, b);
        assertNotEquals(b,a);

        b = new DateTimeStamp("2018-04-04T09:10:00.586-0200", 0.19);
        assertNotEquals(a, b);
        assertNotEquals(b,a);

    }

    @Test
    void testBefore() {
        DateTimeStamp a;
        DateTimeStamp b;

        a = new DateTimeStamp(.586);
        b = new DateTimeStamp(.586);
        assertFalse(b.before(a));
        assertFalse(a.before(b));
        b = new DateTimeStamp(.587);
        assertTrue(a.before(b));
        assertFalse(b.before(a));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertFalse(b.before(a));
        assertFalse(a.before(b));
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        assertTrue(a.before(b));
        assertFalse(b.before(a));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertFalse(b.before(a));
        assertFalse(a.before(b));
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        assertTrue(a.before(b));
        assertFalse(b.before(a));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertFalse(b.before(a));
        assertFalse(a.before(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", 0.19);
        assertTrue(a.before(b));
        assertFalse(b.before(a));
    }

    @Test
    void testAfter() {
        DateTimeStamp a;
        DateTimeStamp b;

        a = new DateTimeStamp(.586);
        b = new DateTimeStamp(.586);
        assertFalse(b.after(a));
        assertFalse(a.after(b));
        b = new DateTimeStamp(.587);
        assertTrue(b.after(a));
        assertFalse(a.after(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertFalse(b.after(a));
        assertFalse(a.after(b));
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        assertTrue(b.after(a));
        assertFalse(a.after(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertFalse(b.after(a));
        assertFalse(a.after(b));
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        assertTrue(b.after(a));
        assertFalse(a.after(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertFalse(b.after(a));
        assertFalse(a.after(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", 0.19);
        assertTrue(b.after(a));
        assertFalse(a.after(b));
    }

    @Test
    void compare() {
        var a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        var b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        assertTrue(a.compare(b.getDateTime()) < 0);
        assertTrue(b.compare(a.getDateTime()) > 0);
        assertEquals(0, a.compare(new DateTimeStamp("2018-04-04T09:10:00.586-0100").getDateTime()));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", 0.18);
        assertTrue(a.compare(b.getDateTime()) < 0);
        assertTrue(b.compare(a.getDateTime()) > 0);
        assertEquals(0, a.compare(new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18).getDateTime()));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0000", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertTrue(a.compare(b.getDateTime()) < 0);
        assertTrue(b.compare(a.getDateTime()) > 0);
        assertEquals(0, a.compare(new DateTimeStamp("2018-04-04T09:10:00.586-0000", 0.18).getDateTime()));
    }

    @Test
    void add() {
        var a = new DateTimeStamp(.586);
        var a_ts = a.getTimeStamp();
        ZonedDateTime a_dt = a.getDateTime();
        var b = new DateTimeStamp(.586 + .587);
        assertEquals(b.getTimeStamp(), a.add(.587).getTimeStamp(), .001);
        assertEquals(a_ts, a.getTimeStamp(), .001); // test that a is unmodified
        assertEquals(a_dt, a.getDateTime()); // test that a is unmodified

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        a_ts = a.getTimeStamp();
        a_dt = a.getDateTime();
        b = new DateTimeStamp("2018-04-04T09:10:01.173-0100");
        assertEquals(b.getDateTime(), a.add(.587).getDateTime());
        assertEquals(a_ts, a.getTimeStamp(), .001); // test that a is unmodified
        assertEquals(a_dt, a.getDateTime()); // test that a is unmodified

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        a_ts = a.getTimeStamp();
        a_dt = a.getDateTime();
        b = new DateTimeStamp("2018-04-04T09:10:01.173-0100", 0.18 + .587);
        DateTimeStamp c = a.add(.587);
        assertEquals(b.getDateTime(), c.getDateTime());
        assertEquals(b.getTimeStamp(), c.getTimeStamp(), 0.001);
        assertEquals(a_ts, a.getTimeStamp(), .001); // test that a is unmodified
        assertEquals(a_dt, a.getDateTime()); // test that a is unmodified

    }

    @Test
    void minus() {
        var a = new DateTimeStamp(.586);
        var a_ts = a.getTimeStamp();
        ZonedDateTime a_dt = a.getDateTime();
        var b = new DateTimeStamp(.586 - .587);
        assertEquals(b.getTimeStamp(), a.minus(.587).getTimeStamp(), .001);
        assertEquals(a_ts, a.getTimeStamp(), .001); // test that a is unmodified
        assertEquals(a_dt, a.getDateTime()); // test that a is unmodified

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        a_ts = a.getTimeStamp();
        a_dt = a.getDateTime();
        b = new DateTimeStamp("2018-04-04T09:09:59.999-0100");
        assertEquals(b.getDateTime(), a.minus(.587).getDateTime());
        assertEquals(a_ts, a.getTimeStamp(), .001); // test that a is unmodified
        assertEquals(a_dt, a.getDateTime()); // test that a is unmodified

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        a_ts = a.getTimeStamp();
        a_dt = a.getDateTime();
        b = new DateTimeStamp("2018-04-04T09:09:59.999-0100", 0.18 - .587);
        DateTimeStamp c = a.minus(.587);
        assertEquals(b.getDateTime(), c.getDateTime());
        assertEquals(b.getTimeStamp(), c.getTimeStamp(), 0.001);
        assertEquals(a_ts, a.getTimeStamp(), .001); // test that a is unmodified
        assertEquals(a_dt, a.getDateTime()); // test that a is unmodified

    }

    @Test
    void testMinus() {
        var a = new DateTimeStamp(.586);
        var b = new DateTimeStamp(.587);
        var diff = a.minus(b);
        assertEquals(.586 - .587, diff, .001);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        diff = a.minus(b);
        var expected = -0.001d;
        assertEquals(expected, diff, 0.001);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", .18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", .19);
        diff = a.minus(b);
        assertEquals(.18 - .19, diff, .001);

    }

    @Test
    void timeSpanInMinutes() {
        var a = new DateTimeStamp(.586);
        var b = new DateTimeStamp(.587);
        var diff = b.timeSpanInMinutes(a);
        assertEquals( 0.001d / 60.0d, diff, .001);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        diff = b.timeSpanInMinutes(a);
        assertEquals(0.001d / 60.0d, diff, .001);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", .18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", .19);
        diff = b.timeSpanInMinutes(a);
        assertEquals((0.19d - 0.18d) / 60d, diff, .001);
    }

    @Test
    void testCompareLessThan() {
        var smaller = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        var greater = new DateTimeStamp("2018-04-04T10:10:00.587-0100");
        assertEquals(-1, smaller.compareTo(greater));
    }

    @Test
    void testCompareGreaterThan() {
        var smaller = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        var greater = new DateTimeStamp("2018-04-04T10:10:00.587-0100");
        assertEquals(1, greater.compareTo(smaller));
    }

    @Test
    void testCompareEquals() {
        var object1 = new DateTimeStamp("2018-04-04T10:10:00.586-0100");
        var object2 = new DateTimeStamp("2018-04-04T10:10:00.586-0100");
        assertEquals(0, object1.compareTo(object2));
    }

    @Test
    void testCompareEqualsTimeStamp() {
        var object1 = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 123);
        var object2 = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 123);
        assertEquals(0, object2.compareTo(object1));
    }

    @Test
    void testCompareGreaterTimeStamp() {
        var smaller = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 122);
        var greater = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 123);
        assertEquals(1, greater.compareTo(smaller));
    }

    @Test
    void testCompareSmallerTimeStamp() {
        var smaller = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 123);
        var greater = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 124);
        assertEquals(-1, smaller.compareTo(greater));
    }

    @Test
    void testCompareNullValue() {
        var smaller = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertEquals(-1, smaller.compareTo(null));
    }

    @Test
    void testCompareEqualsTimeStampWithoutDateTime() {
        var object1 = new DateTimeStamp(123);
        var object2 = new DateTimeStamp(123);
        assertEquals(0, object2.compareTo(object1));
    }

    @Test
    void testCompareGreaterTimeStampWithoutDateTime() {
        var smaller = new DateTimeStamp(122);
        var greater = new DateTimeStamp(124);
        assertEquals(1, greater.compareTo(smaller));
    }

    @Test
    void testCompareSmallerTimeStampWithoutDateTime() {
        var smaller = new DateTimeStamp(123);
        var greater = new DateTimeStamp(124);
        assertEquals(-1, smaller.compareTo(greater));
    }

    @Test
    void testAddingNaN() {
        assertThrows(IllegalArgumentException.class,
                () -> { new DateTimeStamp("2018-04-04T10:10:00.586-0100").add(Double.NaN); },
                "IllegalAccess Not Thrown");
    }

    @Test
    void testNanWithMinusNonZero() {
        assertThrows(IllegalArgumentException.class,
                () -> { new DateTimeStamp("2018-04-04T10:10:00.586-0100").minus(Double.NaN); },
                "IllegalAccess Not Thrown");
    }

    @Test
    void testNanWithMinus() {
        var dateTimeStamp = new DateTimeStamp("2018-04-04T10:09:59.586-0100");
        DateTimeStamp forComparing = new DateTimeStamp("2018-04-04T10:10:00.586-0100").minus(1.0d);
        assertEquals(dateTimeStamp, forComparing);
    }

    @Test
    void testNanWithConstructor() {
        var dateTimeStamp = new DateTimeStamp((String) null,Double.NaN);
        var forComparing = new DateTimeStamp(0.0);
        assertNotEquals(dateTimeStamp, forComparing);
    }

    /*
     *
     */
    @Test
    void compareWithNANValue(){
        // This is an illegal state which is unlikely to happen in the context of a single GC log.
        var dateTimeStamp = new DateTimeStamp(12d);
        var dateTimeStampCompare = new DateTimeStamp(Double.NaN);
        assertThrows(IllegalStateException.class,
                () -> { dateTimeStamp.compareTo(dateTimeStampCompare); },
                "IllegalStateException Not Thrown");
    }


    @Test
    void compareWithNullDates() {
        var stamp1 = new DateTimeStamp((String)null, 100);
        var stamp2 = new DateTimeStamp((String)null, 200);
        var stamp3 = new DateTimeStamp((String)null, 100);
        assertTrue(stamp1.compareTo(stamp2) < 0);
        assertTrue(stamp1.compareTo(stamp3) == 0);
        assertTrue(stamp2.compareTo(stamp3) > 0);
    }

    @Test
    void compareToTransitivity() {
        var stamp1 = new DateTimeStamp("2021-09-01T11:12:13.111-0100", 100);
        var stamp2 = new DateTimeStamp((String)null, 200);
        var stamp3 = new DateTimeStamp("2021-08-31T11:12:13.111-0100", 300);
        assertTrue(stamp1.compareTo(stamp2) < 0);
        assertTrue(stamp2.compareTo(stamp3) < 0);
        assertTrue(stamp1.compareTo(stamp3) < 0);
    }

    @Test
    void compareToTransitivityWithEqualTimeStamps() {
        var stamp1 = new DateTimeStamp("2021-09-01T11:12:13.111-0100", 100);
        var stamp2 = new DateTimeStamp((String)null, 100);
        var stamp3 = new DateTimeStamp("2021-08-31T11:12:13.111-0100", 100);
        var comp1To2 = stamp1.compareTo(stamp2);
        var comp2To3 = stamp2.compareTo(stamp3);
        assertEquals(comp1To2, comp2To3);
        var comp1To3 = stamp1.compareTo(stamp3);
        assertEquals(comp1To2, comp1To3, "compareTo() is not transitive");
    }

    @Test
    void malformedDateTimeStampThrowsException() {
        final var onlyDate = new DateTimeStamp("2021-09-01T11:12:13.111-0100");
        final var onlyTime = new DateTimeStamp(1.0D);
        assertThrows(IllegalStateException.class, () -> onlyDate.after(onlyTime));
    }

    @Test 
    void shouldParseGCLogLineWithBrackets() {
        final var dateTimeString = "2025-05-08T11:07:55.681+0530";
        final String gcLogLine = "[" + dateTimeString + "][gc,phases   ] GC(4)   Other: 0.2ms";
        final DateTimeStamp dateTimeStamp = DateTimeStamp.fromGCLogLine(gcLogLine);
        final ZonedDateTime expected = ZonedDateTime.from(formatter.parse(dateTimeString));
        assertTrue(expected.isEqual(dateTimeStamp.getDateTime()));
    }
}