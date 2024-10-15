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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Test;

@SuppressWarnings("MemberName") // class name
public class ISO8601UtilsTest {

  private static ZoneId utcZoneId() {
    return ZoneId.of("UTC");
  }

  @Test
  public void testDateFormatString() {
    ZonedDateTime zdt = ZonedDateTime.of(2018, 6, 25, 0, 0, 0, 0, utcZoneId());
    Instant instant = zdt.toInstant();
    String dateStr = ISO8601Utils.format(instant);
    String expectedDate = "2018-06-25T00:00:00Z";
    assertThat(dateStr).isEqualTo(expectedDate);
  }

  @Test
  public void testDateFormatWithMilliseconds() {
    long time = 1530209176870L;
    Instant instant = Instant.ofEpochMilli(time);
    String dateStr = ISO8601Utils.format(instant, true);
    String expectedDate = "2018-06-28T18:06:16.870Z";
    assertThat(dateStr).isEqualTo(expectedDate);
  }

  @Test
  public void testDateFormatWithTimezone() {
    long time = 1530209176870L;
    Instant instant = Instant.ofEpochMilli(time);
    String dateStr = ISO8601Utils.format(instant, true, ZoneId.of("Brazil/East"));
    String expectedDate = "2018-06-28T15:06:16.870-03:00";
    assertThat(dateStr).isEqualTo(expectedDate);
  }

  @Test
  public void testDateParseWithDefaultTimezone() throws ParseException {
    String dateStr = "2018-06-25";
    Instant instant = ISO8601Utils.parse(dateStr, new ParsePosition(0));
    Instant expectedInstant =
        LocalDate.of(2018, 6, 25).atStartOfDay(ZoneId.systemDefault()).toInstant();
    assertThat(instant).isEqualTo(expectedInstant);
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
  public void testDateParseWithTimezone() throws ParseException {
    String dateStr = "2018-06-25T00:00:00-03:00";
    Instant instant = ISO8601Utils.parse(dateStr, new ParsePosition(0));
    ZonedDateTime zdt = ZonedDateTime.of(2018, 6, 25, 3, 0, 0, 0, utcZoneId());
    Instant expectedInstant = zdt.toInstant();
    assertThat(instant).isEqualTo(expectedInstant);
  }

  @Test
  public void testDateParseSpecialTimezone() throws ParseException {
    String dateStr = "2018-06-25T00:02:00-02:58";
    Instant instant = ISO8601Utils.parse(dateStr, new ParsePosition(0));
    ZonedDateTime zdt = ZonedDateTime.of(2018, 6, 25, 3, 0, 0, 0, utcZoneId());
    Instant expectedInstant = zdt.toInstant();
    assertThat(instant).isEqualTo(expectedInstant);
  }

  @Test
  public void testDateParseInvalidTime() {
    String dateStr = "2018-06-25T61:60:62-03:00";
    assertThrows(ParseException.class, () -> ISO8601Utils.parse(dateStr, new ParsePosition(0)));
  }
}
