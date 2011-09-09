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
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapts types whose static type is only 'Object'. Uses getClass() on
 * serialization and a primitive/Map/List on deserialization.
 */
public final class ObjectTypeAdapter extends TypeAdapter<Object> {
  public static final Factory FACTORY = new Factory() {
    @SuppressWarnings("unchecked")
    @Override public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> type) {
      if (type.getRawType() == Object.class) {
        return (TypeAdapter<T>) new ObjectTypeAdapter(context);
      }
      return null;
    }
  };

  private final MiniGson miniGson;

  private ObjectTypeAdapter(MiniGson miniGson) {
    this.miniGson = miniGson;
  }

  @Override public Object read(JsonReader reader) throws IOException {
    JsonToken token = reader.peek();
    switch (token) {
    case BEGIN_ARRAY:
      List<Object> list = new ArrayList<Object>();
      reader.beginArray();
      while (reader.hasNext()) {
        list.add(read(reader));
      }
      reader.endArray();
      return list;

    case BEGIN_OBJECT:
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      reader.beginObject();
      while (reader.hasNext()) {
        map.put(reader.nextName(), read(reader));
      }
      reader.endObject();
      return map;

    case STRING:
      return reader.nextString();

    case NUMBER:
      return reader.nextDouble();

    case BOOLEAN:
      return reader.nextBoolean();

    case NULL:
      reader.nextNull();
      return null;

    }
    throw new IllegalStateException();
  }

  @SuppressWarnings("unchecked")
  @Override public void write(JsonWriter writer, Object value) throws IOException {
    if (value == null) {
      writer.nullValue(); // TODO: better policy here?
      return;
    }

    TypeAdapter<Object> typeAdapter = (TypeAdapter<Object>) miniGson.getAdapter(value.getClass());
    if (typeAdapter instanceof ObjectTypeAdapter) {
      writer.beginObject();
      writer.endObject();
      return;
    }

    typeAdapter.write(writer, value);
  }
}
