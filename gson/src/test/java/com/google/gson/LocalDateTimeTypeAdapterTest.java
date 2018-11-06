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

import com.google.gson.internal.bind.LocalDateTimeTypeAdapter;
import junit.framework.TestCase;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * A basic unit test for the {@link com.google.gson.internal.bind.LocalDateTimeTypeAdapter} class.
 *
 * @author Raihaan Cassim
 */
public class LocalDateTimeTypeAdapterTest extends TestCase {


  public void testDatePattern() {
    LocalDateTimeTypeAdapter adapter = new LocalDateTimeTypeAdapter();
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    LocalDateTime currentDate = LocalDateTime.now();

    String dateString = adapter.toJson(currentDate);
    assertEquals(toLiteral(currentDate.format(dateTimeFormatter)), dateString);
  }

  public void testNullValue() throws Exception {
    LocalDateTimeTypeAdapter adapter = new LocalDateTimeTypeAdapter();
    assertNull(adapter.fromJson("null"));
    assertEquals("null", adapter.toJson(null));
  }

  public void testUnexpectedToken() throws Exception {
    try {
      LocalDateTimeTypeAdapter adapter = new LocalDateTimeTypeAdapter();
      adapter.fromJson("{}");
      fail("Unexpected token should fail.");
    } catch (IllegalStateException expected) { }
  }

  public void testDateDeserializationISO8601() throws Exception {
    LocalDateTimeTypeAdapter adapter = new LocalDateTimeTypeAdapter();
    assertEquals("1969-12-31T06:00", LocalDateTime.ofEpochSecond(0,0, ZoneOffset.MIN),
            adapter.fromJson(toLiteral("1969-12-31T06:00:00")));
    assertEquals("1970-01-01T00:00:00", LocalDateTime.ofEpochSecond(0,0, ZoneOffset.UTC),
            adapter.fromJson(toLiteral("1970-01-01T00:00:00")));
    assertEquals("1970-01-01T18:00", LocalDateTime.ofEpochSecond(0,0, ZoneOffset.MAX),
            adapter.fromJson(toLiteral("1970-01-01T18:00:00")));
  }
  
  private static String toLiteral(String s) {
    return '"' + s + '"';
  }
}
