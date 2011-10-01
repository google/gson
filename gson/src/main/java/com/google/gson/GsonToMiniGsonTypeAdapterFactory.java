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

import com.google.gson.internal.ParameterizedTypeHandlerMap;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.MiniGson;
import com.google.gson.internal.bind.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;

final class GsonToMiniGsonTypeAdapterFactory implements TypeAdapter.Factory {
  private final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers;
  private final ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers;
  private final JsonDeserializationContext deserializationContext;
  private final JsonSerializationContext serializationContext;

  public GsonToMiniGsonTypeAdapterFactory(final Gson gson,
      ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers,
      ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers) {
    this.serializers = serializers;
    this.deserializers = deserializers;

    this.deserializationContext = new JsonDeserializationContext() {
      public <T> T deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
        return gson.fromJson(json, typeOfT);
      }
    };

    this.serializationContext = new JsonSerializationContext() {
      public JsonElement serialize(Object src) {
        return gson.toJsonTree(src);
      }
      public JsonElement serialize(Object src, Type typeOfSrc) {
        return gson.toJsonTree(src, typeOfSrc);
      }
    };
  }

  public <T> TypeAdapter<T> create(final MiniGson context, final TypeToken<T> typeToken) {
    final Type type = typeToken.getType();

    @SuppressWarnings("unchecked") // guaranteed to match typeOfT
    final JsonSerializer<T> serializer
        = (JsonSerializer<T>) serializers.getHandlerFor(type, false);
    @SuppressWarnings("unchecked") // guaranteed to match typeOfT
    final JsonDeserializer<T> deserializer
        = (JsonDeserializer<T>) deserializers.getHandlerFor(type, false);

    if (serializer == null && deserializer == null) {
      return null;
    }

    return new TypeAdapter<T>() {
      /**
       * The delegate is lazily created because it may not be needed, and
       * creating it may fail.
       */
      private TypeAdapter<T> delegate;

      @Override public T read(JsonReader reader) throws IOException {
        if (deserializer == null) {
          return delegate().read(reader);
        }
        JsonElement value = Streams.parse(reader);
        if (value.isJsonNull()) {
          return null;
        }
        return deserializer.deserialize(value, type, deserializationContext);
      }

      @Override public void write(JsonWriter writer, T value) throws IOException {
        if (serializer == null) {
          delegate().write(writer, value);
          return;
        }
        if (value == null) {
          writer.nullValue();
          return;
        }
        JsonElement element = serializer.serialize(value, type, serializationContext);
        Streams.write(element, writer);
      }

      private TypeAdapter<T> delegate() {
        TypeAdapter<T> d = delegate;
        return d != null
            ? d
            : (delegate = context.getNextAdapter(GsonToMiniGsonTypeAdapterFactory.this, typeToken));
      }
    };
  }
}
