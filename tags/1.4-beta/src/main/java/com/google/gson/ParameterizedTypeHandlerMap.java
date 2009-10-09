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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A map that provides ability to associate handlers for a specific type or all
 * of its sub-types
 * 
 * @author Inderjeet Singh
 * @author Joel Leitch
 * 
 * @param <T>
 *          The handler that will be looked up by type
 */
final class ParameterizedTypeHandlerMap<T> {
  private static final Logger logger =
      Logger.getLogger(ParameterizedTypeHandlerMap.class.getName());
  private final Map<Type, T> map = new HashMap<Type, T>();
  private boolean modifiable = true;

  public synchronized void register(Type typeOfT, T value) {
    if (!modifiable) {
      throw new IllegalStateException(
          "Attempted to modify an unmodifiable map.");
    }
    if (hasSpecificHandlerFor(typeOfT)) {
      logger.log(Level.WARNING,
          "Overriding the existing type handler for " + typeOfT);
    }
    map.put(typeOfT, value);
  }

  public synchronized void registerIfAbsent(ParameterizedTypeHandlerMap<T> other) {
    if (!modifiable) {
      throw new IllegalStateException(
          "Attempted to modify an unmodifiable map.");
    }
    for (Map.Entry<Type, T> entry : other.entrySet()) {
      if (!map.containsKey(entry.getKey())) {
        register(entry.getKey(), entry.getValue());
      }
    }
  }

  public synchronized void registerIfAbsent(Type typeOfT, T value) {
    if (!modifiable) {
      throw new IllegalStateException(
          "Attempted to modify an unmodifiable map.");
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

      // Check for map or collection
      if (handler == null) {
        if (Map.class.isAssignableFrom(rawClass)) {
          handler = map.get(Map.class);
        } else if (Collection.class.isAssignableFrom(rawClass)) {
          handler = map.get(Collection.class);
        } else if (Enum.class.isAssignableFrom(rawClass)) {
          handler = map.get(Enum.class);
        }
      }
    }
    return handler;
  }

  public synchronized boolean hasSpecificHandlerFor(Type type) {
    return map.containsKey(type);
  }

  public synchronized ParameterizedTypeHandlerMap<T> copyOf() {
    ParameterizedTypeHandlerMap<T> copy = new ParameterizedTypeHandlerMap<T>();
    for (Map.Entry<Type, T> entry : map.entrySet()) {
      copy.register(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  public synchronized Set<Map.Entry<Type, T>> entrySet() {
    return map.entrySet();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
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
    return sb.toString();
  }

  private String typeToString(Type type) {
    return TypeUtils.toRawClass(type).getSimpleName();
  }
}
