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
    public void getTimeStamp() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp(.586);
        assertEquals(.586, dateTimeStamp.getTimeStamp(), 0.0001);

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        double timeStamp = dateTimeStamp.getTimeStamp();
        assertEquals(.586, timeStamp - (long) timeStamp, 0.0001);

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertEquals(.18, dateTimeStamp.getTimeStamp(), 0.0001);
    }

    @Test
    public void getDateStampAsString() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp(.586);
        assertEquals("@0.586", dateTimeStamp.toString());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertEquals("2018-04-04T09:10:00.586-01:00@1522836600.586", dateTimeStamp.toString());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertEquals("2018-04-04T09:10:00.586-01:00@0.180", dateTimeStamp.toString());
    }

    @Test
    public void getDateTime() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp(.586);
        assertNull(dateTimeStamp.getDateTime());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertEquals(ZonedDateTime.from(formatter.parse("2018-04-04T09:10:00.586-0100")), dateTimeStamp.getDateTime());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertEquals(ZonedDateTime.from(formatter.parse("2018-04-04T09:10:00.586-0100")), dateTimeStamp.getDateTime());
    }

    @Test
    public void hasDateStamp() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp(.586);
        assertFalse(dateTimeStamp.hasDateStamp());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertTrue(dateTimeStamp.hasDateStamp());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertTrue(dateTimeStamp.hasDateStamp());
    }

    @Test
    public void testHash() {
        DateTimeStamp a = new DateTimeStamp(.586);
        DateTimeStamp b = new DateTimeStamp(.587);
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
    public void testEquals() {
        DateTimeStamp a = new DateTimeStamp(.586);
        DateTimeStamp b = new DateTimeStamp(.587);
        assertNotEquals(a, b);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        assertNotEquals(a, b);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        assertNotEquals(a, b);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", 0.18);
        assertNotEquals(a, b);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0000", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertNotEquals(a, b);

        a = new DateTimeStamp(.587);
        b = new DateTimeStamp(.587);
        assertEquals(a, b);

        a = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        assertEquals(a, b);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        assertEquals(a, b);
    }

    @Test
    public void testBefore() {
        DateTimeStamp a = new DateTimeStamp(.586);
        DateTimeStamp b = new DateTimeStamp(.587);
        assertTrue(a.before(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        assertTrue(a.before(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        assertTrue(a.before(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", 0.18);
        assertTrue(a.before(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0000", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertTrue(a.before(b));


        a = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertFalse(a.before(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertFalse(a.before(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.587-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertFalse(a.before(b));

        a = new DateTimeStamp(.586);
        b = new DateTimeStamp(.586);
        assertTrue(a.before(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertTrue(a.before(b));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertTrue(a.before(b));
    }

    @Test
    public void testAfter() {
        DateTimeStamp a = new DateTimeStamp(.586);
        DateTimeStamp b = new DateTimeStamp(.587);
        assertTrue(b.after(a));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        assertTrue(b.after(a));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        assertTrue(b.after(a));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", 0.18);
        assertTrue(b.after(a));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0000", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertTrue(b.after(a));


        a = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertFalse(b.after(a));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.19);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertFalse(b.after(a));

        a = new DateTimeStamp("2018-04-04T09:10:00.587-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertFalse(b.after(a));

        a = new DateTimeStamp(.586);
        b = new DateTimeStamp(.586);
        assertFalse(b.after(a));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertFalse(b.after(a));

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertFalse(b.after(a));
    }

    @Test
    public void compare() {
        DateTimeStamp a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        DateTimeStamp b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
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
    public void add() {
        DateTimeStamp a = new DateTimeStamp(.586);
        double a_ts = a.getTimeStamp();
        ZonedDateTime a_dt = a.getDateTime();
        DateTimeStamp b = new DateTimeStamp(.586 + .587);
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
    public void minus() {
        DateTimeStamp a = new DateTimeStamp(.586);
        double a_ts = a.getTimeStamp();
        ZonedDateTime a_dt = a.getDateTime();
        DateTimeStamp b = new DateTimeStamp(.586 - .587);
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
    public void testMinus() {
        DateTimeStamp a = new DateTimeStamp(.586);
        DateTimeStamp b = new DateTimeStamp(.587);
        double diff = a.minus(b);
        assertEquals(.586 - .587, diff, .001);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        diff = a.minus(b);
        double expected = -0.001d;
        assertEquals(expected, diff, 0.001);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", .18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", .19);
        diff = a.minus(b);
        assertEquals(.18 - .19, diff, .001);

    }

    @Test
    public void timeSpanInMinutes() {
        DateTimeStamp a = new DateTimeStamp(.586);
        DateTimeStamp b = new DateTimeStamp(.587);
        double diff = a.timeSpanInMinutes(b);
        assertEquals((0.0) / 60d, diff, .001);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        diff = a.timeSpanInMinutes(b);
        assertEquals(0d, diff, .001);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", .18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", .19);
        diff = a.timeSpanInMinutes(b);
        assertEquals((.18 - .19) / 60d, diff, .001);
    }

    @Test
    public void testCompareLessThan() {
        DateTimeStamp smaller = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        DateTimeStamp greater = new DateTimeStamp("2018-04-04T10:10:00.587-0100");
        assertEquals(-1, smaller.compareTo(greater));
    }

    @Test
    public void testCompareGreaterThan() {
        DateTimeStamp smaller = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        DateTimeStamp greater = new DateTimeStamp("2018-04-04T10:10:00.587-0100");
        assertEquals(1, greater.compareTo(smaller));
    }

    @Test
    public void testCompareEquals() {
        DateTimeStamp object1 = new DateTimeStamp("2018-04-04T10:10:00.586-0100");
        DateTimeStamp object2 = new DateTimeStamp("2018-04-04T10:10:00.586-0100");
        assertEquals(0, object1.compareTo(object2));
    }

    @Test
    public void testCompareEqualsTimeStamp() {
        DateTimeStamp object1 = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 123);
        DateTimeStamp object2 = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 123);
        assertEquals(0, object2.compareTo(object1));
    }

    @Test
    public void testCompareGreaterTimeStamp() {
        DateTimeStamp smaller = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 122);
        DateTimeStamp greater = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 123);
        assertEquals(1, greater.compareTo(smaller));
    }

    @Test
    public void testCompareSmallerTimeStamp() {
        DateTimeStamp smaller = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 123);
        DateTimeStamp greater = new DateTimeStamp("2018-04-04T10:10:00.586-0100", 124);
        assertEquals(-1, smaller.compareTo(greater));
    }

    @Test
    public void testCompareNullValue() {
        DateTimeStamp smaller = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertEquals(-1, smaller.compareTo(null));
    }

    @Test
    public void testCompareEqualsTimeStampWithoutDateTime() {
        DateTimeStamp object1 = new DateTimeStamp(123);
        DateTimeStamp object2 = new DateTimeStamp(123);
        assertEquals(0, object2.compareTo(object1));
    }

    @Test
    public void testCompareGreaterTimeStampWithoutDateTime() {
        DateTimeStamp smaller = new DateTimeStamp(122);
        DateTimeStamp greater = new DateTimeStamp(124);
        assertEquals(1, greater.compareTo(smaller));
    }

    @Test
    public void testCompareSmallerTimeStampWithoutDateTime() {
        DateTimeStamp smaller = new DateTimeStamp(123);
        DateTimeStamp greater = new DateTimeStamp(124);
        assertEquals(-1, smaller.compareTo(greater));
    }

    @Test
    public void testCompareEqualsTimeStampMixed() {
        DateTimeStamp object1 = new DateTimeStamp(1.522840200586E9);
        DateTimeStamp object2 = new DateTimeStamp("2018-04-04T10:10:00.586-0100");
        assertEquals(0, object2.compareTo(object1));
    }

    @Test
    public void testCompareGreaterTimeStampMixed() {
        DateTimeStamp smaller = new DateTimeStamp(122);
        DateTimeStamp greater = new DateTimeStamp("2018-04-04T10:10:00.586-0100");
        assertEquals(1, greater.compareTo(smaller));
    }

    @Test
    public void testCompareSmallerTimeStampMixed() {
        DateTimeStamp smaller = new DateTimeStamp(123);
        DateTimeStamp greater = new DateTimeStamp("2018-04-04T10:10:00.586-0100");
        assertEquals(-1, smaller.compareTo(greater));
    }

    @Test
    public void testNanWithZero() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp(0.0);
        DateTimeStamp forComparing = new DateTimeStamp(0.0).add(Double.NaN);
        assertEquals(dateTimeStamp, forComparing);
    }

    @Test
    public void testNanWithNonZero() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp("2018-04-04T10:10:00.586-0100");
        DateTimeStamp forComparing = new DateTimeStamp("2018-04-04T10:10:00.586-0100").add(Double.NaN);
        assertEquals(dateTimeStamp, forComparing);
    }

    @Test
    public void testNanWithMinusNonZero() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp("2018-04-04T10:10:00.586-0100");
        DateTimeStamp forComparing = new DateTimeStamp("2018-04-04T10:10:00.586-0100").minus(Double.NaN);
        assertEquals(dateTimeStamp, forComparing);
    }

    @Test
    public void testNanWithMinus() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp("2018-04-04T10:09:59.586-0100");
        DateTimeStamp forComparing = new DateTimeStamp("2018-04-04T10:10:00.586-0100").minus(1);
        assertEquals(dateTimeStamp, forComparing);
    }

    @Test
    public void testNanWithConstructor() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp((String) null,Double.NaN);
        DateTimeStamp forComparing = new DateTimeStamp(0.0);
        assertEquals(dateTimeStamp, forComparing);
    }

    @Test
    public void testBeforeForNAN(){
        DateTimeStamp dateTimeStamp = new DateTimeStamp(-1);
        boolean before = dateTimeStamp.before(Double.NaN);
        assertTrue(before);
    }

    @Test
    public void testAfterForNAN(){
        DateTimeStamp dateTimeStamp = new DateTimeStamp(1);
        boolean after = dateTimeStamp.after(Double.NaN);
        assertTrue(after);
    }

    @Test
    public void compareWithNANValue(){
        DateTimeStamp dateTimeStamp = new DateTimeStamp(12d);
        DateTimeStamp dateTimeStampCompare = new DateTimeStamp(Double.NaN);
        int compare = dateTimeStamp.compareTo(dateTimeStampCompare);
        assertEquals(1,compare);

    }

    @Test
    public void compareWithNullDates() {
        DateTimeStamp stamp1 = new DateTimeStamp((String)null, 100);
        DateTimeStamp stamp2 = new DateTimeStamp((String)null, 200);
        DateTimeStamp stamp3 = new DateTimeStamp((String)null, 100);
        assertTrue(stamp1.compareTo(stamp2) < 0);
        assertTrue(stamp1.compareTo(stamp3) == 0);
        assertTrue(stamp2.compareTo(stamp3) > 0);
    }

    @Test
    public void compareToTransitivity() {
        DateTimeStamp stamp1 = new DateTimeStamp("2021-09-01T11:12:13.111-0100", 100);
        DateTimeStamp stamp2 = new DateTimeStamp((String)null, 200);
        DateTimeStamp stamp3 = new DateTimeStamp("2021-08-31T11:12:13.111-0100", 300);
        assertTrue(stamp1.compareTo(stamp2) < 0);
        assertTrue(stamp2.compareTo(stamp3) < 0);
        // therefore, stamp1.compareTo(stamp3) should be < 0
        assertTrue(stamp1.compareTo(stamp3) < 0);
    }


    @Test
    public void compareToTransitivityWithEqualTimeStamps() {
        DateTimeStamp stamp1 = new DateTimeStamp("2021-09-01T11:12:13.111-0100", 100);
        DateTimeStamp stamp2 = new DateTimeStamp((String)null, 100);
        DateTimeStamp stamp3 = new DateTimeStamp("2021-08-31T11:12:13.111-0100", 100);
        int comp1To2 = stamp1.compareTo(stamp2);
        int comp2To3 = stamp2.compareTo(stamp3);
        assertEquals(comp1To2, comp2To3);
        int comp1To3 = stamp1.compareTo(stamp3);
        assertEquals(comp1To2, comp1To3, "compareTo() is not transitive");
    }
}