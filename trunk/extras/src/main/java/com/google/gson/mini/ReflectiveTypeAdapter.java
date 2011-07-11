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

package com.google.gson.mini;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adapts the fields of an object to the properties of a JSON object.
 */
final class ReflectiveTypeAdapter<T> extends TypeAdapter<T>  {
  public static final Factory FACTORY = new Factory() {
    public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> type) {
      Class<? super T> raw = type.getRawType();

      if (!Object.class.isAssignableFrom(raw)) {
        // TODO: does this catch primitives?
        return null;
      }

      // TODO: use Joel's constructor calling code (with setAccessible)
      Constructor<T> constructor;
      try {
        constructor = (Constructor<T>) raw.getDeclaredConstructor();
      } catch (NoSuchMethodException e) {
        return null;
      }

      return new ReflectiveTypeAdapter<T>(constructor, getBoundFields(context, type, raw));
    }

    private Map<String, BoundField<?>> getBoundFields(
        MiniGson context, TypeToken<?> type, Class<?> raw) {
      Map<String, BoundField<?>> result = new LinkedHashMap<String, BoundField<?>>();
      while (raw != Object.class) {
        for (Field field : raw.getDeclaredFields()) {
          Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
          BoundField<?> boundField = BoundField.create(context, field, TypeToken.get(fieldType));
          result.put(boundField.name, boundField);
        }
        type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
        raw = type.getRawType();
      }
      return result;
    }
  };

  private final Constructor<T> constructor;
  private final Map<String, BoundField<?>> map;
  private final BoundField<?>[] boundFields;

  ReflectiveTypeAdapter(Constructor<T> constructor, Map<String, BoundField<?>> map) {
    this.constructor = constructor;
    this.map = map;
    this.boundFields = map.values().toArray(new BoundField<?>[map.size()]);
  }

  public T read(JsonReader reader) throws IOException {
    T instance = MiniGson.newInstance(constructor);

    // TODO: null out the other fields?

    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      BoundField<?> field = map.get(name);
      if (field == null) {
        // TODO: define a better policy
        reader.skipValue();
      } else {
        field.read(reader, instance);
      }
    }
    reader.endObject();
    return instance;
  }

  public void write(JsonWriter writer, T value) throws IOException {
    writer.beginObject();
    for (BoundField<?> boundField : boundFields) {
      writer.name(boundField.name);
      boundField.write(writer, value);
    }
    writer.endObject();
  }

  static class BoundField<T> {
    final String name;
    final Field field;
    final TypeAdapter<T> typeAdapter;

    BoundField(String name, Field field, TypeAdapter<T> typeAdapter) {
      this.name = name;
      this.field = field;
      this.typeAdapter = typeAdapter;
    }

    static <T> BoundField<T> create(MiniGson context, Field field, TypeToken<T> fieldType) {
      return new BoundField<T>(field.getName(), field, context.getAdapter(fieldType));
    }

    void write(JsonWriter writer, Object value) throws IOException {
      try {
        @SuppressWarnings("unchecked") // we previously verified that field is of type T
        T fieldValue = (T) field.get(value);
        typeAdapter.write(writer, fieldValue);
      } catch (IllegalAccessException e) {
        throw new AssertionError();
      }
    }

    void read(JsonReader reader, Object value) throws IOException {
      T fieldValue = typeAdapter.read(reader);
      try {
        field.set(value, fieldValue);
      } catch (IllegalAccessException e) {
        throw new AssertionError();
      }
    }
  }
}
