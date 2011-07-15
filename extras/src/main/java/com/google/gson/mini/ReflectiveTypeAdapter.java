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
import com.google.gson.stream.JsonToken;
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
        return null; // it's a primitive!
      }

      // TODO: use Joel's constructor calling code (with setAccessible)
      Constructor<? super T> constructor;
      try {
        constructor = raw.getDeclaredConstructor();
      } catch (NoSuchMethodException e) {
        return null;
      }

      return new ReflectiveTypeAdapter<T>(constructor, getBoundFields(context, type, raw));
    }

    private Map<String, BoundField> getBoundFields(
        MiniGson context, TypeToken<?> type, Class<?> raw) {
      Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
      while (raw != Object.class) {
        for (Field field : raw.getDeclaredFields()) {
          field.setAccessible(true); // TODO: don't call setAccessible unless necessary
          Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
          BoundField boundField = createBoundField(context, field, TypeToken.get(fieldType));
          result.put(boundField.name, boundField);
        }
        type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
        raw = type.getRawType();
      }
      return result;
    }
  };

  private final Constructor<? super T> constructor;
  private final Map<String, BoundField> map;
  private final BoundField[] boundFields;

  ReflectiveTypeAdapter(Constructor<? super T> constructor, Map<String, BoundField> map) {
    this.constructor = constructor;
    this.map = map;
    this.boundFields = map.values().toArray(new BoundField[map.size()]);
  }

  public T read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull(); // TODO: does this belong here?
      return null;
    }

    @SuppressWarnings("unchecked") // the '? super T' is a raw T (the only kind we can construct)
    T instance = (T) MiniGson.newInstance(constructor);

    // TODO: null out the other fields?

    reader.beginObject();
    try {
      while (reader.hasNext()) {
        String name = reader.nextName();
        BoundField field = map.get(name);
        if (field == null) {
          // TODO: define a better policy
          reader.skipValue();
        } else {
          field.read(reader, instance);
        }
      }
    } catch (IllegalAccessException e) {
      throw new AssertionError();
    }
    reader.endObject();
    return instance;
  }

  public void write(JsonWriter writer, T value) throws IOException {
    if (value == null) {
      writer.nullValue(); // TODO: better policy here?
      return;
    }

    writer.beginObject();
    try {
      for (BoundField boundField : boundFields) {
        writer.name(boundField.name);
        boundField.write(writer, value);
      }
    } catch (IllegalAccessException e) {
      throw new AssertionError();
    }
    writer.endObject();
  }

  static BoundField createBoundField(
      final MiniGson context, final Field field, final TypeToken<?> fieldType) {
    // special casing primitives here saves ~5% on Android...
    return new BoundField(field.getName()) {
      final TypeAdapter<?> typeAdapter = context.getAdapter(fieldType);
      @SuppressWarnings("unchecked") // the type adapter and field type always agree
      @Override void write(JsonWriter writer, Object value)
          throws IOException, IllegalAccessException {
        Object fieldValue = field.get(value);
        ((TypeAdapter) typeAdapter).write(writer, fieldValue);
      }
      @Override void read(JsonReader reader, Object value)
          throws IOException, IllegalAccessException {
        Object fieldValue = typeAdapter.read(reader);
        field.set(value, fieldValue);
      }
    };
  }

  static abstract class BoundField {
    final String name;

    protected BoundField(String name) {
      this.name = name;
    }

    abstract void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException;
    abstract void read(JsonReader reader, Object value) throws IOException, IllegalAccessException;
  }
}
