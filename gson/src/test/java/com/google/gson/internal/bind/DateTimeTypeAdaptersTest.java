/*
 * Copyright (C) 2008-2016 Google Inc.
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

package com.google.gson.internal.bind;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.DateTimeTypeAdapters.DateTypeAdapter;

import junit.framework.TestCase;

/**
 * A simple unit test for the {@link DefaultDateTypeAdapter} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class DateTimeTypeAdaptersTest extends TestCase {
  private static final Gson gson = new Gson();

  public void testFormattingInEnUs() {
    assertFormattingAlwaysEmitsUsLocale(Locale.US);
  }

  public void testFormattingInFr() {
    assertFormattingAlwaysEmitsUsLocale(Locale.FRANCE);
  }

  private void assertFormattingAlwaysEmitsUsLocale(Locale locale) {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(locale);
    try {
      assertFormatted("Jan 1, 1970 12:00:00 AM", new DateTypeAdapter<Date>());
      assertFormatted("1/1/70", new DateTypeAdapter<Date>(DateFormat.SHORT));
      assertFormatted("Jan 1, 1970", new DateTypeAdapter<Date>(DateFormat.MEDIUM));
      assertFormatted("January 1, 1970", new DateTypeAdapter<Date>(DateFormat.LONG));
      assertFormatted("1/1/70 12:00 AM",
          new DateTypeAdapter<Date>(DateFormat.SHORT, DateFormat.SHORT));
      assertFormatted("Jan 1, 1970 12:00:00 AM",
          new DateTypeAdapter<Date>(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertFormatted("January 1, 1970 12:00:00 AM UTC",
          new DateTypeAdapter<Date>(DateFormat.LONG, DateFormat.LONG));
      assertFormatted("Thursday, January 1, 1970 12:00:00 AM UTC",
          new DateTypeAdapter<Date>(DateFormat.FULL, DateFormat.FULL));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testParsingDatesFormattedWithSystemLocale() {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.FRANCE);
    try {
      assertParsed("1 janv. 1970 00:00:00", new DateTypeAdapter<Date>());
      assertParsed("01/01/70", new DateTypeAdapter<Date>(DateFormat.SHORT));
      assertParsed("1 janv. 1970", new DateTypeAdapter<Date>(DateFormat.MEDIUM));
      assertParsed("1 janvier 1970", new DateTypeAdapter<Date>(DateFormat.LONG));
      assertParsed("01/01/70 00:00",
          new DateTypeAdapter<Date>(DateFormat.SHORT, DateFormat.SHORT));
      assertParsed("1 janv. 1970 00:00:00",
          new DateTypeAdapter<Date>(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertParsed("1 janvier 1970 00:00:00 UTC",
          new DateTypeAdapter<Date>(DateFormat.LONG, DateFormat.LONG));
      assertParsed("jeudi 1 janvier 1970 00 h 00 UTC",
          new DateTypeAdapter<Date>(DateFormat.FULL, DateFormat.FULL));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testParsingDatesFormattedWithUsLocale() {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      assertParsed("Jan 1, 1970 0:00:00 AM", new DateTypeAdapter<Date>());
      assertParsed("1/1/70", new DateTypeAdapter<Date>(DateFormat.SHORT));
      assertParsed("Jan 1, 1970", new DateTypeAdapter<Date>(DateFormat.MEDIUM));
      assertParsed("January 1, 1970", new DateTypeAdapter<Date>(DateFormat.LONG));
      assertParsed("1/1/70 0:00 AM",
          new DateTypeAdapter<Date>(DateFormat.SHORT, DateFormat.SHORT));
      assertParsed("Jan 1, 1970 0:00:00 AM",
          new DateTypeAdapter<Date>(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertParsed("January 1, 1970 0:00:00 AM UTC",
          new DateTypeAdapter<Date>(DateFormat.LONG, DateFormat.LONG));
      assertParsed("Thursday, January 1, 1970 0:00:00 AM UTC",
          new DateTypeAdapter<Date>(DateFormat.FULL, DateFormat.FULL));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testFormatUsesDefaultTimezone() {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      assertFormatted("Dec 31, 1969 4:00:00 PM", new DateTypeAdapter<Date>());
      assertParsed("Dec 31, 1969 4:00:00 PM", new DateTypeAdapter<Date>());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testDateDeserializationISO8601() throws Exception {
    TypeAdapter<Date> adapter = new DateTypeAdapter<Date>();
    assertParsed("1970-01-01T00:00:00.000Z", adapter);
    assertParsed("1970-01-01T00:00Z", adapter);
    assertParsed("1970-01-01T00:00:00+00:00", adapter);
    assertParsed("1970-01-01T01:00:00+01:00", adapter);
    assertParsed("1970-01-01T01:00:00+01", adapter);
  }

  public void testDateSerialization() throws Exception {
    int dateStyle = DateFormat.LONG;
    DateTypeAdapter<Date> dateTypeAdapter = new DateTypeAdapter<Date>(dateStyle);
    DateFormat formatter = DateFormat.getDateInstance(dateStyle, Locale.US);
    Date currentDate = new Date();

    String dateString = stringify(dateTypeAdapter.toJsonTree(currentDate));
    assertEquals(formatter.format(currentDate), dateString);
  }

  public void testDatePattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    DateTypeAdapter<Date> dateTypeAdapter = new DateTypeAdapter<Date>(pattern);
    DateFormat formatter = new SimpleDateFormat(pattern);
    Date currentDate = new Date();

    String dateString = stringify(dateTypeAdapter.toJsonTree(currentDate));
    assertEquals(formatter.format(currentDate), dateString);
  }

  public void testInvalidDatePattern() throws Exception {
    try {
      new DateTypeAdapter<Date>("I am a bad Date pattern....");
      fail("Invalid date pattern should fail.");
    } catch (IllegalArgumentException expected) { }
  }

  private void assertFormatted(String formatted, TypeAdapter<Date> adapter) {
    assertEquals(formatted, stringify(adapter.toJsonTree(new Date(0))));
  }

  private void assertParsed(String date, TypeAdapter<Date> adapter) {
    assertEquals(date, new Date(0), adapter.fromJsonTree(new JsonPrimitive(date)));
    assertEquals("ISO 8601", new Date(0), adapter.fromJsonTree(
        new JsonPrimitive("1970-01-01T00:00:00Z")));
  }

  private static String stringify(JsonElement root) {
    String json = gson.toJson(root);
    // remove quotes around for easier comparison
    return json.substring(1, json.length() - 1);
  }
}
