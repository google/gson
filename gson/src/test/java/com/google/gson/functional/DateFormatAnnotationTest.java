/*
 * Copyright (C) 2014 Google Inc.
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

package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.DateFormat;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Functional tests for the {@link DateFormat} annotation on fields.
 */
public final class DateFormatAnnotationTest extends TestCase {
  public void testDateFormatShouldBePickedFromAnnotation() {
    Date expectedDate = getDate(2015, 12, 11);
    String expectedJson = "{\"startDate\":\"2015-12-10\",\"endDate\":\"2015/12/14\"}";
    Gson gson = new Gson();

    String json = gson.toJson(new Trip(getDate(2015, 12, 10), getDate(2015, 12, 14)));
    assertEquals(json, expectedJson);

    Trip trip = gson.fromJson("{\"startDate\":\"2015-12-11\",\"endDate\":\"2015/12/11\"}", Trip.class);
    assertEqualsDate(expectedDate, trip.startDate);
    assertEqualsDate(expectedDate, trip.endDate);
  }

  public void testDateFormatShouldBePickedFromAnnotationForSqlDateType() {
    Date expectedDate = getDate(2015, 12, 11);
    Gson gson = new Gson();
    DBTrip trip = gson.fromJson("{\"startDate\":\"2015-12-11\",\"endDate\":\"2015/12/11\"}", DBTrip.class);
    assertEqualsDate(expectedDate, trip.startDate);
    assertEqualsDate(expectedDate, trip.endDate);
  }

  public void testDateFormatAnnotationTakesPrecedenceOverGivenAdapter() {
    Date expectedDate = getDate(2015, 12, 11);
    Gson gson = new Gson();
    Item item = gson.fromJson("{\"date\":\"2015-12-11\"}", Item.class);
    assertEqualsDate(expectedDate, item.date);
  }

  private Date getDate(int year, int month, int date) {
    Calendar instance = Calendar.getInstance();
    instance.set(year, month - 1, date);
    return instance.getTime();
  }

  @SuppressWarnings("deprecation")
  private void assertEqualsDate(Date expectedDate, Date date) {
    assertEquals(expectedDate.getYear(), date.getYear());
    assertEquals(expectedDate.getMonth(), date.getMonth());
    assertEquals(expectedDate.getDate(), date.getDate());
  }


  private static final class Trip {

    @DateFormat("yyyy-MM-dd")
    final Date startDate;

    @DateFormat("yyyy/MM/dd")
    final Date endDate;

    public Trip(Date startDate, Date endDate) {
      this.startDate = startDate;
      this.endDate = endDate;
    }
  }

  private static final class DBTrip {

    @DateFormat("yyyy-MM-dd")
    final java.sql.Date startDate;

    @DateFormat("yyyy/MM/dd")
    final java.sql.Date endDate;

    public DBTrip(java.sql.Date startDate, java.sql.Date endDate) {
      this.startDate = startDate;
      this.endDate = endDate;
    }
  }

  private static final class Item {

    @DateFormat("yyyy-MM-dd")
    @JsonAdapter(DummyDateAdapter.class)
    final Date date;

    public Item(Date date) {
      this.date = date;
    }
  }

  private static class DummyDateAdapter extends TypeAdapter<Date> {
    @Override public void write(JsonWriter out, Date user) throws IOException {
      out.value(new Date(0).toString());
    }
    @Override public Date read(JsonReader in) throws IOException {
      in.nextString();
      return new Date(0);
    }
  }


}
