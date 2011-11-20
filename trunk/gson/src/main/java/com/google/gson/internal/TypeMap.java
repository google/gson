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
public final class TypeMap<T> {
  private static final Logger logger = Logger.getLogger(TypeMap.class.getName());

  /** Map that is meant for storing default type adapters */
  private final Map<Type, T> typeMap = new HashMap<Type, T>();

  /** List of default type hierarchy adapters */
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
    int index = getIndexOfSpecificHandlerForTypeHierarchy(pair.first, typeHierarchyList);
    if (index != -1) {
      logger.log(Level.WARNING, "Overriding the existing type handler for {0}", pair.first);
      typeHierarchyList.remove(index);
    }
    index = getIndexOfAnOverriddenHandler(pair.first, typeHierarchyList);
    if (index != -1) {
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
    typeMap.put(typeOfT, value);
  }

  public synchronized TypeMap<T> makeUnmodifiable() {
    modifiable = false;
    return this;
  }

  public synchronized T getHandlerFor(Type type) {
    T handler = typeMap.get(type);
    if (handler != null) {
      return handler;
    }
    Class<?> rawClass = $Gson$Types.getRawType(type);
    if (rawClass != type) {
      handler = getHandlerFor(rawClass);
      if (handler != null) {
        return handler;
      }
    }
    // check if something registered for type hierarchy
    return getHandlerForTypeHierarchy(rawClass);
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
    return typeMap.containsKey(type);
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

  public synchronized TypeMap<T> copyOf() {
    TypeMap<T> copy = new TypeMap<T>();
    // Instead of individually registering entries in the map, make an efficient copy
    // of the list and map
    copy.typeMap.putAll(typeMap);
    copy.typeHierarchyList.addAll(typeHierarchyList);
    return copy;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{typeHierarchyList:{");
    appendList(sb, typeHierarchyList);
    sb.append("},typeMap:{");
    appendMap(sb, typeMap);
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
