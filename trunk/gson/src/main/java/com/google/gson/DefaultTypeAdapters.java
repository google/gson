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

import java.lang.reflect.Type;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.internal.ParameterizedTypeHandlerMap;

/**
 * List of all the default type adapters ({@link JsonSerializer}s, {@link JsonDeserializer}s,
 * and {@link InstanceCreator}s.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class DefaultTypeAdapters {

  private static final DefaultDateTypeAdapter DATE_TYPE_ADAPTER = new DefaultDateTypeAdapter();
  private static final DefaultJavaSqlDateTypeAdapter JAVA_SQL_DATE_TYPE_ADAPTER =
    new DefaultJavaSqlDateTypeAdapter();
  private static final DefaultTimeTypeAdapter TIME_TYPE_ADAPTER =
    new DefaultTimeTypeAdapter();
  private static final DefaultTimestampDeserializer TIMESTAMP_DESERIALIZER =
    new DefaultTimestampDeserializer();

  private static final CharacterTypeAdapter CHARACTER_TYPE_ADAPTER = new CharacterTypeAdapter();
  private static final NumberTypeAdapter NUMBER_TYPE_ADAPTER = new NumberTypeAdapter();

  private static final GregorianCalendarTypeAdapter GREGORIAN_CALENDAR_TYPE_ADAPTER =
      new GregorianCalendarTypeAdapter();

  // The constants DEFAULT_SERIALIZERS, DEFAULT_DESERIALIZERS, and DEFAULT_INSTANCE_CREATORS
  // must be defined after the constants for the type adapters. Otherwise, the type adapter
  // constants will appear as nulls.
  static final ParameterizedTypeHandlerMap<JsonSerializer<?>> DEFAULT_SERIALIZERS =
      createDefaultSerializers();
  static final ParameterizedTypeHandlerMap<JsonDeserializer<?>> DEFAULT_DESERIALIZERS =
      createDefaultDeserializers();
  static final ParameterizedTypeHandlerMap<InstanceCreator<?>> DEFAULT_INSTANCE_CREATORS =
      createDefaultInstanceCreators();

  private static ParameterizedTypeHandlerMap<JsonSerializer<?>> createDefaultSerializers() {
    ParameterizedTypeHandlerMap<JsonSerializer<?>> map =
        new ParameterizedTypeHandlerMap<JsonSerializer<?>>();

    map.register(Date.class, DATE_TYPE_ADAPTER, true);
    map.register(java.sql.Date.class, JAVA_SQL_DATE_TYPE_ADAPTER, true);
    map.register(Timestamp.class, DATE_TYPE_ADAPTER, true);
    map.register(Time.class, TIME_TYPE_ADAPTER, true);
    map.register(Calendar.class, GREGORIAN_CALENDAR_TYPE_ADAPTER, true);
    map.register(GregorianCalendar.class, GREGORIAN_CALENDAR_TYPE_ADAPTER, true);

    // Add primitive serializers
    map.register(char.class, CHARACTER_TYPE_ADAPTER, true);
    map.register(Character.class, CHARACTER_TYPE_ADAPTER, true);
    map.register(Number.class, NUMBER_TYPE_ADAPTER, true);

    map.makeUnmodifiable();
    return map;
  }

  private static ParameterizedTypeHandlerMap<JsonDeserializer<?>> createDefaultDeserializers() {
    ParameterizedTypeHandlerMap<JsonDeserializer<?>> map =
        new ParameterizedTypeHandlerMap<JsonDeserializer<?>>();
    map.register(Date.class, wrapDeserializer(DATE_TYPE_ADAPTER), true);
    map.register(java.sql.Date.class, wrapDeserializer(JAVA_SQL_DATE_TYPE_ADAPTER), true);
    map.register(Timestamp.class, wrapDeserializer(TIMESTAMP_DESERIALIZER), true);
    map.register(Time.class, wrapDeserializer(TIME_TYPE_ADAPTER), true);
    map.register(Calendar.class, GREGORIAN_CALENDAR_TYPE_ADAPTER, true);
    map.register(GregorianCalendar.class, GREGORIAN_CALENDAR_TYPE_ADAPTER, true);

    // Add primitive deserializers
    map.register(char.class, wrapDeserializer(CHARACTER_TYPE_ADAPTER), true);
    map.register(Character.class, wrapDeserializer(CHARACTER_TYPE_ADAPTER), true);
    map.register(Number.class, NUMBER_TYPE_ADAPTER, true);

    map.makeUnmodifiable();
    return map;
  }

  private static ParameterizedTypeHandlerMap<InstanceCreator<?>> createDefaultInstanceCreators() {
    ParameterizedTypeHandlerMap<InstanceCreator<?>> map
        = new ParameterizedTypeHandlerMap<InstanceCreator<?>>();
    map.makeUnmodifiable();
    return map;
  }

  @SuppressWarnings("unchecked")
  private static JsonDeserializer<?> wrapDeserializer(JsonDeserializer<?> deserializer) {
    return new JsonDeserializerExceptionWrapper(deserializer);
  }

  /**
   * This type adapter supports three subclasses of date: Date, Timestamp, and
   * java.sql.Date.
   */
  static final class DefaultDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
    private final DateFormat enUsFormat;
    private final DateFormat localFormat;
    private final DateFormat iso8601Format;

    DefaultDateTypeAdapter() {
      this(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US),
          DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT));
    }

    DefaultDateTypeAdapter(String datePattern) {
      this(new SimpleDateFormat(datePattern, Locale.US), new SimpleDateFormat(datePattern));
    }

    DefaultDateTypeAdapter(int style) {
      this(DateFormat.getDateInstance(style, Locale.US), DateFormat.getDateInstance(style));
    }

    public DefaultDateTypeAdapter(int dateStyle, int timeStyle) {
      this(DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US),
          DateFormat.getDateTimeInstance(dateStyle, timeStyle));
    }

    DefaultDateTypeAdapter(DateFormat enUsFormat, DateFormat localFormat) {
      this.enUsFormat = enUsFormat;
      this.localFormat = localFormat;
      this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
      this.iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // These methods need to be synchronized since JDK DateFormat classes are not thread-safe
    // See issue 162
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
      synchronized (localFormat) {
        String dateFormatAsString = enUsFormat.format(src);
        return new JsonPrimitive(dateFormatAsString);
      }
    }

    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      if (!(json instanceof JsonPrimitive)) {
        throw new JsonParseException("The date should be a string value");
      }
      Date date = deserializeToDate(json);
      if (typeOfT == Date.class) {
        return date;
      } else if (typeOfT == Timestamp.class) {
        return new Timestamp(date.getTime());
      } else if (typeOfT == java.sql.Date.class) {
        return new java.sql.Date(date.getTime());
      } else {
        throw new IllegalArgumentException(getClass() + " cannot deserialize to " + typeOfT);
      }
    }

    private Date deserializeToDate(JsonElement json) {
      synchronized (localFormat) {
        try {
          return localFormat.parse(json.getAsString());
        } catch (ParseException ignored) {
        }
        try {
          return enUsFormat.parse(json.getAsString());
        } catch (ParseException ignored) {
        }
        try {
          return iso8601Format.parse(json.getAsString());
        } catch (ParseException e) {
          throw new JsonSyntaxException(json.getAsString(), e);
        }
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(DefaultDateTypeAdapter.class.getSimpleName());
      sb.append('(').append(localFormat.getClass().getSimpleName()).append(')');
      return sb.toString();
    }
  }

  static final class DefaultJavaSqlDateTypeAdapter implements JsonSerializer<java.sql.Date>,
      JsonDeserializer<java.sql.Date> {
    private final DateFormat format;
    DefaultJavaSqlDateTypeAdapter() {
      this.format = new SimpleDateFormat("MMM d, yyyy");
    }

    public JsonElement serialize(java.sql.Date src, Type typeOfSrc,
        JsonSerializationContext context) {
      synchronized (format) {
        String dateFormatAsString = format.format(src);
        return new JsonPrimitive(dateFormatAsString);
      }
    }

    public java.sql.Date deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      if (!(json instanceof JsonPrimitive)) {
        throw new JsonParseException("The date should be a string value");
      }
      try {
        synchronized (format) {
          Date date = format.parse(json.getAsString());
          return new java.sql.Date(date.getTime());
        }
      } catch (ParseException e) {
        throw new JsonSyntaxException(e);
      }
    }
  }

  static final class DefaultTimestampDeserializer implements JsonDeserializer<Timestamp> {
    public Timestamp deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      Date date = context.deserialize(json, Date.class);
      return new Timestamp(date.getTime());
    }
  }

  static final class DefaultTimeTypeAdapter implements JsonSerializer<Time>, JsonDeserializer<Time> {
    private final DateFormat format;
    DefaultTimeTypeAdapter() {
      this.format = new SimpleDateFormat("hh:mm:ss a");
    }
    public JsonElement serialize(Time src, Type typeOfSrc, JsonSerializationContext context) {
      synchronized (format) {
        String dateFormatAsString = format.format(src);
        return new JsonPrimitive(dateFormatAsString);
      }
    }
    public Time deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      if (!(json instanceof JsonPrimitive)) {
        throw new JsonParseException("The date should be a string value");
      }
      try {
        synchronized (format) {
          Date date = format.parse(json.getAsString());
          return new Time(date.getTime());
        }
      } catch (ParseException e) {
        throw new JsonSyntaxException(e);
      }
    }
  }

  private static final class GregorianCalendarTypeAdapter
      implements JsonSerializer<GregorianCalendar>, JsonDeserializer<GregorianCalendar> {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY_OF_MONTH = "dayOfMonth";
    private static final String HOUR_OF_DAY = "hourOfDay";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";

    public JsonElement serialize(GregorianCalendar src, Type typeOfSrc,
        JsonSerializationContext context) {
      JsonObject obj = new JsonObject();
      obj.addProperty(YEAR, src.get(Calendar.YEAR));
      obj.addProperty(MONTH, src.get(Calendar.MONTH));
      obj.addProperty(DAY_OF_MONTH, src.get(Calendar.DAY_OF_MONTH));
      obj.addProperty(HOUR_OF_DAY, src.get(Calendar.HOUR_OF_DAY));
      obj.addProperty(MINUTE, src.get(Calendar.MINUTE));
      obj.addProperty(SECOND, src.get(Calendar.SECOND));
      return obj;
    }

    public GregorianCalendar deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      JsonObject obj = json.getAsJsonObject();
      int year = obj.get(YEAR).getAsInt();
      int month = obj.get(MONTH).getAsInt();
      int dayOfMonth = obj.get(DAY_OF_MONTH).getAsInt();
      int hourOfDay = obj.get(HOUR_OF_DAY).getAsInt();
      int minute = obj.get(MINUTE).getAsInt();
      int second = obj.get(SECOND).getAsInt();
      return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
    }

    @Override
    public String toString() {
      return GregorianCalendarTypeAdapter.class.getSimpleName();
    }
  }

  private static final class NumberTypeAdapter
      implements JsonSerializer<Number>, JsonDeserializer<Number> {
    public JsonElement serialize(Number src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }

    public Number deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      try {
        return json.getAsNumber();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      } catch (UnsupportedOperationException e) {
        throw new JsonSyntaxException(e);
      } catch (IllegalStateException e) {
        throw new JsonSyntaxException(e);
      }
    }

    @Override
    public String toString() {
      return NumberTypeAdapter.class.getSimpleName();
    }
  }

  private static final class CharacterTypeAdapter
      implements JsonSerializer<Character>, JsonDeserializer<Character> {
    public JsonElement serialize(Character src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }

    public Character deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return json.getAsCharacter();
    }

    @Override
    public String toString() {
      return CharacterTypeAdapter.class.getSimpleName();
    }
  }
}
