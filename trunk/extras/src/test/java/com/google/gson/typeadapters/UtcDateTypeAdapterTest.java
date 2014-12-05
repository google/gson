/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.typeadapters;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class UtcDateTypeAdapterTest extends TestCase {
  private final Gson gson = new GsonBuilder()
  .registerTypeAdapter(Date.class, new UtcDateTypeAdapter())
  .create();

  public void testLocalTimeZone() {
    Date expected = new Date();
    String json = gson.toJson(expected);
    Date actual = gson.fromJson(json, Date.class);
    assertEquals(expected.getTime(), actual.getTime());
  }

  public void testDifferentTimeZones() {
    for (String timeZone : TimeZone.getAvailableIDs()) {
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
      Date expected = cal.getTime();
      String json = gson.toJson(expected);
      // System.out.println(json + ": " + timeZone);
      Date actual = gson.fromJson(json, Date.class);
      assertEquals(expected.getTime(), actual.getTime());
    }
  }
}
