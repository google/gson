/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import junit.framework.TestCase;

/**
 * A simple unit test for the {@link DefaultDateTypeAdapter} class.
 *
 * @author Joel Leitch
 */
public class DefaultDateTypeAdapterTest extends TestCase {

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
      assertFormatted("Jan 1, 1970 12:00:00 AM", new DefaultDateTypeAdapter(Date.class));
      assertFormatted("1/1/70", new DefaultDateTypeAdapter(Date.class, DateFormat.SHORT));
      assertFormatted("Jan 1, 1970", new DefaultDateTypeAdapter(Date.class, DateFormat.MEDIUM));
      assertFormatted("January 1, 1970", new DefaultDateTypeAdapter(Date.class, DateFormat.LONG));
      assertFormatted("1/1/70 12:00 AM",
          new DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT));
      assertFormatted("Jan 1, 1970 12:00:00 AM",
          new DefaultDateTypeAdapter(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertFormatted("January 1, 1970 12:00:00 AM UTC",
          new DefaultDateTypeAdapter(DateFormat.LONG, DateFormat.LONG));
      assertFormatted("Thursday, January 1, 1970 12:00:00 AM UTC",
          new DefaultDateTypeAdapter(DateFormat.FULL, DateFormat.FULL));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testParsingDatesFormattedWithSystemLocale() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.FRANCE);
    try {
      assertParsed("1 janv. 1970 00:00:00", new DefaultDateTypeAdapter(Date.class));
      assertParsed("01/01/70", new DefaultDateTypeAdapter(Date.class, DateFormat.SHORT));
      assertParsed("1 janv. 1970", new DefaultDateTypeAdapter(Date.class, DateFormat.MEDIUM));
      assertParsed("1 janvier 1970", new DefaultDateTypeAdapter(Date.class, DateFormat.LONG));
      assertParsed("01/01/70 00:00",
          new DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT));
      assertParsed("1 janv. 1970 00:00:00",
          new DefaultDateTypeAdapter(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertParsed("1 janvier 1970 00:00:00 UTC",
          new DefaultDateTypeAdapter(DateFormat.LONG, DateFormat.LONG));
      assertParsed("jeudi 1 janvier 1970 00 h 00 UTC",
          new DefaultDateTypeAdapter(DateFormat.FULL, DateFormat.FULL));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testParsingDatesFormattedWithUsLocale() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      assertParsed("Jan 1, 1970 0:00:00 AM", new DefaultDateTypeAdapter(Date.class));
      assertParsed("1/1/70", new DefaultDateTypeAdapter(Date.class, DateFormat.SHORT));
      assertParsed("Jan 1, 1970", new DefaultDateTypeAdapter(Date.class, DateFormat.MEDIUM));
      assertParsed("January 1, 1970", new DefaultDateTypeAdapter(Date.class, DateFormat.LONG));
      assertParsed("1/1/70 0:00 AM",
          new DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT));
      assertParsed("Jan 1, 1970 0:00:00 AM",
          new DefaultDateTypeAdapter(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertParsed("January 1, 1970 0:00:00 AM UTC",
          new DefaultDateTypeAdapter(DateFormat.LONG, DateFormat.LONG));
      assertParsed("Thursday, January 1, 1970 0:00:00 AM UTC",
          new DefaultDateTypeAdapter(DateFormat.FULL, DateFormat.FULL));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testFormatUsesDefaultTimezone() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      assertFormatted("Dec 31, 1969 4:00:00 PM", new DefaultDateTypeAdapter(Date.class));
      assertParsed("Dec 31, 1969 4:00:00 PM", new DefaultDateTypeAdapter(Date.class));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testDateDeserializationISO8601() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    assertParsed("1970-01-01T00:00:00.000Z", adapter);
    assertParsed("1970-01-01T00:00Z", adapter);
    assertParsed("1970-01-01T00:00:00+00:00", adapter);
    assertParsed("1970-01-01T01:00:00+01:00", adapter);
    assertParsed("1970-01-01T01:00:00+01", adapter);
  }
  
  public void testDateSerialization() throws Exception {
    int dateStyle = DateFormat.LONG;
    DefaultDateTypeAdapter dateTypeAdapter = new DefaultDateTypeAdapter(Date.class, dateStyle);
    DateFormat formatter = DateFormat.getDateInstance(dateStyle, Locale.US);
    Date currentDate = new Date();

    String dateString = dateTypeAdapter.toJson(currentDate);
    assertEquals(toLiteral(formatter.format(currentDate)), dateString);
  }

  public void testDatePattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    DefaultDateTypeAdapter dateTypeAdapter = new DefaultDateTypeAdapter(Date.class, pattern);
    DateFormat formatter = new SimpleDateFormat(pattern);
    Date currentDate = new Date();

    String dateString = dateTypeAdapter.toJson(currentDate);
    assertEquals(toLiteral(formatter.format(currentDate)), dateString);
  }

  public void testInvalidDatePattern() throws Exception {
    try {
      new DefaultDateTypeAdapter(Date.class, "I am a bad Date pattern....");
      fail("Invalid date pattern should fail.");
    } catch (IllegalArgumentException expected) { }
  }

  private void assertFormatted(String formatted, DefaultDateTypeAdapter adapter) {
    assertEquals(toLiteral(formatted), adapter.toJson(new Date(0)));
  }

  private void assertParsed(String date, DefaultDateTypeAdapter adapter) throws IOException {
    assertEquals(date, new Date(0), adapter.fromJson(toLiteral(date)));
    assertEquals("ISO 8601", new Date(0), adapter.fromJson(toLiteral("1970-01-01T00:00:00Z")));
  }

  private static String toLiteral(String s) {
    return '"' + s + '"';
  }
}
