/*
 * Copyright (C) 2011-2016 Google Inc.
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

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public final class DateTimeTypeAdapters {

  public static final class SqlDateTypeAdapter extends DateTypeAdapter<java.sql.Date> {
    public SqlDateTypeAdapter() {
      this("MMM d, yyyy");
    }
    public SqlDateTypeAdapter(String datePattern) {
      super(datePattern);
    }
    public SqlDateTypeAdapter(int style) {
      super(style);
    }
    public SqlDateTypeAdapter(int dateStyle, int timeStyle) {
      super(dateStyle, timeStyle);
    }
    @Override protected java.sql.Date instantiate(Date date) {
      long millis = date.getTime();
      return new java.sql.Date(millis);
    }
  }
  public static final TypeAdapter<java.sql.Date> SQL_DATE = new SqlDateTypeAdapter();

  public static final class TimestampTypeAdapter extends DateTypeAdapter<Timestamp> {
    public TimestampTypeAdapter() {}
    public TimestampTypeAdapter(String datePattern) {
      super(datePattern);
    }
    public TimestampTypeAdapter(int style) {
      super(style);
    }
    public TimestampTypeAdapter(int dateStyle, int timeStyle) {
      super(dateStyle, timeStyle);
    }
    @Override protected Timestamp instantiate(Date date) {
      long millis = date.getTime();
      return new Timestamp(millis);
    }
  }
  public static final TypeAdapter<Timestamp> TIMESTAMP = new TimestampTypeAdapter();

  public static final class TimeTypeAdapter extends DateTypeAdapter<Time> {
    public TimeTypeAdapter() {
      super("hh:mm:ss a");
    }
    @Override protected Time instantiate(Date date) {
      long millis = date.getTime();
      return new Time(millis);
    }
  }
  public static final TypeAdapter<Time> TIME = new TimeTypeAdapter();

  /**
   * Adapter for Date. Although this class appears stateless, it is not.
   * DateFormat captures its time zone and locale when it is created, which gives
   * this class state. DateFormat isn't thread safe either, so this class has
   * to synchronize its read and write methods.
   */
  public static class DateTypeAdapter<D extends Date> extends TypeAdapter<D> {

    final DateFormat enUsFormat;
    final DateFormat localFormat;

    public DateTypeAdapter() {
      this(Locale.US);
    }

    public DateTypeAdapter(Locale locale) {
      this(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale),
          DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT));
    }

    public DateTypeAdapter(String datePattern) {
      this(new SimpleDateFormat(datePattern, Locale.US), new SimpleDateFormat(datePattern));
    }

    public DateTypeAdapter(int style) {
      this(DateFormat.getDateInstance(style, Locale.US), DateFormat.getDateInstance(style));
    }

    public DateTypeAdapter(int dateStyle, int timeStyle) {
      this(DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US),
          DateFormat.getDateTimeInstance(dateStyle, timeStyle));
    }

    public DateTypeAdapter(DateFormat enUsFormat, DateFormat localFormat) {
      this.enUsFormat = enUsFormat;
      this.localFormat = localFormat;
    }

    @Override public D read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      Date date = deserializeToDate(in.nextString());
      return instantiate(date);
    }

    @SuppressWarnings("unchecked")
    protected D instantiate(Date date) {
      return (D) date;
    }

    private synchronized Date deserializeToDate(String json) {
      try {
        return localFormat.parse(json);
      } catch (ParseException ignored) {
      }
      try {
        return enUsFormat.parse(json);
      } catch (ParseException ignored) {
      }
      try {
        return ISO8601Utils.parse(json, new ParsePosition(0));
      } catch (ParseException e) {
        throw new JsonSyntaxException(json, e);
      }
    }

    @Override public synchronized void write(JsonWriter out, Date value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      String dateFormatAsString = enUsFormat.format(value);
      out.value(dateFormatAsString);
    }
  }
  public static final TypeAdapter<Date> DATE = new DateTypeAdapter<Date>();
}
