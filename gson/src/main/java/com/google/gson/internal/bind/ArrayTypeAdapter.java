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
import com.google.gson.internal.GsonTypes;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.ArrayList;

/** Adapter for arrays. */
public final class ArrayTypeAdapter<E> extends TypeAdapter<Object> {
  public static final TypeAdapterFactory FACTORY =
      new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
          Type type = typeToken.getType();
          if (!(type instanceof GenericArrayType
              || (type instanceof Class && ((Class<?>) type).isArray()))) {
            return null;
          }

          Type componentType = GsonTypes.getArrayComponentType(type);
          TypeAdapter<?> componentTypeAdapter = gson.getAdapter(TypeToken.get(componentType));

          @SuppressWarnings({"unchecked", "rawtypes"})
          TypeAdapter<T> arrayAdapter =
              new ArrayTypeAdapter(gson, componentTypeAdapter, GsonTypes.getRawType(componentType));
          return arrayAdapter;
        }
      };

  private final Class<E> componentType;
  private final TypeAdapter<E> componentTypeAdapter;

  public ArrayTypeAdapter(
      Gson context, TypeAdapter<E> componentTypeAdapter, Class<E> componentType) {
    this.componentTypeAdapter =
        new TypeAdapterRuntimeTypeWrapper<>(context, componentTypeAdapter, componentType);
    this.componentType = componentType;
  }

  @Override
  public Object read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }

    ArrayList<E> list = new ArrayList<>();
    in.beginArray();
    while (in.hasNext()) {
      E instance = componentTypeAdapter.read(in);
      if (instance == null && componentType.isPrimitive()) {
        throw new JsonSyntaxException(
            "Null value is not allowed in "
                + componentType
                + "[] array; at path "
                + in.getPreviousPath());
      }
      list.add(instance);
    }
    in.endArray();

    int size = list.size();
    // Have to copy primitives one by one to primitive array
    if (componentType.isPrimitive()) {
      if (componentType == int.class) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
          Object value = list.get(i);
          if (value instanceof Integer) {
            array[i] = (Integer) value;
          } else {
            Array.set(array, i, value);
          }
        }
        return array;
      }
      if (componentType == long.class) {
        long[] array = new long[size];
        for (int i = 0; i < size; i++) {
          Object value = list.get(i);
          if (value instanceof Long) {
            array[i] = (Long) value;
          } else {
            Array.set(array, i, value);
          }
        }
        return array;
      }
      if (componentType == double.class) {
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
          Object value = list.get(i);
          if (value instanceof Double) {
            array[i] = (Double) value;
          } else {
            Array.set(array, i, value);
          }
        }
        return array;
      }
      Object array = Array.newInstance(componentType, size);
      for (int i = 0; i < size; i++) {
        Array.set(array, i, list.get(i));
      }
      return array;
    }
    // But for Object[] can use ArrayList.toArray
    else {
      @SuppressWarnings("unchecked")
      E[] array = (E[]) Array.newInstance(componentType, size);
      return list.toArray(array);
    }
  }

  @Override
  public void write(JsonWriter out, Object array) throws IOException {
    if (array == null) {
      out.nullValue();
      return;
    }

    out.beginArray();
    if (array instanceof Object[]) {
      for (Object value : (Object[]) array) {
        writeElement(out, value);
      }
    } else if (array instanceof int[]) {
      for (int value : (int[]) array) {
        writeElement(out, value);
      }
    } else if (array instanceof long[]) {
      for (long value : (long[]) array) {
        writeElement(out, value);
      }
    } else if (array instanceof double[]) {
      for (double value : (double[]) array) {
        writeElement(out, value);
      }
    } else {
      for (int i = 0, length = Array.getLength(array); i < length; i++) {
        writeElement(out, Array.get(array, i));
      }
    }
    out.endArray();
  }

  private void writeElement(JsonWriter out, Object value) throws IOException {
    @SuppressWarnings("unchecked")
    E element = (E) value;
    componentTypeAdapter.write(out, element);
  }
}
