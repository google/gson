/*
 * Copyright (C) 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal.bind.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Test;

@SuppressWarnings("MemberName") // class name
public class ISO8601UtilsTest {

  private static TimeZone utcTimeZone() {
    return TimeZone.getTimeZone("UTC");
  }

  private static GregorianCalendar createUtcCalendar() {
    TimeZone utc = utcTimeZone();
    GregorianCalendar calendar = new GregorianCalendar(utc);
    // Calendar was created with current time, must clear it
    calendar.clear();
    return calendar;
  }

  @Test
  public void testParseDateOnlyDSTGap() throws ParseException {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    try {
      // 1966-11-01 00:00 does not exist in America/Sao_Paulo because clocks were shifted
      // forward from 0:00 to 1:00 at midnight; parsing must not fail for this valid date
      TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
      Date date = ISO8601Utils.parse("1966-11-01", new ParsePosition(0));

      GregorianCalendar calendar =
          new GregorianCalendar(TimeZone.getTimeZone("America/Sao_Paulo"), Locale.US);
      // Calendar was created with current time, must clear it
      calendar.clear();
      calendar.setTime(date);
      assertThat(calendar.get(Calendar.YEAR)).isEqualTo(1966);
      assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.NOVEMBER);
      assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
      // The resolved hour depends on the DST rules of the JDK's bundled timezone data
      // (midnight itself, or the first hour after the 0:00 -> 1:00 transition), so only
      // assert that the date fields are preserved while the hour stays within the day
      assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isAnyOf(0, 1);
    } finally {
      TimeZone.setDefault(defaultTimeZone);
    }
  }

  @Test
  public void testParseInvalidDateOnlyStillFails() {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    try {
      // Strict parsing introduced for date-only values must keep rejecting dates which do
      // not exist, even in time zones where midnight is skipped by a DST transition
      TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
      assertThrows(
          ParseException.class, () -> ISO8601Utils.parse("2021-02-30", new ParsePosition(0)));
      assertThrows(
          ParseException.class, () -> ISO8601Utils.parse("2021-13-01", new ParsePosition(0)));
      assertThrows(
          ParseException.class, () -> ISO8601Utils.parse("1966-00-01", new ParsePosition(0)));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
    }
  }

  @Test
  public void testDateFormatString() {
    GregorianCalendar calendar = new GregorianCalendar(utcTimeZone(), Locale.US);
    // Calendar was created with current time, must clear it
    calendar.clear();
    calendar.set(2018, Calendar.JUNE, 25);
    Date date = calendar.getTime();
    String dateStr = ISO8601Utils.format(date);
    String expectedDate = "2018-06-25T00:00:00Z";
    assertThat(dateStr).isEqualTo(expectedDate);
  }

  @Test
  @SuppressWarnings("JavaUtilDate")
  public void testDateFormatWithMilliseconds() {
    long time = 1530209176870L;
    Date date = new Date(time);
    String dateStr = ISO8601Utils.format(date, true);
    String expectedDate = "2018-06-28T18:06:16.870Z";
    assertThat(dateStr).isEqualTo(expectedDate);
  }

  @Test
  @SuppressWarnings("JavaUtilDate")
  public void testDateFormatWithTimezone() {
    long time = 1530209176870L;
    Date date = new Date(time);
    String dateStr = ISO8601Utils.format(date, true, TimeZone.getTimeZone("Brazil/East"));
    String expectedDate = "2018-06-28T15:06:16.870-03:00";
    assertThat(dateStr).isEqualTo(expectedDate);
  }

  @Test
  @SuppressWarnings("UndefinedEquals")
  public void testDateParseWithDefaultTimezone() throws ParseException {
    String dateStr = "2018-06-25";
    Date date = ISO8601Utils.parse(dateStr, new ParsePosition(0));
    Date expectedDate = new GregorianCalendar(2018, Calendar.JUNE, 25).getTime();
    assertThat(date).isEqualTo(expectedDate);
  }

  @Test
  public void testDateParseInvalidDay() {
    String dateStr = "2022-12-33";
    assertThrows(ParseException.class, () -> ISO8601Utils.parse(dateStr, new ParsePosition(0)));
  }

  @Test
  public void testDateParseInvalidMonth() {
    String dateStr = "2022-14-30";
    assertThrows(ParseException.class, () -> ISO8601Utils.parse(dateStr, new ParsePosition(0)));
  }

  @Test
  @SuppressWarnings("UndefinedEquals")
  public void testDateParseWithTimezone() throws ParseException {
    String dateStr = "2018-06-25T00:00:00-03:00";
    Date date = ISO8601Utils.parse(dateStr, new ParsePosition(0));
    GregorianCalendar calendar = createUtcCalendar();
    calendar.set(2018, Calendar.JUNE, 25, 3, 0);
    Date expectedDate = calendar.getTime();
    assertThat(date).isEqualTo(expectedDate);
  }

  @Test
  @SuppressWarnings("UndefinedEquals")
  public void testDateParseSpecialTimezone() throws ParseException {
    String dateStr = "2018-06-25T00:02:00-02:58";
    Date date = ISO8601Utils.parse(dateStr, new ParsePosition(0));
    GregorianCalendar calendar = createUtcCalendar();
    calendar.set(2018, Calendar.JUNE, 25, 3, 0);
    Date expectedDate = calendar.getTime();
    assertThat(date).isEqualTo(expectedDate);
  }

  @Test
  public void testDateParseInvalidTime() {
    String dateStr = "2018-06-25T61:60:62-03:00";
    assertThrows(ParseException.class, () -> ISO8601Utils.parse(dateStr, new ParsePosition(0)));
  }
}
