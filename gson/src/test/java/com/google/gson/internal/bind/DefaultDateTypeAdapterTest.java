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

package com.google.gson.internal.bind;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.JavaVersion;
import com.google.gson.internal.bind.DefaultDateTypeAdapter.DateType;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Test;

/**
 * A simple unit test for the {@link DefaultDateTypeAdapter} class.
 *
 * @author Joel Leitch
 */
public class DefaultDateTypeAdapterTest {

  @Test
  public void testFormattingInEnUs() {
    assertFormattingAlwaysEmitsUsLocale(Locale.US);
  }

  @Test
  public void testFormattingInFr() {
    assertFormattingAlwaysEmitsUsLocale(Locale.FRANCE);
  }

  private void assertFormattingAlwaysEmitsUsLocale(Locale locale) {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(locale);
    try {
      String afterYearSep = JavaVersion.isJava9OrLater() ? ", " : " ";
      String afterYearLongSep = JavaVersion.isJava9OrLater() ? " at " : " ";
      String utcFull = JavaVersion.isJava9OrLater() ? "Coordinated Universal Time" : "UTC";
      assertFormatted(String.format("Jan 1, 1970%s12:00:00 AM", afterYearSep),
          DateType.DATE.createDefaultsAdapterFactory());
      assertFormatted("1/1/70", DateType.DATE.createAdapterFactory(DateFormat.SHORT));
      assertFormatted("Jan 1, 1970", DateType.DATE.createAdapterFactory(DateFormat.MEDIUM));
      assertFormatted("January 1, 1970", DateType.DATE.createAdapterFactory(DateFormat.LONG));
      assertFormatted(String.format("1/1/70%s12:00 AM", afterYearSep),
          DateType.DATE.createAdapterFactory(DateFormat.SHORT, DateFormat.SHORT));
      assertFormatted(String.format("Jan 1, 1970%s12:00:00 AM", afterYearSep),
          DateType.DATE.createAdapterFactory(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertFormatted(String.format("January 1, 1970%s12:00:00 AM UTC", afterYearLongSep),
          DateType.DATE.createAdapterFactory(DateFormat.LONG, DateFormat.LONG));
      assertFormatted(String.format("Thursday, January 1, 1970%s12:00:00 AM %s", afterYearLongSep, utcFull),
          DateType.DATE.createAdapterFactory(DateFormat.FULL, DateFormat.FULL));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  public void testParsingDatesFormattedWithSystemLocale() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.FRANCE);
    try {
      Date date = new Date(0);
      assertParsed(
          DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(date),
          DateType.DATE.createDefaultsAdapterFactory());
      assertParsed(
          DateFormat.getDateInstance(DateFormat.SHORT).format(date),
          DateType.DATE.createAdapterFactory(DateFormat.SHORT));
      assertParsed(
          DateFormat.getDateInstance(DateFormat.MEDIUM).format(date),
          DateType.DATE.createAdapterFactory(DateFormat.MEDIUM));
      assertParsed(
          DateFormat.getDateInstance(DateFormat.LONG).format(date),
          DateType.DATE.createAdapterFactory(DateFormat.LONG));
      assertParsed(
          DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date),
          DateType.DATE.createAdapterFactory(DateFormat.SHORT, DateFormat.SHORT));
      assertParsed(
          DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(date),
          DateType.DATE.createAdapterFactory(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertParsed(
          DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(date),
          DateType.DATE.createAdapterFactory(DateFormat.LONG, DateFormat.LONG));
      assertParsed(
          DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(date),
          DateType.DATE.createAdapterFactory(DateFormat.FULL, DateFormat.FULL));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  public void testParsingDatesFormattedWithUsLocale() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      assertParsed("Jan 1, 1970 0:00:00 AM", DateType.DATE.createDefaultsAdapterFactory());
      assertParsed("1/1/70", DateType.DATE.createAdapterFactory(DateFormat.SHORT));
      assertParsed("Jan 1, 1970", DateType.DATE.createAdapterFactory(DateFormat.MEDIUM));
      assertParsed("January 1, 1970", DateType.DATE.createAdapterFactory(DateFormat.LONG));
      assertParsed("1/1/70 0:00 AM",
          DateType.DATE.createAdapterFactory(DateFormat.SHORT, DateFormat.SHORT));
      assertParsed("Jan 1, 1970 0:00:00 AM",
          DateType.DATE.createAdapterFactory(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertParsed("January 1, 1970 0:00:00 AM UTC",
          DateType.DATE.createAdapterFactory(DateFormat.LONG, DateFormat.LONG));
      assertParsed("Thursday, January 1, 1970 0:00:00 AM UTC",
          DateType.DATE.createAdapterFactory(DateFormat.FULL, DateFormat.FULL));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  public void testFormatUsesDefaultTimezone() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      String afterYearSep = JavaVersion.isJava9OrLater() ? ", " : " ";
      assertFormatted(String.format("Dec 31, 1969%s4:00:00 PM", afterYearSep),
          DateType.DATE.createDefaultsAdapterFactory());
      assertParsed("Dec 31, 1969 4:00:00 PM", DateType.DATE.createDefaultsAdapterFactory());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  public void testDateDeserializationISO8601() throws Exception {
    TypeAdapterFactory adapterFactory = DateType.DATE.createDefaultsAdapterFactory();
    assertParsed("1970-01-01T00:00:00.000Z", adapterFactory);
    assertParsed("1970-01-01T00:00Z", adapterFactory);
    assertParsed("1970-01-01T00:00:00+00:00", adapterFactory);
    assertParsed("1970-01-01T01:00:00+01:00", adapterFactory);
    assertParsed("1970-01-01T01:00:00+01", adapterFactory);
  }

  @Test
  public void testDateSerialization() {
    int dateStyle = DateFormat.LONG;
    TypeAdapter<Date> dateTypeAdapter = dateAdapter(DateType.DATE.createAdapterFactory(dateStyle));
    DateFormat formatter = DateFormat.getDateInstance(dateStyle, Locale.US);
    Date currentDate = new Date();

    String dateString = dateTypeAdapter.toJson(currentDate);
    assertThat(dateString).isEqualTo(toLiteral(formatter.format(currentDate)));
  }

  @Test
  public void testDatePattern() {
    String pattern = "yyyy-MM-dd";
    TypeAdapter<Date> dateTypeAdapter = dateAdapter(DateType.DATE.createAdapterFactory(pattern));
    DateFormat formatter = new SimpleDateFormat(pattern);
    Date currentDate = new Date();

    String dateString = dateTypeAdapter.toJson(currentDate);
    assertThat(dateString).isEqualTo(toLiteral(formatter.format(currentDate)));
  }

  @Test
  public void testInvalidDatePattern() {
    try {
      DateType.DATE.createAdapterFactory("I am a bad Date pattern....");
      fail("Invalid date pattern should fail.");
    } catch (IllegalArgumentException expected) { }
  }

  @Test
  public void testNullValue() throws Exception {
    TypeAdapter<Date> adapter = dateAdapter(DateType.DATE.createDefaultsAdapterFactory());
    assertThat(adapter.fromJson("null")).isNull();
    assertThat(adapter.toJson(null)).isEqualTo("null");
  }

  @Test
  public void testUnexpectedToken() throws Exception {
    try {
      TypeAdapter<Date> adapter = dateAdapter(DateType.DATE.createDefaultsAdapterFactory());
      adapter.fromJson("{}");
      fail("Unexpected token should fail.");
    } catch (IllegalStateException expected) { }
  }

  private static TypeAdapter<Date> dateAdapter(TypeAdapterFactory adapterFactory) {
    TypeAdapter<Date> adapter = adapterFactory.create(new Gson(), TypeToken.get(Date.class));
    assertThat(adapter).isNotNull();
    return adapter;
  }

  private static void assertFormatted(String formatted, TypeAdapterFactory adapterFactory) {
    TypeAdapter<Date> adapter = dateAdapter(adapterFactory);
    assertThat(adapter.toJson(new Date(0))).isEqualTo(toLiteral(formatted));
  }

  private static void assertParsed(String date, TypeAdapterFactory adapterFactory) throws IOException {
    TypeAdapter<Date> adapter = dateAdapter(adapterFactory);
    assertWithMessage(date).that(adapter.fromJson(toLiteral(date))).isEqualTo(new Date(0));
    assertWithMessage("ISO 8601").that(adapter.fromJson(toLiteral("1970-01-01T00:00:00Z"))).isEqualTo(new Date(0));
  }

  private static String toLiteral(String s) {
    return '"' + s + '"';
  }
}
