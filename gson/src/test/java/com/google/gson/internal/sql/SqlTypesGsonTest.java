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

package com.google.gson.internal.sql;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.functional.DefaultTypeAdaptersTest;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// Suppression for `java.sql.Date` to make it explicit that this is not `java.util.Date`
@SuppressWarnings("UnnecessarilyFullyQualified")
public class SqlTypesGsonTest {
  private Gson gson;
  private TimeZone oldTimeZone;
  private Locale oldLocale;

  @Before
  public void setUp() throws Exception {
    this.oldTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    this.oldLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    gson = new Gson();
  }

  @After
  public void tearDown() throws Exception {
    TimeZone.setDefault(oldTimeZone);
    Locale.setDefault(oldLocale);
  }

  @Test
  public void testNullSerializationAndDeserialization() {
    testNullSerializationAndDeserialization(java.sql.Date.class);
    testNullSerializationAndDeserialization(Time.class);
    testNullSerializationAndDeserialization(Timestamp.class);
  }

  private void testNullSerializationAndDeserialization(Class<?> c) {
    DefaultTypeAdaptersTest.testNullSerializationAndDeserialization(gson, c);
  }

  @Test
  public void testDefaultSqlDateSerialization() {
    java.sql.Date instant = new java.sql.Date(1259875082000L);
    String json = gson.toJson(instant);
    assertThat(json).isEqualTo("\"Dec 3, 2009\"");
  }

  @Test
  public void testDefaultSqlDateDeserialization() {
    String json = "'Dec 3, 2009'";
    java.sql.Date extracted = gson.fromJson(json, java.sql.Date.class);
    DefaultTypeAdaptersTest.assertEqualsDate(extracted, 2009, 11, 3);
  }

  // http://code.google.com/p/google-gson/issues/detail?id=230
  @Test
  public void testSqlDateSerialization() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      java.sql.Date sqlDate = new java.sql.Date(0L);
      Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      String json = gson.toJson(sqlDate, Timestamp.class);
      assertThat(json).isEqualTo("\"1970-01-01\"");
      assertThat(gson.fromJson("\"1970-01-01\"", java.sql.Date.class).getTime()).isEqualTo(0);
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  public void testDefaultSqlTimeSerialization() {
    Time now = new Time(1259875082000L);
    String json = gson.toJson(now);
    assertThat(json).isEqualTo("\"01:18:02 PM\"");
  }

  @Test
  public void testDefaultSqlTimeDeserialization() {
    String json = "'1:18:02 PM'";
    Time extracted = gson.fromJson(json, Time.class);
    DefaultTypeAdaptersTest.assertEqualsTime(extracted, 13, 18, 2);
  }

  @Test
  public void testDefaultSqlTimestampSerialization() {
    Timestamp now = new java.sql.Timestamp(1259875082000L);
    String json = gson.toJson(now);
    // The exact format of the serialized date-time string depends on the JDK version. The pattern
    // here allows for an optional comma after the date, and what might be U+202F (Narrow No-Break
    // Space) before "PM".
    assertThat(json).matches("\"Dec 3, 2009,? 1:18:02\\hPM\"");
  }

  @Test
  public void testDefaultSqlTimestampDeserialization() {
    String json = "'Dec 3, 2009 1:18:02 PM'";
    Timestamp extracted = gson.fromJson(json, Timestamp.class);
    DefaultTypeAdaptersTest.assertEqualsDate(extracted, 2009, 11, 3);
    DefaultTypeAdaptersTest.assertEqualsTime(extracted, 13, 18, 2);
  }

  // http://code.google.com/p/google-gson/issues/detail?id=230
  @Test
  public void testTimestampSerialization() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      Timestamp timestamp = new Timestamp(0L);
      Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      String json = gson.toJson(timestamp, Timestamp.class);
      assertThat(json).isEqualTo("\"1970-01-01\"");
      assertThat(gson.fromJson("\"1970-01-01\"", Timestamp.class).getTime()).isEqualTo(0);
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }
}
