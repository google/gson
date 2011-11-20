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

import com.google.gson.internal.$Gson$Preconditions;
import com.google.gson.internal.Streams;
import com.google.gson.internal.TypeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Adapts a Gson 1.x tree-style adapter as a streaming TypeAdapter. Since the
 * tree adapter may be serialization-only or deserialization-only, this class
 * has a facility to lookup a delegate type adapter on demand.
 */
public final class TreeTypeAdapter<T> extends TypeAdapter<T> {
  private final JsonSerializer<T> serializer;
  private final JsonDeserializer<T> deserializer;
  private final Gson gson;
  private final TypeToken<T> typeToken;
  private final Factory skipPast;

  /** The delegate is lazily created because it may not be needed, and creating it may fail. */
  private TypeAdapter<T> delegate;

  private TreeTypeAdapter(JsonSerializer<T> serializer, JsonDeserializer<T> deserializer,
      Gson gson, TypeToken<T> typeToken, Factory skipPast) {
    this.serializer = serializer;
    this.deserializer = deserializer;
    this.gson = gson;
    this.typeToken = typeToken;
    this.skipPast = skipPast;
  }

  @Override public T read(JsonReader reader) throws IOException {
    if (deserializer == null) {
      return delegate().read(reader);
    }
    JsonElement value = Streams.parse(reader);
    if (value.isJsonNull()) {
      return null;
    }
    return deserializer.deserialize(value, typeToken.getType(), gson.deserializationContext);
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
    JsonElement tree = serializer.serialize(value, typeToken.getType(), gson.serializationContext);
    Streams.write(tree,writer);
  }

  private TypeAdapter<T> delegate() {
    TypeAdapter<T> d = delegate;
    return d != null
        ? d
        : (delegate = gson.getNextAdapter(skipPast, typeToken));
  }

  public static class SingleTypeFactory implements TypeAdapter.Factory {
    private final TypeToken<?> typeToken;
    private final JsonSerializer<?> serializer;
    private final JsonDeserializer<?> deserializer;

    public SingleTypeFactory(TypeToken<?> typeToken, Object typeAdapter) {
      this.typeToken = typeToken;
      serializer = typeAdapter instanceof JsonSerializer
          ? (JsonSerializer) typeAdapter
          : null;
      deserializer = typeAdapter instanceof JsonDeserializer
          ? (JsonDeserializer) typeAdapter
          : null;
      $Gson$Preconditions.checkArgument(serializer != null || deserializer != null);
    }

    @SuppressWarnings("unchecked") // guarded by typeToken.equals() call
    public <T> TypeAdapter<T> create(Gson context, TypeToken<T> type) {
      return typeToken.equals(type)
          ? new TreeTypeAdapter<T>((JsonSerializer<T>) serializer,
              (JsonDeserializer<T>) deserializer, context, type, this)
          : null;
    }
  }

  public static class TypeHierarchyFactory implements TypeAdapter.Factory {
    private final TypeMap<JsonSerializer<?>> serializers;
    private final TypeMap<JsonDeserializer<?>> deserializers;

    public TypeHierarchyFactory(TypeMap<JsonSerializer<?>> serializers,
        TypeMap<JsonDeserializer<?>> deserializers) {
      this.serializers = serializers;
      this.deserializers = deserializers;
    }

    @SuppressWarnings("unchecked") // guaranteed by serializers lookup matching type
    public <T> TypeAdapter<T> create(Gson context, TypeToken<T> typeToken) {
      Type type = typeToken.getType();
      JsonSerializer<T> serializer = (JsonSerializer<T>) serializers.getHandlerFor(type);
      JsonDeserializer<T> deserializer = (JsonDeserializer<T>) deserializers.getHandlerFor(type);
      return (serializer != null || deserializer != null)
          ? new TreeTypeAdapter<T>(serializer, deserializer, context, typeToken, this)
          : null;
    }
  }
}
