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

package com.google.gson.internal;

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
public final class ParameterizedTypeHandlerMap<T> {

  private static final Logger logger =
      Logger.getLogger(ParameterizedTypeHandlerMap.class.getName());

  /** Map that is meant for storing default type adapters */
  private final Map<Type, T> userMap = new HashMap<Type, T>();

  /** List of default type hierarchy adapters */
  private final List<Pair<Class<?>, T>> userTypeHierarchyList = new ArrayList<Pair<Class<?>, T>>();
  private boolean modifiable = true;

  public synchronized void registerForTypeHierarchy(Class<?> typeOfT, T value) {
    Pair<Class<?>, T> pair = new Pair<Class<?>, T>(typeOfT, value);
    registerForTypeHierarchy(pair);
  }

  public synchronized void registerForTypeHierarchy(Pair<Class<?>, T> pair) {
    if (!modifiable) {
      throw new IllegalStateException("Attempted to modify an unmodifiable map.");
    }
    List<Pair<Class<?>, T>> typeHierarchyList = userTypeHierarchyList;
    int index = getIndexOfSpecificHandlerForTypeHierarchy(pair.first, typeHierarchyList);
    if (index >= 0) {
      logger.log(Level.WARNING, "Overriding the existing type handler for {0}", pair.first);
      typeHierarchyList.remove(index);
    }
    index = getIndexOfAnOverriddenHandler(pair.first, typeHierarchyList);
    if (index >= 0) {
      throw new IllegalArgumentException("The specified type handler for type " + pair.first
          + " hides the previously registered type hierarchy handler for "
          + typeHierarchyList.get(index).first + ". Gson does not allow this.");
    }
    // We want stack behavior for adding to this list. A type adapter added subsequently should
    // override a previously registered one.
    typeHierarchyList.add(0, pair);
  }

  private static <T> int getIndexOfAnOverriddenHandler(Class<?> type, List<Pair<Class<?>, T>> typeHierarchyList) {
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
    Map<Type, T> map = userMap;
    map.put(typeOfT, value);
  }

  public synchronized void registerIfAbsent(ParameterizedTypeHandlerMap<T> other) {
    if (!modifiable) {
      throw new IllegalStateException("Attempted to modify an unmodifiable map.");
    }
    for (Map.Entry<Type, T> entry : other.userMap.entrySet()) {
      if (!userMap.containsKey(entry.getKey())) {
        register(entry.getKey(), entry.getValue());
      }
    }
    // Quite important to traverse the typeHierarchyList from stack bottom first since
    // we want to register the handlers in the same order to preserve priority order
    for (int i = other.userTypeHierarchyList.size()-1; i >= 0; --i) {
      Pair<Class<?>, T> entry = other.userTypeHierarchyList.get(i);
      int index = getIndexOfSpecificHandlerForTypeHierarchy(entry.first, userTypeHierarchyList);
      if (index < 0) {
        registerForTypeHierarchy(entry);
      }
    }
  }

  public synchronized ParameterizedTypeHandlerMap<T> makeUnmodifiable() {
    modifiable = false;
    return this;
  }

  public synchronized T getHandlerFor(Type type, boolean systemOnly) {
    T handler;
    if (!systemOnly) {
      handler = userMap.get(type);
      if (handler != null) {
        return handler;
      }
    }
    Class<?> rawClass = $Gson$Types.getRawType(type);
    if (rawClass != type) {
      handler = getHandlerFor(rawClass, systemOnly);
      if (handler != null) {
        return handler;
      }
    }
    // check if something registered for type hierarchy
    handler = getHandlerForTypeHierarchy(rawClass, systemOnly);
    return handler;
  }

  private T getHandlerForTypeHierarchy(Class<?> type, boolean systemOnly) {
    if (!systemOnly) {
      for (Pair<Class<?>, T> entry : userTypeHierarchyList) {
        if (entry.first.isAssignableFrom(type)) {
          return entry.second;
        }
      }
    }
    return null;
  }

  public synchronized boolean hasSpecificHandlerFor(Type type) {
    return userMap.containsKey(type);
  }

  private static <T> int getIndexOfSpecificHandlerForTypeHierarchy(
      Class<?> type, List<Pair<Class<?>, T>> typeHierarchyList) {
    for (int i = typeHierarchyList.size()-1; i >= 0; --i) {
      if (type.equals(typeHierarchyList.get(i).first)) {
        return i;
      }
    }
    return -1;
  }

  public synchronized ParameterizedTypeHandlerMap<T> copyOf() {
    ParameterizedTypeHandlerMap<T> copy = new ParameterizedTypeHandlerMap<T>();
    // Instead of individually registering entries in the map, make an efficient copy
    // of the list and map

    // TODO (inder): Performance optimization. We can probably just share the
    // systemMap and systemTypeHierarchyList instead of making copies
    copy.userMap.putAll(userMap);
    copy.userTypeHierarchyList.addAll(userTypeHierarchyList);
    return copy;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{userTypeHierarchyList:{");
    appendList(sb, userTypeHierarchyList);
    sb.append("},userMap:{");
    appendMap(sb, userMap);
    sb.append("}");
    return sb.toString();
  }

  private void appendList(StringBuilder sb, List<Pair<Class<?>,T>> list) {
    boolean first = true;
    for (Pair<Class<?>, T> entry : list) {
      if (first) {
        first = false;
      } else {
        sb.append(',');
      }
      sb.append(typeToString(entry.first)).append(':');
      sb.append(entry.second);
    }
  }

  private void appendMap(StringBuilder sb, Map<Type, T> map) {
    boolean first = true;
    for (Map.Entry<Type, T> entry : map.entrySet()) {
      if (first) {
        first = false;
      } else {
        sb.append(',');
      }
      sb.append(typeToString(entry.getKey())).append(':');
      sb.append(entry.getValue());
    }
  }

  private String typeToString(Type type) {
    return $Gson$Types.getRawType(type).getSimpleName();
  }
}
