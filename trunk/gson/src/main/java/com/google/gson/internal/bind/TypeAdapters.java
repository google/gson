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

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * Type adapters for basic types.
 */
public final class TypeAdapters {
  private TypeAdapters() {}

  public static final TypeAdapter<Boolean> BOOLEAN = new TypeAdapter<Boolean>() {
    public Boolean read(JsonReader reader) throws IOException {
      return reader.nextBoolean();
    }
    public void write(JsonWriter writer, Boolean value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory BOOLEAN_FACTORY
      = newFactory(boolean.class, Boolean.class, BOOLEAN);

  public static final TypeAdapter<Integer> INTEGER = new TypeAdapter<Integer>() {
    public Integer read(JsonReader reader) throws IOException {
      return reader.nextInt();
    }
    public void write(JsonWriter writer, Integer value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory INTEGER_FACTORY
      = newFactory(int.class, Integer.class, INTEGER);

  public static final TypeAdapter<Long> LONG = new TypeAdapter<Long>() {
    public Long read(JsonReader reader) throws IOException {
      return reader.nextLong();
    }
    public void write(JsonWriter writer, Long value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory LONG_FACTORY
      = newFactory(long.class, Long.class, LONG);

  public static final TypeAdapter<Double> DOUBLE = new TypeAdapter<Double>() {
    public Double read(JsonReader reader) throws IOException {
      return reader.nextDouble();
    }
    public void write(JsonWriter writer, Double value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory DOUBLE_FACTORY
      = newFactory(double.class, Double.class, DOUBLE);

  public static final TypeAdapter<Float> FLOAT = new TypeAdapter<Float>() {
    public Float read(JsonReader reader) throws IOException {
      return (float) reader.nextDouble();
    }
    public void write(JsonWriter writer, Float value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory FLOAT_FACTORY
      = newFactory(float.class, Float.class, FLOAT);

  public static final TypeAdapter<String> STRING = new TypeAdapter<String>() {
    public String read(JsonReader reader) throws IOException {
      return reader.nextString();
    }
    public void write(JsonWriter writer, String value) throws IOException {
      writer.value(value);
    }
  };

  public static final TypeAdapter.Factory STRING_FACTORY = newFactory(String.class, STRING);

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
      final Class<T> unboxed, final Class<T> boxed, final TypeAdapter<T> typeAdapter) {
    return new TypeAdapter.Factory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        return (rawType == unboxed || rawType == boxed) ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }

  public static <T> TypeAdapter.Factory newTypeHierarchyFactory(
      TypeToken<T> type, TypeAdapter<T> typeAdapter) {
    return new TypeAdapter.Factory() {
      public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
        // TODO: use Inder's TypeHierarchyAdapter here
        throw new UnsupportedOperationException();
      }
    };
  }
}
