/*
 * Copyright (C) 2025 Google Inc.
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

import com.google.gson.internal.bind.DefaultDateTypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.internal.sql.SqlTypesSupport;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

final class GsonBuilderHelper {

  private GsonBuilderHelper() {}

  static <E> List<E> newImmutableList(Collection<E> collection) {
    if (collection.isEmpty()) {
      return Collections.emptyList();
    }
    if (collection.size() == 1) {
      return Collections.singletonList(
          collection instanceof List
              ? ((List<E>) collection).get(0)
              : collection.iterator().next());
    }
    @SuppressWarnings("unchecked")
    List<E> list = (List<E>) Collections.unmodifiableList(Arrays.asList(collection.toArray()));
    return list;
  }

  private static final TypeAdapter<Number> DOUBLE_STRICT =
      new TypeAdapter<Number>() {
        @Override
        public Double read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          return in.nextDouble();
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
          if (value == null) {
            out.nullValue();
            return;
          }
          double doubleValue = value.doubleValue();
          checkValidFloatingPoint(doubleValue);
          out.value(doubleValue);
        }
      };

  private static final TypeAdapter<Number> FLOAT_STRICT =
      new TypeAdapter<Number>() {
        @Override
        public Float read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          return (float) in.nextDouble();
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
          if (value == null) {
            out.nullValue();
            return;
          }
          float floatValue = value.floatValue();
          checkValidFloatingPoint(floatValue);
          // For backward compatibility don't call `JsonWriter.value(float)` because that method has
          // been newly added and not all custom JsonWriter implementations might override it yet
          Number floatNumber = value instanceof Float ? value : floatValue;
          out.value(floatNumber);
        }
      };

  private static void checkValidFloatingPoint(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      throw new IllegalArgumentException(
          value
              + " is not a valid double value as per JSON specification. To override this"
              + " behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.");
    }
  }

  static TypeAdapter<Number> doubleAdapter(boolean serializeSpecialFloatingPointValues) {
    return serializeSpecialFloatingPointValues ? TypeAdapters.DOUBLE : DOUBLE_STRICT;
  }

  static TypeAdapter<Number> floatAdapter(boolean serializeSpecialFloatingPointValues) {
    return serializeSpecialFloatingPointValues ? TypeAdapters.FLOAT : FLOAT_STRICT;
  }

  static TypeAdapter<AtomicLong> atomicLongAdapter(TypeAdapter<Number> longAdapter) {
    return new TypeAdapter<AtomicLong>() {
      @Override
      public void write(JsonWriter out, AtomicLong value) throws IOException {
        longAdapter.write(out, value.get());
      }

      @Override
      public AtomicLong read(JsonReader in) throws IOException {
        Number value = longAdapter.read(in);
        return new AtomicLong(value.longValue());
      }
    }.nullSafe();
  }

  static TypeAdapter<AtomicLongArray> atomicLongArrayAdapter(TypeAdapter<Number> longAdapter) {
    return new TypeAdapter<AtomicLongArray>() {
      @Override
      public void write(JsonWriter out, AtomicLongArray value) throws IOException {
        out.beginArray();
        for (int i = 0, length = value.length(); i < length; i++) {
          longAdapter.write(out, value.get(i));
        }
        out.endArray();
      }

      @Override
      public AtomicLongArray read(JsonReader in) throws IOException {
        List<Long> list = new ArrayList<>();
        in.beginArray();
        while (in.hasNext()) {
          long value = longAdapter.read(in).longValue();
          list.add(value);
        }
        in.endArray();
        int length = list.size();
        AtomicLongArray array = new AtomicLongArray(length);
        for (int i = 0; i < length; ++i) {
          array.set(i, list.get(i));
        }
        return array;
      }
    }.nullSafe();
  }

  static void addDateTypeAdapters(
      String datePattern, int dateStyle, int timeStyle, List<TypeAdapterFactory> factories) {
    TypeAdapterFactory dateAdapterFactory;
    boolean sqlTypesSupported = SqlTypesSupport.SUPPORTS_SQL_TYPES;
    TypeAdapterFactory sqlTimestampAdapterFactory = null;
    TypeAdapterFactory sqlDateAdapterFactory = null;

    if (datePattern != null && !datePattern.trim().isEmpty()) {
      dateAdapterFactory = DefaultDateTypeAdapter.DateType.DATE.createAdapterFactory(datePattern);

      if (sqlTypesSupported) {
        sqlTimestampAdapterFactory =
            SqlTypesSupport.TIMESTAMP_DATE_TYPE.createAdapterFactory(datePattern);
        sqlDateAdapterFactory = SqlTypesSupport.DATE_DATE_TYPE.createAdapterFactory(datePattern);
      }
    } else if (dateStyle != DateFormat.DEFAULT || timeStyle != DateFormat.DEFAULT) {
      dateAdapterFactory =
          DefaultDateTypeAdapter.DateType.DATE.createAdapterFactory(dateStyle, timeStyle);

      if (sqlTypesSupported) {
        sqlTimestampAdapterFactory =
            SqlTypesSupport.TIMESTAMP_DATE_TYPE.createAdapterFactory(dateStyle, timeStyle);
        sqlDateAdapterFactory =
            SqlTypesSupport.DATE_DATE_TYPE.createAdapterFactory(dateStyle, timeStyle);
      }
    } else {
      return;
    }

    factories.add(dateAdapterFactory);
    if (sqlTypesSupported) {
      factories.add(sqlTimestampAdapterFactory);
      factories.add(sqlDateAdapterFactory);
    }
  }
}
