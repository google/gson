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

package com.google.gson.graph;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Writes a graph of objects as a list of named nodes.
 */
// TODO: proper documentation
public final class GraphTypeAdapterFactory implements TypeAdapterFactory {
  private final ThreadLocal<Graph> graphThreadLocal = new ThreadLocal<Graph>();
  private final Set<Type> graphTypes;

  private GraphTypeAdapterFactory(Type... graphTypes) {
    this.graphTypes = new HashSet<Type>();
    this.graphTypes.addAll(Arrays.asList(graphTypes));
  }

  public static GraphTypeAdapterFactory of(Type... graphTypes) {
    return new GraphTypeAdapterFactory(graphTypes);
  }

  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    if (!graphTypes.contains(type.getType())) {
      return null;
    }

    final TypeAdapter<T> typeAdapter = gson.getNextAdapter(this, type);
    return new TypeAdapter<T>() {
      @Override public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
          out.nullValue();
          return;
        }

        Graph graph = graphThreadLocal.get();

        // this is the top-level object in the graph; write the whole graph recursively
        if (graph == null) {
          graph = new Graph();
          graphThreadLocal.set(graph);
          Element element = new Element<T>(value, graph.elements.size() + 1, typeAdapter);
          graph.elements.put(value, element);
          graph.queue.add(element);

          out.beginObject();
          Element current;
          while ((current = graph.queue.poll()) != null) {
            out.name(current.getName());
            current.write(out);
          }
          out.endObject();
          graphThreadLocal.remove();

        // this is an element nested in the graph; just reference it by ID
        } else {
          Element element = graph.elements.get(value);
          if (element == null) {
            element = new Element<T>(value, graph.elements.size() + 1, typeAdapter);
            graph.elements.put(value, element);
            graph.queue.add(element);
          }
          out.value(element.getName());
        }
      }

      @Override public T read(JsonReader in) throws IOException {
        // TODO:
        return null;
      }
    };
  }

  static class Graph {
    private final Map<Object, Element> elements = new IdentityHashMap<Object, Element>();
    private final Queue<Element> queue = new LinkedList<Element>();
  }

  static class Element<T> {
    private final T value;
    private final int id;
    private final TypeAdapter<T> typeAdapter;
    Element(T value, int id, TypeAdapter<T> typeAdapter) {
      this.value = value;
      this.id = id;
      this.typeAdapter = typeAdapter;
    }
    private String getName() {
      return "0x" + Integer.toHexString(id);
    }
    private void write(JsonWriter out) throws IOException {
      typeAdapter.write(out, value);
    }
  }
}
