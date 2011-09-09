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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A basic binding between JSON and Java objects.
 */
public final class MiniGson {
  /**
   * This thread local guards against reentrant calls to getAdapter(). In
   * certain object graphs, creating an adapter for a type may recursively
   * require an adapter for the same type! Without intervention, the recursive
   * lookup would stack overflow. We cheat by returning a proxy type adapter.
   * The proxy is wired up once the initial adapter has been created.
   */
  private final ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>> calls
      = new ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>>() {
    @Override protected Map<TypeToken<?>, FutureTypeAdapter<?>> initialValue() {
      return new HashMap<TypeToken<?>, FutureTypeAdapter<?>>();
    }
  };

  private final List<TypeAdapter.Factory> factories;

  private MiniGson(Builder builder) {
    List<TypeAdapter.Factory> factories = new ArrayList<TypeAdapter.Factory>();
    if (builder.addDefaultFactories) {
      factories.add(TypeAdapters.BOOLEAN_FACTORY);
      factories.add(TypeAdapters.INTEGER_FACTORY);
      factories.add(TypeAdapters.DOUBLE_FACTORY);
      factories.add(TypeAdapters.FLOAT_FACTORY);
      factories.add(TypeAdapters.LONG_FACTORY);
      factories.add(TypeAdapters.STRING_FACTORY);
    }
    factories.addAll(builder.factories);
    if (builder.addDefaultFactories) {
      factories.add(CollectionTypeAdapter.FACTORY);
      factories.add(StringToValueMapTypeAdapter.FACTORY);
      factories.add(ArrayTypeAdapter.FACTORY);
      factories.add(ObjectTypeAdapter.FACTORY);
      factories.add(ReflectiveTypeAdapter.FACTORY);
    }
    this.factories = Collections.unmodifiableList(factories);
  }

  /**
   * Returns the type adapter for {@code} type.
   *
   * @throws IllegalArgumentException if this GSON cannot serialize and
   *     deserialize {@code type}.
   */
  public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
    // TODO: create a cache!

    Map<TypeToken<?>, FutureTypeAdapter<?>> threadCalls = calls.get();
    @SuppressWarnings("unchecked") // the key and value type parameters always agree
    FutureTypeAdapter<T> ongoingCall = (FutureTypeAdapter<T>) threadCalls.get(type);
    if (ongoingCall != null) {
      return ongoingCall;
    }

    FutureTypeAdapter<T> call = new FutureTypeAdapter<T>();
    threadCalls.put(type, call);
    try {
      for (TypeAdapter.Factory factory : factories) {
        TypeAdapter<T> candidate = factory.create(this, type);
        if (candidate != null) {
          call.setDelegate(candidate);
          return candidate;
        }
      }
      throw new IllegalArgumentException("This MiniGSON cannot serialize " + type);
    } finally {
      threadCalls.remove(type);
    }
  }

  static class FutureTypeAdapter<T> extends TypeAdapter<T> {
    private TypeAdapter<T> delegate;

    public void setDelegate(TypeAdapter<T> typeAdapter) {
      if (delegate != null) {
        throw new AssertionError();
      }
      delegate = typeAdapter;
    }

    @Override public T read(JsonReader reader) throws IOException {
      if (delegate == null) {
        throw new IllegalStateException();
      }
      return delegate.read(reader);
    }

    @Override public void write(JsonWriter writer, T value) throws IOException {
      if (delegate == null) {
        throw new IllegalStateException();
      }
      delegate.write(writer, value);
    }
  }

  /**
   * Returns the type adapter for {@code} type.
   *
   * @throws IllegalArgumentException if this GSON cannot serialize and
   *     deserialize {@code type}.
   */
  public <T> TypeAdapter<T> getAdapter(Class<T> type) {
    return getAdapter(TypeToken.get(type));
  }

  /**
   * Returns the type adapters of this context in order of precedence.
   */
  public List<TypeAdapter.Factory> getFactories() {
    return factories;
  }

  public static final class Builder {
    private final List<TypeAdapter.Factory> factories = new ArrayList<TypeAdapter.Factory>();
    boolean addDefaultFactories = true;

    public Builder factory(TypeAdapter.Factory factory) {
      factories.add(factory);
      return this;
    }

    public Builder withoutDefaultFactories() {
      this.addDefaultFactories = false;
      return this;
    }

    public <T> Builder typeAdapter(final Class<T> type, final TypeAdapter<T> typeAdapter) {
      factories.add(TypeAdapters.newFactory(type, typeAdapter));
      return this;
    }

    public <T> Builder typeAdapter(TypeToken<T> type, TypeAdapter<T> typeAdapter) {
      factories.add(TypeAdapters.newFactory(type, typeAdapter));
      return this;
    }

    public <T> Builder typeHierarchyAdapter(Class<T> type, TypeAdapter<T> typeAdapter) {
      factories.add(TypeAdapters.newTypeHierarchyFactory(type, typeAdapter));
      return this;
    }

    public MiniGson build() {
      return new MiniGson(this);
    }
  }
}
