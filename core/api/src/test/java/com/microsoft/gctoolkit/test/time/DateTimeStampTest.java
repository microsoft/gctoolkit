// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.test.time;

import com.microsoft.gctoolkit.time.DateTimeStamp;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeStampTest {

    // For some reason, ISO_DATE_TIME doesn't like that time-zone is -0100. It wants -01:00.
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Test
    void getTimeStamp() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp(.586);
        assertEquals(.586, dateTimeStamp.getTimeStamp(), 0.0001);

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        double timeStamp = dateTimeStamp.getTimeStamp();
        assertEquals(.586, timeStamp - (long)timeStamp, 0.0001);

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertEquals(.18, dateTimeStamp.getTimeStamp(), 0.0001);
    }

    @Test
    void getDateStampAsString() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp(.586);
        assertEquals("@0.586", dateTimeStamp.toString());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertEquals("2018-04-04T09:10:00.586-01:00@1522836600.586", dateTimeStamp.toString());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertEquals("2018-04-04T09:10:00.586-01:00@0.180", dateTimeStamp.toString());
    }

    @Test
    void getDateTime() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp(.586);
        assertNull(dateTimeStamp.getDateTime());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertEquals(ZonedDateTime.from(formatter.parse("2018-04-04T09:10:00.586-0100")), dateTimeStamp.getDateTime());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertEquals(ZonedDateTime.from(formatter.parse("2018-04-04T09:10:00.586-0100")), dateTimeStamp.getDateTime());
    }

    @Test
    void hasDateStamp() {
        DateTimeStamp dateTimeStamp = new DateTimeStamp(.586);
        assertFalse(dateTimeStamp.hasDateStamp());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        assertTrue(dateTimeStamp.hasDateStamp());

        dateTimeStamp = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertTrue(dateTimeStamp.hasDateStamp());
    }

    @Test
    void testHash() {
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
    void testEquals() {
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
    void testBefore() {
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

        a = new DateTimeStamp(.587);
        b = new DateTimeStamp(.586);

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
    void testAfter() {
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

        a = new DateTimeStamp(.587);
        b = new DateTimeStamp(.586);

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
    void compare() {
        DateTimeStamp a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        DateTimeStamp b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        assertTrue(a.compare(b.getDateTime()) < 0);
        assertTrue(b.compare(a.getDateTime()) > 0);
        assertTrue(a.compare(new DateTimeStamp("2018-04-04T09:10:00.586-0100").getDateTime()) == 0);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", 0.18);
        assertTrue(a.compare(b.getDateTime()) < 0);
        assertTrue(b.compare(a.getDateTime()) > 0);
        assertTrue(a.compare(new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18).getDateTime()) == 0);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0000", 0.18);
        b = new DateTimeStamp("2018-04-04T09:10:00.586-0100", 0.18);
        assertTrue(a.compare(b.getDateTime()) < 0);
        assertTrue(b.compare(a.getDateTime()) > 0);
        assertTrue(a.compare(new DateTimeStamp("2018-04-04T09:10:00.586-0000", 0.18).getDateTime()) == 0);
    }

    @Test
    void add() {
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
    void minus() {
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
    void testMinus() {
        DateTimeStamp a = new DateTimeStamp(.586);
        DateTimeStamp b = new DateTimeStamp(.587);
        double diff = a.minus(b);
        assertEquals(.586-.587, diff, .001);

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
    void timeSpanInMinutes() {
        DateTimeStamp a = new DateTimeStamp(.586);
        DateTimeStamp b = new DateTimeStamp(.587);
        double diff = a.timeSpanInMinutes(b);
        assertEquals((.586-.586)/60d, diff, .001);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100");
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100");
        diff = a.timeSpanInMinutes(b);
        assertEquals(0d, diff, .001);

        a = new DateTimeStamp("2018-04-04T09:10:00.586-0100", .18);
        b = new DateTimeStamp("2018-04-04T09:10:00.587-0100", .19);
        diff = a.timeSpanInMinutes(b);
        assertEquals((.18 - .19)/60d, diff, .001);
    }

}