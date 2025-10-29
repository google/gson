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

import static com.google.gson.Gson.DEFAULT_COMPLEX_MAP_KEYS;
import static com.google.gson.Gson.DEFAULT_DATE_PATTERN;
import static com.google.gson.Gson.DEFAULT_FIELD_NAMING_STRATEGY;
import static com.google.gson.Gson.DEFAULT_NUMBER_TO_NUMBER_STRATEGY;
import static com.google.gson.Gson.DEFAULT_OBJECT_TO_NUMBER_STRATEGY;
import static com.google.gson.Gson.DEFAULT_SPECIALIZE_FLOAT_VALUES;
import static com.google.gson.Gson.DEFAULT_USE_JDK_UNSAFE;

import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.bind.ArrayTypeAdapter;
import com.google.gson.internal.bind.CollectionTypeAdapterFactory;
import com.google.gson.internal.bind.DefaultDateTypeAdapter;
import com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;
import com.google.gson.internal.bind.MapTypeAdapterFactory;
import com.google.gson.internal.bind.NumberTypeAdapter;
import com.google.gson.internal.bind.ObjectTypeAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
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

final class BuilderHelper {
  private static final ConstructorConstructor DEFAULT_CONSTRUCTOR_CONSTRUCTOR =
      new ConstructorConstructor(
          Collections.emptyMap(), DEFAULT_USE_JDK_UNSAFE, Collections.emptyList());

  private static final MapTypeAdapterFactory DEFAULT_MAP_TYPE_ADAPTER_FACTORY =
      new MapTypeAdapterFactory(DEFAULT_CONSTRUCTOR_CONSTRUCTOR, DEFAULT_COMPLEX_MAP_KEYS);

  private static final CollectionTypeAdapterFactory DEFAULT_COLLECTION_TYPE_ADAPTER_FACTORY =
      new CollectionTypeAdapterFactory(DEFAULT_CONSTRUCTOR_CONSTRUCTOR);

  private static final JsonAdapterAnnotationTypeAdapterFactory
      DEFAULT_JSON_ADAPTER_ANNOTATION_TYPE_ADAPTER_FACTORY =
          new JsonAdapterAnnotationTypeAdapterFactory(DEFAULT_CONSTRUCTOR_CONSTRUCTOR);

  private static final ReflectiveTypeAdapterFactory DEFAULT_REFLECTIVE_TYPE_ADAPTER_FACTORY =
      new ReflectiveTypeAdapterFactory(
          DEFAULT_CONSTRUCTOR_CONSTRUCTOR,
          DEFAULT_FIELD_NAMING_STRATEGY,
          Excluder.DEFAULT,
          DEFAULT_JSON_ADAPTER_ANNOTATION_TYPE_ADAPTER_FACTORY,
          Collections.emptyList());

  private BuilderHelper() {}

  static final List<TypeAdapterFactory> DEFAULT_TYPE_ADAPTER_FACTORIES;

  static final int EXPECTED_FACTORIES_SIZE = 45;

  static {
    List<TypeAdapterFactory> factories = new ArrayList<>(EXPECTED_FACTORIES_SIZE);

    // built-in type adapters that cannot be overridden
    factories.add(TypeAdapters.JSON_ELEMENT_FACTORY);
    factories.add(ObjectTypeAdapter.getFactory(DEFAULT_OBJECT_TO_NUMBER_STRATEGY));

    // the excluder must precede all adapters that handle user-defined types
    factories.add(Excluder.DEFAULT);

    // dates
    addTypeAdaptersForDate(DEFAULT_DATE_PATTERN, DateFormat.DEFAULT, DateFormat.DEFAULT, factories);

    // type adapters for basic platform types
    factories.add(TypeAdapters.STRING_FACTORY);
    factories.add(TypeAdapters.INTEGER_FACTORY);
    factories.add(TypeAdapters.BOOLEAN_FACTORY);
    factories.add(TypeAdapters.BYTE_FACTORY);
    factories.add(TypeAdapters.SHORT_FACTORY);
    TypeAdapter<Number> longAdapter = LongSerializationPolicy.DEFAULT.typeAdapter();
    factories.add(TypeAdapters.newFactory(long.class, Long.class, longAdapter));
    factories.add(
        TypeAdapters.newFactory(
            double.class, Double.class, doubleAdapter(DEFAULT_SPECIALIZE_FLOAT_VALUES)));
    factories.add(
        TypeAdapters.newFactory(
            float.class, Float.class, floatAdapter(DEFAULT_SPECIALIZE_FLOAT_VALUES)));
    factories.add(NumberTypeAdapter.getFactory(DEFAULT_NUMBER_TO_NUMBER_STRATEGY));
    factories.add(TypeAdapters.ATOMIC_INTEGER_FACTORY);
    factories.add(TypeAdapters.ATOMIC_BOOLEAN_FACTORY);
    factories.add(TypeAdapters.newFactory(AtomicLong.class, atomicLongAdapter(longAdapter)));
    factories.add(
        TypeAdapters.newFactory(AtomicLongArray.class, atomicLongArrayAdapter(longAdapter)));
    factories.add(TypeAdapters.ATOMIC_INTEGER_ARRAY_FACTORY);
    factories.add(TypeAdapters.CHARACTER_FACTORY);
    factories.add(TypeAdapters.STRING_BUILDER_FACTORY);
    factories.add(TypeAdapters.STRING_BUFFER_FACTORY);
    factories.add(TypeAdapters.BIG_DECIMAL_FACTORY);
    factories.add(TypeAdapters.BIG_INTEGER_FACTORY);
    // Add adapter for LazilyParsedNumber because user can obtain it from Gson and then try to
    // serialize it again
    factories.add(TypeAdapters.LAZILY_PARSED_NUMBER_FACTORY);
    factories.add(TypeAdapters.URL_FACTORY);
    factories.add(TypeAdapters.URI_FACTORY);
    factories.add(TypeAdapters.UUID_FACTORY);
    factories.add(TypeAdapters.CURRENCY_FACTORY);
    factories.add(TypeAdapters.LOCALE_FACTORY);
    factories.add(TypeAdapters.INET_ADDRESS_FACTORY);
    factories.add(TypeAdapters.BIT_SET_FACTORY);
    factories.add(DefaultDateTypeAdapter.DEFAULT_STYLE_FACTORY);
    factories.add(TypeAdapters.CALENDAR_FACTORY);
    factories.addAll(SqlTypesSupport.SQL_TYPE_FACTORIES);
    factories.add(ArrayTypeAdapter.FACTORY);
    factories.add(TypeAdapters.CLASS_FACTORY);

    // type adapters for composite and user-defined types
    factories.add(DEFAULT_COLLECTION_TYPE_ADAPTER_FACTORY);
    factories.add(DEFAULT_MAP_TYPE_ADAPTER_FACTORY);
    factories.add(DEFAULT_JSON_ADAPTER_ANNOTATION_TYPE_ADAPTER_FACTORY);
    factories.add(TypeAdapters.ENUM_FACTORY);
    factories.add(DEFAULT_REFLECTIVE_TYPE_ADAPTER_FACTORY);

    DEFAULT_TYPE_ADAPTER_FACTORIES = immutableList(factories);
  }

  @SuppressWarnings("unchecked")
  static <E> List<E> immutableList(Collection<E> collection) {
    if (collection.isEmpty()) {
      return Collections.emptyList();
    }
    if (collection.size() == 1) {
      return Collections.singletonList(
          collection instanceof List
              ? ((List<E>) collection).get(0)
              : collection.iterator().next());
    }
    return (List<E>)
        Collections.unmodifiableList(
            collection instanceof List
                ? (List<E>) collection
                : Arrays.asList(collection.toArray()));
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

  static TypeAdapter<Number> doubleAdapter(boolean serializeSpecialFloatingPointValues) {
    return serializeSpecialFloatingPointValues ? TypeAdapters.DOUBLE : DOUBLE_STRICT;
  }

  static TypeAdapter<Number> floatAdapter(boolean serializeSpecialFloatingPointValues) {
    return serializeSpecialFloatingPointValues ? TypeAdapters.FLOAT : FLOAT_STRICT;
  }

  private static void checkValidFloatingPoint(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      throw new IllegalArgumentException(
          value
              + " is not a valid double value as per JSON specification. To override this"
              + " behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.");
    }
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
        // Simulates ArrayList growth behavior
        long[] array = new long[10];
        int count = 0;
        in.beginArray();
        while (in.hasNext()) {
          long value = longAdapter.read(in).longValue();
          if (count >= array.length) {
            array = Arrays.copyOf(array, count + (count >> 1));
          }
          array[count++] = value;
        }
        in.endArray();
        if (count != array.length) {
          array = Arrays.copyOf(array, count);
        }
        return new AtomicLongArray(array);
      }
    }.nullSafe();
  }

  static void addTypeAdaptersForDate(
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
