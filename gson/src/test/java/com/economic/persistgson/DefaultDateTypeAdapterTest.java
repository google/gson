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

package com.economic.persistgson;

import com.economic.persistgson.JsonPrimitive;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import junit.framework.TestCase;

/**
 * A simple unit test for the {@link com.economic.persistgson.DefaultDateTypeAdapter} class.
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
      assertFormatted("Jan 1, 1970 12:00:00 AM", new com.economic.persistgson.DefaultDateTypeAdapter());
      assertFormatted("1/1/70", new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.SHORT));
      assertFormatted("Jan 1, 1970", new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.MEDIUM));
      assertFormatted("January 1, 1970", new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.LONG));
      assertFormatted("1/1/70 12:00 AM",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT));
      assertFormatted("Jan 1, 1970 12:00:00 AM",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertFormatted("January 1, 1970 12:00:00 AM UTC",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.LONG, DateFormat.LONG));
      assertFormatted("Thursday, January 1, 1970 12:00:00 AM UTC",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.FULL, DateFormat.FULL));
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
      assertParsed("1 janv. 1970 00:00:00", new com.economic.persistgson.DefaultDateTypeAdapter());
      assertParsed("01/01/70", new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.SHORT));
      assertParsed("1 janv. 1970", new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.MEDIUM));
      assertParsed("1 janvier 1970", new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.LONG));
      assertParsed("01/01/70 00:00",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT));
      assertParsed("1 janv. 1970 00:00:00",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertParsed("1 janvier 1970 00:00:00 UTC",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.LONG, DateFormat.LONG));
      assertParsed("jeudi 1 janvier 1970 00 h 00 UTC",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.FULL, DateFormat.FULL));
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
      assertParsed("Jan 1, 1970 0:00:00 AM", new com.economic.persistgson.DefaultDateTypeAdapter());
      assertParsed("1/1/70", new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.SHORT));
      assertParsed("Jan 1, 1970", new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.MEDIUM));
      assertParsed("January 1, 1970", new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.LONG));
      assertParsed("1/1/70 0:00 AM",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT));
      assertParsed("Jan 1, 1970 0:00:00 AM",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertParsed("January 1, 1970 0:00:00 AM UTC",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.LONG, DateFormat.LONG));
      assertParsed("Thursday, January 1, 1970 0:00:00 AM UTC",
          new com.economic.persistgson.DefaultDateTypeAdapter(DateFormat.FULL, DateFormat.FULL));
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
      assertFormatted("Dec 31, 1969 4:00:00 PM", new com.economic.persistgson.DefaultDateTypeAdapter());
      assertParsed("Dec 31, 1969 4:00:00 PM", new com.economic.persistgson.DefaultDateTypeAdapter());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testDateDeserializationISO8601() throws Exception {
  	com.economic.persistgson.DefaultDateTypeAdapter adapter = new com.economic.persistgson.DefaultDateTypeAdapter();
    assertParsed("1970-01-01T00:00:00.000Z", adapter);
    assertParsed("1970-01-01T00:00Z", adapter);
    assertParsed("1970-01-01T00:00:00+00:00", adapter);
    assertParsed("1970-01-01T01:00:00+01:00", adapter);
    assertParsed("1970-01-01T01:00:00+01", adapter);
  }
  
  public void testDateSerialization() throws Exception {
    int dateStyle = DateFormat.LONG;
    com.economic.persistgson.DefaultDateTypeAdapter dateTypeAdapter = new com.economic.persistgson.DefaultDateTypeAdapter(dateStyle);
    DateFormat formatter = DateFormat.getDateInstance(dateStyle, Locale.US);
    Date currentDate = new Date();

    String dateString = dateTypeAdapter.serialize(currentDate, Date.class, null).getAsString();
    assertEquals(formatter.format(currentDate), dateString);
  }

  public void testDatePattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    com.economic.persistgson.DefaultDateTypeAdapter dateTypeAdapter = new com.economic.persistgson.DefaultDateTypeAdapter(pattern);
    DateFormat formatter = new SimpleDateFormat(pattern);
    Date currentDate = new Date();

    String dateString = dateTypeAdapter.serialize(currentDate, Date.class, null).getAsString();
    assertEquals(formatter.format(currentDate), dateString);
  }

  public void testInvalidDatePattern() throws Exception {
    try {
      new com.economic.persistgson.DefaultDateTypeAdapter("I am a bad Date pattern....");
      fail("Invalid date pattern should fail.");
    } catch (IllegalArgumentException expected) { }
  }

  private void assertFormatted(String formatted, com.economic.persistgson.DefaultDateTypeAdapter adapter) {
    assertEquals(formatted, adapter.serialize(new Date(0), Date.class, null).getAsString());
  }

  private void assertParsed(String date, com.economic.persistgson.DefaultDateTypeAdapter adapter) {
    assertEquals(date, new Date(0), adapter.deserialize(new com.economic.persistgson.JsonPrimitive(date), Date.class, null));
    assertEquals("ISO 8601", new Date(0), adapter.deserialize(
        new JsonPrimitive("1970-01-01T00:00:00Z"), Date.class, null));
  }
}
