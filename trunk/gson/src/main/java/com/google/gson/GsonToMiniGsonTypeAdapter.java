/*
 * Copyright (C) 2011 Google Inc.
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

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.MiniGson;
import com.google.gson.internal.bind.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

final class GsonToMiniGsonTypeAdapter implements TypeAdapter.Factory {
  private final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers;
  private final ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers;
  private final boolean serializeNulls;

  GsonToMiniGsonTypeAdapter(ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers,
      ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers, boolean serializeNulls) {
    this.serializers = serializers;
    this.deserializers = deserializers;
    this.serializeNulls = serializeNulls;
  }

  public <T> TypeAdapter<T> create(final MiniGson miniGson, TypeToken<T> type) {
    final Type typeOfT = type.getType();
    final JsonSerializer serializer = serializers.getHandlerFor(typeOfT, false);
    final JsonDeserializer deserializer = deserializers.getHandlerFor(typeOfT, false);
    if (serializer == null && deserializer == null) {
      return null;
    }
    return new TypeAdapter() {
      @Override
      public Object read(JsonReader reader) throws IOException {
        if (deserializer == null) {
          // TODO: handle if deserializer is null
          throw new UnsupportedOperationException();
        }
        JsonElement value = Streams.parse(reader);
        if (value.isJsonNull()) {
          return null;
        }
        return deserializer.deserialize(value, typeOfT, createDeserializationContext(miniGson));
      }
      @Override
      public void write(JsonWriter writer, Object value) throws IOException {
        if (serializer == null) {
          // TODO: handle if serializer is null
          throw new UnsupportedOperationException();
        }
        if (value == null) {
          writer.nullValue();
          return;
        }
        JsonElement element = serializer.serialize(value, typeOfT, createSerializationContext(miniGson));
        Streams.write(element, serializeNulls, writer);
      }
    };
  }

  public JsonSerializationContext createSerializationContext(final MiniGson miniGson) {
    return new JsonSerializationContext() {
      @Override
      JsonElement serialize(Object src, Type typeOfSrc, boolean preserveType, boolean defaultOnly) {
        TypeToken typeToken = TypeToken.get(typeOfSrc);
        return miniGson.getAdapter(typeToken).toJsonElement(src);
      }
    };
  }
  public JsonDeserializationContext createDeserializationContext(final MiniGson miniGson) {
    return new JsonDeserializationContext() {
      @Override
      public <T> T deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
        TypeToken typeToken = TypeToken.get(typeOfT);
        return (T) miniGson.getAdapter(typeToken).fromJsonElement(json);
      }

      @Override public <T> T construct(Type type) {
        throw new UnsupportedOperationException();
      }

      @Override public Object constructArray(Type type, int length) {
        throw new UnsupportedOperationException();
      }

      @Override public <T> T deserializeDefault(JsonElement json, Type typeOfT) throws JsonParseException {
        throw new UnsupportedOperationException();
      }
    };
  }
}
