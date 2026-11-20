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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.GsonTypes;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

/** Adapt a homogeneous collection of objects. */
public final class CollectionTypeAdapterFactory implements TypeAdapterFactory {
  private final ConstructorConstructor constructorConstructor;

  public CollectionTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
  }

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    if (!Collection.class.isAssignableFrom(rawType)) {
      return null;
    }

    Type elementType = GsonTypes.getCollectionElementType(type, rawType);
    TypeAdapter<?> elementTypeAdapter = gson.getAdapter(TypeToken.get(elementType));
    TypeAdapter<?> wrappedTypeAdapter =
        new TypeAdapterRuntimeTypeWrapper<>(gson, elementTypeAdapter, elementType);
    // Don't allow Unsafe usage to create instance; instances might be in broken state and calling
    // Collection methods could lead to confusing exceptions
    boolean allowUnsafe = false;
    ObjectConstructor<T> constructor = constructorConstructor.get(typeToken, allowUnsafe);

    @SuppressWarnings({"unchecked", "rawtypes"}) // create() doesn't define a type parameter
    TypeAdapter<T> result = new Adapter(wrappedTypeAdapter, constructor);
    return result;
  }

  private static final class Adapter<E> extends TypeAdapter<Collection<E>> {
    private final TypeAdapter<E> elementTypeAdapter;
    private final ObjectConstructor<? extends Collection<E>> constructor;

    Adapter(
        TypeAdapter<E> elementTypeAdapter, ObjectConstructor<? extends Collection<E>> constructor) {
      this.elementTypeAdapter = elementTypeAdapter;
      this.constructor = constructor;
    }

    @Override
    public Collection<E> read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      Collection<E> collection = constructor.construct();
      in.beginArray();
      while (in.hasNext()) {
        E instance = elementTypeAdapter.read(in);
        if (instance == null) {
          // Most collections accept null, but some (TreeSet, ArrayDeque, PriorityQueue, EnumSet...)
          // reject it with a NullPointerException. Surface that as Gson's documented
          // JsonSyntaxException. Only the null case is wrapped, so a NullPointerException from
          // anywhere else is not masked.
          try {
            collection.add(null);
          } catch (NullPointerException e) {
            throw new JsonSyntaxException(
                "null is not a valid element for "
                    + collection.getClass().getName()
                    + "; at path "
                    + in.getPreviousPath(),
                e);
          }
        } else {
          collection.add(instance);
        }
      }
      in.endArray();
      return collection;
    }

    @Override
    public void write(JsonWriter out, Collection<E> collection) throws IOException {
      if (collection == null) {
        out.nullValue();
        return;
      }

      out.beginArray();
      for (E element : collection) {
        elementTypeAdapter.write(out, element);
      }
      out.endArray();
    }
  }
}
