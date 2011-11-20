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

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * This type adapter skips values using an exclusion strategy. It may delegate
 * to another type adapter if only one direction is excluded.
 */
public final class ExcludedTypeAdapterFactory implements TypeAdapter.Factory {
  private final ExclusionStrategy serializationExclusionStrategy;
  private final ExclusionStrategy deserializationExclusionStrategy;

  public ExcludedTypeAdapterFactory(ExclusionStrategy serializationExclusionStrategy,
      ExclusionStrategy deserializationExclusionStrategy) {
    this.serializationExclusionStrategy = serializationExclusionStrategy;
    this.deserializationExclusionStrategy = deserializationExclusionStrategy;
  }

  public <T> TypeAdapter<T> create(final Gson context, final TypeToken<T> type) {
    Class<?> rawType = type.getRawType();
    final boolean skipSerialize = serializationExclusionStrategy.shouldSkipClass(rawType);
    final boolean skipDeserialize = deserializationExclusionStrategy.shouldSkipClass(rawType);

    if (!skipSerialize && !skipDeserialize) {
      return null;
    }

    return new TypeAdapter<T>() {
      /** The delegate is lazily created because it may not be needed, and creating it may fail. */
      private TypeAdapter<T> delegate;

      @Override public T read(JsonReader reader) throws IOException {
        if (skipDeserialize) {
          reader.skipValue();
          return null;
        }
        return delegate().read(reader);
      }

      @Override public void write(JsonWriter writer, T value) throws IOException {
        if (skipSerialize) {
          writer.nullValue();
          return;
        }
        delegate().write(writer, value);
      }

      private TypeAdapter<T> delegate() {
        TypeAdapter<T> d = delegate;
        return d != null
            ? d
            : (delegate = context.getNextAdapter(ExcludedTypeAdapterFactory.this, type));
      }
    };
  }
}
