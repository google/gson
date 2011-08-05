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

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

final class TypeAdapterRuntimeTypeWrapper<T> extends TypeAdapter<T> {

  private final MiniGson context;
  private final TypeAdapter<T> delegate;
  private final Type type;

  TypeAdapterRuntimeTypeWrapper(MiniGson context, TypeAdapter<T> delegate, Type type) {
    this.context = context;
    this.delegate = delegate;
    this.type = type;
  }

  @Override
  public T read(JsonReader reader) throws IOException {
    return delegate.read(reader);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void write(JsonWriter writer, T value) throws IOException {
    Type runtimeType = Reflection.getRuntimeTypeIfMoreSpecific(type, value);
    TypeAdapter t = runtimeType != type ?
        context.getAdapter(TypeToken.get(runtimeType)) : delegate;
    t.write(writer, value);
  }
}
