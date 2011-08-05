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
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Adapt a homogeneous collection of objects.
 */
public final class CollectionTypeAdapter<E> extends TypeAdapter<Collection<E>> {
  public static final Factory FACTORY = new Factory() {
    public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
      Type type = typeToken.getType();

      Class<? super T> rawType = typeToken.getRawType();
      if (!Collection.class.isAssignableFrom(rawType)) {
        return null;
      }

      Type elementType = $Gson$Types.getCollectionElementType(type, rawType);
      TypeAdapter<?> elementTypeAdapter = context.getAdapter(TypeToken.get(elementType));

      Class<?> constructorType;

      if (rawType == List.class || rawType == Collection.class) {
        constructorType = ArrayList.class;
      } else if (rawType == Set.class) {
        constructorType = LinkedHashSet.class;
      } else {
        constructorType = rawType;
      }

      Constructor<?> constructor;
      try {
        constructor = constructorType.getConstructor();
      } catch (NoSuchMethodException e) {
        return null;
      }

      @SuppressWarnings("unchecked") // create() doesn't define a type parameter
      TypeAdapter<T> result = new CollectionTypeAdapter(context, elementType, elementTypeAdapter, constructor);
      return result;
    }
  };

  private final MiniGson context;
  private final Type elementType;
  private final TypeAdapter<E> elementTypeAdapter;
  private final Constructor<? extends Collection<E>> constructor;

  public CollectionTypeAdapter(MiniGson context, Type elementType, TypeAdapter<E> elementTypeAdapter,
      Constructor<? extends Collection<E>> constructor) {
    this.context = context;
    this.elementType = elementType;
    this.elementTypeAdapter = elementTypeAdapter;
    this.constructor = constructor;
  }

  public Collection<E> read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull(); // TODO: does this belong here?
      return null;
    }

    Collection<E> collection = Reflection.newInstance(constructor);
    reader.beginArray();
    while (reader.hasNext()) {
      E instance = elementTypeAdapter.read(reader);
      collection.add(instance);
    }
    reader.endArray();
    return collection;
  }

  public void write(JsonWriter writer, Collection<E> collection) throws IOException {
    if (collection == null) {
      writer.nullValue(); // TODO: better policy here?
      return;
    }

    writer.beginArray();
    for (E element : collection) {
      Type runtimeType = Reflection.getRuntimeTypeIfMoreSpecific(elementType, element);
      TypeAdapter t = runtimeType != elementType ?
          context.getAdapter(TypeToken.get(runtimeType)) : elementTypeAdapter;
      t.write(writer, element);
    }
    writer.endArray();
  }
}
