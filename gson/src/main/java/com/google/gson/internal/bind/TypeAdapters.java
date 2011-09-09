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

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Type adapters for basic types.
 */
public final class TypeAdapters {
  private TypeAdapters() {}

  public static final TypeAdapter<Boolean> BOOLEAN = new TypeAdapter<Boolean>() {
    public Boolean read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      return reader.nextBoolean();
    }
    public void write(JsonWriter writer, Boolean value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory BOOLEAN_FACTORY
      = newFactory(boolean.class, Boolean.class, BOOLEAN);

  public static final TypeAdapter<Number> BYTE = new TypeAdapter<Number>() {
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      try {
        int intValue = reader.nextInt();
        return (byte) intValue;
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory BYTE_FACTORY
      = newFactory(byte.class, Byte.class, BYTE);

  public static final TypeAdapter<Number> SHORT = new TypeAdapter<Number>() {
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      try {
        return (short) reader.nextInt();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory SHORT_FACTORY
      = newFactory(short.class, Short.class, SHORT);

  public static final TypeAdapter<Number> INTEGER = new TypeAdapter<Number>() {
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      try {
        return reader.nextInt();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory INTEGER_FACTORY
      = newFactory(int.class, Integer.class, INTEGER);

  public static final TypeAdapter<Number> LONG = new TypeAdapter<Number>() {
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      try {
        return reader.nextLong();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory LONG_FACTORY
      = newFactory(long.class, Long.class, LONG);

  public static final TypeAdapter<Number> FLOAT = new TypeAdapter<Number>() {
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      return (float) reader.nextDouble();
    }
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory FLOAT_FACTORY
      = newFactory(float.class, Float.class, FLOAT);

  public static final TypeAdapter<Number> DOUBLE = new TypeAdapter<Number>() {
    public Number read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      return reader.nextDouble();
    }
    public void write(JsonWriter writer, Number value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory DOUBLE_FACTORY
      = newFactory(double.class, Double.class, DOUBLE);

  public static final TypeAdapter<String> STRING = new TypeAdapter<String>() {
    public String read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      return reader.nextString();
    }
    public void write(JsonWriter writer, String value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory STRING_FACTORY = newFactory(String.class, STRING);

  public static final TypeAdapter<StringBuilder> STRING_BUILDER = new TypeAdapter<StringBuilder>() {
    public StringBuilder read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      return new StringBuilder(reader.nextString());
    }
    public void write(JsonWriter writer, StringBuilder value) throws IOException {
      writer.value(value.toString());
    }
  };

  public static final TypeAdapter.Factory STRING_BUILDER_FACTORY =
    newFactory(StringBuilder.class, STRING_BUILDER);

  public static final TypeAdapter<StringBuffer> STRING_BUFFER = new TypeAdapter<StringBuffer>() {
    public StringBuffer read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      return new StringBuffer(reader.nextString());
    }
    public void write(JsonWriter writer, StringBuffer value) throws IOException {
      writer.value(value.toString());
    }
  };

  public static final TypeAdapter.Factory STRING_BUFFER_FACTORY =
    newFactory(StringBuffer.class, STRING_BUFFER);

  public static final TypeAdapter<URL> URL = new TypeAdapter<URL>() {
    public URL read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      String nextString = reader.nextString();
      return "null".equals(nextString) ? null : new URL(nextString);
    }
    public void write(JsonWriter writer, URL value) throws IOException {
      writer.value(value == null ? null : value.toExternalForm());
    }
  };

  public static final TypeAdapter.Factory URL_FACTORY = newFactory(URL.class, URL);

  public static final TypeAdapter<URI> URI = new TypeAdapter<URI>() {
    public URI read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      try {
        String nextString = reader.nextString();
        return "null".equals(nextString) ? null : new URI(nextString);
      } catch (URISyntaxException e) {
        throw new IOException(e);
      }
    }
    public void write(JsonWriter writer, URI value) throws IOException {
      writer.value(value == null ? null : value.toASCIIString());
    }
  };

  public static final TypeAdapter.Factory URI_FACTORY = newFactory(URI.class, URI);

  public static final TypeAdapter<InetAddress> INET_ADDRESS = new TypeAdapter<InetAddress>() {
    public InetAddress read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      return InetAddress.getByName(reader.nextString());
    }
    public void write(JsonWriter writer, InetAddress value) throws IOException {
      writer.value(value.getHostAddress());
    }
  };

  public static final TypeAdapter.Factory INET_ADDRESS_FACTORY =
    newTypeHierarchyFactory(InetAddress.class, INET_ADDRESS);

  public static final TypeAdapter<UUID> UUID = new TypeAdapter<UUID>() {
    public UUID read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }
      return java.util.UUID.fromString(reader.nextString());
    }
    public void write(JsonWriter writer, UUID value) throws IOException {
      writer.value(value.toString());
    }
  };

  public static final TypeAdapter.Factory UUID_FACTORY = newFactory(UUID.class, UUID);

  public static final TypeAdapter<Locale> LOCALE = new TypeAdapter<Locale>() {
    public Locale read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
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
    public void write(JsonWriter writer, Locale value) throws IOException {
      writer.value(value.toString());
    }
  };

  public static final TypeAdapter.Factory LOCALE_FACTORY = newFactory(Locale.class, LOCALE);

  public static final TypeAdapter EXCLUDED_TYPE_ADAPTER = new TypeAdapter<Object>() {
    @Override public Object read(JsonReader reader) throws IOException {
      reader.skipValue();
      return null;
    }
    @Override public void write(JsonWriter writer, Object value) throws IOException {
      writer.nullValue();
    }
  };

  public static <T> TypeAdapter.Factory newFactory(
      final TypeToken<T> type, final TypeAdapter<T> typeAdapter) {
    return new TypeAdapter.Factory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        return typeToken.equals(type) ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }

  public static <T> TypeAdapter.Factory newFactory(
      final Class<T> type, final TypeAdapter<T> typeAdapter) {
    return new TypeAdapter.Factory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        return typeToken.getRawType() == type ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }

  public static <T> TypeAdapter.Factory newFactory(
      final Class<T> unboxed, final Class<T> boxed, final TypeAdapter<? super T> typeAdapter) {
    return new TypeAdapter.Factory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        return (rawType == unboxed || rawType == boxed) ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }

  public static <T> TypeAdapter.Factory newTypeHierarchyFactory(
      final Class<T> clazz, final TypeAdapter<T> typeAdapter) {
    return new TypeAdapter.Factory() {
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        return clazz.isAssignableFrom(typeToken.getRawType()) ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }
}
