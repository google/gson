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

package com.google.gson.mini;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A basic binding between JSON and Java objects.
 */
public final class MiniGson {
  private final List<TypeAdapter.Factory> factories;

  private MiniGson(Builder builder) {
    List<TypeAdapter.Factory> factories = new ArrayList<TypeAdapter.Factory>();
    factories.addAll(builder.factories);
    factories.add(TypeAdapters.BOOLEAN_FACTORY);
    factories.add(TypeAdapters.INTEGER_FACTORY);
    factories.add(TypeAdapters.DOUBLE_FACTORY);
    factories.add(TypeAdapters.LONG_FACTORY);
    factories.add(TypeAdapters.STRING_FACTORY);
    factories.add(ReflectiveTypeAdapter.FACTORY);
    factories.add(CollectionTypeAdapter.FACTORY);
    factories.add(StringToValueMapTypeAdapter.FACTORY);
    factories.add(ArrayTypeAdapter.FACTORY);
    this.factories = Collections.unmodifiableList(factories);
  }

  // TODO: this should use Joel's unsafe constructor stuff
  static <T> T newInstance(Constructor<T> constructor) {
    try {
      return constructor.newInstance();
    } catch (InstantiationException e) {
      // TODO: JsonParseException ?
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      // TODO: don't wrap if cause is unchecked!
      // TODO: JsonParseException ?
      throw new RuntimeException(e.getTargetException());
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Returns the type adapter for {@code} type.
   *
   * @throws IllegalArgumentException if this GSON cannot serialize and
   *     deserialize {@code type}.
   */
  public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
    // TODO: create a cache here
    for (TypeAdapter.Factory factory : factories) {
      TypeAdapter<T> candidate = factory.create(this, type);
      if (candidate != null) {
        return candidate;
      }
    }
    throw new IllegalArgumentException("This MiniGSON cannot serialize " + type);
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

    public Builder factory(TypeAdapter.Factory factory) {
      factories.add(factory);
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

    public <T> Builder typeHierarchyAdapter(TypeToken<T> type, TypeAdapter<T> typeAdapter) {
      factories.add(TypeAdapters.newTypeHierarchyFactory(type, typeAdapter));
      return this;
    }

    public MiniGson build() {
      return new MiniGson(this);
    }
  }
}
