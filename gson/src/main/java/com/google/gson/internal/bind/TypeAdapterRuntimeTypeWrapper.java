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

import java.io.IOException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

final class TypeAdapterRuntimeTypeWrapper<T> extends TypeAdapter<T> {
  private final Gson context;
  private final TypeAdapter<T> delegate;
  private final Type type;

  TypeAdapterRuntimeTypeWrapper(Gson context, TypeAdapter<T> delegate, Type type) {
    this.context = context;
    this.delegate = delegate;
    this.type = type;
  }

  @Override
  public T read(JsonReader in) throws IOException {
    return delegate.read(in);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void write(JsonWriter out, T value) throws IOException {
    // Order of preference for choosing type adapters
    // First preference: a type adapter registered for the runtime type
    // Second preference: a type adapter registered for the declared type
    // Third preference: reflective type adapter for the runtime type (if it is a sub class of the declared type)
    // Fourth preference: reflective type adapter for the declared type

    TypeAdapter chosen = delegate;
    Type runtimeType = getRuntimeTypeIfMoreSpecific(type, value);
    if (runtimeType != type) {
      TypeAdapter runtimeTypeAdapter = context.getAdapter(TypeToken.get(runtimeType));
      if (!isReflective(runtimeTypeAdapter)) {
        // The user registered a type adapter for the runtime type, so we will use that
        chosen = runtimeTypeAdapter;
      } else if (!isReflective(delegate)) {
        // The user registered a type adapter for Base class, so we prefer it over the
        // reflective type adapter for the runtime type
        chosen = delegate;
      } else {
        // Use the type adapter for runtime type
        chosen = runtimeTypeAdapter;
      }
    }
    chosen.write(out, value);
  }

  /**
   * @param typeAdapter the type adapter to check.
   * @return whether the type adapter uses reflection.
   */
  private static boolean isReflective(TypeAdapter<?> typeAdapter) {
    if (typeAdapter instanceof ReflectiveTypeAdapterFactory.Adapter) {
      return true;
    } else if (typeAdapter instanceof TreeTypeAdapter) {
      // Have to get actual serializing adapter for TreeTypeAdapter because
      // TreeTypeAdapter without `serializer` might fall back to reflective
      // type adapter
      TreeTypeAdapter<?> treeTypeAdapter = (TreeTypeAdapter<?>) typeAdapter;
      TypeAdapter<?> serializingTypeAdapter = treeTypeAdapter.getSerializingTypeAdapter();
      if (serializingTypeAdapter == treeTypeAdapter) {
        return false;
      } else {
        // Check TreeTypeAdapter delegate
        return isReflective(serializingTypeAdapter);
      }
    }
    return false;
  }

  /**
   * Finds a compatible runtime type if it is more specific
   */
  private static Type getRuntimeTypeIfMoreSpecific(Type type, Object value) {
    if (value != null && (type instanceof Class<?> || type instanceof TypeVariable<?>)) {
      type = value.getClass();
    }
    return type;
  }
}
