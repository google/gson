/*
 * Copyright (C) 2026 Google Inc.
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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Type adapters for {@link Optional}, {@link OptionalInt}, {@link OptionalLong}, and {@link
 * OptionalDouble}.
 *
 * <p>These adapters mimic what {@link ReflectiveTypeAdapterFactory} would produce for the same
 * types without using reflection or {@code sun.misc.Unsafe}, which require {@code
 * --add-opens=java.base/java.util=ALL-UNNAMED} and cannot instantiate value classes (JEP 401).
 */
final class JdkOptionalTypeAdapters {
  private JdkOptionalTypeAdapters() {}

  static final TypeAdapterFactory FACTORY =
      new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
          Class<? super T> rawType = typeToken.getRawType();
          TypeAdapter<?> adapter;
          if (rawType == Optional.class) {
            Type type = typeToken.getType();
            Type innerType =
                type instanceof ParameterizedType
                    ? ((ParameterizedType) type).getActualTypeArguments()[0]
                    : Object.class;
            TypeAdapter<?> innerAdapter = gson.getAdapter(TypeToken.get(innerType));
            TypeAdapter<?> wrappedAdapter =
                new TypeAdapterRuntimeTypeWrapper<>(gson, innerAdapter, innerType);
            adapter = new OptionalAdapter<>(wrappedAdapter).nullSafe();
          } else if (rawType == OptionalInt.class) {
            adapter = OPTIONAL_INT;
          } else if (rawType == OptionalLong.class) {
            adapter = OPTIONAL_LONG;
          } else if (rawType == OptionalDouble.class) {
            adapter = OPTIONAL_DOUBLE;
          } else {
            return null;
          }
          @SuppressWarnings("unchecked")
          TypeAdapter<T> result = (TypeAdapter<T>) adapter;
          return result;
        }
      };

  private static final class OptionalAdapter<E> extends TypeAdapter<Optional<E>> {
    private final TypeAdapter<E> innerAdapter;

    OptionalAdapter(TypeAdapter<E> innerAdapter) {
      this.innerAdapter = innerAdapter;
    }

    @Override
    public Optional<E> read(JsonReader in) throws IOException {
      E value = null;
      in.beginObject();
      while (in.peek() != JsonToken.END_OBJECT) {
        String name = in.nextName();
        if (name.equals("value")) {
          value = innerAdapter.read(in);
        } else {
          in.skipValue();
        }
      }
      in.endObject();
      return Optional.ofNullable(value);
    }

    @Override
    public void write(JsonWriter out, Optional<E> value) throws IOException {
      out.beginObject();
      out.name("value");
      if (value.isPresent()) {
        innerAdapter.write(out, value.get());
      } else {
        out.nullValue();
      }
      out.endObject();
    }
  }

  private static final TypeAdapter<OptionalInt> OPTIONAL_INT =
      new TypeAdapter<OptionalInt>() {
        @Override
        public OptionalInt read(JsonReader in) throws IOException {
          boolean isPresent = false;
          int value = 0;
          in.beginObject();
          while (in.peek() != JsonToken.END_OBJECT) {
            String name = in.nextName();
            switch (name) {
              case "isPresent":
                isPresent = in.nextBoolean();
                break;
              case "value":
                value = in.nextInt();
                break;
              default:
                in.skipValue();
            }
          }
          in.endObject();
          return isPresent ? OptionalInt.of(value) : OptionalInt.empty();
        }

        @Override
        public void write(JsonWriter out, OptionalInt value) throws IOException {
          out.beginObject();
          out.name("isPresent");
          out.value(value.isPresent());
          out.name("value");
          out.value(value.orElse(0));
          out.endObject();
        }
      }.nullSafe();

  private static final TypeAdapter<OptionalLong> OPTIONAL_LONG =
      new TypeAdapter<OptionalLong>() {
        @Override
        public OptionalLong read(JsonReader in) throws IOException {
          boolean isPresent = false;
          long value = 0;
          in.beginObject();
          while (in.peek() != JsonToken.END_OBJECT) {
            String name = in.nextName();
            switch (name) {
              case "isPresent":
                isPresent = in.nextBoolean();
                break;
              case "value":
                value = in.nextLong();
                break;
              default:
                in.skipValue();
            }
          }
          in.endObject();
          return isPresent ? OptionalLong.of(value) : OptionalLong.empty();
        }

        @Override
        public void write(JsonWriter out, OptionalLong value) throws IOException {
          out.beginObject();
          out.name("isPresent");
          out.value(value.isPresent());
          out.name("value");
          out.value(value.orElse(0));
          out.endObject();
        }
      }.nullSafe();

  private static final TypeAdapter<OptionalDouble> OPTIONAL_DOUBLE =
      new TypeAdapter<OptionalDouble>() {
        @Override
        public OptionalDouble read(JsonReader in) throws IOException {
          boolean isPresent = false;
          double value = 0;
          in.beginObject();
          while (in.peek() != JsonToken.END_OBJECT) {
            String name = in.nextName();
            switch (name) {
              case "isPresent":
                isPresent = in.nextBoolean();
                break;
              case "value":
                value = in.nextDouble();
                break;
              default:
                in.skipValue();
            }
          }
          in.endObject();
          return isPresent ? OptionalDouble.of(value) : OptionalDouble.empty();
        }

        @Override
        public void write(JsonWriter out, OptionalDouble value) throws IOException {
          out.beginObject();
          out.name("isPresent");
          out.value(value.isPresent());
          out.name("value");
          // Note: in OpenJDK, OptionalDouble.EMPTY has value = Double.NaN, which causes
          // ReflectiveTypeAdapterFactory to fail unless serializeSpecialFloatingPointValues()
          // is enabled. Writing 0.0 when empty avoids that failure.
          out.value(value.orElse(0));
          out.endObject();
        }
      }.nullSafe();
}
