/*
 * Copyright (C) 2012 Google Inc.
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

package com.google.gson.interceptors;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * A type adapter factory that implements {@code @Intercept}.
 */
public final class InterceptorFactory implements TypeAdapterFactory {
  @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Intercept intercept = type.getRawType().getAnnotation(Intercept.class);
    if (intercept == null) {
      return null;
    }

    TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
    return new InterceptorAdapter<>(delegate, intercept);
  }

  static class InterceptorAdapter<T> extends TypeAdapter<T> {
    private final TypeAdapter<T> delegate;
    private final JsonPostDeserializer<T> postDeserializer;

    @SuppressWarnings("unchecked") // ?
    public InterceptorAdapter(TypeAdapter<T> delegate, Intercept intercept) {
      try {
        this.delegate = delegate;
        this.postDeserializer = intercept.postDeserialize().getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override public void write(JsonWriter out, T value) throws IOException {
      delegate.write(out, value);
    }

    @Override public T read(JsonReader in) throws IOException {
      T result = delegate.read(in);
      postDeserializer.postDeserialize(result);
      return result;
    }
  }
}
