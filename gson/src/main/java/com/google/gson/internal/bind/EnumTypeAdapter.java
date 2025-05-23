/*
 * Copyright (C) 2024 Google Inc.
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
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Adapter for enum classes (but not for the base class {@code java.lang.Enum}). */
class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
  static final TypeAdapterFactory FACTORY =
      new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
          Class<? super T> rawType = typeToken.getRawType();
          if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
            return null;
          }
          if (!rawType.isEnum()) {
            rawType = rawType.getSuperclass(); // handle anonymous subclasses
          }
          @SuppressWarnings({"rawtypes", "unchecked"})
          TypeAdapter<T> adapter = (TypeAdapter<T>) new EnumTypeAdapter(rawType);
          return adapter;
        }
      };

  private final Map<String, T> nameToConstant = new HashMap<>();
  private final Map<String, T> stringToConstant = new HashMap<>();
  private final Map<T, String> constantToName = new HashMap<>();

  private EnumTypeAdapter(Class<T> classOfT) {
    try {
      // Uses reflection to find enum constants to work around name mismatches for obfuscated
      // classes
      Field[] fields = classOfT.getDeclaredFields();
      int constantCount = 0;
      for (Field f : fields) {
        // Filter out non-constant fields, replacing elements as we go
        if (f.isEnumConstant()) {
          fields[constantCount++] = f;
        }
      }

      // Trim the array to the new length. Every enum type can be expected to have at least
      // one declared field which is not an enum constant, namely the implicit $VALUES array
      fields = Arrays.copyOf(fields, constantCount);

      AccessibleObject.setAccessible(fields, true);

      for (Field constantField : fields) {
        @SuppressWarnings("unchecked")
        T constant = (T) constantField.get(null);
        String name = constant.name();
        String toStringVal = constant.toString();

        SerializedName annotation = constantField.getAnnotation(SerializedName.class);
        if (annotation != null) {
          name = annotation.value();
          for (String alternate : annotation.alternate()) {
            nameToConstant.put(alternate, constant);
          }
        }
        nameToConstant.put(name, constant);
        stringToConstant.put(toStringVal, constant);
        constantToName.put(constant, name);
      }
    } catch (IllegalAccessException e) {
      // IllegalAccessException should be impossible due to the `setAccessible` call above;
      // and even that should probably not fail since enum constants are implicitly public
      throw new AssertionError(e);
    }
  }

  @Override
  public T read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    String key = in.nextString();
    T constant = nameToConstant.get(key);
    // Note: If none of the approaches find the constant, this returns null
    return (constant == null) ? stringToConstant.get(key) : constant;
  }

  @Override
  public void write(JsonWriter out, T value) throws IOException {
    out.value(value == null ? null : constantToName.get(value));
  }
}
