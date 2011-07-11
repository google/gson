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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adapt a map whose keys are strings.
 */
final class StringToValueMapTypeAdapter<V> extends TypeAdapter<Map<String, V>> {
  public static final Factory FACTORY = new Factory() {
    public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
      Type type = typeToken.getType();
      if (!(type instanceof ParameterizedType)) {
        return null;
      }

      Class<? super T> rawType = typeToken.getRawType();
      if (!Map.class.isAssignableFrom(rawType)) {
        return null;
      }

      Type[] keyAndValueTypes = $Gson$Types.getMapKeyAndValueTypes(type, rawType);
      if (keyAndValueTypes[0] != String.class) {
        return null;
      }
      TypeAdapter<?> valueAdapter = context.getAdapter(TypeToken.get(keyAndValueTypes[1]));

      Constructor<?> constructor;
      try {
        Class<?> constructorType = (rawType == Map.class) ? LinkedHashMap.class : rawType;
        constructor = constructorType.getConstructor();
      } catch (NoSuchMethodException e) {
        return null;
      }

      @SuppressWarnings("unchecked") // we don't define a type parameter for the key or value types
      TypeAdapter<T> result = new StringToValueMapTypeAdapter(valueAdapter, constructor);
      return result;
    }
  };

  private final TypeAdapter<V> valueTypeAdapter;
  private final Constructor<? extends Map<String, V>> constructor;

  public StringToValueMapTypeAdapter(TypeAdapter<V> valueTypeAdapter,
      Constructor<? extends Map<String, V>> constructor) {
    this.valueTypeAdapter = valueTypeAdapter;
    this.constructor = constructor;
  }

  public Map<String, V> read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull(); // TODO: does this belong here?
      return null;
    }

    Map<String, V> map = MiniGson.newInstance(constructor);
    reader.beginObject();
    while (reader.hasNext()) {
      String key = reader.nextName();
      V value = valueTypeAdapter.read(reader);
      map.put(key, value);
    }
    reader.endObject();
    return map;
  }

  public void write(JsonWriter writer, Map<String, V> map) throws IOException {
    if (map == null) {
      writer.nullValue(); // TODO: better policy here?
      return;
    }

    writer.beginObject();
    for (Map.Entry<String, V> entry : map.entrySet()) {
      writer.name(entry.getKey());
      valueTypeAdapter.write(writer, entry.getValue());
    }
    writer.endObject();
  }
}
