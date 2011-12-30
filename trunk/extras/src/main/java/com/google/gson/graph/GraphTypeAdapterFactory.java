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
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
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
    final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
    return new TypeAdapter<T>() {
      @Override public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
          out.nullValue();
          return;
        }

        Graph graph = graphThreadLocal.get();
        boolean writeEntireGraph = false;

        if (graph == null) {
          writeEntireGraph = true;
          graph = new Graph(new IdentityHashMap<Object, Element<?>>());
        }

        Element<T> element = (Element<T>) graph.map.get(value);
        if (element == null) {
          element = new Element<T>(value, graph.nextName(), typeAdapter, null);
          graph.map.put(value, element);
          graph.queue.add(element);
        }

        if (writeEntireGraph) {
          graphThreadLocal.set(graph);
          try {
            out.beginObject();
            Element<?> current;
            while ((current = graph.queue.poll()) != null) {
              out.name(current.id);
              current.write(out);
            }
            out.endObject();
          } finally {
            graphThreadLocal.remove();
          }
        } else {
          out.value(element.id);
        }
      }

      @Override public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }

        String currentName = null;
        Graph graph = graphThreadLocal.get();
        boolean readEntireGraph = false;

        if (graph == null) {
          graph = new Graph(new HashMap<Object, Element<?>>());
          readEntireGraph = true;

          // read the entire tree into memory
          in.beginObject();
          while (in.hasNext()) {
            String name = in.nextName();
            if (currentName == null) {
              currentName = name;
            }
            JsonElement element = elementAdapter.read(in);
            graph.map.put(name, new Element<T>(null, name, typeAdapter, element));
          }
          in.endObject();
        } else {
          currentName = in.nextString();
        }

        if (readEntireGraph) {
          graphThreadLocal.set(graph);
        }
        try {
          Element<T> element = (Element<T>) graph.map.get(currentName);
          if (element.value == null) {
            element.typeAdapter = typeAdapter;
            element.read();
          }
          return element.value;
        } finally {
          if (readEntireGraph) {
            graphThreadLocal.remove();
          }
        }
      }
    };
  }

  static class Graph {
    /**
     * The graph elements. On serialization keys are objects (using an identity
     * hash map) and on deserialization keys are the string names (using a
     * standard hash map).
     */
    private final Map<Object, Element<?>> map;
    private final Queue<Element> queue = new LinkedList<Element>();

    private Graph(Map<Object, Element<?>> map) {
      this.map = map;
    }

    /**
     * Returns a unique name for an element to be inserted into the graph.
     */
    public String nextName() {
      return "0x" + Integer.toHexString(map.size() + 1);
    }
  }

  static class Element<T> {
    private final String id;
    private T value;
    private TypeAdapter<T> typeAdapter;
    private final JsonElement element;
    private boolean reading = false;

    Element(T value, String id, TypeAdapter<T> typeAdapter, JsonElement element) {
      this.value = value;
      this.id = id;
      this.typeAdapter = typeAdapter;
      this.element = element;
    }

    private void write(JsonWriter out) throws IOException {
      typeAdapter.write(out, value);
    }

    private void read() throws IOException {
      if (reading) {
        // TODO: this currently fails because we don't have the instance we want yet
        System.out.println("ALREADY READING " + id);
        return;
      }
      reading = true;
      try {
        // TODO: use TypeAdapter.fromJsonTree() when that's public
        value = typeAdapter.read(new JsonTreeReader(element));
        if (value == null) {
          throw new IllegalStateException("non-null value deserialized to null: " + element);
        }
      } finally {
        reading = false;
      }
    }
  }
}
