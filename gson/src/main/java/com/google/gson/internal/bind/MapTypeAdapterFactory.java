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

import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Adapt a map whose keys are any type.
 */
public final class MapTypeAdapterFactory implements TypeAdapter.Factory {
  private final ConstructorConstructor constructorConstructor;

  public MapTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
  }

  public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    if (!Map.class.isAssignableFrom(rawType)) {
      return null;
    }

    Class<?> rawTypeOfSrc = $Gson$Types.getRawType(type);
    Type childGenericType = $Gson$Types.getMapKeyAndValueTypes(type, rawTypeOfSrc)[1];
    TypeAdapter valueAdapter = context.getAdapter(TypeToken.get(childGenericType));
    ObjectConstructor<T> constructor = constructorConstructor.getConstructor(typeToken);

    @SuppressWarnings("unchecked") // we don't define a type parameter for the key or value types
    TypeAdapter<T> result = new Adapter(valueAdapter, constructor);
    return result;
  }

  private final class Adapter<V> extends TypeAdapter<Map<?, V>> {
    private final TypeAdapter<V> valueTypeAdapter;
    private final ObjectConstructor<? extends Map<String, V>> constructor;

    public Adapter(TypeAdapter<V> valueTypeAdapter,
        ObjectConstructor<? extends Map<String, V>> constructor) {
      this.valueTypeAdapter = valueTypeAdapter;
      this.constructor = constructor;
    }

    public Map<?, V> read(JsonReader reader) throws IOException {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull(); // TODO: does this belong here?
        return null;
      }

      Map<String, V> map = constructor.construct();

      reader.beginObject();
      while (reader.hasNext()) {
        String key = reader.nextName();
        V value = valueTypeAdapter.read(reader);
        map.put(key, value); // TODO: convert to the map's key type?
      }
      reader.endObject();
      return map;
    }

    public void write(JsonWriter writer, Map<?, V> map) throws IOException {
      if (map == null) {
        writer.nullValue(); // TODO: better policy here?
        return;
      }

      writer.beginObject();
      for (Map.Entry<?, V> entry : map.entrySet()) {
        String key = String.valueOf(entry.getKey());
        writer.name(key);
        valueTypeAdapter.write(writer, entry.getValue());
      }
      writer.endObject();
    }
  }
}
