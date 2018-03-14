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

import com.google.gson.internal.bind.TypeAdapters;
import junit.framework.TestCase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A basic unit test for the {@link TypeAdapters.LocalDateAdapter} class.
 *
 * @author Raihaan Cassim
 */
public class LocalDateTypeAdapterTest extends TestCase {


  public void testDatePattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    TypeAdapters.LocalDateAdapter adapter = new TypeAdapters.LocalDateAdapter();
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    LocalDate currentDate = LocalDate.now();

    String dateString = adapter.toJson(currentDate);
    assertEquals(toLiteral(currentDate.format(dateTimeFormatter)), dateString);
  }

  public void testNullValue() throws Exception {
    TypeAdapters.LocalDateAdapter adapter = new TypeAdapters.LocalDateAdapter();
    assertNull(adapter.fromJson("null"));
    assertEquals("null", adapter.toJson(null));
  }

  public void testUnexpectedToken() throws Exception {
    try {
      TypeAdapters.LocalDateAdapter adapter = new TypeAdapters.LocalDateAdapter();
      adapter.fromJson("{}");
      fail("Unexpected token should fail.");
    } catch (IllegalStateException expected) { }
  }

  public void testDateDeserializationISO8601() throws Exception {
    TypeAdapters.LocalDateAdapter adapter = new TypeAdapters.LocalDateAdapter();
    assertEquals("1970-01-01", LocalDate.ofEpochDay(0), adapter.fromJson(toLiteral("1970-01-01")));
  }

  private static String toLiteral(String s) {
    return '"' + s + '"';
  }
}
