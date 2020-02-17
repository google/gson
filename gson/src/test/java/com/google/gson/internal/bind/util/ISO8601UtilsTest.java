package com.google.gson.internal.bind.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class ISO8601UtilsTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testDateFormatString() {
        Date date = new GregorianCalendar(2018, Calendar.JUNE, 25).getTime();
        String dateStr = ISO8601Utils.format(date);
        String expectedDate = "2018-06-25";
        assertEquals(expectedDate, dateStr.substring(0, expectedDate.length()));
    }

    @Test
    public void testDateFormatWithMilliseconds() {
        long time = 1530209176870L;
        Date date = new Date(time);
        String dateStr = ISO8601Utils.format(date, true);
        String expectedDate = "2018-06-28T18:06:16.870Z";
        assertEquals(expectedDate, dateStr);
    }

    @Test
    public void testDateFormatWithTimezone() {
        long time = 1530209176870L;
        Date date = new Date(time);
        String dateStr = ISO8601Utils.format(date, true, TimeZone.getTimeZone("Brazil/East"));
        String expectedDate = "2018-06-28T15:06:16.870-03:00";
        assertEquals(expectedDate, dateStr);
    }

    @Test
    public void testDateParseWithDefaultTimezone() throws ParseException {
        String dateStr = "2018-06-25";
        Date date = ISO8601Utils.parse(dateStr, new ParsePosition(0));
        Date expectedDate = new GregorianCalendar(2018, Calendar.JUNE, 25).getTime();
        assertEquals(expectedDate, date);
    }

    @Test
    public void testDateParseWithTimezone() throws ParseException {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        try {
            String dateStr = "2018-06-25T00:00:00-03:00";
            Date date = ISO8601Utils.parse(dateStr, new ParsePosition(0));
            Date expectedDate = new GregorianCalendar(2018, Calendar.JUNE, 25, 3, 0).getTime();
            assertEquals(expectedDate, date);
        } finally {
            TimeZone.setDefault(defaultTimeZone);
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    public void testDateParseSpecialTimezone() throws ParseException {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        try {
            String dateStr = "2018-06-25T00:02:00-02:58";
            Date date = ISO8601Utils.parse(dateStr, new ParsePosition(0));
            Date expectedDate = new GregorianCalendar(2018, Calendar.JUNE, 25, 3, 0).getTime();
            assertEquals(expectedDate, date);
        } finally {
            TimeZone.setDefault(defaultTimeZone);
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    public void testDateParseInvalidTime() throws ParseException {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        try {
            String dateStr = "2018-06-25T61:60:62-03:00";
            exception.expect(ParseException.class);
            ISO8601Utils.parse(dateStr, new ParsePosition(0));
        } finally {
            TimeZone.setDefault(defaultTimeZone);
            Locale.setDefault(defaultLocale);
        }
    }
}
