/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A map that provides ability to associate handlers for a specific type or all
 * of its sub-types
 * 
 * @author Inderjeet Singh
 * @author Joel Leitch
 * 
 * @param <T> The handler that will be looked up by type
 */
final class ParameterizedTypeHandlerMap<T> {
  private static final Logger logger =
      Logger.getLogger(ParameterizedTypeHandlerMap.class.getName());
  private final Map<Type, T> map = new HashMap<Type, T>();
  private final List<Pair<Class<?>, T>> typeHierarchyList = new ArrayList<Pair<Class<?>, T>>();
  private boolean modifiable = true;

  public synchronized void registerForTypeHierarchy(Class<?> typeOfT, T value) {
    Pair<Class<?>, T> pair = new Pair<Class<?>, T>(typeOfT, value);
    registerForTypeHierarchy(pair);
  }

  public synchronized void registerForTypeHierarchy(Pair<Class<?>, T> pair) {
    if (!modifiable) {
      throw new IllegalStateException("Attempted to modify an unmodifiable map.");
    }
    int index = getIndexOfSpecificHandlerForTypeHierarchy(pair.first);
    if (index >= 0) {
      logger.log(Level.WARNING, "Overriding the existing type handler for {0}", pair.first);
      typeHierarchyList.remove(index);
    }
    index = getIndexOfAnOverriddenHandler(pair.first);
    if (index >= 0) {
      throw new IllegalArgumentException("The specified type handler for type " + pair.first
          + " hides the previously registered type hierarchy handler for "
          + typeHierarchyList.get(index).first + ". Gson does not allow this.");
    }
    // We want stack behavior for adding to this list. A type adapter added subsequently should
    // override a previously registered one.
    typeHierarchyList.add(0, pair);
  }

  private int getIndexOfAnOverriddenHandler(Class<?> type) {
    for (int i = typeHierarchyList.size()-1; i >= 0; --i) {
      Pair<Class<?>, T> entry = typeHierarchyList.get(i);
      if (type.isAssignableFrom(entry.first)) {
        return i;
      }
    }
    return -1;
  }

  public synchronized void register(Type typeOfT, T value) {
    if (!modifiable) {
      throw new IllegalStateException("Attempted to modify an unmodifiable map.");
    }
    if (hasSpecificHandlerFor(typeOfT)) {
      logger.log(Level.WARNING, "Overriding the existing type handler for {0}", typeOfT);
    }
    map.put(typeOfT, value);
  }

  public synchronized void registerIfAbsent(ParameterizedTypeHandlerMap<T> other) {
    if (!modifiable) {
      throw new IllegalStateException("Attempted to modify an unmodifiable map.");
    }
    for (Map.Entry<Type, T> entry : other.map.entrySet()) {
      if (!map.containsKey(entry.getKey())) {
        register(entry.getKey(), entry.getValue());
      }
    }
    // Quite important to traverse the typeHierarchyList from stack bottom first since
    // we want to register the handlers in the same order to preserve priority order
    for (int i = other.typeHierarchyList.size()-1; i >= 0; --i) {
      Pair<Class<?>, T> entry = other.typeHierarchyList.get(i);
      int index = getIndexOfSpecificHandlerForTypeHierarchy(entry.first);
      if (index < 0) {
        registerForTypeHierarchy(entry);
      }
    }
  }

  public synchronized void registerIfAbsent(Type typeOfT, T value) {
    if (!modifiable) {
      throw new IllegalStateException("Attempted to modify an unmodifiable map.");
    }
    if (!map.containsKey(typeOfT)) {
      register(typeOfT, value);
    }
  }

  public synchronized void makeUnmodifiable() {
    modifiable = false;
  }

  public synchronized T getHandlerFor(Type type) {
    T handler = map.get(type);
    if (handler == null) {
      Class<?> rawClass = TypeUtils.toRawClass(type);
      if (rawClass != type) {
        handler = getHandlerFor(rawClass);
      }
      if (handler == null) {
        // check if something registered for type hierarchy
        handler = getHandlerForTypeHierarchy(rawClass);
      }
    }
    return handler;
  }

  private T getHandlerForTypeHierarchy(Class<?> type) {
    for (Pair<Class<?>, T> entry : typeHierarchyList) {
      if (entry.first.isAssignableFrom(type)) {
        return entry.second;
      }
    }
    return null;
  }

  public synchronized boolean hasSpecificHandlerFor(Type type) {
    return map.containsKey(type);
  }

  private synchronized int getIndexOfSpecificHandlerForTypeHierarchy(Class<?> type) {
    for (int i = typeHierarchyList.size()-1; i >= 0; --i) {
      if (type.equals(typeHierarchyList.get(i).first)) {
        return i;
      }
    }
    return -1;
  }

  public synchronized ParameterizedTypeHandlerMap<T> copyOf() {
    ParameterizedTypeHandlerMap<T> copy = new ParameterizedTypeHandlerMap<T>();
    for (Map.Entry<Type, T> entry : map.entrySet()) {
      copy.register(entry.getKey(), entry.getValue());
    }
    for (Pair<Class<?>, T> entry : typeHierarchyList) {
      copy.registerForTypeHierarchy(entry);
    }
    return copy;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{mapForTypeHierarchy:{");
    boolean first = true;
    for (Pair<Class<?>, T> entry : typeHierarchyList) {
      if (first) {
        first = false;
      } else {
        sb.append(',');
      }
      sb.append(typeToString(entry.first)).append(':');
      sb.append(entry.second);
    }
    sb.append("},map:{");
    first = true;
    for (Map.Entry<Type, T> entry : map.entrySet()) {
      if (first) {
        first = false;
      } else {
        sb.append(',');
      }
      sb.append(typeToString(entry.getKey())).append(':');
      sb.append(entry.getValue());
    }
    sb.append("}");
    return sb.toString();
  }

  private String typeToString(Type type) {
    return TypeUtils.toRawClass(type).getSimpleName();
  }
}
