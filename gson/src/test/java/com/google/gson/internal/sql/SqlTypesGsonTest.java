package com.google.gson.internal.sql;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.functional.DefaultTypeAdaptersTest;
import com.google.gson.internal.JavaVersion;

import junit.framework.TestCase;

public class SqlTypesGsonTest extends TestCase {
  private Gson gson;
  private TimeZone oldTimeZone;
  private Locale oldLocale;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.oldTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    this.oldLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    gson = new Gson();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    TimeZone.setDefault(oldTimeZone);
    Locale.setDefault(oldLocale);
  }

  public void testNullSerializationAndDeserialization() {
    testNullSerializationAndDeserialization(Date.class);
    testNullSerializationAndDeserialization(Time.class);
    testNullSerializationAndDeserialization(Timestamp.class);
  }

  private void testNullSerializationAndDeserialization(Class<?> c) {
    DefaultTypeAdaptersTest.testNullSerializationAndDeserialization(gson, c);
  }

  public void testDefaultSqlDateSerialization() {
    java.sql.Date instant = new java.sql.Date(1259875082000L);
    String json = gson.toJson(instant);
    assertEquals("\"Dec 3, 2009\"", json);
  }

  public void testDefaultSqlDateDeserialization() {
    String json = "'Dec 3, 2009'";
    java.sql.Date extracted = gson.fromJson(json, java.sql.Date.class);
    DefaultTypeAdaptersTest.assertEqualsDate(extracted, 2009, 11, 3);
  }

  // http://code.google.com/p/google-gson/issues/detail?id=230
  public void testSqlDateSerialization() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      java.sql.Date sqlDate = new java.sql.Date(0L);
      Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      String json = gson.toJson(sqlDate, Timestamp.class);
      assertEquals("\"1970-01-01\"", json);
      assertEquals(0, gson.fromJson("\"1970-01-01\"", java.sql.Date.class).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  public void testDefaultSqlTimeSerialization() {
    Time now = new Time(1259875082000L);
    String json = gson.toJson(now);
    assertEquals("\"01:18:02 PM\"", json);
  }

  public void testDefaultSqlTimeDeserialization() {
    String json = "'1:18:02 PM'";
    Time extracted = gson.fromJson(json, Time.class);
    DefaultTypeAdaptersTest.assertEqualsTime(extracted, 13, 18, 2);
  }

  public void testDefaultSqlTimestampSerialization() {
    Timestamp now = new java.sql.Timestamp(1259875082000L);
    String json = gson.toJson(now);
    if (JavaVersion.isJava9OrLater()) {
      assertEquals("\"Dec 3, 2009, 1:18:02 PM\"", json);
    } else {
      assertEquals("\"Dec 3, 2009 1:18:02 PM\"", json);
    }
  }

  public void testDefaultSqlTimestampDeserialization() {
    String json = "'Dec 3, 2009 1:18:02 PM'";
    Timestamp extracted = gson.fromJson(json, Timestamp.class);
    DefaultTypeAdaptersTest.assertEqualsDate(extracted, 2009, 11, 3);
    DefaultTypeAdaptersTest.assertEqualsTime(extracted, 13, 18, 2);
  }

  // http://code.google.com/p/google-gson/issues/detail?id=230
  public void testTimestampSerialization() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      Timestamp timestamp = new Timestamp(0L);
      Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      String json = gson.toJson(timestamp, Timestamp.class);
      assertEquals("\"1970-01-01\"", json);
      assertEquals(0, gson.fromJson("\"1970-01-01\"", Timestamp.class).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }
}
