/*
 * Copyright (C) 2026 Gson Authors
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

package com.google.gson.typeadapters;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Serializes {@link PriorityQueue} elements in their natural order without modifying the queue.
 * Gson's default collection adapter uses iteration order, which is unspecified for a priority
 * queue.
 *
 * <p>Register this factory with {@code new GsonBuilder().registerTypeAdapterFactory(new
 * PriorityQueueTypeAdapterFactory()).create()}. It handles {@code PriorityQueue} itself, not
 * subclasses. Queues with a custom comparator are rejected because the comparator cannot be
 * recovered during deserialization.
 *
 * <p>Serialization and deserialization delegate to the {@code List<E>} adapter, preserving the
 * queue's declared element type. Serialization sorts a copy of the elements, requiring additional
 * storage and sorting time. Deserialization creates a new queue using natural ordering.
 *
 * @since $next-version$
 */
public final class PriorityQueueTypeAdapterFactory implements TypeAdapterFactory {
  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    if (type.getRawType() != PriorityQueue.class) {
      return null;
    }

    Type queueType = type.getType();
    if (queueType instanceof WildcardType) {
      queueType = ((WildcardType) queueType).getUpperBounds()[0];
    }
    Type elementType =
        queueType instanceof ParameterizedType
            ? ((ParameterizedType) queueType).getActualTypeArguments()[0]
            : Object.class;
    TypeToken<?> listType = TypeToken.getParameterized(List.class, elementType);
    @SuppressWarnings("unchecked")
    TypeAdapter<List<Object>> listAdapter = (TypeAdapter<List<Object>>) gson.getAdapter(listType);
    @SuppressWarnings("unchecked")
    TypeAdapter<T> adapter = (TypeAdapter<T>) new Adapter(listAdapter).nullSafe();
    return adapter;
  }

  private static final class Adapter extends TypeAdapter<PriorityQueue<Object>> {
    private final TypeAdapter<List<Object>> listAdapter;

    Adapter(TypeAdapter<List<Object>> listAdapter) {
      this.listAdapter = listAdapter;
    }

    @Override
    public PriorityQueue<Object> read(JsonReader in) throws IOException {
      List<Object> elements = listAdapter.read(in);
      if (elements == null) {
        return null;
      }
      try {
        PriorityQueue<Object> queue = new PriorityQueue<>();
        queue.addAll(elements);
        return queue;
      } catch (NullPointerException | ClassCastException e) {
        throw new JsonSyntaxException(
            "PriorityQueue elements must be non-null and mutually comparable", e);
      }
    }

    @Override
    public void write(JsonWriter out, PriorityQueue<Object> queue) throws IOException {
      if (queue.comparator() != null) {
        throw new IllegalArgumentException(
            "PriorityQueue with a custom comparator is not supported");
      }
      Object[] elements = queue.toArray();
      Arrays.sort(elements);
      listAdapter.write(out, Arrays.asList(elements));
    }
  }
}
