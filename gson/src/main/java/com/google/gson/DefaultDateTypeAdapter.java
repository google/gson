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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.gson.internal.JavaVersion;
import com.google.gson.internal.PreJava9DateFormatProvider;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * This type adapter supports three subclasses of date: Date, Timestamp, and
 * java.sql.Date.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class DefaultDateTypeAdapter<T extends Date> extends TypeAdapter<T> {
  private static final String SIMPLE_NAME = "DefaultDateTypeAdapter";

  static abstract class DateType<T extends Date> {
    private DateType() {
    }

    public static final DateType<Date> DATE = new DateType<Date>() {
      @Override
      protected Date deserialize(Date date) {
        return date;
      }
    };
    public static final DateType<java.sql.Date> SQL_DATE = new DateType<java.sql.Date>() {
      @Override
      protected java.sql.Date deserialize(Date date) {
        return new java.sql.Date(date.getTime());
      }
    };
    public static final DateType<Timestamp> SQL_TIMESTAMP = new DateType<Timestamp>() {
      @Override
      protected Timestamp deserialize(Date date) {
        return new Timestamp(date.getTime());
      }
    };

    protected abstract T deserialize(Date date);

    public DefaultDateTypeAdapter<T> createAdapter(String datePattern) {
      return new DefaultDateTypeAdapter<T>(this, datePattern);
    }

    public DefaultDateTypeAdapter<T> createAdapter(int style) {
      return new DefaultDateTypeAdapter<T>(this, style);
    }

    public DefaultDateTypeAdapter<T> createAdapter(int dateStyle, int timeStyle) {
      return new DefaultDateTypeAdapter<T>(this, dateStyle, timeStyle);
    }

    public DefaultDateTypeAdapter<T> createDefaultsAdapter() {
      return new DefaultDateTypeAdapter<T>(this, DateFormat.DEFAULT, DateFormat.DEFAULT);
    }
  }

  private final DateType<T> dateType;

  /**
   * List of 1 or more different date formats used for de-serialization attempts.
   * The first of them is used for serialization as well.
   */
  private final List<DateFormat> dateFormats = new ArrayList<DateFormat>();

  private DefaultDateTypeAdapter(DateType<T> dateType, String datePattern) {
    this.dateType = verifyDateType(dateType);
    dateFormats.add(new SimpleDateFormat(datePattern, Locale.US));
    if (!Locale.getDefault().equals(Locale.US)) {
      dateFormats.add(new SimpleDateFormat(datePattern));
    }
  }

  private DefaultDateTypeAdapter(DateType<T> dateType, int style) {
    this.dateType = verifyDateType(dateType);
    dateFormats.add(DateFormat.getDateInstance(style, Locale.US));
    if (!Locale.getDefault().equals(Locale.US)) {
      dateFormats.add(DateFormat.getDateInstance(style));
    }
    if (JavaVersion.isJava9OrLater()) {
      dateFormats.add(PreJava9DateFormatProvider.getUSDateFormat(style));
    }
  }

  private DefaultDateTypeAdapter(DateType<T> dateType, int dateStyle, int timeStyle) {
    this.dateType = verifyDateType(dateType);
    dateFormats.add(DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US));
    if (!Locale.getDefault().equals(Locale.US)) {
      dateFormats.add(DateFormat.getDateTimeInstance(dateStyle, timeStyle));
    }
    if (JavaVersion.isJava9OrLater()) {
      dateFormats.add(PreJava9DateFormatProvider.getUSDateTimeFormat(dateStyle, timeStyle));
    }
  }

  private static <T extends Date> DateType<T> verifyDateType(DateType<T> dateType) {
    if (dateType == null) {
      throw new NullPointerException("dateType == null");
    }
    return dateType;
  }

  // These methods need to be synchronized since JDK DateFormat classes are not thread-safe
  // See issue 162
  @Override
  public void write(JsonWriter out, Date value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    synchronized(dateFormats) {
      String dateFormatAsString = dateFormats.get(0).format(value);
      out.value(dateFormatAsString);
    }
  }

  @Override
  public T read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    Date date = deserializeToDate(in.nextString());
    return dateType.deserialize(date);
  }

  private Date deserializeToDate(String s) {
    synchronized (dateFormats) {
      for (DateFormat dateFormat : dateFormats) {
        try {
          return dateFormat.parse(s);
        } catch (ParseException ignored) {}
      }
      try {
        return ISO8601Utils.parse(s, new ParsePosition(0));
      } catch (ParseException e) {
        throw new JsonSyntaxException(s, e);
      }
    }
  }

  @Override
  public String toString() {
    DateFormat defaultFormat = dateFormats.get(0);
    if (defaultFormat instanceof SimpleDateFormat) {
      return SIMPLE_NAME + '(' + ((SimpleDateFormat) defaultFormat).toPattern() + ')';
    } else {
      return SIMPLE_NAME + '(' + defaultFormat.getClass().getSimpleName() + ')';
    }
  }
}
