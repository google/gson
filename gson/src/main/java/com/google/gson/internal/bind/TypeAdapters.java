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

package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Type adapters for basic types.
 */
public final class TypeAdapters {
  private TypeAdapters() {}

  public static final TypeAdapter<BitSet> BIT_SET = new TypeAdapter<BitSet>() {
    public BitSet read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }

      BitSet bitset = new BitSet();
      reader.beginArray();
      int i = 0;
      JsonToken tokenType = reader.peek();
      while (tokenType != JsonToken.END_ARRAY) {
        boolean set;
        switch (tokenType) {
        case NUMBER:
          set = reader.nextInt() != 0;
          break;
        case BOOLEAN:
          set = reader.nextBoolean();
          break;
        case STRING:
          String stringValue = reader.nextString();
          try {
            set = Integer.parseInt(stringValue) != 0;
          } catch (NumberFormatException e) {
            throw new JsonSyntaxException(
                "Error: Expecting: bitset number value (1, 0), Found: " + stringValue);
          }
          break;
        default:
          throw new JsonSyntaxException("Invalid bitset value type: " + tokenType);
        }
        if (set) {
          bitset.set(i);
        }
        ++i;
        tokenType = reader.peek();
      }
      reader.endArray();
      return bitset;
    }

    public void write(JsonWriter writer, BitSet src) throws IOException {
      if (src == null) {
        writer.nullValue();
        return;
      }

      writer.beginArray();
      for (int i = 0; i < src.length(); i++) {
        int value = (src.get(i)) ? 1 : 0;
        writer.value(value);
      }
      writer.endArray();
    }
  };

  public static final TypeAdapter.Factory BIT_SET_FACTORY = newFactory(BitSet.class, BIT_SET);

  public static final TypeAdapter<Boolean> BOOLEAN = new TypeAdapter<Boolean>() {
    @Override
    public Boolean read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return reader.nextBoolean();
    }
    @Override
    public void write(JsonWriter writer, Boolean value) throws IOException {
      if (value == null) {
        writer.nullValue();
        return;
      }
      writer.value(value);
    }
  };

  /**
   * Writes a boolean as a string. Useful for map keys, where booleans aren't
   * otherwise permitted.
   */
  public static final TypeAdapter<Boolean> BOOLEAN_AS_STRING = new TypeAdapter<Boolean>() {
    @Override public Boolean read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return Boolean.valueOf(reader.nextString());
    }

    @Override public void write(JsonWriter writer, Boolean value) throws IOException {
      writer.value(value == null ? "null" : value.toString());
    }
  };

  public static final TypeAdapter.Factory BOOLEAN_FACTORY
      = newFactory(boolean.class, Boolean.class, BOOLEAN);

  public static final TypeAdapter<Number> BYTE = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      try {
        int intValue = reader.nextInt();
        return (byte) intValue;
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    @Override
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory BYTE_FACTORY
      = newFactory(byte.class, Byte.class, BYTE);

  public static final TypeAdapter<Number> SHORT = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      try {
        return (short) reader.nextInt();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    @Override
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory SHORT_FACTORY
      = newFactory(short.class, Short.class, SHORT);

  public static final TypeAdapter<Number> INTEGER = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      try {
        return reader.nextInt();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    @Override
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory INTEGER_FACTORY
      = newFactory(int.class, Integer.class, INTEGER);

  public static final TypeAdapter<Number> LONG = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      try {
        return reader.nextLong();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    @Override
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory LONG_FACTORY
      = newFactory(long.class, Long.class, LONG);

  public static final TypeAdapter<Number> FLOAT = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return (float) reader.nextDouble();
    }
    @Override
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory FLOAT_FACTORY
      = newFactory(float.class, Float.class, FLOAT);

  public static final TypeAdapter<Number> DOUBLE = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return reader.nextDouble();
    }
    @Override
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory DOUBLE_FACTORY
      = newFactory(double.class, Double.class, DOUBLE);

  public static final TypeAdapter<Number> NUMBER = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader reader) throws IOException {
      JsonToken jsonToken = reader.peek();
      switch (jsonToken) {
      case NULL:
        reader.nextNull();
        return null;
      case NUMBER:
        return new LazilyParsedNumber(reader.nextString());
      default:
        throw new JsonSyntaxException("Expecting number, got: " + jsonToken);
      }
    }
    @Override
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory NUMBER_FACTORY = newFactory(Number.class, NUMBER);

  public static final TypeAdapter<Character> CHARACTER = new TypeAdapter<Character>() {
    @Override
    public Character read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return reader.nextString().charAt(0);
    }
    @Override
    public void write(JsonWriter writer, Character value) throws IOException {
      writer.value(value == null ? null : String.valueOf(value));
    }
  };

  public static final TypeAdapter.Factory CHARACTER_FACTORY
      = newFactory(char.class, Character.class, CHARACTER);

  public static final TypeAdapter<String> STRING = new TypeAdapter<String>() {
    @Override
    public String read(JsonReader reader) throws IOException {
      JsonToken peek = reader.peek();
      if (peek == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      /* coerce booleans to strings for backwards compatibility */
      if (peek == JsonToken.BOOLEAN) {
        return Boolean.toString(reader.nextBoolean());
      }
      return reader.nextString();
    }
    @Override
    public void write(JsonWriter writer, String value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory STRING_FACTORY = newFactory(String.class, STRING);

  public static final TypeAdapter<StringBuilder> STRING_BUILDER = new TypeAdapter<StringBuilder>() {
    @Override
    public StringBuilder read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return new StringBuilder(reader.nextString());
    }
    @Override
    public void write(JsonWriter writer, StringBuilder value) throws IOException {
      writer.value(value == null ? null : value.toString());
    }
  };

  public static final TypeAdapter.Factory STRING_BUILDER_FACTORY =
    newFactory(StringBuilder.class, STRING_BUILDER);

  public static final TypeAdapter<StringBuffer> STRING_BUFFER = new TypeAdapter<StringBuffer>() {
    @Override
    public StringBuffer read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return new StringBuffer(reader.nextString());
    }
    @Override
    public void write(JsonWriter writer, StringBuffer value) throws IOException {
      writer.value(value == null ? null : value.toString());
    }
  };

  public static final TypeAdapter.Factory STRING_BUFFER_FACTORY =
    newFactory(StringBuffer.class, STRING_BUFFER);

  public static final TypeAdapter<URL> URL = new TypeAdapter<URL>() {
    @Override
    public URL read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      String nextString = reader.nextString();
      return "null".equals(nextString) ? null : new URL(nextString);
    }
    @Override
    public void write(JsonWriter writer, URL value) throws IOException {
      writer.value(value == null ? null : value.toExternalForm());
    }
  };

  public static final TypeAdapter.Factory URL_FACTORY = newFactory(URL.class, URL);

  public static final TypeAdapter<URI> URI = new TypeAdapter<URI>() {
    @Override
    public URI read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      try {
        String nextString = reader.nextString();
        return "null".equals(nextString) ? null : new URI(nextString);
      } catch (URISyntaxException e) {
        throw new JsonIOException(e);
      }
    }
    @Override
    public void write(JsonWriter writer, URI value) throws IOException {
      writer.value(value == null ? null : value.toASCIIString());
    }
  };

  public static final TypeAdapter.Factory URI_FACTORY = newFactory(URI.class, URI);

  public static final TypeAdapter<InetAddress> INET_ADDRESS = new TypeAdapter<InetAddress>() {
    @Override
    public InetAddress read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return InetAddress.getByName(reader.nextString());
    }
    @Override
    public void write(JsonWriter writer, InetAddress value) throws IOException {
      writer.value(value == null ? null : value.getHostAddress());
    }
  };

  public static final TypeAdapter.Factory INET_ADDRESS_FACTORY =
    newTypeHierarchyFactory(InetAddress.class, INET_ADDRESS);

  public static final TypeAdapter<UUID> UUID = new TypeAdapter<UUID>() {
    @Override
    public UUID read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return java.util.UUID.fromString(reader.nextString());
    }
    @Override
    public void write(JsonWriter writer, UUID value) throws IOException {
      writer.value(value == null ? null : value.toString());
    }
  };

  public static final TypeAdapter.Factory UUID_FACTORY = newFactory(UUID.class, UUID);

  public static final TypeAdapter.Factory TIMESTAMP_FACTORY = new TypeAdapter.Factory() {
    @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
    public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
      if (typeToken.getRawType() != Timestamp.class) {
        return null;
      }

      final TypeAdapter<Date> dateTypeAdapter = context.getAdapter(Date.class);
      return (TypeAdapter<T>) new TypeAdapter<Timestamp>() {
        @Override public Timestamp read(JsonReader reader) throws IOException {
          Date date = dateTypeAdapter.read(reader);
          return date != null ? new Timestamp(date.getTime()) : null;
        }

        @Override public void write(JsonWriter writer, Timestamp value) throws IOException {
          dateTypeAdapter.write(writer, value);
        }
      };
    }
  };

  public static final TypeAdapter<Calendar> CALENDAR = new TypeAdapter<Calendar>() {
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY_OF_MONTH = "dayOfMonth";
    private static final String HOUR_OF_DAY = "hourOfDay";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";

    @Override
    public Calendar read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return  null;
      }
      reader.beginObject();
      int year = 0;
      int month = 0;
      int dayOfMonth = 0;
      int hourOfDay = 0;
      int minute = 0;
      int second = 0;
      while (reader.peek() != JsonToken.END_OBJECT) {
        String name = reader.nextName();
        int value = reader.nextInt();
        if (YEAR.equals(name)) {
          year = value;
        } else if (MONTH.equals(name)) {
          month = value;
        } else if (DAY_OF_MONTH.equals(name)) {
          dayOfMonth = value;
        } else if (HOUR_OF_DAY.equals(name)) {
          hourOfDay = value;
        } else if (MINUTE.equals(name)) {
          minute = value;
        } else if (SECOND.equals(name)) {
          second = value;
        }
      }
      reader.endObject();
      return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
    }

    @Override
    public void write(JsonWriter writer, Calendar value) throws IOException {
      if (value == null) {
        writer.nullValue();
        return;
      }
      writer.beginObject();
      writer.name(YEAR);
      writer.value(value.get(Calendar.YEAR));
      writer.name(MONTH);
      writer.value(value.get(Calendar.MONTH));
      writer.name(DAY_OF_MONTH);
      writer.value(value.get(Calendar.DAY_OF_MONTH));
      writer.name(HOUR_OF_DAY);
      writer.value(value.get(Calendar.HOUR_OF_DAY));
      writer.name(MINUTE);
      writer.value(value.get(Calendar.MINUTE));
      writer.name(SECOND);
      writer.value(value.get(Calendar.SECOND));
      writer.endObject();
    }
  };

  public static final TypeAdapter.Factory CALENDAR_FACTORY =
    newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, CALENDAR);

  public static final TypeAdapter<Locale> LOCALE = new TypeAdapter<Locale>() {
    @Override
    public Locale read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      String locale = reader.nextString();
      StringTokenizer tokenizer = new StringTokenizer(locale, "_");
      String language = null;
      String country = null;
      String variant = null;
      if (tokenizer.hasMoreElements()) {
        language = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreElements()) {
        country = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreElements()) {
        variant = tokenizer.nextToken();
      }
      if (country == null && variant == null) {
        return new Locale(language);
      } else if (variant == null) {
        return new Locale(language, country);
      } else {
        return new Locale(language, country, variant);
      }
    }
    @Override
    public void write(JsonWriter writer, Locale value) throws IOException {
      writer.value(value == null ? null : value.toString());
    }
  };

  public static final TypeAdapter.Factory LOCALE_FACTORY = newFactory(Locale.class, LOCALE);

  public static final TypeAdapter<JsonElement> JSON_ELEMENT = new TypeAdapter<JsonElement>() {
    @Override public JsonElement read(JsonReader reader) throws IOException {
      switch (reader.peek()) {
      case STRING:
        return new JsonPrimitive(reader.nextString());
      case NUMBER:
        String number = reader.nextString();
        return new JsonPrimitive(new LazilyParsedNumber(number));
      case BOOLEAN:
        return new JsonPrimitive(reader.nextBoolean());
      case NULL:
        reader.nextNull();
        return JsonNull.INSTANCE;
      case BEGIN_ARRAY:
        JsonArray array = new JsonArray();
        reader.beginArray();
        while (reader.hasNext()) {
          array.add(read(reader));
        }
        reader.endArray();
        return array;
      case BEGIN_OBJECT:
        JsonObject object = new JsonObject();
        reader.beginObject();
        while (reader.hasNext()) {
          object.add(reader.nextName(), read(reader));
        }
        reader.endObject();
        return object;
      case END_DOCUMENT:
      case NAME:
      case END_OBJECT:
      case END_ARRAY:
      default:
        throw new IllegalArgumentException();
      }
    }

    @Override public void write(JsonWriter writer, JsonElement value) throws IOException {
      if (value == null || value.isJsonNull()) {
        writer.nullValue();
      } else if (value.isJsonPrimitive()) {
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isNumber()) {
          writer.value(primitive.getAsNumber());
        } else if (primitive.isBoolean()) {
          writer.value(primitive.getAsBoolean());
        } else {
          writer.value(primitive.getAsString());
        }

      } else if (value.isJsonArray()) {
        writer.beginArray();
        for (JsonElement e : value.getAsJsonArray()) {
          write(writer, e);
        }
        writer.endArray();

      } else if (value.isJsonObject()) {
        writer.beginObject();
        for (Map.Entry<String, JsonElement> e : value.getAsJsonObject().entrySet()) {
          writer.name(e.getKey());
          write(writer, e.getValue());
        }
        writer.endObject();

      } else {
        throw new IllegalArgumentException("Couldn't write " + value.getClass());
      }
    }
  };

  public static final TypeAdapter.Factory JSON_ELEMENT_FACTORY
      = newFactory(JsonElement.class, JSON_ELEMENT);

  private static final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
    private final Class<T> classOfT;

    public EnumTypeAdapter(Class<T> classOfT) {
      this.classOfT = classOfT;
    }
    public T read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }
      return (T) Enum.valueOf((Class<T>) classOfT, reader.nextString());
    }

    public void write(JsonWriter writer, T value) throws IOException {
      writer.value(value == null ? null : value.name());
    }
  };

  public static final TypeAdapter.Factory ENUM_FACTORY = newEnumTypeHierarchyFactory(Enum.class);

  public static <TT> TypeAdapter.Factory newEnumTypeHierarchyFactory(final Class<TT> clazz) {
    return new TypeAdapter.Factory() {
      @SuppressWarnings({"rawtypes", "unchecked"})
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        return clazz.isAssignableFrom(rawType)
          ? (TypeAdapter<T>) new EnumTypeAdapter(rawType) : null;
      }
    };
  }

  public static <TT> TypeAdapter.Factory newFactory(
      final TypeToken<TT> type, final TypeAdapter<TT> typeAdapter) {
    return new TypeAdapter.Factory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        return typeToken.equals(type) ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }

  public static <TT> TypeAdapter.Factory newFactory(
      final Class<TT> type, final TypeAdapter<TT> typeAdapter) {
    return new TypeAdapter.Factory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        return typeToken.getRawType() == type ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }

  public static <TT> TypeAdapter.Factory newFactory(
      final Class<TT> unboxed, final Class<TT> boxed, final TypeAdapter<? super TT> typeAdapter) {
    return new TypeAdapter.Factory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        return (rawType == unboxed || rawType == boxed) ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }

  public static <TT> TypeAdapter.Factory newFactoryForMultipleTypes(
      final Class<TT> base, final Class<? extends TT> sub, final TypeAdapter<? super TT> typeAdapter) {
    return new TypeAdapter.Factory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        return (rawType == base || rawType == sub) ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }

  public static <TT> TypeAdapter.Factory newTypeHierarchyFactory(
      final Class<TT> clazz, final TypeAdapter<TT> typeAdapter) {
    return new TypeAdapter.Factory() {
      @SuppressWarnings("unchecked")
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        return clazz.isAssignableFrom(typeToken.getRawType()) ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }

}
